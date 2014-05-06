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

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

/**
 * LRU cache for storing Ajax Responses
 * @author Phil Brown
 * @since 8:25:44 AM Oct 4, 2013
 *
 */
public class AjaxCache
{
	/**
	 * Timeout to use in {@link AjaxOptions#cacheTimeout(long)} in order to specify that the cached
	 * response will only be timed out when {@link #clearCache()} is called or, if {@link #maxSize}
	 * is greater than 0, if the size is too large, and this has not been accessed recently.
	 */
	public static final int TIMEOUT_NEVER = -1;
	/**
	 * Timeout to use in {@link AjaxOptions#cacheTimeout(long)} in order to specify that the cached
	 * response will never be cleared unless {@link #removeEntry(AjaxOptions)} or {@link #removeEntry(String)}
	 * is specifically called. Additionally, {@link #maxSize} and LRU algorithms will not affect these
	 * entries.
	 */
	public static final int TIMEOUT_NEVER_CLEAR_FROM_CACHE = -2;
	
	/** singleton instance */
	private static AjaxCache self;
	
	/** Stores data */
	private static Map<String, ? super Object> data;
	/** Stores always-cached data */
	private static Map<String, ? super Object> alwaysData;
	/** Stores dates */
	private static Map<String, Date> dates;
	/** {@code true} to show verbose output. Otherwise {@code false}. */
	private boolean verbose;
	
	/** The current size of the cache. */
	private int size;
	/** The maximum size of the cache. If -1, the cache size is unlimited. */
	private int maxSize = -1;
	private OnObjectCachedListener sizeListener;
	
	/** Callback for cache-cleared events. */
	private OnCacheClearedListener onCacheClearedListener;
	
	/**
	 * Constructor.
	 */
	private AjaxCache()
	{
		data = new LinkedHashMap<String, Object>(0, 0.75f, true);
		dates = new LinkedHashMap<String, Date>(0, 0.75f, true);
		alwaysData = new LinkedHashMap<String, Object>(0, 0.75f, true);
	}
	
	/**
	 * Singleton accessor
	 * @return
	 */
	public static AjaxCache sharedCache()
	{
		if (self == null)
			self = new AjaxCache();
		return self;
	}
	
	/**
	 * Set the max size, in user-specified units. If {@code -1}, there will be no cache size limit.
	 * @param size
	 * @param listener used to determine the user-defined size of an entry. May be {@code null},
	 * to specify that one unit equals one entry.
	 */
	public void setMaxSize(int size, OnObjectCachedListener listener)
	{
		this.maxSize = size;
		this.sizeListener = listener;
	}
	
	/**
	 * Sets the callback that is invoked when the cache is cleared.
	 * @param listener the callback to invoke
	 */
	public void setOnCacheClearedListener(OnCacheClearedListener listener)
	{
		this.onCacheClearedListener = listener;
	}
	
	/**
	 * Enable or disable verbose logging
	 * @param verbose {@code true} to log more. Otherwise {@code false}
	 * @return this
	 */
	public AjaxCache verbose(boolean verbose)
	{
		this.verbose = verbose;
		return this;
	}
	
	/**
	 * Get the cached response for the given options
	 * @param options the options used to store the cache entry, or an options with the same data type, type, url, and data.
	 * @return the cached Object
	 */
	public Object getCachedResponse(AjaxOptions options)
	{
		String key = String.format(Locale.US, "%s::%s::%s::%s", options.dataType(), (options.type() == null ? "GET" : options.type()), options.url(), (options.data() == null ? "" : options.data().toString()));

		Object response = null;
		Date date = null;
		
		boolean hasDate = true;
		if (options.cacheTimeout() == TIMEOUT_NEVER_CLEAR_FROM_CACHE)
		{
			hasDate = false;
			synchronized(alwaysData)
			{
				response = alwaysData.get(key);
				date = null;
			}
		}
		else
		{
			synchronized(data)
			{
				response = data.get(key);
				synchronized(dates)
				{
					date = dates.get(key);
				}
			}
		}
		
		if (verbose)
		{
			Log.i("getCachedResponse", "Key = " + key);
			Log.i("getCachedResponse", "Response = " + (response == null ? "null" : response.toString()));
			if (hasDate)
				Log.i("getCachedResponse", "Date = " + (date == null ? "null" : date.toString()));
		}
		
		if (!hasDate)
		{
			if (verbose) Log.i("getCachedResponse", "Returning cached response");
			return response;
		}
		
		if (response != null && date != null)
		{
			long cacheTime = date.getTime();
			long now = new Date().getTime();
			long cacheTimeout = options.cacheTimeout();
			if (cacheTimeout == TIMEOUT_NEVER || cacheTimeout == TIMEOUT_NEVER_CLEAR_FROM_CACHE || now < cacheTime + cacheTimeout)
			{
				if (verbose) Log.i("getCachedResponse", "Returning cached response");
				return response;
			}
			else
			{
				if (verbose) Log.i("getCachedResponse", "Returning null. Cache out of date.");
				synchronized(data)
				{
					data.remove(key);
					synchronized(dates)
					{
						dates.remove(key);
					}
				}
				
			}
		}
		
		return null;
	}
	
