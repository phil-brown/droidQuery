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
 * droidQuery:<br>
 * jQuery-esque functions for Android.
 * @author Phil Brown
 */
public class $ 
{
	public static enum DataType
	{
		JSON, XML, TEXT, SCRIPT, IMAGE
	};
	
	/**
	 * File locations used for {@code write} methods
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
	 * Relates to the interpolator used for droidQuery animations
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
	
	private static final Map<Class<?>, Class<?>> PRIMITIVE_TYPE_MAP = buildPrimitiveTypeMap();

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
	
	
	/** Keeps track of {@link LogFileObserver}s so they are not freed by garbage collection before they are used */
	private static List<LogFileObserver> fileObservers;

	private Context context;
	private View view;
	private View rootView;
	private static Map<String, Constructor<?>> extensions = new HashMap<String, Constructor<?>>();
	
	private Function onFocus, offFocus;
	private Function keyDown, keyPress, keyUp;
	private Function swipe, swipeUp, swipeDown, swipeLeft, swipeRight;
	
	private SwipeDetector swiper;
	
	/**
	 * Constructor<br>
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
	
	public $(View view)
	{
		this.rootView = view;
		this.view = view;
		this.context = view.getContext();
		setup();
	}
	
	private void setup()
	{
		setupFocusListener();
		setupKeyListener();
		setupSwipeListener();
	}
	
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
	
	/** Convenience method for initializing class (removes the need for the new keyword): 
	 * <pre>
	 * $.with(context)
	 * </pre> 
	 */
	public static $ with(Context context)
	{
		return new $(context);
	}
	
	public static $ with(View view)
	{
		return new $(view);
	}
	
	/**
	 * Shortcut method to $.with(this).id(R.id.main)
	 * @param context
	 * @param id
	 * @return
	 */
	public static $ with(Context context, int id) 
	{
		return $.with(context).id(id);
	}
	
	private View findViewById(int id)
	{
		View v = null;
		if (context instanceof Activity)
			v = ((Activity) context).findViewById(id);
		else	
			v = rootView.findViewById(id);
		return v;
	}

	/** creates a new view and sets its reference in {@link #view} */
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

	/** gets a new view and sets its reference in {@link #view} */
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
	
	/** gets a new view and sets its reference in {@link #view} */
	public $ push(View v)
	{
		return add(v);
	}
	
	/**
	 * Pops up an entire generation in hierarchy - meaning any siblings will also be removed
	 * @return
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
	
	/** sets the controlling view to the parent of the current view. Does not remove any view. */
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
	
	public $ animate(String properties, long duration, Easing easing, Function complete)
	{
		return animate(properties, AnimationOptions.create().duration(duration).easing(easing).complete(complete));
	}
	
	/**
	 * Animate the current view. Example:
	 * <pre>
	 * $.with(mView).animate("{
	 *                           left: 1000dip;
	 *                           top: 0px;
	 *                           width: 50%;
	 *                           alpha: 0.5;
	 *                        }", 
	 *                        3000, 
	 *                        "linear", 
	 *                        new Function() {
	 *                        	public void invoke(Object... args) {
	 *                        		$.alert("Animation Complete!");
	 *                        	}
	 *                        }");
	 * </pre>
	 * @param properties CSS representation
	 * @param duration
	 * @param easing default is linear
	 * @param complete
	 * @return
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
	
	public $ animate(Map<String, Object> properties, long duration, Easing easing, final Function complete)
	{
		return animate(properties, AnimationOptions.create().duration(duration).easing(easing).complete(complete));
	}
	
	/**
	 * Animate multiple view properties at the same time. Example:
	 * <pre>
	 * $.with(myView).animate(new QuickMap(QuickEntry.qe("alpha", .8f), QuickEntry.qe("width", 50%)), 400, Easing.LINEAR, null);
	 * </pre>
	 * @param properties
	 * @param duration
	 * @param easing
	 * @param complete
	 * @return
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
				if (options.always() != null)
					options.always().invoke($.this);
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (options.complete() != null)
					options.complete().invoke($.this);
				if (options.always() != null)
					options.always().invoke($.this);
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void fadeIn(AnimationOptions options)
	{
		this.animate(new QuickMap(QuickEntry.qe("alpha", new Float(1.0f))), options);
	}
	
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void fadeOut(AnimationOptions options)
	{
		this.animate(new QuickMap(QuickEntry.qe("alpha", new Float(0.0f))), options);
	}
	
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void fadeTo(float opacity, AnimationOptions options)
	{
		this.animate(new QuickMap(QuickEntry.qe("alpha", new Float(opacity))), options);
	}
	
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
	
	public void fadeToggle(long duration, final Function complete)
	{
		if (this.view.getAlpha() < 0.5)
			this.fadeIn(duration, complete);
		else
			this.fadeIn(duration, complete);
	}
	
	public void fadeToggle(AnimationOptions options)
	{
		if (this.view.getAlpha() < 0.5)
			this.fadeIn(options);
		else
			this.fadeIn(options);
	}
	
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void slideUp(AnimationOptions options)
	{
		this.animate(new QuickMap(QuickEntry.qe("y", new Float(0f))), options);
	}
	
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
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void slideLeft(AnimationOptions options)
	{
		this.animate(new QuickMap(QuickEntry.qe("x", new Float(0f))), options);
	}
	
	
	/** get attribute */
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
	
