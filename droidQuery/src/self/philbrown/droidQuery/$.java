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

package self.philbrown.droidQuery;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import self.philbrown.droidQuery.SwipeDetector.SwipeListener;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.FileObserver;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


/** 
 * <h1>droidQuery</h1>
 * An <a href="https://github.com/github/android">Android</a> <i>port</i> of 
 * <a href="https://github.com/jquery/jquery">jQuery</a>.
 * @author Phil Brown
 */
public class $ 
{
	/**
	 * Data types for <em>ajax</em> request responses
	 */
	public static enum DataType
	{
		/** JavaScript Object Notation */
		JSON, 
		/** Extensible Markup Language */
		XML, 
		/** Textual response */
		TEXT, 
		/** Bourne Script response*/
		SCRIPT, 
		/** Bitmap response */
		IMAGE
	};
	
	/**
	 * File locations used for {@code write} methods.
	 * @see $#write(byte[], FileLocation, String, boolean, boolean)
	 * @see $#write(byte[], FileLocation, String, boolean, boolean, Function, Function)
	 */
	public static enum FileLocation
	{
		/** 
		 * File Location constant. Writes files to the private directory
		 * {@code /data/data/<package>}. Internal files cannot specify a file name with separators.
		 */
		INTERNAL,
		/** 
		 * File Location constant. Writes files to the external directory
		 * {@code <external dir>/<package>}
		 */
		EXTERNAL,
		
		/** 
		 * File Location constant. Writes files to the private directory
		 * {@code /data/data/<package>/cache}. Cache files can be deleted by the system when space is
		 * needed.
		 */
		CACHE,
		
		/** 
		 * File Location constant. Writes files to the external directory
		 * {@code <external dir>Android/data/<package>}
		 */
		DATA,
		
		/** 
		 * File Location constant. Writes files to the path given by {@code fileName}.
		 */
		CUSTOM
	};
	
	/**
	 * Relates to the interpolator used for <em>droidQuery</em> animations
	 */
	public static enum Easing
	{
		/** Rate of change starts out slowly and then accelerates. */
		ACCELERATE,
		/** Rate of change starts and ends slowly but accelerates through the middle. */
		ACCELERATE_DECELERATE,
		/** change starts backward then flings forward. */
		ANTICIPATE,
		/** change starts backward, flings forward and overshoots the target value, then finally goes back to the final value. */
		ANTICIPATE_OVERSHOOT,
		/** change bounces at the end. */
		BOUNCE,
		/** Rate of change starts out quickly and and then decelerates. */
		DECELERATE,
		/** Rate of change is constant. */
		LINEAR,
		/** change flings forward and overshoots the last value then comes back. */
		OVERSHOOT
	}
	
	/** Used to correctly call methods that use simple type parameters via reflection */
	private static final Map<Class<?>, Class<?>> PRIMITIVE_TYPE_MAP = buildPrimitiveTypeMap();

	/** Inflates the mapping of data types to primitive types */
	private static Map<Class<?>, Class<?>> buildPrimitiveTypeMap()
	{
	    Map<Class<?>, Class<?>> map = new HashMap<Class<?>, Class<?>>();
	    map.put(Float.class, float.class);
	    map.put(Double.class, double.class);
	    map.put(Integer.class, int.class);
	    map.put(Boolean.class, boolean.class);
	    map.put(Long.class, long.class);
	    map.put(Short.class, short.class);
	    map.put(Byte.class, byte.class);
	    return map;
	}
	
	/** 
	 * Keeps track of {@link LogFileObserver}s so they are not freed by garbage collection 
	 * before they are used 
	 */
	private static List<LogFileObserver> fileObservers;

	/**
	 * Provides access to the system and the layout
	 */
	private Context context;
	/** The current view that will be manipulated */
	private View view;
	/** The lowest level view registered with {@code this} droidQuery. */
	private View rootView;
	/** 
	 * Optional data referenced by this droidQuery Object. Best practice is to make this a 
	 * {@link WeakReference} to avoid memory leaks.
	 */
	private Object data;
	/** Contains a mapping of {@code droidQuery} extensions. */
	private static Map<String, Constructor<?>> extensions = new HashMap<String, Constructor<?>>();
	/** Function to be called when this{@link #view} gains focus */
	private Function onFocus;
	/** Function to be called when this {@link #view} no longer has focus. */
	private Function offFocus;
	/** Function to be called when a key is pressed down when this {@link #view} has the focus. */
	private Function keyDown;
	/** Function to be called when a key is pressed when this {@link #view} has the focus. */
	private Function keyPress;
	/** Function to be called when a key is released when this {@link #view} has the focus. */
	private Function keyUp;
	/** Function to be called when a swipe event is captured by this {@link #view}. */
	private Function swipe;
	/** Function to be called when a swipe-up event is captured by this {@link #view}. */
	private Function swipeUp;
	/** Function to be called when a swipe-down event is captured by this {@link #view}. */
	private Function swipeDown;
	/** Function to be called when a swipe-left event is captured by this {@link #view}. */
	private Function swipeLeft;
	/** Function to be called when a swipe-right event is captured by this {@link #view}. */
	private Function swipeRight;
	/** Used to detect swipes on this {@link #view}. */
	private SwipeDetector swiper;
	
	/**
	 * Constructor. Accepts a {@code Context} Object. If the {@code context} is an {@link Activity},
	 * {@link #view} will be set to the content view. For example:<br>
	 * <pre>
	 * new $(context)
	 * </pre>
	 * @param context
	 * @see #with(Context)
	 */
	public $(Context context)
	{
		this.context = context;
		if (context instanceof Activity)//if an activity is used, and a contentView is set, use this as the view
		{
			this.view = this.findViewById(android.R.id.content).getRootView();
			if (this.view != null)
				this.rootView = view;
			else
			{
				//if no view is set, make sure not to get null pointers from view references.
				this.view = new View(context);
				this.rootView = view;
			}
		}
		this.view = new View(context);//if view operations are attempted without the view set, this prevents null pointer exceptions
		this.rootView = view;
		setup();
	}
	
	/**
	 * Constructor. Accepts a {@code View} Object.For example:<br>
	 * <pre>
	 * new $(myView)
	 * </pre>
	 * @param view
	 * @see #with(View)
	 */
	public $(View view)
	{
		this.rootView = view;
		this.view = view;
		this.context = view.getContext();
		setup();
	}
	
	/**
	 * Refreshes the listeners for focus changes, key inputs, and swipe events.
	 */
	private void setup()
	{
		setupFocusListener();
		setupKeyListener();
		setupSwipeListener();
	}
	
