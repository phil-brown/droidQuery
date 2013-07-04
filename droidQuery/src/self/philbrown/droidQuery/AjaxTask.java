/*
 * Copyright 2013 Phil Brown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package self.philbrown.droidQuery;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;
import org.w3c.dom.Document;

import self.philbrown.droidQuery.AjaxTask.TaskResponse;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;

import com.commonsware.cwac.task.AsyncTaskEx;

/**
 * Main driver behind droidQuery/Ajax.
 * @author Phil Brown
 *
 */
public class AjaxTask extends AsyncTaskEx<Void, Void, TaskResponse>
{
	private AjaxOptions options;
	private HttpUriRequest request = null;
	
	/** Used for synchronous operations. */
	private static Semaphore mutex = new Semaphore(1);
	/** Contains the current non-global tasks */
	private static volatile List<AjaxTask> localTasks = new ArrayList<AjaxTask>();
	/** Contains the current global tasks */
	private static volatile List<AjaxTask> globalTasks = new ArrayList<AjaxTask>();
	
	public AjaxTask(JSONObject options) throws Exception
	{
		this(new AjaxOptions(options));
	}
	
	public AjaxTask(AjaxOptions options)
	{
		this.options = options;
		if (options.url() == null)
		{
			throw new NullPointerException("Cannot call Ajax with null URL!");
		}
		
	}
	
	public static void killTasks()
	{
		for (AjaxTask task : globalTasks) {
			task.cancel(true);
		}
		for (AjaxTask task : localTasks) {
			task.cancel(true);
		}
		globalTasks.clear();
		localTasks.clear();
		$.ajaxStop();
		
	}
	
	@Override
	protected void onPreExecute()
	{
		if (!options.async())
		{
			try {
				mutex.acquire();
			} catch (InterruptedException e) {
				Log.w("AjaxTask", "Synchronization Error. Running Task Async");
			}
		}
		
		if (options.global())
		{
			synchronized(globalTasks)
			{
				if (globalTasks.isEmpty())
				{
					$.ajaxStart();
				}
				globalTasks.add(this);
			}
		}
		else
		{
			synchronized(localTasks)
			{
				localTasks.add(this);
			}
		}
		
		if (options.beforeSend() != null)
		{
			options.beforeSend().invoke(options);
		} 
		
		if (options.global())
			$.ajaxSend();
		
	}

	@Override
	protected TaskResponse doInBackground(Void... arg0) 
	{
		
		String type = options.type();
		if (type == null)
			type = "GET";
		if (type.equalsIgnoreCase("DELETE"))
		{
			request = new HttpDelete(options.url());
		}
		else if (type.equalsIgnoreCase("GET"))
		{
			request = new HttpGet(options.url());
		}
		else if (type.equalsIgnoreCase("HEAD"))
		{
			request = new HttpHead(options.url());
		}
		else if (type.equalsIgnoreCase("OPTIONS"))
		{
			request = new HttpOptions(options.url());
		}
		else if (type.equalsIgnoreCase("POST"))
		{
			request = new HttpPost(options.url());
		}
		else if (type.equalsIgnoreCase("PUT"))
		{
			request = new HttpPut(options.url());
		}
		else if (type.equalsIgnoreCase("TRACE"))
		{
			request = new HttpTrace(options.url());
		}
		else if (type.equalsIgnoreCase("CUSTOM"))
		{
			try
			{
				request = options.customRequest();
			}
			catch (Exception e)
			{
				request = null;
			}
			
			if (request == null)
			{
				Log.w("droidQuery.ajax", "CUSTOM type set, but AjaxOptions.customRequest is invalid. Defaulting to GET.");
				request = new HttpGet();
			}
			
		}
		else
		{
			//default to GET
			request = new HttpGet();
		}
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("options", options);
		args.put("request", request);
		EventCenter.trigger("ajaxPrefilter", args, null);
		
		if (options.headers() != null)
		{
			if (options.headers().authorization() != null)
			{
				options.headers().authorization(options.headers().authorization() + " " + options.getEncodedCredentials());
			}
			else if (options.username() != null)
			{
				//guessing that authentication is basic
				options.headers().authorization("Basic " + options.getEncodedCredentials());
			}
			
			for (Entry<String, String> entry : options.headers().map().entrySet())
			{
				request.addHeader(entry.getKey(), entry.getValue());
			}
		}
		
		if (options.data() != null)
		{
			try
			{
				Method setEntity = request.getClass().getMethod("setEntity", new Class<?>[]{HttpEntity.class});
				if (options.processData() == null)
				{
					setEntity.invoke(request, new StringEntity(options.data().toString()));
				}
				else
				{
					Class<?> dataProcessor = Class.forName(options.processData());
					Constructor<?> constructor = dataProcessor.getConstructor(new Class<?>[]{Object.class});
					setEntity.invoke(request, constructor.newInstance(options.data()));
				}
			}
			catch (Throwable t)
			{
				Log.w("Ajax", "Could not post data");
			}
		}
		
		HttpParams params = new BasicHttpParams();
		
		if (options.timeout() != 0)
		{
			HttpConnectionParams.setConnectionTimeout(params, options.timeout());
			HttpConnectionParams.setSoTimeout(params, options.timeout());
		}
		
		HttpClient client = new DefaultHttpClient(params);
		
		
		try {
			HttpResponse response = client.execute(request);
			
			if (options.dataFilter() != null)
			{
				options.dataFilter().invoke(response, options.dataType());
			}
			
			StatusLine statusLine = response.getStatusLine();
			
			Function function = options.statusCode().get(statusLine);
			if (function != null)
			{
				function.invoke();
			}
			
			if (statusLine.getStatusCode() >= 300)
	        {
				//an error occurred
				Error e = new Error();
				e.obj = request;
				e.status = statusLine.getStatusCode();
				e.reason = statusLine.getReasonPhrase();
				return e;
	        }
			else
			{
				//handle dataType
				String dataType = options.dataType();
				if (dataType == null)
					dataType = "text";
				Object parsedResponse = null;
				boolean success = true;
				try
				{
					if (dataType.equalsIgnoreCase("text") || dataType.equalsIgnoreCase("html"))
					{
						parsedResponse = parseText(response);
					}
					else if (dataType.equalsIgnoreCase("xml"))
					{
						parsedResponse = parseXML(response);
					}
					else if (dataType.equalsIgnoreCase("json"))
					{
						parsedResponse = parseJSON(response);
					}
					else if (dataType.equalsIgnoreCase("script"))
					{
						parsedResponse = parseScript(response);
					}
					else if (dataType.equalsIgnoreCase("image"))
					{
						parsedResponse = parseImage(response);
					}
				}
				catch (ClientProtocolException cpe)
				{
					cpe.printStackTrace();
					success = false;
					Error e = new Error();
					e.obj = request;
					e.status = 0;
					e.reason = "parsererror";
					return e;
				}
				catch (Exception ioe)
				{
					ioe.printStackTrace();
					success = false;
					Error e = new Error();
					e.obj = request;
					e.status = 0;
					e.reason = "parsererror";
					return e;
				}
				if (success)
				{
					//success
					Success s = new Success();
					s.obj = parsedResponse;
					s.reason = statusLine.getReasonPhrase();
					return s;
				}
				//success
				Success s = new Success();
				s.obj = parsedResponse;
				s.reason = statusLine.getReasonPhrase();
				return s;
			}
			
		} catch (Throwable t) {
			return null;
		}
	}
	