	/** set attribute */
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
	 * This method hands the current context and the view to the developer to make custom changes.
	 * This can be used to do many things that the methods provided by droidQuery do not. For example:
	 * <pre>
	 * TextView tv = new TextView(this);
	 * $.with(tv).on("click", new Function() {
	 * 	public void invoke(Object... args) {
	 * 		TextView mTV = (TextView) args[0];
	 * 		$.alert(mTV.getText().toString());
	 * 	}
	 * }).attr("textColor", Color.BLUE).manage(new Function() {
	 * 	public void invoke(Object... args) {
	 * 		Context context = args[0];
	 * 		View v = args[1];
	 * 		TextView mTV = (TextView) v;
	 * 			mTV.addTextChangedListener(new TextWatcher(){
	 *
	 *			@Override
	 *			public void afterTextChanged(Editable s) { }
	 *
	 *			@Override
	 *			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	 *
	 *			@Override
	 *			public void onTextChanged(CharSequence s, int start, int before, int count) {}
	 *			
	 *		});
	 * 	}
	 * });
	 * </pre>
	 * @param function receives the current context as {@code arg0}. {@code arg1} is set to the current
	 * view.
	 * @return this droidQuery
	 */
	public $ manage(Function function)
	{
		function.invoke(context, view);
		return this;
	}
	
	/** shortcut to addView */
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
	
	/** add view with id */
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
	
	/** remove view */
	public $ remove(View v)
	{
		if (this.view instanceof ViewGroup)
		{
			((ViewGroup) this.view).removeView(v);
		}
		return null;
	}
	
	/** remove view with id */
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
	
