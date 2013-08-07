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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import self.philbrown.droidQuery.AjaxTask.TaskResponse;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;

import com.commonsware.cwac.task.AsyncTaskEx;

/**
 * Asynchronously performs HTTP Requests
 * @author Phil Brown
 */
public class AjaxTask extends AsyncTaskEx<Void, Void, TaskResponse>
{
	/** Options used to configure this task */
	private AjaxOptions options;
	/** The HTTP Request to perform */
	private HttpUriRequest request = null;
	
	/** Used for synchronous operations. */
	private static Semaphore mutex = new Semaphore(1);
	/** Contains the current non-global tasks */
	private static volatile List<AjaxTask> localTasks = new ArrayList<AjaxTask>();
	/** Contains the current global tasks */
	private static volatile List<AjaxTask> globalTasks = new ArrayList<AjaxTask>();
	/** Represents a cached HTTP response */
	class CachedResponse
	{
		/** The response Object */
		public Object response;
		/** The Last-Requested timestamp */
		public Date timestamp;
		/** The Last-Modified timestamp */
		public Date lastModified;
	}
	/** 
	 * Keeps track of the responses made by each URL. This cache will be used for caching and
	 * modified headers. 
	 */
	private static volatile Map<String, CachedResponse> URLresponses = new HashMap<String, CachedResponse>();
	
	/**
	 * Constructor
	 * @param options JSON representation of the Ajax Options
	 * @throws Exception if the JSON is malformed
	 */
	public AjaxTask(JSONObject options) throws Exception
	{
		this(new AjaxOptions(options));
	}
	
	/**
	 * Can be used to restart an Ajax Task
	 * @param request a request (to retry)
	 * @param options options for request retry.
	 */
	public AjaxTask(HttpUriRequest request, AjaxOptions options)
	{
		this(options);
		this.request = request;
	}
	
	/**
	 * Constructor
	 * @param options used to configure this task
	 */
	public AjaxTask(AjaxOptions options)
	{
		this.options = options;
		if (options.url() == null)
		{
			throw new NullPointerException("Cannot call Ajax with null URL!");
		}
		
	}
	
