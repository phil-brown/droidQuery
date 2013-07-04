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

import java.util.ArrayList;
import java.util.List;

/**
 * A multi-purpose callbacks list object that provides a powerful way to manage callback lists.
 * @author Phil Brown
 *
 */
public class Callbacks 
{
	/** Callback options */
	public static enum Options
	{
		/** saves the argument after locking. */
		MEMORY
	}
	
	/** Callback functions for this instance of {@code Callbacks}. */
	private List<Function> functions;
	
	private boolean isEnabled;
	private boolean hasFired;
	private boolean isLocked;
	private boolean memory = false;
	private Object[] memArgs;
	private boolean memArgsExists = false;

	/** Constructor */
	public Callbacks()
	{
		functions = new ArrayList<Function>();
	}
	
	public Callbacks(Options opt)
	{
		this();
		if (opt == Options.MEMORY)
			this.memory = true;
	}
	
	/**
	 * Add a callback or a collection of callbacks to a callback list.
	 * @param function a function to add
	 */
	public void add(Function function)
	{
		if (isEnabled)
			functions.add(function);
	}
	
	/**
	 * Add a callback or a collection of callbacks to a callback list.
	 * @param function an array of functions to add
	 */
	public void add(Function[] _functions)
	{
		if (!isEnabled)
			return;
		for (Function f : _functions)
		{
			functions.add(f);
		}
	}
	
	/**
	 * Add a callback or a collection of callbacks to a callback list.
	 * @param function a list of functions to add
	 */
	public void add(List<Function> _functions)
	{
		if (isEnabled)
			functions.addAll(_functions);
	}
	
	/**
	 * Disable a callback list from doing anything more.
	 */
	public void disable()
	{
		isEnabled = false;
	}
	
	/**
	 * Determine if the callbacks list has been disabled.
	 * @return
	 */
	public boolean disabled()
	{
		return !isEnabled;
	}
	
	/**
	 * Remove all of the callbacks from a list.
	 */
	public void empty()
	{
		if (isEnabled)
			functions.clear();
	}
	
	/**
	 * Allow a callback list to do more.
	 */
	public void enable()
	{
		isEnabled = true;
	}
	
	/**
	 * Determine whether or not the callbacks list has been disabled.
	 * @return
	 */
	public boolean enabled()
	{
		return isEnabled;
	}
	
	/**
	 * Call all of the callbacks with the given arguments
	 */
	public void fire()
	{
		fire((Object[]) null);
	}
	
	/**
	 * Call all of the callbacks with the given arguments
	 */
	public void fire(Object... args)
	{
		if (disabled())
			return;
		if (isLocked)
		{
			if (memory)
			{
				if (memArgsExists)
				{
					for (Function f : functions)
					{
						f.invoke(memArgs);
					}
					hasFired = true;
				}
				else
				{
					memArgs = args;
					memArgsExists = true;
				}
			}
			else
			{
				return;
			}
		}
		for (Function f : functions)
		{
			f.invoke(args);
		}
		hasFired = true;
	}
	
	/**
	 * Determine if the callbacks have already been called at least once.
	 * @return
	 */
	public boolean fired()
	{
		return hasFired;
	}
	
	/**
	 * Call all callbacks in a list with the given context and arguments.
	 * @param args
	 */
	public void fireWith(Object... args)
	{
		fire(args);
	}
	
	/**
	 * Determine whether a supplied callback is in a list
	 * @param function
	 * @return
	 */
	public boolean has(Function function)
	{
		return functions.contains(function);
	}
	
	/**
	 * Lock a callback list in its current state.
	 */
	public void lock()
	{
		isLocked = true;
	}
	
	/**
	 * Determine if the callbacks list has been locked.
	 * @return
	 */
	public boolean locked()
	{
		return isLocked;
	}
	
	/**
	 * Remove a callback or a collection of callbacks from a callback list.
	 * @param function
	 */
	public void remove(Function function)
	{
		if (isEnabled)
			functions.remove(function);
	}
	
	/**
	 * Unlocks a callback, allowing further state changes
	 */
	public void unlock()
	{
		isLocked = false;
	}
	
}
