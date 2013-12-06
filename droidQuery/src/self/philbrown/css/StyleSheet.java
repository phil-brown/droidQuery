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

package self.philbrown.css;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import self.philbrown.droidQuery.$;
import self.philbrown.droidQuery.Function;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.osbcp.cssparser.CSSParser;
import com.osbcp.cssparser.PropertyValue;
import com.osbcp.cssparser.Rule;
import com.osbcp.cssparser.Selector;

/**
 * CSS StyleSheets
 * @author Phil Brown
 * @since 3:46:46 PM Dec 6, 2013
 *
 */
@SuppressWarnings("unused")
public class StyleSheet 
{
	/** All methods declared in this class. Used for reflection for handling CSS Property values. */
	private static Map<String, Method> methods = new HashMap<String, Method>();
	static
	{
		 Method[] _methods = StyleSheet.class.getDeclaredMethods();
		for (Method m : _methods)
		{
			methods.put(m.getName(), m);
		}
	}
	
	/**
	 * Keeps track of the keyframes used for each animation.
	 */
	private Map<String, KeyFrames> animationKeyFrames;
	
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
	 * @see CSSSelector
	 */
	public void applyRules(View layout)
	{
		//clean approach using CSSSelector.java
		
		for (Rule r : rules)
		{
			List<Selector> selectors = r.getSelectors();
			for (Selector s : selectors)
			{
				CSSSelector cssSelector = new CSSSelector();
				$ droidQuery = cssSelector.makeSelection(layout, s.toString());
				animationKeyFrames = cssSelector.getAnimationKeyFrames();
				applyProperties(droidQuery, r.getPropertyValues());
			}
			
		}
	}
	
	/**
	 * Recursively apply rules to view and subviews
	 * @param layout
	 * @param rules
	 * @see CSSSelector
	 */
	public void applyRules($ droidQuery)
	{
		//clean approach using CSSSelector.java
		
		for (Rule r : rules)
		{
			List<Selector> selectors = r.getSelectors();
			for (Selector s : selectors)
			{
				CSSSelector cssSelector = new CSSSelector();
				$ d = cssSelector.makeSelection(droidQuery, s.toString());
				animationKeyFrames = cssSelector.getAnimationKeyFrames();
				applyProperties(d, r.getPropertyValues());
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
		//apply actual css properties (big step here)
		for (PropertyValue prop : properties)
		{
			final String property = prop.getProperty();
			final String value = prop.getValue();
			droidQuery.each(new Function() {
				
				@Override
				public void invoke($ droidQuery, Object... params) {
					
					//need to fix this... 
					try
					{
						String _value = value.replace("-", "_");
						_value = value.replace("@", "at_symbol_");
						//these methods must be propert($, value)
						methods.get(property).invoke(StyleSheet.this, droidQuery, _value);
					}
					catch (Throwable t)
					{
						Log.w("CSS", String.format(Locale.US, "Could not set property named %s with value %s!", property, value));
					}
				}
			});
		}
	}
	
	private void background($ droidQuery, String value)
	{
		
	}
	
	
	
	/**
	 * <h1>@keyframes<h1>
	 * <h3>CSS 3</h1>
	 * {@literal @}keyframes animationname {keyframes-selector {css-styles;}}
	 * @param aniationname			Required. Defines the name of the animation.
	 * 
	 * @param keyframes-selector	Required. Percentage of the animation duration.
	 * 
	 * 								Legal values:
	 * 
	 * 								0-100%
	 * 								from (same as 0%)
	 * 								to (same as 100%)
	 * 
	 * 								<b>Note:<b> you can have many keyframes-selectors in one animation
	 * 
	 * @param css-styles			Required. One or more legal CSS style properties
	 */
	private void at_symbol_keyframes($ droidQuery, String value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	private void animation($ droidQuery, String value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	private void animation_duration($ droidQuery, String value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	private void animation_timing_function($ droidQuery, String value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	private void animation_delay($ droidQuery, String value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	private void animation_iteration_count($ droidQuery, String value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	private void animation_direction($ droidQuery, String value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	private void animation_play_state($ droidQuery, String value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	/**
	 * Used for handling the CSS Selector {@literal @}keyframes.
	 * @author Phil Brown
	 * @since 4:39:31 PM Dec 6, 2013
	 *
	 */
	public static class KeyFrames
	{
		private Map<Integer, String> frames = new HashMap<Integer, String>();
		public void addKeyFrame(int percent, String frame)
		{
			frames.put(percent, frame);
		}
		/**
		 * Get the frames. The key is a percent, and the value is a CSS-style string. 
		 * @return
		 */
		public Map<Integer, String> getFrames()
		{
			return frames;
		}
	}

}