	/**
	 * Stops all currently running Ajax Tasks
	 */
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
			if (options.context() != null)
				options.beforeSend().invoke($.with(options.context()), options);
			else
				options.beforeSend().invoke(null, options);
		} 
		
		if (options.global())
			$.ajaxSend();
		
	}

	@Override
	protected TaskResponse doInBackground(Void... arg0) 
	{
		//handle cached responses
		CachedResponse cachedResponse = URLresponses.get(String.format(Locale.US, "%s_?=%s", options.url(), options.dataType()));
		//handle ajax caching option
		if (cachedResponse != null)
		{
			if (options.cache())
			{
				if (new Date().getTime() - cachedResponse.timestamp.getTime() < options.cacheTimeout())
				{
					//return cached response
					Success s = new Success();
					s.obj = cachedResponse.response;
					s.reason = "cached response";
					s.headers = null;
					return s;
				}
			}
			
		}
		
		if (request == null)
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
			
			HttpResponse response;
			
			if (options.cookies() != null)
			{
				CookieStore cookies = new BasicCookieStore();
				for (Entry<String, String> entry : options.cookies().entrySet())
				{
					cookies.addCookie(new BasicClientCookie(entry.getKey(), entry.getValue()));
				}
				HttpContext httpContext = new BasicHttpContext();
				httpContext.setAttribute(ClientContext.COOKIE_STORE, cookies);
				response = client.execute(request, httpContext);
			}
			else
			{
				response = client.execute(request);
			}
			
			
			if (options.dataFilter() != null)
			{
				if (options.context() != null)
					options.dataFilter().invoke($.with(options.context()), response, options.dataType());
				else
					options.dataFilter().invoke(null, response, options.dataType());
			}
			
			StatusLine statusLine = response.getStatusLine();
			
			Function function = options.statusCode().get(statusLine);
			if (function != null)
			{
				if (options.context() != null)
					function.invoke($.with(options.context()));
				else
					function.invoke(null);
			}
			
			if (statusLine.getStatusCode() >= 300)
	        {
				//an error occurred
				Error e = new Error();
				AjaxError error = new AjaxError();
				error.request = request;
				error.options = options;
				e.status = statusLine.getStatusCode();
				e.reason = statusLine.getReasonPhrase();
				error.status = e.status;
				error.reason = e.reason;
				e.headers = response.getAllHeaders();
				e.error = error;
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
						if (options.customXMLParser() != null)
						{
							InputStream is = response.getEntity().getContent();
							if (options.SAXContentHandler() != null)
								options.customXMLParser().parse(is, options.SAXContentHandler());
							else
								options.customXMLParser().parse(is, new DefaultHandler());
							parsedResponse = "Response handled by custom SAX parser";
						}
						else if (options.SAXContentHandler() != null)
						{
							InputStream is = response.getEntity().getContent();
							
							SAXParserFactory factory = SAXParserFactory.newInstance();
							
							factory.setFeature("http://xml.org/sax/features/namespaces", false);
							factory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
							
							SAXParser parser = factory.newSAXParser();
							
							XMLReader reader = parser.getXMLReader();
							reader.setContentHandler(options.SAXContentHandler());
							reader.parse(new InputSource(is));
							parsedResponse = "Response handled by custom SAX content handler";
						}
						else
						{
							parsedResponse = parseXML(response);
						}
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
					if (options.debug())
						cpe.printStackTrace();
					success = false;
					Error e = new Error();
					AjaxError error = new AjaxError();
					error.request = request;
					error.options = options;
					e.status = statusLine.getStatusCode();
					e.reason = statusLine.getReasonPhrase();
					error.status = e.status;
					error.reason = e.reason;
					e.headers = response.getAllHeaders();
					e.error = error;
					return e;
				}
				catch (Exception ioe)
				{
					if (options.debug())
						ioe.printStackTrace();
					success = false;
					Error e = new Error();
					AjaxError error = new AjaxError();
					error.request = request;
					error.options = options;
					e.status = statusLine.getStatusCode();
					e.reason = statusLine.getReasonPhrase();
					error.status = e.status;
					error.reason = e.reason;
					e.headers = response.getAllHeaders();
					e.error = error;
					return e;
				}
				if (success)
				{
					//Handle cases where successful requests still return errors (these include
					//configurations in AjaxOptions and HTTP Headers
					String key = String.format(Locale.US, "%s_?=%s", options.url(), options.dataType());
					CachedResponse cache = URLresponses.get(key);
					Date now = new Date();
					//handle ajax caching option
					if (cache != null)
					{
						if (options.cache())
						{
							if (now.getTime() - cache.timestamp.getTime() < options.cacheTimeout())
							{
								parsedResponse = cache;
							}
							else
							{
								cache.response = parsedResponse;
								cache.timestamp = now;
								synchronized(URLresponses) {
									URLresponses.put(key, cache);
								}
							}
						}
						
					}
					//handle ajax ifModified option
					Header[] lastModifiedHeaders = response.getHeaders("last-modified");
					if (lastModifiedHeaders.length >= 1) {
						try
						{
							Header h = lastModifiedHeaders[0];
							SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
							Date lastModified = format.parse(h.getValue());
							if (options.ifModified() && lastModified != null)
							{
								if (cache.lastModified != null && cache.lastModified.compareTo(lastModified) == 0)
								{
									//request response has not been modified. 
									//Causes an error instead of a success.
									Error e = new Error();
									AjaxError error = new AjaxError();
									error.request = request;
									error.options = options;
									e.status = statusLine.getStatusCode();
									e.reason = statusLine.getReasonPhrase();
									error.status = e.status;
									error.reason = e.reason;
									e.headers = response.getAllHeaders();
									e.error = error;
									Function func = options.statusCode().get(304);
									if (func != null)
									{
										if (options.context() != null)
											func.invoke($.with(options.context()));
										else
											func.invoke(null);
									}
									return e;
								}
								else
								{
									cache.lastModified = lastModified;
									synchronized(URLresponses) {
										URLresponses.put(key, cache);
									}
								}
							}
						}
						catch (Throwable t)
						{
							Log.e("Ajax", "Could not parse Last-Modified Header", t);
						}
						
					}
					
					//Now handle a successful request
					
					Success s = new Success();
					s.obj = parsedResponse;
					s.reason = statusLine.getReasonPhrase();
					s.headers = response.getAllHeaders();
					return s;
				}
				//success
				Success s = new Success();
				s.obj = parsedResponse;
				s.reason = statusLine.getReasonPhrase();
				s.headers = response.getAllHeaders();
				return s;
			}
			
		} catch (Throwable t) {
			if (options.debug())
				t.printStackTrace();
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
				AjaxError error = new AjaxError();
				error.request = request;
				error.status = 0;
				error.options = options;
				error.reason = "null response";
				//invoke error with Request, Status, and Error
				if (options.context() != null)
					options.error().invoke($.with(options.context()), error, 0, "null response");
				else
					options.error().invoke(null, error, 0, "null response");
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
				if (options.context() != null)
					options.error().invoke($.with(options.context()), e.error, e.status, e.reason);
				else
					options.error().invoke(null, e.error, e.status, e.reason);
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
				if (options.context() != null)
					options.success().invoke($.with(options.context()), s.obj, s.reason);
				else
					options.success().invoke(null, s.obj, s.reason);
			}
			
			if (options.global())
				$.ajaxSuccess();
		}
		
		
		if (options.complete() != null)
		{
			if (response != null)
			{
				if (options.context() != null)
					options.complete().invoke($.with(options.context()), response.reason);
				else
					options.complete().invoke(null, response.reason);
			}
			else
			{
				if (options.context() != null)
					options.complete().invoke($.with(options.context()), "null response");
				else
					options.complete().invoke(null, "null response");
			}
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
	 * Parses the HTTP response as JSON representation
	 * @param response the response to parse
	 * @return a JSONObject response
	 */
	private Object parseJSON(HttpResponse response) throws ClientProtocolException, IOException
	{
		JSONResponseHandler handler = new JSONResponseHandler();
		return handler.handleResponse(response);
	}
	
	/**
	 * Parses the HTTP response as XML representation
	 * @param response the response to parse
	 * @return an XML Document response
	 */
	private Document parseXML(HttpResponse response) throws ClientProtocolException, IOException
	{
		XMLResponseHandler handler = new XMLResponseHandler();
		return handler.handleResponse(response);
	}
	
	/**
	 * Parses the HTTP response as Text
	 * @param response the response to parse
	 * @return a String response
	 */
	private String parseText(HttpResponse response) throws ClientProtocolException, IOException
	{
		BasicResponseHandler handler = new BasicResponseHandler();
		return handler.handleResponse(response);
	}
	
	/**
	 * Parses the HTTP response as a Script, then runs it.
	 * @param response the response to parse
	 * @return a ScriptResponse Object containing the output String, if any, as well as the original
	 * Script
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
	
	/**
	 * Parses the HTTP response as a Bitmap
	 * @param response the response to parse
	 * @return a Bitmap response
	 */
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
	
	/**
	 * Defines a response to a Task
	 * @see Error
	 * @see Success
	 */
	class TaskResponse {
		/** The reason text */
		public String reason;
		/** The status ID */
		public int status;
		/** The response Headers. If a cached response is returned, {@code headers} will be {@code null}. */
		public Header[] headers;
	}
	
	/**
	 * Response for tasks that run into an error or exception
	 */
	class Error extends TaskResponse
	{
		/** The response Object */
		public AjaxError error;
	}
	
	/**
	 * Response for tasks that complete successfully
	 */
	class Success extends TaskResponse
	{
		/** The response Object */
		public Object obj;
	}
	
	/**
	 * This is the first object that is returned when an Error occurs for an Ajax Request
	 * @see AjaxTask#AjaxTask(HttpUriRequest, AjaxOptions)
	 */
	public static class AjaxError
	{
		/** The original request */
		public HttpUriRequest request;
		/** The original options */
		public AjaxOptions options;
		/** The error status code */
		public int status;
		/** The error string */
		public String reason;
	}
}