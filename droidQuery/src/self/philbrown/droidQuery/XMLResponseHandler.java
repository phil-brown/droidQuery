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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.util.Log;

/**
 * Handle an HttpResponse as a XML Object.
 * @author Phil Brown
 * @see org.apache.http.impl.client.BasicResponseHandler
 */
public class XMLResponseHandler implements ResponseHandler<Document> 
{
	
	@Override
	public Document handleResponse(HttpResponse response) throws ClientProtocolException, IOException
	{
		StatusLine statusLine = response.getStatusLine();
		
        if (statusLine.getStatusCode() >= 300)
        {
        	Log.e("droidQuery", "HTTP Response Error " + statusLine.getStatusCode() + ":" + statusLine.getReasonPhrase());
        }

        final HttpEntity entity = response.getEntity();
        
        if (entity == null)
        	return null;
        
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			return factory.newDocumentBuilder().parse(AjaxUtil.getInputStream(entity));
		} catch (IllegalStateException e) {
			throw e;
		} catch (SAXException e) {
			throw new IOException();
		} catch (ParserConfigurationException e) {
			throw new IOException();
		} catch (NullPointerException e)  {
        	return null;
        }
	}
	
	public Document handleResponse(HttpURLConnection connection) throws ClientProtocolException, IOException
	{
		int statusCode = connection.getResponseCode();
		
        if (statusCode >= 300)
        {
        	Log.e("droidQuery", "HTTP Response Error " + statusCode + ":" + connection.getResponseMessage());
        }

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		InputStream is = null;
		try {
			is = AjaxUtil.getInputStream(connection);
			return factory.newDocumentBuilder().parse(is);
		} catch (IllegalStateException e) {
			throw e;
		} catch (SAXException e) {
			throw new IOException();
		} catch (ParserConfigurationException e) {
			throw new IOException();
		} catch (NullPointerException e)  {
        	return null;
        } finally {
        	if (is != null)
        		is.close();
        }
	}

}
