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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import self.philbrown.css.CSSSelector;
import self.philbrown.cssparser.TokenSequence;
import self.philbrown.droidQuery.AjaxOptions.Redundancy;
import self.philbrown.droidQuery.SwipeDetector.SwipeListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
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
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.webkit.URLUtil;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.nineoldandroids.view.ViewHelper;


/** 
 * <h1>droidQuery</h1>
 * An <a href="https://github.com/github/android">Android</a> <i>port</i> of 
 * <a href="https://github.com/jquery/jquery">jQuery</a>. Supports Android API 2.2 (Froyo) and up.
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
	/** The current views that will be manipulated */
	private List<View> views;
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
		this.views = new ArrayList<View>();
		if (context instanceof Activity)//if an activity is used, and a contentView is set, use this as the view
		{
			View view = this.findViewById(android.R.id.content).getRootView();
			if (view != null)
			{
				this.rootView = view;
				this.views.add(view);
			}
			else
			{
				//if no view is set, make sure not to get null pointers from view references.
				view = new View(context);
				this.rootView = view;
				this.views.add(view);
			}
		}
		else
		{
			View view = new View(context);//if view operations are attempted without the view set, this prevents null pointer exceptions
			this.rootView = view;
			this.views.add(view);
		}
		
		
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
		this.views = new ArrayList<View>();
		this.views.add(view);
		this.context = view.getContext();
	}
	
	/**
	 * Constructor used to privately and safely allow the creation of droidQuery instances using with empty lists
	 * @param context the current context
	 * @param views the selection
	 * @see #with(Context, List)
	 */
	private $(Context context, List<View> views)
	{
		if (views == null)
			views = new ArrayList<View>();
		this.views = views;
	}
	
	/**
	 * Constructor. Accepts a <em>List&lt;View&gt;</em> Object.
	 * @param view
	 * @see #with(View)
	 */
	public $(List<View> views)
	{
		if (views == null)
		{
			throw new NullPointerException("Cannot create droidQuery Instance with null List.");
		}
		else if (views.isEmpty())
		{
			throw new NullPointerException("Cannot create droidQuery Instance with empty List.");
		}
		this.rootView = views.get(0);
		this.context = this.rootView.getContext();
		this.views = views;
	}
	
	
	
	/**
	 * Constructor. Accepts a {@code View[]} Object or a Variable number of View objects (varargs).
	 * @param views 
	 * @see #with(View)
	 */
	public $(View... views)
	{
		if (views == null)
		{
			throw new NullPointerException("Cannot create droidQuery Instance with null Array.");
		}
		else if (views.length == 0)
		{
			throw new NullPointerException("Cannot create droidQuery Instance with empty Array.");
		}
		this.rootView = views[0];
		this.context = this.rootView.getContext();
		this.views = Arrays.asList(views);
	}
	
	/**
	 * Uses css selector to make a selection within the scope of the given parent view.
	 * @param parent
	 * @param selector
	 */
	public $(View parent, String selector)
	{
		this(parent);
		try
		{
			$ query = new CSSSelector().makeSelection(parent, TokenSequence.parse(selector));
			this.rootView = query.rootView;
			this.context = query.context;
			this.views = query.views;
		}
		catch(Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	/**
	 * Constructor that clones the selection and the data of another droidQuery instance.
	 * @param droidQuery
	 */
	public $($ droidQuery)
	{
		this.rootView = droidQuery.rootView;
		this.views = new ArrayList<View>(droidQuery.views);
		this.context = droidQuery.context;
		this.data = droidQuery.data;
	}
	
	/**
	 * Use css selectors to make selection
	 * <pre>
	 * new $(this, ".TextEdit");
	 * </pre>
	 * @param context
	 * @param resources
	 */
	public $(Context context, String selector)
	{
		this(context);
		try
		{
			$ query = new CSSSelector().makeSelection(this.rootView, TokenSequence.parse(selector));
			this.rootView = query.rootView;
			this.context = query.context;
			this.views = query.views;
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
		
	}
	
	/**
	 * Refreshes the listeners for focus changes
	 */
	private void setupFocusListener()
	{
		for (View view : views)
		{
			view.setOnFocusChangeListener(new View.OnFocusChangeListener(){

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus && onFocus != null)
					{
						onFocus.invoke($.with(v));
					}
					else if (!hasFocus && offFocus != null)
					{
						offFocus.invoke($.with(v));
					}
				}
				
			});
		}
	}
	
	/**
	 * Refreshes the listeners for key events
	 */
	private void setupKeyListener()
	{
		for (View view : views)
		{
			view.setOnKeyListener(new View.OnKeyListener() {
				
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					boolean retVal = false;
					switch(event.getKeyCode())
					{
						case KeyEvent.ACTION_DOWN : {
							if (keyDown != null)
							{
								keyDown.invoke($.with(v), keyCode, event);
								retVal = true;
							}
							break;
						}
						case KeyEvent.ACTION_UP : {
							if (keyUp != null)
							{
								keyUp.invoke($.with(v), keyCode, event);
								retVal = true;
							}
							break;
						}
					}
					if (keyPress != null)
					{
						keyPress.invoke($.with(v), keyCode, event);
						retVal = true;
					}
					
					return retVal;
				}
			});
		}
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
					swipeUp.invoke($.with(v));
				}
				else if (swipe != null)
				{
					swipe.invoke($.with(v), SwipeDetector.Direction.UP);
				}
			}

			@Override
			public void onRightSwipe(View v) {
				if (swipeRight != null)
				{
					swipeRight.invoke($.with(v));
				}
				else if (swipe != null)
				{
					swipe.invoke($.with(v), SwipeDetector.Direction.RIGHT);
				}
			}

			@Override
			public void onLeftSwipe(View v) {
				if (swipeLeft != null)
				{
					swipeLeft.invoke($.with(v));
				}
				else if (swipe != null)
				{
					swipe.invoke($.with(v), SwipeDetector.Direction.LEFT);
				}
			}

			@Override
			public void onDownSwipe(View v) {
				if (swipeDown != null)
				{
					swipeDown.invoke($.with(v));
				}
				else if (swipe != null)
				{
					swipe.invoke($.with(v), SwipeDetector.Direction.DOWN);
				}
			}

			@Override
			public void onStartSwipe(View v) {
				if (swipe != null)
				{
					swipe.invoke($.with(v), SwipeDetector.Direction.START);
				}
			}

			@Override
			public void onStopSwipe(View v) {
				if (swipe != null)
				{
					swipe.invoke($.with(v), SwipeDetector.Direction.STOP);
				}
			}
			
		});
		
		for (View view : views)
		{
			view.setOnTouchListener(new View.OnTouchListener() {
				
				@Override
				public boolean onTouch(View v, MotionEvent e) {
					return swiper.onTouch(v, e);
				}
			});
		}
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
	 * Convenience method for initializing a droidQuery Object. For example:
	 * <pre>
	 *	List&lt;View&gt; list = new ArrayList&lt;View&gt;();
	 *	//manipute the list
	 *	$.with(list);
	 * </pre>
	 * @param views
	 * @return a new droidQuery instance
	 */
	public static $ with(List<View> views)
	{
		return new $(views);
	}
	
	/**
	 * Used to privately and safely allow the creation of droidQuery instances using with empty lists
	 * @param context the current context
	 * @param views the selection
	 * @return a new instance of droidQuery with the selection set to the given views
	 */
	private static $ with(Context context, List<View> views)
	{
		return new $(context, views);
	}
	
	/**
	 * Convenience method for initializing a droidQuery Object. For example:
	 * <pre>
	 *	View[] views = new View[10];
	 *	//manipute the array
	 *	$.with(views).attr("alpha", 0.5);
	 * </pre>
	 * or
	 * <pre>
	 * TextView tv = (TextView) findViewById(R.id.textview);
	 * ImageView img = (ImageView) findViewById(R.id.img);
	 * $.with(tv, img).attr("alpha", 0.5);
	 * </pre>
	 * @param views
	 * @return a new droidQuery instance
	 */
	public static $ with(View... views)
	{
		return new $(views);
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
	 * 
	 * @param context
	 * @param ids
	 * @return
	 */
	public static $ with(Context context, int... ids)
	{
		return $.with(context).ids(ids);
	}
	
	/**
	 * Selects the subviews specified by the given resource {@code ids}.
	 * @param view
	 * @param ids
	 * @return
	 */
	public static $ with(View view, int... ids)
	{
		return $.with(view).ids(ids);
	}
	
	/**
	 * Initiate new droidQuery with the given selector
	 * @param context
	 * @param selector
	 * @return
	 * @see $#$(Context, String)
	 */
	public static $ with(Context context, String selector)
	{
		return new $(context, selector);
	}
	
	/**
	 * Initiate new droidQuery with the given selector
	 * @param parent view scope
	 * @param selector
	 * @return
	 * @see $#$(Context, String)
	 */
	public static $ with(View parent, String selector)
	{
		return new $(parent, selector);
	}
	
	/**
	 * Create a copy of a droidQuery object that contains the same {@link #data()} and selection.
	 * @param droidQuery
	 * @return
	 */
	public static $ with($ droidQuery)
	{
		return new $(droidQuery);
	}
	
	/**
	 * Finds a view that is identified by the given id
	 * @param id
	 * @return the found view, or {@code null} if it was not found.
	 */
	private View findViewById(int id)
	{
		//first check within the current selection
		for (View view : views)
		{
			if (view != null)
			{
				if (view.getId() == id) 
				{
					return view;
				}
			}
			
		}
		View v = null;
		
		//if not found, check the current scope (rootView)
		if (rootView != null)
			v = rootView.findViewById(id);
		
		//if not found, check the Activity's scope
		if (v == null && context instanceof Activity)
			v = ((Activity) context).findViewById(id);
		
		return v;
	}
	
	/** Sets the set of views to the parents of all currently-selected views */
	public $ parent()
	{
		List<View> _views = new ArrayList<View>();
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			ViewParent parent = view.getParent();
			if (parent != null && !_views.contains(parent) && parent instanceof View)
			{
				_views.add((View) parent);
			}
		}
		this.views.clear();
		this.views = _views;
		return this;
	}
	
	/** Sets the set of views to the children of the current set of views with the given child index */
	public $ child(int index)
	{
		List<View> _views = new ArrayList<View>();
		for (View view : views)
		{
			if (view instanceof ViewGroup)
			{
				View v = ((ViewGroup) view).getChildAt(index);
				if (v != null)
					_views.add(v);
			}
		}
		views.clear();
		views = _views;
		
		return this;
	}
	
	/**
	 * Animates the selected views using the JSON properties, the given duration, the easing function,
	 * and with the onComplete callback
	 * @param properties JSON String of an {@link AnimationOptions} Object
	 * @param duration the duration of the animation, in milliseconds
	 * @param easing the Easing function to use
	 * @param complete the Function to invoke once the animation has completed for all views
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
	 * Animate the current views. Example:
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
	 *                        						public void invoke($ droidQuery, Object... args) {
	 *                        							droidQuery.alert("Animation Complete!");
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
	 * Animate the currently selected views. For example:
	 * <pre>
	 * $.with(myView).animate(new QuickMap(QuickEntry.qe("alpha", .8f), QuickEntry.qe("width", 50%)), 400, Easing.LINEAR, null);
	 * </pre>
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
	 * This reusable chunk of code can set up the given animation using the given animation options
	 * @param options the options used to manipulate how the animation behaves
	 * @return the container for placing views that will be animated using the given options
	 */
	private AnimatorSet animationWithOptions(final AnimationOptions options, List<Animator> animators)
	{
		AnimatorSet animation = new AnimatorSet();
		animation.playTogether(animators);
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
		Interpolator interpolator = null;
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
		
		return animation;
	}
	
	/**
	 * Interprets the CSS-style String and sets the value
	 * @param view the view that will change.
	 * @param key the name of the attribute
	 * @param _value the end animation value
	 * @return the computed value
	 */
	public Number getAnimationValue(View view, String key, String _value)
	{
		Number value = null;
		
		boolean negativeValue = false;
		if (_value.startsWith("-"))
		{
			negativeValue = true;
			_value = _value.substring(1);
		}
		
		String[] split = (_value).split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
		if (negativeValue)
			split[0] = String.format(Locale.US, "-%s", split[0]);
		if (split.length == 1)
		{
			if (split[0].contains("."))
			{
				value = Float.valueOf(split[0]);
			}
			else
			{
				value = Integer.valueOf(split[0]);
			}
		}
		else
		{
			if (split.length > 2)
			{
				Log.w("droidQuery", "parsererror for key " + key);
				return null;
			}
			if (split[1].equalsIgnoreCase("px"))
			{
				//this is the default. Just determine if float or int
				if (split[0].contains("."))
				{
					value = Float.valueOf(split[0]);
				}
				else
				{
					value = Integer.valueOf(split[0]);
				}
			}
			else if (split[1].equalsIgnoreCase("dip") || split[1].equalsIgnoreCase("dp"))
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
				ViewParent parent = view.getParent();
				float pixels = 0;
				if (parent == null || !(parent instanceof View))
				{
					pixels = context.getResources().getDisplayMetrics().widthPixels;
					//use best guess for width or height dpi
					if (split[0].equalsIgnoreCase("y") || split[0].equalsIgnoreCase("top") || split[0].equalsIgnoreCase("bottom"))
					{
						pixels = context.getResources().getDisplayMetrics().heightPixels;
					}
				}
				else
				{
					pixels = ((View) parent).getWidth();
					if (split[0].equalsIgnoreCase("y") || split[0].equalsIgnoreCase("top") || split[0].equalsIgnoreCase("bottom"))
					{
						pixels = ((View) parent).getHeight();
					}
				}
				float percent = 0;
				if (pixels != 0)
					percent = Float.valueOf(split[0])/100*pixels;
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
				return null;
			}
		}
		return value;
	}
	
	/**
	 * Animate multiple view properties at the same time. For example:
	 * <pre>
	 * $.with(myView).animate(new QuickMap(QuickEntry.qe("alpha", .8f), QuickEntry.qe("width", 50%)), new AnimationOptions());
	 * </pre>
	 * @param properties mapping of property names and final values to animate
	 * @param options the options for setting the duration, easing, etc of the animation
	 * @return this
	 */
	public $ animate(Map<String, Object> properties, final AnimationOptions options)
	{
		List<Animator> animations = new ArrayList<Animator>();
		for (Entry<String, Object> entry : properties.entrySet())
		{
			final String key = entry.getKey();
			//Java sometimes will interpret these Strings as Numbers, so some trickery is needed below
			Object value = entry.getValue();
			
			for (final View view : this.views)
			{
				ValueAnimator anim = null;
				if (value instanceof String)
					value = getAnimationValue(view, key, (String) value);
				if (value != null)
				{
					final ViewGroup.LayoutParams params = view.getLayoutParams();
					try {
						final Field field = params.getClass().getField(key);
						if (field != null)
						{
							if (value instanceof Integer || value.getClass() == int.class)
								anim = ValueAnimator.ofInt((Integer) field.get(params), (Integer) value);
							else if (value instanceof Float || value.getClass() == float.class)
								anim = ValueAnimator.ofFloat((Float) field.get(params), (Float) value);
							else if (value instanceof Long || value.getClass() == long.class)
								anim = ValueAnimator.ofFloat((Float) field.get(params), new Float((Long) value));
							else if (value instanceof Double || value.getClass() == double.class)
								anim = ValueAnimator.ofFloat((Float) field.get(params), new Float((Double) value));
							anim.addUpdateListener(new AnimatorUpdateListener(){

								@Override
								public void onAnimationUpdate(ValueAnimator animation) {
									try {
										field.set(params, animation.getAnimatedValue());
										view.requestLayout();
									} catch (Throwable t)
									{
										if (options.debug())
											t.printStackTrace();
									}
									if (options.progress() != null)
									{
										options.progress().invoke($.with(view), key, animation.getAnimatedValue(), animation.getDuration() - animation.getCurrentPlayTime());
									}
									
								}
								
							});
						}
						
					} catch (Throwable t) {
						
						if (options.debug())
							Log.w("$", String.format(Locale.US, "%s is not a LayoutParams attribute.", key));
					}
					
					if (anim == null)
					{
						if (value instanceof Integer || value.getClass() == int.class)
							anim = ObjectAnimator.ofInt(view, key, (Integer) value);
						else if (value instanceof Float || value.getClass() == float.class)
							anim = ObjectAnimator.ofFloat(view, key, (Float) value);
						else if (value instanceof Long || value.getClass() == long.class)
							anim = ObjectAnimator.ofFloat(view, key, new Float((Long) value));
						else if (value instanceof Double || value.getClass() == double.class)
							anim = ObjectAnimator.ofFloat(view, key, new Float((Double) value));
						
						if (options.progress() != null)
						{
							anim.addUpdateListener(new AnimatorUpdateListener(){

								@Override
								public void onAnimationUpdate(ValueAnimator animation) {
									options.progress().invoke($.with(view), key, animation.getAnimatedValue(), animation.getDuration() - animation.getCurrentPlayTime());
								}
								
							});
						}
					}
					

					anim.setRepeatCount(options.repeatCount());
					if (options.reverse())
						anim.setRepeatMode(ValueAnimator.REVERSE);
					animations.add(anim);
				}
				
			}
		}
		AnimatorSet animation = animationWithOptions(options, animations);
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
	 * Shortcut method for animating the alpha attribute of the selected views to 1.0.
	 * @param duration the length of time the animation should last
	 * @param complete the function to call when the animation has completed
	 */
	public void fadeIn(long duration, final Function complete)
	{
		AnimatorSet anim = new AnimatorSet();
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
		AnimatorSet.Builder builder = null;
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1.0f);
			if (builder == null) 
			{
				builder = anim.play(animator);
			}
			else
			{
				builder.with(animator);
			}
		}
		anim.start();
	}
	
	/**
	 * Shortcut method for animating the alpha attribute of the selected views to 0.0.
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
		AnimatorSet anim = new AnimatorSet();
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
		AnimatorSet.Builder builder = null;
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0.0f);
			if (builder == null) 
			{
				builder = anim.play(animator);
			}
			else
			{
				builder.with(animator);
			}
		}
		anim.start();
	}
	
	/**
	 * Shortcut method for animating the alpha attribute of the selected views to the given value.
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
		AnimatorSet anim = new AnimatorSet();
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
		AnimatorSet.Builder builder = null;
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", opacity);
			if (builder == null) 
			{
				builder = anim.play(animator);
			}
			else
			{
				builder.with(animator);
			}
		}
		anim.start();
	}
	
	/**
	 * For each selected view, if its alpha is less than 0.5, it will fade in. Otherwise, it will
	 * fade out.
	 * @param duration the length of time the animation should last
	 * @param complete the function to call when the animation has completed
	 */
	public void fadeToggle(long duration, final Function complete)
	{
		List<View> zeros = new ArrayList<View>();
		List<View> ones = new ArrayList<View>();
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			if (ViewHelper.getAlpha(view) < 0.5)
				zeros.add(view);
			else
				ones.add(view);
		}
		$.with(context, zeros).fadeIn(duration, complete);
		$.with(context, ones).fadeOut(duration, complete);
	}
	
	/**
	 * If this {@link #view} has an alpha of less than 0.5, it will fade in. Otherwise, it will
	 * fade out.
	 * @param options use to modify the behavior of the animation
	 */
	public void fadeToggle(AnimationOptions options)
	{
		List<View> zeros = new ArrayList<View>();
		List<View> ones = new ArrayList<View>();
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			if (ViewHelper.getAlpha(view) < 0.5)
				zeros.add(view);
			else
				ones.add(view);
		}
		$.with(context, zeros).fadeIn(options);
		$.with(context, ones).fadeOut(options);
	}
	
	/**
	 * Animates the selected views out of their parent views by sliding it down, past its bottom
	 * @param duration the length of time the animation should last
	 * @param complete the function to call when the animation has completed
	 */
	public void slideDown(long duration, final Function complete)
	{
		AnimatorSet anim = new AnimatorSet();
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
		AnimatorSet.Builder builder = null;
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
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
			ObjectAnimator animator = ObjectAnimator.ofFloat(view, "y", y);
			if (builder == null)
				builder = anim.play(animator);
			else
				builder.with(animator);
		}
		anim.setDuration(duration);
		anim.start();
	}
	
	/**
	 * Animates the selected views out of its parent by sliding it down, past its bottom
	 * @param options use to modify the behavior of the animation
	 */
	public void slideDown(final AnimationOptions options)
	{
		List<Animator> animations = new ArrayList<Animator>();
		for (final View view : this.views)
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
			ObjectAnimator anim = ObjectAnimator.ofFloat(view, "y", new Float(y));
			if (options.progress() != null)
			{
				anim.addUpdateListener(new AnimatorUpdateListener(){

					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						options.progress().invoke($.with(view), "y", animation.getAnimatedValue(), animation.getDuration() - animation.getCurrentPlayTime());
					}
					
				});
			}
			animations.add(anim);
		}
		
		AnimatorSet animation = animationWithOptions(options, animations);
		animation.start();
		
	}
	
	/**
	 * Animates the selected views out of its parent by sliding it up, past its top
	 * @param duration the length of time the animation should last
	 * @param complete the function to call when the animation has completed
	 */
	public void slideUp(long duration, final Function complete)
	{
		AnimatorSet anim = new AnimatorSet();
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
		AnimatorSet.Builder builder = null;
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			
			ObjectAnimator animator = ObjectAnimator.ofFloat(view, "y", 0);
			if (builder == null)
				builder = anim.play(animator);
			else
				builder.with(animator);
		}
		anim.setDuration(duration);
		anim.start();
	}
	
	/**
	 * Animates the selected views out of its parent by sliding it up, past its top
	 * @param options use to modify the behavior of the animation
	 */
	public void slideUp(final AnimationOptions options)
	{		
		List<Animator> animations = new ArrayList<Animator>();
		for (final View view : this.views)
		{
			ObjectAnimator anim = ObjectAnimator.ofFloat(view, "y", new Float(0));
			if (options.progress() != null)
			{
				anim.addUpdateListener(new AnimatorUpdateListener(){

					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						options.progress().invoke($.with(view), "y", animation.getAnimatedValue(), animation.getDuration() - animation.getCurrentPlayTime());
					}
					
				});
			}
			animations.add(anim);
		}
		
		AnimatorSet animation = animationWithOptions(options, animations);
		animation.start();
	}
	
	/**
	 * Animates the selected views out of its parent by sliding it right, past its edge
	 * @param duration the length of time the animation should last
	 * @param complete the function to call when the animation has completed
	 */
	public void slideRight(long duration, final Function complete)
	{
		AnimatorSet anim = new AnimatorSet();
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
		AnimatorSet.Builder builder = null;
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
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
			ObjectAnimator animator = ObjectAnimator.ofFloat(view, "x", x);
			if (builder == null)
				builder = anim.play(animator);
			else
				builder.with(animator);
		}
		anim.setDuration(duration);
		anim.start();
	}
	
	/**
	 * Animates the selected views out of its parent by sliding it right, past its edge
	 * @param options use to modify the behavior of the animation
	 */
	public void slideRight(final AnimationOptions options)
	{		
		List<Animator> animations = new ArrayList<Animator>();
		for (final View view : this.views)
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
			ObjectAnimator anim = ObjectAnimator.ofFloat(view, "x", x);
			if (options.progress() != null)
			{
				anim.addUpdateListener(new AnimatorUpdateListener(){

					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						options.progress().invoke($.with(view), "x", animation.getAnimatedValue(), animation.getDuration() - animation.getCurrentPlayTime());
					}
					
				});
			}
			animations.add(anim);
		}
		
		AnimatorSet animation = animationWithOptions(options, animations);
		animation.start();
	}
	
	/**
	 * Animates the selected views out of its parent by sliding it left, past its edge
	 * @param duration the length of time the animation should last
	 * @param complete the function to call when the animation has completed
	 */
	public void slideLeft(long duration, final Function complete)
	{
		AnimatorSet anim = new AnimatorSet();
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
		AnimatorSet.Builder builder = null;
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			ObjectAnimator animator = ObjectAnimator.ofFloat(view, "x", 0);
			if (builder == null)
				builder = anim.play(animator);
			else
				builder.with(animator);
		}
		anim.setDuration(duration);
		anim.start();
	}
	
	/**
	 * Animates the selected views out of its parent by sliding it left, past its edge
	 * @param options use to modify the behavior of the animation
	 */
	public void slideLeft(final AnimationOptions options)
	{
		List<Animator> animations = new ArrayList<Animator>();
		for (final View view : this.views)
		{
			ObjectAnimator anim = ObjectAnimator.ofFloat(view, "x", 0);
			if (options.progress() != null)
			{
				anim.addUpdateListener(new AnimatorUpdateListener(){

					@Override
					public void onAnimationUpdate(ValueAnimator animation) {
						options.progress().invoke($.with(view), "x", animation.getAnimatedValue(), animation.getDuration() - animation.getCurrentPlayTime());
					}
					
				});
			}
			animations.add(anim);
		}
		
		AnimatorSet animation = animationWithOptions(options, animations);
		animation.start();
	}
	
	/**
	 * Gets the value for the given attribute of the first view in the current selection. 
	 * This is done using reflection, and as such
	 * expects a <em>get-</em> or <em>is-</em> prefixed method name for the view.
	 * @param s the name of the attribute to retrieve
	 * @return the value of the given attribute name on the first view in the current selection
	 */
	public Object attr(String s)
	{
		try
		{
			Method m = view(0).getClass().getMethod("get" + capitalize(s));
			return m.invoke(view(0));
		}
		catch (Throwable t)
		{
			try
			{
				Method m = view(0).getClass().getMethod("is" + capitalize(s));
				return m.invoke(view(0));
			}
			catch (Throwable t2)
			{
				//try using NineOldAndroids
				try {
					Method m = ViewHelper.class.getMethod("get" + capitalize(s), new Class<?>[]{View.class});
					return m.invoke(null, view(0));
				}
				catch (Throwable t3) {
					Log.w("droidQuery", view(0).getClass().getSimpleName() + "has no getter method for the variable " + s + ".");
				}
				
				return null;
			}
		}
	}
	
	/**
	 * Sets the value of the given attribute on each view in the current selection. This is done 
	 * using reflection, and as such a <em>set-</em>prefixed method name for each view.
	 * @param s the name of the attribute to set
	 * @param o the value to set to the given attribute
	 * @return this
	 * TODO add additional support for common CSS syntax
	 */
	public $ attr(String s, Object o)
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			try
			{
				Class<?> objClass = o.getClass();
				Class<?> simpleClass = PRIMITIVE_TYPE_MAP.get(objClass);
				if (simpleClass != null)
				{
					objClass = simpleClass;
				}
				try {
					Method m = view.getClass().getMethod("set" + capitalize(s), new Class<?>[]{objClass});
					m.invoke(view, o);
				}
				catch (Throwable t) {
					//try using NineOldAndroids
					try {
						Method m = ViewHelper.class.getMethod("set" + capitalize(s), new Class<?>[]{View.class, objClass});
						m.invoke(null, view(0), o);
					}
					catch (Throwable t2)
					{
						Log.w("droidQuery", view.getClass().getSimpleName() + ".set" + capitalize(s) + "(" + o.getClass().getSimpleName() + ") is not a method!");
					}
				}
				
			}
			catch (Throwable t)
			{
				Log.w("droidQuery", view.getClass().getSimpleName() + ".set" + capitalize(s) + "(" + o.getClass().getSimpleName() + ") is not a method!");
			}
		}
		return this;
	}
	
	/**
	 * @return the view at the given index of the current selection
	 */
	public View view(int index)
	{
		return this.views.get(index);
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
	 * Adds a subview to the first view in the selection
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
		if (view(0) instanceof ViewGroup)
		{
			((ViewGroup) view(0)).addView(v);
		}
		return this;
	}
	
	/**
	 * Adds a subview to the first view in the current selection
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
		if (view(0) instanceof ViewGroup)
		{
			((ViewGroup) view(0)).addView(v);
		}
		return this;
	}
	
	/* TODO include these CSS methods once css parsing and handling is complete! */
