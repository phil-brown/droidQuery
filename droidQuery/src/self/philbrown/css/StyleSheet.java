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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import self.philbrown.cssparser.CSSParser;
import self.philbrown.cssparser.Declaration;
import self.philbrown.cssparser.DefaultCSSHandler;
import self.philbrown.cssparser.FontFace;
import self.philbrown.cssparser.KeyFrame;
import self.philbrown.cssparser.ParserConstants;
import self.philbrown.cssparser.RuleSet;
import self.philbrown.cssparser.Token;
import self.philbrown.cssparser.TokenSequence;
import self.philbrown.droidQuery.$;
import self.philbrown.droidQuery.AnimationOptions;
import self.philbrown.droidQuery.Function;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

/**
 * CSS StyleSheets
 * @author Phil Brown
 * @since 3:46:46 PM Dec 6, 2013
 *
 */
@SuppressWarnings("unused")
public class StyleSheet implements ParserConstants
{
	/**
	 * keeps track of styles by type
	 */
	private List<RuleSet> rules;
	/**
	 * Keeps track of the keyframes used for each animation.
	 */
	private Map<String, List<KeyFrame>> animationKeyFrames;
	
	/**
	 * Keeps track of fonts declared in css.
	 */
	private List<FontFace> fonts;
	
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
	 * Most efficient way to apply the CSS one time. This uses the css parser's stream parsing ability
	 * to optimize performance
	 * @param droidQuery
	 * @param in
	 */
	public static void applyCSS($ droidQuery, InputStream in)
	{
		//TODO: make this the only way to apply CSS
		
		//This may be the best way to handle things like keyframe animation, since it would require keeping
		//track of one animation at a time - so the current animation-name is only for the current element
		
		//it is also more efficient.
		//THIS COULD ALSO BE DONE IN A SEPARATE THREAD!!!!
		
	}
	
	protected StyleSheet(List<RuleSet> rules)
	{
		this(rules, null, null);
	}
	
	public StyleSheet(List<RuleSet> rules, Map<String, List<KeyFrame>> animationKeyFrames, List<FontFace> fonts)
	{
		this.rules = rules;
		this.animationKeyFrames = animationKeyFrames;
		this.fonts = fonts;
	}
	
	private static StyleSheet fromInputStream(InputStream in) throws Exception
	{
		final List<RuleSet> rules = new ArrayList<RuleSet>();
		final Map<String, List<KeyFrame>> animationKeyFrames = new HashMap<String, List<KeyFrame>>();
		final List<FontFace> fonts = new ArrayList<FontFace>();
		DefaultCSSHandler handler = new DefaultCSSHandler() {
			@Override
			public void handleRuleSet(RuleSet set)
			{
				rules.add(set);
			}
			@Override
			public void handleKeyframes(String identifier, List<KeyFrame> keyframes)
			{
				animationKeyFrames.put(identifier, keyframes);
			}
			@Override
			public void handleFontFace(FontFace font)
			{
				fonts.add(font);
			}
		};
		CSSParser parser = new CSSParser(in, handler);
		parser.parse();
		
        return new StyleSheet(rules, animationKeyFrames, fonts);
	}
	
	public static StyleSheet fromAsset(Context context, String assetPath) throws Exception
	{
		InputStream in = context.getAssets().open(assetPath);
		return fromInputStream(in);
		
	}
	
	public static StyleSheet fromString(String css) throws Exception
	{
		return fromInputStream(new ByteArrayInputStream(css.getBytes()));
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
		
		for (int i = 0; i < rules.size(); i++)
		{
			RuleSet rule = rules.get(i);
			TokenSequence selector = rule.getSelector();
			CSSSelector cssSelector = new CSSSelector();
			$ d = cssSelector.makeSelection(layout, selector);
			List<Declaration> declarations = rule.getDeclarationBlock();
			applyProperties(d, declarations);
		}
	}
//	
	/**
	 * Recursively apply rules to view and subviews
	 * @param layout
	 * @param rules
	 * @see CSSSelector
	 */
	public void applyRules($ droidQuery)
	{
		//clean approach using CSSSelector.java
		
		for (int i = 0; i < rules.size(); i++)
		{
			RuleSet rule = rules.get(i);
			TokenSequence selector = rule.getSelector();
			CSSSelector cssSelector = new CSSSelector();
			$ d = cssSelector.makeSelection(droidQuery, selector);
			List<Declaration> declarations = rule.getDeclarationBlock();
			applyProperties(d, declarations);
		}
	}
	