	/**
	 * Refreshes the listeners for focus changes
	 */
	private void setupFocusListener()
	{
		this.view.setOnFocusChangeListener(new View.OnFocusChangeListener(){

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && onFocus != null)
				{
					onFocus.invoke($.this);
				}
				else if (!hasFocus && offFocus != null)
				{
					offFocus.invoke($.this);
				}
			}
			
		});
	}
	
	/**
	 * Refreshes the listeners for key events
	 */
	private void setupKeyListener()
	{
		this.view.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				boolean retVal = false;
				switch(event.getKeyCode())
				{
					case KeyEvent.ACTION_DOWN : {
						if (keyDown != null)
						{
							keyDown.invoke($.this, keyCode, event);
							retVal = true;
						}
						break;
					}
					case KeyEvent.ACTION_UP : {
						if (keyUp != null)
						{
							keyUp.invoke($.this, keyCode, event);
							retVal = true;
						}
						break;
					}
				}
				if (keyPress != null)
				{
					keyPress.invoke($.this, keyCode, event);
					retVal = true;
				}
				
				return retVal;
			}
		});
	}
	
	/**
	 * Refreshes the listeners for swipe events
	 */
	private void setupSwipeListener()
	{
		swiper = new SwipeDetector(new SwipeListener(){

			@Override
			public void onUpSwipe(View v) {
				if (swipeUp != null)
				{
					swipeUp.invoke($.this);
				}
				else if (swipe != null)
				{
					swipe.invoke($.this, SwipeDetector.Direction.UP);
				}
			}

			@Override
			public void onRightSwipe(View v) {
				if (swipeRight != null)
				{
					swipeRight.invoke($.this);
				}
				else if (swipe != null)
				{
					swipe.invoke($.this, SwipeDetector.Direction.RIGHT);
				}
			}

			@Override
			public void onLeftSwipe(View v) {
				if (swipeLeft != null)
				{
					swipeLeft.invoke($.this);
				}
				else if (swipe != null)
				{
					swipe.invoke($.this, SwipeDetector.Direction.LEFT);
				}
			}

			@Override
			public void onDownSwipe(View v) {
				if (swipeDown != null)
				{
					swipeDown.invoke($.this);
				}
				else if (swipe != null)
				{
					swipe.invoke($.this, SwipeDetector.Direction.DOWN);
				}
			}

			@Override
			public void onStartSwipe(View v) {}

			@Override
			public void onStopSwipe(View v) {}
			
		});
		
		this.view.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				return swiper.onTouch(v, e);
			}
		});
	}
	
	/** 
	 * Convenience method for initializing a droidQuery Object. For example:
	 * <pre>
	 * $.with(context)
	 * </pre> 
	 * @param context
	 * @return a new droidQuery instance
	 */
	public static $ with(Context context)
	{
		return new $(context);
	}
	
	/** 
	 * Convenience method for initializing a droidQuery Object. For example:
	 * <pre>
	 * $.with(myView)
	 * </pre> 
	 * @param context
	 * @return a new droidQuery instance
	 */
	public static $ with(View view)
	{
		return new $(view);
	}
	
	/**
	 * Shortcut method for initializing a droidQuery Object and setting the view to manipulate.
	 * For example:<pre>
	 * $.with(this).id(R.id.main)
	 * </pre>
	 * @param context
	 * @param id
	 * @return a new droidQuery instance
	 */
	public static $ with(Context context, int id) 
	{
		return $.with(context).id(id);
	}
	
	/**
	 * Finds a view that is identified by the given id
	 * @param id
	 * @return the found view, or {@code null} if it was not found.
	 */
	private View findViewById(int id)
	{
		View v = null;
		if (context instanceof Activity)
			v = ((Activity) context).findViewById(id);
		else	
			v = rootView.findViewById(id);
		return v;
	}

	/** 
	 * Creates a new View of the given String type, and sets this droidQuery instance to manipulate
	 * that new instance. For example:
	 * <pre>
	 * $.with(this, R.id.myView).fadeIn().attr("alpha", 0.5f).push("android.widget.Button").click(new Function() {
	 * 	public void invoke(Object... params) {
	 *   	$.alert(MyActivity.this, "button clicked");
	 * 	}
	 * }).manage(new Function() {
	 * 	public void invoke(Object... params) {
	 * 		Context context = (Context) params[0];
	 * 		View view = (View) params[1];
	 * 		findViewById(R.id.mainView).addView(view);
	 * 	}
	 * });
	 * </pre>
	 */
	public $ push(String className)
	{
		try
		{
			Class<?> clazz = Class.forName(className);
			Constructor<?> constructor = clazz.getConstructor(new Class<?>[]{Context.class});
			View v = (View) constructor.newInstance(context);
			if (v == null || v.getParent() != null)
			{
				Log.w("droidQuery", "Cannot add View");
				return this;
			}
			add(v);
		}
		catch (Throwable t) 
		{
			throw new NullPointerException("Invalid view class!");
		}
		return this;
	}

	/** 
	 * Gets a new View by the given id, and sets this droidQuery instance to manipulate
	 * that view. For example:
	 * <pre>
	 * $.with(this, R.id.myView).fadeIn().attr("alpha", 0.5f).push(R.id.myButton).click(new Function() {
	 * 	public void invoke(Object... params) {
	 *   	$.alert(MyActivity.this, "button clicked");
	 * 	}
	 * });
	 * </pre>
	 */
	public $ push(int id)
	{
		View v = findViewById(id);
		if (v == null || v.getParent() != null)
		{
			Log.w("droidQuery", "Cannot add View");
			return this;
		}
		return add(v);
	}
	
	/** 
	 * Sets this droidQuery instance to manipulate the given view. For example:
	 * <pre>
	 * $.with(this, R.id.myView).fadeIn().attr("alpha", 0.5f).push(myButton).click(new Function() {
	 * 	public void invoke(Object... params) {
	 *   	$.alert(MyActivity.this, "button clicked");
	 * 	}
	 * });
	 * </pre>
	 */
	public $ push(View v)
	{
		return add(v);
	}
	
	/**
	 * Pops up an entire generation in the view hierarchy - meaning any siblings will also be removed.
	 * This will only pop views if views have been pushed, and then only down to the originally loaded
	 * view.
	 * @return this
	 * @see #push(int)
	 * @see #push(String)
	 * @see #push(View)
	 */
	public $ pop()
	{
		if (this.view == this.rootView)
		{
			Log.w("droidQuery", "Cannot pop root");
			return this;
		}
		this.view = (View) this.view.getParent();
		((ViewGroup) this.view).removeAllViews();
		return this;
	}
	
	/**
	 * Pops all generation in the view hierarchy above the original view.
	 * This will only pop views if views have been pushed, and then only down to the originally loaded
	 * view.
	 * @return this
	 * @see #push(int)
	 * @see #push(String)
	 * @see #push(View)
	 */
	public $ popAll()
	{
		if (this.view == this.rootView)
		{
			Log.w("droidQuery", "Cannot pop root");
			return this;
		}
		while(this.view != this.rootView)
		{
			this.view = (View) this.view.getParent();
			((ViewGroup) this.view).removeAllViews();
		}
		return this;
	}
	
	/** Sets this droidQuery instance to manipulate the parent view. Does not remove any view. */
	public $ parent()
	{
		if (this.view == this.rootView)
		{
			Log.w("droidQuery", "No parent to root exists");
			return this;
		}
		this.view = (View) this.view.getParent();
		return this;
	}
	
	/** Sets this droidQuery instance to manipulate the child view at the given index. */
	public $ child(int index)
	{
		View v = ((ViewGroup) this.view).getChildAt(index);
		if (v == null)
		{
			Log.w("droidQuery", "No such child exists");
			return this;
		}
		this.view = v;
		return this;
	}
	
	/**
	 * Animates the {@link #view} using the JSON properties, the given duration, the easing function,
	 * and with the onComplete callback
	 * @param properties JSON String of an {@link AnimationOptions} Object
	 * @param duration the duration of the animation, in milliseconds
	 * @param easing the Easing function to use
	 * @param complete the Function to invoke once the animation has completed
	 * @return this
	 * @see Easing
	 * @see #animate(Map, long, Easing, Function)
	 * @see #animate(String, AnimationOptions)
	 * @see #animate(Map, AnimationOptions)
	 */
	public $ animate(String properties, long duration, Easing easing, Function complete)
	{
		return animate(properties, AnimationOptions.create().duration(duration).easing(easing).complete(complete));
	}
	
	/**
	 * Animate the current view. Example:
	 * <pre>
	 * $.with(mView).animate("{
	 *                           left: 1000px,
	 *                           top: 0%,
	 *                           width: 50%,
	 *                           alpha: 0.5
	 *                        }", 
	 *                        new AnimationOptions("{ duration: 3000,
	 *                                                easing: linear
	 *                                            }").complete(new Function() {
	 *                        						public void invoke(Object... args) {
	 *                        							$.alert("Animation Complete!");
	 *                        						}
	 *                        					  });
	 * </pre>
	 * @param properties to animate, in CSS representation
	 * @param options the {@link AnimationOptions} for the animation
	 * @return this
	 */
	public $ animate(String properties, AnimationOptions options)
	{
		try
		{
			JSONObject props = new JSONObject(properties);
			@SuppressWarnings("unchecked")
			Iterator<String> iterator = props.keys();
			Map<String, Object> map = new HashMap<String, Object>();
			while (iterator.hasNext()) {
		        String key = iterator.next();
		        try {
		            Object value = props.get(key);
		            map.put(key, value);
		            
		        } catch (JSONException e) {
		        	Log.w("droidQuery", "Cannot handle CSS String. Some values may not be animated.");
		        }
		    }
			return animate(map, options);
		} catch (JSONException e)
		{
			Log.w("droidQuery", "Cannot handle CSS String. Unable to animate.");
			return this;
		}
	}
	
	/**
	 * Animate the current {@link #view}
	 * @param properties mapping of {@link AnimationOptions} attributes
	 * @param duration the length of time for the animation to last
	 * @param easing the Easing to use to interpolate the animation
	 * @param complete the Function to call once the animation has completed or has been canceled.
	 * @return this
	 * @see QuickMap
	 */
	public $ animate(Map<String, Object> properties, long duration, Easing easing, final Function complete)
	{
		return animate(properties, AnimationOptions.create().duration(duration).easing(easing).complete(complete));
	}
	
	/**
	 * Animate multiple view properties at the same time. Example:
	 * <pre>
	 * $.with(myView).animate(new QuickMap(QuickEntry.qe("alpha", .8f), QuickEntry.qe("width", 50%)), 400, Easing.LINEAR, null);
	 * </pre>
	 * @param properties mapping of property names and final values to animate
	 * @param options the options for setting the duration, easing, etc of the animation
	 * @return this
	 */
	public $ animate(Map<String, Object> properties, final AnimationOptions options)
	{
		AnimatorSet animation = new AnimatorSet();
		animation.setDuration(options.duration());
		animation.addListener(new AnimatorListener(){

			@Override
			public void onAnimationCancel(Animator animation) {
				if (options.fail() != null)
					options.fail().invoke($.this);
				if (options.complete() != null)
					options.complete().invoke($.this);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (options.success() != null)
					options.success().invoke($.this);
				if (options.complete() != null)
					options.complete().invoke($.this);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {}

			@Override
			public void onAnimationStart(Animator animation) {}
			
		});
		TimeInterpolator interpolator = null;
		if (options.easing() == null)
			options.easing(Easing.LINEAR);
		final Easing easing = options.easing();
		switch(easing)
		{
		case ACCELERATE : {
			interpolator = new AccelerateInterpolator();
			break;
		}
		case ACCELERATE_DECELERATE : {
			interpolator = new AccelerateDecelerateInterpolator();
			break;
		}
		case ANTICIPATE : {
			interpolator = new AnticipateInterpolator();
			break;
		}
		case ANTICIPATE_OVERSHOOT : {
			interpolator = new AnticipateOvershootInterpolator();
			break;
		}
		case BOUNCE : {
			interpolator = new BounceInterpolator();
			break;
		}
		case DECELERATE : {
			interpolator = new DecelerateInterpolator();
			break;
		}
		case OVERSHOOT : {
			interpolator = new OvershootInterpolator();
			break;
		}
		//linear is default.
		case LINEAR :
		default :
			interpolator = new LinearInterpolator();
			break;
		}
		
		//allow custom interpolator
		if (options.specialEasing() != null)
			interpolator = options.specialEasing();
		
		animation.setInterpolator(interpolator);
		
		List<Animator> animations = new ArrayList<Animator>();
		for (Entry<String, Object> entry : properties.entrySet())
		{
			String key = entry.getKey();
			Object value = entry.getValue();
			ObjectAnimator anim = null;
			if (value instanceof String)
			{
				String[] split = ((String) value).split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
				if (split.length == 1)
				{
					if (split[0].contains("."))
					{
						value = Float.parseFloat(split[0]);
					}
					else
					{
						value = Integer.parseInt(split[0]);
					}
				}
				else
				{
					if (split.length > 2)
					{
						Log.w("droidQuery", "parsererror for key " + key);
						continue;
					}
					if (split[1].equalsIgnoreCase("px"))
					{
						//this is the default. Just determine if float or int
						if (split[0].contains("."))
						{
							value = Float.parseFloat(split[0]);
						}
						else
						{
							value = Integer.parseInt(split[0]);
						}
					}
					else if (split[1].equalsIgnoreCase("dip"))
					{
						float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, Float.parseFloat(split[0]), context.getResources().getDisplayMetrics());
						if (split[0].contains("."))
						{
							value = px;
						}
						else
						{
							value = (int) px;
						}
					}
					else if (split[1].equalsIgnoreCase("dp"))
					{
						DisplayMetrics metrics = context.getResources().getDisplayMetrics();
					    float px = Float.parseFloat(split[0]) * (metrics.density/160f);
					    if (split[0].contains("."))
						{
							value = px;
						}
						else
						{
							value = (int) px;
						}
					}
					else if (split[1].equalsIgnoreCase("in"))
					{
						float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, Float.parseFloat(split[0]), context.getResources().getDisplayMetrics());
						if (split[0].contains("."))
						{
							value = px;
						}
						else
						{
							value = (int) px;
						}
					}
					else if (split[1].equalsIgnoreCase("mm"))
					{
						float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, Float.parseFloat(split[0]), context.getResources().getDisplayMetrics());
						if (split[0].contains("."))
						{
							value = px;
						}
						else
						{
							value = (int) px;
						}
					}
					else if (split[1].equalsIgnoreCase("pt"))
					{
						float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, Float.parseFloat(split[0]), context.getResources().getDisplayMetrics());
						if (split[0].contains("."))
						{
							value = px;
						}
						else
						{
							value = (int) px;
						}
					}
					else if (split[1].equalsIgnoreCase("sp"))
					{
//						float pixels = context.getResources().getDisplayMetrics().widthPixels;
//						//use best guess for width or height dpi
//						if (split[0].equalsIgnoreCase("y") || split[0].equalsIgnoreCase("top") || split[0].equalsIgnoreCase("bottom"))
//						{
//							pixels = context.getResources().getDisplayMetrics().heightPixels;
//						}
//						float sp = Float.parseFloat(split[0])/pixels;
//						if (split[0].contains("."))
//						{
//							value = sp;
//						}
//						else
//						{
//							value = (int) sp;
//						}
						float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, Float.parseFloat(split[0]), context.getResources().getDisplayMetrics());
						if (split[0].contains("."))
						{
							value = px;
						}
						else
						{
							value = (int) px;
						}
					}
					else if (split[1].equals("%"))
					{
						float pixels = context.getResources().getDisplayMetrics().widthPixels;
						//use best guess for width or height dpi
						if (split[0].equalsIgnoreCase("y") || split[0].equalsIgnoreCase("top") || split[0].equalsIgnoreCase("bottom"))
						{
							pixels = context.getResources().getDisplayMetrics().heightPixels;
						}
						float percent = Float.parseFloat(split[0])/100*pixels;
						if (split[0].contains("."))
						{
							value = percent;
						}
						else
						{
							value = (int) percent;
						}
					}
					else
					{
						Log.w("droidQuery", "invalid units for Object with key " + key);
						continue;
					}
				}
			}
			
			if (value instanceof Integer)
				anim = ObjectAnimator.ofInt(this.view, key, (Integer) value);
			else if (value instanceof Float)
				anim = ObjectAnimator.ofFloat(this.view, key, (Float) value);
			
			if (options.progress() != null)
			{
				anim.addUpdateListener(new AnimatorUpdateListener(){

					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						options.progress().invoke(animation.getAnimatedValue(), animation.getDuration() - animation.getCurrentPlayTime());
					}
					
				});
			}
			
			animations.add(anim);
		}
		animation.playTogether(animations);
		animation.start();
		
		return this;
	}
	
	/**
	 * Shortcut method for animating the alpha attribute of this {@link #view} to 1.0.
	 * @param options use to modify the behavior of the animation
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void fadeIn(AnimationOptions options)
	{
		this.animate(new QuickMap(QuickEntry.qe("alpha", new Float(1.0f))), options);
	}
	
	/**
	 * Shortcut method for animating the alpha attribute of this {@link #view} to 1.0.
	 * @param duration the length of time the animation should last
	 * @param complete the function to call when the animation has completed
	 */
	public void fadeIn(long duration, final Function complete)
	{
		ObjectAnimator anim = ObjectAnimator.ofFloat(this.view, "alpha", 1.0f);
		anim.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(Animator animation) {}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (complete != null)
					complete.invoke($.this);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {}

			@Override
			public void onAnimationStart(Animator animation) {}
		});
		anim.setDuration(duration);
		anim.start();
	}
	
	/**
	 * Shortcut method for animating the alpha attribute of this {@link #view} to 0.0.
	 * @param options use to modify the behavior of the animation
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void fadeOut(AnimationOptions options)
	{
		this.animate(new QuickMap(QuickEntry.qe("alpha", new Float(0.0f))), options);
	}
	
	/**
	 * Shortcut method for animating the alpha attribute of this {@link #view} to 0.0.
	 * @param duration the length of time the animation should last
	 * @param complete the function to call when the animation has completed
	 */
	public void fadeOut(long duration, final Function complete)
	{
		ObjectAnimator anim = ObjectAnimator.ofFloat(this.view, "alpha", 0.0f);
		anim.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(Animator animation) {}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (complete != null)
					complete.invoke($.this);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {}

			@Override
			public void onAnimationStart(Animator animation) {}
		});
		anim.setDuration(duration);
		anim.start();
	}
	
	/**
	 * Shortcut method for animating the alpha attribute of this {@link #view} to the given value.
	 * @param opacity the alpha value at the end of the animation
	 * @param options use to modify the behavior of the animation
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void fadeTo(float opacity, AnimationOptions options)
	{
		this.animate(new QuickMap(QuickEntry.qe("alpha", new Float(opacity))), options);
	}
	
	/**
	 * Shortcut method for animating the alpha attribute of this {@link #view} to the given value.
	 * @param duration the length of time the animation should last
	 * @param opacity the alpha value at the end of the animation
	 * @param complete the function to call when the animation has completed
	 */
	public void fadeTo(long duration, float opacity, final Function complete)
	{
		ObjectAnimator anim = ObjectAnimator.ofFloat(this.view, "alpha", opacity);
		anim.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(Animator animation) {}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (complete != null)
					complete.invoke($.this);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {}

			@Override
			public void onAnimationStart(Animator animation) {}
		});
		anim.setDuration(duration);
		anim.start();
	}
	
	/**
	 * If this {@link #view} has an alpha of less than 0.5, it will fade in. Otherwise, it will
	 * fade out.
	 * @param duration the length of time the animation should last
	 * @param complete the function to call when the animation has completed
	 */
	public void fadeToggle(long duration, final Function complete)
	{
		if (this.view.getAlpha() < 0.5)
			this.fadeIn(duration, complete);
		else
			this.fadeIn(duration, complete);
	}
	
	/**
	 * If this {@link #view} has an alpha of less than 0.5, it will fade in. Otherwise, it will
	 * fade out.
	 * @param options use to modify the behavior of the animation
	 */
	public void fadeToggle(AnimationOptions options)
	{
		if (this.view.getAlpha() < 0.5)
			this.fadeIn(options);
		else
			this.fadeIn(options);
	}
	
	/**
	 * Animates this {@link #view} out of its parent by sliding it down, past its bottom
	 * @param duration the length of time the animation should last
	 * @param complete the function to call when the animation has completed
	 */
	public void slideDown(long duration, final Function complete)
	{
		ViewParent parent = view.getParent();
		float y = 0;
		if (parent != null && parent instanceof View)
		{
			y = ((View) parent).getHeight();
		}
		else
		{
			Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();  
			y = display.getHeight();
		}
		ObjectAnimator anim = ObjectAnimator.ofFloat(this.view, "y", y);
		anim.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(Animator animation) {}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (complete != null)
					complete.invoke($.this);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {}

			@Override
			public void onAnimationStart(Animator animation) {}
		});
		anim.setDuration(duration);
		anim.start();
	}
	
	/**
	 * Animates this {@link #view} out of its parent by sliding it down, past its bottom
	 * @param options use to modify the behavior of the animation
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void slideDown(AnimationOptions options)
	{
		ViewParent parent = view.getParent();
		float y = 0;
		if (parent != null && parent instanceof View)
		{
			y = ((View) parent).getHeight();
		}
		else
		{
			Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();  
			y = display.getHeight();
		}
		this.animate(new QuickMap(QuickEntry.qe("y", new Float(y))), options);
	}
	
	/**
	 * Animates this {@link #view} out of its parent by sliding it up, past its top
	 * @param duration the length of time the animation should last
	 * @param complete the function to call when the animation has completed
	 */
	public void slideUp(long duration, final Function complete)
	{
		ObjectAnimator anim = ObjectAnimator.ofFloat(this.view, "y", 0);
		anim.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(Animator animation) {}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (complete != null)
					complete.invoke($.this);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {}

			@Override
			public void onAnimationStart(Animator animation) {}
		});
		anim.setDuration(duration);
		anim.start();
	}
	
	/**
	 * Animates this {@link #view} out of its parent by sliding it up, past its top
	 * @param options use to modify the behavior of the animation
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void slideUp(AnimationOptions options)
	{
		this.animate(new QuickMap(QuickEntry.qe("y", new Float(0f))), options);
	}
	
	/**
	 * Animates this {@link #view} out of its parent by sliding it right, past its edge
	 * @param duration the length of time the animation should last
	 * @param complete the function to call when the animation has completed
	 */
	public void slideRight(long duration, final Function complete)
	{
		ViewParent parent = view.getParent();
		float x = 0;
		if (parent != null && parent instanceof View)
		{
			x = ((View) parent).getWidth();
		}
		else
		{
			Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();  
			x = display.getHeight();
		}
		ObjectAnimator anim = ObjectAnimator.ofFloat(this.view, "x", x);
		anim.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(Animator animation) {}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (complete != null)
					complete.invoke($.this);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {}

			@Override
			public void onAnimationStart(Animator animation) {}
		});
		anim.setDuration(duration);
		anim.start();
	}
	
	/**
	 * Animates this {@link #view} out of its parent by sliding it right, past its edge
	 * @param options use to modify the behavior of the animation
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void slideRight(AnimationOptions options)
	{
		ViewParent parent = view.getParent();
		float x = 0;
		if (parent != null && parent instanceof View)
		{
			x = ((View) parent).getWidth();
		}
		else
		{
			Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();  
			x = display.getHeight();
		}
		this.animate(new QuickMap(QuickEntry.qe("x", new Float(x))), options);
	}
	
	/**
	 * Animates this {@link #view} out of its parent by sliding it left, past its edge
	 * @param duration the length of time the animation should last
	 * @param complete the function to call when the animation has completed
	 */
	public void slideLeft(long duration, final Function complete)
	{
		ObjectAnimator anim = ObjectAnimator.ofFloat(this.view, "x", 0f);
		anim.addListener(new AnimatorListener() {
			@Override
			public void onAnimationCancel(Animator animation) {}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (complete != null)
					complete.invoke($.this);
			}

			@Override
			public void onAnimationRepeat(Animator animation) {}

			@Override
			public void onAnimationStart(Animator animation) {}
		});
		anim.setDuration(duration);
		anim.start();
	}
	
	/**
	 * Animates this {@link #view} out of its parent by sliding it left, past its edge
	 * @param options use to modify the behavior of the animation
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void slideLeft(AnimationOptions options)
	{
		this.animate(new QuickMap(QuickEntry.qe("x", new Float(0f))), options);
	}
	
	/**
	 * Gets the value for the given attribute. This is done using reflection, and as such
	 * expects a <em>get-</em> or <em>is-</em> prefixed method name for this {@link #view}.
	 * @param s the name of the attribute to retrieve
	 * @return the value of the given attribute name on this {@link #view}
	 */
	public Object attr(String s)
	{
		try
		{
			Method m = view.getClass().getMethod("get" + capitalize(s));
			return m.invoke(this.view);
		}
		catch (Throwable t)
		{
			try
			{
				Method m = view.getClass().getMethod("is" + capitalize(s));
				return m.invoke(this.view);
			}
			catch (Throwable t2)
			{
				Log.w("droidQuery", this.view.getClass().getSimpleName() + ".get" + capitalize(s) + "() is not a method!");
				Log.w("droidQuery", this.view.getClass().getSimpleName() + ".is" + capitalize(s) + "() is not a method!");
				return null;
			}
		}
	}
	
	/**
	 * Sets the value of the given attribute on this {@link #view}. This is done using reflection, 
	 * and as such a <em>set-</em>prefixed method name for this {@link #view}.
	 * @param s the name of the attribute to set
	 * @param o the value to set to the given attribute
	 * @return this
	 */
	public $ attr(String s, Object o)
	{
		try
		{
			Class<?> objClass = o.getClass();
			Class<?> simpleClass = PRIMITIVE_TYPE_MAP.get(objClass);
			if (simpleClass != null)
			{
				objClass = simpleClass;
			}
			Method m = view.getClass().getMethod("set" + capitalize(s), new Class<?>[]{objClass});
			m.invoke(this.view, o);
		}
		catch (Throwable t)
		{
			Log.w("droidQuery", this.view.getClass().getSimpleName() + ".set" + capitalize(s) + "(" + o.getClass().getSimpleName() + ") is not a method!");
		}
		return this;
	}
	
	/**
	 * Many of the modifications required for views and context on Android are not simple to manipulate.
	 * This method hands the current context, the view, and any data to the developer to make custom changes.
	 * This can be used to do many things that the methods provided by droidQuery do not. For example:
	 * <pre>
	 * MyCustomView custom = new MyCustomView(this);
	 * $.with(custom).attr("alpha", 0.5f).manage(new Function() {
	 * 	public void invoke(Object... params) {
	 * 		Context context = (Context) params[0];
	 * 		MyCustomView view = (MyCustomView) params[1];
	 *		Object data = params[2];
	 * 		view.doSomething();
	 * 	}
	 * });
	 * </pre>
	 * @param function Function to invoke. Receives two arguments: the current context, and the 
	 * current view (respectively).
	 * @return this droidQuery
	 */
	public $ manage(Function function)
	{
		function.invoke(context, view, data);
		return this;
	}
	
	/**
	 * @return the current view
	 */
	public View view()
	{
		return this.view;
	}
	
	/**
	 * @return the current context
	 */
	public Context context()
	{
		return this.context;
	}
	
	/**
	 * @return the current data.
	 */
	public Object data()
	{
		return this.data;
	}
	
	/**
	 * Sets the data associated with this droidQuery
	 * @param data the data to set
	 * @return this
	 */
	public $ data(Object data)
	{
		this.data = data;
		return this;
	}
	
	/**
	 * Adds a subview to this {@link #view}
	 * @param v the subview to add
	 * @return this
	 */
	public $ add(View v)
	{
		if (v == null || v.getParent() != null)
		{
			Log.w("droidQuery", "Cannot add View");
			return this;
		}
		if (this.view instanceof ViewGroup)
		{
			((ViewGroup) this.view).addView(v);
		}
		return this;
	}
	
	/**
	 * Adds a subview to this {@link #view}
	 * @param v the id of the subview to add
	 * @return this
	 */
	public $ add(int id)
	{
		View v = findViewById(id);
		if (v == null || v.getParent() != null)
		{
			Log.w("droidQuery", "Cannot add View");
			return this;
		}
		if (this.view instanceof ViewGroup)
		{
			((ViewGroup) this.view).addView(v);
		}
		return this;
	}
	
	/**
	 * Removes a subview from this {@link #view}
	 * @param v the subview to remove
	 * @return this
	 */
	public $ remove(View v)
	{
		if (this.view instanceof ViewGroup)
		{
			((ViewGroup) this.view).removeView(v);
		}
		return null;
	}
	
	/**
	 * Removes a subview from this {@link #view}
	 * @param v the id of the subview to remove
	 * @return this
	 */
	public $ remove(int id)
	{
		View v = findViewById(id);
		if (v == null)
		{
			Log.w("droidQuery", "Cannot remove View");
			return this;
		}
		if (this.view instanceof ViewGroup)
		{
			((ViewGroup) this.view).removeView(v);
		}
		return this;
	}
	
	/**
	 * Sets the visibility of this {@link #view} to {@link View#VISIBLE}
	 * @return this
	 */
	public $ hide()
	{
		view.setVisibility(View.VISIBLE);
		return this;
	}
	
	/**
	 * Sets the visibility of this {@link #view} to {@link View#INVISIBLE}
	 * @return this
	 */
	public $ show()
	{
		view.setVisibility(View.INVISIBLE);
		return this;
	}
	
	///Event Handler Attachment
	
	/**
	 * Binds the current view to the event. For example:
	 * <pre>
	 * $.with(myView).bind("click", "Hello World!", new Function() {
	 * 	public void invoke(Object... args) {
	 * 		View v = args[0];
	 * 		Object data = args[1];
	 * 		$.alert(v.getContext(), (String) data);
	 * 	}
	 * });
	 * </pre>
	 * @note that for events with multiple words, all words except the first are required to be 
	 * capitalized. For example, to bind to a long-click event, both of the following are acceptable:
	 * <pre>
	 * $.with(myView).bind("longClick", "Hello World!", new Function() {
	 * 	public void invoke(Object... args) {
	 * 		View v = args[0];
	 * 		Object data = args[1];
	 * 		$.alert(v.getContext(), (String) data);
	 * 	}
	 * });
	 * 
	 * $.with(myView).bind("LongClick", "Hello World!", new Function() {
	 * 	public void invoke(Object... args) {
	 * 		View v = args[0];
	 * 		Object data = args[1];
	 * 		$.alert(v.getContext(), (String) data);
	 * 	}
	 * });
	 * </pre>
	 * However, this will fail:
	 * <pre>
	 * $.with(myView).bind("longclick", "Hello World!", new Function() {
	 * 	public void invoke(Object... args) {
	 * 		View v = args[0];
	 * 		Object data = args[1];
	 * 		$.alert(v.getContext(), (String) data);
	 * 	}
	 * });
	 * </pre>
	 * @param eventType should be the verb in OnVerbListener
	 * @param data an Object passed to {@code handler} when the event is triggered.
	 * @param handler receives two arguments: the affected view, and the {@code data} parameter
	 * @return the current instance of {@code droidQuery}
	 * @see #on(String, Function)
	 * @see #one(String, Function)
	 * @see #unbind(String)
	 */
	public $ bind(String eventType, Object data, Function handler)
	{
		String method = String.format(Locale.US, "setOn%sListener", capitalize(eventType));
		Class<?>[] classes = this.view.getClass().getClasses();
		String listener = String.format(Locale.US, "On%sListener", capitalize(eventType));
		try
		{
			//dynamically create instance of the listener interface
			
			Class<?> eventInterface = null;
			
			for (Class<?> clazz : classes)
			{
				if (clazz.getSimpleName().equalsIgnoreCase(listener))
				{
					eventInterface = clazz;
					break;
				}
			}
//			Method setEventListener = null;
//			for (Method m : this.view.getClass().getMethods())
//			{
//				if (m.getName().equalsIgnoreCase(method))
//				{
//					Class<?>[] mParams =  m.getParameterTypes();
//					if (mParams.length == 1 && mParams[0] == eventInterface)
//					{
//						setEventListener = m;
//						break;
//					}
//				}
//			}
			//Class<?> eventInterface = Class.forName(listener);
			Method setEventListener = this.view.getClass().getMethod(method, new Class<?>[]{eventInterface});
			EventHandlerCreator proxy = new EventHandlerCreator(handler, this.view, data);
			Object eventHandler = Proxy.newProxyInstance(eventInterface.getClassLoader(), new Class<?>[]{eventInterface}, proxy);
			setEventListener.invoke(this.view, eventInterface.cast(eventHandler));
			
		}
		catch (Throwable t)
		{
			Log.w("droidQuery", String.format(Locale.US, "Could not bind to event %s.\n%s", eventType, t.getMessage()));
		}
		return this;
	}
	
	/**
	 * Binds the current view to the event. For example:
	 * <pre>
	 * $.with(myView).on("click", new Function() {
	 * 	public void invoke(Object... args) {
	 * 		View v = args[0];
	 * 		$.alert(v.getContext(), "View Clicked!");
	 * 	}
	 * });
	 * </pre>
	 * @note that for events with multiple words, all words except the first are required to be 
	 * capitalized. For example, to bind to a long-click event, both of the following are acceptable:
	 * <pre>
	 * $.with(myView).on("longClick", new Function() {
	 * 	public void invoke(Object... args) {
	 * 		View v = args[0];
	 * 		$.alert(v.getContext(), "View LongClicked!");
	 * 	}
	 * });
	 * 
	 * $.with(myView).on("LongClick", new Function() {
	 * 	public void invoke(Object... args) {
	 * 		View v = args[0];
	 * 		$.alert(v.getContext(), "View LongClicked!");
	 * 	}
	 * });
	 * </pre>
	 * However, this will fail:
	 * <pre>
	 * $.with(myView).on("longclick", new Function() {
	 * 	public void invoke(Object... args) {
	 * 		View v = args[0];
	 * 		$.alert(v.getContext(), "View LongClicked!");
	 * 	}
	 * });
	 * </pre>
	 * @param event should be the verb in OnVerbListener
	 * @param handler receives two arguments: the affected view, and the {@code data} parameter
	 * @return the current instance of {@code droidQuery}
	 * @see #bind(String, Object, Function)
	 * @see #one(String, Function)
	 * @see #unbind(String)
	 */
	public $ on(String event, Function handler)
	{
		String method = String.format(Locale.US, "setOn%sListener", capitalize(event));
		Class<?>[] classes = this.view.getClass().getClasses();
		String listener = String.format(Locale.US, "On%sListener", capitalize(event));
		
		try
		{

			Class<?> eventInterface = null;
			
			for (Class<?> clazz : classes)
			{
				if (clazz.getSimpleName().equalsIgnoreCase(listener))
				{
					eventInterface = clazz;
					break;
				}
			}

			Method setEventListener = this.view.getClass().getMethod(method, new Class<?>[]{eventInterface});
			EventHandlerCreator proxy = new EventHandlerCreator(handler, this.view);
			Object eventHandler = Proxy.newProxyInstance(eventInterface.getClassLoader(), new Class<?>[]{eventInterface}, proxy);
			setEventListener.invoke(this.view, eventInterface.cast(eventHandler));
		}
		catch (Throwable t)
		{
			Log.w("droidQuery", String.format(Locale.US, "Could not bind to event %s.", event));
		}
		return this;
	}
	
	/**
	 * Like {@link #on(String, Function)}, but the function will only run once for the event. Future
	 * events will not trigger the given function
	 * @param event the name of the event
	 * @param handler the function to invoke the first time the event occurs
	 * @return this
	 */
	public $ one(final String event, final Function handler)
	{
		Function function = new Function()
		{

			@Override
			public void invoke(Object... params) {
				handler.invoke();
				$.with((View) params[0]).unbind(event);
			}
			
		};
		
		return on(event, function);
	}
	
	/**
	 * Registers change listeners for TextViews, EditTexts, and CompoundButtons. For all other
	 * view types, this will trigger a function when the view's layout has been changed.
	 * @param function the Function to call when the change event occurs
	 * @return this
	 */
	public $ change(final Function function)
	{
		if (this.view instanceof TextView)
		{
			((TextView) this.view).addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable arg0) {
					function.invoke($.this);
				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
				
			});
		}
		else if (this.view instanceof EditText)
		{//this is overkill, but what the hey
			((EditText) this.view).addTextChangedListener(new TextWatcher(){

				@Override
				public void afterTextChanged(Editable arg0) {
					function.invoke($.this);
				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
				
			});
		}
		else if (this.view instanceof CompoundButton)
		{
			((CompoundButton) this.view).setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					function.invoke($.this);
				}
				
			});
		}
		else
		{
			//default to size
			this.view.addOnLayoutChangeListener(new View.OnLayoutChangeListener(){

				@Override
				public void onLayoutChange(View v, int left, int top,
						int right, int bottom, int oldLeft, int oldTop,
						int oldRight, int oldBottom) {
					function.invoke($.this);
				}
				
			});
		}
		return this;
	}
	
	/**
	 * Get the value associated with this {@link #view}. If the view is a TextView, this method
	 * returns the CharSequence text. If it is a Button, the boolean checked state is returned. 
	 * If it is an ImageView, the Drawable is returned.
	 * @return the value of this view, or <em>null</em> if not applicable.
	 */
	public Object val()
	{
		if (this.view instanceof TextView)
		{
			return ((TextView) this.view).getText();
		}
		else if (this.view instanceof CompoundButton)
		{
			return ((CompoundButton) this.view).isChecked();
		}
		else if (this.view instanceof ImageView)
		{
			return ((ImageView) this.view).getDrawable();
		}
		return null;
	}
	
	/**
	 * Set the value associated with this {@link #view}. If the view is a TextView, this method
	 * sets the CharSequence text. If it is a Button, the boolean checked state is set. 
	 * If it is an ImageView, the Drawable or Bitmap is set. All other view types are ignored.
	 * @return this
	 */
	public $ val(Object object)
	{
		if (this.view instanceof TextView && object instanceof CharSequence)
		{
			((TextView) this.view).setText((CharSequence) object);
		}
		else if (this.view instanceof CompoundButton && object instanceof Boolean)
		{
			((CompoundButton) this.view).setChecked((Boolean) object);
		}
		else if (this.view instanceof ImageView)
		{
			if (object instanceof Bitmap)
			{
				((ImageView) this.view).setImageBitmap((Bitmap) object);
			}
			else if (object instanceof Drawable)
			{
				((ImageView) this.view).setImageDrawable((Drawable) object);
			}
			
		}
		return this;
	}
	
	/**
	 * Triggers a click event on this view
	 * @return this
	 */
	public $ click()
	{
		this.view.performClick();
		return this;
	}
	
	/**
	 * Invokes the given Function every time this {@link #view} is clicked. The only parameter passed 
	 * to the given function is this droidQuery instance.
	 * @param function the function to call when this view is clicked
	 * @return this
	 */
	public $ click(final Function function)
	{
		this.view.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				function.invoke($.this);
			}
			
		});
		return this;
	}
	
	/**
	 * Invokes the given Function for click events on this view. The function will receive two arguments:
	 * <ol>
	 * <li>this droidQuery
	 * <li>{@code eventData}
	 * </ol>
	 * @param eventData the second argument to pass to the {@code function}
	 * @param function the function to invoke
	 * @return
	 */
	public $ click(final Object eventData, final Function function)
	{
		this.view.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				function.invoke($.this, eventData);
			}
			
		});
		return this;
	}
	
	/**
	 * Triggers a long-click event on this view
	 * @return this
	 */
	public $ longclick()
	{
		this.view.performLongClick();
		return this;
	}
	
	/**
	 * Invokes the given Function every time this {@link #view} is long-clicked. The only 
	 * parameter passed to the given function is this droidQuery instance.
	 * @param function the function to call when this view is long-clicked
	 * @return this
	 */
	public $ longclick(final Function function)
	{
		this.view.setOnLongClickListener(new View.OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				function.invoke($.this);
				return true;
			}
			
		});
		return this;
	}
	
	/**
	 * Invokes the given Function for long-click events on this view. The function will receive two 
	 * arguments:
	 * <ol>
	 * <li>this droidQuery
	 * <li>{@code eventData}
	 * </ol>
	 * @param eventData the second argument to pass to the {@code function}
	 * @param function the function to invoke
	 * @return
	 */
	public $ longclick(final Object eventData, final Function function)
	{
		this.view.setOnLongClickListener(new View.OnLongClickListener(){

			@Override
			public boolean onLongClick(View v) {
				function.invoke($.this, eventData);
				return true;
			}
			
		});
		return this;
	}
	
