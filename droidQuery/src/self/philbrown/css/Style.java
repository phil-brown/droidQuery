package self.philbrown.css;

import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import self.philbrown.droidQuery.$;

/**
 * CSS Style
 * @author Phil Brown
 * @since 7:15:27 PM Oct 29, 2013
 *
 */
public class Style 
{
	/** Style attributes */
	protected Map<String, String> attributes;
	/** Style name */
	protected String name;

	public static enum Type
	{
		/** . prefix */
		CLASS('.'), 
		/** no prefix */
		VIEW('\u0000'), //null char is '\u0000'. 
		/** # prefix */
		LABEL('#'),
		/** {@literal @} prefix*/
		SPECIAL('@');
		
		char prefix;
		
		Type(char prefix)
		{
			this.prefix = prefix;
		}
		
		
	}
	
	public static Type getTypeForPrefix(char prefix)
	{
		if (prefix == '\u0000')
			return Type.VIEW;
		Type[] types = Type.values();
		for (Type t : types)
		{
			if (prefix == t.prefix)
				return t;
		}
		return null;
	}
	
	public Style(Type type, Map<String, String> data)
	{
		attributes = data;
	}
	
	@SuppressWarnings("unchecked")
	public Style(Type type, JSONObject data) throws JSONException
	{
		attributes = (Map<String, String>) $.map(data);
	}
	
	@Override
	public boolean equals(Object object)
	{
		if (object.getClass() != getClass())
		{
			return false;
		}
		Set<String> theseKeys = attributes.keySet();
		Style that = (Style) object;
		Set<String> thoseKeys = that.attributes.keySet();
		if (theseKeys.equals(thoseKeys))
		{
			for (String key : theseKeys)
			{
				Object o0 = attributes.get(key);
				Object o1 = that.attributes.get(key);
				if (!o0.equals(o1))
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public Object get(String key)
	{
		return attributes.get(key);
	}
	
	public void set(String key, String value)
	{
		attributes.put(key, value);
	}
	
	
	
}
