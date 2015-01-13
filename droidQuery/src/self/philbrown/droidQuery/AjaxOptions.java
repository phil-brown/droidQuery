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
import java.net.URL;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.SAXParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.helpers.DefaultHandler;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.webkit.URLUtil;

/**
 * Build an Ajax Request
 * @author Phil Brown
 */
public class AjaxOptions implements Cloneable
{
	/**
	 * Used to determine how to handle redundant Ajax Requests. This optimization is used 
	 * for minimizing network traffic.
	 */
	public static enum Redundancy
	{
		/** Do nothing special.*/
		DO_NOTHING,
		/** Abort redundant requests. */
		ABORT_REDUNDANT_REQUESTS,
		/** 
		 * Respond to all listeners during events for the first request. This is the default, as
		 * it provides the most optimal solution.
		 */
		RESPOND_TO_ALL_LISTENERS
	}
	
	/** Used for reflection */
	private static Field[] fields = AjaxOptions.class.getDeclaredFields();
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
	 * two varargs: the original {@link AjaxOptions}, and the reason string. 
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
	 * Set the data to be sent to the server. Will be treated like JSON by taking declared fields 
	 * and converting them to JSON fields, recursively. For example:
	 * <pre>
	 * new AjaxOptions().data(new JSONModel() {
	 *     public int number = 0;
	 *     public String name = "foobar"
	 *     public int id = 12345;
	 *     public boolean isMale = true;
	 * });
	 * </pre>
	 * Complex Objects are converted to String using its {@code toString()} method.
	 * @param data
	 * @return this
	 */
	public AjaxOptions data(JSONModel data)
	{
		this.data = encodeToJSON(data);
		return this;
	}
	
