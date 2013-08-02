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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

/**
 * Build an Ajax Request
 * @author Phil Brown
 */
public class AjaxOptions 
{
	
	/** Used privately for reflection */
	private static Method[] methods = AjaxOptions.class.getMethods();
	/** Used privately for reflection */
	private static Map<String, Method> setters = new HashMap<String, Method>();
	static
	{
		for (Method m : methods)
		{
			if (m.getTypeParameters().length != 0)
				setters.put(m.getName(), m);
		}
	}
	
	/** Used for reflection */
	private static Field[] fields = AjaxOptions.class.getFields();
	/** global ajax options. This is set if $.ajaxSetup is called. */
	private static AjaxOptions globalOptions;
	
	/**
	 * The content type sent in the request header that tells the server what kind of response
	 * it will accept in return.
	 */
	private String accepts;
	/**
	 * Get the content type sent in the request header that tells the server what kind of response
	 * it will accept in return.
	 */
	public String accepts(){ return accepts; }
	/**
	 * Set the content type sent in the request header that tells the server what kind of response
	 * it will accept in return.
	 */
	public AjaxOptions accepts(String accepts)
	{
		this.accepts = accepts;
		headers.accept(accepts);
		return this;
	}
	
	/**
	 * By default, all requests are sent asynchronously (i.e. this is set to true by default). 
	 * If you need synchronous requests, set this option to false.
	 */
	private boolean async = true;//use this to still do in background, but don't allow other tasks to run (queue it up!)
	/**
	 * Get the asynchronous nature of the Task.
	 * @return {@code true} if the task should be asynchronous (default). {@code false} Otherwise.
	 */
	public boolean async() { return async; }
	/**
	 * Set the asynchronous nature of the Task.
	 * @param async {@code true} if the task should be asynchronous (default). {@code false} Otherwise.
	 * @return this
	 */
	public AjaxOptions async(boolean async)
	{
		this.async = async;
		return this;
	}
	
	/**
	 * A pre-request callback function. Receives these options as a parameter.
	 */
	private Function beforeSend;
	/**
	 * Gets the function that is registered to call before the Ajax Task begins
	 * @return the Function
	 */
	public Function beforeSend(){ return beforeSend; }
	/**
	 * Sets the function that is registered to call before the Ajax Task begins
	 * @param beforeSend the Function to call. This will receive a {@code null} Object for the
	 * <em>droidQuery</em> parameter unless {@link #context() context} is non-null. If that is
	 * the case, {@code beforeSend} will receive a <em>droidQuery</em> instance with that <em>context</em>.
	 * The varargs parameter will include these options, so that they can be manipulated. 
	 * @return this
	 */
	public AjaxOptions beforeSend(Function beforeSend)
	{
		this.beforeSend = beforeSend;
		return this;
	}
	
	/**
	 * A function to be called when the request finishes (after success and error callbacks are executed). 
	 * Receives the text status
	 */
	private Function complete;
	/**
	 * Gets the function that is registered to call when the task has completed
	 * @return the Function
	 */
	public Function complete() { return complete; }
	/**
	 * Sets the function that is registered to when the task has completed. The function will receive
	 * no varargs, 
	 * It will also receive a {@code null} Object for the
	 * <em>droidQuery</em> parameter unless {@link #context() context} is non-null. If that is
	 * the case, {@code complete} will receive a <em>droidQuery</em> instance with that <em>context</em>.
	 * @param complete the Function to call
	 * @return this
	 */
	public AjaxOptions complete(Function complete)
	{
		this.complete = complete;
		return this;
	}
	
	/**
	 * When sending data to the server, use this content type. 
	 * Default is "application/x-www-form-urlencoded; charset=UTF-8", which is fine for most cases. 
	 */
	private String contentType = "application/x-www-form-urlencoded; charset=UTF-8";
	/**
	 * Get the content type of the data sent to the server.
	 * Default is "application/x-www-form-urlencoded; charset=UTF-8", which is fine for most cases. 
	 * @return the content type
	 */
	public String contentType() { return contentType; }
	/**
	 * Set the content type of the data sent to the server.
	 * Default is "application/x-www-form-urlencoded; charset=UTF-8", which is fine for most cases.
	 * @param contentType
	 * @return this
	 */
	public AjaxOptions contentType(String contentType)
	{
		this.contentType = contentType;
		headers.content_type(contentType);
		return this;
	}
	
