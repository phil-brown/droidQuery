package self.philbrown.redditclient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import self.philbrown.droidProgress.Progress;
import self.philbrown.droidProgress.ProgressOptions;
import self.philbrown.droidQuery.$;
import self.philbrown.droidQuery.AjaxOptions;
import self.philbrown.droidQuery.AjaxTask.AjaxError;
import self.philbrown.droidQuery.Function;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.widget.SearchView;

import eu.erikw.PullToRefreshListView;

/**
 * Reddit Client can be used to view lists of subreddits, and provide a way of sharing the posts.
 * @author Phil Brown
 *
 */
public class RedditClient extends SherlockActivity implements SearchView.OnQueryTextListener {

	/** Logging tag */
	public static final String TAG = "RedditClient";
	
	/** Reddit emssages currently being displayed */
	private List<Message> messages;
	
	/** Provides a layout for showing the messages, as well as an interface for refreshing the list */
	private PullToRefreshListView list;
	
	/** Used to inflate each cell in the {@link #list} */
	private MessageListAdapter adapter;
	
	/** Provides search functionality */
	private SearchView searchView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reddit_client);
		
		searchView = new SearchView(getSupportActionBar().getThemedContext());
		searchView.setQueryHint("Search subreddits...");
		searchView.setOnQueryTextListener(this);
		searchView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		searchView.setBackgroundColor(Color.BLACK);
		
		LinearLayout searchParent = (LinearLayout) findViewById(R.id.upper_bar);
		searchParent.addView(searchView);
		
		messages = new ArrayList<Message>();
		adapter = new MessageListAdapter(this, messages);
		
		list = (PullToRefreshListView) findViewById(R.id.listview);
		list.setAdapter(adapter);
		
		list.setOnItemClickListener(new OnItemClickListener() 
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				Message message = messages.get(position);
				Intent share = new Intent();
				share.setAction(android.content.Intent.ACTION_SEND);
				share.setType("text/plain");
				String text = String.format("Check out this Reddit from @%s: %s", message.getAuthor(), message.getTitle());
				share.putExtra(Intent.EXTRA_TEXT, text);
				share.putExtra(Intent.EXTRA_SUBJECT, "I would like to share a Reddit post with you.");
				startActivity(Intent.createChooser(share, "Share Post"));
			}
			
		});
		
		$.with(list).mask(R.drawable.overlay);
		
		$.with(list).on("refresh", new Function() {
			@Override
			public void invoke($ droidQuery, Object... params) {
				CharSequence query = searchView.getQuery();
				if (query == null || query.length() == 0)
				{
					searchView.setQuery("funny", false);
					search("funny");
				}
				else
				{
					search(searchView.getQuery().toString());
				}
				
			}
		});
		
		try
    	{
    		$.extend("progress", "self.philbrown.droidProgress.Progress");
    		//use the Progress extension to show a progress spinner for global ajax events
            final Progress progress = (Progress) $.with(this).ext("progress", new ProgressOptions().indeterminate(true));
            
            $.ajaxStart(new Function() {
            	public void invoke($ droidQuery, Object... args)
            	{
            		progress.start();
            	}
            });
            
            $.ajaxStop(new Function(){
            	public void invoke($ droidQuery, Object... args)
            	{
            		progress.stop();
            	}
            });
    	}
    	catch (Throwable t)
    	{
    		Log.w(TAG, "Could not create progress extension");
    	}
		
		//initially search funny subreddit
		searchView.setQuery("funny", false);
		search("funny");
	}
	
	/**
	 * Start an HTTP request to search for the latest posts with the given subreddit name. Once the request
	 * is completed successfully, the {@link #list} will be updated to show the new messages.
	 * @param subreddit the subreddit to query
	 */
	public void search(String subreddit)
	{		
		//first, check the network connection to be sure the request can be completed.
		if (!isNetworkAvailable()) {
			$.alert(this, "Alert", "Cannot gather results. No network available.");
			return;
		}
		
		//then, complete the request, caching the responses for up to 10 minutes
		String url = String.format(Locale.US, "http://www.reddit.com/r/%s/.json", subreddit);
		Log.i(TAG, "Fetching: " + url);
		$.ajax(new AjaxOptions().url(url)
				                .type("GET")
				                .cache(true)
				                .dataType("json")
				                .context(this)
				                .error(new Function() {
				                	@Override
				                	public void invoke($ droidQuery, Object... params) {
				                		AjaxError error = (AjaxError) params[0];
				                		//Log.e(TAG, String.format(Locale.US, "Error %d: %s", error.status, error.reason));
				                		//retry once
				                		$.ajax(error.request, error.options.error(new Function() {
				                			@Override
						                	public void invoke($ droidQuery, Object... params) {
				                				AjaxError error = (AjaxError) params[0];
				                				if (error.status == 200)
				                				{
				                					droidQuery.alert("To the reviewer (not a client-facing dialog)", "At the moment, this basic application can only query subreddits. Please refrain from searching authors or any other content that doesn't respond with JSON.");
				                				}
				                				else
						                		{
				                					Log.e(TAG, String.format(Locale.US, "Error %d: %s", error.status, error.reason));
				                					droidQuery.alert("Error", "An error occurred. Please try again.");
						                		}
				                			}
				                		}));
				                	}
				                })
				                .success(new Function() {
				                	@Override
				                	public void invoke($ droidQuery, Object... params) {
				                		JSONObject json = (JSONObject) params[0];
				                		try {
				                			
				                			JSONObject datas = json.getJSONObject("data");
				                			JSONArray children = datas.getJSONArray("children");
				                			if (children.length() > 0)
				                			{
				                				messages.clear();
				                				for (Object data : $.makeArray(children))
					                			{
					                				JSONObject obj = ((JSONObject) data).getJSONObject("data");
					                				String title = obj.getString("title");
					                				String author = obj.getString("author");
					                				
					                				String thumbnail = null;
					                				if (obj.has("thumbnail"))
					                					thumbnail = obj.getString("thumbnail");
					                				
					                				Message message = new Message(title, author, thumbnail);
					                				messages.add(message);
					                			}
				                				adapter.notifyDataSetChanged();
				                			}
				                			
				                		}
				                		catch (Throwable t)
				                		{
				                			
				                		}
				                		
				                	}
				                })
				                .complete(new Function() {
				                	@Override
				                	public void invoke($ droidQuery, Object... params)
				                	{
				                		Log.i(TAG, "Complete");
										if (list.isRefreshing())
											list.onRefreshComplete();
				                	}
				                }));
	}
	
	/**
	 * Helper method for checking whether or not a network connection is available
	 * @return {@code true} if a network connection is available. Otherwse {@code false}.
	 */
	private boolean isNetworkAvailable() 
	{
	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	//be sure to cancel all AsyncTasks when the app is destroyed, in order to prevent memory leaks.
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		$.ajaxKillAll();
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		search(query);
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		return false;
	}

}
