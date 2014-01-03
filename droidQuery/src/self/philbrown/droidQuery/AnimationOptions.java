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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.view.animation.Interpolator;

/**
 * Effect (Animation) Options for droidQuery
 * @author Phil Brown
 */
public class AnimationOptions implements Cloneable
{
	
	
	/** Used for reflection */
	private static Field[] fields = AnimationOptions.class.getDeclaredFields();

	/**
	 * Default constructor
	 */
	public AnimationOptions()
	{
		
	}
	
	/**
	 * Constructor. Accepts a key-value mapping of animation options.
	 * @param options a key-value mapping of animation options.
	 */
	public AnimationOptions(Map<String, Object> options)
	{
		this();
		for (Entry<String, Object> entry : options.entrySet())
		{
			try
			{
				Method m = getClass().getMethod(entry.getKey(), new Class<?>[]{entry.getValue().getClass()});
				m.invoke(this, entry.getValue());
			}
			catch (Throwable t)
			{
				Log.w("AnimationOptions", "Invalid Field " + entry.getKey());
			}
		}
	}
	
	/**
	 * Constructor. Accepts a JSONObject that maps options to their values.
	 * @param options the JSONObject that contains the options.
	 * @throws JSONException if the JSONObject is malformed
	 * @throws Throwable if another error occurs
	 */
	public AnimationOptions(JSONObject options) throws JSONException, Throwable
	{
		@SuppressWarnings("unchecked")
		Iterator<String> iterator = options.keys();
		
	    while (iterator.hasNext()) {
	        String key = iterator.next();
	        try {
	            Object value = options.get(key);
	            for (Field f : fields)
	            {
	            	if (f.getName().equalsIgnoreCase(key))
	            	{
	            		f.set(this, value);
	            		break;
	            	}
	            }
	            
	        } catch (JSONException e) {
	        	throw new JSONException("Invalid JSON String");
	        } catch (Throwable t)
	        {
	        	if (key != null)
	        		Log.w("AnimationOptions", "Could not set value " + key);
	        	else
	        		throw new NullPointerException("Iterator reference is null.");
	        }
	    }
	}
	
	/**
	 * Constructor. Accepts a JSON string that maps options to their values.
	 * @param options the JSON string that contains the options.
	 * @throws JSONException if the JSON string is malformed
	 * @throws Throwable if another error occurs
	 */
	public AnimationOptions(String json) throws JSONException, Throwable
	{
		this(new JSONObject(json));
	}
	
	/**
	 * Creates a new AnimationOptions Object
	 * @return
	 */
	public static AnimationOptions create()
	{
		return new AnimationOptions();
	}
	
	/**
	 * Creates a new AnimationOptions Object
	 * @param options
	 * @return
	 * @see #AnimationOptions(Map)
	 */
	public static AnimationOptions create(Map<String, Object> options)
	{
		return new AnimationOptions(options);
	}
	
	/**
	 * Creates a new AnimationOptions Object
	 * @param json
	 * @return
	 * @see #AnimationOptions(JSONObject)
	 */
	public static AnimationOptions create(JSONObject json)
	{
		try {
			return new AnimationOptions(json);
		} catch (Throwable e) {
			return null;
		}
	}
	
	/**
	 * Creates a new AnimationOptions Object
	 * @param json
	 * @return
	 * @see #AnimationOptions(String)
	 */
	public static AnimationOptions create(String json)
	{
		try {
			return new AnimationOptions(json);
		} catch (Throwable e) {
			return null;
		}
	}
	
	/**
	 * Determines how long the animation will run in milliseconds. Default is 400.
	 */
	private long duration = 400;
	
	/**
	 * @return the length of time an animation will run in milliseconds.
	 */
	public long duration() { return duration; }
	
	/**
	 * Sets the length of time an animation will run, in milliseconds
	 * @param duration the length of time, in milliseconds, that the animation will run
	 * @return this
	 */
	public AnimationOptions duration(long duration)
	{
		this.duration = duration;
		return this;
	}
	
	/**
	 * Indicates which function to use for the transition. Default is Linear.
	 */
	private $.Easing easing = $.Easing.LINEAR;
	/**
	 * @return the Easing function that will be used to animate the view
	 */
	public $.Easing easing() { return easing; }
	
	/**
	 * Set the Easing type that will be used to animate the view
	 * @param easing
	 * @return
	 */
	public AnimationOptions easing($.Easing easing)
	{
		this.easing = easing;
		return this;
	}
	
