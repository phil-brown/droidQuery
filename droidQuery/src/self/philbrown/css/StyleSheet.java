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
import android.graphics.Color;
import android.graphics.Typeface;
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
	 * Keeps track of fonts declared in css.
	 */
	private List<FontFace> fonts;
	
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
				if (s.toString().startsWith("@"))
				{
					handleSpecialSelectors(s.toString(), r.getPropertyValues());
				}
				else
				{
					CSSSelector cssSelector = new CSSSelector();
					$ droidQuery = cssSelector.makeSelection(layout, s.toString());
					applyProperties(droidQuery, r.getPropertyValues());
				}
				
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
				if (s.toString().startsWith("@"))
				{
					handleSpecialSelectors(s.toString(), r.getPropertyValues());
				}
				else
				{
					CSSSelector cssSelector = new CSSSelector();
					$ d = cssSelector.makeSelection(droidQuery, s.toString());
					applyProperties(d, r.getPropertyValues());
				}
				
			}
			
		}
	}
	
	/**
	 * Handles special selectors that contain information, such as keyframes or fonts
	 * @param selector
	 */
	private void handleSpecialSelectors(String selector, List<PropertyValue> propertyValues)
	{
		//FIXME: these are probably not supported by the parser currently! 
		Log.e("CSS", String.format(Locale.US, "Cannot handle special selector %s! Functionality coming soon.", selector));
//		if (selector.startsWith("@"))
//		{
//			if (selector.startsWith("@keyframes"))
//			{
//				
//				String body = selector.substring(10).trim();//FIXME: does this work???
//				int start = body.indexOf("{");//FIXME: is this even present after parsed by css parser?
//				String animationName = body.substring(0, start).trim();
//				KeyFrames kf = new KeyFrames();
//				body = body.substring(start, body.length()-1);
//				try
//				{
//					List<Rule> keyframes = CSSParser.parse(body);
//					for (Rule rule : keyframes)
//					{
//						for (Selector sel : rule.getSelectors())
//						{
//							int percentage = 0;
//							if (sel.equals("from"))
//								percentage = 0;
//							else if (sel.equals("to"))
//								percentage = 100;
//							else if (sel.toString().endsWith("%"))
//							{
//								try {
//									percentage = Integer.parseInt(sel.toString().replace("%", ""));
//								}
//								catch (Throwable t)
//								{
//									//invalid value
//									Log.w("CSS", "Cannot use invalid keyframe with key " + sel);
//									continue;
//								}
//							}
//							else
//							{
//								//invalid value
//								Log.w("CSS", "Cannot use invalid keyframe with key " + sel);
//								continue;
//							}
//							
//							StringBuilder css = new StringBuilder();
//							for (PropertyValue pv : rule.getPropertyValues())
//							{
//								css.append(" ").append(pv.toString());
//							}
//							kf.addKeyFrame(percentage, css.toString());
//						}
//					}
//					animationKeyFrames.put(animationName, kf);
//				}
//				catch (Throwable t)
//				{
//					Log.w("CSS", "Could not parse Keyframes");
//				}
//			}
//			else if (selector.startsWith("@font-face"))
//			{
//				//TODO: handle fonts!!!
//				
//			}
//		}
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
	
	private void background($ droidQuery, final String value)
	{
		//TODO all background_* properties
		Log.w("CSS", "CSS \"background\" not supported yet. Use each attribute separately, such as \"background-color\".");
	}
	
	/**
	 * Sets the background color
	 * @param droidQuery
	 * @param value
	 */
	private void background_color($ droidQuery, final String value)
	{
		droidQuery.each(new Function() {
			
			@Override
			public void invoke($ droidQuery, Object... params) {
				droidQuery.view(0).setBackgroundColor(Color.parseColor(value));
			}
		});
	}

	
	/**
	 * Sets the background image. To use a drawable, set as R.drawable.myDrawable. To use a url, use
	 * "background-image:url('http://www.example.com/img.jpg')". To use a asset, use asset(), and to
	 * use a local file, use file(). For example, the following CSS will work:
	 * <pre>
	 * #myButton {
	 *     background-image: R.drawable.background;/*also, just background will work - but using R prefix allows access to raw resources too *\/
	 *     /*also note that you can specify the package using something like: android:R.drawable.ic_menu_search. * /
	 * }
	 * 
	 * .TextView {
	 *     background-image: asset("images/white.9.png");
	 * }
	 * 
	 * #myImage {
	 *     background-image: url("http://www.example.com/example.png");
	 * }
	 * 
	 * #myView {
	 *     background-image: file("/sdcard/com.myapp.android/someImage.png");
	 * }
	 * </pre>
	 * 
	 * @param droidQuery
	 * @param value
	 */
	private void background_image($ droidQuery, final String value)
	{
		Context context = droidQuery.view(0).getContext();
		if (value.startsWith("R."))
		{
			String[] split = value.split(".");
			
			final int resourceID = context.getResources().getIdentifier(split[2], split[1], null);
			if (resourceID == 0)
			{
				Log.w("CSS", "Could not find Resource " + value);
				return;
			}
			
			droidQuery.each(new Function() {
				
				@Override
				public void invoke($ droidQuery, Object... params) {
					droidQuery.view(0).setBackgroundResource(resourceID);
				}
			});
		}
		else if (value.startsWith("asset("))
		{
			droidQuery.image(value.substring(6, value.length()-1));
		}
		else if (value.startsWith("url("))
		{
			droidQuery.image(value.substring(4, value.length()-1));
		}
		else if (value.startsWith("file("))
		{
			droidQuery.image("file://" + value.substring(5, value.length()-1));
		}
		else if (value.contains(":R"))
		{
			//drawable with package specified.
			String[] split = value.split(":");

			String namespace = split[0];
			split = split[1].split(".");
			
			final int resourceID = context.getResources().getIdentifier(split[2], split[1], namespace);
			if (resourceID == 0)
			{
				Log.w("CSS", "Could not find Resource " + value);
				return;
			}
			
			droidQuery.each(new Function() {
				
				@Override
				public void invoke($ droidQuery, Object... params) {
					droidQuery.view(0).setBackgroundResource(resourceID);
				}
			});
		}
		else
		{
			//assume R.drawable
			final int resourceID = context.getResources().getIdentifier(value, "drawable", null);
			if (resourceID == 0)
			{
				Log.w("CSS", "Could not find Resource " + value);
				return;
			}
			
			droidQuery.each(new Function() {
				
				@Override
				public void invoke($ droidQuery, Object... params) {
					droidQuery.view(0).setBackgroundResource(resourceID);
				}
			});
		}
		
		
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
	
	/**
	 * Used for handling the CSS Selector {@literal @}font-face
	 * @author Phil Brown
	 * @since 1:15:27 PM Dec 11, 2013
	 *
	 */
	public static class FontFace
	{
		Map<String, String> fonts = new HashMap<String, String>();
		public void addFontFace(String font_family, String src)
		{
			fonts.put(font_family, src);
		}
		public Map<String, String> getFonts()
		{
			return fonts;
		}
	}

}
