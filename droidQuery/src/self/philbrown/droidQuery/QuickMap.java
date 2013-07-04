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

import java.util.HashMap;

/**
 * Allows for quicker Map creation
 * @author Phil Brown
 *
 * @param <K>
 * @param <V>
 */
public class QuickMap<K extends Object, V extends Object> extends HashMap<K,V>
{

	/**
	 * generated serial version for HashMap subclass
	 */
	private static final long serialVersionUID = 4189306358484466596L;

	public QuickMap()
	{
		super();
	}
	
	public QuickMap(Entry<K,V>... entries)
	{
		this();
		for (Entry<K,V> entry : entries)
		{
			this.put(entry);
		}
	}
	
	/**
	 * Shortcut to constructor
	 * @param entries
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static QuickMap qm(Entry<?,?>... entries)
	{
		return new QuickMap(entries);
	}
	
	public V put(Entry<K,V> entry)
	{
		return this.put(entry.getKey(), entry.getValue());
	}
	
	
}