	/**
	 * Recursively encodes a model object to JSON.
	 * @param data
	 * @return
	 * @see #data(JSONModel)
	 */
	private JSONObject encodeToJSON(Object data)
	{
		JSONObject json = new JSONObject();
		try {
			Field[] fields = data.getClass().getDeclaredFields();
			for (Field f : fields)
			{
				try {
					Object obj = f.get(data);
					//check valid JSONObject data fields
					if (obj == null || 
						obj instanceof JSONObject || 
						obj instanceof JSONArray || 
						obj == JSONObject.NULL || 
						obj instanceof String ||
						obj instanceof Boolean||
						obj instanceof Integer||
						obj instanceof Double||
						obj instanceof Long)
					{
						json.put(f.getName(), obj);
					}
					else
					{
						try {
							json.put(f.getName(), encodeToJSON(obj));
						}
						catch (Throwable t)
						{
							if (debug())
								t.printStackTrace();
							//otherwise it must be toString-ed.
							json.put(f.getName(), obj.toString());
						}
						
					}
				}
				catch (Throwable t)
				{
					if (debug())
						t.printStackTrace();
				}
			}
		}
		catch (Throwable t)
		{
			if (debug())
				t.printStackTrace();
		}
		return json;
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
	 * data. The function accepts two arguments: The raw data returned from the server (HttpResponse if using AjaxTask, HttpURLConnection if using Ajax) 
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
	 * data. The function accepts two varargs: The raw data returned from the server (HttpResponse if using AjaxTask, HttpURLConnection if using Ajax) 
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
	 * "raw" : a byte[]
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
	 * "xml": Returns a XML document that can be processed via droidQuery. Note that if {@link #customXMLParser()}
	 * or {@link #SAXContentHandler()} have been set, no Document will be returned. Instead, it will 
	 * pass a descriptive String.
	 * "html": Returns HTML as plain text.
	 * "script": Evaluates the response as bourne (NOT bash) script and returns it as plain text. 
	 * "json": Evaluates the response as JSON and returns a JSONObject object. The JSON data is parsed in a strict manner; any malformed JSON is rejected and a parse error is thrown. (See json.org for more information on proper JSON formatting.)
	 * "text": A plain text string.
	 * "image" : returns a bitmap object
	 * "raw" : a byte[]
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
	 * A custom content handler that can be used to handle XML using the SAX parser.  
	 * {@link org.xml.sax.helpers.DefaultHandler}
	 */
	private DefaultHandler SAXContentHandler;
	
	/**
	 * Set the Content Handler that should be used to handle SAX parsing. This will cause 
	 * {@link #success()} to NOT pass a XML Document variable as a parameter. Instead, it will pass a
	 * descriptive String.
	 * @param SAXContentHandler
	 * @return this
	 */
	public AjaxOptions SAXContentHandler(DefaultHandler SAXContentHandler)
	{
		this.SAXContentHandler = SAXContentHandler;
		return this;
	}
	
	/**
	 * Get the SAX parser Content Handler
	 * @return the handler
	 */
	public DefaultHandler SAXContentHandler()
	{
		return SAXContentHandler;
	}
	
	/**
	 * The custom parser for handling XML with SAX parser, instead of converting to a Document
	 */
	private SAXParser customXMLParser;
	/**
	 * Set the custom parser for handling XML with a SAX parser, instead of converting it to a
	 * Document Object. This will cause {@link #success()} to NOT pass a XML Document variable as a
	 * parameter.Instead, it will pass a descriptive String.
	 * @param customXMLParser
	 * @return this
	 */
	public AjaxOptions customXMLParser(SAXParser customXMLParser)
	{
		this.customXMLParser = customXMLParser;
		return this;
	}
	/**
	 * Get the custom parser for handling XML with a SAX parser instead of converting it to a Document.
	 * @return the parser
	 */
	public SAXParser customXMLParser()
	{
		return customXMLParser;
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
	 * @param cacheTimeout the time, in milliseconds.
	 * @return this
	 * @see AjaxCache#TIMEOUT_NEVER
	 * @see AjaxCache#TIMEOUT_NEVER_CLEAR_FROM_CACHE
	 */
	public AjaxOptions cacheTimeout(long cacheTimeout)
	{
		this.cacheTimeout = cacheTimeout;
		return this;
	}
	
	/**
	 * Contains a Key-Value mapping of cookies to send to in the Ajax request.
	 */
	private Map<String, String> cookies;
	
	/**
	 * Get the Key-Value mapping of cookies to send in the Ajax request.
	 * @return the Key-Value mapping of cookies to send
	 */
	public Map<String, String> cookies()
	{
		return cookies;
	}
	
	/**
	 * Set the Key-Value mapping of cookies to send in the Ajax request.
	 * @param cookies
	 */
	public void cookies(Map<String, String> cookies)
	{
		this.cookies = cookies;
	}
	
	/**
	 * Set the Key-Value mapping of cookies to send in the Ajax request.
	 * @param cookies
	 * @throws JSONException if the JSON is malformed
	 */
	@SuppressWarnings("unchecked")
	public void cookies(JSONObject cookies) throws JSONException
	{
		this.cookies = (Map<String, String>) $.map(cookies);
	}
	
	/**
	 * Set the Key-Value mapping of cookies to send in the Ajax request using a JSON string.
	 * @param cookies JSON representation of the cookies to send.
	 * @throws JSONException if the JSON is malformed
	 */
	public void cookies(String cookies) throws JSONException
	{
		cookies(new JSONObject(cookies));
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
	 * It will receive the int status code as the first parameter and a {@code clone} of this {@code AjaxOptions}
	 * object as the second parameter.
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
	 * Shortcut for calling {@link #statusCode(Integer, Function)} and using one {@code Function}
	 * for all codes. For example:
	 * <pre>
	 * new AjaxOptions().url("http://www.example.com").statusCode(new Integer[]{500, 408, 508}, new Function() {
	 * 	public void invoke($ d, Object... args) {
	 *  	int code = (Integer) args[0];
	 *      switch(code) {
	 *           case 500:
	 *           	break;
	 *           case 408:
	 *           	break;
	 *           case 508:
	 *           	break;
	 *           default:
	 *           	break;
	 *      }
	 * 	}
	 * });
	 * </pre>
	 * @param codes
	 * @param function
	 * @return
	 */
	public AjaxOptions statusCode(Integer[] codes, Function function)
	{
		for (int code : codes)
		{
			this.statusCode.put(code, function);
		}
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
	 * @deprecated Use {@link #customConnectionClass}
	 */
	private String customRequestClass;
	
	/**
	 * Get a new instance of the subclass of {@link CustomHttpUriRequest} that is used when the 
	 * {@link #type() type} option is set to "CUSTOM".
	 * @return the new instance 
	 * @throws Exception if the class name is {@code null} or is not a valid class name
	 * @deprecated Use {@link #customConnection}
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
	 * @deprecated Use {@link #customConnection(String)}
	 */
	public AjaxOptions customRequest(String customRequestClass)
	{
		this.customRequestClass = customRequestClass;
		return this;
	}
	
	/**
	 * Set the class name of the subclass of {@link CustomHttpUrlConnection} that is used when the 
	 * {@link #type() type} option is set to "CUSTOM". Once instantiated, the Object is used to pass
	 * a custom HTTP Connection type to the HTTP Client.
	 * @param customRequestClass the name of the class. Should include the full package name. 
	 * For example: "com.example.android.MyCustomHttpUrlConnection".
	 * @return this
	 */
	public AjaxOptions customConnection(String customConnectionClass)
	{
		this.customConnectionClass = customConnectionClass;
		return this;
	}
	
	/** 
	 * If {@link #type} is set to "CUSTOM", then this can be used to pass a custom HTTP Connection 
	 * type to the HTTP Client by setting it to name of a custom class that must extend 
	 * {@link CustomHttpUrlConnection}. 
	 */
	private String customConnectionClass;
	
	/**
	 * Get a new instance of the subclass of {@link CustomHttpUriRequest} that is used when the 
	 * {@link #type() type} option is set to "CUSTOM".
	 * @return the new instance 
	 * @throws Exception if the class name is {@code null} or is not a valid class name
	 */
	public CustomHttpUrlConnection customConnection() throws Exception { 
		try
		{
			Class<?> clazz = Class.forName(customConnectionClass);
			Constructor<?> constructor = clazz.getConstructor(new Class<?>[]{URL.class});
			return (CustomHttpUrlConnection) constructor.newInstance(new URL(url));
		}
		catch (Throwable t)
		{
			throw new Exception("Invalid Custom Request Class!");
		}
	}
	
	/** 
	 * If {@code true}, all SSL certificates will be trusted (for HTTPS requests). Default is 
	 * {@code false}, since allowing this poses a security threat. Never allow this for production 
	 * applications.
	 * @deprecated this is not secure, and is not used in the new API.
	 */
	private boolean trustAllSSLCertificates = false;
	
	/**
	 * Allows all SSL certificates to be trusted (for HTTPS requests). This should <b>never</b> be
	 * {@code true} in a production application, as it poses a security threat.
	 * @return {@code true} if all SSL certificates are trusted, otherwise {@code false}.
	 * @deprecated this is not secure, and is not used in the new API.
	 */
	public boolean trustAllSSLCertificates()
	{
		return this.trustAllSSLCertificates;
	}
	
	/**
	 * Allows all SSL certificates to be trusted (for HTTPS requests). This should <b>never</b> be
	 * {@code true} in a production application, as it poses a security threat.
	 * @param trustAllSSLCertificates {@code true} to trust all SSL certificates. Note that setting
	 * as {@code true} will cause the logcat to output this setting on every Ajax call. This is helpful
	 * for ensuring this is not forgotten in production applications. Default is {@code false}.
	 * @return this
	 * @deprecated this is not secure, and is not used in the new API.
	 */
	public AjaxOptions trustAllSSLCertificates(boolean trustAllSSLCertificates)
	{
		this.trustAllSSLCertificates = trustAllSSLCertificates;
		return this;
	}
	
	/**
	 * Specifies a trusted certificate for HTTPS requests.
	 */
	private Certificate trustedCertificate;
	
	/**
	 * Specifies a trusted certificate for HTTPS requests.
	 */
	public Certificate trustedCertificate() {
		return trustedCertificate;
	}
	/**
	 * Specifies a trusted certificate for HTTPS requests.
	 * @param trustedCertificate
	 * @return this
	 */
	public AjaxOptions trustedCertificate(Certificate trustedCertificate) {
		this.trustedCertificate = trustedCertificate;
		return this;
	}
	
	/**
	 * If {@code true}, AjaxOptions will configure the Ajax task to use the new API, which uses executors and HttpURLConnections, instead of the Apache Framework.
	 * This is still a work in progress, so currently defaults to {@code false}.
	 */
	private boolean usesNewAPI = false;
	
	/**
	 * If {@code true}, AjaxOptions will configure the Ajax task to use the new API, which uses executors and HttpURLConnections, instead of the Apache Framework.
	 * This is still a work in progress, so currently defaults to {@code false}.
	 */
	public boolean usesNewAPI() {
		return usesNewAPI;
	}
	
	/**
	 * If {@code true}, AjaxOptions will configure the Ajax task to use the new API, which uses executors and HttpURLConnections, instead of the Apache Framework.
	 * This is still a work in progress, so currently defaults to {@code false}.
	 * @return this
	 */
	public AjaxOptions usesNewAPI(boolean usesNewAPI) {
		this.usesNewAPI = usesNewAPI;
		return this;
	}
	
	//TODO add specific trusted certs.
	
	/** Defines how redundant Ajax Requests are handled. */
	private Redundancy redundancy = Redundancy.RESPOND_TO_ALL_LISTENERS;
	
	/**
	 * Set how redundant Ajax Requests are handled.
	 * @param redundancy
	 * @return this
	 */
	public AjaxOptions redundancy(Redundancy redundancy)
	{
		this.redundancy = redundancy;
		return this;
	}
	
	/**
	 * Get how redundant Ajax Requests are handled.
	 * @return the method used for handling redundant requests
	 */
	public Redundancy redundancy()
	{
		return this.redundancy;
	}
	
	/** 
	 * Variable that can be set in {@link #beforeSend}  to abort an
	 * Ajax Request before it begins.
	 */
	private boolean aborted = false;
	
	/**
	 * Causes the Ajax Request to be aborted. This must be called in {@link #beforeSend} in order to
	 * take affect.
	 */
	public void abort()
	{
		this.aborted = true;
	}
	
	/**
	 * Get whether or not the task has been aborted.
	 * @return {@code true} if {@link #abort()} was called. Otherwise, {@code false}.
	 */
	public boolean isAborted()
	{
		return this.aborted;
	}
	
	/**
	 * Specifies the task's thread priority. Default is {@link Thread#NORM_PRIORITY}. Must be [0-10].
	 */
	private int priority = Thread.NORM_PRIORITY;
	
	/**
	 * Get the Thread priority for the Ajax task
	 * @return
	 */
	public int priority()
	{
		return this.priority;
	}
	
	/**
	 * Set the thread priority
	 * @param priority
	 * @return this
	 * throws IllegalArgumentException if the given priority is less than {@link Thread#MIN_PRIORITY} or greater than {@link Thread#MAX_PRIORITY}.
	 */
	public AjaxOptions priority(int priority) 
	{
		if (priority < Thread.MIN_PRIORITY || priority > Thread.MAX_PRIORITY) 
		{
			throw new IllegalArgumentException("invalid priority value. Must be [0-10].");
		}
		this.priority = priority;
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
//				Method setter = setters.get(f.getName());
//				Method getter = getters.get(f.getName());
//				if (setter != null && getter != null)
//				{
//					try {
//						setter.invoke(this, getter.invoke(globalOptions));
//					} catch (Throwable t) {}
//				}
				try {
					f.set(this, f.get(globalOptions));
				} catch (Throwable t) {}
				
			}
		}
	}
	
	/**
	 * Construct with JSON string. To support versions 0.1.0-0.1.3, this method can also accept a URL.
	 * @param json JSON options
	 * @throws JSONException 
	 */
	public AjaxOptions(String json) throws JSONException
	{
		this();
		if (URLUtil.isValidUrl(json))
		{
			this.url = json;
		}
		else
		{
			handleJSONOptions(new JSONObject(json));
		}
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
		handleJSONOptions(json);
	}
	
	/**
	 * Used privately by constructors to parse JSON options
	 * @param json
	 * @throws JSONException
	 */
	private void handleJSONOptions(JSONObject json) throws JSONException
	{
		@SuppressWarnings("unchecked")
		Iterator<String> iterator = json.keys();
		
	    while (iterator.hasNext()) {
	        String key = iterator.next();
	        try {
	            final Object value = json.get(key);
	            for (Field f : fields)
	            {
	            	if (f.getName().equalsIgnoreCase(key))
	            	{
	            		if (f.getType() == Function.class && value instanceof String)
	            		{
	            			//special case for handling strings as Functions
	            			f.set(this, new Function() {
								
								@Override
								public void invoke($ droidQuery, Object... params) {
									if (params == null)
										EventCenter.trigger(droidQuery, (String) value, null, null);
									else
									{
										Map<String, Object> args = new HashMap<String, Object>();
										for (int i = 0; i < params.length; i++)
										{
											args.put(String.valueOf(i), params[i]);
										}
										EventCenter.trigger(droidQuery, (String) value, args, null);
									}
								}
							});
	            		}
	            		else
	            			f.set(this, value);
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
	
	@Override
	public Object clone()
	{
		//custom clone implementation
		AjaxOptions clone = new AjaxOptions();
		for (Field f : fields)
		{
			try {
				f.set(clone, f.get(this));
			} catch (Throwable t) {}
		}
		return clone;
	}
	
	/**
	 * This empty interface can be used for passing custom data to requests using {@link #data(Model)}.
	 * This works by converting the object into JSON based on what fields it contains. For example:
	 * <pre>
	 * new AjaxOptions().data(new JSONModel(){
	 *     public String foo = "bar";
	 *     public int number = 200;
	 * });
	 * </pre>
	 * @author Phil Brown
	 * @since 6:38:28 PM Nov 12, 2013
	 *
	 */
	public interface JSONModel {
	}
	
}
