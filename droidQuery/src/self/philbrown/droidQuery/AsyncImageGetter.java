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
import java.net.MalformedURLException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html.ImageGetter;
import android.view.View;
import android.webkit.URLUtil;

/**
 * Based on the original source for <a href="http://stackoverflow.com/questions/7424512/android-html-imagegetter-as-asynctask">URLImageParser</a>,
 * this class allow for the Asynchronous retrieval of Bitmap images, and provides a fallback image to
 * display if the retrieval fails.
 * @author Phil Brown
 * @see TextDrawable
 */
public class AsyncImageGetter implements ImageGetter {
    private Context c;
    private View container;

    /***
     * Construct the URLImageParser which will execute the AsyncTask and refresh the parent container
     * @param t the parent container that will gain a child ImageView
     */
    public AsyncImageGetter(View t) {
        this.c = t.getContext();
        this.container = t;
    }

    /**
     * Gets the drawable from the given URL resource, or from the local file path.
     * @param source the URL of the drawable, or the local file path to the image.
     */
    public Drawable getDrawable(String source) {
    	
    	if (URLUtil.isValidUrl(source))
		{
			//need to download image
    		URLDrawable urlDrawable = new URLDrawable();

            // get the actual source
            ImageGetterAsyncTask asyncTask = 
                new ImageGetterAsyncTask( urlDrawable);

            asyncTask.execute(source);

            // return reference to URLDrawable where I will change with actual image from
            // the src tag
            return urlDrawable;
		}
		else
		{
			//must be a local reference
			Drawable drawFromPath;
			int path = c.getResources().getIdentifier(source, "drawable", c.getApplicationInfo().packageName);
			if (path == 0)
			{
				return new TextDrawable("Could not set image");
			}
			drawFromPath = (Drawable) c.getResources().getDrawable(path);
			drawFromPath.setBounds(0, 0, drawFromPath.getIntrinsicWidth(), drawFromPath.getIntrinsicHeight());
			return drawFromPath;
		}
    	
    	
        
    }

    /**
     * Asynchronously retrieves an Image from a URL and sets it as the background of
     * the parent view.
     */
    public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable>  {
        URLDrawable urlDrawable;

        public ImageGetterAsyncTask(URLDrawable d) {
            this.urlDrawable = d;
        }

        @Override
        protected Drawable doInBackground(String... params) {
            String source = params[0];
            return fetchDrawable(source);
        }

        @Override
        protected void onPostExecute(Drawable result) {
            // set the correct bound according to the result from HTTP call
            urlDrawable.setBounds(0, 0, 0 + result.getIntrinsicWidth(), 0 
                    + result.getIntrinsicHeight()); 

            // change the reference of the current drawable to the result
            // from the HTTP call
            urlDrawable.drawable = result;

            // redraw the image by invalidating the container
            AsyncImageGetter.this.container.invalidate();
        }

        /***
         * Get the Drawable from URL
         * @param urlString
         * @return
         */
        public Drawable fetchDrawable(String urlString) {
            try {
                InputStream is = fetch(urlString);
                Drawable drawable = Drawable.createFromStream(is, "src");
                drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(), 0 
                        + drawable.getIntrinsicHeight()); 
                return drawable;
            } catch (Exception e) {
                return null;
            } 
        }

        /**
         * This method does the actual retrieval of the image data stream
         * @param urlString
         * @return
         * @throws MalformedURLException
         * @throws IOException
         */
        private InputStream fetch(String urlString) throws MalformedURLException, IOException {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet(urlString);
            HttpResponse response = httpClient.execute(request);
            return response.getEntity().getContent();
        }
    }
}