	/**
	 * Cache a response
	 * @param response the response value
	 * @param options the options used to get the value. This is used as the key.
	 * @return the key used to cache the response.
	 */
	public String cacheResponse(Object response, AjaxOptions options)
	{
		String key = String.format(Locale.US, "%s::%s::%s::%s", options.dataType(), (options.type() == null ? "GET" : options.type()), options.url(), (options.data() == null ? "" : options.data().toString()));
		if (verbose)
		{
			Log.i("cacheResponse", "Key = " + key);
			Log.i("cacheResponse", "Response = " + (response == null ? "null" : response.toString()));
		}
		size += safeSizeOf(key, response);
		synchronized(data)
		{
			Object previous = data.put(key, response);
			synchronized(dates)
			{
				dates.put(key, new Date());
			}
			if (previous != null)
				size -= safeSizeOf(key, response);
		}
		if (options.cacheTimeout() == TIMEOUT_NEVER_CLEAR_FROM_CACHE)
		{
			synchronized(alwaysData)
			{
				alwaysData.put(key, response);
			}
		}
		trimToSize(maxSize);
		return key;
	}
	
	/**
	 * Print the cache contents
	 */
	public void printCache()
	{
		if (data.size() == 0)
		{
			Log.i("printCache", "Cache is empty");
			return;
		}
		Map<String, Object> copy = new HashMap<String, Object>(data);
		for (Entry<String, ?> entry : copy.entrySet())
		{
			Log.i("printCache", String.format(Locale.US, "%s : %s", entry.getKey(), entry.getValue().toString()));
		}
	}
	
	/**
	 * Get a copy of the current cache to see what can be added or removed
	 * @return
	 * @see #removeEntry(String)
	 * @see #cacheResponse(Object, AjaxOptions)
	 */
	public Map<String, Object> getCache()
	{
		return new HashMap<String, Object>(data);
	}
	
	/**
	 * Remove the entry for the given String key
	 * @param key
	 * @see #printCache()
	 * @see #getCache()
	 */
	public void removeEntry(String key)
	{
		synchronized(data)
		{
			Object previous = data.remove(key);
			if (previous != null)
				size -= safeSizeOf(key, previous);
			synchronized(dates)
			{
				dates.remove(key);
			}
		}
		synchronized(alwaysData)
		{
			alwaysData.remove(key);
		}
	}
	
	/**
	 * Remove entry for the given AjaxOptions key
	 * @param options
	 */
	public void removeEntry(AjaxOptions options)
	{
		String key = String.format(Locale.US, "%s::%s::%s", options.dataType(), (options.type() == null ? "GET" : options.type()), options.url());
		synchronized(data)
		{
			Object previous = data.remove(key);
			if (previous != null)
				size -= safeSizeOf(key, previous);
			synchronized(dates)
			{
				dates.remove(key);
			}
		}
		synchronized(alwaysData)
		{
			alwaysData.remove(key);
		}
	}
	
	/**
	 * Clears all cache entries.
	 */
	public void clearCache()
	{
		synchronized(data)
		{
			data.clear();
			synchronized(dates)
			{
				dates.clear();
			}
			size = 0;
			synchronized(alwaysData)
			{
				if (sizeListener == null)
				{
					data.putAll(alwaysData);
					size = data.size();
				}
				else
				{
					String k;
					Object v;
					for (Entry<String, ? super Object> entry : alwaysData.entrySet())
					{
						k = entry.getKey();
						v = entry.getValue();
						data.put(k, v);
						size += safeSizeOf(k, v);
					}
				}
				
				
			}
		}
		if (this.onCacheClearedListener != null)
			this.onCacheClearedListener.onCacheCleared();
	}
	
	/**
	 * This is the exact method used by android.support.v4.util.LruCache.java
	 * @param key
	 * @param value
	 * @return
	 */
	private int safeSizeOf(String key, Object value)
	{
		int result = sizeOf(key, value);
		if (result < 0)
			throw new IllegalStateException("Negative size: " + key + "=" + value);
		return result;
	}
	
	/**
	 * Returns the size of the entry for {@code key} and {@code value} in
	 * user-defined units. The default implementation returns 1 so that size
	 * is the number of entries and max size is the maximum number of entries.
	 * If {@link #sizeListener} is not {@code null}, its implementation will
	 * override the default behavior.
	 * 
	 * <p>An entry's size must not change while it is in the cache.
	 * @param key
	 * @param value
	 * @return 
	 */
	private int sizeOf(String key, Object value)
	{
		if (sizeListener != null)
			return sizeListener.getSizeOf(key, value);
		return 1;
	}
	
	/**
	 * This is derived from android.support.v4.util.LruCache.java
	 * @param maxSize
	 */
	private void trimToSize(int maxSize)
	{
		if (maxSize == -1)
			return;
		
		synchronized(data)
		{
			if (!data.isEmpty())
			{
				int _size = size;
				while (_size > maxSize)
				{
					Entry<String, ?> toEvict = data.entrySet().iterator().next();
					if (toEvict == null)
						break;//data is empty. if size is not 0 then throw an error below
					String key = toEvict.getKey();
					Object value = toEvict.getValue();
					
					_size -= safeSizeOf(key, value);
					synchronized(alwaysData)
					{
						if (alwaysData.containsKey(key))
							break;//this data is never removed automatically.
					}
					
					synchronized(data)
					{
						data.remove(key);
						
						synchronized(dates)
						{
							dates.remove(key);
						}
					}
				}
				size = _size;
			}
		}
		
		if (size < 0 || (data.isEmpty() && size != 0))
			throw new IllegalStateException(getClass().getName() + ".sizeOf() is reporting inconsistent results!");
	}
	
	/**
	 * Callback for clearing the Ajax Cache
	 * @author Phil Brown
	 * @since 1:01:24 PM Dec 3, 2013
	 *
	 */
	public interface OnCacheClearedListener
	{
		/** Called after the cache has been cleared. */
		public void onCacheCleared();
	}
	
	public interface OnObjectCachedListener
	{
		public int getSizeOf(String key, Object value);
	}
	
}
