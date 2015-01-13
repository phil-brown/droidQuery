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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * HTTPClient Headers
 * @author Phil Brown
 */
public class Headers 
{
	/** Key-Value Mapping of Header values */
	protected Map<String, String> headers;
	
	/**
	 * Default constructor
	 */
	public Headers()
	{
		headers = new HashMap<String, String>();
	}
	
	/**
	 * Construct a new Headers Object with the given attributes
	 * @param headers a Key-Value pairing of the header attributes to set
	 */
	public Headers(Map<String, String> headers)
	{
		this.headers = headers;
	}
	
	/**
	 * Construct a new Headers Object with the given JSON String
	 * @param jsonString contains header attributes to set
	 * @throws JSONException if the JSON string is malformed
	 */
	public Headers(String jsonString) throws JSONException
	{
		this();
		JSONObject json = null;
		try
		{
			json = new JSONObject(jsonString);
		}
		catch (Throwable t)
		{
			throw new JSONException("Invalid JSON String");
		}
		unpackJSON(json);
	}
	
	/**
	 * Construct a new Headers Object with the given JSON Object
	 * @param json contains header attributes to set
	 * @throws JSONException if the JSON string is malformed
	 */
	public Headers(JSONObject json) throws JSONException
	{
		this();
		unpackJSON(json);
	}
	
	/**
	 * Create Headers from Apache Http Headers
	 * @param headers
	 */
	public Headers(Header[] headers) {
		this();
		for (Header h : headers) {
			this.headers.put(h.getName(), h.getValue());
		}
	}
	
	/**
	 * Create headers from HttpUrlConnection headers.
	 * @param headers
	 */
	public static Headers createHeaders(Map<String, List<String>> headers) {
		Headers h = new Headers();
		for (Map.Entry<String, List<String>> header : headers.entrySet()) {
			h.headers.put(header.getKey(), header.getValue().get(0));
		}
		return h;
	}

	/**
	 * Unpack a JSONObject into a Key-Value Mapping
	 * @param json the JSONObject to parse
	 * @throws JSONException if the JSON string is malformed
	 */
	private void unpackJSON(JSONObject json) throws JSONException
	{
		
		@SuppressWarnings("unchecked")
		Iterator<String> iterator = json.keys();
	    while (iterator.hasNext()) {
	        String key = iterator.next();
	        try {
	            String value = (String) json.get(key);
	            headers.put(key, value);
	        } catch (JSONException e) {
	        	throw new JSONException("Invalid JSON String");
	        }
	    }
	}
	
	/**
	 * Get the Headers information in a Key-Value Mapping
	 * @return the Headers in a Key-Value pairing
	 */
	public Map<String, String> map()
	{
		return headers;
	}
	
	/**
	 * Add another Header to this Headers Object using the String key and the String value
	 * @param key the attribute name
	 * @param value the attribute value
	 */
	public void add(String key, String value)
	{
		headers.put(key, value);
	}
	
	//Accept
	/**
	 * Get the content type sent in the request header that tells the server what kind of response
	 * it will accept in return. For example, "text/plain".
	 * @param acceptable content types
	 */
	public String accept(){return headers.get("Accept");}
	/**
	 * Set the content type sent in the request header that tells the server what kind of response
	 * it will accept in return. For example, "text/plain".
	 * @return this
	 */
	public Headers accept(String accept){headers.put("Accept", accept); return this;}

	//Accept-Charset
	/**
	 * Get the character sets that are acceptable (for example, "utf-8")
	 * @return the acceptable character set
	 */
	public String accept_charset(){return headers.get("Accept-Charset");}
	/**
	 * Set the character sets that are acceptable (for example, "utf-8")
	 * @param accept_charset the acceptable character set
	 * @return this
	 */
	public Headers accept_charset(String accept_charset){headers.put("Accept-Charset", accept_charset); return this;}

	//Accept-Encoding
	/**
	 * Get the list of acceptable encodings. For example, "gzip, deflate"
	 * @return the list of acceptable encodings
	 */
	public String accept_encoding(){return headers.get("Accept-Encoding");}
	/**
	 * Set the list of acceptable encodings. For example, "gzip, deflate"
	 * @param accept_encoding the list of acceptable encodings
	 * @return this
	 */
	public Headers accept_encoding(String accept_encoding){headers.put("Accept-Encoding", accept_encoding); return this;}

