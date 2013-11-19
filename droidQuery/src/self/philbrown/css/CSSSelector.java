package self.philbrown.css;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import self.philbrown.droidQuery.$;
import self.philbrown.droidQuery.Function;
import android.content.Context;
import android.util.Log;
import android.view.View;

public class CSSSelector 
{
	public static $ makeSelection(Context context, String selector)
	{
		return makeSelection($.with(context), selector);
	}
	
	public static $ makeSelection(View parent, String selector)
	{
		return makeSelection($.with(parent.getContext()), selector);
	}
	
	//TODO: this should be used recursively!!!! for things like element>element, etc, since droidQuery will change the selection
	private static $ makeSelection($ droidQuery, String selector)
	{
		//### class selector
		if (selector.startsWith("."))
		{
			String sel = selector.substring(1);
			if (sel == null) sel = "";
			if (sel.contains("."))
			{
				//expected that the full class is provided
				droidQuery.selectByType(sel);
			}
			else
			{
				//check android.view
				try {
					Class<?> clazz = Class.forName(String.format(Locale.US, "android.view%s", selector));
					droidQuery.selectByType(clazz);
				}
				catch (Throwable t)
				{
					//check android.widget
					try {
						Class<?> clazz = Class.forName(String.format(Locale.US, "android.widget%s", selector));
						droidQuery.selectByType(clazz);
					}
					catch (Throwable t2)
					{
						//check android.webkit
						try {
							Class<?> clazz = Class.forName(String.format(Locale.US, "android.webkit%s", selector));
							droidQuery.selectByType(clazz);
						}
						catch (Throwable t3)
						{
							//could not select class
							Log.w("droidQuery", String.format(Locale.US, "Could not select class %s.", sel));
						}
					}
				}
				
			}
			
			
		}
		//### id selector
		else if (selector.startsWith("#"))
		{
			String classname = "";
			try
			{
				classname = String.format(Locale.US, "%s.R.id", droidQuery.context().getPackageName());
				Class<?> clazz = Class.forName(classname);
				String sel = selector.substring(1);
				if (sel == null) sel = "";
				try
				{
					Field f = clazz.getField(sel);
					int id = (Integer) f.get(null);
					droidQuery.id(id);
				}
				catch (Throwable t)
				{
					Log.w("droidQuery", String.format(Locale.US, "No resource found for R.id.%s", sel));
				}
			}
			catch (Throwable t)
			{
				Log.w("droidQuery", String.format(Locale.US, "No class found for %s", classname));
			}
			//TODO ensure this case is handled:  #news:target
		}
		//### all selector
		else if (selector.equals("*"))
		{
			droidQuery.selectAll();
		}
		//### attribute selectors. Added for android: attributes can be named like: @color/green
		else if (selector.startsWith("["))
		{
			if (!selector.endsWith("]"))
			{
				Log.w("droidQuery", String.format(Locale.US, "Invalid css selector: %s", selector));
				return droidQuery;
			}
			String sel = selector.substring(1, selector.length()-1);
			
			List<View> selection = new ArrayList<View>();
			
			//### startsWith
			if (sel.contains("|="))
			{
				String[] split = sel.split("|=");
				String attribute = split[0];
				String value = split[1];
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute)));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute);
							val = f.get(v);
						}
						catch (Throwable t2)
						{
							//don't print
						}
					}
					if (val != null)
					{
						if (val.toString().startsWith(value))
						{
							selection.add(v);
						}
					}
				}
				
				droidQuery = $.with(selection);
				
			}
			//### contains
			else if (sel.contains("~="))
			{
				String[] split = sel.split("|=");
				String attribute = split[0];
				String value = split[1];
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute)));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute);
							val = f.get(v);
						}
						catch (Throwable t2)
						{
							//don't print
						}
					}
					if (val != null)
					{
						if (value.contains(val.toString()))
						{
							selection.add(v);
						}
					}
				}
				
				droidQuery = $.with(selection);
			}
			//### equals
			else if (sel.contains("="))
			{
				String[] split = sel.split("|=");
				String attribute = split[0];
				String value = split[1];
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute)));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute);
							val = f.get(v);
						}
						catch (Throwable t2)
						{
							//don't print
						}
					}
					if (val != null)
					{
						if (val.toString().equals(value))
						{
							selection.add(v);
						}
					}
				}
				
				droidQuery = $.with(selection);
			}
			//### has attribute
			else
			{
				String[] split = sel.split("|=");
				String attribute = split[0];
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute)));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute);
							val = f.get(v);
						}
						catch (Throwable t2)
						{
							//don't print
						}
					}
					if (val != null)
					{
						selection.add(v);
					}
				}
				
				droidQuery = $.with(selection);
			}
			
		}
		else if (selector.contains("::"))
		{
			//TODO select selected views
		}
		else if (selector.contains(":"))
		{
			//TODO type-ofs
		}
		else if (selector.contains("["))
		{
			//TODO complex attributes
		}
		else if (selector.contains(","))
		{
			//TODO complex attributes
		}
		else if (selector.contains("+"))
		{
			//TODO elements
		}
		else if (selector.contains(" "))
		{
			//TODO elements
		}
		else if (selector.contains(">"))
		{
			//TODO elements
		}
		
		
		return droidQuery;
		
	}
	
	/** 
	 * Capitalizes the first letter of the given string.
	 * @param string the string whose first letter should be capitalized
	 * @return the given string with its first letter capitalized
	 * @throws NullPointerException if the string is null or empty
	 */
	private static String capitalize(String string)
	{
		if (string == null || string.trim().length() == 0)
			throw new NullPointerException("Cannot handle null or empty string");
		
		StringBuilder strBuilder = new StringBuilder(string);
		strBuilder.setCharAt(0, Character.toUpperCase(strBuilder.charAt(0)));
		return strBuilder.toString();
	}
}
