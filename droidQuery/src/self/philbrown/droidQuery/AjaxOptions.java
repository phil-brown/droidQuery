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
	
	/** Used for reflection */
	private static Method[] methods = AjaxOptions.class.getMethods();
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
	public String accepts(){ return accepts; }
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
	public boolean async() { return async; }
	public AjaxOptions async(boolean async)
	{
		this.async = async;
		return this;
	}
	
	/**
	 * A pre-request callback function. Receives these options as a parameter.
	 */
	private Function beforeSend;
	public Function beforeSend(){ return beforeSend; }
	public AjaxOptions beforeSend(Function beforeSend)
	{
		this.beforeSend = beforeSend;
		return this;
	}
	
//	/**
//	 * Default is false. If set to true, the most recent responses will be cached is sharedPreferences.
//	 * FIXME: This is not implemented yet.
//	 */
//	private boolean cache;
//	public boolean cache() { return cache; }
//	public AjaxOptions cache(boolean cache)
//	{
//		this.cache = cache;
//		return this;
//	}
	
	/**
	 * A function to be called when the request finishes (after success and error callbacks are executed). 
	 * Receives the text status
	 */
	private Function complete;
	public Function complete() { return complete; }
	public AjaxOptions complete(Function complete)
	{
		this.complete = complete;
		return this;
	}
//	private Object contents;
//	public Object contents() { return contents; }
//	public AjaxOptions contents(Object contents)
//	{
//		this.contents = contents;
//		return this;
//	}
	
	/**
	 * When sending data to the server, use this content type. 
	 * Default is "application/x-www-form-urlencoded; charset=UTF-8", which is fine for most cases. 
	 */
	private String contentType = "application/x-www-form-urlencoded; charset=UTF-8";
	public String contentType() { return contentType; }
	public AjaxOptions contentType(String contentType)
	{
		this.contentType = contentType;
		headers.content_type(contentType);
		return this;
	}
	
	/**
	 * Used to set get or change the current context
	 */
	private Context context;//TODO: consider this: This context should be passed to all Functions! OR: change which Activity or context will receive the callback
	public Context context() { return context; }
	public AjaxOptions context(Context context)
	{
		this.context = context;
		return this;
	}
	//converters;//probably not needed
	//crossDomain;
	/**
	 * Data to be sent to the server. Will be converted to String unless processData is set to false.
	 */
	private Object data;
	public Object data() { return data; }
	public AjaxOptions data(Object data)
	{
		this.data = data;
		return this;
	}
	

	/**
	 * A function to be used to handle the raw response data. This is a 
	 * pre-filtering function to sanitize the response. You should return the sanitized 
	 * data. The function accepts two arguments: The raw data returned from the server (HttpResponse) 
	 * and the 'dataType' parameter (String).
	 */
	private Function dataFilter;
	public Function dataFilter() { return dataFilter; }
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
	public String dataType() { return dataType; }
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
	public Function error() { return error; }
	public AjaxOptions error(Function error)
	{
		this.error = error;
		return this;
	}
	
	/**
	 * Whether to trigger global Ajax event handlers for this request. The default is true. 
	 * Set to false to prevent the global handlers like ajaxStart or ajaxStop from being triggered. 
	 * This can be used to control various Ajax Events.
	 */
	private boolean global = true;
	public boolean global() { return global; }
	public AjaxOptions global(boolean global)
	{
		this.global = global;
		return this;
	}
	
	/**
	 * HTTP Request Headers
	 */
	private Headers headers = new Headers();
	public Headers headers() { return headers; }
	public AjaxOptions headers(Headers headers)
	{
		this.headers = headers;
		return this;
	}
	
	/**
	 * Allow the request to be successful only if the response has changed since the last request. 
	 * This is done by checking the Last-Modified header. Default value is false, ignoring the header.
	 * @note This is not yet implemented
	 */
	private boolean ifModified = false;
	public boolean ifModified() { return ifModified; }
	public AjaxOptions ifModified(boolean ifModified)
	{
		this.ifModified = ifModified;
		return this;
	}
	
//	private boolean isLocal;
//	public boolean isLocal() { return isLocal; }
//	public AjaxOptions isLocal(boolean isLocal)
//	{
//		this.isLocal = isLocal;
//		return this;
//	}
	