	//Accept-Language
	/**
	 * Get the list of acceptable human languages for response. Example: "en-US"
	 * @return the list of acceptable human languages for response
	 */
	public String accept_language(){return headers.get("Accept-Language");}
	/**
	 * Set the list of acceptable human languages for response. Example: "en-US"
	 * @param accept_language the list of acceptable human languages for response
	 * @return this
	 */
	public Headers accept_language(String accept_language){headers.put("Accept-Language", accept_language); return this;}

	//Accept-Datetime
	/**
	 * Get the acceptable version in time. Example: "Thu, 31 May 2007 20:35:00 GMT"
	 * @return the acceptable version in time
	 */
	public String accept_datetime(){return headers.get("Accept-Datetime");}
	
	/**
	 * Set the acceptable version in time. Example: "Thu, 31 May 2007 20:35:00 GMT"
	 * @param accept_datetime the acceptable version in time
	 * @return this
	 */
	public Headers accept_datetime(String accept_datetime){headers.put("Accept-Datetime", accept_datetime); return this;}

	//Authorization
	/**
	 * Get the authentication credentials for HTTP authentication. For example: "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ=="
	 * @return the authentication credentials for HTTP authentication
	 */
	public String authorization(){return headers.get("Authorization");}
	/**
	 * Set the authentication credentials for HTTP authentication. For example: "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ=="
	 * @param authorization the authentication credentials for HTTP authentication
	 * @return this
	 */
	public Headers authorization(String authorization){headers.put("Authorization", authorization); return this;}

	//Cache-Control
	/**
	 * Get the directives that MUST be obeyed by all caching mechanisms along the request/response 
	 * chain. For example: "no-cache"
	 * @return the directives
	 */
	public String cache_control(){return headers.get("Cache-Control");}
	/**
	 * Set the directives that MUST be obeyed by all caching mechanisms along the request/response 
	 * chain. For example: "no-cache"
	 * @param cache_control the directives
	 * @return this
	 */
	public Headers cache_control(String cache_control){headers.put("Cache-Control", cache_control); return this;}

	//Connection
	/**
	 * Get the type of connection the user-agent would prefer. For example: "keep-alive"
	 * @return the type of connection
	 */
	public String connection(){return headers.get("Connection");}
	/**
	 * Set the type of connection the user-agent would prefer. For example: "keep-alive"
	 * @param connection the type of connection
	 * @return this
	 */
	public Headers connection(String connection){headers.put("Connection", connection); return this;}

	//Cookie
	/**
	 * Get the data to attach to the request. For example: "Version=1; Option=new;"
	 * @return the data to attach to the request
	 */
	public String cookie(){return headers.get("Cookie");}
	/**
	 * Set the data to attach to the request. For example: "Version=1; Option=new;"
	 * @param cookie the data to attach to the request
	 * @return this
	 */
	public Headers cookie(String cookie){headers.put("Cookie", cookie); return this;}

	//Content-Length
	/**
	 * Get the length of the request body in octets (8-bit bytes). For example: "348"
	 * @return the length of the request body in octets
	 */
	public String content_length(){return headers.get("Content-Length");}
	/**
	 * Set the length of the request body in octets (8-bit bytes). For example: "348"
	 * @param content_length the length of the request body in octets
	 * @return this
	 */
	public Headers content_length(String content_length){headers.put("Content-Length", content_length); return this;}

	//Content-MD5
	/**
	 * Get the Base64-encoded binary MD5 sum of the content of the request body
	 * @return the MD5 sum
	 */
	public String content_md5(){return headers.get("Content-MD5");}
	/**
	 * Set the Base64-encoded binary MD5 sum of the content of the request body
	 * @param content_md5 the MD5 sum
	 * @return this
	 */
	public Headers content_md5(String content_md5){headers.put("Content-MD5", content_md5); return this;}

	//Content-Type
	/**
	 * Get the content type of the data sent to the server.
	 * Default is "application/x-www-form-urlencoded; charset=UTF-8", which is fine for most cases. 
	 * @return the content type
	 */
	public String content_type(){return headers.get("Content-Type");}
	/**
	 * Set the content type of the data sent to the server.
	 * Default is "application/x-www-form-urlencoded; charset=UTF-8", which is fine for most cases.
	 * @param content_type the content type
	 * @return this
	 */
	public Headers content_type(String content_type){headers.put("Content-Type", content_type); return this;}

	//Date
	/**
	 * Get the date and time that the message was sent. Example: "Tue, 15 Nov 1994 08:12:31 GMT"
	 * @return the date and time that the message was sent
	 */
	public String date(){return headers.get("Date");}
	/**
	 * Set the date and time that the message was sent. Example: "Tue, 15 Nov 1994 08:12:31 GMT"
	 * @param date the date and time that the message was sent
	 * @return this
	 */
	public Headers date(String date){headers.put("Date", date); return this;}