	/**
	 * Used to set get or change the current context
	 */
	private Context context;
	
	/**
	 * Get the context
	 * @return the context
	 */
	public Context context() { return context; }
	
	/**
	 * Set the context. Setting this to a non-{@code null} value will allow the callback Functions
	 * (such as {@link #success() success}, {@link #error() error}, {@link #beforeSend() beforeSend},
	 * and {@link #complete() complete}) to pass a non-{@code null} <em>droidQuery</em> instance.
	 * @param context
	 * @return this
	 */
	public AjaxOptions context(Context context)
	{
		this.context = context;
		return this;
	}
	
	/**
	 * Data to be sent to the server. Will be converted to String unless 
	 * {@link #processData() processData} is set to false.
	 */
	private Object data;
	
	/**
	 * Get the data to be sent to the server
	 * @return the data to be sent to the server
	 */
	public Object data() { return data; }
	
	/**
	 * Set the data to be sent to the server. Will be converted to String unless 
	 * {@link #processData() processData} is set to false.
	 * @param data
	 * @return this
	 */
	public AjaxOptions data(Object data)
	{
		this.data = data;
		return this;
	}
	
	/**
	 * If {@code true}, some Ajax Debug information will be provided in the logcat
	 */
	private boolean debug = false;
	/**
	 * Get whether or not Ajax debug output will be pushed to the logcat
	 * @return {@code true} if the debug information will be printed. Otherwise {@code false}
	 */
	public boolean debug() { return debug; }
	
	/**
	 * Sets whether or not Ajax debug output will be pushed to the logcat
	 * @param debug {@code true} if the debug information will be printed. Otherwise {@code false}. 
	 * Default is {@code false}.
	 * @return this
	 */
	public AjaxOptions debug(boolean debug)
	{
		this.debug = debug;
		return this;
	}

	/**
	 * A function to be used to handle the raw response data. This is a 
	 * pre-filtering function to sanitize the response. You should return the sanitized 
	 * data. The function accepts two arguments: The raw data returned from the server (HttpResponse) 
	 * and the 'dataType' parameter (String).
	 */
	private Function dataFilter;
	
	/**
	 * Get the function to be used to handle the raw response data.
	 * @return the function to be used to handle the raw response data.
	 */
	public Function dataFilter() { return dataFilter; }
	
	/**
	 * Set the function to be used to handle the raw response data. This is a 
	 * pre-filtering function to sanitize the response. You should return the sanitized 
	 * data. The function accepts two varargs: The raw data returned from the server (HttpResponse) 
	 * and the 'dataType' parameter (String). It will also receive a {@code null} Object for the
	 * <em>droidQuery</em> parameter unless {@link #context() context} is non-null. If that is
	 * the case, {@code dataFilter} will receive a <em>droidQuery</em> instance with that <em>context</em>.
	 * @param dataFilter
	 * @return this
	 */
	public AjaxOptions dataFilter(Function dataFilter)
	{
		this.dataFilter = dataFilter;
		return this;
	}
	
	/**
	 * The type of data that you're expecting back from the server. If none is specified, a String
	 * is returned.
	 * "xml": Returns a XML document that can be processed via droidQuery.
	 * "html": Returns HTML as plain text.
	 * "script": Evaluates the response as bourne (NOT bash) script and returns it as plain text. 
	 * "json": Evaluates the response as JSON and returns a JSONObject object. The JSON data is parsed in a strict manner; any malformed JSON is rejected and a parse error is thrown. (See json.org for more information on proper JSON formatting.)
	 * "text": A plain text string.
	 * "image" : returns a bitmap object
	 * @note if Script is used, {@link context} MUST be set.

	 */
	private String dataType = "text";
	
