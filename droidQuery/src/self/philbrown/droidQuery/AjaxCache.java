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
	
	public AjaxCache verbose(boolean verbose)
	{
		this.verbose = verbose;
		return this;
	}
	
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
	
	public void printCache()
	{
		if (data.size() == 0)
		{
			Log.i("printCache", "Cache is empty");
			return;
		}
		for (Entry<String, ?> entry : data.entrySet())
		{
			Log.i("printCache", String.format(Locale.US, "%s : %s", entry.getKey(), entry.getValue().toString()));
		}
	}
	
}