	//Expect
	/**
	 * Get particular server behaviors that are required by the client. Example: "100-continue"
	 * @return server behaviors required by the client
	 */
	public String expect(){return headers.get("Expect");}
	/**
	 * Set particular server behaviors that are required by the client. Example: "100-continue"
	 * @param expect server behaviors required by the client
	 * @return this
	 */
	public Headers expect(String expect){headers.put("Expect", expect); return this;}

	//From
	/**
	 * Get the email address of the user making the request
	 * @return the email address of the user making the request
	 */
	public String from(){return headers.get("From");}
	/**
	 * Set the email address of the user making the request
	 * @param from the email address of the user making the request
	 * @return this
	 */
	public Headers from(String from){headers.put("From", from); return this;}

	//Host
	/**
	 * Get the domain name of the server. May include the port number. 
	 * Examples: "192.168.0.1", "192.168.0.1:8080", "http://www.github.com", "http://www.github.com:8081"
	 * @return the domain name of the server
	 */
	public String host(){return headers.get("Host");}
	/**
	 * Set the domain name of the server. May include the port number. 
	 * Examples: "192.168.0.1", "192.168.0.1:8080", "http://www.github.com", "http://www.github.com:8081"
	 * @param host the domain name of the server
	 * @return this
	 */
	public Headers host(String host){headers.put("Host", host); return this;}

	//If-Match
	/**
	 * Get the String entity that must match the same entity on the server in order for the request 
	 * action to succeed. This is mainly for methods like PUT to only update a resource if it has not 
	 * been modified since the user last updated it.
	 * @return the String entity
	 */
	public String if_match(){return headers.get("If-Match");}
	/**
	 * Set the String entity that must match the same entity on the server in order for the request 
	 * action to succeed. This is mainly for methods like PUT to only update a resource if it has not 
	 * been modified since the user last updated it.
	 * @param if_match the String entity
	 * @return this
	 */
	public Headers if_match(String if_match){headers.put("If-Match", if_match); return this;}

	//If-Modified-Since
	/**
	 * Get the date string that the response must be modified after. If it has not been modified
	 * since the given date string, a {@code 304 Not Modified} error will occur.
	 * @return the date string
	 */
	public String if_modified_since(){return headers.get("If-Modified-Since");}
	/**
	 * Set the date string that the response must be modified after. If it has not been modified
	 * since the given date string, a {@code 304 Not Modified} error will occur.
	 * @param if_modified_since the date string
	 * @return this
	 */
	public Headers if_modified_since(String if_modified_since){headers.put("If-Modified-Since", if_modified_since); return this;}

	//If-None-Match
	/**
	 * Get the String entity that must not match the same entity on the server in order for the request 
	 * action to succeed. This is often used as a fallback in conjuncture with {@link #if_modified_since()}
	 * @return the String entity
	 */
	public String if_none_match(){return headers.get("If-None-Match");}
	/**
	 * Set the String entity that must not match the same entity on the server in order for the request 
	 * action to succeed. This is often used as a fallback in conjuncture with {@link #if_modified_since()}
	 * @param if_none_match the String entity
	 * @return this
	 */
	public Headers if_none_match(String if_none_match){headers.put("If-None-Match", if_none_match); return this;}

	//If-Range
	/**
	 * Get the String entity that can be broken into pieces of "changed data" to send in the response,
	 * instead of sending all data.
	 * @return the String entity
	 */
	public String if_range(){return headers.get("If-Range");}
	/**
	 * Set the String entity that can be broken into pieces of "changed data" to send in the response,
	 * instead of sending all data.
	 * @param if_range the String entity
	 * @return this
	 */
	public Headers if_range(String if_range){headers.put("If-Range", if_range); return this;}

	//If-Unmodified-Since
	/**
	 * Get the date-time String for when the response must have been last modified, in order for 
	 * the request to receive a response.
	 * @return the date String
	 */
	public String if_unmodified_since(){return headers.get("If-Unmodified-Since");}
	/**
	 * Set the date-time String for when the response must have been last modified, in order for 
	 * the request to receive a response.
	 * @param if_unmodified_since the date String
	 * @return this
	 */
	public Headers if_unmodified_since(String if_unmodified_since){headers.put("If-Unmodified-Since", if_unmodified_since); return this;}