//	private String mimeType;
//	public String mimeType() { return mimeType; }
//	public AjaxOptions mimeType(String mimeType)
//	{
//		this.mimeType = mimeType;
//		return this;
//	}
	
	/**
	 * A password to be used with HTTP Request in response to an HTTP access authentication request.
	 */
	private String password;
	//there is no password() method to protect the password.
	public AjaxOptions password(String password)
	{
		this.password = password;
		return this;
	}
	
	//TODO continue documentation here.
	/**
	 * Should be set to a class that extends {@link DataProcessor} in order to handle raw data
	 * to be sent to an HTTP request, instead of it being converted to a String.
	 */
	private String processDataClass = null;
	public String processData() { return processDataClass; }
	public AjaxOptions processData(String processDataClass) 
	{
		this.processDataClass = processDataClass;
		return this;
	}
	
//	
//	private String scriptCharset;
//	public String scriptCharset() { return scriptCharset; }
//	public AjaxOptions scriptCharset(String scriptCharset)
//	{
//		this.scriptCharset = scriptCharset;
//		return this;
//	}
	
	/**
	 * An object of numeric HTTP codes and functions to be called when the response has the 
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
	public Map<Integer, Function> statusCode() { return statusCode; }
	/** sets the status code */
	public AjaxOptions statusCode(Map<Integer, Function> statusCode)
	{
		this.statusCode = statusCode;
		return this;
	}
	/** Adds a status code */
	public AjaxOptions statusCode(Integer code, Function function)
	{
		this.statusCode.put(code, function);
		return this;
	}
	
	/**
	 * A function to be called if the request succeeds. The function gets passed two arguments:
	 * <ul>
	 * <li>The data returned from the server, formatted according to the dataType parameter
	 * <li>a string describing the status
	 * </ul>
	 */
	private Function success;
	public Function success() { return success; }
	public AjaxOptions success(Function success)
	{
		this.success = success;
		return this;
	}
	
	/**
	 * Set a timeout (in milliseconds) for the request.
	 */
	private int timeout;
	public int timeout() { return timeout; }
	public AjaxOptions timeout(int timeout) 
	{
		this.timeout = timeout;
		return this;
	}
//	
//	private boolean traditional;
//	public boolean traditional() { return traditional; }
//	public AjaxOptions traditional(boolean traditional)
//	{
//		this.traditional = traditional;
//		return this;
//	}
	
	/**
	 * The type of request to make ("POST", "GET", "DELETE", "PUT", "HEAD", "OPTIONS", "TRACE" or "CUSTOM"), default is "GET".
	 * @see #customRequestClass
	 */
	private String type;
	public String type() { return type; }
	public AjaxOptions type(String type)
	{
		this.type = type;
		return this;
	}
	
	/**
	 * Used to set the output width for Async Requests that set the type attribute to image. 
	 * If not set, no scaling will be done of the raw image.
	 */
	private int imageWidth = -1;
	public int imageWidth() { return imageWidth; }
	public AjaxOptions imageWidth(int width)
	{
		this.imageWidth = width;
		return this;
	}
	
	/**
	 * Used to set the output height for Async Requests that set the type attribute to image. 
	 * If not set, no scaling will be done of the raw image.
	 */
	private int imageHeight = -1;
	public int imageHeight() { return imageHeight; }
	public AjaxOptions imageHeight(int height)
	{
		this.imageHeight = height;
		return this;
	}
	
	/**
	 * A string containing the URL to which the request is sent.
	 */
	private String url;
	public String url() { return url; }
	public AjaxOptions url(String url)
	{
		this.url = url;
		return this;
	}
	
	/**
	 * A username to be used with HTTP Request in response to an HTTP access authentication request.
	 */
	private String username;
	public String username() { return username; }
	public AjaxOptions username(String username)
	{
		this.username = username;
		return this;
	}
	
	/** If "type" is set to "custom", then this can be used to pass a custom HTTP Request type to the HTTP Client. 
	 * Class must extend CustomHttpRequest. */
	private String customRequestClass;
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
	public AjaxOptions customRequest(String customRequestClass)
	{
		this.customRequestClass = customRequestClass;
		return this;
	}
	
//	private Function xhr;//probably not needed
//	//xhrFields
	
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
	
	public AjaxOptions(String url, Map<String, Object> settings)
	{
		this(settings);
		this.url = url;
	}
	
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
	 * Construct with JSONObject. This is more time consuming that other options.
	 * @param json
	 * @throws JSONException 
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
	 * Since password is not publicly available, use this to assemble the encoded authentication
	 * credentials (base64). 
	 * @return
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