	public $ hide()
	{
		view.setVisibility(View.VISIBLE);
		return this;
	}
	
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
	 * 		$.alert((String) data);
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
	 * 		$.alert((String) data);
	 * 	}
	 * });
	 * 
	 * $.with(myView).bind("LongClick", "Hello World!", new Function() {
	 * 	public void invoke(Object... args) {
	 * 		View v = args[0];
	 * 		Object data = args[1];
	 * 		$.alert((String) data);
	 * 	}
	 * });
	 * </pre>
	 * However, this will fail:
	 * <pre>
	 * $.with(myView).bind("longclick", "Hello World!", new Function() {
	 * 	public void invoke(Object... args) {
	 * 		View v = args[0];
	 * 		Object data = args[1];
	 * 		$.alert((String) data);
	 * 	}
	 * });
	 * </pre>
	 * @param eventType should be the verb in OnVerbListener
	 * @param data an Object passed to {@code handler} when the event is triggered.
	 * @param handler receives two arguments: the affected view, and the {@code data} parameter
	 * @return the current instance of {@code droidQuery}
	 * @see #on(String, Function)
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
	 * Like bind, only
	 * @param event
	 * @param handler
	 * @return
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
	 * add standard change functionality to standard views.
	 * @return
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
	 * get the value associated with the view. If the view is a Textview, returns the CharSequence text.
	 * If button, the boolean checked state. If Image, the drawable.
	 * @return
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
	 * set the value associated with the view. If the view is a Textview, returns the CharSequence text.
	 * If button, the boolean checked state. If Image, the drawable or bitmap.
	 * @return
	 */
	public void val(Object object)
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
	}
	
	/**
	 * simulate a click event on this view
	 * @return
	 */
	public $ click()
	{
		this.view.performClick();
		return this;
	}
	
	/**
	 * perform the given function for click events on this view. The function will receive this droidQuery
	 * as a parameter.
	 * @param function
	 * @return
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
	 * perform the given function for click events on this view. The function will receive two arguments:
	 * 1. this droidQuery, 2. {@code eventData}
	 * @param eventData
	 * @param function
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
	
	public $ longclick()
	{
		this.view.performLongClick();
		return this;
	}
	
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
	 * Handles swipes. This will override any onTouchListener added.
	 * @param function will receive this droidQuery a {@link SwipeDetector.Direction} corresponding
	 * to the direction of the swipe.
	 * @return
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
	 * @param function
	 * @return
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
	 * @param function
	 * @return
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
	 * @param function
	 * @return
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
	 * @param function
	 * @return
	 */
	public $ swipeRight(Function function)
	{
		swipeRight = function;
		setupSwipeListener();
		return this;
	}
	
	public $ swipeUp()
	{
		if (swiper != null)
			swiper.performSwipeUp();
		return this;
	}
	
	public $ swipeDown()
	{
		if (swiper != null)
			swiper.performSwipeDown();
		return this;
	}
	
	public $ swipeLeft()
	{
		if (swiper != null)
			swiper.performSwipeLeft();
		return this;
	}
	
	public $ swipeRight()
	{
		if (swiper != null)
			swiper.performSwipeRight();
		return this;
	}
	
	/**
	 * will receive param $.
	 * @param function
	 * @return
	 */
	public $ focus(Function function)
	{
		onFocus = function;
		setupFocusListener();//fixes any changes to the onfocuschanged listener
		return this;
	}
	
	public $ focus()
	{
		this.view.requestFocus();
		return this;
	}
	
	/**
	 * will receive param $.
	 * @param function
	 * @return
	 */
	public $ focusout(Function function)
	{
		offFocus = function;
		setupFocusListener();//fixes any changes to the onfocuschanged listener
		return this;
	}
	
	public $ focusout()
	{
		this.view.clearFocus();
		return this;
	}
	
	/**
	 * 
	 * @param function receives 1. this droidQuery 2. the key code (int) 3. the event (KeyEvent)
	 * @return
	 */
	public $ keydown(Function function)
	{
		keyDown = function;
		setupKeyListener();
		return this;
	}
	
	/**
	 * 
	 * @param function receives 1. this droidQuery 2. the key code (int) 3. the event (KeyEvent)
	 * @return
	 */
	public $ keypress(Function function)
	{
		keyPress = function;
		setupKeyListener();
		return this;
	}
	
	/**
	 * 
	 * @param function receives 1. this droidQuery 2. the key code (int) 3. the event (KeyEvent)
	 * @return
	 */
	public $ keyup(Function function)
	{
		keyUp = function;
		setupKeyListener();
		return this;
	}
	
	/**
	 * add function for if this adapter view has a selected item
	 * @param function receives two args. 1: this droidQuery. 2. the view position, or -1 if none is selected.
	 * @return
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
	 * Remove a previously-attached event handler from this view
	 * @param eventType
	 * @param data
	 */
	public void unbind(String eventType, Object data)
	{
		String method = String.format(Locale.US, "setOn%sListener", capitalize(eventType));
		String listener = String.format(Locale.US, "%s.On%sListener", this.view.getClass().getName(), capitalize(eventType));
		try
		{
			//dynamically create instance of the listener interface
			
			Class<?> eventInterface = Class.forName(listener);
			Method setEventListener = this.view.getClass().getMethod(method, new Class<?>[]{eventInterface});
			EventHandlerCreator proxy = new EventHandlerCreator(new Function(){
				@Override
				public void invoke(Object... params){}
			}, this.view, data);
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
	 * Loops through all the child views in {@link view}, and wraps each in a droidQuery object
	 * @param function receives the droidQuery for the view, and the index for arg1
	 */
	public $ each(Function function)
	{
		if (this.view instanceof ViewGroup)
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
	 * Loops through all the child views in {@link view}, and wraps each in a droidQuery object
	 * @param function receives the droidQuery for the view, and the index for arg1
	 */
	public $ children(Function function)
	{
		return each(function);
	}
	
	/**
	 * Loops through all the sibling views in {@link view}, and wraps each in a droidQuery object
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
	
	public List<$> slice(int start)
	{
		if (this.view instanceof ViewGroup)
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
	
	public List<$> slice(int start, int end)
	{
		if (this.view instanceof ViewGroup)
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
	
	/** returns number of children in current view */
	public int length()
	{
		if (view instanceof ViewGroup)
		{
			return ((ViewGroup) view).getChildCount();
		}
		return 0;
	}
	
	public int size()
	{
		return length();
	}
	
	/**
	 * performs a {@code instanceof} check of the current View Object
	 * @param className
	 * @return
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
	
	//remove the current view from the layout
	public void remove()
	{
		ViewParent parent = this.view.getParent();
		if (parent != null && parent instanceof ViewGroup)
		{
			((ViewGroup) parent).removeView(view);
		}
	}
	
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
	
	/////Selectors
	
	/**
	 * Select all views and return them in a droidQuery wrapper.
	 * @return
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
	 * Selects the current view's children
	 * @return
	 */
	public List<$> selectChildren()
	{
		List<$> list = new ArrayList<$>();
		if (view instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
			{
				list.add($.with(((ViewGroup) view).getChildAt(i)));
			}
		}
		return list;
	}
	
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
		else
		{
			list.add($.with(v));
		}
		return list;
	}
	
	/**
	 * Select all non-ViewGroups, or ViewGroups with no children
	 * @return
	 */
	public List<$> selectEmpties()
	{
		return recursivelySelectEmpties(this.view);
	}
	
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
	
	public $ selectFocused()
	{
		if (this.view.isFocused())
			return $.with(view);
		return $.with(recursivelyFindSelectedSubView(view));
	}
	
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
	
	public List<$> selectHidden()
	{
		return recursivelySelectHidden(view);
	}
	
	public $ id(int id)
	{
		this.view = this.findViewById(id);
		return this;
	}
	
	public List<$> selectImages()
	{
		return recursivelySelectByType(view, ImageView.class);
	}

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
	
	public List<$> selectOnlyChilds()
	{
		return recursivelySelectOnlyChilds(view);
	}
	
	public List<$> selectParents()
	{
		return recursivelySelectByType(view, ViewGroup.class);
	}
	
	public List<$> selectVisible()
	{
		return recursivelySelectVisible(view);
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
	 * @param extension
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
	 * Write a String to file
	 * @param s
	 * @param path
	 * @param append
	 * @param async
	 */
	public void write(byte[] s, FileLocation path, String fileName, boolean append, boolean async)
	{
		write(s, path, fileName, append, async, null, null);
		
	}
	
	/**
	 * Write a String to file, and execute functions once complete. 
	 * @param s
	 * @param path
	 * @param append
	 * @param async
	 * @param success. Parameters will be the byte[] to write and the path to file.
	 * @param error. Parameters will be the byte[] to write, the path to the file and error message.
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
	 * 
	 * @param array an Object[] containing any of: JSONObject, JSONArray, String, Boolean, Integer, 
	 * Long, Double, NULL, or null. May not be NaNs or infinities. Unsupported values are not 
	 * permitted and will cause the JSONArray to be in an inconsistent state.
	 * @return
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
	
	public static Map<String, ?> map(String json) throws JSONException
	{
		return map(new JSONObject(json));
	}
	
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
	
	/** Merge the contents of two arrays together into the first array. */
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
	
	public static Function noop()
	{
		return new Function() {
			@Override
			public void invoke(Object... args) {}
		};
	}
	
	public static long now()
	{
		return new Date().getTime();
	}
	
	/**
	 * parses a json string.
	 * @param json
	 * @return JSONObject if parse succeeds. Otherwise null.
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
	 * parses the XML
	 * @param xml
	 * @return XML Document if parse succeeds. Otherwise null.
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
	 * parses the HTML
	 * @param html
	 * @return
	 */
	public Spanned parseHTML(String html)
	{
		return Html.fromHtml(html);
	}
	
	
	/////AJAX
	
	/**
	 * Map
	 */
	public static void ajax(Map<String, Object> options)
	{
		ajax(new JSONObject(options));
	}
	
	/**
	 * JSON String
	 * @param options
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
	 * JSONObject
	 * @param options
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
	
	public static void get(String url, Object data, Function success, String dataType)
	{
		$.ajax(new AjaxOptions().url(url).data(data).success(success).dataType(dataType));
	}
	
	public static void getJSON(String url, Object data, Function success)
	{
		get(url, data, success, "JSON");
	}
	
	public static void getScript(String url, Function success)
	{
		$.ajax(new AjaxOptions().url(url).success(success).dataType("SCRIPT"));
	}
	
	public static void post(String url, Object data, Function success, String dataType)
	{
		$.ajax(new AjaxOptions().type("POST")
				                .url(url)
				                .data(data)
				                .success(success)
				                .dataType(dataType));
	}
	
	public static void ajaxComplete(Function complete)
	{
		EventCenter.bind("ajaxComplete", complete, null);
	}
	public static void ajaxComplete()
	{
		EventCenter.trigger("ajaxComplete", null, null);
	}
	public static void ajaxError(Function error)
	{
		EventCenter.bind("ajaxError", error, null);
	}
	public static void ajaxError()
	{
		EventCenter.trigger("ajaxError", null, null);
	}
	//send start stop success

	public static void ajaxSend(Function send)
	{
		EventCenter.bind("ajaxSend", send, null);
	}
	public static void ajaxSend()
	{
		EventCenter.trigger("ajaxSend", null, null);
	}
	
	public static void ajaxStart(Function start)
	{
		EventCenter.bind("ajaxStart", start, null);
	}
	public static void ajaxStart()
	{
		EventCenter.trigger("ajaxStart", null, null);
	}
	
	public static void ajaxStop(Function stop)
	{
		EventCenter.bind("ajaxStop", stop, null);
	}
	public static void ajaxStop()
	{
		EventCenter.trigger("ajaxStop", null, null);
	}
	
	public static void ajaxSuccess(Function success)
	{
		EventCenter.bind("ajaxSuccess", success, null);
	}
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