//	public $ dblclick(Function function)
//	{
//		final DoubleClickHandler handler = new DoubleClickHandler(function, null);
//		
//		this.view.setOnTouchListener(new View.OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				return handler.onDoubleTap(event);
//			}
//		});
//		return this;
//	}
//	
//	public $ dblclick(Object eventData, Function function)
//	{
//		final DoubleClickHandler handler = new DoubleClickHandler(function, eventData);
//		
//		this.view.setOnTouchListener(new View.OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				return handler.onDoubleTap(event);
//			}
//		});
//		return this;
//	}
//	
//	public $ dblclick()
//	{
//		this.view.performClick();
//		this.view.performClick();
//		return this;
//	}
	
	/**
	 * Handles swipe events. This will override any onTouchListener added.
	 * @param function will receive this droidQuery and a {@link SwipeDetector.Direction} corresponding
	 * to the direction of the swipe.
	 * @return this
	 */
	public $ swipe(Function function)
	{
		swipe = function;
		setupSwipeListener();
		return this;
	}
	
	/**
	 * Sets the function that is called when the user swipes Up. This will cause any function set by
	 * {@link #swipe(Function)} to not get called for up events.
	 * @param function the function to invoke
	 * @return this
	 */
	public $ swipeUp(Function function)
	{
		swipeUp = function;
		setupSwipeListener();
		return this;
	}
	
	/**
	 * Sets the function that is called when the user swipes Left. This will cause any function set by
	 * {@link #swipe(Function)} to not get called for left events.
	 * @param function the function to invoke
	 * @return this
	 */
	public $ swipeLeft(Function function)
	{
		swipeLeft = function;
		setupSwipeListener();
		return this;
	}
	
	/**
	 * Sets the function that is called when the user swipes Down. This will cause any function set by
	 * {@link #swipe(Function)} to not get called for down events.
	 * @param function the function to invoke
	 * @return this
	 */
	public $ swipeDown(Function function)
	{
		swipeDown = function;
		setupSwipeListener();
		return this;
	}
	
	/**
	 * Sets the function that is called when the user swipes Right. This will cause any function set by
	 * {@link #swipe(Function)} to not get called for right events.
	 * @param function the function to invoke
	 * @return this
	 */
	public $ swipeRight(Function function)
	{
		swipeRight = function;
		setupSwipeListener();
		return this;
	}
	
	/**
	 * Triggers a swipe-up event on this view
	 * @return this
	 */
	public $ swipeUp()
	{
		if (swiper != null)
			swiper.performSwipeUp();
		return this;
	}
	
	/**
	 * Triggers a swipe-down event on this view
	 * @return this
	 */
	public $ swipeDown()
	{
		if (swiper != null)
			swiper.performSwipeDown();
		return this;
	}
	
	/**
	 * Triggers a swipe-left event on this view
	 * @return this
	 */
	public $ swipeLeft()
	{
		if (swiper != null)
			swiper.performSwipeLeft();
		return this;
	}
	
	/**
	 * Triggers a swipe-right event on this view
	 * @return this
	 */
	public $ swipeRight()
	{
		if (swiper != null)
			swiper.performSwipeRight();
		return this;
	}
	
	/**
	 * Sets the function to call when this {@link #view} has gained focus. This function
	 * will receive this instance of droidQuery as its only parameter
	 * @param function the function to invoke
	 * @return this
	 */
	public $ focus(Function function)
	{
		onFocus = function;
		setupFocusListener();//fixes any changes to the onfocuschanged listener
		return this;
	}
	
	/**
	 * Gives focus to this {@link #view}, if it is focusable in its current state.
	 * @return this
	 */
	public $ focus()
	{
		this.view.requestFocus();
		return this;
	}
	
	/**
	 * Sets the function to call when this {@link #view} loses focus.
	 * @param function the function to invoke. Will receive this instance of droidQuery as its 
	 * only parameter
	 * @return this
	 */
	public $ focusout(Function function)
	{
		offFocus = function;
		setupFocusListener();//fixes any changes to the onfocuschanged listener
		return this;
	}
	
	/**
	 * Removes focus from this {@link #view}, if it is currently focused.
	 * @return this
	 */
	public $ focusout()
	{
		this.view.clearFocus();
		return this;
	}
	
	/**
	 * Set the function to call when a key-down event has been detected on this view.
	 * @param function the Function to invoke. Receives three arguments:
	 * <ol>
	 * <li>this droidQuery
	 * <li>the Integer key code
	 * <li>the {@link KeyEvent} Object that was produced
	 * </ol>
	 * @return this
	 */
	public $ keydown(Function function)
	{
		keyDown = function;
		setupKeyListener();
		return this;
	}
	
	/**
	 * Set the function to call when a key-press event has been detected on this view.
	 * @param function the Function to invoke. Receives three arguments:
	 * <ol>
	 * <li>this droidQuery
	 * <li>the Integer key code
	 * <li>the {@link KeyEvent} Object that was produced
	 * </ol>
	 * @return this
	 */
	public $ keypress(Function function)
	{
		keyPress = function;
		setupKeyListener();
		return this;
	}
	
	/**
	 * Set the function to call when a key-up event has been detected on this view.
	 * @param function the Function to invoke. Receives three arguments:
	 * <ol>
	 * <li>this droidQuery
	 * <li>the Integer key code
	 * <li>the {@link KeyEvent} Object that was produced
	 * </ol>
	 * @return this
	 */
	public $ keyup(Function function)
	{
		keyUp = function;
		setupKeyListener();
		return this;
	}
	
	/**
	 * This function can be called when this view is a subview of an {@link AdapterView}, in order
	 * to register an {@link AdapterView.OnItemSelectedListener OnItemSelectedListener} to invoke
	 * the given function.
	 * @param function function to invoke. receives two aruments:
	 * <ol>
	 * <li>this droidQuery
	 * <li>the view position, or -1 if none is selected
	 * </ol>
	 * @return this
	 */
	public $ select(final Function function)
	{
		if (view instanceof AdapterView)
		{
			((AdapterView<?>) view).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					function.invoke($.this, position);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					function.invoke($.this, -1);
				}
			});
		}
		return this;
	}
	
	/**
	 * This function can be called when this view is a subview of an {@link AdapterView}, in order
	 * to set the selected position
	 * @param index the index of the subview to select
	 * @return this
	 */
	public $ select(int index)
	{
		try
		{
			Method m = view.getClass().getMethod("setSelection", new Class<?>[]{Integer.class});
			m.invoke(view, index);
		}
		catch (Throwable t)
		{
			//not available
		}
		return this;
	}
	
	/**
	 * Remove a previously-attached event handler from this view. This can remove events registered
	 * by {@link #bind(String, Object, Function)}, {@link #on(String, Function)}, {@link #click()}, 
	 * etc.- or directly on the view.
	 * @param eventType the name of the event to unbind
	 */
	public void unbind(String eventType)
	{
		String method = String.format(Locale.US, "setOn%sListener", capitalize(eventType));
		String listener = String.format(Locale.US, "%s.On%sListener", this.view.getClass().getName(), capitalize(eventType));
		try
		{
			//dynamically create instance of the listener interface
			
			Class<?> eventInterface = Class.forName(listener);
			Method setEventListener = this.view.getClass().getMethod(method, new Class<?>[]{eventInterface});
			EventHandlerCreator proxy = new EventHandlerCreator($.noop(), this.view, null);
			Object eventHandler = Proxy.newProxyInstance(eventInterface.getClassLoader(), new Class<?>[]{eventInterface}, proxy);
			setEventListener.invoke(this.view, eventInterface.cast(eventHandler));
			
		}
		catch (Throwable t)
		{
			Log.w("droidQuery", String.format(Locale.US, "Could not unbind from event %s.", eventType));
		}
	}
	
	/////Miscellaneous
	
	/**
	 * If the current view is a subclass of {@link AdapterView}, this will loop through all the 
	 * adapter data and invoke the given function, passing the parameters:
	 * <ol>
	 * <li>this droidQuery
	 * <li>the item from the adapter
	 * <li>the index
	 * </ol>
	 * Otherwise, if the current view is a subclass of {@link ViewGroup}, {@code each} will
	 * loop through all the child views, and wrap each one in a droidQuery object. The invoked
	 * function will receive these arguments:
	 * 
	 * <ol>
	 * <li>the droidQuery wrapping the child view
	 * <li>the index of the child view
	 * </ol>
	 * @param function Function the function to invoke
	 * @return this
	 */
	public $ each(Function function)
	{
		if (this.view instanceof AdapterView)
		{
			AdapterView<?> group = (AdapterView<?>) view;
			for (int i = 0; i < (group).getCount(); i++)
			{
				function.invoke($.this, group.getItemAtPosition(i), i);
			}
		}
		else if (this.view instanceof ViewGroup)
		{
			ViewGroup group = (ViewGroup) this.view;
			for (int i = 0; i < group.getChildCount(); i++)
			{
				function.invoke($.with(group.getChildAt(i)), i);
			}
		}
		return this;
	}
	
	/**
	 * If the current view is a subclass of {@link AdapterView}, this will loop through all the 
	 * adapter data and invoke the given function, passing the parameters:
	 * <ol>
	 * <li>this droidQuery
	 * <li>the item from the adapter
	 * <li>the index
	 * </ol>
	 * Otherwise, if the current view is a subclass of {@link ViewGroup}, {@code each} will
	 * loop through all the child views, and wrap each one in a droidQuery object. The invoked
	 * function will receive these arguments:
	 * 
	 * <ol>
	 * <li>the droidQuery wrapping the child view
	 * <li>the index of the child view
	 * </ol>
	 * @param function Function the function to invoke
	 * @return this
	 */
	public $ children(Function function)
	{
		return each(function);
	}
	
	/**
	 * Loops through all the sibling views of the current {@link #view}, and wraps each in a 
	 * droidQuery object. When invoked, the given function will receive two parameters:
	 * <ol>
	 * <li>the droidQuery for the view
	 * <li>the child index of the sibling
	 * </ol>
	 * @param function receives the droidQuery for the view, and the index for arg1
	 */
	public $ siblings(Function function)
	{
		ViewParent parent = this.view.getParent();
		if (parent != null && parent instanceof ViewGroup)
		{
			ViewGroup group = (ViewGroup) parent;
			for (int i = 0; i < group.getChildCount(); i++)
			{
				function.invoke($.with(group.getChildAt(i)), i);
			}
		}
		return this;
	}
	
	/**
	 * If this view is a subclass of {@link AdapterView}, {@code slice} gets all Objects associated
	 * with the data for positions {@code start} until the end of the list or array. The Objects are
	 * then wrapped into the {@link #data} Object, and a list of droidQueries is returned.
	 * Otherwise, if this view is a subclass of {@link ViewGroup}, {@code slice} gets all subviews
	 * from the {@code start} position to the last child, and wraps them into each droidQuery view in
	 * the list that is returned.
	 * @param start the starting index
	 * @return a list of droidQuery Objects.
	 */
	public List<$> slice(int start)
	{
		if (this.view instanceof AdapterView)
		{
			AdapterView<?> group = (AdapterView<?>) view;
			if (group.getCount() <= start)
			{
				return null;
			}
			List<$> list = new ArrayList<$>();
			for (int i = start+1; i < group.getCount(); i++)
			{
				list.add($.with(context).data(group.getItemAtPosition(i)));
			}
		}
		else if (this.view instanceof ViewGroup)
		{
			ViewGroup group = (ViewGroup) view;
			if (group.getChildCount() <= start)
				return null;
			List<$> list = new ArrayList<$>();
			for (int i = start+1; i < group.getChildCount(); i++)
			{
				list.add($.with(group.getChildAt(i)));
			}
			return list;
		}
		return null;
	}
	
	/**
	 * If this view is a subclass of {@link AdapterView}, {@code slice} gets all Objects associated
	 * with the data for positions {@code start} to {@code end}. The Objects are
	 * then wrapped into the {@link #data} Object, and a list of droidQueries is returned.
	 * Otherwise, if this view is a subclass of {@link ViewGroup}, {@code slice} gets all subviews
	 * from the {@code start} to {@code end}, and wraps them into each droidQuery view in
	 * the list that is returned.
	 * @param start the starting index
	 * @return a list of droidQuery Objects.
	 */
	public List<$> slice(int start, int end)
	{
		if (this.view instanceof AdapterView)
		{
			AdapterView<?> group = (AdapterView<?>) view;
			if (group.getCount() <= start)
			{
				return null;
			}
			List<$> list = new ArrayList<$>();
			for (int i = start+1; i < Math.min(group.getCount(), end); i++)
			{
				list.add($.with(context).data(group.getItemAtPosition(i)));
			}
		}
		else if (this.view instanceof ViewGroup)
		{
			ViewGroup group = (ViewGroup) view;
			if (group.getChildCount() <= start)
				return null;
			List<$> list = new ArrayList<$>();
			for (int i = start+1; i < Math.min(group.getChildCount(), end); i++)
			{
				list.add($.with(group.getChildAt(i)));
			}
			return list;
		}
		return null;
	}
	
	/** @return the number of subviews or adapter cells in the current view */
	public int length()
	{
		if (view instanceof AdapterView)
		{
			return ((AdapterView<?>) view).getCount();
		}
		if (view instanceof ViewGroup)
		{
			return ((ViewGroup) view).getChildCount();
		}
		return 0;
	}
	
	/** @return the number of subviews or adapter cells in the current view */
	public int size()
	{
		return length();
	}
	
	/**
	 * Checks to see if the current view is a subclass of the given class name
	 * @param className the name of the superclass to check
	 * @return {@code true} if this {@link #view} is a subclass of the given class name. 
	 * Otherwise, {@code false}.
	 */
	public boolean is(String className)
	{
		try
		{
			Class<?> clazz = Class.forName(className);
			if (clazz.isInstance(this.view))
				return true;
			return false;
		}
		catch (Throwable t)
		{
			return false;
		}
	}
	
	/**
	 * Removes the current view from the layout
	 */
	public void remove()
	{
		ViewParent parent = this.view.getParent();
		if (parent != null && parent instanceof ViewGroup)
		{
			((ViewGroup) parent).removeView(view);
		}
	}
	
	/////Selectors
	
	/**
	 * Recursively selects all subviews of the given view
	 * @param v the parent view of all the suclasses to select
	 * @return a list of all views (wrapped in a droidQuery) that are subviews in the 
	 * view hierarchy with the given view as the root
	 */
	private List<$> recursivelySelectAllSubViews(View v)
	{
		List<$> list = new ArrayList<$>();
		if (v instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				list.addAll(recursivelySelectAllSubViews(((ViewGroup) v).getChildAt(i)));
			}
		}
		list.add($.with(v));
		return list;
	}
	
	/**
	 * Recursively selects all subviews of the given view that are subclasses of the given Object type
	 * @param v the parent view of all the suclasses to select
	 * @return a list of all views (wrapped in a droidQuery) that are subviews in the 
	 * view hierarchy with the given view as the root
	 */
	private List<$> recursivelySelectByType(View v, Class<?> clazz)
	{
		List<$> list = new ArrayList<$>();
		if (v instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				list.addAll(recursivelySelectByType(((ViewGroup) v).getChildAt(i), clazz));
			}
		}
		if (clazz.isInstance(v))
			list.add($.with(v));
		return list;
	}
	
	/**
	 * Select all views and return them in a droidQuery wrapper.
	 * @return a list of droidQuery Objects with the view set to each subview
	 */
	public List<$> selectAll()
	{
		return recursivelySelectAllSubViews(view);
	}
	
	/**
	 * Select all views that are subclasses of the given {@code className}. 
	 * @param className
	 * @return all the selected views in a droidQuery wrapper
	 */
	public List<$> selectByType(String className)
	{
		try
		{
			return recursivelySelectByType(this.view, Class.forName(className));
		}
		catch (Throwable t)
		{
			return null;
		}
		
	}
	
	/**
	 * Selects the child views of the current view
	 * @return a list of droidQuery Objects that wrap each returned subview. If the current
	 * view is a subclass of {@link AdapterView}, the data at each position is set to the {@link #data}
	 * attribute of the droidQuery instance
	 */
	public List<$> selectChildren()
	{
		List<$> list = new ArrayList<$>();
		if (view instanceof AdapterView)
		{
			for (int i = 0; i < ((AdapterView<?>) view).getCount(); i++)
			{
				list.add($.with(context).data(((AdapterView<?>) view).getItemAtPosition(i)));
			}
		}
		else if (view instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
			{
				list.add($.with(((ViewGroup) view).getChildAt(i)));
			}
		}
		return list;
	}
	
	/**
	 * Selects all subviews of the given view that do not contain subviews
	 * @param v the view whose subviews will be retrieved
	 * @return a list of droidQuery objects that wrap the empty views
	 */
	private List<$> recursivelySelectEmpties(View v)
	{
		List<$> list = new ArrayList<$>();
		if (v instanceof ViewGroup && ((ViewGroup) v).getChildCount() > 0)
		{
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				list.addAll(recursivelySelectEmpties(((ViewGroup) v).getChildAt(i)));
			}
		}
		else if (!(v instanceof AdapterView && ((AdapterView<?>) v).getCount() > 0))
		{
			list.add($.with(v));
		}
		return list;
	}
	
	/**
	 * Select all non-ViewGroups, or ViewGroups with no children
	 * @return a list of droidQuery objects that wrap the returned views
	 */
	public List<$> selectEmpties()
	{
		return recursivelySelectEmpties(this.view);
	}
	
	/**
	 * Searches the view hierarchy rooted at the given view in order to find the currently
	 * selected view
	 * @param view the view to search whithin
	 * @return the selected view, or null if no view in the given hierarchy was found.
	 */
	private View recursivelyFindSelectedSubView(View view)
	{
		if (view.isFocused())
			return view;
		else if (view instanceof ViewGroup)
		{
			View v = null;
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
			{
				v = recursivelyFindSelectedSubView(((ViewGroup) view).getChildAt(i));
				if (v != null)
					return v;
			}
			return null;
		}
		else
			return null;
	}
	
	/**
	 * Selects the currently-focused view.
	 * @return a droidQuery Object created with the currently-selected View
	 */
	public $ selectFocused()
	{
		if (this.view.isFocused())
			return $.with(view);
		return $.with(recursivelyFindSelectedSubView(view));
	}
	
	/**
	 * Select all {@link View#INVISIBLE invisible}, {@link View#GONE gone}, and 0-alpha views within the 
	 * view hierarchy rooted at the given view
	 * @param v the view hierarchy in which to search
	 * @return a list of droidQuery Objects that wrap the found views
	 */
	private List<$> recursivelySelectHidden(View v)
	{
		List<$> list = new ArrayList<$>();
		if (v instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				list.addAll(recursivelySelectHidden(((ViewGroup) v).getChildAt(i)));
			}
		}
		if (v.getVisibility() == View.INVISIBLE || v.getVisibility() == View.GONE || v.getAlpha() == 0)
			list.add($.with(v));
		return list;
	}
	
	/**
	 * Select all {@link View#VISIBLE visible} and 1-alpha views within the given view hierarchy
	 * @param v the view to search in
	 * @return a list of droidQuery Objects that wrap the found views
	 */
	private List<$> recursivelySelectVisible(View v)
	{
		List<$> list = new ArrayList<$>();
		if (v instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				list.addAll(recursivelySelectVisible(((ViewGroup) v).getChildAt(i)));
			}
		}
		if (v.getVisibility() == View.VISIBLE || v.getAlpha() == 1)
			list.add($.with(v));
		return list;
	}
	
	/**
	 * Select all {@link View#INVISIBLE invisible}, {@link View#GONE gone}, and 0-alpha views within the 
	 * view hierarchy rooted at the current view
	 * @return a list of droidQuery Objects that wrap the found views
	 */
	public List<$> selectHidden()
	{
		return recursivelySelectHidden(view);
	}
	
	/**
	 * Select all {@link View#VISIBLE visible} and 1-alpha views within the current view hierarchy
	 * @return a list of droidQuery Objects that wrap the found views
	 */
	public List<$> selectVisible()
	{
		return recursivelySelectVisible(view);
	}
	
	/**
	 * Set the current view to the view with the given id
	 * @param id the id of the view to manipulate
	 * @return this
	 */
	public $ id(int id)
	{
		this.view = this.findViewById(id);
		return this;
	}
	
	/**
	 * Selects all {@link ImageView}s within the current view hierarchy
	 * @return a list of droidQuery Objects that wrap the found {@code ImageView}s
	 */
	public List<$> selectImages()
	{
		return recursivelySelectByType(view, ImageView.class);
	}

	/**
	 * Selects all views within the given view hierarchy that are the single children of their 
	 * parent views
	 * @param v the view whose hierarchy will be checked
	 * @return a list of droidQuery Objects that wrap the found views.
	 */
	private List<$> recursivelySelectOnlyChilds(View v)
	{
		List<$> list = new ArrayList<$>();
		if (v instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				list.addAll(recursivelySelectOnlyChilds(((ViewGroup) v).getChildAt(i)));
			}
		}
		if (v.getParent() instanceof ViewGroup && ((ViewGroup) v.getParent()).getChildCount() == 1)
			list.add($.with(v));
		return list;
	}
	
	/**
	 * Selects all views within the current view hierarchy that are the single children of their 
	 * parent views
	 * @return a list of droidQuery Objects that wrap the found views.
	 */
	public List<$> selectOnlyChilds()
	{
		return recursivelySelectOnlyChilds(view);
	}
	
	/**
	 * Selects all views in the current hierarchy that can contain other child views
	 * @return a list of droidQuery Objects that wrap the found ViewGroups
	 */
	public List<$> selectParents()
	{
		return recursivelySelectByType(view, ViewGroup.class);
	}
	
	
	
	/////Extensions
	
	/**
	 * Add an extension with the reference <em>name</em>
	 * @param name String used by the {@link #ext(String)} method for calling this extension
	 * @param clazz the name of the subclass of {@link $Extension} that will be mapped to {@code name}.
	 * Calling {@code $.with(this).ext(myExtension); } will now create a new instance of the given
	 * {@code clazz}, passing in {@code this} instance of <em>$</em>, then calling the {@code invoke}
	 * method.
	 * @throws ClassNotFoundException if {@code clazz} is not a valid class name, or if it is not a 
	 * subclass of {@code $Extension}.
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @note be aware that there is no check for if this extension overwrites a different extension.
	 */
	public static void extend(String name, String clazz) throws ClassNotFoundException, SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException
	{
		Class<?> _class = Class.forName(clazz);
		Class<?> _super = _class.getSuperclass();
		if (_super == null || _super != $Extension.class)
		{
			throw new ClassNotFoundException("clazz must be subclass of $Extension!");
		}
		Constructor<?> constructor = _class.getConstructor(new Class<?>[]{$.class});
		extensions.put(name, constructor);
		
	}
	
	/**
	 * Add an extension with the reference <em>name</em>
	 * @param name String used by the {@link #ext(String)} method for calling this extension
	 * @param clazz subclass of {@link $Extension} that will be mapped to {@code name}.
	 * Calling {@code $.with(this).ext(MyExtension.class); } will now create a new instance of the given
	 * {@code clazz}, passing in {@code this} instance of <em>$</em>, then calling the {@code invoke}
	 * method.
	 * @throws ClassNotFoundException if {@code clazz} is not a valid class name, or if it is not a 
	 * subclass of {@code $Extension}.
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @note be aware that there is no check for if this extension overwrites a different extension.
	 */
	public static void extend(String name, Class<?> clazz) throws ClassNotFoundException, SecurityException, NoSuchMethodException
	{
		Class<?> _super = clazz.getSuperclass();
		if (_super == null || _super != $Extension.class)
		{
			throw new ClassNotFoundException("clazz must be subclass of $Extension!");
		}
		Constructor<?> constructor = clazz.getConstructor(new Class<?>[]{$.class});
		extensions.put(name, constructor);
	}
	
	/**
	 * Load the extension with the name defined in {@link #extend(String, String)}
	 * @param extension the name of the extension to load
	 * @return the new extension instance
	 */
	public $Extension ext(String extension, Object... args)
	{
		Constructor<?> constructor = extensions.get(extension);
		try {
			$Extension $e = ($Extension) constructor.newInstance(this);
			$e.invoke(args);
			return $e;
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
		
	}
	
	/////File IO
	
	/**
	 * Write a byte stream to file
	 * @param s the bytes to write to the file
	 * @param path defines the save location of the file
	 * @param append {@code true} to append the new data to the end of the file. {@code false} to overwrite any existing file.
	 * @param async {@code true} if the operation should be performed asynchronously. Otherwise, {@code false}.
	 */
	public void write(byte[] s, FileLocation path, String fileName, boolean append, boolean async)
	{
		write(s, path, fileName, append, async, null, null);
		
	}
	
	/**
	 * Write a String to file
	 * @param s the String to write to the file
	 * @param path defines the save location of the file
	 * @param append {@code true} to append the new String to the end of the file. {@code false} to overwrite any existing file.
	 * @param async {@code true} if the operation should be performed asynchronously. Otherwise, {@code false}.
	 */
	public void write(String s, FileLocation path, String fileName, boolean append, boolean async)
	{
		write(s.getBytes(), path, fileName, append, async, null, null);
		
	}
	
	/**
	 * Write a String to file, and execute functions once complete. 
	 * @param s the String to write to the file
	 * @param path defines the save location of the file
	 * @param append {@code true} to append the new String to the end of the file. {@code false} to overwrite any existing file.
	 * @param async {@code true} if the operation should be performed asynchronously. Otherwise, {@code false}.
	 * @param success Function to invoke on a successful file-write. Parameters received will be:
	 * <ol>
	 * <li>the String to write
	 * <li>the path to the file
	 * </ol>
	 * @param error Function to invoke on a file I/O error. Parameters received will be:
	 * <ol>
	 * <li>the String to write
	 * <li>the path to the file
	 * </ol>
	 */
	public void write(final String s, final FileLocation path, String fileName, boolean append, boolean async, final Function success, Function error)
	{
		boolean hasWritePermissions = false;
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
			if (info.requestedPermissions != null)
			{
				for (String p : info.requestedPermissions)
				{
					if (p.equals("android.permission.WRITE_EXTERNAL_STORAGE"))
					{
						hasWritePermissions = true;
						break;
					}
				}
			}
		} catch (Exception e)
		{
			if (error != null)
			{
				error.invoke(s, path, "Invalid Project Package!");
			}
			return;
		}
		if (!hasWritePermissions)
		{
			if (error != null)
			{
				error.invoke(s, path, "You do not have file write privelages. Add the android.permission.WRITE_EXTERNAL_STORAGE permission to your Android Manifest.");
			}
			return;
		}
		
		File logFile;
		
		if (path == FileLocation.INTERNAL)
		{
			if (fileName.contains("\\")) {
				if (error != null)
				{
					error.invoke(s, path, "Internal file names cannot include a path separator. Aborting.");
				}
				return;
			}
			try {
				if (fileObservers == null)
				{
					fileObservers = new ArrayList<LogFileObserver>();
				}
				LogFileObserver o = new LogFileObserver(fileName, new Runnable(){
					@Override
					public void run()
					{
						if (success != null)
							success.invoke(s, path);
					}
				});
				fileObservers.add(o);
				o.startWatching();
				
				FileOutputStream fw = context.openFileOutput(fileName, (append ? Context.MODE_APPEND : Context.MODE_PRIVATE));
				fw.write(s.getBytes());
				fw.close();
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return;
		}
		else if (path == FileLocation.CACHE)
		{
			String storageDir = context.getCacheDir().toString();
			
			logFile = new File(String.format(Locale.US, "%s/%s", storageDir, fileName));
			//make the parent directory if it does not exist
			logFile.getParentFile().mkdirs();
		}
		else if (path == FileLocation.DATA)
		{
			String storageDir = Environment.getExternalStorageDirectory().toString();
			String mainDirName = String.format(Locale.US, "%s/Android/data/%s", storageDir, context.getPackageName());
			
			logFile = new File(String.format(Locale.US, "%s/%s", mainDirName, fileName));
			//make the parent directory if it does not exist
			logFile.getParentFile().mkdirs();
		}
		else if (path == FileLocation.CUSTOM)
		{
			logFile = new File(fileName);
			//make the parent directory if it does not exist
			logFile.getParentFile().mkdirs();
		}
		else //external (default)
		{
			String storageDir = Environment.getExternalStorageDirectory().toString();
			String mainDirName = String.format(Locale.US, "%s/%s", storageDir, context.getPackageName());
			
			logFile = new File(String.format(Locale.US, "%s/%s", mainDirName, fileName));
			//make the parent directory if it does not exist
			logFile.getParentFile().mkdirs();
		}
		
		try {
			if (fileObservers == null)
			{
				fileObservers = new ArrayList<LogFileObserver>();
			}
			LogFileObserver o = new LogFileObserver(logFile, new Runnable(){
				@Override
				public void run()
				{
					if (success != null)
						success.invoke(s, path);
				}
			});
			
			fileObservers.add(o);
			o.startWatching();
			
			FileOutputStream os = new FileOutputStream(logFile, append);
			os.write(s.getBytes());
			os.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Write a byte stream to file, and execute functions once complete. 
	 * @param s the bytes to write to the file
	 * @param path defines the save location of the file
	 * @param append {@code true} to append the new bytes to the end of the file. {@code false} to overwrite any existing file.
	 * @param async {@code true} if the operation should be performed asynchronously. Otherwise, {@code false}.
	 * @param success Function to invoke on a successful file-write. Parameters received will be:
	 * <ol>
	 * <li>the byte[] to write
	 * <li>the path to the file
	 * </ol>
	 * @param error Function to invoke on a file I/O error. Parameters received will be:
	 * <ol>
	 * <li>the byte[] to write
	 * <li>the path to the file
	 * </ol>
	 */
	public void write(final byte[] s, final FileLocation path, String fileName, boolean append, boolean async, final Function success, Function error)
	{
		boolean hasWritePermissions = false;
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
			if (info.requestedPermissions != null)
			{
				for (String p : info.requestedPermissions)
				{
					if (p.equals("android.permission.WRITE_EXTERNAL_STORAGE"))
					{
						hasWritePermissions = true;
						break;
					}
				}
			}
		} catch (Exception e)
		{
			if (error != null)
			{
				error.invoke(s, path, "Invalid Project Package!");
			}
			return;
		}
		if (!hasWritePermissions)
		{
			if (error != null)
			{
				error.invoke(s, path, "You do not have file write privelages. Add the android.permission.WRITE_EXTERNAL_STORAGE permission to your Android Manifest.");
			}
			return;
		}
		
		File logFile;
		
		if (path == FileLocation.INTERNAL)
		{
			if (fileName.contains("\\")) {
				if (error != null)
				{
					error.invoke(s, path, "Internal file names cannot include a path separator. Aborting.");
				}
				return;
			}
			try {
				if (fileObservers == null)
				{
					fileObservers = new ArrayList<LogFileObserver>();
				}
				LogFileObserver o = new LogFileObserver(fileName, new Runnable(){
					@Override
					public void run()
					{
						if (success != null)
							success.invoke(s, path);
					}
				});
				fileObservers.add(o);
				o.startWatching();
				
				FileOutputStream fw = context.openFileOutput(fileName, (append ? Context.MODE_APPEND : Context.MODE_PRIVATE));
				fw.write(s);
				fw.close();
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return;
		}
		else if (path == FileLocation.CACHE)
		{
			String storageDir = context.getCacheDir().toString();
			
			logFile = new File(String.format(Locale.US, "%s/%s", storageDir, fileName));
			//make the parent directory if it does not exist
			logFile.getParentFile().mkdirs();
		}
		else if (path == FileLocation.DATA)
		{
			String storageDir = Environment.getExternalStorageDirectory().toString();
			String mainDirName = String.format(Locale.US, "%s/Android/data/%s", storageDir, context.getPackageName());
			
			logFile = new File(String.format(Locale.US, "%s/%s", mainDirName, fileName));
			//make the parent directory if it does not exist
			logFile.getParentFile().mkdirs();
		}
		else if (path == FileLocation.CUSTOM)
		{
			logFile = new File(fileName);
			//make the parent directory if it does not exist
			logFile.getParentFile().mkdirs();
		}
		else //external (default)
		{
			String storageDir = Environment.getExternalStorageDirectory().toString();
			String mainDirName = String.format(Locale.US, "%s/%s", storageDir, context.getPackageName());
			
			logFile = new File(String.format(Locale.US, "%s/%s", mainDirName, fileName));
			//make the parent directory if it does not exist
			logFile.getParentFile().mkdirs();
		}
		
		try {
			if (fileObservers == null)
			{
				fileObservers = new ArrayList<LogFileObserver>();
			}
			LogFileObserver o = new LogFileObserver(logFile, new Runnable(){
				@Override
				public void run()
				{
					if (success != null)
						success.invoke(s, path);
				}
			});
			
			fileObservers.add(o);
			o.startWatching();
			
			FileOutputStream os = new FileOutputStream(logFile, append);
			os.write(s);
			os.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	////Utilities
	
	/**
	 * Convert an Object Array to a JSONArray
	 * @param array an Object[] containing any of: JSONObject, JSONArray, String, Boolean, Integer, 
	 * Long, Double, NULL, or null. May not include NaNs or infinities. Unsupported values are not 
	 * permitted and will cause the JSONArray to be in an inconsistent state.
	 * @return the newly-created JSONArray Object
	 */
	public static JSONArray makeArray(Object[] array)
	{
		JSONArray json = new JSONArray();
		for (Object obj : array)
		{
			json.put(obj);
		}
		return json;
	}
	
	/**
	 * Convert a JSONArray to an Object Array
	 * @param array the array to convert
	 * @return the converted array
	 */
	public static Object[] makeArray(JSONArray array)
	{
		Object[] obj = new Object[array.length()];
		try
		{
			for (int i = 0; i < array.length(); i++)
			{
				obj[i] = array.get(i);
			}
			return obj;
		}
		catch (Throwable t)
		{
			return null;
		}
		
	}
	
	/**
	 * Converts a JSON String to a Map
	 * @param json the String to convert
	 * @return a Key-Value Mapping of attributes declared in the JSON string.
	 * @throws JSONException thrown if the JSON string is malformed or incorrectly written
	 */
	public static Map<String, ?> map(String json) throws JSONException
	{
		return map(new JSONObject(json));
	}
	
	/**
	 * Convert a {@code JSONObject} to a Map
	 * @param json the JSONObject to parse
	 * @return a Key-Value mapping of the Objects set in the JSONObject
	 * @throws JSONException if the JSON is malformed
	 */
	public static Map<String, ?> map(JSONObject json) throws JSONException
	{
		@SuppressWarnings("unchecked")
		Iterator<String> iterator = json.keys();
		Map<String, Object> map = new HashMap<String, Object>();
		
	    while (iterator.hasNext()) {
	        String key = iterator.next();
	        try {
	            Object value = json.get(key);
	            map.put(key, value);
	        } catch (JSONException e) {
	        	throw e;
	        } catch (Throwable t)
	        {
	        	if (key != null)
	        		Log.w("AjaxOptions", "Could not set value " + key);
	        	else
	        		throw new NullPointerException("Iterator reference is null.");
	        }
	    }
		return map;
	}
	
	/** 
	 * Merges the contents of two arrays together into the first array. 
	 */
	public static void merge(Object[] array1, Object[] array2)
	{
		Object[] newArray = new Object[array1.length + array2.length];
		for (int i = 0; i < array1.length; i++)
		{
			newArray[i] = array1[i];
		}
		for (int i = 0; i < array2.length; i++)
		{
			newArray[i] = array2[i];
		}
		array1 = newArray;
	}
	
	/**
	 * @return a new Function that does nothing when invoked
	 */
	public static Function noop()
	{
		return new Function() {
			@Override
			public void invoke(Object... args) {}
		};
	}
	
	/**
	 * @return the current time, in milliseconds
	 */
	public static long now()
	{
		return new Date().getTime();
	}
	
	/**
	 * Parses a JSON string into a JSONObject
	 * @param json the String to parse
	 * @return JSONObject if parse succeeds. Otherwise {@code null}.
	 */
	public JSONObject parseJSON(String json)
	{
		try {
			return new JSONObject(json);
		} catch (JSONException e) {
			return null;
		}
	}
	
	/**
	 * Parses XML into an XML Document
	 * @param xml the XML to parse
	 * @return XML Document if parse succeeds. Otherwise {@code null}.
	 */
	public Document parseXML(String xml)
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			return factory.newDocumentBuilder().parse(xml);
		} catch (Throwable t) {
			return null;
		}
	}
	
	/**
	 * Parses HTML into a {@link Spanned} Object
	 * @param html the HTML to parse
	 * @return the Spanned Object created from the given HTML
	 */
	public Spanned parseHTML(String html)
	{
		return Html.fromHtml(html);
	}
	
	
	/////AJAX
	
	/**
	 * Perform a new Ajax Task using the AjaxOptions set in the given Key-Value Map
	 * @param options {@link AjaxOptions} options
	 */
	public static void ajax(Map<String, Object> options)
	{
		ajax(new JSONObject(options));
	}
	
	/**
	 * Perform a new Ajax Task using the given JSON string to configure the {@link AjaxOptions}
	 * @param options {@link AjaxOptions} as a JSON String
	 */
	public static void ajax(String options)
	{
		try
		{
			ajax(new JSONObject(options));
		}
		catch (Throwable t)
		{
			throw new NullPointerException("Could not parse JSON!");
		}
	}
	
	/**
	 * Perform a new Ajax Task using the given JSONObject to configure the {@link AjaxOptions}
	 * @param options {@link AjaxOptions} as a JSONObject Object
	 */
	public static void ajax(JSONObject options)
	{
		try
		{
			new AjaxTask(options).execute();
		}
		catch (Throwable t)
		{
			Log.e("droidQuery", "Could not complete ajax task!", t);
		}
	}
	
	/**
	 * Perform an Ajax Task using the given {@code AjaxOptions}
	 * @param options the options to set for the Ajax Task
	 */
	public static void ajax(AjaxOptions options)
	{
		try
		{
			new AjaxTask(options).execute();
		}
		catch (Throwable t)
		{
			Log.e("droidQuery", "Could not complete ajax task!", t);
		}
	}
	
	///////ajax shortcut methods
	
	/**
	 * Shortcut method to use the default AjaxOptions to perform an Ajax GET request
	 * @param url the URL to access
	 * @param data the data to pass, if any
	 * @param success the Function to invoke once the task completes successfully.
	 * @param dataType the type of data to expect as a response from the URL. See 
	 * {@link AjaxOptions#dataType()} for a list of available data types
	 */
	public static void get(String url, Object data, Function success, String dataType)
	{
		$.ajax(new AjaxOptions().url(url).data(data).success(success).dataType(dataType));
	}
	
	/**
	 * Shortcut method to use the default Ajax Options to perform an Ajax GET request and receive
	 * a JSON-formatted response
	 * @param url the URL to access
	 * @param data the data to send, if any
	 * @param success Function to invoke once the task completes successfully.
	 */
	public static void getJSON(String url, Object data, Function success)
	{
		get(url, data, success, "JSON");
	}
	
	/**
	 * Shortcut method to use the default Ajax Options to perform an Ajax GET request and receive
	 * a Script response
	 * @param url the URL to access
	 * @param data the data to send, if any
	 * @param success Function to invoke once the task completes successfully.
	 * @see {@link ScriptResponse}
	 */
	public static void getScript(String url, Function success)
	{
		$.ajax(new AjaxOptions().url(url).success(success).dataType("SCRIPT"));
	}
	
	/**
	 * Shortcut method to use the default Ajax Options to perform an Ajax POST request
	 * @param url the URL to access
	 * @param data the data to post
	 * @param success Function to invoke once the task completes successfully.
	 * @param dataType the type of data to expect as a response from the URL. See 
	 * {@link AjaxOptions#dataType()} for a list of available data types
	 */
	public static void post(String url, Object data, Function success, String dataType)
	{
		$.ajax(new AjaxOptions().type("POST")
				                .url(url)
				                .data(data)
				                .success(success)
				                .dataType(dataType));
	}
	
	/**
	 * Register an event to invoke a Function every time a global Ajax Task completes
	 * @param complete the Function to call.
	 */
	public static void ajaxComplete(Function complete)
	{
		EventCenter.bind("ajaxComplete", complete, null);
	}
	
	/**
	 * Manually invoke the Function set to be called every time a global Ajax Task completes.
	 * @see #ajaxComplete(Function)
	 */
	public static void ajaxComplete()
	{
		EventCenter.trigger("ajaxComplete", null, null);
	}
	
	/**
	 * Register an event to invoke a Function every time a global Ajax Task receives an error
	 * @param error the Function to call.
	 */
	public static void ajaxError(Function error)
	{
		EventCenter.bind("ajaxError", error, null);
	}
	
	/**
	 * Manually invoke the Function set to be called every time a global Ajax Task receives an error.
	 * @see #ajaxError(Function)
	 */
	public static void ajaxError()
	{
		EventCenter.trigger("ajaxError", null, null);
	}

	/**
	 * Register an event to invoke a Function every time a global Ajax Task sends data
	 * @param send the Function to call.
	 */
	public static void ajaxSend(Function send)
	{
		EventCenter.bind("ajaxSend", send, null);
	}
	
	/**
	 * Manually invoke the Function set to be called every time a global Ajax Task sends data.
	 * @see #ajaxSend(Function)
	 */
	public static void ajaxSend()
	{
		EventCenter.trigger("ajaxSend", null, null);
	}
	
	/**
	 * Register an event to invoke a Function every time a global Ajax Task begins, if no other
	 * global task is running.
	 * @param start the Function to call.
	 */
	public static void ajaxStart(Function start)
	{
		EventCenter.bind("ajaxStart", start, null);
	}
	
	/**
	 * Manually invoke the Function set to be called every time a global Ajax Task begins, if 
	 * no other global task is running.
	 * @see #ajaxStart(Function)
	 */
	public static void ajaxStart()
	{
		EventCenter.trigger("ajaxStart", null, null);
	}
	
	/**
	 * Register an event to invoke a Function every time a global Ajax Task stops, if it was
	 * the last global task running
	 * @param stop the Function to call.
	 */
	public static void ajaxStop(Function stop)
	{
		EventCenter.bind("ajaxStop", stop, null);
	}

	/**
	 * Manually invoke the Function set to be called every time a global Ajax Task stops, if it was
	 * the last global task running
	 * @see #ajaxStop(Function)
	 */
	public static void ajaxStop()
	{
		EventCenter.trigger("ajaxStop", null, null);
	}
	
	/**
	 * Register an event to invoke a Function every time a global Ajax Task completes successfully.
	 * @param start the Function to invoke.
	 */
	public static void ajaxSuccess(Function success)
	{
		EventCenter.bind("ajaxSuccess", success, null);
	}

	/**
	 * Manually invoke the Function set to be called every time a global Ajax Task completes 
	 * successfully.
	 * @see #ajaxSuccess(Function)
	 */
	public static void ajaxSuccess()
	{
		EventCenter.trigger("ajaxSuccess", null, null);
	}
	
	/**
	 * Handle custom Ajax options or modify existing options before each request is sent and before they are processed by $.ajax().
	 * Note that this will be run in the background thread, so any changes to the UI must be made through a call to Activity.runOnUiThread().
	 * @param prefilter {@link Function} that will receive one Map argument with the following contents:
	 * <ul>
	 * <li>"options" : AjaxOptions for the request
	 * <li>"request" : HttpClient request Object
	 * </ul>
	 * 
	 */
	public static void ajaxPrefilter(Function prefilter)
	{
		EventCenter.bind("ajaxPrefilter", prefilter, null);
	}
	
	/**
	 * Setup global options
	 * @param options
	 */
	public static void ajaxSetup(AjaxOptions options)
	{
		AjaxOptions.ajaxSetup(options);
	}
	
	/**
	 * Clears async task queue
	 */
	public static void ajaxKillAll()
	{
		AjaxTask.killTasks();
	}
	
	/**
	 * Load data from the server and place the returned HTML into the matched element
	 * @param url A string containing the URL to which the request is sent.
	 * @param data A plain object or string that is sent to the server with the request.
	 * @param complete A callback function that is executed when the request completes. Will receive
	 * two arguments: 1. response text, 2. text status
	 */
	public void load(String url, Object data, final Function complete)
	{
		$.ajax(new AjaxOptions(url).data(data).complete(new Function() {

			@Override
			public void invoke(Object... params) {
				$.this.html(params[0].toString());
				complete.invoke(params);
			}
			
		}));
	}
	
	//// Convenience
	
	
	public $ html(int resourceID)
	{
		return html(context.getResources().getText(resourceID).toString());
	}
	
	/**
	 * Include the html string in this view. If this view has a setText method, it is used. Otherwise,
	 * a new TextView is created. This html can also handle image tags for both urls and local files.
	 * Local files should be the name (for example, for R.id.ic_launcher, just use ic_launcher).
	 * @param html
	 */
	public $ html(String html)
	{
		try
		{
			Method m = this.view.getClass().getMethod("setText", new Class<?>[]{CharSequence.class});
			m.invoke(this.view, (CharSequence) Html.fromHtml(html));
		}
		catch (Throwable t)
		{
			if (this.view instanceof ViewGroup)
			{
				try
				{
					//no setText method. Try a TextView
					TextView tv = new TextView(this.context);
					tv.setBackgroundColor(android.R.color.transparent);
					tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
					((ViewGroup) this.view).addView(tv);
					tv.setText(Html.fromHtml(html, new AsyncImageGetter(tv), null));
				}
				catch (Throwable t2)
				{
					//unable to set content
					Log.w("droidQuery", "unable to set HTML content");
				}
			}
			else
			{
				//unable to set content
				Log.w("droidQuery", "unable to set textual content");
			}
		}
		return this;
	}
	
	public $ text(int resourceID)
	{
		return text(context.getResources().getText(resourceID).toString());
	}
	
	/**
	 * Includes the given text string inside of this view. If this view has a setText method, it is used
	 * otherwise, if possible, a textview is added as a child to display the text.
	 * @param text
	 */
	public $ text(String text)
	{
		try
		{
			Method m = this.view.getClass().getMethod("setText", new Class<?>[]{CharSequence.class});
			m.invoke(this.view, (CharSequence) text);
		}
		catch (Throwable t)
		{
			if (this.view instanceof ViewGroup)
			{
				try
				{
					//no setText method. Try a TextView
					TextView tv = new TextView(this.context);
					tv.setBackgroundColor(android.R.color.transparent);
					tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
					tv.setText(text);
					((ViewGroup) this.view).addView(tv);
				}
				catch (Throwable t2)
				{
					//unable to set content
					Log.w("droidQuery", "unable to set textual content");
				}
			}
			else
			{
				//unable to set content
				Log.w("droidQuery", "unable to set textual content");
			}
		}
		return this;
	}
	
	public static void toast(Context context, String text, int duration)
	{
		Toast.makeText(context, text, duration).show();
	}
	
	public static void alert(Context context, String text)
	{
		alert(context, context.getString(context.getResources().getIdentifier("app_name", "string", context.getPackageName())), text);
	}
	
	public static void alert(Context context, String title, String text)
	{
		AlertDialog alert = new AlertDialog.Builder(context).create();
		alert.setTitle(title);
		alert.setMessage(text);
		alert.setButton("OK", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
			
		});
		alert.show();
	}
	
	public void alert(String text)
	{
		alert(context.getString(context.getResources().getIdentifier("app_name", "string", context.getPackageName())), text);
	}
	
	public void alert(String title, String text)
	{
		AlertDialog alert = new AlertDialog.Builder(context).create();
		alert.setTitle(title);
		alert.setMessage(text);
		alert.setButton("OK", new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
			
		});
		alert.show();
	}
	
	////Callbacks
	
	/**
	 * A multi-purpose callbacks list object that provides a powerful way to manage callback lists.
	 * @return a new instance of {@link Callbacks}
	 */
	public static Callbacks Callbacks()
	{
		return new Callbacks();
	}
	
	//////CSS-based
	
	/**
	 * Get the current computed height for the first element in the set of matched 
	 * elements or set the height of every matched element.
	 * @return
	 */
	public int height()
	{
		return Math.abs(view.getBottom() - view.getTop());
	}
	
	/**
	 * Get the current computed width for the first element in the set of matched elements 
	 * or set the width of every matched element.
	 * @return
	 */
	public int width()
	{
		return Math.abs(view.getRight() - view.getLeft());
	}
	
	/**
	 * Get the current computed height for the first element in the set of matched elements, 
	 * including padding but not margin.
	 * @return
	 */
	public int innerHeight()
	{
		return height() - (view.getPaddingTop() + view.getPaddingBottom());
	}

	/**
	 * Get the current computed width for the first element in the set of matched elements, 
	 * including padding but not margin.
	 * @return
	 */
	public int innerWidth()
	{
		return width() - (view.getPaddingLeft() + view.getPaddingRight());
	}
	
	/**
	 * Get the current computed height for the first element in the set of matched elements, 
	 * including padding and margin.
	 * @return
	 */
	public int outerHeight()
	{
		Object params = view.getLayoutParams();
		int margin = 0;
		try
		{
			margin += Integer.valueOf(params.getClass().getField("bottomMargin").get(params).toString());
			margin += Integer.valueOf(params.getClass().getField("topMargin").get(params).toString());
		}
		catch (Throwable t)
		{
			//cannot get margin values
		}
		return height() - (view.getPaddingTop() + view.getPaddingBottom() + margin);
	}
	
	/**
	 * Get the current computed width for the first element in the set of matched elements, 
	 * including padding and margin.
	 * @return
	 */
	public int outerWidth()
	{
		Object params = view.getLayoutParams();
		int margin = 0;
		try
		{
			margin += Integer.valueOf(params.getClass().getField("leftMargin").get(params).toString());
			margin += Integer.valueOf(params.getClass().getField("rightMargin").get(params).toString());
		}
		catch (Throwable t)
		{
			//cannot get margin values
		}
		return width() - (view.getPaddingLeft() + view.getPaddingRight() + margin);
	}
	
	/**
	 * Get the current coordinates of the first element, or set the coordinates of every element, 
	 * in the set of matched elements, relative to the document.
	 * @return
	 */
	public Point offset()
	{
		int[] loc = new int[2];
		view.getLocationOnScreen(loc);
		return new Point(loc[0], loc[1]);
	}
	
	/**
	 * Get the current coordinates of the first element in the set of matched elements, 
	 * relative to the offset parent.
	 * @return
	 */
	public Point position()
	{
		return new Point(view.getLeft(), view.getTop());
	}
	
	/**
	 * Get the current horizontal position of the scroll bar for the first element in the set of 
	 * matched elements or set the horizontal position of the scroll bar for every matched element.
	 * @return
	 */
	public int scrollLeft()
	{
		return view.getScrollX();
	}
	
	/**
	 * Get the current vertical position of the scroll bar for the first element in the set of 
	 * matched elements or set the vertical position of the scroll bar for every matched element.
	 */
	public int scrollTop()
	{
		return view.getScrollY();
	}
	
	//////ignoring jQuery data. Doesn't come off as important here.
	
	
	
	//Timer Functions
	
	/**
	 * Schedule a task for single execution after a specified delay.
	 * @param function the task to schedule. Receives no args. Note that the function will be
	 * run on a Timer thread, and not the UI Thread.
	 * @param delay amount of time in milliseconds before execution.
	 * @return the created Timer
	 */
	public static Timer setTimeout(final Function function, long delay)
	{
		Timer t = new Timer();
		t.schedule(new TimerTask(){

			@Override
			public void run() {
				function.invoke();
			}
			
		}, delay);
		return t;
	}
	
	/**
	 * Schedule a task for repeated fixed-rate execution after a specific delay has passed.
	 * @param the task to schedule. Receives no args. Note that the function will be
	 * run on a Timer thread, and not the UI Thread.
	 * @param delay amount of time in milliseconds before execution.
	 * @return the created Timer
	 */
	public static Timer setInterval(final Function function, long delay)
	{
		Timer t = new Timer();
		t.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				function.invoke();
			}
			
		}, 0, delay);
		return t;
	}
	
	/** 
	 * Capitalizes the first letter of the given string.
	 * @param string the string whose first letter should be capitalized
	 * @return the given string with its first letter capitalized
	 * @throws NullPointerException if the string is null or empty
	 */
	private String capitalize(String string)
	{
		if (string == null || string.isEmpty())
			throw new NullPointerException("Cannot handle null or empty string");
		
		StringBuilder strBuilder = new StringBuilder(string);
		strBuilder.setCharAt(0, Character.toUpperCase(strBuilder.charAt(0)));
		return strBuilder.toString();
	}

	/**
	 * Used for Observing when a log file has been written to.
	 * @author Phil Brown
	 */
	class LogFileObserver extends FileObserver
	{
		/** the runnable to post when the file is no longer being written to */
		private Runnable r;
		
		/**
		 * Create the observer
		 * @param path the file to observe
		 * @param postExec the runnable to post when the file is no longer being written to
		 */
		public LogFileObserver(String path, Runnable postExec)
		{
			super(path, FileObserver.CLOSE_WRITE);
			r = postExec;
		}
		
		/**
		 * Create the observer
		 * @param file the file to observe
		 * @param postExec the runnable to post when the file is no longer being written to
		 */
		public LogFileObserver(File file, Runnable postExec)
		{
			super(file.getAbsolutePath(), FileObserver.CLOSE_WRITE);
			r = postExec;
		}

		@Override
		public void onEvent(int event, String path) {
			if (event == FileObserver.CLOSE_WRITE)
			{
				r.run();
				stopWatching();
				fileObservers.remove(this);
			}
		}
		
	}
	
	/**
	 * Sets a function for double click events registered with dblclick.
	 * Receives this droidQuery instance and the motionEvent as params.
	 * Third param if set in constructor.
	 */
	class DoubleClickHandler extends GestureDetector.SimpleOnGestureListener
	{
		private Function function;
		private Object data;
		
		public DoubleClickHandler(Function function, Object eventData)
		{
			this.function = function;
			this.data = eventData;
		}
		
		@Override
		public boolean onDoubleTap (MotionEvent e)
		{
			if (function != null)
			{
				if (data != null)
					function.invoke($.this, e, data);
				else
					function.invoke($.this, e);
				return true;
			}
			return false;
		}
	}
}
