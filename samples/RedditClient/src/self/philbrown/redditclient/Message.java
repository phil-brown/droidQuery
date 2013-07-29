package self.philbrown.redditclient;

import self.philbrown.droidQuery.$;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * This Object represents the relevant information in a Reddit Post. It is {@link Parcelable} to allow
 * it to be passed amongst activities, which will work towards the scalablility of this application.
 * @author Phil Brown
 */
public class Message implements Parcelable
{
	/** The post text */
	private String title;
	/** The post author */
	private String author;
	/** The thumbnail image URL related to the post */
	private String thumbnail;
	
	/** Used to set the parcelable flags using binary or's, to describe which fields exist in this Message */
	private int flagTitle = 1,
			    flagAuthor = 2,
			    flagThumbnail = 4;
	
	@Override
	public int describeContents() {
		return 0 | ((title != null) ? flagTitle : 0) | ((author != null) ? flagAuthor : 0) | ((thumbnail != null) ? flagThumbnail : 0);
	}
	
	/**
	 * Constructor 
	 * @param title the post text
	 * @param author the post author
	 * @param thumbnail the related thumbnail URL (if any)
	 */
	public Message(String title, String author, String thumbnail)
	{
		this.title = title;
		this.author = author;
		this.thumbnail = thumbnail;
	}
	
	/**
	 * Constructor
	 * @param in the Parcel to unpack
	 */
	public Message(Parcel in)
	{
		title = in.readString();
		author = in.readString();
		String thumb = in.readString();
		if (!thumb.equals("null"))
			thumbnail = thumb;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) 
	{
		dest.writeString(title);
		dest.writeString(author);
		dest.writeString(thumbnail == null ? "null" : thumbnail);
	}

	/**
	 * Get the post text
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Get the post author
	 * @return
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * Get the thumbnail URL
	 * @return
	 */
	public String getThumbnail() {
		return thumbnail;
	}
	
	@Override
	public String toString()
	{
		return $.map($.entry("author", author), $.entry("title", title), $.entry("thumbnail", (thumbnail == null ? "null" : thumbnail))).toString();
	}

	/** This CREATOR is used to parcel this Object. */
	public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() 
    {
     
		/** Construct and return an Message from a Parcel*/
		@Override
		public Message createFromParcel(Parcel in) 
		{
			return new Message(in);
		}

		/**
		 * Creates a new array of Messages
		 */
		@Override
		public Message[] newArray(int size) 
		{
			return new Message[size];
		}
	};
	
}
