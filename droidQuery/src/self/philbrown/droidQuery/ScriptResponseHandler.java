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
import java.net.HttpURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;

import android.content.Context;
import android.util.Log;

/**
 * Handles a Script by executing it, and returning the result
 * @author Phil Brown
 * @see ScriptResponse
 * @see Script
 */
public class ScriptResponseHandler implements ResponseHandler<ScriptResponse>
{
	/** Used to execute the Script */
	private Context context;
	
	/**
	 * Constructor
	 * @param context
	 */
	public ScriptResponseHandler(Context context)
	{
		this.context = context;
	}

	@Override
	public ScriptResponse handleResponse(HttpResponse response) throws ClientProtocolException, IOException 
	{
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() >= 300)
        {
        	Log.e("droidQuery", "HTTP Response Error " + statusLine.getStatusCode() + ":" + statusLine.getReasonPhrase());
        }

        HttpEntity entity = response.getEntity();
        if (entity == null) 
        	return null;
        
        ScriptResponse script = new ScriptResponse();
        script.text = AjaxUtil.toString(entity);
        //new line characters currently represent a new command. 
        //Although one file can be all one line, it will be executed as a shell script.
        //for something else, the first line should contain be: #!<shell>\n, where <shell> points
        //to the shell that should be used.
        String[] commands = script.text.split("\n");
        script.script = new Script(context, commands);
        try {
			script.output = script.script.execute();
		} catch (Throwable t) {
			//could not execute script
			script.output = null;
		}
        return script;
	}
	
	public ScriptResponse handleResponse(HttpURLConnection connection) throws ClientProtocolException, IOException
	{
		int statusCode = connection.getResponseCode();
		
        if (statusCode >= 300)
        {
        	Log.e("droidQuery", "HTTP Response Error " + statusCode + ":" + connection.getResponseMessage());
        }

        ScriptResponse script = new ScriptResponse();
        InputStream stream = AjaxUtil.getInputStream(connection);
        script.text = Ajax.parseText(stream);
        //new line characters currently represent a new command. 
        //Although one file can be all one line, it will be executed as a shell script.
        //for something else, the first line should contain be: #!<shell>\n, where <shell> points
        //to the shell that should be used.
        String[] commands = script.text.split("\n");
        script.script = new Script(context, commands);
        try {
			script.output = script.script.execute();
		} catch (Throwable t) {
			//could not execute script
			script.output = null;
		} finally
		{
			if (stream != null) 
			{
				stream.close();
			}
		}
        return script;
	}
}
