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

import android.content.Context;
import android.graphics.BitmapFactory.Options;

/**
 * A multi-purpose callbacks list object that provides a powerful way to manage callback lists.
 * @author Phil Brown
 */
public class Callbacks 
{
	/** Callback functions for this instance of {@code Callbacks}. */
	private List<Function> functions;
	/** {@code true} if the Callbacks are enabled. {@code false} if it is disabled. */
	private boolean isEnabled;
	/** {@code true} if the Callbacks Object has fired. {@code false} otherwise. */
	private boolean hasFired;
	/** {@code true} if the Callbacks Object is locked. {@code false} otherwise. */
	private boolean isLocked;
	/** {@code true} if the Callbacks Object should save the arguments after locking. Otherwise {@code false}. */
	private boolean memory = false;
	/** saved arguments */
	private Object[] memArgs;
	/** {@code true} if {@link #memArgs} has been set. */
	private boolean memArgsExists = false;
	/** {@code true} if the {@link Options#ONCE} flag is set. */
	private boolean once = false;
	/** {@code true} if a callback can only be added once */
	private boolean unique = false;
	/** The variable to pass as the droidQuery argument to Callback Functions. May be {@code null}. */
	private $ droidQuery;

	/** 
	 * Constructor 
	 */
	public Callbacks()
	{
		functions = new ArrayList<Function>();
	}
	
	/**
	 * Constructor 
	 * @param context used to create the {@link self.philbrown.droidQuery.$ droidQuery} Object
	 * that will be passed to callback functions
	 */
	public Callbacks(Context context)
	{
		this();
		this.droidQuery = $.with(context);
	}
	
	/**
	 * Constructor 
	 * @param droidQuery the <em>droidQuery</em> Object that will be passed to callback functions
	 */
	public Callbacks($ droidQuery)
	{
		this();
		this.droidQuery = droidQuery;
	}
	
	/**
	 * Constructor. Accepts options to configure this instance.
	 * @param opt the Callbacks Options
	 */
	public Callbacks(CallbacksOptions opt)
	{
		this();
		if (opt.memory())
			this.memory = true;
		if (opt.once())
			this.once = true;
		if (opt.unique())
			this.unique = true;
	}
	
	/**
	 * Constructor 
	 * @param context used to create the {@link self.philbrown.droidQuery.$ droidQuery} Object
	 * that will be passed to callback functions
	 * @param opt the Callbacks Options
	 */
	public Callbacks(Context context, CallbacksOptions opt)
	{
		this(opt);
		this.droidQuery = $.with(context);
	}

	/**
	 * Constructor 
	 * @param droidQuery the <em>droidQuery</em> Object that will be passed to callback functions
	 * @param opt the Callbacks Options
	 */
	public Callbacks($ droidQuery, CallbacksOptions opt)
	{
		this(opt);
		this.droidQuery = droidQuery;
	}
	
	/**
	 * Add a callback or a collection of callbacks to a callback list.
	 * @param function a function to add
	 */
	public void add(Function function)
	{
		if (isEnabled)
		{
			if (unique && !functions.contains(function))
				functions.add(function);
		}
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
			if (unique && !functions.contains(f))
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
		{
			if (unique)
			{
				for (Function f : _functions)
				{
					if (!functions.contains(f))
						functions.add(f);
				}
			}
			else
			{
				functions.addAll(_functions);
			}
		}
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
		if (once && hasFired)
			return;
		if (isLocked)
		{
			if (memory)
			{
				if (memArgsExists)
				{
					for (Function f : functions)
					{
						f.invoke(droidQuery, memArgs);
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
			f.invoke(droidQuery, args);
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
	
	/** Callback options */
	public static class CallbacksOptions
	{
		/** Saves the argument after locking */
		private boolean memory;
		/** Ensures the callback list can only be fired once */
		private boolean once;
		/**  Ensures a callback can only be added once (so there are no duplicates in the list). */
		private boolean unique;
		
		/**
		 * Set the {@code memory} option. If {@code true}, the Callbacks will save the arguments after locking
		 * @param mem
		 * @return this
		 */
		public CallbacksOptions memory(boolean mem)
		{
			memory = mem;
			return this;
		}
		
		/**
		 * @return {@code true} if {@code memory} is set to true
		 * @see #memory(boolean)
		 */
		public boolean memory()
		{
			return memory;
		}
		
		/**
		 * Set the {@code once} option. If {@code true}, the Callbacks will only be fired once.
		 * @param once
		 * @return this
		 */
		public CallbacksOptions once(boolean once)
		{
			this.once = once;
			return this;
		}
		
		/**
		 * @return {@code true} if {@code once} is set to true
		 * @see #once(boolean)
		 */
		public boolean once()
		{
			return once;
		}
		
		/**
		 * Set the {@code unique} option. If {@code true}, the Callbacks will not allow multiple
		 * copies of the same Function to be called
		 * @param unique
		 * @return this
		 */
		public CallbacksOptions unique(boolean unique)
		{
			this.unique = unique;
			return this;
		}
		
		/**
		 * @return {@code true} if {@code unique} is set to true
		 * @see #unique(boolean)
		 */
		public boolean unique()
		{
			return unique;
		}
		
	}
}
