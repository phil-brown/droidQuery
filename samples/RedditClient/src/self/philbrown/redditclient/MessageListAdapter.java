package self.philbrown.redditclient;

import java.util.List;

import self.philbrown.droidQuery.$;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This adapter is used to inflate the cells in the list of Reddit posts.
 * @author Phil Brown
 */
public class MessageListAdapter extends ArrayAdapter<Message>
{
	/** The list of messages */
	private List<Message> messages;
	
	/** provides access to the layout inflater */
	private Context context;
	
	/**
	 * Constructor 
	 * @param context provides access to resources
	 * @param list the data to display
	 */
	public MessageListAdapter(Context context, List<Message> list)
	{
		super(context, 0, list);
		this.context = context;
		this.messages = list;
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) 
	{
		//because of the asynchronous nature of the image get requests, convertViews may cause some UI
		//errors by keeping references to the wrong cells. So for this app, it is better not to reuse
		//cells
        
		LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.cell, null);
		Message message = messages.get(position);
		TextView author = (TextView) layout.findViewById(R.id.author);
		author.setText("@" + message.getAuthor());
		author.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/bebasneue.ttf"));
		TextView title = (TextView) layout.findViewById(R.id.title);
		title.setText(message.getTitle());
		
		ImageView image = (ImageView) layout.findViewById(R.id.thumb);
		$.with(image).image(message.getThumbnail());
		
        return layout;
	}
}
