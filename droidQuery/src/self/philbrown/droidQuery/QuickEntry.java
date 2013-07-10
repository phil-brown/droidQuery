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

import java.util.Map.Entry;

/**
 * Allows the quick insertion of Map Entries
 * @author Phil Brown
 * @see $#entry(Object, Object)
 */
public class QuickEntry<K extends Object, V extends Object> implements Entry<K,V> 
{
	/** Entry Key */
	protected K key;
	/** Entry Value */
	protected V value;
	
	/**
	 * Constructs a new QuickEntry
	 * @param key the Entry key
	 * @param value the Value key
	 */
	public QuickEntry(K key, V value)
	{
		this.key = key;
		this.value = value;
	}
	
	/**
	 * Shortcut to the constructor
	 * @param key the Entry key
	 * @param value the Entry value
	 * @return the new QuickEntry instance
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static QuickEntry qe(Object key, Object value)
	{
		return new QuickEntry(key, value);
	}
	
	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V object) {
		V temp = value;
		value = object;
		return temp;
	}

}