//	
//	/**
//	 * Get a key-value mapping of the css values for the given property names, for the first selected view.
//	 * @param propertyNames
//	 * @return
//	 */
//	public Map<String, Object> css(String[] propertyNames)
//	{
//		//TODO
//	}
//	
//	/**
//	 * Set a CSS property for the set of matched elements
//	 * @param propertyName
//	 * @param value
//	 * @return
//	 */
//	public $ css(String propertyName, Object value)
//	{
//		//TODO
//	}
//	
//	/**
//	 * Handle getting/setting each CSS property
//	 * @param propertyName the name of the property
//	 * @param function includes a droidQuery selected with each element, and args that include the index and the property's value
//	 * @return
//	 */
//	public $ css(String propertyName, Function function)
//	{
//		//TODO
//	}
//	
//	/**
//	 * Set the css for the selected views
//	 * @param properties json object as string
//	 */
//	public $ css(String properties)
//	{
//		try {
//			StyleSheet.fromString(properties).applyRules(this);
//		} catch (Exception e) {
//			Log.w("droidQuery", "Could not load css", e);
//		}
//		return this;
//	}
//	
//	/**
//	 * Applies the CSS-style rules to the selection. 
//	 * @param css
//	 * @return
//	 */
//	public $ css(JSONObject properties)
//	{
//		return css(properties.toString());
//	}
	
	/**
	 * Removes a subview from the first view in the current selection
	 * @param v the subview to remove
	 * @return this
	 */
	public $ remove(View v)
	{
		if (view(0) instanceof ViewGroup)
		{
			((ViewGroup) view(0)).removeView(v);
		}
		return null;
	}
	
	/**
	 * Removes a subview from the first view in the current selection
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
		if (view(0) instanceof ViewGroup)
		{
			((ViewGroup) view(0)).removeView(v);
		}
		return this;
	}
	
	/**
	 * Sets the visibility of the current selection to {@link View#VISIBLE}
	 * @return this
	 */
	public $ hide()
	{
		for (View view : views)
		{
			view.setVisibility(View.VISIBLE);
		}
		return this;
	}
	
	/**
	 * Sets the visibility of this {@link #view} to {@link View#INVISIBLE}
	 * @return this
	 */
	public $ show()
	{
		for (View view : views)
		{
			view.setVisibility(View.INVISIBLE);
		}
		return this;
	}
	
	///Event Handler Attachment
	
	/**
	 * Binds the views in the current selection to the event. For example:
	 * <pre>
	 * $.with(myView).bind("click", "Hello World!", new Function() {
	 * 	public void invoke($ droidQuery, Object... args) {
	 * 		Object data = args[0];
	 * 		droidQuery.alert((String) data);
	 * 	}
	 * });
	 * </pre>
	 * @note that for events with multiple words, all words except the first are required to be 
	 * capitalized. For example, to bind to a long-click event, both of the following are acceptable:
	 * <pre>
	 * $.with(myView).bind("longClick", "Hello World!", new Function() {
	 * 	public void invoke($ droidQuery, Object... args) {
	 * 		Object data = args[0];
	 * 		droidQuery.alert((String) data);
	 * 	}
	 * });
	 * 
	 * $.with(myView).bind("LongClick", "Hello World!", new Function() {
	 * 	public void invoke($ droidQuery, Object... args) {
	 * 		Object data = args[0];
	 * 		droidQuery.alert((String) data);
	 * 	}
	 * });
	 * </pre>
	 * However, this will fail:
	 * <pre>
	 * $.with(myView).bind("longclick", "Hello World!", new Function() {
	 * 	public void invoke($ droidQuery, Object... args) {
	 * 		Object data = args[0];
	 * 		droidQuery.alert((String) data);
	 * 	}
	 * });
	 * </pre>
	 * @param eventType should be the verb in OnVerbListener
	 * @param data an Object passed to {@code handler} when the event is triggered.
	 * @param handler receives two arguments: a droidQuery with the affected view selected, and the {@code data} parameter
	 * @return the current instance of {@code droidQuery}
	 * @see #on(String, Function)
	 * @see #one(String, Function)
	 * @see #unbind(String)
	 */
	public $ bind(String eventType, Object data, Function handler)
	{
		String method = String.format(Locale.US, "setOn%sListener", capitalize(eventType));
		String listener = String.format(Locale.US, "On%sListener", capitalize(eventType));
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			Class<?>[] classes = view.getClass().getClasses();
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
				Method setEventListener = view.getClass().getMethod(method, new Class<?>[]{eventInterface});
				EventHandlerCreator proxy = new EventHandlerCreator($.with(view), handler, data);
				Object eventHandler = Proxy.newProxyInstance(eventInterface.getClassLoader(), new Class<?>[]{eventInterface}, proxy);
				setEventListener.invoke(view, eventInterface.cast(eventHandler));
				
			}
			catch (Throwable t)
			{
				Log.w("droidQuery", String.format(Locale.US, "Could not bind to event %s.\n%s", eventType, t.getMessage()));
			}
		}
		
		return this;
	}
	
	/**
	 * Binds the views in the current selection to the event. For example:
	 * <pre>
	 * $.with(myView).on("click", new Function() {
	 * 	public void invoke($ droidQuery, Object... args) {
	 * 		droidQuery.alert("View Clicked!");
	 * 	}
	 * });
	 * </pre>
	 * @note that for events with multiple words, all words except the first are required to be 
	 * capitalized. For example, to bind to a long-click event, both of the following are acceptable:
	 * <pre>
	 * $.with(myView).on("longClick", new Function() {
	 * 	public void invoke($droidQuery, Object... args) {
	 * 		droidQuery.alert("View LongClicked!");
	 * 	}
	 * });
	 * 
	 * $.with(myView).on("LongClick", new Function() {
	 * 	public void invoke($ droidQuery, Object... args) {
	 * 		droidQuery.alert("View LongClicked!");
	 * 	}
	 * });
	 * </pre>
	 * However, this will fail:
	 * <pre>
	 * $.with(myView).on("longclick", new Function() {
	 * 	public void invoke($ droidQuery, Object... args) {
	 * 		droidQuery.alert("View LongClicked!");
	 * 	}
	 * });
	 * </pre>
	 * @param event should be the verb in OnVerbListener
	 * @param handler receives one argument: a droidQuery with the affected view selected
	 * @return the current instance of {@code droidQuery}
	 * @see #bind(String, Object, Function)
	 * @see #one(String, Function)
	 * @see #unbind(String)
	 */
	public $ on(String event, Function handler)
	{
		String method = String.format(Locale.US, "setOn%sListener", capitalize(event));
		String listener = String.format(Locale.US, "On%sListener", capitalize(event));
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			Class<?>[] classes = view.getClass().getClasses();
			
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

				Method setEventListener = view.getClass().getMethod(method, new Class<?>[]{eventInterface});
				EventHandlerCreator proxy = new EventHandlerCreator($.with(view), handler);
				Object eventHandler = Proxy.newProxyInstance(eventInterface.getClassLoader(), new Class<?>[]{eventInterface}, proxy);
				setEventListener.invoke(view, eventInterface.cast(eventHandler));
			}
			catch (Throwable t)
			{
				Log.w("droidQuery", String.format(Locale.US, "Could not bind to event %s.", event));
			}
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
			public void invoke($ droidQuery, Object... params) {
				handler.invoke(droidQuery);
				droidQuery.unbind(event);
			}
			
		};
		
		return on(event, function);
	}
	
	//these listenTo methods expose the EventCenter, and provide a behavior similar to that provided
	//by backbone.js
	
	/**
	 * Listen for all events triggered using the {@link #notify(String)} or {@link #notify(String, Map)}
	 * method
	 * @param event the name of the event
	 * @param callback the function to call when the event is triggered.
	 * @see #listenToOnce(String, Function)
	 */
	public static void listenTo(String event, Function callback)
	{
		EventCenter.bind(event, callback, null);
	}
	
	/**
	 * Listen for the next event triggered using the {@link #notify(String)} or 
	 * {@link #notify(String, Map)} method
	 * @param event the name of the event
	 * @param callback the function to call when the event is triggered.
	 * @see #listenTo(String, Function)
	 */
	public static void listenToOnce(final String event, final Function callback)
	{
		EventCenter.bind(event, new Function() {

			@Override
			public void invoke($ droidQuery, Object... params) {
				callback.invoke(droidQuery, params);
				EventCenter.unbind(event, this, null);
			}
			
		}, null);
	}
	
	/**
	 * Stop listening for events triggered using the {@link #notify(String)} and
	 * {@link #notify(String, Map)} methods
	 * @param event the name of the event
	 * @param callback the function to no longer call when the event is triggered.
	 * @see #listenTo(String, Function)
	 */
	public static void stopListening(String event, Function callback )
	{
		EventCenter.unbind(event, callback, null);
	}
	
	/**
	 * Trigger a notification for functions registered to the given event String
	 * @param event the event string to which registered listeners will respond
	 * @see #listenTo(String, Function)
	 * @see #listenToOnce(String, Function)
	 */
	public $ notify(String event)
	{
		EventCenter.trigger(this, event, null, null);
		return this;
	}
	
	/**
	 * Trigger a notification for functions registered to the given event String
	 * @param event the event string to which registered listeners will respond
	 * @param data Object passed to the notified functions
	 * @see #listenTo(String, Function)
	 * @see #listenToOnce(String, Function)
	 */
	public $ notify(String event, Map<String, Object> data)
	{
		EventCenter.trigger(this, event, data, null);
		return this;
	}
	
	/**
	 * Registers change listeners for TextViews, EditTexts, and CompoundButtons. For all other
	 * view types, this will trigger a function when the view's layout has been changed.
	 * @param function the Function to call when the change event occurs. This will receive a
	 * droidQuery instance for the changed view
	 * @return this
	 */
	public $ change(final Function function)
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			final int index = i;
			if (view instanceof TextView)
			{
				((TextView) view).addTextChangedListener(new TextWatcher(){

					@Override
					public void afterTextChanged(Editable arg0) {
						function.invoke($.with($.this.views.get(index)));
					}

					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {}
					
				});
			}
			else if (view instanceof EditText)
			{//this is overkill, but what the hey
				((EditText) view).addTextChangedListener(new TextWatcher(){

					@Override
					public void afterTextChanged(Editable arg0) {
						function.invoke($.with($.this.views.get(index)));
					}

					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {}
					
				});
			}
			else if (view instanceof CompoundButton)
			{
				((CompoundButton) view).setOnCheckedChangeListener(new OnCheckedChangeListener(){

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						function.invoke($.with($.this.views.get(index)));
					}
					
				});
			}
			else if (android.os.Build.VERSION.SDK_INT >= 11)
			{
				//default to size for API 11+
				try {
					Class<?> eventInterface = Class.forName("android.view.View.OnLayoutChangeListener");
					Method addOnLayoutChangeListener = view.getClass().getMethod("addOnLayoutChangeListener", new Class<?>[]{eventInterface});
					InvocationHandler proxy = new InvocationHandler() {

						@Override
						public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
							function.invoke($.with($.this.views.get(index)));
							return null;
						}
						
					};
					Object eventHandler = Proxy.newProxyInstance(eventInterface.getClassLoader(), new Class<?>[]{eventInterface}, proxy);
					addOnLayoutChangeListener.invoke(view, eventInterface.cast(eventHandler));
				}
				catch (Throwable t)
				{
					//unknown error
				}
			}
		}
		
		return this;
	}
	
	/**
	 * Get the value associated with the first view in the current selection. If the view is a 
	 * TextView, this method returns the CharSequence text. If it is a Button, the boolean checked 
	 * state is returned. If it is an ImageView, the Drawable is returned.
	 * @return the value of this view, or <em>null</em> if not applicable.
	 */
	public Object val()
	{
		if (view(0) instanceof TextView)
		{
			return ((TextView) view(0)).getText();
		}
		else if (view(0) instanceof CompoundButton)
		{
			return ((CompoundButton) view(0)).isChecked();
		}
		else if (view(0) instanceof ImageView)
		{
			return ((ImageView) view(0)).getDrawable();
		}
		return null;
	}
	
	/**
	 * Set the value associated with the views in the current selection. If the view is a TextView, 
	 * this method sets the CharSequence text. If it is a Button, the boolean checked state is set. 
	 * If it is an ImageView, the Drawable or Bitmap is set. All other view types are ignored.
	 * @return this
	 */
	public $ val(Object object)
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			if (view instanceof TextView && object instanceof CharSequence)
			{
				((TextView) view).setText((CharSequence) object);
			}
			else if (view instanceof CompoundButton && object instanceof Boolean)
			{
				((CompoundButton) view).setChecked((Boolean) object);
			}
			else if (view instanceof ImageView)
			{
				if (object instanceof Bitmap)
				{
					((ImageView) view).setImageBitmap((Bitmap) object);
				}
				else if (object instanceof Drawable)
				{
					((ImageView) view).setImageDrawable((Drawable) object);
				}
				
			}
		}
		
		return this;
	}
	
	/**
	 * Triggers a click event on the views in the current selection
	 * @return this
	 */
	public $ click()
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			view.performClick();
		}
		return this;
	}
	
	/**
	 * Invokes the given Function every time each view in the current selection is clicked. The only 
	 * parameter passed to the given function is a droidQuery instance containing the clicked view
	 * @param function the function to call when this view is clicked
	 * @return this
	 */
	public $ click(final Function function)
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			view.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					function.invoke($.with(v));
				}
				
			});
		}
		return this;
	}
	
	/**
	 * Invokes the given Function for click events on each view in the current selection. 
	 * The function will receive two arguments:
	 * <ol>
	 * <li>a droidQuery containing the clicked view
	 * <li>{@code eventData}
	 * </ol>
	 * @param eventData the second argument to pass to the {@code function}
	 * @param function the function to invoke
	 * @return
	 */
	public $ click(final Object eventData, final Function function)
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			view.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					function.invoke($.with(v), eventData);
				}
				
			});
		}
		return this;
	}
	
	/**
	 * Triggers a long-click event on this each view in the current selection
	 * @return this
	 */
	public $ longclick()
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			view.performLongClick();
		}
		return this;
	}
	
	/**
	 * Invokes the given Function every time each view in the current selection is long-clicked. 
	 * The only parameter passed to the given function a droidQuery instance with the long-clicked view.
	 * @param function the function to call when this view is long-clicked
	 * @return this
	 */
	public $ longclick(final Function function)
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			view.setOnLongClickListener(new View.OnLongClickListener(){

				@Override
				public boolean onLongClick(View v) {
					function.invoke($.with(v));
					return true;
				}
				
			});
		}
		return this;
	}
	
	/**
	 * Invokes the given Function for long-click events on the views in the current selection. 
	 * The function will receive two arguments:
	 * <ol>
	 * <li>a droidQuery containing the long-clicked view
	 * <li>{@code eventData}
	 * </ol>
	 * @param eventData the second argument to pass to the {@code function}
	 * @param function the function to invoke
	 * @return
	 */
	public $ longclick(final Object eventData, final Function function)
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			view.setOnLongClickListener(new View.OnLongClickListener(){

				@Override
				public boolean onLongClick(View v) {
					function.invoke($.with(v), eventData);
					return true;
				}
				
			});
		}
		return this;
	}
	
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
	 * Triggers a swipe-up event on the current selection
	 * @return this
	 */
	public $ swipeUp()
	{
		if (swiper != null)
			swiper.performSwipeUp();
		return this;
	}
	
	/**
	 * Triggers a swipe-down event on the current selection
	 * @return this
	 */
	public $ swipeDown()
	{
		if (swiper != null)
			swiper.performSwipeDown();
		return this;
	}
	
	/**
	 * Triggers a swipe-left event on the current selection
	 * @return this
	 */
	public $ swipeLeft()
	{
		if (swiper != null)
			swiper.performSwipeLeft();
		return this;
	}
	
	/**
	 * Triggers a swipe-right event on the current selection
	 * @return this
	 */
	public $ swipeRight()
	{
		if (swiper != null)
			swiper.performSwipeRight();
		return this;
	}
	
	/**
	 * Sets the function to call when a view in the current selection has gained focus. This function
	 * will receive an instance of droidQuery for this view as its only parameter
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
	 * Gives focus to the first focusable view in the current selection.
	 * @return this
	 */
	public $ focus()
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			if (view.requestFocus()) {
				break;
			}
		}
		return this;
	}
	
	/**
	 * Sets the function to call when this {@link #view} loses focus.
	 * @param function the function to invoke. Will receive a droidQuery instance containing
	 * the view as its only parameter
	 * @return this
	 */
	public $ focusout(Function function)
	{
		offFocus = function;
		setupFocusListener();
		return this;
	}
	
	/**
	 * Removes focus from all views in the current selection.
	 * @return this
	 */
	public $ focusout()
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			view.clearFocus();
		}
		return this;
	}
	
	/**
	 * Set the function to call when a key-down event has been detected on this view.
	 * @param function the Function to invoke. Receives a droidQuery containing the responding view
	 * and two variable arguments:
	 * <ol>
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
	 * @param function the Function to invoke. Receives a droidQuery containing the responding view
	 * and two variable arguments:
	 * <ol>
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
	 * @param function the Function to invoke. Receives a droidQuery containing the responding view
	 * and two variable arguments:
	 * <ol>
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
	 * For each subclass on an {@link AdapterView} in the current selection, {@code select(Function)}
	 * will register an {@link AdapterView.OnItemSelectedListener OnItemSelectedListener} to invoke
	 * the given function.
	 * @param function function to invoke. Receives a droidQuery with the {@code AdapterView} selected,
	 * and the view position (int) as the only varargs parameter.
	 * @return this
	 */
	public $ select(final Function function)
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			if (view instanceof AdapterView)
			{
				((AdapterView<?>) view).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						function.invoke($.with(view), position);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {}
				});
			}
		}
		
		return this;
	}
	
	/**
	 * For each subclass of an {@link AdapterView} in the current selection, this will set the item
	 * selection to the given position.
	 * @param index the index of the child view to select
	 * @return this
	 */
	public $ select(int index)
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			try
			{
				Method m = view.getClass().getMethod("setSelection", new Class<?>[]{Integer.class});
				m.invoke(view, index);
			}
			catch (Throwable t)
			{
				//not available
			}
		}
		return this;
	}
	
	/**
	 * Remove a previously-attached event handler from the views in the current selection. 
	 * This can remove events registered
	 * by {@link #bind(String, Object, Function)}, {@link #on(String, Function)}, {@link #click()}, 
	 * etc.- or directly on the view.
	 * @param eventType the name of the event to unbind
	 */
	public void unbind(String eventType)
	{
		String method = String.format(Locale.US, "setOn%sListener", capitalize(eventType));
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			String listener = String.format(Locale.US, "%s.On%sListener", view.getClass().getName(), capitalize(eventType));
			try
			{
				//dynamically create instance of the listener interface
				
				Class<?> eventInterface = Class.forName(listener);
				Method setEventListener = view.getClass().getMethod(method, new Class<?>[]{eventInterface});
				EventHandlerCreator proxy = new EventHandlerCreator($.with(view), $.noop());
				Object eventHandler = Proxy.newProxyInstance(eventInterface.getClassLoader(), new Class<?>[]{eventInterface}, proxy);
				setEventListener.invoke(view, eventInterface.cast(eventHandler));
				
			}
			catch (Throwable t)
			{
				Log.w("droidQuery", String.format(Locale.US, "Could not unbind from event %s.", eventType));
			}
		}
		
	}
	
	/////Miscellaneous
	
	/**
	 * Invokes the given function for each view in the current selection. Function receives a 
	 * droidQuery instance containing the single view, and an integer of the current index
	 * @param function the function to invoke
	 * @return this
	 */
	public $ each(Function function)
	{
		for (int i = 0; i < views.size(); i++)
		{
			function.invoke($.with(views.get(i)), i);
		}
		return this;
	}
	
	/**
	 * If the first view of the current selection is a subclass of {@link AdapterView}, this will loop through all the 
	 * adapter data and invoke the given function, passing the varargs:
	 * <ol>
	 * <li>the item from the adapter
	 * <li>the index
	 * </ol>
	 * Otherwise, if the first view in the current selection is a subclass of {@link ViewGroup}, {@code each} will
	 * loop through all the child views, and wrap each one in a droidQuery object. The invoked
	 * function will receive it, and an int for the index of the selected child view.
	 * @param function Function the function to invoke
	 * @return this
	 */
	public $ children(Function function)
	{
		if (view(0) instanceof AdapterView)
		{
			AdapterView<?> group = (AdapterView<?>) view(0);
			for (int i = 0; i < (group).getCount(); i++)
			{
				function.invoke($.this, group.getItemAtPosition(i), i);
			}
		}
		else if (view(0) instanceof ViewGroup)
		{
			ViewGroup group = (ViewGroup) view(0);
			for (int i = 0; i < group.getChildCount(); i++)
			{
				function.invoke($.with(group.getChildAt(i)), i);
			}
		}
		return this;
	}
	
	/**
	 * Loops through all the sibling views of the first view in the current selection, and wraps 
	 * each in a droidQuery object. When invoked, the given function will receive two parameters:
	 * <ol>
	 * <li>the droidQuery for the view
	 * <li>the child index of the sibling
	 * </ol>
	 * @param function receives the droidQuery for the view, and the index for arg1
	 */
	public $ siblings(Function function)
	{
		ViewParent parent = view(0).getParent();
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
	 * Gets all the views in the current selection after the given start index
	 * @param start the starting position of the views to pass to the new instance of droidQuery.
	 * @return a droidQuery object containing the views from {@code start} to the end of the list.
	 */
	public $ slice(int start)
	{
		return $.with(context, this.views.subList(start, this.views.size()));
	}
	
	/**
	 * Gets all the views in the current selection after the given start index and before the given
	 * end index
	 * @param start the starting position of the views to pass to the new instance of droidQuery.
	 * @return a droidQuery object containing the views from {@code start} to {@code end}.
	 */
	public $ slice(int start, int end)
	{
		return $.with(context, this.views.subList(start, end));
	}
	
	/** @return the number of views that are currently selected */
	public int length()
	{
		return this.views.size();
	}
	
	/** @return the number of views that are currently selected */
	public int size()
	{
		return length();
	}
	
	/**
	 * Checks to see if the first view in the current selection is a subclass of the given class name
	 * @param className the name of the superclass to check
	 * @return {@code true} if the view is a subclass of the given class name. 
	 * Otherwise, {@code false}.
	 */
	public boolean is(String className)
	{
		try
		{
			Class<?> clazz = Class.forName(className);
			if (clazz.isInstance(view(0)))
				return true;
			return false;
		}
		catch (Throwable t)
		{
			return false;
		}
	}
	
	/**
	 * Checks to see if the given Object is a subclass of the given class name
	 * @param obj the Object to check
	 * @param className the name of the superclass to check
	 * @return {@code true} if the view is a subclass of the given class name. 
	 * Otherwise, {@code false}.
	 */
	public static boolean is(Object obj, String className)
	{
		try
		{
			Class<?> clazz = Class.forName(className);
			if (clazz.isInstance(obj))
				return true;
			return false;
		}
		catch (Throwable t)
		{
			return false;
		}
	}
	
	/**
	 * Removes each view in the current selection from the layout
	 */
	public void remove()
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			ViewParent parent = view.getParent();
			if (parent != null && parent instanceof ViewGroup)
			{
				((ViewGroup) parent).removeView(view);
			}
		}
		
	}
	
	/////Selectors
	
	/**
	 * Recursively selects all subviews of the given view
	 * @param v the parent view of all the suclasses to select
	 * @return a list of all views that are subviews in the 
	 * view hierarchy with the given view as the root
	 */
	private List<View> recursivelySelectAllSubViews(View v)
	{
		List<View> list = new ArrayList<View>();
		if (v instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				list.addAll(recursivelySelectAllSubViews(((ViewGroup) v).getChildAt(i)));
			}
		}
		list.add(v);
		return list;
	}
	
	/**
	 * Recursively selects all subviews of the given view that are subclasses of the given Object type
	 * @param v the parent view of all the suclasses to select
	 * @return a list of all views that are subviews in the 
	 * view hierarchy with the given view as the root
	 */
	private List<View> recursivelySelectByType(View v, Class<?> clazz)
	{
		List<View> list = new ArrayList<View>();
		if (v instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				list.addAll(recursivelySelectByType(((ViewGroup) v).getChildAt(i), clazz));
			}
		}
		if (clazz.isInstance(v))
			list.add(v);
		return list;
	}
	
	/**
	 * Select all subviews of the currently-selected views
	 * @return a droidQuery Object with all the views
	 */
	public $ selectAll()
	{
		List<View> subviews = new ArrayList<View>();
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			subviews.addAll(recursivelySelectAllSubViews(view));
		}
		return $.with(context, subviews);
	}
	
	/**
	 * Select all subviews of the currently-selected views that are subclasses of the given {@code className}. 
	 * @param className
	 * @return all the selected views in a droidQuery wrapper
	 */
	public $ selectByType(String className)
	{
		Class<?> clazz = null;
		try
		{
			clazz = Class.forName(className);
		}
		catch (Throwable t)
		{
			try
			{
				clazz = Class.forName(String.format(Locale.US, "android.widget.%s", className));
			}
			catch (Throwable t2)
			{
				try
				{
					clazz = Class.forName(String.format(Locale.US, "android.webkit.%s", className));
				}
				catch (Throwable t3)
				{
					return null;
				}
			}
		}
		
		return selectByType(clazz);
		
	}
	
	/**
	 * Select all subviews of the currently-selected views that are subclasses of the given class.
	 * @param clazz class
	 * @return all the selected views in a droidQuery wrapper
	 */
	public $ selectByType(Class<?> clazz)
	{
		List<View> subviews = new ArrayList<View>();
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			subviews.addAll(recursivelySelectByType(view, clazz));
		}
		return $.with(context, subviews);
	}
	
	/**
	 * Selects the child views of the first view in the current selection
	 * @return a droidQuery Objects containing the child views. If the view is a subclass of 
	 * {@link AdapterView}, the {@link #data() data} of the droidQuery will be set to an Object[]
	 * of adapter items.
	 */
	public $ selectChildren()
	{
		List<View> list = new ArrayList<View>();
		if (view(0) instanceof AdapterView)
		{
			AdapterView<?> adapter = (AdapterView<?>) view(0);
			Object[] data = new Object[adapter.getCount()];
			for (int i = 0; i < adapter.getCount(); i++)
			{
				data[i] = adapter.getItemAtPosition(i);
			}
			return $.with(view(0)).data(data);
		}
		else if (view(0) instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) view(0)).getChildCount(); i++)
			{
				list.add(((ViewGroup) view(0)).getChildAt(i));
			}
		}
		return $.with(context, list);
	}
	
	/**
	 * Selects all subviews of the given view that do not contain subviews
	 * @param v the view whose subviews will be retrieved
	 * @return a list empty views
	 */
	private List<View> recursivelySelectEmpties(View v)
	{
		List<View> list = new ArrayList<View>();
		if (v instanceof ViewGroup && ((ViewGroup) v).getChildCount() > 0)
		{
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				list.addAll(recursivelySelectEmpties(((ViewGroup) v).getChildAt(i)));
			}
		}
		else if (!(v instanceof AdapterView && ((AdapterView<?>) v).getCount() > 0))
		{
			list.add(v);
		}
		return list;
	}
	
	/**
	 * Select all non-ViewGroups, or ViewGroups with no children, that lay within the view
	 * hierarchy of the current selection
	 * @return a droidQuery object containing the selection
	 */
	public $ selectEmpties()
	{
		List<View> subviews = new ArrayList<View>();
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			subviews.addAll(recursivelySelectEmpties(view));
		}
		return $.with(context, subviews);
	}
	
	/**
	 * Searches the view hierarchy rooted at the given view in order to find the currently
	 * focused view
	 * @param view the view to search within
	 * @return the selected view, or null if no view in the given hierarchy was focused.
	 */
	private View recursivelyFindFocusedSubView(View view)
	{
		if (view.isFocused())
			return view;
		else if (view instanceof ViewGroup)
		{
			View v = null;
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
			{
				v = recursivelyFindFocusedSubView(((ViewGroup) view).getChildAt(i));
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
	 * @return a droidQuery Object created with the currently-focused View, if there is one
	 */
	public $ selectFocused()
	{
		View focused = recursivelyFindFocusedSubView(rootView);
		if (focused != null)
			return $.with(focused);
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			focused = recursivelyFindFocusedSubView(view);
			if (focused != null)
				return $.with(focused);
		}
		
		return $.with(view(0).getContext());
	}
	
	/**
	 * Selects the currently-selected views.
	 * @return a droidQuery Object created with the currently-selected View, if there is one
	 */
	public $ selectSelected()
	{
		List<View> selected = recursivelyFindSelectedSubView(rootView);
		if (selected != null)
			return $.with(selected);
		selected = new ArrayList<View>();
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			selected.addAll(recursivelyFindSelectedSubView(view));
		}
		
		return $.with(view(0).getContext());
	}
	
	/**
	 * Searches the view hierarchy rooted at the given view in order to find the currently
	 * selected views
	 * @param view the view to search within
	 * @return the selected views, or null if no view in the given hierarchy was selected.
	 */
	private static List<View> recursivelyFindSelectedSubView(View view)
	{
		List<View> views = new ArrayList<View>();
		if (view.isSelected())
			views.add(view);
		else if (view instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
			{
				views.addAll(recursivelyFindSelectedSubView(((ViewGroup) view).getChildAt(i)));
			}
		}
		return views;
	}
	
	/**
	 * Select all {@link View#INVISIBLE invisible}, {@link View#GONE gone}, and 0-alpha views within the 
	 * view hierarchy rooted at the given view
	 * @param v the view hierarchy in which to search
	 * @return a list the found views
	 */
	private List<View> recursivelySelectHidden(View v)
	{
		List<View> list = new ArrayList<View>();
		if (v instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				list.addAll(recursivelySelectHidden(((ViewGroup) v).getChildAt(i)));
			}
		}
		if (v.getVisibility() == View.INVISIBLE || v.getVisibility() == View.GONE || ViewHelper.getAlpha(v) == 0)
			list.add(v);
		return list;
	}
	
	/**
	 * Select all {@link View#VISIBLE visible} and 1-alpha views within the given view hierarchy
	 * @param v the view to search in
	 * @return a list the found views
	 */
	private List<View> recursivelySelectVisible(View v)
	{
		List<View> list = new ArrayList<View>();
		if (v instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				list.addAll(recursivelySelectVisible(((ViewGroup) v).getChildAt(i)));
			}
		}
		if (v.getVisibility() == View.VISIBLE || ViewHelper.getAlpha(v) == 1)
			list.add(v);
		return list;
	}
	
	/**
	 * Select all {@link View#INVISIBLE invisible}, {@link View#GONE gone}, and 0-alpha views within 
	 * the view hierarchy of the currently selected views
	 * @return a droidQuery Object containing the found views
	 */
	public $ selectHidden()
	{
		List<View> subviews = new ArrayList<View>();
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			subviews.addAll(recursivelySelectHidden(view));
		}
		return $.with(context, subviews);
	}
	
	/**
	 * Select all {@link View#VISIBLE visible} and 1-alpha views within the view hierarchy
	 * of the currenly selected views
	 * @return a droidQuery Object containing the found views
	 */
	public $ selectVisible()
	{
		List<View> subviews = new ArrayList<View>();
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			subviews.addAll(recursivelySelectVisible(view));
		}
		return $.with(context, subviews);
	}
	
	/**
	 * Set the current selection to the view with the given id
	 * @param id the id of the view to manipulate
	 * @return this
	 * @see #ids(int...)
	 */
	public $ id(int id)
	{
		View view = this.findViewById(id);
		if (view != null)
		{
			this.views.clear();
			this.rootView = view;
			this.views.add(view);
		}
		return this;
	}
	
	/**
	 * Set the tag for all selected Views
	 * @param tag the Object to set as the tag, or {@code null} to clear the tag
	 * @return this
	 */
	public $ tag(Object tag)
	{
		for (int i = 0; i < views.size(); i++)
		{
			views.get(i).setTag(tag);
		}
		return this;
	}
	
	/**
	 * Get the tag for the first selected view
	 * @return the tag for the first view in the current selection
	 */
	public Object tag()
	{
		return view(0).getTag();
	}
	
	/**
	 * Set the current selection to the set of views with the given id.
	 * @param ids
	 * @return
	 * @see #id(int)
	 */
	public $ ids(int... ids)
	{
		this.views.clear();
		this.rootView = findViewById(ids[0]);
		views.add(rootView);
		for (int i = 1; i < ids.length; i++) 
		{
			this.views.add(findViewById(ids[i]));
		}
		return this;
	}
	
	/**
	 * Selects all {@link ImageView}s within the currently selected view hierarchies
	 * @return a droidQuery Object containing the found {@code ImageView}s
	 */
	public $ selectImages()
	{
		List<View> subviews = new ArrayList<View>();
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			subviews.addAll(recursivelySelectByType(view, ImageView.class));
		}
		return $.with(context, subviews);
	}

	/**
	 * Selects all views within the given current selection that are the single 
	 * children of their parent views
	 * @param v the view whose hierarchy will be checked
	 * @return a list of the found views.
	 */
	private List<View> recursivelySelectOnlyChilds(View v)
	{
		List<View> list = new ArrayList<View>();
		if (v instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				list.addAll(recursivelySelectOnlyChilds(((ViewGroup) v).getChildAt(i)));
			}
		}
		if (v.getParent() instanceof ViewGroup && ((ViewGroup) v.getParent()).getChildCount() == 1)
			list.add(v);
		return list;
	}
	
	/**
	 * Selects all views within the current selection that are the single children of their 
	 * parent views
	 * @return a droidQuery Object containing the found views.
	 */
	public $ selectOnlyChilds()
	{
		List<View> subviews = new ArrayList<View>();
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			subviews.addAll(recursivelySelectOnlyChilds(view));
		}
		return $.with(context, subviews);
	}
	
	/**
	 * Selects all views within the given current selection that are the nth 
	 * child of their parent views
	 * @param v the view whose hierarchy will be checked
	 * @param n the index to search. Note that a value of 1 will search the 0th child.
	 * @return a list of the found views.
	 */
	private List<View> recursivelySelectNthChilds(View v, final int n)
	{
		final List<View> list = new ArrayList<View>();
		if (v instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				list.addAll(recursivelySelectNthChilds(((ViewGroup) v).getChildAt(i), n));
			}
		}
		$.with(v).siblings(new Function() {
			
			@Override
			public void invoke($ droidQuery, Object... params) {
				int index = (Integer) params[0];
				if (index == n)
					list.add(droidQuery.view(0));
			}
		});
		return list;
	}
	
	/**
	 * Selects all views within the current selection that are the nth child of their parent views
	 * @param n the index to search. Note that a value of 1 will search the 0th child.
	 * @return a droidQuery Object containing the found views.
	 */
	public $ selectNthChilds(int n)
	{
		List<View> subviews = new ArrayList<View>();
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			subviews.addAll(recursivelySelectNthChilds(view, n));
		}
		return $.with(context, subviews);
	}
	
	/**
	 * Selects all views within the given current selection that are the nth-from-the-end 
	 * child of their parent views
	 * @param v the view whose hierarchy will be checked
	 * @param n the index to search. Note that a value of 1 will search the 0th child.
	 * @return a list of the found views.
	 */
	private List<View> recursivelySelectNthFromEndChilds(View v, final int n)
	{
		final List<View> list = new ArrayList<View>();
		if (v instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				list.addAll(recursivelySelectNthFromEndChilds(((ViewGroup) v).getChildAt(i), n));
			}
		}
		final List<View> siblings = new ArrayList<View>();
		$.with(v).siblings(new Function() {
			
			@Override
			public void invoke($ droidQuery, Object... params) {
				siblings.add(droidQuery.view(0));
			}
		});
		Collections.reverse(siblings);
		if (siblings.size() >= n)
			list.add(siblings.get(n));
		return list;
	}
	
	/**
	 * Selects all views within the current selection that are the nth-from-the-end 
	 * child of their parent views
	 * @param n the index to search. Note that a value of 1 will search the 0th child.
	 * @return a droidQuery Object containing the found views.
	 */
	public $ selectNthFromEndChilds(int n)
	{
		List<View> subviews = new ArrayList<View>();
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			subviews.addAll(recursivelySelectNthFromEndChilds(view, n));
		}
		return $.with(context, subviews);
	}
	
	/**
	 * Selects all views within the given current selection that are the nth 
	 * child of type {@code type} of their parent views
	 * @param v the view whose hierarchy will be checked
	 * @param n the index to search. Note that a value of 1 will search the 0th child.
	 * @param type the type of view to select
	 * @return a list of the found views.
	 */
	private List<View> recursivelySelectNthChildsOfType(View v, final int n, final Class<?> type)
	{
		final List<View> list = new ArrayList<View>();
		if (v instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				list.addAll(recursivelySelectNthChildsOfType(((ViewGroup) v).getChildAt(i), n, type));
			}
		}
		final Count count = new Count();
		$.with(v).siblings(new Function() {
			
			@Override
			public void invoke($ droidQuery, Object... params) {
				if (type.isInstance(droidQuery.view(0)))
					count.index++;
				if (count.index == n)
					list.add(droidQuery.view(0));
			}
		});
		return list;
	}
	
	/**
	 * Selects all views within the current selection that are the nth child of type {@code type} of 
	 * their parent views
	 * @param n the index to search. Note that a value of 1 will search the 0th child.
	 * @param type the type of view to select
	 * @return a droidQuery Object containing the found views.
	 */
	public $ selectNthChildsOfType(int n, Class<?> type)
	{
		List<View> subviews = new ArrayList<View>();
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			subviews.addAll(recursivelySelectNthChildsOfType(view, n, type));
		}
		return $.with(context, subviews);
	}
	
	/**
	 * Selects all views within the current selection that are the nth child of type {@code type} 
	 * of their parent views
	 * @param n the index to search. Note that a value of 1 will search the 0th child.
	 * @param className the name of the type of view to select
	 * @return a droidQuery Object containing the found views.
	 */
	public $ selectNthChildsOfType(int n, String className)
	{
		Class<?> clazz = null;
		try
		{
			clazz = Class.forName(className);
		}
		catch (Throwable t)
		{
			try
			{
				clazz = Class.forName(String.format(Locale.US, "android.widget.%s", className));
			}
			catch (Throwable t2)
			{
				try
				{
					clazz = Class.forName(String.format(Locale.US, "android.webkit.%s", className));
				}
				catch (Throwable t3)
				{
					return null;
				}
			}
		}
		
		return selectNthChildsOfType(n, clazz);
	}
	
	/**
	 * Selects all views within the given current selection that are the nth-from-the-end 
	 * child of type {@code type} of their parent views
	 * @param v the view whose hierarchy will be checked
	 * @param n the index to search. 
	 * @param type the type of view to select
	 * @return a list of the found views.
	 */
	private List<View> recursivelySelectNthFromEndChildsOfType(View v, final int n, final Class<?> type)
	{
		final List<View> list = new ArrayList<View>();
		if (v instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				list.addAll(recursivelySelectNthFromEndChildsOfType(((ViewGroup) v).getChildAt(i), n, type));
			}
		}
		final List<View> siblings = new ArrayList<View>();
		$.with(v).siblings(new Function() {
			
			@Override
			public void invoke($ droidQuery, Object... params) {
				siblings.add(droidQuery.view(0));
			}
		});
		Collections.reverse(siblings);
		int count = 0;
		for (int i = 0; i < siblings.size(); i++)
		{
			if (type.isInstance(siblings.get(i)))
				count++;
			if (count == n)
			{
				list.add(siblings.get(i));
				break;
			}
			
		}
		return list;
	}
	
	/**
	 * Selects all views within the current selection that are the nth-from-the-end 
	 * child of type {@code type} of their parent views
	 * @param n the index to search. 
	 * @param type the type of view to select
	 * @return a droidQuery Object containing the found views.
	 */
	public $ selectNthFromEndChildsOfType(int n, Class<?> type)
	{
		List<View> subviews = new ArrayList<View>();
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			subviews.addAll(recursivelySelectNthFromEndChildsOfType(view, n, type));
		}
		return $.with(context, subviews);
	}
	
	/**
	 * Selects all views within the current selection that are the nth-from-the-end 
	 * child of type {@code type} of their parent views
	 * @param n the index to search. Note that a value of 1 will search the 0th child.
	 * @param className the name of the type of view to select
	 * @return a droidQuery Object containing the found views.
	 */
	public $ selectNthFromEndChildsOfType(int n, String className)
	{
		Class<?> clazz = null;
		try
		{
			clazz = Class.forName(className);
		}
		catch (Throwable t)
		{
			try
			{
				clazz = Class.forName(String.format(Locale.US, "android.widget.%s", className));
			}
			catch (Throwable t2)
			{
				try
				{
					clazz = Class.forName(String.format(Locale.US, "android.webkit.%s", className));
				}
				catch (Throwable t3)
				{
					return null;
				}
			}
		}
		
		return selectNthFromEndChildsOfType(n, clazz);
	}
	
	/**
	 * Selects all views within the given current selection that are the last 
	 * child of their parent views
	 * @param v the view whose hierarchy will be checked
	 * @return a list of the found views.
	 */
	private List<View> recursivelySelectLastChilds(View v, final int n)
	{
		final List<View> list = new ArrayList<View>();
		if (v instanceof ViewGroup)
		{
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++)
			{
				list.addAll(recursivelySelectLastChilds(((ViewGroup) v).getChildAt(i), n));
			}
		}
		
		final List<View> siblings = new ArrayList<View>();
		$.with(v).siblings(new Function() {
			
			@Override
			public void invoke($ droidQuery, Object... params) {
				siblings.add(droidQuery.view(0));
			}
		});
		if (siblings.size() > 0)
			list.add(siblings.get(siblings.size()-1));
		return list;
	}
	
	/**
	 * Selects all views within the current selection that are the last child of their parent views
	 * @return a droidQuery Object containing the found views.
	 */
	public $ selectLastChilds(int n)
	{
		List<View> subviews = new ArrayList<View>();
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			subviews.addAll(recursivelySelectLastChilds(view, n));
		}
		return $.with(context, subviews);
	}
	
	/**
	 * Selects all views in the current selection that can contain other child views
	 * @return a droidQuery Object containing the found ViewGroups
	 */
	public $ selectParents()
	{
		List<View> subviews = new ArrayList<View>();
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			subviews.addAll(recursivelySelectByType(view, ViewGroup.class));
		}
		return $.with(context, subviews);
	}
	
	/**
	 * Select the intersection of this droidQuery's selection with another droidQuery's selection.
	 * This is a costly operation, with a time-complexity of O(n^2).
	 * @param other$ the droidQuery which which to intersect
	 * @return a new droidQuery instance containing a selection of Views only found in both droidQuery instances.
	 */
	public $ intersect($ other$)
	{
		List<View> a = new ArrayList<View>(views);
		List<View> b = new ArrayList<View>(other$.views);
		List<View> x = new ArrayList<View>();
		for (View v0 : a)
		{
			for (View v1 : b)
			{
				if (v0.equals(v1))
				{
					x.add(v0);
					continue;
				}
			}
		}
		return $.with(x);
	}
	
	/**
	 * Select the union of this droidQuery's selection with another droidQuery's selection.
	 * @param other$ the droidQuery which which to merge
	 * @return a new droidQuery instance containing a selection of all Views found in each droidQuery instances.
	 */
	public $ union($ other$)
	{
		List<View> a = new ArrayList<View>(views);
		List<View> b = new ArrayList<View>(other$.views);
		for (View v : b)
		{
			if (!a.contains(v))
				a.add(v);
		}
		return $.with(a);
	}
	
	/**
	 * Select the intersection two droidQuery's selections.
	 * This is a costly operation, with a time-complexity of O(n^2).
	 * @param a
	 * @param b
	 * @return a new droidQuery instance containing a selection of Views only found in both droidQuery instances.
	 */
	public static $ intersect($ a, $ b)
	{
		List<View> _a = new ArrayList<View>(a.views);
		List<View> _b = new ArrayList<View>(b.views);
		List<View> x = new ArrayList<View>();
		for (View v0 : _a)
		{
			for (View v1 : _b)
			{
				if (v0.equals(v1))
				{
					x.add(v0);
					continue;
				}
			}
		}
		return $.with(x);
	}
	
	/**
	 * Select the union of two droidQuery's selections
	 * @param a
	 * @param b
	 * @return a new droidQuery instance containing a selection of all Views found in each droidQuery instances.
	 */
	public static $ union($ a, $ b)
	{
		List<View> _a = new ArrayList<View>(a.views);
		List<View> _b = new ArrayList<View>(b.views);
		for (View v : _b)
		{
			if (!_a.contains(v))
				_a.add(v);
		}
		return $.with(_a);
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
	 * <li>the File that was written (to)
	 * </ol>
	 * @param error Function to invoke on a file I/O error. Parameters received will be:
	 * <ol>
	 * <li>the String to write
	 * <li>the String reason
	 * </ol>
	 */
	public void write(final String s, final FileLocation path, final String fileName, boolean append, boolean async, final Function success, Function error)
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
				error.invoke(this, s, "Invalid Project Package!");
			}
			return;
		}
		if (!hasWritePermissions)
		{
			if (error != null)
			{
				error.invoke(this, s, "You do not have file write privelages. Add the android.permission.WRITE_EXTERNAL_STORAGE permission to your Android Manifest.");
			}
			return;
		}
		
		File logFile;
		
		if (path == FileLocation.INTERNAL)
		{
			if (fileName.contains("\\")) {
				if (error != null)
				{
					error.invoke(this, s, "Internal file names cannot include a path separator. Aborting.");
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
							success.invoke($.this, s, new File(String.format(Locale.US, "%s/%s", context.getFilesDir().getName(), fileName)));
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
			final File fLogFile = logFile;
			LogFileObserver o = new LogFileObserver(logFile, new Runnable(){
				@Override
				public void run()
				{
					if (success != null)
						success.invoke($.this, s, fLogFile);
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
				error.invoke(this, s, path, "Invalid Project Package!");
			}
			return;
		}
		if (!hasWritePermissions)
		{
			if (error != null)
			{
				error.invoke(this, s, path, "You do not have file write privelages. Add the android.permission.WRITE_EXTERNAL_STORAGE permission to your Android Manifest.");
			}
			return;
		}
		
		File logFile;
		
		if (path == FileLocation.INTERNAL)
		{
			if (fileName.contains("\\")) {
				if (error != null)
				{
					error.invoke(this, s, path, "Internal file names cannot include a path separator. Aborting.");
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
							success.invoke($.this, s, path);
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
						success.invoke($.this, s, path);
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
		for (int i = 0; i < array.length(); i++)
		{
			try
			{
				obj[i] = array.get(i);
			}
			catch (Throwable t)
			{
				obj[i] = JSONObject.NULL;
			}
			
		}
		return obj;
		
		
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
	 * Shortcut method for creating a Map of Key-Value pairings. For example:<br>
	 * $.map($.entry(0, "Zero"), $.entry(1, "One"), $.entry(2, "Two"));
	 * @param entries the MapEntry Objects used to populate the map.
	 * @return a new map with the given entries
	 * @see #entry(Object, Object)
	 */
	public static Map<?, ?> map(Entry<?, ?>... entries)
	{
		return QuickMap.qm(entries);
	}
	
	/**
	 * Shortcut method for creating a Key-Value pairing. For example:<br>
	 * $.map($.entry(0, "Zero"), $.entry(1, "One"), $.entry(2, "Two"));
	 * @param key the key
	 * @param value the value
	 * @return the Key-Value pairing Object
	 * @see #map(Entry...)
	 */
	public static Entry<?, ?> entry(Object key, Object value)
	{
		return QuickEntry.qe(key, value);
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
			public void invoke($ droidQuery, Object... args) {}
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
	 * Parses a JSON string into a JSONObject or JSONArray
	 * @param json the String to parse
	 * @return JSONObject or JSONArray (depending on the given string) if parse succeeds. Otherwise {@code null}.
	 */
	public static Object parseJSON(String json)
	{
		try {
			if (json.trim().startsWith("{"))
        		return new JSONObject(json);
        	else
        		return new JSONArray(json);
		} catch (JSONException e) {
			return null;
		}
	}
	
	/**
	 * Parses XML into an XML Document
	 * @param xml the XML to parse
	 * @return XML Document if parse succeeds. Otherwise {@code null}.
	 */
	public static Document parseXML(String xml)
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
	public static Spanned parseHTML(String html)
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
			ajax(new AjaxOptions(options));
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
			if (options.usesNewAPI())
				new Ajax(options).execute();
			else
				new AjaxTask(options).execute();
		}
		catch (Throwable t)
		{
			Log.e("droidQuery", "Could not complete ajax task!", t);
		}
	}
	
	/**
	 * Perform an Ajax Task. This is usually done as the result of an Ajax Error
	 * @param request the request
	 * @param options the configuration
	 * @see AjaxTask.AjaxError
	 * @deprecated This will make a request using the legacy AjaxTask, which is no longer supported. 
	 * Using {@link #ajax(HttpURLConnection, AjaxOptions)} is recommended.
	 */
	public static void ajax(HttpUriRequest request, AjaxOptions options)
	{
		new AjaxTask(request, options).execute();
	}
	
	/**
	 * Perform an Ajax Task. This is usually done as the result of an Ajax Error. Note that this uses new Ajax methods.
	 * @param request the request
	 * @param options the configuration
	 * @see AjaxTask.AjaxError
	 */
	public static void ajax(HttpURLConnection request, AjaxOptions options) {
		new Ajax(request, options).execute();
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
	 * <li>"request" : HttpClient request Object (if Ajax uses the deprecated {@link AjaxTask} class - otherwise this will always be {@code null})
	 * <li>"connection" : HttpURLConnection Object (only if request is null - if not, this object will not be mapped).
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
		Ajax.killTasks();
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
		$.ajax(new AjaxOptions().url(url).data(data).complete(new Function() {

			@Override
			public void invoke($ droidQuery, Object... params) {
				$.this.html(params[0].toString());
				complete.invoke($.this, params);
			}
			
		}));
	}
	
	/**
	 * creates a text string in standard URL-encoded notation
	 * @return
	 */
	public String serialize()
	{
		StringBuilder serial = new StringBuilder();
		boolean isFirst = true;
		synchronized(views)
		{
			for (int i = 0; i < views.size(); i++)
			{
				View v = views.get(i);
				if (v.getId() != 0)
				{
					String name = v.getContext().getResources().getResourceEntryName(v.getId());
					Object value = $.with(v).val();
					if (value != null)
					{
						if (isFirst)
						{
							serial.append(String.format(Locale.US, "%s=%s", name, value.toString()));
							isFirst = false;
						}
						else
							serial.append(String.format(Locale.US, "&%s=%s", name, value.toString()));
						
					}
				}
			}
		}
		return serial.toString();
	}
	
	/**
	 * creates a JavaScript array of objects, ready to be encoded as a JSON string
	 * @return
	 */
	public String serializeArray()
	{
		StringBuilder serial = new StringBuilder("]");
		boolean isFirst = true;
		synchronized(views)
		{
			for (int i = 0; i < views.size(); i++)
			{
				View v = views.get(i);
				if (v.getId() != 0)
				{
					String name = v.getContext().getResources().getResourceEntryName(v.getId());
					Object value = $.with(v).val();
					if (value != null)
					{
						if (isFirst)
						{
							serial.append(String.format(Locale.US, "{\"name\":\"%s\",\"value\":%s}", name, (value instanceof String ? String.format(Locale.US, "%s", value) : value.toString())));
							isFirst = false;
						}
						else
						{
							serial.append(String.format(Locale.US, ",{\"name\":\"%s\",\"value\":%s}", name, (value instanceof String ? String.format(Locale.US, "%s", value) : value.toString())));
						}
						
					}
				}
			}
		}
		serial.append("]");
		return serial.toString();
	}
	
	//// Convenience
	
	/**
	 * Include the html string in the selected views. If a view has a setText method, it is used. Otherwise,
	 * a new TextView is created. This html can also handle image tags for both urls and local files.
	 * Local files should be the name (for example, for R.id.ic_launcher, just use ic_launcher).
	 * @param resourceID the ID of the String resource
	 */
	public $ html(int resourceID)
	{
		return html(context.getResources().getText(resourceID).toString());
	}
	
	/**
	 * Include the html string the selected views. If a view has a setText method, it is used. Otherwise,
	 * a new TextView is created. This html can also handle image tags for both urls and local files.
	 * Local files should be the name (for example, for R.id.ic_launcher, just use ic_launcher).
	 * @param html the HTML String to include
	 */
	public $ html(String html)
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			try
			{
				Method m = view.getClass().getMethod("setText", new Class<?>[]{CharSequence.class});
				m.invoke(view, (CharSequence) Html.fromHtml(html));
			}
			catch (Throwable t)
			{
				if (view instanceof ViewGroup)
				{
					try
					{
						//no setText method. Try a TextView
						TextView tv = new TextView(this.context);
						tv.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
						tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
						((ViewGroup) view).addView(tv);
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
		}
		
		return this;
	}
	
	/**
	 * Includes the given text string inside of the selected views. If a view has a setText method, it is used
	 * otherwise, if possible, a textview is added as a child to display the text.
	 * @param resourceId resource ID of the text to include
	 */
	public $ text(int resourceId)
	{
		return text(context.getResources().getText(resourceId));
	}
	
	/**
	 * Includes the given text string inside of the selected views. If a view has a setText method, it is used
	 * otherwise, if possible, a textview is added as a child to display the text.
	 * @param text the text to include
	 */
	public $ text(CharSequence text)
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			try
			{
				Method m = view.getClass().getMethod("setText", new Class<?>[]{CharSequence.class});
				m.invoke(view, text);
			}
			catch (Throwable t)
			{
				if (view instanceof ViewGroup)
				{
					try
					{
						//no setText method. Try a TextView
						TextView tv = new TextView(this.context);
						tv.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
						tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
						tv.setText(text);
						((ViewGroup) view).addView(tv);
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
		}
		
		return this;
	}
	
	/**
	 * Includes the given image inside of the selected views. If a view is an `ImageView`, its image
	 * is set. Otherwise, the background image of the view is set.
	 * @param resourceId the resource ID of the drawable to display
	 * @return this
	 */
	public $ image(int resourceId)
	{
		for (View v : views)
		{
			if (v instanceof ImageView)
			{
				((ImageView) v).setImageResource(resourceId);
			}
			else
			{
				v.setBackgroundResource(resourceId);
			}
		}
		return this;
	}
	
	/**
	 * Includes the given image inside of the selected views. If a view is an `ImageView`, its image
	 * is set. Otherwise, the background image of the view is set.
	 * @param image the bitmap image to include
	 * @return this
	 */
	public $ image(Bitmap image)
	{
		for (View v : views)
		{
			if (v instanceof ImageView)
			{
				((ImageView) v).setImageBitmap(Bitmap.createBitmap(image));
			}
			else
			{
				v.setBackgroundDrawable(new BitmapDrawable(image));
			}
		}
		return this;
	}
	
	/**
	 * Includes the given image inside of the selected views. If a view is an `ImageView`, its image
	 * is set. Otherwise, the background image of the view is set.
	 * @param image the drawable image to include
	 * @return this
	 */
	public $ image(Drawable image)
	{
		for (View v : views)
		{
			if (v instanceof ImageView)
			{
				((ImageView) v).setImageDrawable(image);
			}
			else
			{
				v.setBackgroundDrawable(image);
			}
		}
		return this;
	}
	
	/**
	 * For `ImageView`s, this will set the image to the given asset or url. Otherwise, it will set the
	 * background image for the selected views.
	 * @param source asset path, file path (starting with "file://") or URL to image
	 * @return this
	 */
	public $ image(String source)
	{
		return image(source, -1, -1, null);
	}
	
	/**
	 * For `ImageView`s, this will set the image to the given asset or url. Otherwise, it will set the
	 * background image for the selected views.
	 * @param source asset path, file path (starting with "file://") or URL to image
	 * @param width specifies the output bitmap width
	 * @param height specifies the output bitmap height
	 * @param error if the given source is a file or asset, this receives a droidQuery wrapping the 
	 * current context and the {@code Throwable} error. Otherwise, this will receive an
	 * Ajax error.
	 * @return this
	 * @see AjaxOptions#error(Function)
	 */
	public $ image(final String source, int width, int height, final Function error)
	{
		if (source.startsWith("file://"))
		{
			try {
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
				if (width >= 0)
					opt.outWidth = width;
				if (height >= 0)
					opt.outHeight = height;
				Bitmap bitmap = BitmapFactory.decodeFile(source.substring(6), opt);
				for (View v : views)
				{
					if (v instanceof ImageView)
					{
						try
						{
							((ImageView) v).setImageBitmap(Bitmap.createBitmap(bitmap));
						}
						catch (Throwable t)
						{
							if (error != null)
								error.invoke($.with(context), t);
						}
					}
					else
					{
						v.setBackgroundDrawable(new BitmapDrawable(Bitmap.createBitmap(bitmap)));
					}
				}
			}
			catch(Throwable t) {
				if (error != null) {
					error.invoke($.with(context), t);
				}
			}
		}
		else if (URLUtil.isValidUrl(source))
		{
			AjaxOptions options = new AjaxOptions().url(source)
				                                   .type("GET")
				                                   .dataType("image")
				                                   .context(context)
				                                   .global(false)
				                                   .redundancy(Redundancy.RESPOND_TO_ALL_LISTENERS)
				                                   .success(new Function() {
				@Override
				public void invoke($ droidQuery, Object... params) {
					Bitmap bitmap = (Bitmap) params[0];
					for (View v : views)
					{
						if (v instanceof ImageView)
						{
							try {
								((ImageView) v).setImageBitmap(Bitmap.createBitmap(bitmap));
							}
							catch (Throwable t)
							{
								if (error != null)
									error.invoke($.with(context), t);
							}
						}
						else
						{
							v.setBackgroundDrawable(new BitmapDrawable(Bitmap.createBitmap(bitmap)));
						}
					}
				}
			});
			
			if (error != null) {
				options.error(error);
			}
			if (width >= 0)
			{
				options.imageWidth(width);
			}
			if (height >= 0)
			{
				options.imageHeight(height);
			}
			$.ajax(options);
		}
		else
		{
			try {
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inSampleSize = 1;
				opt.inPurgeable = true;
				opt.inInputShareable = false;
				if (width >= 0)
					opt.outWidth = width;
				if (height >= 0)
					opt.outHeight = height;
				Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets().open(source), new Rect(0,0,0,0), opt);
				for (View v : views)
				{
					if (v instanceof ImageView)
					{
						try
						{
							((ImageView) v).setImageBitmap(Bitmap.createBitmap(bitmap));
						}
						catch (Throwable t)
						{
							if (error != null)
								error.invoke($.with(context), t);
						}
					}
					else
					{
						v.setBackgroundDrawable(new BitmapDrawable(Bitmap.createBitmap(bitmap)));
					}
				}
				
			}
			catch(Throwable t) {
				if (error != null) {
					error.invoke($.with(context), t);
				}
			}
			
			
		}
		return this;
	}
	
	/**
	 * Iterates through the selected views and sets the images to the given images (in order)
	 * @param source asset path, file path (starting with "file://") or URL to image
	 * @return this
	 */
	public $ image(final List<String> sources)
	{
		this.each(new Function() {
			@Override
			public void invoke($ droidQuery, Object... params) {
				droidQuery.image(sources.get((Integer) params[0]));
			}
		});
		return this;
	}
	
	/**
	 * Iterates through the selected views and sets the images to the given images (in order)
	 * @param sources the file paths or URLs to set
	 * @param width the output width of the image
	 * @param height the output height of the image
	 * @param error if the given source is a file or asset, this receives a droidQuery wrapping the 
	 * current context and the {@code Throwable} error. Otherwise, this will receive an
	 * Ajax error.
	 * @return this
	 * @see AjaxOptions#error(Function)
	 */
	public $ image(final List<String> sources, final int width, final int height, final Function error)
	{
		this.each(new Function() {
			@Override
			public void invoke($ droidQuery, Object... params) {
				droidQuery.image(sources.get((Integer) params[0]), width, height, error);
			}
		});
		return this;
	}
	
	/**
	 * Adds an Image over each selected View as a mask. 
	 * In most cases, this mask can be retrieved by querying siblings. For example:
	 * <pre>
	 * ImageView mask = (ImageView) $.with(myView).parent().selectChildren().selectImages().view(0);
	 * </pre>
	 * @param resourceId the resource ID of the mask drawable
	 * @return this
	 */
	public $ mask(int resourceId)
	{
		for (View v : views)
		{
			ImageView image = new ImageView(context);
			image.setImageResource(resourceId);
			image.setScaleType(ScaleType.FIT_XY);
			ViewParent parent = v.getParent();
			if (parent != null && parent instanceof ViewGroup)
			{
				image.setLayoutParams(v.getLayoutParams());
				((ViewGroup) parent).addView(image);
			}
			else if (v instanceof ViewGroup)
			{
				image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				((ViewGroup) v).addView(image);
			}
		}
		return this;
	}
	
	/**
	 * Adds an Image over each selected View as a mask.
	 * In most cases, this mask can be retrieved by querying siblings. For example:
	 * <pre>
	 * ImageView mask = (ImageView) $.with(myView).parent().selectChildren().selectImages().view(0);
	 * </pre>
	 * @param mask the bitmap to draw
	 * @return this
	 */
	public $ mask(Bitmap mask)
	{
		for (View v : views)
		{
			ImageView image = new ImageView(context);
			image.setImageBitmap(mask);
			image.setScaleType(ScaleType.FIT_XY);
			ViewParent parent = v.getParent();
			if (parent != null && parent instanceof ViewGroup)
			{
				image.setLayoutParams(v.getLayoutParams());
				((ViewGroup) parent).addView(image);
			}
			else if (v instanceof ViewGroup)
			{
				image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				((ViewGroup) v).addView(image);
			}
		}
		return this;
	}
	
	/**
	 * Adds an Image over each selected View as a mask.
	 * In most cases, this mask can be retrieved by querying siblings. For example:
	 * <pre>
	 * ImageView mask = (ImageView) $.with(myView).parent().selectChildren().selectImages().view(0);
	 * </pre>
	 * @param mask the drawable to draw
	 * @return this
	 */
	public $ mask(Drawable mask)
	{
		for (View v : views)
		{
			ImageView image = new ImageView(context);
			image.setImageDrawable(mask);
			image.setScaleType(ScaleType.FIT_XY);
			ViewParent parent = v.getParent();
			if (parent != null && parent instanceof ViewGroup)
			{
				image.setLayoutParams(v.getLayoutParams());
				((ViewGroup) parent).addView(image);
			}
			else if (v instanceof ViewGroup)
			{
				image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				((ViewGroup) v).addView(image);
			}
		}
		return this;
	}
	
	/**
	 * Adds an Image over each selected View as a mask.
	 * In most cases, this mask can be retrieved by querying siblings. For example:
	 * <pre>
	 * ImageView mask = (ImageView) $.with(myView).parent().selectChildren().selectImages().view(0);
	 * </pre>
	 * @param source asset path, file path (starting with "file://") or URL to image
	 * @return this
	 */
	public $ mask(String source)
	{
		return mask(source, -1, -1, null);
	}
	
	/**
	 * Adds an Image over each selected View as a mask.
	 * In most cases, this mask can be retrieved by querying siblings. For example:
	 * <pre>
	 * ImageView mask = (ImageView) $.with(myView).parent().selectChildren().selectImages().view(0);
	 * </pre>
	 * @param source asset path, file path (starting with "file://") or URL to image
	 * @param width specifies the output bitmap width
	 * @param height specifies the output bitmap height
	 * @param error if the given source is a file or asset, this receives a droidQuery wrapping the 
	 * current context and the {@code Throwable} error. Otherwise, this will receive an
	 * Ajax error.
	 * @return this
	 * @see AjaxOptions#error(Function)
	 */
	public $ mask(String source, int width, int height, Function error)
	{
		if (source.startsWith("file://"))
		{
			try {
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
				if (width >= 0)
					opt.outWidth = width;
				if (height >= 0)
					opt.outHeight = height;
				Bitmap bitmap = BitmapFactory.decodeFile(source.substring(6), opt);
				for (View v : views)
				{
					ImageView image = new ImageView(context);
					image.setImageBitmap(Bitmap.createBitmap(bitmap));
					image.setScaleType(ScaleType.FIT_XY);
					ViewParent parent = v.getParent();
					if (parent != null && parent instanceof ViewGroup)
					{
						image.setLayoutParams(v.getLayoutParams());
						((ViewGroup) parent).addView(image);
					}
					else if (v instanceof ViewGroup)
					{
						image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
						((ViewGroup) v).addView(image);
					}
				}
			}
			catch(Throwable t) {
				if (error != null) {
					error.invoke($.with(context), t);
				}
			}
		}
		else if (URLUtil.isValidUrl(source))
		{
			AjaxOptions options = new AjaxOptions().url(source)
					                               .type("GET")
					                               .dataType("image")
					                               .context(context)
					                               .global(false)
					                               .success(new Function() {
				@Override
				public void invoke($ droidQuery, Object... params) {
					Bitmap bitmap = (Bitmap) params[0];
					for (View v : views)
					{
						ImageView image = new ImageView(context);
						image.setImageBitmap(Bitmap.createBitmap(bitmap));
						image.setScaleType(ScaleType.FIT_XY);
						ViewParent parent = v.getParent();
						if (parent != null && parent instanceof ViewGroup)
						{
							image.setLayoutParams(v.getLayoutParams());
							((ViewGroup) parent).addView(image);
						}
						else if (v instanceof ViewGroup)
						{
							image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
							((ViewGroup) v).addView(image);
						}
					}
				}
			});
			
			if (error != null) {
				options.error(error);
			}
			if (width >= 0)
			{
				options.imageWidth(width);
			}
			if (height >= 0)
			{
				options.imageHeight(height);
			}
			$.ajax(options);
		}
		else
		{
			try {
				BitmapFactory.Options opt = new BitmapFactory.Options();
				opt.inSampleSize = 1;
				opt.inPurgeable = true;
				opt.inInputShareable = false;
				if (width >= 0)
					opt.outWidth = width;
				if (height >= 0)
					opt.outHeight = height;
				Bitmap bitmap = BitmapFactory.decodeStream(context.getAssets().open(source), new Rect(0,0,0,0), opt);
				for (View v : views)
				{
					ImageView image = new ImageView(context);
					image.setImageBitmap(Bitmap.createBitmap(bitmap));
					image.setScaleType(ScaleType.FIT_XY);
					ViewParent parent = v.getParent();
					if (parent != null && parent instanceof ViewGroup)
					{
						image.setLayoutParams(v.getLayoutParams());
						((ViewGroup) parent).addView(image);
					}
					else if (v instanceof ViewGroup)
					{
						image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
						((ViewGroup) v).addView(image);
					}
				}
				
			}
			catch(Throwable t) {
				if (error != null) {
					error.invoke($.with(context), t);
				}
			}
			
			
		}
		return this;
	}
	
	/**
	 * Iterates through the selected views and sets the masks to the given images (in order)
	 * @param source asset path, file path (starting with "file://") or URL to image
	 * @return this
	 */
	public $ mask(final List<String> sources)
	{
		this.each(new Function() {
			@Override
			public void invoke($ droidQuery, Object... params) {
				droidQuery.mask(sources.get((Integer) params[0]));
			}
		});
		return this;
	}
	
	/**
	 * Shortcut to show a Toast Message
	 * @param text the text to display
	 * @param duration the Toast Duration
	 * @see Toast#LENGTH_LONG
	 * @see Toast#LENGTH_SHORT
	 */
	public void toast(CharSequence text, int duration)
	{
		Toast.makeText(context, text, duration).show();
	}
	
	/**
	 * Shortcut to show a Toast Message
	 * @param context used to show the toast
	 * @param text the text to display
	 * @param duration the Toast Duration
	 * @see Toast#LENGTH_LONG
	 * @see Toast#LENGTH_SHORT
	 */
	public static void toast(Context context, CharSequence text, int duration)
	{
		Toast.makeText(context, text, duration).show();
	}
	
	/**
	 * Show an alert. The title will be set to the name of the application.
	 * @param context used to display the alert window
	 * @param text the message to display.
	 * @see #alert(Context, String, String)
	 */
	public static void alert(Context context, String text)
	{
		alert(context, context.getString(context.getResources().getIdentifier("app_name", "string", context.getPackageName())), text);
	}
	
	/**
	 * Show an alert
	 * @param context used to display the alert window
	 * @param title the title of the alert window. Use {@code null} to show no title
	 * @param text the alert message
	 * @see #alert(Context, String)
	 */
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
	
	/**
	 * Uses the current context to show an alert dialog. The title will be set to the name of the application.
	 * @param text the alert message.
	 * @see #alert(String, String)
	 */
	public void alert(String text)
	{
		alert(context.getString(context.getResources().getIdentifier("app_name", "string", context.getPackageName())), text);
	}
	
	/**
	 * Uses the current context to show an alert dialog.
	 * @param title the alert title
	 * @param text the alert message.
	 * @see #alert(String)
	 */
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
	 * Registered callback functions will receive a {@code null} for their <em>droidQuery</em>
	 * variable. To receive a non-{@code null} variable, you must provide a <em>Context</em>.
	 * @return a new instance of {@link Callbacks}
	 * @see #Callbacks(Context)
	 */
	public static Callbacks Callbacks()
	{
		return new Callbacks();
	}
	
	/**
	 * A multi-purpose callbacks list object that provides a powerful way to manage callback lists.
	 * If {@code context} is not {@code null}, Functions invoked will receive a non-{@code null} 
	 * droidQuery instance using that context.
	 * @return a new instance of {@link Callbacks}
	 */
	public static Callbacks Callbacks(Context context)
	{
		return new Callbacks(context);
	}
	
	//////CSS-based
	
	/**
	 * @return the computed height for the first view in the current selection
	 */
	public int height()
	{
		return Math.abs(view(0).getBottom() - view(0).getTop());
	}
	
	/**
	 * Set the height of the selected views
	 * @param height the new height
	 * @return this
	 */
	public $ height(int height)
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			ViewGroup.LayoutParams params = view.getLayoutParams();
			params.height = height;
			view.setLayoutParams(params);
		}
		return this;
	}
	
	/**
	 * @return the computed width for the first view in the current selection
	 */
	public int width()
	{
		return Math.abs(view(0).getRight() - view(0).getLeft());
	}
	
	/**
	 * Set the width of the views in the current selection
	 * @param width the new width
	 * @return this
	 */
	public $ width(int width)
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			ViewGroup.LayoutParams params = view.getLayoutParams();
			params.width = width;
			view.setLayoutParams(params);
		}
		return this;
	}
	
	/**
	 * Get the computed height for the first view in the selection, 
	 * including padding but not margin.
	 * @return
	 */
	public int innerHeight()
	{
		return height() - (view(0).getPaddingTop() + view(0).getPaddingBottom());
	}

	/**
	 * @return the computed width for the first view in the current selection, 
	 * including padding but not margin.
	 */
	public int innerWidth()
	{
		return width() - (view(0).getPaddingLeft() + view(0).getPaddingRight());
	}
	
	/**
	 * @return the computed height for the first view in the current selection, 
	 * including padding and margin.
	 */
	public int outerHeight()
	{
		Object params = view(0).getLayoutParams();
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
		return height() - (view(0).getPaddingTop() + view(0).getPaddingBottom() + margin);
	}
	
	/**
	 * @return the computed width for the first view in the current selection, 
	 * including padding and margin.
	 */
	public int outerWidth()
	{
		Object params = view(0).getLayoutParams();
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
		return width() - (view(0).getPaddingLeft() + view(0).getPaddingRight() + margin);
	}
	
	/**
	 * @return the coordinates of the first view in the current selection.
	 */
	public Point offset()
	{
		int[] loc = new int[2];
		view(0).getLocationOnScreen(loc);
		return new Point(loc[0], loc[1]);
	}
	
	/**
	 * Gets the coordinates of {@code v}
	 * @param v
	 * @return the coordinates of the given view
	 */
	private Point offset(View v)
	{
		int[] loc = new int[2];
		v.getLocationOnScreen(loc);
		return new Point(loc[0], loc[1]);
	}
	
	/**
	 * Set the coordinates of each selected view, relative to the document.
	 * @param x the x-coordinate, in pixels
	 * @param y the y-coordinate, in pixels
	 * @return this
	 */
	public $ offset(int x, int y)
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			Point offset = offset(view);
			int offsetX = x - offset.x;
			int offsetY = y - offset.y;
			Point position = position(view);
			ViewHelper.setX(view, position.x + offsetX);
			ViewHelper.setY(view, position.y + offsetY);
		}
		
		return this;
	}
	
	/**
	 * @return the coordinates of the first view in the current selection, 
	 * relative to the offset parent.
	 */
	public Point position()
	{
		return new Point(view(0).getLeft(), view(0).getTop());
	}
	
	/**
	 * Get the coordinates of the {@code view}, relative to the offset parent
	 * @param view
	 * @return the coordinates of the given view, relative to the offset parent
	 */
	private Point position(View view)
	{
		return new Point(view.getLeft(), view.getTop());
	}
	
	/**
	 * Sets the coordinates of each selected view, relative to its offset parent
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 * @return 
	 */
	public $ position(int x, int y)
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			ViewHelper.setX(view, x);
			ViewHelper.setY(view, y);
		}
		return this;
	}
	
	/**
	 * @return the current horizontal position of the scroll bar the first view of the current selection.
	 */
	public int scrollLeft()
	{
		return view(0).getScrollX();
	}
	
	/**
	 * Set the horizontal position of the scroll bar for each view in the current selection
	 * @param position the x position to which to scroll
	 * @return this
	 */
	public $ scrollLeft(int position)
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			view.scrollTo(position, view.getScrollY());
		}
		return this;
	}
	
	/**
	 * @return the current vertical position of the scroll bar for the first view of the current selection
	 */
	public int scrollTop()
	{
		return view(0).getScrollY();
	}
	
	/**
	 * Set the vertical position of the scroll bar for the currently selected views
	 * @param position the scroll position
	 * @return this
	 */
	public $ scrollTop(int position)
	{
		for (int i = 0; i < this.views.size(); i++)
		{
			View view = this.views.get(i);
			view.scrollTo(view.getScrollX(), position);
		}
		return this;
	}
	
	//////ignoring jQuery data. Doesn't come off as important here.
	
	
	
	//Timer Functions
	
	/**
	 * Schedule a task for single execution after a specified delay.
	 * @param function the task to schedule. Receives no args. Note that the function will be
	 * run on the thread on this method was called.
	 * @param delay amount of time in milliseconds before execution.
	 * @return the created Timer
	 */
	public static Timer setTimeout(final Function function, long delay)
	{
		Timer t = new Timer();
		final Handler h = new Handler();
		t.schedule(new TimerTask(){

			@Override
			public void run() {
				h.post(new Runnable() {
					@Override
					public void run()
					{
						function.invoke(null);
					}
				});
				
			}
			
		}, delay);
		return t;
	}
	
	/**
	 * Schedule a task for repeated fixed-rate execution after a specific delay has passed.
	 * @param the task to schedule. Receives no args. Note that the function will be
	 * run on a the thread on which this method was called.
	 * @param delay amount of time in milliseconds before execution.
	 * @return the created Timer
	 */
	public static Timer setInterval(final Function function, long delay)
	{
		Timer t = new Timer();
		final Handler h = new Handler();
		t.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				h.post(new Runnable() {
					@Override
					public void run()
					{
						function.invoke(null);
					}
				});
				
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
		if (string == null || string.trim().length() == 0)
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
	 * Simple class for allowing final access to an int
	 * @author Phil Brown
	 * @since 3:24:53 PM Dec 5, 2013
	 *
	 */
	private class Count
	{
		public int index;
		public Count()
		{
			index = 0;
		}
	}
}
