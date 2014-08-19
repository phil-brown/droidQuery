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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of all registered events
 * @author Phil Brown
 *
 */
public class EventCenter 
{
	/**
	 * Mapping of Scope to a Mapping of Event Names and a List of functions that respond to events
	 * with the name for the scope.
	 */
	private static volatile Map<Object, Map<String, List<Function>>> receivers = new HashMap<Object, Map<String, List<Function>>>();

	/**
	 * Global Scope
	 */
	private static volatile Object global = new Object();
	
	/**
	 * Constructor
	 */
	private EventCenter()
	{
		
	}
	
	/**
	 * Bind a function to the name of a trigger for the given scope
	 * @param trigger the event name
	 * @param function the function to invoke
	 * @param scope the scope. Use {@code null} for global scope.
	 */
	public static void bind(String trigger, Function function, Object scope)
	{
		synchronized(receivers) {
			if (scope == null) scope = global;
			Map<String, List<Function>> triggers = receivers.get(scope);
			if (triggers == null)
				triggers = new HashMap<String, List<Function>>();
			List<Function> functions = triggers.get(trigger);
			if (functions == null)
				functions = new ArrayList<Function>();
			functions.add(function);
			triggers.put(trigger, functions);
			receivers.put(scope, triggers);
		}
		
		
	}
	
	/**
	 * Unbind a function for the given event trigger and scope
	 * @param trigger the name of the event
	 * @param function the function to remove
	 * @param scope the scope of the bound event
	 */
	public static void unbind(String trigger, Function function, Object scope)
	{
		synchronized(receivers) {
			if (scope == null) scope = global;
			Map<String, List<Function>> triggers = receivers.get(scope);
			if (triggers == null)
				return;
			List<Function> functions = triggers.get(trigger);
			if (functions == null)
				return;
			functions.remove(function);
			triggers.put(trigger, functions);
			receivers.put(scope, triggers);
		}
	}
	
	/**
	 * Trigger an event. Note that the {@code droidQuery} function parameter will be {@code null}.
	 * @param text the name of the event
	 * @param args the arguments to pass to the registered functions
	 * @param scope the scope of the event
	 */
	public static void trigger(String text, Map<String, Object> args, Object scope)
	{
		synchronized(receivers) {
			if (scope == null) scope = global;
			Map<String, List<Function>> triggers = receivers.get(scope);
			if (triggers == null)
				return;
			List<Function> functions = triggers.get(text);
			if (functions == null)
				return;
			
			for (Function f : functions)
			{
				f.invoke(null, text, args);
			}
		}
		
	}
	
	/**
	 * Trigger an event.
	 * @param droidQuery the instance of droidQuery to pass as a parameter
	 * @param text the name of the event
	 * @param args the arguments to pass to the registered functions
	 * @param scope the scope of the event
	 */
	public static void trigger($ droidQuery, String text, Map<String, Object> args, Object scope)
	{
		synchronized(receivers) {
			if (scope == null) scope = global;
			Map<String, List<Function>> triggers = receivers.get(scope);
			if (triggers == null)
				return;
			List<Function> functions = triggers.get(text);
			if (functions == null)
				return;
			
			for (Function f : functions)
			{
				f.invoke(droidQuery, text, args);
			}
		}
		
	}
	
}