	/**
	 * Gets the type of the data that the request expects from the server
	 * @return the type of the data that the request expects from the server
	 */
	public String dataType() { return dataType; }
	
	/**
	 * Sets the type of the data that the request expects from the server. Can be one of:
	 * "xml": Returns a XML document that can be processed via droidQuery.
	 * "html": Returns HTML as plain text.
	 * "script": Evaluates the response as bourne (NOT bash) script and returns it as plain text. 
	 * "json": Evaluates the response as JSON and returns a JSONObject object. The JSON data is parsed in a strict manner; any malformed JSON is rejected and a parse error is thrown. (See json.org for more information on proper JSON formatting.)
	 * "text": A plain text string.
	 * "image" : returns a bitmap object
	 * @note if Script is used, {@link context} MUST be set.
	 * @param dataType
	 * @return this
	 */
	public AjaxOptions dataType(String dataType)
	{
		this.dataType = dataType;
		return this;
	}
	
	/**
	 * A function to be called if the request fails. Receives original Request, 
	 * the integer Status, and the String Error
	 */
	private Function error;

	/**
	 * Get the function to be called if the request fails. Receives original Request, 
	 * the integer Status, and the String Error
	 */
	public Function error() { return error; }

	/**
	 * Set the function to be called if the request fails. Receives an {@link AjaxTask.AjaxError}
	 * error, the integer Status, and the String Error for varargs. 
	 * It will also receive a {@code null} Object for the
	 * <em>droidQuery</em> parameter unless {@link #context() context} is non-null. If that is
	 * the case, {@code error} will receive a <em>droidQuery</em> instance with that <em>context</em>.

	 */
	public AjaxOptions error(Function error)
	{
		this.error = error;
		return this;
	}
	
	/**
	 * Whether to trigger global Ajax event handlers for this request. The default is {@code true}. 
	 * Set to {@code false} to prevent the global handlers like {@link $#ajaxStart() ajaxStart} or 
	 * {@link $#ajaxStop() ajaxStop} from being triggered. This can be used to control various 
	 * Ajax Events.
	 */
	private boolean global = true;
	
	/**
	 * Get whether to trigger global Ajax event handlers for this request.
	 * @return
	 */
	public boolean global() { return global; }
	
	/**
	 * Set whether to trigger global Ajax event handlers for this request. The default is {@code true}. 
	 * Set to {@code false} to prevent the global handlers like {@link $#ajaxStart() ajaxStart} or 
	 * {@link $#ajaxStop() ajaxStop} from being triggered. This can be used to control various 
	 * Ajax Events.
	 */
	public AjaxOptions global(boolean global)
	{
		this.global = global;
		return this;
	}
	
	/**
	 * HTTP Request Headers
	 */
	private Headers headers = new Headers();
	
	/**
	 * Get the HTTP Headers for the request
	 * @return the HTTP Headers for the request
	 */
	public Headers headers() { return headers; }
	
	/**
	 * Set the HTTP Headers for the request
	 * @param headers the HTTP Headers for the request
	 */
	public AjaxOptions headers(Headers headers)
	{
		this.headers = headers;
		return this;
	}
	
	/**
	 * If set to {@code true}, the most recent responses will be cached. The length of time
	 * that a cached response is considered valid can be set using the 
	 * {@link #cacheTimeout() cacheTimeout} option. Default is {@code false}. 
	 */
	private boolean cache;
	
	/**
	 * Get whether or not the most recent responses will be cached.
	 * @return {@code true} if the most recent responses will be cached. Otherwise, {@code false}.
	 */
	public boolean cache() { return cache; }
	
	/**
	 * Set whether or not the most recent responses will be cached.
	 * @param cache If set to {@code true}, the most recent responses will be cached. 
	 * The length of time that a cached response is considered valid can be set using the 
	 * {@link #cacheTimeout() cacheTimeout} option. Default is {@code false}. 
	 * @return this
	 */
	public AjaxOptions cache(boolean cache)
	{
		this.cache = cache;
		return this;
	}
	
