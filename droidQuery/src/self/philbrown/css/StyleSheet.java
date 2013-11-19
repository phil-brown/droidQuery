package self.philbrown.css;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import self.philbrown.droidQuery.$;
import android.content.Context;
import android.view.View;

import com.osbcp.cssparser.CSSParser;
import com.osbcp.cssparser.PropertyValue;
import com.osbcp.cssparser.Rule;
import com.osbcp.cssparser.Selector;

public class StyleSheet {

	/**
	 * keeps track of styles by type
	 */
	private List<Rule> rules;
	
	private StyleSheet(List<Rule> rules){
		this.rules = rules;
		//TODO: add optimization by setting all values now
	}
	
	public static StyleSheet fromAsset(Context context, String assetPath) throws Exception
	{
		InputStream in = context.getAssets().open(assetPath);
		
		InputStreamReader stream = new InputStreamReader(in);
		
		BufferedReader inputReader = new BufferedReader(stream);
		String line = null;
		
		StringBuilder builder = new StringBuilder();
        while ((line = inputReader.readLine()) != null) 
        {
        	builder.append(line);
        }
        inputReader.close();
        stream.close();
        in.close();

        return new StyleSheet(CSSParser.parse(builder.toString()));
	}
	
	public static StyleSheet fromString(String css) throws Exception
	{
		return new StyleSheet(CSSParser.parse(css));
	}
	
	/**
	 * Recursively apply rules to view and subviews
	 * @param layout
	 * @param rules
	 */
	public void applyRules(View layout, List<Rule> rules)
	{
		//clean approach using CSSSelector.java
		
		for (Rule r : rules)
		{
			List<Selector> selectors = r.getSelectors();
			for (Selector s : selectors)
			{
				applyProperties(CSSSelector.makeSelection(layout, s.toString()), r.getPropertyValues());
			}
			
		}
	}
	
	/**
	 * Apply selector to the given view. This is not recursive.
	 * @param v
	 * @param properties
	 */
	public void applyProperties($ droidQuery, List<PropertyValue> properties)
	{
		//TODO apply actual css properties (big step here)
		
	}

}
