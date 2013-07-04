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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Parses a JSONArray of movies into a Java List of Movie Objects
 * @author Phil Brown
 *
 */
public class MoviesListParser 
{
	
	/**
	 * Parse the given list of movies and return them as a <em>Java</em> {@code List} of {@link Movie} Objects
	 * @param movies
	 * @return a List of Movie Objects (Java Object)
	 */
	public static List<Movie> parse(JSONArray movies)
	{
		List<Movie> list = new ArrayList<Movie>();
		
		for (int i = 0; i < movies.length(); i++)
		{
			try
			{
				JSONObject obj = movies.getJSONObject(i);
				Movie movie = new Movie();
				movie.setId(obj.getString("id"));
				movie.setTitle(obj.getString("title"));
				movie.setYear(obj.getInt("year"));
				movie.setMpaaRating(obj.getString("mpaa_rating"));
				movie.setRuntime(obj.getInt("runtime"));
				if (obj.has("critics_consensus"))
					movie.setBlurbs(obj.getString("critics_consensus"));
				
				JSONObject release_dates = obj.getJSONObject("release_dates");
				Map<String, String> datesByTheater = new HashMap<String, String>();
				
				if (release_dates.has("theater"))
				{
					datesByTheater.put("theater", release_dates.getString("theater"));
					movie.setReleaseDates(datesByTheater);
				}
				else
				{
					//it is not clear from the API docs if I can rely on the key "theater" (how generic!). 
					//This ensures I use only the correct keys
					Iterator<?> iterator = release_dates.keys();
					while(iterator.hasNext()){
						String key = (String) iterator.next();
						iterator.remove();
						datesByTheater.put(key, release_dates.getString(key));
					}
					movie.setReleaseDates(datesByTheater);
				}
				
				Movie.Rating rating = new Movie.Rating();
				JSONObject ratings = obj.getJSONObject("ratings");
				rating.criticsRating = "n/a";//ratings.getString("critics_rating");
				rating.criticsScore = ratings.getInt("critics_score");
				rating.audienceRating = ratings.getString("audience_rating");
				rating.audienceScore = ratings.getInt("audience_score");
				movie.setRatings(rating);
				
				movie.setSynopsis(obj.getString("synopsis"));
				
				Map<String, String> posters = new HashMap<String, String>();
				JSONObject posterList = obj.getJSONObject("posters");
				if (posterList.has("thumbnail"))
					posters.put("thumbnail", posterList.getString("thumbnail"));
				if (posterList.has("profile"))
					posters.put("profile", posterList.getString("profile"));
				if (posterList.has("detailed"))
					posters.put("detailed", posterList.getString("detailed"));
				if (posterList.has("original"))
					posters.put("original", posterList.getString("original"));
				movie.setPosters(posters);
				
				JSONArray cast = obj.getJSONArray("abridged_cast");
				List<Map<String, Object>> castMap = new ArrayList<Map<String, Object>>();
				for (int j = 0; j < cast.length(); j++)
				{
					JSONObject castMember = cast.getJSONObject(j);
					Map<String, Object> actor = new HashMap<String, Object>();
					actor.put("name", castMember.getString("name"));
					if (castMember.has("characters"))
					{
						JSONArray characters = castMember.getJSONArray("characters");
						String[] _chars = new String[characters.length()];
						for (int k = 0; k < characters.length(); k++)
						{
							_chars[k] = characters.getString(k);
						}
						actor.put("characters", _chars);
						castMap.add(actor);
					}
					
				}
				movie.setAbridgedCast(castMap);
				
//				JSONObject altIDs = obj.getJSONObject("alternate_ids");
//				Map<String, String> ids = new HashMap<String, String>();
//				Iterator<?> iterator = altIDs.keys();
//				while(iterator.hasNext()){
//					String key = (String) iterator.next();
//					ids.put(key, altIDs.getString(key));
//					iterator.remove();
//				}
//				movie.setAlternateIDs(ids);
				
				JSONObject _links = obj.getJSONObject("links");
				Map<String, String> links = new HashMap<String, String>();
				Iterator<?> iterator = _links.keys();
				while(iterator.hasNext()){
					String key = (String) iterator.next();
					links.put(key, _links.getString(key));
					iterator.remove();
				}
				movie.setLinks(links);
				
				list.add(movie);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
			
		}
		
		return list;
	}
}
