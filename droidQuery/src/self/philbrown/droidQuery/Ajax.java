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
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.LockSupport;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import self.philbrown.droidQuery.AjaxOptions.Redundancy;
import self.philbrown.droidQuery.AjaxTask.AjaxError;
import self.philbrown.droidQuery.AjaxTask.Error;
import self.philbrown.droidQuery.AjaxTask.Success;
import self.philbrown.droidQuery.AjaxTask.TaskResponse;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;

/**
 * Ajax
 * TODO Description
 * <br>
 * @author Phil Brown
 * @since 4:56:37 PM, Jan 5, 2015
 *
 */
public class Ajax {
	
	private AjaxOptions options;
	private HttpURLConnection connection;
	/** Used to run functions in the thread in which this task was started. */
	private Handler mHandler;
	/** 
	 * This value is set in {@link #onPreExecute()} then accessed in
	 * {@link #onPostExecute(TaskResponse)}, and is used to properly handle redundancy checking.
	 */
	private Redundancy redundancyType = Redundancy.DO_NOTHING;
	
	/** Used for synchronous operations. */
	private static Semaphore mutex = new Semaphore(1);
	/** 
	 * Used to ensure beforeSend Function is not called twice if the user changes the async status in
	 * beforeSend() when the original options object is set to not async.
	 */
	private boolean beforeSendIsAsync = true;
	/**
	 * {@code true} if the current thread is locked. This is used for synchronous requests.
	 */
	private volatile boolean isLocked = false;
	
	private boolean isCancelled = false;
	
	/** Contains all AjaxOptions for current tasks. This is used to handle redundancy.*/
	private static volatile Map<String, AjaxOptions> redundancyHelper = new HashMap<String, AjaxOptions>();
	/** Used to keep track of the last modified dates for specific URLs */
	private static volatile Map<String, Date> lastModifiedUrls = new HashMap<String, Date>();

	/**
     * Executor service handling request threads.
     */
    private ExecutorService executor;

	/** Contains the current non-global tasks */
	private static volatile List<Ajax> localTasks = new ArrayList<Ajax>();
	/** Contains the current global tasks */
	private static volatile List<Ajax> globalTasks = new ArrayList<Ajax>();
	
	/**
	 * Constructor
	 * @param options JSON representation of the Ajax Options
	 * @throws Exception if the JSON is malformed
	 */
	public Ajax(JSONObject options) throws Exception
	{
		this(new AjaxOptions(options));
	}
	
	/**
	 * Can be used to restart an Ajax Task
	 * @param connection a request (to retry)
	 * @param options options for request retry.
	 */
	public Ajax(HttpURLConnection connection, AjaxOptions options)
	{
		this(options);
		this.connection = connection;
	}
	
