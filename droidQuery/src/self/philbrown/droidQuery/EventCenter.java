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
	private static Map<Object, Map<String, List<Function>>> receivers = new HashMap<Object, Map<String, List<Function>>>();

	private static Object global = new Object();
	
	private EventCenter()
	{
		
	}
	
	/**
	 * Functions receive name of trigger and a mapping of objects
	 * @param trigger
	 * @param function
	 * @param scope
	 */
	public static void bind(String trigger, Function function, Object scope)
	{
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
	
	public static void unbind(String trigger, Function function, Object scope)
	{
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
	
	public static void trigger(String text, Map<String, Object> args, Object scope)
	{
		if (scope == null) scope = global;
		Map<String, List<Function>> triggers = receivers.get(scope);
		if (triggers == null)
			return;
		List<Function> functions = triggers.get(text);
		if (functions == null)
			return;
		
		for (Function f : functions)
		{
			f.invoke(text, args);
		}
	}
	
}