	/**
	 * Set the Easing type that will be used to animate the view. If the given String
	 * does not match with an Easing type, the easing type is set to Linear.
	 * @param easing
	 * @return
	 */
	public AnimationOptions easing(String easing)
	{
		this.easing = $.Easing.valueOf(easing.toUpperCase());
		if (this.easing == null) 
		{
			this.easing = $.Easing.LINEAR;
		}
		return this;
	}
	
//	/**
//	 * Whether or not the animation is queued
//	 */
//	private boolean isQueued = true;
//	/**
//	 * name of the queue. For the default queue, null is used.
//	 */
//	private String queueName = null;//default queue
//	public boolean isQueued() { return isQueued; }
//	public boolean isDefaultQueue() { return queueName == null; };
//	public String queueName() { return queueName; }
//	public AnimationOptions queue(boolean queue) {
//		isQueued = queue;
//		return this;
//	}
//	public AnimationOptions queue(String queueName)
//	{
//		isQueued = true;
//		this.queueName = queueName;
//		return this;
//	}
	
	/**
	 * Custom Interpolator
	 */
	private Interpolator specialEasing;
	/**
	 * @return the custom animation interpolator
	 */
	public Interpolator specialEasing() { return specialEasing; }
	/**
	 * Set a custom animation interpolator
	 * @param specialEasing the interpolator
	 * @return this
	 */
	public AnimationOptions specialEasing(Interpolator specialEasing)
	{
		this.specialEasing = specialEasing;
		return this;
	}
	
	/**
	 * A function called after each step of the animation. The invoked method will receive two 
	 * arguments:
	 * <ol>
	 * <li><b>Object</b> current value
	 * <li><b>long</b> remaining milliseconds
	 * </ol>
	 */
	private Function progress;
	/**
	 * @return the function called after each animation step
	 */
	public Function progress() { return progress; }
	/**
	 * Set the function called after each step of the animation. The invoked function will receive
	 * three arguments:
	 * <ol>
	 * <li><b>$</b> the droidQuery selected on the view that is being animated
	 * <li><b>String</b> the name of the animated value
	 * <li><b>Object</b> current value
	 * <li><b>long</b> remaining milliseconds
	 * </ol>
	 * @param progress the Function
	 * @return this
	 */
	public AnimationOptions progress(Function progress)
	{
		this.progress = progress;
		return this;
	}
	
	/** If {@code true}, show error messages in the logcat. */
	private boolean debug;
	
	/** If {@code true}, show error messages in the logcat. */
	public boolean debug() 
	{
		return debug;
	}
	
	/** 
	 * If {@code true}, show error messages in the logcat. 
	 * @return this
	 */
	public AnimationOptions debug(boolean debug)
	{
		this.debug = debug;
		return this;
	}
	
	/**
	 * The number of times the animation should repeat. Default is 0
	 */
	private int repeatCount = 0;
	
	/**
	 * Get the number of times the animation will repeat
	 * @return count
	 */
	public int repeatCount()
	{
		return repeatCount;
	}
	
	/**
	 * Set the number of times the animation should repeat. Default is 0.
	 * @param repeatCount
	 * @return this
	 */
	public AnimationOptions repeatCount(int repeatCount) 
	{
		this.repeatCount = repeatCount;
		return this;
	}
	
	/**
	 * Whether or not the animation should reverse after completing
	 */
	private boolean reverse;
	
	/**
	 * Get whether or not the animation should reverse after completing.
	 * @return {@code true} if the animation should reverse. Otherwise {@code false}.
	 */
	public boolean reverse()
	{
		return reverse;
	}
	
	/**
	 * Set whether or not the animation should reverse after completing.
	 * @param reverse {@code true} if the animation should reverse. Otherwise {@code false}.
	 */
	public AnimationOptions reverse(boolean reverse) 
	{
		this.reverse = reverse;
		return this;
	}
	
	/**
	 * A function to call once the animation has completed successfully
	 */
	private Function success;
	
	/**
	 * @return the Function called once the animation has completed successfully
	 */
	public Function success() { return success; }
	
	/**
	 * Set the function to call once the animation has completed successfully
	 * @param complete the function
	 * @return this
	 */
	public AnimationOptions success(Function success)
	{
		this.success = success;
		return this;
	}
	
	/**
	 * Called when the animation is unsuccessful (such as canceled)
	 */
	private Function fail;
	
	/**
	 * @return get the function to call when the animation is unsuccessful
	 */
	public Function fail() { return fail; }
	
	/**
	 * Sets the function to call when the animation is unsuccessful
	 * @param fail the function
	 * @return this
	 */
	public AnimationOptions fail(Function fail)
	{
		this.fail = fail;
		return this;
	}
	
	/**
	 * Always called after fail or success
	 */
	private Function complete;
	
	/**
	 * @return the function to call when the animation has completed
	 */
	public Function complete() { return complete; }
	/**
	 * Sets the function to call when the animation has completed
	 * @param complete the function
	 * @return this
	 */
	public AnimationOptions complete(Function complete)
	{
		this.complete = complete;
		return this;
	}
	
	@Override
	public Object clone()
	{
		//custom clone implementation
		AnimationOptions clone = new AnimationOptions();
		for (Field f : fields)
		{
			try {
				f.set(clone, f.get(this));
			} catch (Throwable t) {}
		}
		return clone;
	}
	
}