	/**
	 * Constructor
	 * @param options used to configure this task
	 */
	public Ajax(AjaxOptions options)
	{
		this.options = options;
		if (options.url() == null)
		{
			throw new NullPointerException("Cannot call Ajax with null URL!");
		}
		this.mHandler = new Handler();
		this.executor = Executors.newFixedThreadPool(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable runnable) {
				Thread t = new Thread(runnable);
				t.setPriority(Ajax.this.options.priority());
				return t;
			}
		});
	}
	
	/**
	 * Run the Ajax Request
	 */
	public void execute() {
		try {
			onPreExecute();
			executor.execute(new Runnable() {

				@Override
				public void run() {
					final TaskResponse response = doInBackground();
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							onPostExecute(response);
						}
					});
				}
				
			});
		} catch (Exception e) {
			if (options.debug())
				e.printStackTrace();
		}
	}
	
	/**
	 * Cancel the executor
	 * @param now	{@code true} to cancel immediately. {@false} to allow existing tasks to complete, but don't allow new ones to begin.
	 */
	public void cancel(boolean now) {
		if (now)
			executor.shutdownNow();
		else
			executor.shutdown();
		isCancelled = true;
	}
	
	/**
	 * Stops all currently running Ajax Tasks
	 */
	public static void killTasks()
	{
		for (int i = 0; i < globalTasks.size(); i++) {
			globalTasks.get(i).cancel(true);
		}
		for (int i = 0; i < localTasks.size(); i++) {
			localTasks.get(i).cancel(true);
		}
		
		globalTasks.clear();
		localTasks.clear();
		$.ajaxStop();
	}

	protected void onPreExecute()
	{
		//handle redundacy options
		redundancyType = options.redundancy();
		if (redundancyType != null)
		{
			switch(redundancyType)
			{
				case DO_NOTHING :
					break;
				case ABORT_REDUNDANT_REQUESTS :
					synchronized(redundancyHelper)
					{
						if (this.isRedundant())
						{
							cancel(true);
							return;
						}
						else
						{
							String key = String.format(Locale.US, "%s::%s::%s::%s", options.dataType(), (options.type() == null ? "GET" : options.type()), options.url(), (options.data() == null ? "" : options.data().toString()));
							redundancyHelper.put(key, options);
						}
					}
					break;
				case RESPOND_TO_ALL_LISTENERS :
					synchronized(redundancyHelper)
					{
						String key = String.format(Locale.US, "%s::%s::%s::%s", options.dataType(), (options.type() == null ? "GET" : options.type()), options.url(), (options.data() == null ? "" : options.data().toString()));
						AjaxOptions taskOptions = redundancyHelper.get(key);
						if (taskOptions != null)
						{
							//add this options' callbacks to the callbacks for the request already taking place.
							synchronized(taskOptions)
							{
								if (options.success() != null)
								{
									final Function _success = taskOptions.success();
									taskOptions.success(new Function() {
										
										@Override
										public void invoke($ droidQuery, Object... params) {
											if (_success != null)
												_success.invoke(droidQuery, params);
											options.success().invoke(droidQuery, params);
										}
									});
								}
								if (options.error() != null)
								{
									final Function _error = taskOptions.error();
									taskOptions.error(new Function() {
										
										@Override
										public void invoke($ droidQuery, Object... params) {
											if (_error != null)
												_error.invoke(droidQuery, params);
											options.error().invoke(droidQuery, params);
										}
									});
								}
								if (options.complete() != null)
								{
									final Function _complete = taskOptions.complete();
									taskOptions.complete(new Function() {
										
										@Override
										public void invoke($ droidQuery, Object... params) {
											if (_complete != null)
												_complete.invoke(droidQuery, params);
											options.complete().invoke(droidQuery, params);
										}
									});
								}
							}
						}
						else
						{
							redundancyHelper.put(key, options);
						}
					}
					break;
			}
		}
		
		beforeSendIsAsync = options.async();
		if (options.async())
		{
			if (options.beforeSend() != null)
			{
				if (options.context() != null)
					options.beforeSend().invoke($.with(options.context()), options);
				else
					options.beforeSend().invoke(null, options);
			}
			
			if (options.isAborted())
			{
				cancel(true);
				return;
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
				$.ajaxSend();
			}
			else
			{
				synchronized(localTasks)
				{
					localTasks.add(this);
				}
			}
		}
		
	}

	protected TaskResponse doInBackground(Void... arg0) 
	{
		if (this.isCancelled)
			return null;
		
		//if synchronous, block on the background thread until ready. Then call beforeSend, etc, before resuming.
		if (!beforeSendIsAsync)
		{
			try {
				mutex.acquire();
			} catch (InterruptedException e) {
				Log.w("AjaxTask", "Synchronization Error. Running Task Async");
			}
			final Thread asyncThread = Thread.currentThread();
			isLocked = true;
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (options.beforeSend() != null)
					{
						if (options.context() != null)
							options.beforeSend().invoke($.with(options.context()), options);
						else
							options.beforeSend().invoke(null, options);
					}
					
					if (options.isAborted())
					{
						cancel(true);
						return;
					}
					
					if (options.global())
					{
						synchronized(globalTasks)
						{
							if (globalTasks.isEmpty())
							{
								$.ajaxStart();
							}
							globalTasks.add(Ajax.this);
						}
						$.ajaxSend();
					}
					else
					{
						synchronized(localTasks)
						{
							localTasks.add(Ajax.this);
						}
					}
					isLocked = false;
					LockSupport.unpark(asyncThread);
				}
			});
			if (isLocked)
				LockSupport.park();
		}
		
		
		//here is where to use the mutex
		
		//handle cached responses
		Object cachedResponse = AjaxCache.sharedCache().getCachedResponse(options);
		//handle ajax caching option
		if (cachedResponse != null && options.cache())
		{
			Success s = new Success(cachedResponse);
			s.reason = "cached response";
			s.headers = null;
			return s;
			
		}
		
		if (connection == null)
		{
			String type = options.type();
			URL url = new URL(options.url());
			if (type == null) {
				type = "GET";
			}
			if (type.equalsIgnoreCase("CUSTOM")) {
				
				try
				{
					connection = options.customConnection();
				}
				catch (Exception e)
				{
					connection = null;
				}
				
				if (connection == null)
				{
					Log.w("droidQuery.ajax", "CUSTOM type set, but AjaxOptions.customRequest is invalid. Defaulting to GET.");
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
				}
			}
			else {
	            connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod(type);
			}
		}
		
		//TODO add SSL support
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("options", options);
		args.put("request", null);
		args.put("connection", connection);
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
				connection.setRequestProperty(entry.getKey(), entry.getValue());
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
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		if (options.trustAllSSLCertificates())
		{
			X509HostnameVerifier hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
			SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
			socketFactory.setHostnameVerifier(hostnameVerifier);
			schemeRegistry.register(new Scheme("https", socketFactory, 443));
			Log.w("Ajax", "Warning: All SSL Certificates have been trusted!");
		}
		else if (options.trustSomeSSLCerts()) {
			//TODO
		}
		else
		{
			schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
		}
		
		SingleClientConnManager mgr = new SingleClientConnManager(params, schemeRegistry);
		HttpClient client = new DefaultHttpClient(mgr, params);
		
		HttpResponse response = null;
		try {
			
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
			
			final StatusLine statusLine = response.getStatusLine();
			
			final Function function = options.statusCode().get(statusLine.getStatusCode());
			if (function != null)
			{
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						if (options.context() != null)
							function.invoke($.with(options.context()), statusLine.getStatusCode(), options.clone());
						else
							function.invoke(null, statusLine.getStatusCode(), options.clone());
					}
					
				});
				
			}
			
			//handle dataType
			String dataType = options.dataType();
			if (dataType == null)
				dataType = "text";
			if (options.debug())
				Log.i("Ajax", "dataType = " + dataType);
			Object parsedResponse = null;
			try
			{
				if (dataType.equalsIgnoreCase("text") || dataType.equalsIgnoreCase("html"))
				{
					if (options.debug())
						Log.i("Ajax", "parsing text");
					parsedResponse = parseText(response);
				}
				else if (dataType.equalsIgnoreCase("xml"))
				{
					if (options.debug())
						Log.i("Ajax", "parsing xml");
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
						parsedResponse = AjaxTask.parseXML(response);
					}
				}
				else if (dataType.equalsIgnoreCase("json"))
				{
					if (options.debug())
						Log.i("Ajax", "parsing json");
					parsedResponse = AjaxTask.parseJSON(response);
				}
				else if (dataType.equalsIgnoreCase("script"))
				{
					if (options.debug())
						Log.i("Ajax", "parsing script");
					parsedResponse = parseScript(response);
				}
				else if (dataType.equalsIgnoreCase("image"))
				{
					if (options.debug())
						Log.i("Ajax", "parsing image");
					parsedResponse = parseImage(response);
				}
				else if (dataType.equalsIgnoreCase("raw"))
				{
					if (options.debug())
						Log.i("Ajax", "parsing raw data");
					parsedResponse = AjaxTask.parseRawContent(response);
				}
			}
			catch (ClientProtocolException cpe)
			{
				if (options.debug())
					cpe.printStackTrace();
				Error e = new Error(parsedResponse);
				AjaxError error = new AjaxError();
				error.connection = connection;
				error.options = options;
				e.status = statusLine.getStatusCode();
				e.reason = statusLine.getReasonPhrase();
				error.status = e.status;
				error.reason = e.reason;
				error.response = e.response;
				e.headers = response.getAllHeaders();
				e.error = error;
				return e;
			}
			catch (Exception ioe)
			{
				if (options.debug())
					ioe.printStackTrace();
				Error e = new Error(parsedResponse);
				AjaxError error = new AjaxError();
				error.connection = connection;
				error.options = options;
				e.status = statusLine.getStatusCode();
				e.reason = statusLine.getReasonPhrase();
				error.status = e.status;
				error.reason = e.reason;
				error.response = e.response;
				e.headers = response.getAllHeaders();
				e.error = error;
				return e;
			}
			
			if (statusLine.getStatusCode() >= 300)
	        {
				//an error occurred
				Error e = new Error(parsedResponse);
				Log.e("Ajax Test", parsedResponse.toString());
				//AjaxError error = new AjaxError();
				//error.request = request;
				//error.options = options;
				e.status = statusLine.getStatusCode();
				e.reason = statusLine.getReasonPhrase();
				//error.status = e.status;
				//error.reason = e.reason;
				//error.response = e.response;
				e.headers = response.getAllHeaders();
				//e.error = error;
				if (options.debug())
					Log.i("Ajax", "Error " + e.status + ": " + e.reason);
				return e;
	        }
			else
			{
				//handle ajax ifModified option
				Header[] lastModifiedHeaders = response.getHeaders("last-modified");
				if (lastModifiedHeaders.length >= 1) {
					try
					{
						Header h = lastModifiedHeaders[0];
						SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
						Date lastModified = format.parse(h.getValue());
						if (options.ifModified() && lastModified != null)
						{
							Date lastModifiedDate;
							synchronized(lastModifiedUrls)
							{
								lastModifiedDate = lastModifiedUrls.get(options.url());
							}
							
							if (lastModifiedDate != null && lastModifiedDate.compareTo(lastModified) == 0)
							{
								//request response has not been modified. 
								//Causes an error instead of a success.
								Error e = new Error(parsedResponse);
								AjaxError error = new AjaxError();
								error.connection = connection;
								error.options = options;
								e.status = statusLine.getStatusCode();
								e.reason = statusLine.getReasonPhrase();
								error.status = e.status;
								error.reason = e.reason;
								error.response = e.response;
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
								synchronized(lastModifiedUrls)
								{
									lastModifiedUrls.put(options.url(), lastModified);
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
				
				Success s = new Success(parsedResponse);
				s.reason = statusLine.getReasonPhrase();
				s.headers = response.getAllHeaders();
				return s;
			}
			
		} catch (Throwable t) {
			if (options.debug())
				t.printStackTrace();
			if (t instanceof java.net.SocketTimeoutException)
			{
				Error e = new Error(null);
				AjaxError error = new AjaxError();
				error.connection = connection;
				error.options = options;
				error.response = e.response;
				e.status = 0;
				String reason = t.getMessage();
				if (reason == null)
					reason = "Socket Timeout";
				e.reason = reason;
				error.status = e.status;
				error.reason = e.reason;
				if (response != null)
					e.headers = response.getAllHeaders();
				else
					e.headers = new Header[0];
				e.error = error;
				return e;
			}
			return null;
		}
	}
	
	public void onPostExecute(TaskResponse response)
	{
		if (!options.async())
		{
			mutex.release();
		}
		if (response == null)
		{

			if (options.debug())
				Log.w("Ajax", "null response");
			
			if (this.isCancelled)
				return;
			
			if (options.error() != null)
			{
				AjaxError error = new AjaxError();
				error.connection = connection;
				error.status = 0;
				error.options = options;
				error.reason = "null response";
				error.response = null;
				//invoke error with Request, Status, and Error
				if (options.context() != null)
					options.error().invoke($.with(options.context()), error, 0, "null response", null);
				else
					options.error().invoke(null, error, 0, "null response", null);
			}
			
			if (options.global())
				$.ajaxError();
		}
		else if (response instanceof Error)
		{
			if (options.error() != null)
			{
				Error e = (Error) response;
				AjaxError error = new AjaxError();
				error.connection = connection;
				error.status = e.status;
				error.options = options;
				error.reason = e.reason;
				error.response = e.response;

				if (options.debug())
					Log.i("Ajax", error.toString());
				
				//invoke error with Request, Status, and Error
				if (options.context() != null)
					options.error().invoke($.with(options.context()), error, e.status, e.reason, e.headers);
				else
					options.error().invoke(null, error, e.status, e.reason, e.headers);
			}
			
			if (options.global())
				$.ajaxError();
		}
		else if (response instanceof Success)
		{
			Success s = (Success) response;
			if (options.cache())
				AjaxCache.sharedCache().cacheResponse(s.response, options);
			if (options.success() != null)
			{
				//invoke success with parsed response and the status string
				if (options.context() != null)
					options.success().invoke($.with(options.context()), s.response, s.reason, s.headers);
				else
					options.success().invoke(null, s.response, s.reason, s.headers);
			}
			
			if (options.global())
				$.ajaxSuccess();
		}
		
		if (options.complete() != null)
		{
			if (response != null)
			{
				if (options.context() != null)
					options.complete().invoke($.with(options.context()), options, response.reason, response.headers);
				else
					options.complete().invoke(null, options, response.reason, response.headers);
			}
			else
			{
				if (options.context() != null)
					options.complete().invoke($.with(options.context()), options, "null response", null);
				else
					options.complete().invoke(null, options, "null response", null);
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
		
		//remove request from redundancy helper
		if (redundancyType != null)
		{
			switch(redundancyType)
			{
				case DO_NOTHING :
					break;
				case ABORT_REDUNDANT_REQUESTS :
				case RESPOND_TO_ALL_LISTENERS :
					synchronized(redundancyHelper)
					{
						String key = String.format(Locale.US, "%s::%s::%s::%s", options.dataType(), (options.type() == null ? "GET" : options.type()), options.url(), (options.data() == null ? "" : options.data().toString()));
						redundancyHelper.remove(key);
					}
					break;
			}
		}
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
	 * Checks to see if a request is redundant. This is only used for Redundancy Types 
	 * {@link AjaxOptions.Redundancy#ABORT_REDUNDANT_REQUESTS} and {@link AjaxOptions.Redundancy#RESPOND_TO_ALL_LISTENERS}.
	 * @return {@code true} if the same request is already taking place. Otherwise {@code false}.
	 */
	private boolean isRedundant()
	{
		String key = String.format(Locale.US, "%s::%s::%s::%s", options.dataType(), (options.type() == null ? "GET" : options.type()), options.url(), (options.data() == null ? "" : options.data().toString()));
		return redundancyHelper.containsKey(key);
	}
}
