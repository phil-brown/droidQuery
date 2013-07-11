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

package self.philbrown.droidQuery.Test;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONObject;

import self.philbrown.droidProgress.Progress;
import self.philbrown.droidProgress.ProgressOptions;
import self.philbrown.droidQuery.$;
import self.philbrown.droidQuery.AjaxOptions;
import self.philbrown.droidQuery.Function;
import self.philbrown.droidQuery.Example.R;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import eu.erikw.PullToRefreshListView;

/**
 * Test Activity for droidQuery.
 * @author Phil Brown
 * @deprecated use {@link self.philbrown.droidQuery.ExampleActivity ExampleActivity}
 */
public class DroidQueryTestActivity extends Activity {
	
	private String url = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/box_office.json?apikey=dahanc6cbk9sgknn6wv54dre&limit=10";

	/**
	 * This is the list of movies that are displayed in the {@link #list}.
	 */
	private List<Movie> movies;
	
	/**
	 * This adapter provides a way to inflate each movie as a cell with relevant, unique UI
	 */
	private MovieListAdapter adapter;
	
	private $ list;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //set droidQuery Extensions
        try
        {
        	$.extend("progress", "self.philbrown.droidProgress.Progress");
        	$.extend("mail", "self.philbrown.droidMail.$Mail");
        }
        catch (Throwable t)
        {
        	//some extensions failed.
        	t.printStackTrace();
        }
        
        list = $.with(this);
        list.id(R.id.listview);
        
        movies = new ArrayList<Movie>();
		adapter = new MovieListAdapter(this, movies);
		
		final Progress progress = (Progress) $.with(this).ext("progress", new ProgressOptions().indeterminate(true));
        
        $.ajaxStart(new Function() {
        	public void invoke(Object... args)
        	{
        		Log.i("Ajax Test", "Global start");
        		progress.start();
        	}
        });
        
        $.ajaxStop(new Function(){
        	public void invoke(Object... args)
        	{
        		Log.i("Ajax Test", "Global stop");
        		progress.stop();
        	}
        });
        
        
        list.on("refresh", new Function(){
        	public void invoke(Object... args)
        	{
        		refresh((View) args[0]);
        	}
        });
        
        list.bind("itemClick", null, new Function() {
        	public void invoke(Object... args)
        	{
        		list.alert("clicked an item");
        	}
        });
        
//        list.dblclick(new Function() {//FIXME: needs work - over sensitive.
//        	public void invoke(Object... args)
//        	{
//        		list.alert("dblclick");
//        	}
//        });
        
        refresh(list.view(0));
    }
    
    private void refresh(View v)
    {
    	//$.with(DroidQueryTestActivity.this).alert("Refreshing View");
		final PullToRefreshListView view = (PullToRefreshListView) v;
		$.ajax(new AjaxOptions().url(url)
				                .type("GET")
				                .dataType("json")
				                .beforeSend(new Function() {
				                	public void invoke(Object... args)
				                	{
				                		Log.i("ajax test", "get request beginning");
				                	}
				                })
				                .success(new Function(){
				                	
				                	int counter = 0;
				                	
				                	public void invoke(Object... args)
				                	{
				                		try
				                		{
				                			JSONObject response = (JSONObject) args[0];
    				                		String status = (String) args[1];
    				                		Log.i("Ajax Test", "status: " + status);
    				                		Log.i("Ajax Test", "RESPONSE: " + response.toString(2));
    				                		//parse JSON response
    				                		
    				                		final List<Movie> newMovies = MoviesListParser.parse(response.getJSONArray("movies"));
    				    					movies.clear();
    				    					
    				    					counter = 0;
    				    					for (final Movie m : newMovies)
    				    					{
    				    						$.ajax(new AjaxOptions(m.getPosters().get("thumbnail")).type("GET").dataType("image").success(new Function(){
	    				    							public void invoke(Object... args)
	    				    							{
	    				    								Bitmap response = (Bitmap) args[0];
	    				    								String status = (String) args[1];
	    				    								ImageView iv = new ImageView(DroidQueryTestActivity.this);
	    				    								iv.setImageBitmap(response);
	    				    								m.setThumbnail(iv);
	    				    							}
    				    							}).complete(new Function() {
	    				    							public void invoke(Object... args) {
	    				    								counter++;
	    				    								movies.add(m);
	    				    								if (counter == newMovies.size()) 
	    				    								{
	    				    									adapter.notifyDataSetChanged();
	    				    									if (view.isRefreshing())
	    				    										view.onRefreshComplete();
	    				    								}
	    				    							}
    				    						}));
    				    					}
				                		}
				                		catch (Throwable t)
				                		{
				                			$.with(DroidQueryTestActivity.this).alert("Could not handle malformed ajax response.");
				                		}
				                		
				                	}
				                })
				                .error(new Function() {
				                	public void invoke(Object... args)
				                	{
				                		HttpUriRequest request = (HttpUriRequest) args[0];
				                		int statusCode = (Integer) args[1];
				                		String reason = (String) args[2];
				                		Log.i("ajax test", "ERROR: " + reason);
				                	}
				                })
				                .complete(new Function() {
				                	public void invoke(Object... args)
				                	{
				                		Log.i("ajax test", "get request complete");
				                		if (view.isRefreshing())
				            				view.onRefreshComplete();
				                	}
				                }));
    }
}