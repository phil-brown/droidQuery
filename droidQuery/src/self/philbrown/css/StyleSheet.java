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

import self.philbrown.cssparser.CSSHandler;
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
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;

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
	public static void applyCSS(final $ droidQuery, InputStream in)
	{
		//TODO: make this the only way to apply CSS
		
		//This may be the best way to handle things like keyframe animation, since it would require keeping
		//track of one animation at a time - so the current animation-name is only for the current element(s)
		
		//it is also more efficient.
		//THIS COULD ALSO BE DONE IN A SEPARATE THREAD!!!!
		
		final Handler ui = new Handler();
		CSSHandler handler = new AsyncCSSHandler(droidQuery, ui);
		AsyncCSSParser parser = new AsyncCSSParser(in, handler);
		parser.execute();
		
		
	}
	
	public StyleSheet(List<RuleSet> rules)
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
	public static void applyProperties($ droidQuery, List<Declaration> declarations)
	{
		//apply actual css properties (big step here)
		for (int i = 0; i < declarations.size(); i++)
		{
			Declaration prop = declarations.get(i);
			final TokenSequence property = prop.getProperty();
			final TokenSequence value = prop.getValue();
			final AnimationOptions animation = new AnimationOptions();
			//FIXME: this does not seem optimal!
			droidQuery.each(new Function() {
				
				@Override
				public void invoke($ droidQuery, Object... params) {
					
					try
					{
						//these methods must be propert($, value)
						String key = property.toString().replace("-", "_");
						if (key.equalsIgnoreCase("float"))
							key = "_float";
						Object result = methods.get(key).invoke(null, droidQuery, value);
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
	
	public static void animation($ droidQuery, TokenSequence value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	public static void animationDuration($ droidQuery, TokenSequence value) { animation_duration(droidQuery, value); }
	public static void animation_duration($ droidQuery, TokenSequence value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	public static void animationTimingFunction($ droidQuery, TokenSequence value) { animation_timing_function(droidQuery, value); }
	public static void animation_timing_function($ droidQuery, TokenSequence value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	public static void animationDelay($ droidQuery, TokenSequence value) { animation_delay(droidQuery, value); }
	public static void animation_delay($ droidQuery, TokenSequence value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	public static void animationIterationCount($ droidQuery, TokenSequence value) { animation_iteration_count(droidQuery, value); }
	public static void animation_iteration_count($ droidQuery, TokenSequence value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	public static void animationDirection($ droidQuery, TokenSequence value) { animation_direction(droidQuery, value); }
	public static void animation_direction($ droidQuery, TokenSequence value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	public static void animationPlayState($ droidQuery, TokenSequence value) { animation_play_state(droidQuery, value); }
	public static void animation_play_state($ droidQuery, TokenSequence value)
	{
		Log.w("CSS", "CSS Animations not implemented.");
	}
	
	//////////////////////////////////////////
	///      Background Properties         ///
	//////////////////////////////////////////
	
	public static void background($ droidQuery, final TokenSequence value)
	{
		Log.w("CSS", "CSS \"background\" not supported (yet?). Use each attribute separately, such as \"background-color\".");
	}
	
	/**
	 * Sets the background color
	 * @param droidQuery
	 * @param value
	 */
	public static void backgroundColor($ droidQuery, TokenSequence value) { background_color(droidQuery, value); }
	public static void background_color($ droidQuery, final TokenSequence value)
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
	public static void backgroundImage($ droidQuery, TokenSequence value) throws IOException { background_image(droidQuery, value); }
	public static void background_image($ droidQuery, final TokenSequence value) throws IOException
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
	
	public static void backgroundPosition($ droidQuery, TokenSequence value) { background_position(droidQuery, value); }
	public static void background_position($ droidQuery, final TokenSequence value)
	{
		TokenSequence[] split = value.split(new Token(SPACE));
		if (split[0].toString().equalsIgnoreCase("inherit"))
		{
			//get these values from the view's parents
			droidQuery.each(new Function() {
				
				@Override
				public void invoke($ droidQuery, Object... params) {
					View v = droidQuery.view(0);
					if (v.getBackground() == null)
						return;
					ViewParent parent = v.getParent();
					if (parent != null && parent instanceof View)
					{
						View p = (View) parent;
						if (p.getBackground() != null)
							v.getBackground().setBounds(p.getBackground().getBounds());
						else
						{
							DisplayMetrics metrics = v.getContext().getResources().getDisplayMetrics();
							v.getBackground().setBounds(new Rect(0,0,metrics.widthPixels, metrics.heightPixels));
						}
					}
					else
					{
						//use visible size
						DisplayMetrics metrics = v.getContext().getResources().getDisplayMetrics();
						v.getBackground().setBounds(new Rect(0,0,metrics.widthPixels, metrics.heightPixels));
					}
				}
				
			});
		}
		else if (split[0].endsWith(new Token(PERCENT)) || split[0].startsWith(new Token(NUMBER)))
		{
			final String x = split[0].toString();
			final String y = split.length == 2 ? split[1].toString() : "0px";
			droidQuery.each(new Function() {
				
				@Override
				public void invoke($ droidQuery, Object... params) {
					Number left = droidQuery.getAnimationValue(droidQuery.view(0), "left", x);
					Number top = droidQuery.getAnimationValue(droidQuery.view(0), "top", y);
					Rect r = droidQuery.view(0).getBackground().getBounds();
					r.offset((Integer) left, (Integer) top);
					droidQuery.view(0).getBackground().setBounds(r);
				}
				
			});
		}
		else
		{
			int x = 0;
			int y = 0;
			String a = split[0].toString();
			String b = "center";
			if (split.length == 2)
				b = split[1].toString();
			if (a.equalsIgnoreCase("left"))
				x = 0;
			else if (a.equalsIgnoreCase("right"))
				x = 100;
			else if (a.equalsIgnoreCase("center"))
				x = 50;
			if (b.equalsIgnoreCase("top"))
				y = 0;
			else if (b.equalsIgnoreCase("center"))
				y = 50;
			else if (b.equalsIgnoreCase("bottom"))
				y = 100;
			
			final int _x = x;
			final int _y = y;
			droidQuery.each(new Function() {
				
				@Override
				public void invoke($ droidQuery, Object... params) {
					View v = droidQuery.view(0);
					Drawable d = v.getBackground();
					if (d == null)
						return;
					Rect r = d.getBounds();
					ViewParent parent = v.getParent();
					int h = 0;
					int w = 0;
					
					if (parent != null && parent instanceof View)
					{
						h = ((View) parent).getHeight();
						w = ((View) parent).getWidth();
					}
					else
					{
						DisplayMetrics metrics = v.getContext().getResources().getDisplayMetrics();
						h = metrics.heightPixels;
						w = metrics.widthPixels;
					}
					r.offsetTo(_x == 0 ? 0 : _y == 50 ? w/2-r.width()/2 : w-r.width(), _y == 0 ? 0 : _y == 50 ? h/2-r.height()/2 : h-r.height());
					d.setBounds(r);
					
				}
				
			});
			
		}
	}
	
	public static void backgroundRepeat($ droidQuery, TokenSequence value) { background_repeat(droidQuery, value); }
	public static void background_repeat($ droidQuery, final TokenSequence value)
	{
		final String string = value.toString();
		droidQuery.each(new Function() {
			@Override
			public void invoke($ d, Object... args) 
			{
				try
				{
					Drawable background = d.view(0).getBackground();
					BitmapDrawable drawable = null;
					if (background instanceof BitmapDrawable)
					{
						drawable = (BitmapDrawable) background;
					}
					else
					{
						Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Config.ARGB_8888);
						Canvas canvas = new Canvas(bitmap);
						background.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
						background.draw(canvas);
						drawable = new BitmapDrawable(bitmap);
					}
					
					if (string.equalsIgnoreCase("repeat"))
					{
						drawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
					}
					else if (string.equalsIgnoreCase("repeat-x"))
					{
						drawable.setTileModeXY(TileMode.REPEAT, TileMode.CLAMP);
					}
					else if (string.equalsIgnoreCase("repeat-y"))
					{
						drawable.setTileModeXY(TileMode.CLAMP, TileMode.REPEAT);
					}
					else if (string.equalsIgnoreCase("no-repeat"))
					{
						drawable.setTileModeXY(TileMode.CLAMP, TileMode.CLAMP);
					}
					else if (string.equalsIgnoreCase("inherit"))
					{
						//get repeat mode from parent
						ViewParent parent = d.view(0).getParent();
						
						if (parent != null && parent instanceof View && ((View) parent).getBackground() != null)
						{
							Drawable parent_background = ((View) parent).getBackground();
							BitmapDrawable parent_drawable = null;
							if (parent_background instanceof BitmapDrawable)
							{
								parent_drawable = (BitmapDrawable) parent_background;
							}
							else
							{
								Bitmap bitmap = Bitmap.createBitmap(parent_background.getIntrinsicWidth(), parent_background.getIntrinsicHeight(), Config.ARGB_8888);
								Canvas canvas = new Canvas(bitmap);
								parent_background.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
								parent_background.draw(canvas);
								parent_drawable = new BitmapDrawable(bitmap);
							}
							drawable.setTileModeX(parent_drawable.getTileModeX());
							drawable.setTileModeY(parent_drawable.getTileModeY());
						}
					}
					else
					{
						Log.w("CSS", "Invalid \"background-repeat\" value.");
					}
					
				}
				catch (Throwable t)
				{
					//out of memory, most likely
					t.printStackTrace();
				}
				
				
			}
		});
		
		
	}
	
	public static void backgroundClip($ droidQuery, TokenSequence value) { background_clip(droidQuery, value); }
	public static void background_clip($ droidQuery, final TokenSequence value)
	{
		final String v = value.toString();
		droidQuery.each(new Function() {
			@Override
			public void invoke($ d, Object... args)
			{
				View view = d.view(0);
				if (v.equalsIgnoreCase("border-box"))
				{
					Rect r = view.getBackground().getBounds();
					ViewParent parent = view.getParent();
					
					if (parent != null && parent instanceof View)
					{
						View p = (View) parent;
						view.getBackground().setBounds(p.getLeft(), p.getTop(), p.getRight(), p.getBottom());
					}
					else
					{
						DisplayMetrics dm = view.getContext().getResources().getDisplayMetrics();
						view.getBackground().setBounds(0, 0, dm.widthPixels, dm.heightPixels);
					}
				}
				else if (v.equalsIgnoreCase("padding-box"))
				{
					view.getBackground().setBounds(view.getLeft() + view.getPaddingLeft(), view.getTop() + view.getPaddingTop(), view.getRight() + view.getPaddingRight(), view.getBottom() + view.getPaddingBottom());
				}
				else if (v.equalsIgnoreCase("content-box"))
				{
					view.getBackground().setBounds(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
				}
				else
				{
					Log.w("CSS", "Invalid \"background-clip\" value.");
				}
			}
		});
	}
	
	public static void backgroundOrigin($ droidQuery, TokenSequence value) { background_origin(droidQuery, value); }
	public static void background_origin($ droidQuery, final TokenSequence value)
	{
		final String v = value.toString();
		droidQuery.each(new Function() {
			@Override
			public void invoke($ d, Object... args)
			{
				View view = d.view(0);
				if (v.equalsIgnoreCase("border-box"))
				{
					Rect r = view.getBackground().getBounds();
					ViewParent parent = view.getParent();
					
					if (parent != null && parent instanceof View)
					{
						View p = (View) parent;
						view.getBackground().setBounds(p.getLeft(), p.getTop(), view.getRight(), view.getBottom());
					}
					else
					{
						DisplayMetrics dm = view.getContext().getResources().getDisplayMetrics();
						view.getBackground().setBounds(0, 0, view.getRight(), view.getBottom());
					}
				}
				else if (v.equalsIgnoreCase("padding-box"))
				{
					view.getBackground().setBounds(view.getLeft() + view.getPaddingLeft(), view.getTop() + view.getPaddingTop(), view.getRight(), view.getBottom());
				}
				else if (v.equalsIgnoreCase("content-box"))
				{
					view.getBackground().setBounds(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
				}
				else
				{
					Log.w("CSS", "Invalid \"background-clip\" value.");
				}
			}
		});
	}
	
	public static void backgroundSize($ droidQuery, TokenSequence value) { background_size(droidQuery, value); }
	public static void background_size($ droidQuery, final TokenSequence value)
	{
		String string = value.toString();
		if (string.equalsIgnoreCase("contain"))
		{
			droidQuery.each(new Function() {
				
				@Override
				public void invoke($ d, Object... params) {
					View v = d.view(0);
					if (v.getBackground() != null)
						v.getBackground().setBounds(0, 0, v.getWidth(), v.getHeight());
				}
			});
		}
		else if (string.equalsIgnoreCase("cover"))
		{
			
			droidQuery.each(new Function() {
				
				@Override
				public void invoke($ d, Object... params) {
					View v = d.view(0);
					if (v.getBackground() != null)
					{
						int max = Math.max(v.getWidth(), v.getHeight());
						v.getBackground().setBounds(0, 0, max, max);
					}
				}
			});
		}
		else if (string.equalsIgnoreCase("auto"))
		{
			return;//not perfect, but should work for most cases.
		}
		final TokenSequence[] split = value.split(new Token(SPACE));
		
		droidQuery.each(new Function() {
			
			@Override
			public void invoke($ d, Object... params) {
				View v = d.view(0);
				Rect r = v.getBackground().getBounds();
				int x = r.left;
				int y = r.top;
				if (split[0].endsWith(new Token(PERCENT)))
				{
					double percentLeft = Double.parseDouble(split[0].toString().replace("%", ""));
					double percentTop = 100;
					if (split.length == 2)
						percentTop = Double.parseDouble(split[1].toString().replace("%", ""));
					
					x = (int) (r.left * percentLeft/100);
					y = (int) (r.top * percentTop/100);
				}
				else if (split[0].toString().endsWith("px"))
				{
					x = Integer.parseInt(split[0].toString().replace("px", ""));
					if (split.length == 2)
						y = Integer.parseInt(split[1].toString().replace("px", ""));
					else
						y = r.top;
				}
				v.getBackground().setBounds(r);
			}
		});
	}
	
	//////////////////////////////////////////
	///         Border Properties          ///
	//////////////////////////////////////////

	public static void border($ droidQuery, final TokenSequence value)
	{
		Log.w("CSS", "CSS \"border\" not supported (yet?). Use each attribute separately, such as \"border-color\".");
	}
	
	public static void borderColor($ droidQuery, TokenSequence value) { border_color(droidQuery, value); }
	public static void border_color($ droidQuery, final TokenSequence value)
	{
		Token spaceToken = new Token(Token.SPACE);
		if (value.contains(spaceToken))
		{
			TokenSequence[] parts = value.split(spaceToken);
			//can be one, two, or four different strings
			if (parts.length == 2)
			{
				border_left_color(droidQuery, parts[1]);
				border_top_color(droidQuery, parts[0]);
				border_right_color(droidQuery, parts[1]);
				border_bottom_color(droidQuery, parts[0]);
			}
			else if (parts.length == 4)
			{
				border_top_color(droidQuery, parts[0]);
				border_right_color(droidQuery, parts[1]);
				border_bottom_color(droidQuery, parts[2]);
				border_left_color(droidQuery, parts[3]);
			}
			else
			{
				Log.w("CSS", "Invalid number of arguments for \"border_color\" property");
			}
		}
		else
		{
			border_left_color(droidQuery, value);
			border_top_color(droidQuery, value);
			border_right_color(droidQuery, value);
			border_bottom_color(droidQuery, value);
		}
		
	}
	
	public static void borderStyle($ droidQuery, TokenSequence value) { border_style(droidQuery, value); }
	public static void border_style($ droidQuery, final TokenSequence value)
	{
		Token spaceToken = new Token(Token.SPACE);
		if (value.contains(spaceToken))
		{
			TokenSequence[] parts = value.split(spaceToken);
			//can be one, two, or four different strings
			if (parts.length == 2)
			{
				border_left_style(droidQuery, parts[1]);
				border_top_style(droidQuery, parts[0]);
				border_right_style(droidQuery, parts[1]);
				border_bottom_style(droidQuery, parts[0]);
			}
			else if (parts.length == 4)
			{
				border_top_style(droidQuery, parts[0]);
				border_right_style(droidQuery, parts[1]);
				border_bottom_style(droidQuery, parts[2]);
				border_left_style(droidQuery, parts[3]);
			}
			else
			{
				Log.w("CSS", "Invalid number of arguments for \"border_color\" property");
			}
		}
		else
		{
			border_left_style(droidQuery, value);
			border_top_style(droidQuery, value);
			border_right_style(droidQuery, value);
			border_bottom_style(droidQuery, value);
		}
	}
	
	public static void borderWidth($ droidQuery, TokenSequence value) { border_width(droidQuery, value); }
	public static void border_width($ droidQuery, final TokenSequence value)
	{
		Token spaceToken = new Token(Token.SPACE);
		if (value.contains(spaceToken))
		{
			TokenSequence[] parts = value.split(spaceToken);
			//can be one, two, or four different strings
			if (parts.length == 2)
			{
				border_left_width(droidQuery, parts[1]);
				border_top_width(droidQuery, parts[0]);
				border_right_width(droidQuery, parts[1]);
				border_bottom_width(droidQuery, parts[0]);
			}
			else if (parts.length == 4)
			{
				border_top_width(droidQuery, parts[0]);
				border_right_width(droidQuery, parts[1]);
				border_bottom_width(droidQuery, parts[2]);
				border_left_width(droidQuery, parts[3]);
			}
			else
			{
				Log.w("CSS", "Invalid number of arguments for \"border_color\" property");
			}
		}
		else
		{
			border_left_width(droidQuery, value);
			border_top_width(droidQuery, value);
			border_right_width(droidQuery, value);
			border_bottom_width(droidQuery, value);
		}
	}
	
	public static void borderRadius($ droidQuery, TokenSequence value) { border_radius(droidQuery, value); }
	public static void border_radius($ droidQuery, final TokenSequence value)
	{
		//TODO
		String string = value.toString();
		if (string.equalsIgnoreCase("initial"))
		{
			
		}
		else if (string.equalsIgnoreCase("inherit"))
		{
			
		}
		else 
		{
			Token slashToken = new Token(Token.SLASH);
			Token spaceToken = new Token(Token.SPACE);
			if (value.contains(slashToken))
			{
				
			}
			else if (value.contains(spaceToken))
			{
				
			}
			else
			{
				
			}
		}
	}
	
	public static void borderBottom($ droidQuery, TokenSequence value) { border_bottom(droidQuery, value); }
	public static void border_bottom($ droidQuery, final TokenSequence value)
	{
		Log.w("CSS", "CSS \"border-bottom\" not supported (yet?). Use each attribute separately, such as \"border-bottom-color\".");
	}
	
	public static void borderBottomColor($ droidQuery, TokenSequence value) { border_bottom_color(droidQuery, value); }
	public static void border_bottom_color($ droidQuery, final TokenSequence value)
	{
		//TODO this is NOT completed - just started
		String string = value.toString();
		if (string.equalsIgnoreCase("transparent"))
		{
			droidQuery.each(new Function() {
				
				@Override
				public void invoke($ d, Object... params) {
					//TODO
				}
			});
		}
		else if (string.equalsIgnoreCase("initial"))
		{
			droidQuery.each(new Function() {
				
				@Override
				public void invoke($ d, Object... params) {
					//TODO
				}
			});
		}
		else if (string.equalsIgnoreCase("inherit"))
		{
			droidQuery.each(new Function() {
				
				@Override
				public void invoke($ d, Object... params) {
					//TODO
				}
			});
		}
		else
		{
			int color = 0x000000;
			try
			{
				Color.parseColor(string);
				droidQuery.each(new Function() {
					
					@Override
					public void invoke($ d, Object... params) {
						//TODO
					}
				});
			}
			catch (IllegalArgumentException e)
			{
				Log.w("CSS", "Cannot parse the color " + string);
			}
		}
	}
	
	public static void borderBottomStyle($ droidQuery, TokenSequence value) { border_bottom_style(droidQuery, value); }
	public static void border_bottom_style($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderBottomWidth($ droidQuery, TokenSequence value) { border_bottom_width(droidQuery, value); }
	public static void border_bottom_width($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderBottomLeftRadius($ droidQuery, TokenSequence value) { border_bottom_left_radius(droidQuery, value); }
	public static void border_bottom_left_radius($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderBottomRightRadius($ droidQuery, TokenSequence value) { border_bottom_right_radius(droidQuery, value); }
	public static void border_bottom_right_radius($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderLeft($ droidQuery, TokenSequence value) { border_left(droidQuery, value); }
	public static void border_left($ droidQuery, final TokenSequence value)
	{
		Log.w("CSS", "CSS \"border-left\" not supported (yet?). Use each attribute separately, such as \"border-left-color\".");
	}
	
	public static void borderLeftColor($ droidQuery, TokenSequence value) { border_left_color(droidQuery, value); }
	public static void border_left_color($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderLeftStyle($ droidQuery, TokenSequence value) { border_left_style(droidQuery, value); }
	public static void border_left_style($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderLeftWidth($ droidQuery, TokenSequence value) { border_left_width(droidQuery, value); }
	public static void border_left_width($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderRight($ droidQuery, TokenSequence value) { border_right(droidQuery, value); }
	public static void border_right($ droidQuery, final TokenSequence value)
	{
		Log.w("CSS", "CSS \"border-right\" not supported (yet?). Use each attribute separately, such as \"border-right-color\".");
	}
	
	public static void borderRightColor($ droidQuery, TokenSequence value) { border_right_color(droidQuery, value); }
	public static void border_right_color($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderRightStyle($ droidQuery, TokenSequence value) { border_right_style(droidQuery, value); }
	public static void border_right_style($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderRightWidth($ droidQuery, TokenSequence value) { border_right_width(droidQuery, value); }
	public static void border_right_width($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderTop($ droidQuery, TokenSequence value) { border_top(droidQuery, value); }
	public static void border_top($ droidQuery, final TokenSequence value)
	{
		Log.w("CSS", "CSS \"border-top\" not supported (yet?). Use each attribute separately, such as \"border-top-color\".");
	}
	
	public static void borderTopColor($ droidQuery, TokenSequence value) { border_top_color(droidQuery, value); }
	public static void border_top_color($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderTopStyle($ droidQuery, TokenSequence value) { border_top_style(droidQuery, value); }
	public static void border_top_style($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderTopWidth($ droidQuery, TokenSequence value) { border_top_width(droidQuery, value); }
	public static void border_top_width($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderTopLeftRadius($ droidQuery, TokenSequence value) { border_top_left_radius(droidQuery, value); }
	public static void border_top_left_radius($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderTopRightRadius($ droidQuery, TokenSequence value) { border_top_right_radius(droidQuery, value); }
	public static void border_top_right_radius($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void outline($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void outlineColor($ droidQuery, TokenSequence value) { outline_color(droidQuery, value); }
	public static void outline_color($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void outlineStyle($ droidQuery, TokenSequence value) { outline_style(droidQuery, value); }
	public static void outline_style($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void outlineWidth($ droidQuery, TokenSequence value) { outline_width(droidQuery, value); }
	public static void outline_width($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderImage($ droidQuery, TokenSequence value) { border_image(droidQuery, value); }
	public static void border_image($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderImageOutset($ droidQuery, TokenSequence value) { border_image_outset(droidQuery, value); }
	public static void border_image_outset($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderImageRepeat($ droidQuery, TokenSequence value) { border_image_repeat(droidQuery, value); }
	public static void border_image_repeat($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderImageSlice($ droidQuery, TokenSequence value) { border_image_slice(droidQuery, value); }
	public static void border_image_slice($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderImageSource($ droidQuery, TokenSequence value) { border_image_source(droidQuery, value); }
	public static void border_image_source($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void borderImageWidth($ droidQuery, TokenSequence value) { border_image_width(droidQuery, value); }
	public static void border_image_width($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void boxDecorationBreak($ droidQuery, TokenSequence value) { box_decoration_break(droidQuery, value); }
	public static void box_decoration_break($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void boxShadow($ droidQuery, TokenSequence value) { box_shadow(droidQuery, value); }
	public static void box_shadow($ droidQuery, final TokenSequence value)
	{
		
	}
	
	//////////////////////////////////////////
	///           Box Properties           ///
	//////////////////////////////////////////

	public static void overflowX($ droidQuery, TokenSequence value) { overflow_x(droidQuery, value); }
	public static void overflow_x($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void overflowY($ droidQuery, TokenSequence value) { overflow_y(droidQuery, value); }
	public static void overflow_y($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void overflowStyle($ droidQuery, TokenSequence value) { overflow_style(droidQuery, value); }
	public static void overflow_style($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void rotation($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void rotationPoint($ droidQuery, TokenSequence value) { rotation_point(droidQuery, value); }
	public static void rotation_point($ droidQuery, final TokenSequence value)
	{
		
	}
	
	//////////////////////////////////////////
	///           Color Properties         ///
	//////////////////////////////////////////


	public static void colorProfile($ droidQuery, TokenSequence value) { color_profile(droidQuery, value); }
	public static void color_profile($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void opacity($ droidQuery, final TokenSequence value)
	{
		
	}

	public static void renderingIntent($ droidQuery, TokenSequence value) { rendering_intent(droidQuery, value); }
	public static void rendering_intent($ droidQuery, final TokenSequence value)
	{
		
	}
	
	/////////////////////////////////////////////////////////////////////
	///           Skipping Content for Paged Media Properties         ///
	/////////////////////////////////////////////////////////////////////

	
	//////////////////////////////////////////
	///         Dimension Properties       ///
	//////////////////////////////////////////

	public static void height($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void maxHeight($ droidQuery, TokenSequence value) { max_height(droidQuery, value); }
	public static void max_height($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void minHeight($ droidQuery, TokenSequence value) { min_height(droidQuery, value); }
	public static void min_height($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void width($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void maxWidth($ droidQuery, TokenSequence value) { max_width(droidQuery, value); }
	public static void max_width($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void minWidth($ droidQuery, TokenSequence value) { min_width(droidQuery, value); }
	public static void min_width($ droidQuery, final TokenSequence value)
	{
		
	}

	//////////////////////////////////////////////////////////
	///           Skipping Flexible Box Properties         ///
	//////////////////////////////////////////////////////////

	
	//////////////////////////////////////
	///         Font Properties        ///
	//////////////////////////////////////
	
	public static void font($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void fontFamily($ droidQuery, TokenSequence value) { font_family(droidQuery, value); }
	public static void font_family($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void fontSize($ droidQuery, TokenSequence value) { font_size(droidQuery, value); }
	public static void font_size($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void fontStyle($ droidQuery, TokenSequence value) { font_style(droidQuery, value); }
	public static void font_style($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void fontVariant($ droidQuery, TokenSequence value) { font_variant(droidQuery, value); }
	public static void font_variant($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void fontWeight($ droidQuery, TokenSequence value) { font_weight(droidQuery, value); }
	public static void font_weight($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void fontSizeAdjust($ droidQuery, TokenSequence value) { font_size_adjust(droidQuery, value); }
	public static void font_size_adjust($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void fontStretch($ droidQuery, TokenSequence value) { font_stretch(droidQuery, value); }
	public static void font_stretch($ droidQuery, final TokenSequence value)
	{
		
	}

	//////////////////////////////////////////////////////////
	///           Skipping Generated Content Properties    ///
	//////////////////////////////////////////////////////////

	//////////////////////////////////////////////////
	///           Skipping Grid Properties         ///
	//////////////////////////////////////////////////

	//////////////////////////////////////////////////////
	///           Skipping Hyperlink Properties        ///
	//////////////////////////////////////////////////////

	/////////////////////////////////////////////////////
	///           Skipping Linebox Properties         ///
	/////////////////////////////////////////////////////

	/////////////////////////////////////////////////
	///           Skipping List Properties        ///
	/////////////////////////////////////////////////


	////////////////////////////////////////
	///         Margin Properties        ///
	////////////////////////////////////////
	
	public static void margin($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void marginBottom($ droidQuery, TokenSequence value) { margin_bottom(droidQuery, value); }
	public static void margin_bottom($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void marginLeft($ droidQuery, TokenSequence value) { margin_left(droidQuery, value); }
	public static void margin_left($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void marginRight($ droidQuery, TokenSequence value) { margin_right(droidQuery, value); }
	public static void margin_right($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void marginTop($ droidQuery, TokenSequence value) { margin_top(droidQuery, value); }
	public static void margin_top($ droidQuery, final TokenSequence value)
	{
		
	}
	
	/////////////////////////////////////////////////////
	///           Skipping Marquee Properties         ///
	/////////////////////////////////////////////////////
	
	//////////////////////////////////////////////////////////
	///           Skipping Multi-Column Properties         ///
	//////////////////////////////////////////////////////////


	////////////////////////////////////////////
	///           Padding Properties         ///
	////////////////////////////////////////////
	
	public static void padding($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void paddingBottom($ droidQuery, TokenSequence value) { padding_bottom(droidQuery, value); }
	public static void padding_bottom($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void paddingLeft($ droidQuery, TokenSequence value) { padding_left(droidQuery, value); }
	public static void padding_left($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void paddingRight($ droidQuery, TokenSequence value) { padding_right(droidQuery, value); }
	public static void padding_right($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void paddingTop($ droidQuery, TokenSequence value) { padding_top(droidQuery, value); }
	public static void padding_top($ droidQuery, final TokenSequence value)
	{
		
	}
	
	/////////////////////////////////////////////////////////
	///           Skipping Paged-Media Properties         ///
	/////////////////////////////////////////////////////////


	////////////////////////////////////////////////
	///           Positioning Properties         ///
	////////////////////////////////////////////////
	
	public static void bottom($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void clear($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void clip($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void cursor($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void display($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void _float($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void left($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void overflow($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void position($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void right($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void top($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void visibility($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void zIndex($ droidQuery, TokenSequence value) { z_index(droidQuery, value); }
	public static void z_index($ droidQuery, final TokenSequence value)
	{
		
	}
	
	///////////////////////////////////////////////////
	///           Skipping Print Properties         ///
	///////////////////////////////////////////////////
	
	//////////////////////////////////////////////////
	///           Skipping Ruby Properties         ///
	//////////////////////////////////////////////////
	
	///////////////////////////////////////////////////
	///          Skipping Speech Properties         ///
	///////////////////////////////////////////////////
	
	///////////////////////////////////////////////////
	///           Skipping Table Properties         ///
	///////////////////////////////////////////////////


	/////////////////////////////////////////
	///           Text Properties         ///
	/////////////////////////////////////////
	
	public static void color($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void direction($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void letterSpacing($ droidQuery, TokenSequence value) { letter_spacing(droidQuery, value); }
	public static void letter_spacing($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void lineHeight($ droidQuery, TokenSequence value) { line_height(droidQuery, value); }
	public static void line_height($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void textAlign($ droidQuery, TokenSequence value) { text_align(droidQuery, value); }
	public static void text_align($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void textDecoration($ droidQuery, TokenSequence value) { text_decoration(droidQuery, value); }
	public static void text_decoration($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void textIndent($ droidQuery, TokenSequence value) { text_indent(droidQuery, value); }
	public static void text_indent($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void textTransform($ droidQuery, TokenSequence value) { text_transform(droidQuery, value); }
	public static void text_transform($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void unicodeBidi($ droidQuery, TokenSequence value) { unicode_bidi(droidQuery, value); }
	public static void unicode_bidi($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void verticalAlign($ droidQuery, TokenSequence value) { vertical_align(droidQuery, value); }
	public static void vertical_align($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void whiteSpace($ droidQuery, TokenSequence value) { white_space(droidQuery, value); }
	public static void white_space($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void wordSpacing($ droidQuery, TokenSequence value) { word_spacing(droidQuery, value); }
	public static void word_spacing($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void hangingPunctuation($ droidQuery, TokenSequence value) { hanging_punctuation(droidQuery, value); }
	public static void hanging_punctuation($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void textAlignLast($ droidQuery, TokenSequence value) { text_align_last(droidQuery, value); }
	public static void text_align_last($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void textJustify($ droidQuery, TokenSequence value) { text_justify(droidQuery, value); }
	public static void text_justify($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void textOutline($ droidQuery, TokenSequence value) { text_outline(droidQuery, value); }
	public static void text_outline($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void textOverflow($ droidQuery, TokenSequence value) { text_overflow(droidQuery, value); }
	public static void text_overflow($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void textShadow($ droidQuery, TokenSequence value) { text_shadow(droidQuery, value); }
	public static void text_shadow($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void textWrap($ droidQuery, TokenSequence value) { text_wrap(droidQuery, value); }
	public static void text_wrap($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void wordBreak($ droidQuery, TokenSequence value) { word_break(droidQuery, value); }
	public static void word_break($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void wordWrap($ droidQuery, TokenSequence value) { word_wrap(droidQuery, value); }
	public static void word_wrap($ droidQuery, final TokenSequence value)
	{
		
	}
	
	////////////////////////////////////////////////
	///         2D/3D Transform Properties       ///
	////////////////////////////////////////////////
	
	public static void transform($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void transformOrigin($ droidQuery, TokenSequence value) { transform_origin(droidQuery, value); }
	public static void transform_origin($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void transformStyle($ droidQuery, TokenSequence value) { transform_style(droidQuery, value); }
	public static void transform_style($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void perspective($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void perspectiveOrigin($ droidQuery, TokenSequence value) { perspective_origin(droidQuery, value); }
	public static void perspective_origin($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void backfaceVisibility($ droidQuery, TokenSequence value) { backface_visibility(droidQuery, value); }
	public static void backface_visibility($ droidQuery, final TokenSequence value)
	{
		
	}

	////////////////////////////////////////////////
	///           Transition Properties          ///
	////////////////////////////////////////////////
	
	public static void transition($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void transitionProperty($ droidQuery, TokenSequence value) { transition_property(droidQuery, value); }
	public static void transition_property($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void transitionDuration($ droidQuery, TokenSequence value) { transition_duration(droidQuery, value); }
	public static void transition_duration($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void transitionTimingFunction($ droidQuery, TokenSequence value) { transition_timing_function(droidQuery, value); }
	public static void transition_timing_function($ droidQuery, final TokenSequence value)
	{
		
	}
	
	public static void transitionDelay($ droidQuery, TokenSequence value) { transition_delay(droidQuery, value); }
	public static void transition_delay($ droidQuery, final TokenSequence value)
	{
		
	}
	
	/////////////////////////////////////////////////////////
	///         Skipping User-Interface Properties        ///
	/////////////////////////////////////////////////////////
	
	/**
	 * CSS Handler used when asynchronously stream parsing
	 * @author Phil Brown
	 * @since 3:18:32 PM Jan 7, 2014
	 *
	 */
	private static class AsyncCSSHandler implements CSSHandler
	{
		private Handler ui;
		private $ droidQuery;
		
		public AsyncCSSHandler($ droidQuery, Handler ui)
		{
			this.droidQuery = droidQuery;
			this.ui = ui;
		}
		
		@Override
		public boolean supports(String logic) {
			//no easy way to handle this yet.
			return false;
		}
		
		@Override
		public void handleRuleSet(final RuleSet rule) {
			ui.post(new Runnable() {

				@Override
				public void run() {
					
					//TODO first, check if its an animation or something that can only be handled in stream
					
					//then do
					TokenSequence selector = rule.getSelector();
					CSSSelector cssSelector = new CSSSelector();
					$ d = cssSelector.makeSelection(droidQuery, selector);
					List<Declaration> declarations = rule.getDeclarationBlock();
					applyProperties(d, declarations);
					
					
				}
				
			});
		}
		
		@Override
		public void handleNewCharset(String arg0) {
		}
		
		@Override
		public void handleNamespace(String arg0) {
		}
		
		@Override
		public void handleKeyframes(String arg0, List<KeyFrame> arg1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public InputStream handleImport(String arg0) {
			//TODO allow imports from raw and assets
			return null;
		}
		
		@Override
		public void handleFontFace(FontFace arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void handleError(String message, Throwable t) {
		}
	}
	
	/**
	 * Stream parse the CSS in a background thread
	 * @author Phil Brown
	 * @since 3:12:17 PM Jan 7, 2014
	 *
	 */
	private static class AsyncCSSParser extends AsyncTask<Void, Void, Void>
	{
		private CSSParser parser;
		
		public AsyncCSSParser(InputStream stream, CSSHandler handler)
		{
			try
			{
				parser = new CSSParser(stream, handler);
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try
			{
				parser.parse();
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
			return null;
		}
		
	}
}
