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
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * HTTPClient Headers
 * @author Phil Brown
 *
 */
public class Headers 
{
	protected Map<String, String> headers;
	
	public Headers()
	{
		headers = new HashMap<String, String>();
	}
	
	public Headers(Map<String, String> headers)
	{
		this.headers = headers;
	}
	
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
	
	public Headers(JSONObject json) throws JSONException
	{
		this();
		unpackJSON(json);
	}

	//unpack JSON into map
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
	
	public Map<String, String> map()
	{
		return headers;
	}
	
	/** add any header by string */
	public void add(String key, String value)
	{
		headers.put(key, value);
	}
	
	/** OR add a header using the below: */
	
	//Accept
	public String accept(){return headers.get("Accept");}
	public Headers accept(String accept){headers.put("Accept", accept); return this;}

	//Accept-Charset
	public String accept_charset(){return headers.get("Accept-Charset");}
	public Headers accept_charset(String accept_charset){headers.put("Accept-Charset", accept_charset); return this;}

	//Accept-Encoding
	public String accept_encoding(){return headers.get("Accept-Encoding");}
	public Headers accept_encoding(String accept_encoding){headers.put("Accept-Encoding", accept_encoding); return this;}

	//Accept-Language
	public String accept_language(){return headers.get("Accept-Language");}
	public Headers accept_language(String accept_language){headers.put("Accept-Language", accept_language); return this;}

	//Accept-Datetime
	public String accept_datetime(){return headers.get("Accept-Datetime");}
	public Headers accept_datetime(String accept_datetime){headers.put("Accept-Datetime", accept_datetime); return this;}

	//Authorization
	public String authorization(){return headers.get("Authorization");}
	public Headers authorization(String authorization){headers.put("Authorization", authorization); return this;}

	//Cache-Control
	public String cache_control(){return headers.get("Cache-Control");}
	public Headers cach_control(String cache_control){headers.put("Cache-Control", cache_control); return this;}

	//Connection
	public String connection(){return headers.get("Connection");}
	public Headers connection(String connection){headers.put("Connection", connection); return this;}

	//Cookie
	public String cookie(){return headers.get("Cookie");}
	public Headers cookie(String cookie){headers.put("Cookie", cookie); return this;}

	//Content-Length
	public String content_length(){return headers.get("Content-Length");}
	public Headers content_length(String content_length){headers.put("Content-Length", content_length); return this;}

	//Content-MD5
	public String content_md5(){return headers.get("Content-MD5");}
	public Headers content_md5(String content_md5){headers.put("Content-MD5", content_md5); return this;}

	//Content-Type
	public String content_type(){return headers.get("Content-Type");}
	public Headers content_type(String content_type){headers.put("Content-Type", content_type); return this;}

	//Date
	public String date(){return headers.get("Date");}
	public Headers date(String date){headers.put("Date", date); return this;}

	//Expect
	public String expect(){return headers.get("Expect");}
	public Headers expect(String expect){headers.put("Expect", expect); return this;}

	//From
	public String from(){return headers.get("From");}
	public Headers from(String from){headers.put("From", from); return this;}

	//Host
	public String host(){return headers.get("Host");}
	public Headers host(String host){headers.put("Host", host); return this;}

	//If-Match
	public String if_match(){return headers.get("If-Match");}
	public Headers if_match(String if_match){headers.put("If-Match", if_match); return this;}

	//If-Modified-Since
	public String if_modified_since(){return headers.get("If-Modified-Since");}
	public Headers if_modified_since(String if_modified_since){headers.put("If-Modified-Since", if_modified_since); return this;}

	//If-None-Match
	public String if_none_match(){return headers.get("If-None-Match");}
	public Headers if_none_match(String if_none_match){headers.put("If-None-Match", if_none_match); return this;}

	//If-Range
	public String if_range(){return headers.get("If-Range");}
	public Headers if_range(String if_range){headers.put("If-Range", if_range); return this;}

	//If-Unmodified-Since
	public String if_unmodified_since(){return headers.get("If-Unmodified-Since");}
	public Headers if_unmodified_since(String if_unmodified_since){headers.put("If-Unmodified-Since", if_unmodified_since); return this;}

	//Max-Forwards
	public String max_forwards(){return headers.get("Max-Forwards");}
	public Headers max_forwards(String max_forwards){headers.put("Max-Forwards", max_forwards); return this;}

	//Origin
	public String origin(){return headers.get("Origin");}
	public Headers origin(String origin){headers.put("Origin", origin); return this;}

	//Pragma
	public String pragma(){return headers.get("Pragma");}
	public Headers pragma(String pragma){headers.put("Pragma", pragma); return this;}

	//Proxy-Authorization
	public String proxy_authorization(){return headers.get("Proxy-Authorization");}
	public Headers proxy_authorization(String proxy_authorization){headers.put("Proxy-Authorization", proxy_authorization); return this;}

	//Range
	public String range(){return headers.get("Range");}
	public Headers range(String range){headers.put("Range", range); return this;}

	//Referer
	public String referer(){return headers.get("Referer");}
	public Headers referer(String referer){headers.put("Referer", referer); return this;}

	//TE
	public String te(){return headers.get("TE");}
	public Headers te(String te){headers.put("TE", te); return this;}

	//Upgrade
	public String upgrade(){return headers.get("Upgrade");}
	public Headers upgrade(String upgrade){headers.put("Upgrade", upgrade); return this;}

	//User-Agent
	public String user_agent(){return headers.get("User-Agent");}
	public Headers user_agent(String user_agent){headers.put("User-Agent", user_agent); return this;}

	//Via
	public String via(){return headers.get("Via");}
	public Headers via(String via){headers.put("Via", via); return this;}

	//Warning
	public String warning(){return headers.get("Warning");}
	public Headers warning(String warning){headers.put("Warning", warning); return this;}

	//X-Requested-With
	public String x_requested_with(){return headers.get("X-Requested-With");}
	public Headers x_requested_with(String x_requested_with){headers.put("X-Requested-With", x_requested_with); return this;}





}
