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

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.view.animation.Interpolator;

/**
 * Effects (Animation) Options for droidQuery
 * @author Phil Brown
 *
 */
public class AnimationOptions 
{
	private static Method[] methods = AnimationOptions.class.getMethods();

	public AnimationOptions()
	{
		
	}
	
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
	
	public AnimationOptions(JSONObject options) throws JSONException, Throwable
	{
		@SuppressWarnings("unchecked")
		Iterator<String> iterator = options.keys();
		
	    while (iterator.hasNext()) {
	        String key = iterator.next();
	        try {
	            Object value = options.get(key);
	            for (Method m : methods)
	            {
	            	if (m.getName().equalsIgnoreCase(key) && m.getGenericParameterTypes().length != 0)
	            	{
	            		m.invoke(this, value);
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
	
	public AnimationOptions(String json) throws JSONException, Throwable
	{
		this(new JSONObject(json));
	}
	
	public static AnimationOptions create()
	{
		return new AnimationOptions();
	}
	
	public static AnimationOptions create(Map<String, Object> options)
	{
		return new AnimationOptions(options);
	}
	
	public static AnimationOptions create(JSONObject json)
	{
		try {
			return new AnimationOptions(json);
		} catch (Throwable e) {
			return null;
		}
	}
	
	public static AnimationOptions create(String json)
	{
		try {
			return new AnimationOptions(json);
		} catch (Throwable e) {
			return null;
		}
	}
	
	/**
	 * determines how long the animation will run in milliseconds. Default is 400.
	 */
	private long duration = 400;
	public long duration() { return duration; }
	public AnimationOptions duration(long duration)
	{
		this.duration = duration;
		return this;
	}
	
	/**
	 * indicates which function to use for the transition. Default is Linear.
	 */
	private $.Easing easing = $.Easing.LINEAR;
	public $.Easing easing() { return easing; }
	public AnimationOptions easing($.Easing easing)
	{
		this.easing = easing;
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
	 * custom interpolator
	 */
	private Interpolator specialEasing;
	public Interpolator specialEasing() { return specialEasing; }
	public AnimationOptions specialEasing(Interpolator specialEasing)
	{
		this.specialEasing = specialEasing;
		return this;
	}
	
	/**
	 * A function called after each step of the animation. The invoke method will receive two args:
	 * 1. Object current value 2. long remaining milliseconds
	 */
	private Function progress;
	public Function progress() { return progress; }
	public AnimationOptions progress(Function progress)
	{
		this.progress = progress;
		return this;
	}
	
	/**
	 * A function to call once the animtion is complete
	 */
	private Function complete;
	public Function complete() { return complete; }
	public AnimationOptions complete(Function complete)
	{
		this.complete = complete;
		return this;
	}
	
	/**
	 * Called when the animation is unsuccessful (such as canceled)
	 */
	private Function fail;
	public Function fail() { return fail; }
	public AnimationOptions fail(Function fail)
	{
		this.fail = fail;
		return this;
	}
	
	/**
	 * Always called after fail or completion
	 */
	private Function always;
	public Function always() { return always; }
	public AnimationOptions always(Function always)
	{
		this.always = always;
		return this;
	}
	
	
}