	/**
	 * Apply selector to the given view. This is not recursive.
	 * @param v
	 * @param properties
	 */
	public void applyProperties($ droidQuery, List<Declaration> declarations)
	{
		//apply actual css properties (big step here)
		for (int i = 0; i < declarations.size(); i++)
		{
			Declaration prop = declarations.get(i);
			final TokenSequence property = prop.getProperty();
			final TokenSequence value = prop.getValue();
			final AnimationOptions animation = new AnimationOptions();
			droidQuery.each(new Function() {
				
				@Override
				public void invoke($ droidQuery, Object... params) {
					
					try
					{
						//these methods must be propert($, value)
						Object result = methods.get(property.toString().replace("-", "_")).invoke(StyleSheet.this, droidQuery, value);
						if (property.startsWith("animation"))
						{
							//animation.
							//result will be AnimationOptions method?
						}
					}
					catch (Throwable t)
					{
						Log.w("CSS", String.format(Locale.US, "Could not set property named %s with value %s!", property, value), t);
					}
				}
			});
		}
	}
	
	//////////////////////////////////////////
	///       Animation Properties         ///
	//////////////////////////////////////////
	
	private void animation($ droidQuery, TokenSequence value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	private void animationDuration($ droidQuery, TokenSequence value) { animation_duration(droidQuery, value); }
	private void animation_duration($ droidQuery, TokenSequence value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	private void animationTimingFunction($ droidQuery, TokenSequence value) { animation_timing_function(droidQuery, value); }
	private void animation_timing_function($ droidQuery, TokenSequence value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	private void animationDelay($ droidQuery, TokenSequence value) { animation_delay(droidQuery, value); }
	private void animation_delay($ droidQuery, TokenSequence value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	private void animationIterationCount($ droidQuery, TokenSequence value) { animation_iteration_count(droidQuery, value); }
	private void animation_iteration_count($ droidQuery, TokenSequence value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	private void animationDirection($ droidQuery, TokenSequence value) { animation_direction(droidQuery, value); }
	private void animation_direction($ droidQuery, TokenSequence value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	private void animationPlayState($ droidQuery, TokenSequence value) { animation_play_state(droidQuery, value); }
	private void animation_play_state($ droidQuery, TokenSequence value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	//////////////////////////////////////////
	///      Background Properties         ///
	//////////////////////////////////////////
	
	private void background($ droidQuery, final TokenSequence value)
	{
		Log.w("CSS", "CSS \"background\" not supported yet. Use each attribute separately, such as \"background-color\".");
	}
	
	/**
	 * Sets the background color
	 * @param droidQuery
	 * @param value
	 */
	private void backgroundColor($ droidQuery, TokenSequence value) { background_color(droidQuery, value); }
	private void background_color($ droidQuery, final TokenSequence value)
	{
		int backgroundColor = Color.BLACK;
		try
		{
			backgroundColor = Color.parseColor(value.toString());
			
		}
		catch (IllegalArgumentException e)
		{
			Log.w("CSS", "Could not parse color \"" + value.toString() + "\". Defaulting to BLACK.");
		}
		final int color = backgroundColor;
		droidQuery.each(new Function() {
			
			@Override
			public void invoke($ droidQuery, Object... params) {
				droidQuery.view(0).setBackgroundColor(color);
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
	 * @throws IOException 
	 */
	private void backgroundImage($ droidQuery, TokenSequence value) throws IOException { background_image(droidQuery, value); }
	private void background_image($ droidQuery, final TokenSequence value) throws IOException
	{
		Context context = droidQuery.view(0).getContext();
		Enumeration<Token> tokens = value.enumerate();
		if (value.startsWith("R."))
		{
			TokenSequence[] split = value.split(new Token(DOT, null));
			
			final int resourceID = context.getResources().getIdentifier(split[2].toString(), split[1].toString(), null);
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
			droidQuery.image(value.subSequence(6, value.length()-1).toString());
		}
		else if (value.startsWith("url("))
		{
			droidQuery.image(value.subSequence(4, value.length()-1).toString());
		}
		else if (value.startsWith("file("))
		{
			droidQuery.image("file://" + value.subSequence(5, value.length()-1).toString());
		}
		else if (value.contains(TokenSequence.parse(":R")))
		{
			//drawable with package specified.
			TokenSequence[] split = value.split(new Token(COLON, null));

			String namespace = split[0].toString();
			split = split[1].split(new Token(DOT, null));
			
			final int resourceID = context.getResources().getIdentifier(split[2].toString(), split[1].toString(), namespace);
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
			final int resourceID = context.getResources().getIdentifier(value.toString(), "drawable", null);
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
	
	private void backgroundPosition($ droidQuery, TokenSequence value) { background_position(droidQuery, value); }
	public void background_position($ droidQuery, final TokenSequence value)
	{
		Log.w("CSS", "CSS \"background-position\" not supported yet. Use padding or margins.");
	}
	
}
