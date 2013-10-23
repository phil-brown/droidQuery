package self.philbrown.droidQuery;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

/**
 * Used for caching Ajax Responses
 * @author Phil Brown
 * @since 8:25:44 AM Oct 4, 2013
 *
 */
public class AjaxCache
{
	/** singleton instance */
	private static AjaxCache self;
	
	/** Stores data */
	private static Map<String, ? super Object> data;
	/** Stores dates */
	private static Map<String, Date> dates;
	/** {@code true} to show verbose output. Otherwise {@code false}. */
	private boolean verbose;
	
	/**
	 * Constructor.
	 */
	private AjaxCache()
	{
		data = new HashMap<String, Object>();
		dates = new HashMap<String, Date>();
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
	 * @param options the options used to store the cache entry, or an options with the same data type, type, and url.
	 * @return the cached Object
	 */
	public Object getCachedResponse(AjaxOptions options)
	{
		String key = String.format(Locale.US, "%s::%s::%s", options.dataType(), (options.type() == null ? "GET" : options.type()), options.url());

		Object response;
		Date date;
		synchronized(data)
		{
			response = data.get(key);
			synchronized(dates)
			{
				date = dates.get(key);
			}
		}
		if (verbose)
		{
			Log.i("getCachedResponse", "Key = " + key);
			Log.i("getCachedResponse", "Response = " + (response == null ? "null" : response.toString()));
			Log.i("getCachedResponse", "Date = " + (date == null ? "null" : date.toString()));
		}
		
		if (response != null && date != null)
		{
			long cacheTime = date.getTime();
			long now = new Date().getTime();
			long cacheTimeout = options.cacheTimeout();
			if (now < cacheTime + cacheTimeout)
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
	 */
	public void cacheResponse(Object response, AjaxOptions options)
	{
		String key = String.format(Locale.US, "%s::%s::%s", options.dataType(), (options.type() == null ? "GET" : options.type()), options.url());
		if (verbose)
		{
			Log.i("cacheResponse", "Key = " + key);
			Log.i("cacheResponse", "Response = " + (response == null ? "null" : response.toString()));
		}
		synchronized(data)
		{
			data.put(key, response);
			synchronized(dates)
			{
				dates.put(key, new Date());
			}
		}
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
			data.remove(key);
			synchronized(dates)
			{
				dates.remove(key);
			}
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
			data.remove(key);
			synchronized(dates)
			{
				dates.remove(key);
			}
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
		}
	}
	
}
