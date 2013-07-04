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
import java.util.List;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

/**
 * Defines a Movie Object. All fields are derived from the Rotten Tomatoes API doc. This Object
 * is also Parcelable, so that instances can be passed between Activities.
 * @author Phil Brown
 *
 */
public class Movie implements Parcelable
{

	protected String id;
	protected String title;
	protected int year;
	protected String MpaaRating;
	protected int runtime;
	protected String blurbs;
	protected Map<String, String> releaseDates;
	protected Rating ratings;
	protected String synopsis;
	protected Map<String, String> posters;
	protected List<Map<String, Object>> abridgedCast;
	protected Map<String, String> alternateIDs;
	protected Map<String, String> links;
	/**
	 * contains the thumbnail displayed in the list cell
	 */
	protected ImageView thumbnail;
	

	/**
	 * Default constructor
	 */
	public Movie()
	{
		
	}
	
	public Movie(Parcel in)
	{
		id = in.readString();
		title = in.readString();
		year = in.readInt();
		MpaaRating = in.readString();
		runtime = in.readInt();
		blurbs = in.readString();
		releaseDates = new HashMap<String, String>();
		in.readMap(releaseDates, getClass().getClassLoader());
		Map<String, Object> temp = new HashMap<String, Object>();
		in.readMap(temp, getClass().getClassLoader());
		ratings = new Rating(temp);
		synopsis = in.readString();
		posters = new HashMap<String, String>();
		in.readMap(posters, getClass().getClassLoader());
		abridgedCast = new ArrayList<Map<String, Object>>();
		in.readList(abridgedCast, getClass().getClassLoader());
		alternateIDs = new HashMap<String, String>();
		in.readMap(alternateIDs, getClass().getClassLoader());
		links = new HashMap<String, String>();
		in.readMap(links, getClass().getClassLoader());
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) 
	{
		dest.writeString(id);
		dest.writeString(title);
		dest.writeInt(year);
		dest.writeString(MpaaRating);
		dest.writeInt(runtime);
		dest.writeString(blurbs);
		dest.writeMap(releaseDates);
		dest.writeMap(ratings.toMap());
		dest.writeString(synopsis);
		dest.writeMap(posters);
		dest.writeList(abridgedCast);
		dest.writeMap(alternateIDs);
		dest.writeMap(links);
		
		//do not bother with thumbnail. The URL is included in posters, and for this project it is not needed.
	}
	
	@Override
	public String toString()
	{
		return title + " (" + year + ")";
	}
	
	public String getId() {
		return id;
	}



	public void setId(String id) {
		this.id = id;
	}



	public String getTitle() {
		return title;
	}



	public void setTitle(String title) {
		this.title = title;
	}



	public int getYear() {
		return year;
	}



	public void setYear(int year) {
		this.year = year;
	}



	public String getMpaaRating() {
		return MpaaRating;
	}



	public void setMpaaRating(String mpaaRating) {
		MpaaRating = mpaaRating;
	}



	public int getRuntime() {
		return runtime;
	}



	public void setRuntime(int runtime) {
		this.runtime = runtime;
	}



	public String getBlurbs() {
		return blurbs;
	}



	public void setBlurbs(String blurbs) {
		this.blurbs = blurbs;
	}



	public Map<String, String> getReleaseDates() {
		return releaseDates;
	}



	public void setReleaseDates(Map<String, String> releaseDates) {
		this.releaseDates = releaseDates;
	}



	public Rating getRatings() {
		return ratings;
	}



	public void setRatings(Rating ratings) {
		this.ratings = ratings;
	}



	public String getSynopsis() {
		return synopsis;
	}



	public void setSynopsis(String synopsis) {
		this.synopsis = synopsis;
	}



	public Map<String, String> getPosters() {
		return posters;
	}



	public void setPosters(Map<String, String> posters) {
		this.posters = posters;
	}



	public List<Map<String, Object>> getAbridgedCast() {
		return abridgedCast;
	}



	public void setAbridgedCast(List<Map<String, Object>> abridgedCast) {
		this.abridgedCast = abridgedCast;
	}



	public Map<String, String> getAlternateIDs() {
		return alternateIDs;
	}



	public void setAlternateIDs(Map<String, String> alternateIDs) {
		this.alternateIDs = alternateIDs;
	}



	public Map<String, String> getLinks() {
		return links;
	}



	public void setLinks(Map<String, String> links) {
		this.links = links;
	}
	
	public ImageView getThumbnail()
	{
		return thumbnail;
	}

	public void setThumbnail(ImageView thumb)
	{
		this.thumbnail = thumb;
	}

	public static class Rating
	{
		public String criticsRating;
		public int criticsScore;
		public String audienceRating;
		public int audienceScore;
		
		public Map<String, Object> toMap()
		{
			Map<String, Object> map = new HashMap<String, Object>();
			if (criticsRating != null)
				map.put("criticsRating", criticsRating);
			map.put("criticsScore", new Integer(criticsScore));
			if (audienceRating != null)
				map.put("audienceRating", audienceRating);
			map.put("audienceScore", new Integer(audienceScore));
			return map;
			
		}
		
		public Rating(){}
		public Rating(Map<String, Object> map)
		{
			Object temp = map.get("criticsRating");
			if (temp != null)
				criticsRating = (String) temp;
			criticsScore = (Integer) map.get("criticsScore");
			temp = map.get("audienceRating");
			if (temp != null)
				audienceRating = (String) temp;
			audienceScore = (Integer) map.get("audienceScore");
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}
	
	/** This CREATOR is used to parcel this Object. */
	public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() 
    {
     
		/** Construct and return an Movie from a Parcel*/
		@Override
		public Movie createFromParcel(Parcel in) 
		{
			return new Movie(in);
		}
		
		/**
		 * Creates a new array of Movies
		 */
		@Override
		public Movie[] newArray(int size) 
		{
			return new Movie[size];
		}
	};
	
}

