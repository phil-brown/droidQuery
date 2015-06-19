package self.philbrown.droidQuery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

/**
 * Utilities for Ajax
 * @author Phil Brown
 * @since 11:37:10 AM, Jun 18, 2015
 *
 */
public class AjaxUtil {

	public static InputStream getInputStream(HttpURLConnection connection) throws IOException {
		String encoding = connection.getHeaderField("Content-Encoding");
		if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
			return new GZIPInputStream(connection.getInputStream());
		}
		else {
			return connection.getInputStream();
		}
	}
	
	public static InputStream getInputStream(HttpResponse response) throws IOException {
		HttpEntity entity = response.getEntity();
		return getInputStream(entity);
	}
	
	public static InputStream getInputStream(HttpEntity entity) throws IOException {
		Header encoding = entity.getContentEncoding();
		if (encoding != null) {
			HeaderElement[] codecs = encoding.getElements();
			for (HeaderElement codec : codecs) {
				if (codec.getName().equalsIgnoreCase("gzip")) {
					return new GZIPInputStream(entity.getContent());
				}
			}
		}
		return entity.getContent();
	}
	
	/**
	 * Converts the Entity to a String using the correct encoding.
	 * @param entity
	 * @return
	 * @throws IOException 
	 * @see EntityUtils
	 */
	public static String toString(HttpEntity entity) throws IOException {
		InputStream input = getInputStream(entity);
		BufferedReader br = new BufferedReader(new InputStreamReader(input));
		StringBuilder builder = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			builder.append(line).append("\n");
		}
		br.close();
		input.close();
		return builder.toString();
	}
}