	/**
	 * When the {@link #cache() cache} option is set to {@code true}, this option determines
	 * the length of time required (in milliseconds) between the current response and a 
	 * cached response in order to update the response data and cache the new response.
	 * Default is 600,000 ms (10 minutes). 
	 */
	private long cacheTimeout = 600000;
	
	/**
	 * Get the amount of time required, in milliseconds, between the current response and a cached
	 * response, in order to update the response data and cache the new response. This is only
	 * used when the {@link #cache() cache} option is set to {@code true}.
	 * @return the time, in milliseconds
	 */
	public long cacheTimeout() { return cacheTimeout; }
	
	/**
	 * Set the amount of time required, in milliseconds, between the current response and a cached
	 * response, in order to update the response data and cache the new response. This is only
	 * used when the {@link #cache() cache} option is set to {@code true}.
	 * @param cacheTimeout the time, in milliseconds
	 * @return this
	 */
	public AjaxOptions cacheTimeout(long cacheTimeout)
	{
		this.cacheTimeout = cacheTimeout;
		return this;
	}
	
	/**
	 * Allow the request to be successful only if the response has changed since the last request. 
	 * This is done by checking the Last-Modified header. Default value is {@code false}, ignoring 
	 * the header.
	 */
	private boolean ifModified = false;
	
	/**
	 * Get whether or not the response is only considered successful if it has been changed since
	 * the last request. This is done by checking the Last-Modified header.
	 * @return {@code true} to enable the check. Otherwise {@code false}.
	 */
	public boolean ifModified() { return ifModified; }
	
	/**
	 * Set whether or not the response is only considered successful if it has been changed since
	 * the last request. This is done by checking the Last-Modified header. Default value is
	 * {@code false}, ignoring the header.
	 * @param {@code true} to enable the check. Otherwise {@code false}.
	 * @return this
	 */
	public AjaxOptions ifModified(boolean ifModified)
	{
		this.ifModified = ifModified;
		return this;
	}
	
	/**
	 * A password to be used with HTTP Request in response to an HTTP access authentication request.
	 * @see #username
	 */
	private String password;
	
	/**
	 * Set the password to use if prompted with an HTTP access authentication request. There is
	 * no {@code password()} method in order to protect the password.
	 * @param password the password to use for authentication
	 * @return this
	 * @see #username()
	 * @see #getEncodedCredentials()
	 */
	public AjaxOptions password(String password)
	{
		this.password = password;
		return this;
	}
	
	/**
	 * Should be set to the name of a class that extends {@link DataProcessor} in order to handle 
	 * raw data to send in the HTTP request, in order to prevent it from being converted to a String.
	 */
	private String processDataClass = null;
	
	/**
	 * Get the name of a class that extends {@link DataProcessor} that is meant to handle
	 * raw data to send in the HTTP request, in order to prevent it from being converted to a String.
	 * If {@code null} is returned, then no such class has been configured.
	 * @return the name of a class that extends {@link DataProcessor}
	 */
	public String processData() { return processDataClass; }
	
	/**
	 * Set the name of a class that extends {@link DataProcessor} in order to handle 
	 * raw data to send in the HTTP request, in order to prevent it from being converted to a String.
	 * This name should include the package name (for example: "com.example.android.MyDataProcessor").
	 * @param processDataClass the name of a class that extends {@link DataProcessor}
	 * @return this
	 */
	public AjaxOptions processData(String processDataClass) 
	{
		this.processDataClass = processDataClass;
		return this;
	}
	
	/**
	 * A mapping of numeric HTTP codes to functions to be called when the response has the 
	 * corresponding code. For example, the following will alert when the response status is a 404:
	 * <pre>
	 * $.ajax(new AjaxOptions(this).statusCode(404, new Function(){
	 * 	public void invoke(Object... params)
	 * 	{
	 * 		$.with(this).alert("Page not found");
	 * 	}
	 * }));
	 * </pre>
	 */
	private Map<Integer, Function> statusCode = new HashMap<Integer, Function>();
	