	@Override
	public void onPostExecute(TaskResponse response)
	{
		if (!options.async())
		{
			mutex.release();
		}
		if (response == null)
		{
			if (options.error() != null)
			{
				//invoke error with Request, Status, and Error
				options.error().invoke(request, 0, "null response");
			}
			
			if (options.global())
				$.ajaxError();
		}
		else if (response instanceof Error)
		{
			if (options.error() != null)
			{
				//invoke error with Request, Status, and Error
				Error e = (Error) response;
				options.error().invoke(e.obj, e.status, e.reason);
			}
			
			if (options.global())
				$.ajaxError();
		}
		else if (response instanceof Success)
		{
			Success s = (Success) response;
			if (options.success() != null)
			{
				//invoke success with parsed response and the status string
				options.success().invoke(s.obj, s.reason);
			}
			
			if (options.global())
				$.ajaxSuccess();
		}
		
		
		if (options.complete() != null)
		{
			if (response != null)
				options.complete().invoke(response.reason);
			else
				options.complete().invoke("null response");
		}
		if (options.global())
			$.ajaxComplete();
		
		if (options.global())
		{
			synchronized(globalTasks)
			{
				globalTasks.remove(this);
				if (globalTasks.isEmpty())
				{
					$.ajaxStop();
				}
			}
		}
		else
		{
			synchronized(localTasks)
			{
				localTasks.remove(this);
			}
		}
	}
	
	/**
	 * Parses and returns the JSONObject
	 * @param response
	 * @return
	 */
	private JSONObject parseJSON(HttpResponse response) throws ClientProtocolException, IOException
	{
		JSONResponseHandler handler = new JSONResponseHandler();
		return handler.handleResponse(response);
	}
	
	/**
	 * parses and returns a Document Object
	 * @param response
	 * @return
	 */
	private Document parseXML(HttpResponse response) throws ClientProtocolException, IOException
	{
		XMLResponseHandler handler = new XMLResponseHandler();
		return handler.handleResponse(response);
	}
	
	/**
	 * Parses and returns a String Object (Default)
	 * @param response
	 * @return
	 */
	private String parseText(HttpResponse response) throws ClientProtocolException, IOException
	{
		BasicResponseHandler handler = new BasicResponseHandler();
		return handler.handleResponse(response);
	}
	
	/**
	 * Parses a Script, and runs it. Then returns the output String, if any.
	 * @param response
	 * @return
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	private ScriptResponse parseScript(HttpResponse response) throws ClientProtocolException, IOException
	{
		if (options.context() != null)
		{
			ScriptResponseHandler handler = new ScriptResponseHandler(options.context());
			return handler.handleResponse(response);
		}
		else
		{
			throw new NullPointerException("No context provided.");
		}
	}
	
	private Bitmap parseImage(HttpResponse response) throws IllegalStateException, IOException
	{
		InputStream is = response.getEntity().getContent();
    	BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inSampleSize = 1;
		opt.inPurgeable = true;
		opt.inInputShareable = false;
		if (options.imageWidth() >= 0)
			opt.outWidth = options.imageWidth();
		if (options.imageHeight() >= 0)
			opt.outHeight = options.imageHeight();
		WeakReference<Bitmap> bitmap = new WeakReference<Bitmap>(BitmapFactory.decodeStream(is, new Rect(0,0,0,0), opt));
		
		if (bitmap == null || bitmap.get() == null)
		{
			return null;
		}
		
		if (bitmap.get().isRecycled())
		{
			return null;
		}
        
        is.close();
        return bitmap.get();
	}
	
	class TaskResponse {
		public String reason;
		public int status;
		public Object obj;
	}
	
	class Error extends TaskResponse
	{
		
	}
	
	class Success extends TaskResponse
	{
		
	}
}