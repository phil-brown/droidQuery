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
 * @see $#map(java.util.Map.Entry...)
 */
public class QuickMap<K extends Object, V extends Object> extends HashMap<K,V>
{

	/**
	 * generated serial version for HashMap subclass
	 */
	private static final long serialVersionUID = 4189306358484466596L;

	/**
	 * Default Constructor
	 */
	public QuickMap()
	{
		super();
	}
	
	/**
	 * Constructor<br>
	 * Creates a new Mapping of the given entries
	 * @param entries the key-value pairings used to populate the map
	 */
	public QuickMap(Entry<K,V>... entries)
	{
		this();
		for (Entry<K,V> entry : entries)
		{
			this.put(entry);
		}
	}
	
	/**
	 * Shortcut to the constructor
	 * @param entries the key-value pairings used to populate the map
	 * @return a new instance of a QuickMap
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static QuickMap qm(Entry<?,?>... entries)
	{
		return new QuickMap(entries);
	}
	
	/**
	 * Include a Key-Value pairing in this QuickMap
	 * @param entry the entry to include
	 * @return the value of any previous mapping with the specified key or {@code null} if there was 
	 * no such mapping.
	 */
	public V put(Entry<K,V> entry)
	{
		return this.put(entry.getKey(), entry.getValue());
	}
	
	
}