	/**
	 * Get a mapping of numeric HTTP codes to functions to be called when the response has the 
	 * corresponding code.
	 * @return the mapping
	 */
	public Map<Integer, Function> statusCode() { return statusCode; }
	
	/**
	 * Sets a mapping of numeric HTTP codes to functions to be 
	 * called when the response has the corresponding code. 
	 * The function will receive a {@code null} Object for the
	 * <em>droidQuery</em> parameter unless {@link #context() context} is non-null. If that is
	 * the case, {@code statusCode} will receive a <em>droidQuery</em> instance with that <em>context</em>.
	 * @param statusCode the mapping
	 * @return this
	 * @see #statusCode(Integer, Function)
	 */
	public AjaxOptions statusCode(Map<Integer, Function> statusCode)
	{
		this.statusCode = statusCode;
		return this;
	}
	
	/**
	 * Adds a Key-Value entry to the status code mapping of numeric HTTP codes to functions to be 
	 * called when the response has the corresponding code. For example, the following will alert 
	 * when the response status is a 404:
	 * <pre>
	 * $.ajax(new AjaxOptions(this).statusCode(404, new Function(){
	 * 	public void invoke(Object... params)
	 * 	{
	 * 		$.with(this).alert("Page not found");
	 * 	}
	 * }));
	 * </pre>
	 * @param code the code key for the given function
	 * @param function the function to call when the response returns the given code
	 * @return this
	 */
	public AjaxOptions statusCode(Integer code, Function function)
	{
		this.statusCode.put(code, function);
		return this;
	}
	
	/**
	 * A function to be called if the request succeeds. The function gets passed two arguments:
	 * <ol>
	 * <li>The data returned from the server, formatted according to the dataType parameter
	 * <li>a string describing the status
	 * </ol>
	 */
	private Function success;
	
	/**
	 * Gets the function that will be called if the request succeeds. The function gets passed 
	 * two arguments:
	 * <ol>
	 * <li>The data returned from the server, formatted according to the dataType parameter
	 * <li>a string describing the status
	 * </ol>
	 * @return the function
	 */
	public Function success() { return success; }
	
	/**
	 * Sets the function that will be called if the request succeeds. The function will get passed 
	 * two arguments for varargs:
	 * <ol>
	 * <li>The data returned from the server, formatted according to the dataType parameter
	 * <li>a string describing the status
	 * </ol>
	 * It will also receive a {@code null} Object for the
	 * <em>droidQuery</em> parameter unless {@link #context() context} is non-null. If that is
	 * the case, {@code success} will receive a <em>droidQuery</em> instance with that <em>context</em>.
	 * @param success the function
	 * @return this
	 */
	public AjaxOptions success(Function success)
	{
		this.success = success;
		return this;
	}
	
	/**
	 * The timeout (in milliseconds) for the request. This will affect the request timeout
	 * and the socket timeout.
	 */
	private int timeout;
	
	/**
	 * Get the request (and socket) timeout (in milliseconds) for the request.
	 * @return the timeout, in milliseconds
	 */
	public int timeout() { return timeout; }
	
	/**
	 * Set the timeout (in milliseconds) for the request. This will affect the request timeout
	 * and the socket timeout.
	 * @param timeout the timeout, in milliseconds
	 * @return this
	 */
	public AjaxOptions timeout(int timeout) 
	{
		this.timeout = timeout;
		return this;
	}
	
	/**
	 * The type of request to make ("POST", "GET", "DELETE", "PUT", "HEAD", "OPTIONS", "TRACE" or "CUSTOM"), default is "GET".
	 * @see #customRequestClass
	 */
	private String type;
	
	/**
	 * Get the type of request to make. The response String will be one of "POST", "GET", "DELETE", 
	 * "PUT", "HEAD", "OPTIONS", "TRACE" or "CUSTOM"
	 * @return the response type
	 * @see #customRequestClass
	 */
	public String type() { return type; }
	
