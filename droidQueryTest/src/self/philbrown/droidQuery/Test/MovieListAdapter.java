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

import java.util.List;

import self.philbrown.droidQuery.$;
import self.philbrown.droidQuery.Function;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * List adapter for displaying movie info in the list view
 * @author Phil Brown
 *
 */
public class MovieListAdapter extends ArrayAdapter<Movie>
{
	/**
	 * Background color of every other cell
	 */
	private static int customColor = Color.parseColor("#F2F2F2");
	
	/**
	 * Used to access resources
	 */
	private Context context;
	
	/**
	 * List of movies to display
	 */
	private List<Movie> list;
	
	/**
	 * Constructor
	 * @param context
	 * @param list
	 */
	public MovieListAdapter(Context context, List<Movie> list)
	{
		super(context, 0, list);
		this.context = context;
		this.list = list;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) 
	{
		////TODO: rework this method for use with droidQuery. TODO: droidQuery intents (or just use scripts?)
		//TODO: consider droidMail extension for something too.
		TextView view;
        if (convertView == null) 
        {
        	view = new TextView(context);//FIXME AnnotatedLayoutInflater.inflate(context, Cell.class, null);
        	view.setTag("");
        }
        else
        {
        	view = (TextView) convertView;
        }
        
        //set background color
        if (position % 2 == 0)
        {
        	view.setBackgroundColor(Color.WHITE);
        }
        else
        {
        	view.setBackgroundColor(customColor);
        }
        
        final Movie m = list.get(position);
        view.setText(m.getTitle());
        $.with(view).on("click", new Function(){
        	public void invoke(Object... args)
        	{
        		//TODO
        		$.with(context).alert(m.getTitle());
        	}
        });
//        
//        //handle clicks by opening the detail activity
//        view.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Intent intent = new Intent(context, DetailActivity.class);
//				//pass the movie at this position to the DetailActivity
//				intent.putExtra("movie", m);
//				//also pass the cell background color, which will be used to set the background color of the detail view
//				intent.putExtra("background", (position % 2 == 0 ? Color.WHITE : customColor));
//				context.startActivity(intent);
//				context.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//			}
//		});
//        
//        //title
//        TextView tv = (TextView) view.findViewById(R.id.movie_title);
//        tv.setText(m.getTitle());
//        
//        //thumbnail image
//        FrameLayout imageParent = (FrameLayout) view.findViewById(R.id.imageparent);
//        ViewParent vp = m.getThumbnail().getParent();
//        if (!view.getTag().equals(m.getId()))
//        {
//        	
//        	if (vp != null)
//        	{
//        		((ViewGroup) vp).removeView(m.getThumbnail());
//        	}
//        	imageParent.addView(m.getThumbnail());
//        }
//        else if (vp == null)
//        {
//        	imageParent.addView(m.getThumbnail());
//        }
//        
//        //used to determine if a reused cell needs to swap change
//        view.setTag(m.getId());
//        
//        //rating image
//        ImageView rating = (ImageView) view.findViewById(R.id.rating);
//        if (m.getMpaaRating().equalsIgnoreCase("G"))
//        {
//        	rating.setBackgroundResource(R.drawable.g);
//        }
//        else if (m.getMpaaRating().equalsIgnoreCase("PG"))
//        {
//        	rating.setBackgroundResource(R.drawable.pg);
//        }
//        else if (m.getMpaaRating().equalsIgnoreCase("PG-13"))
//        {
//        	rating.setBackgroundResource(R.drawable.pg_13);
//        }
//        else if (m.getMpaaRating().equalsIgnoreCase("R"))
//        {
//        	rating.setBackgroundResource(R.drawable.r);
//        }
//        else if (m.getMpaaRating().equalsIgnoreCase("NC-17"))
//        {
//        	rating.setBackgroundResource(R.drawable.nc_17);
//        }
//        
//        //score
//        ProgressBar score = (ProgressBar) view.findViewById(R.id.critic_score);
//        score.setProgress(m.getRatings().criticsScore);
//        
		return view;
	}
}