	//Max-Forwards
	/**
	 * Get the number of times the message can be forwarded through proxies or gateways.
	 * @return the number of times
	 */
	public String max_forwards(){return headers.get("Max-Forwards");}
	/**
	 * Set the number of times the message can be forwarded through proxies or gateways. Example: "10"
	 * @param max_forwards the number of times
	 * @return this
	 */
	public Headers max_forwards(String max_forwards){headers.put("Max-Forwards", max_forwards); return this;}

	//Origin
	/**
	 * Get the header field meant to initialize a request for cross-origin resource sharing.
	 * @return the URL Origin String
	 */
	public String origin(){return headers.get("Origin");}
	/**
	 * Set the header field meant to initialize a request for cross-origin resource sharing.
	 * @param origin the URL Origin String
	 * @return this
	 */
	public Headers origin(String origin){headers.put("Origin", origin); return this;}

	//Pragma
	/**
	 * Get implementation-specific headers 
	 * @return implementation-specific headers 
	 */
	public String pragma(){return headers.get("Pragma");}
	/**
	 * Set implementation-specific headers 
	 * @param implementation-specific headers 
	 * @return this
	 */
	public Headers pragma(String pragma){headers.put("Pragma", pragma); return this;}

	//Proxy-Authorization
	/**
	 * Gets the authorization credentials for connecting to a proxy.
	 * @return the credentials
	 */
	public String proxy_authorization(){return headers.get("Proxy-Authorization");}
	/**
	 * Sets the authorization credentials for connecting to a proxy.
	 * @param proxy_authorization the credentials
	 * @return this
	 */
	public Headers proxy_authorization(String proxy_authorization){headers.put("Proxy-Authorization", proxy_authorization); return this;}

	//Range
	/**
	 * Get a range of bytes used to only request part of an entity. Example: "bytes=500-999"
	 * @return the byte range string
	 */
	public String range(){return headers.get("Range");}
	/**
	 * Set a range of bytes used to only request part of an entity. Example: "bytes=500-999"
	 * @param range the byte range string
	 * @return this
	 */
	public Headers range(String range){headers.put("Range", range); return this;}

	//TE
	/**
	 * Get the transfer encodings the user agent is willing to accept
	 * @return the transfer encodings the user agent is willing to accept
	 */
	public String te(){return headers.get("TE");}
	/**
	 * Set the transfer encodings the user agent is willing to accept
	 * @param te the transfer encodings the user agent is willing to accept
	 * @return this
	 */
	public Headers te(String te){headers.put("TE", te); return this;}

	//Upgrade
	/**
	 * Get the String used to ask the server to upgrade to another protocol. For example:
	 * "HTTP/2.0, SHTTP/1.3, IRC/6.9, RTA/x11"
	 * @return the upgrade request
	 */
	public String upgrade(){return headers.get("Upgrade");}
	/**
	 * Set the String used to ask the server to upgrade to another protocol. For example:
	 * "HTTP/2.0, SHTTP/1.3, IRC/6.9, RTA/x11"
	 * @param upgrade the upgrade request
	 * @return this
	 */
	public Headers upgrade(String upgrade){headers.put("Upgrade", upgrade); return this;}

	//User-Agent
	/**
	 * Get the user agent String that is used to set the user agent of the requesting client
	 * @return the user agent String
	 */
	public String user_agent(){return headers.get("User-Agent");}
	/**
	 * Set the user agent String that is used to set the user agent of the requesting client
	 * @param user_agent the user agent String
	 * @return this
	 */
	public Headers user_agent(String user_agent){headers.put("User-Agent", user_agent); return this;}

	//Via
	/**
	 * Get which proxies the request was already sent through.
	 * @return the proxies String
	 */
	public String via(){return headers.get("Via");}
	/**
	 * Set which proxies the request was already sent through (may be used to trick the server, or something else).
	 * @param via the proxies String
	 * @return this
	 */
	public Headers via(String via){headers.put("Via", via); return this;}

	//Warning
	/**
	 * Get a general warning about possible problems with the entity body.
	 * @return the general warning
	 */
	public String warning(){return headers.get("Warning");}
	/**
	 * Set a general warning about possible problems with the entity body.
	 * @param warning the general warning
	 * @return this
	 */
	public Headers warning(String warning){headers.put("Warning", warning); return this;}

	//X-Requested-With
	/**
	 * Get the Object type of the request. For example: "XMLHttpRequest"
	 * @return the type of request Object
	 */
	public String x_requested_with(){return headers.get("X-Requested-With");}
	/**
	 * Set the Object type of the request. For example: "XMLHttpRequest"
	 * @param x_requested_with the type of request Object
	 * @return this
	 */
	public Headers x_requested_with(String x_requested_with){headers.put("X-Requested-With", x_requested_with); return this;}

}