	/**
	 * Set the type of request to make. The response String will be one of "POST", "GET", "DELETE", 
	 * "PUT", "HEAD", "OPTIONS", "TRACE" or "CUSTOM"
	 * @param type the response type
	 * @return this
	 * @see #customRequestClass
	 */
	public AjaxOptions type(String type)
	{
		this.type = type;
		return this;
	}
	
	/**
	 * Used to configure the output bitmap width for requests that set the type attribute to "IMAGE". 
	 * If not set, no width scaling will be done of the raw image.
	 */
	private int imageWidth = -1;
	
	/**
	 * Get the output bitmap width for requests that set the type attribute to "IMAGE". 
	 * @return the scaled width, or -1 if the image width should not be scaled
	 */
	public int imageWidth() { return imageWidth; }
	
	/**
	 * Set the output bitmap width for requests that set the type attribute to "IMAGE".
	 * @param width the scaled width, or -1 if the image width should not be scaled
	 * @return this
	 */
	public AjaxOptions imageWidth(int width)
	{
		this.imageWidth = width;
		return this;
	}
	
	/**
	 * Used to configure the output bitmap height for requests that set the type attribute to "IMAGE". 
	 * If not set, no height scaling will be done of the raw image.
	 */
	private int imageHeight = -1;
	
	/**
	 * Get the output bitmap height for requests that set the type attribute to "IMAGE". 
	 * @return the scaled height, or -1 if the image height should not be scaled
	 */
	public int imageHeight() { return imageHeight; }
	
	/**
	 * Set the output bitmap height for requests that set the type attribute to "IMAGE".
	 * @param height the scaled height, or -1 if the image height should not be scaled
	 * @return this
	 */
	public AjaxOptions imageHeight(int height)
	{
		this.imageHeight = height;
		return this;
	}
	
	/**
	 * A string containing the URL to which the request is sent.
	 */
	private String url;
	
	/**
	 * Get the request URL
	 * @return the request URL
	 */
	public String url() { return url; }
	
	/**
	 * Set the request URL
	 * @param url the request URL
	 * @return this
	 */
	public AjaxOptions url(String url)
	{
		this.url = url;
		return this;
	}
	
	/**
	 * A username to be used with the HTTP request in response to an HTTP access authentication request.
	 * @see #password
	 */
	private String username;
	
	/**
	 * Get the username to use if prompted with an HTTP access authentication request.
	 * @return the String username
	 * @see #password(String)
	 * @see #getEncodedCredentials()
	 */
	public String username() { return username; }
	
	/**
	 * Set the username to use if prompted with an HTTP access authentication request
	 * @param username the String username
	 * @return this
	 */
	public AjaxOptions username(String username)
	{
		this.username = username;
		return this;
	}
	
	/** 
	 * If {@link #type} is set to "CUSTOM", then this can be used to pass a custom HTTP Request 
	 * type to the HTTP Client by setting it to name of a custom class that must extend 
	 * {@link CustomHttpUriRequest}. 
	 */
	private String customRequestClass;
	
	/**
	 * Get a new instance of the subclass of {@link CustomHttpUriRequest} that is used when the 
	 * {@link #type() type} option is set to "CUSTOM".
	 * @return the new instance 
	 * @throws Exception if the class name is {@code null} or is not a valid class name
	 */
	public CustomHttpUriRequest customRequest() throws Exception { 
		try
		{
			Class<?> clazz = Class.forName(customRequestClass);
			Constructor<?> constructor = clazz.getConstructor(new Class<?>[]{String.class});
			return (CustomHttpUriRequest) constructor.newInstance(url);
		}
		catch (Throwable t)
		{
			throw new Exception("Invalid Custom Request Class!");
		}
	}
	
	/**
	 * Set the class name of the subclass of {@link CustomHttpUriRequest} that is used when the 
	 * {@link #type() type} option is set to "CUSTOM". Once instantiated, the Object is used to pass
	 * a custom HTTP Request type to the HTTP Client.
	 * @param customRequestClass the name of the class. Should include the full package name. 
	 * For example: "com.example.android.MyCustomHttpUriRequest".
	 * @return this
	 */
	public AjaxOptions customRequest(String customRequestClass)
	{
		this.customRequestClass = customRequestClass;
		return this;
	}
	
	/**
	 * Set options to be included in all ajax requests. Requests can manually override these options
	 * by setting them on a per-request basis.
	 * @param options options to be included in all ajax requests
	 */
	public static void ajaxSetup(AjaxOptions options)
	{
		globalOptions = options;
	}
	
	/**
	 * Default Constructor
	 */
	public AjaxOptions()
	{
		//if #ajaxSetup has been called, this will set the global parameters.
		if (globalOptions != null)
		{
			for (Field f : fields)
			{
				Method setter = setters.get(f.getName());
				try {
					setter.invoke(this, f.get(globalOptions));
				} catch (Throwable t) {
					Log.w("AjaxOptions", "Cannot set global option " + f.getName());
				}
			}
		}
	}
	
	/**
	 * Construct with URL
	 * @param url
	 */
	public AjaxOptions(String url)
	{
		this();
		this.url = url;
	}
	
	/**
	 * Constructs a new AjaxOptions Object with the given URL and the Key-Value Mapping of Ajax Options values.
	 * @param url the request URL
	 * @param settings mapping of Ajax Options values. Can include all types - Strings, Functions, etc.
	 */
	public AjaxOptions(String url, Map<String, Object> settings)
	{
		this(settings);
		this.url = url;
	}
	
	/**
	 * Constructs a new AjaxOptions Object with the given Key-Value Mapping of Ajax Options values.
	 * @param settings mapping of Ajax Options values. Can include all types - Strings, Functions, etc.
	 */
	public AjaxOptions(Map<String, Object> settings)
	{
		this();
		for (Entry<String, Object> entry : settings.entrySet())
		{
			try
			{
				Method m = getClass().getMethod(entry.getKey(), new Class<?>[]{entry.getValue().getClass()});
				m.invoke(this, entry.getValue());
			}
			catch (Throwable t)
			{
				Log.w("AjaxOptions", "Invalid Field " + entry.getKey());
			}
		}
	}
	
	/**
	 * Construct a new AjaxOptions Object with the given JSONObject of Ajax Options values.
	 * @param json the JSONObject
	 * @throws JSONException if the {@code json} is malformed
	 */
	public AjaxOptions(JSONObject json) throws JSONException
	{
		this();
		
		@SuppressWarnings("unchecked")
		Iterator<String> iterator = json.keys();
		
	    while (iterator.hasNext()) {
	        String key = iterator.next();
	        try {
	            Object value = json.get(key);
	            for (Method m : methods)
	            {
	            	if (m.getName().equalsIgnoreCase(key) && m.getGenericParameterTypes().length != 0)
	            	{
	            		m.invoke(this, value);
	            		break;
	            	}
	            }
	            
	        } catch (JSONException e) {
	        	throw new JSONException("Invalid JSON String");
	        } catch (Throwable t)
	        {
	        	if (key != null)
	        		Log.w("AjaxOptions", "Could not set value " + key);
	        	else
	        		throw new NullPointerException("Iterator reference is null.");
	        }
	    }
	}

	/**
	 * As a security feature, this class will not allow queries of authentication passwords. This
	 * method will instead encode the security credentials (username and password) using
	 * Base64-encryption, and return the encrypted data as a byte array.
	 * @return the encrypted credentials
	 * @see #username()
	 * @see #password(String)
	 */
	public byte[] getEncodedCredentials()
	{
		StringBuilder auth = new StringBuilder();
		if (username != null)
		{
			auth.append(username);
		}
		if (password != null)
		{
			auth.append(":").append(password);
		}
		return Base64.encode(auth.toString().getBytes(), Base64.NO_WRAP);
	}
	
}
