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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Used for dynamically creating instances of interfaces, then executing a Function when the instance's
 * method is called
 * @author Phil Brown
 *
 */
public class EventHandlerCreator implements InvocationHandler 
{

	/** The arguments to pass to the Function */
	private Object[] args;
	/** The function to call when the interface's method is invoked. */
	private Function function;
	/** Contains the droidQuery to pass to the function. */
	private $ droidQuery;
	
	/**
	 * Constructor
	 * @param function the function to call instead of the interface's main method
	 * @param args the arguments to pass to the function
	 */
	public EventHandlerCreator($ droidQuery, Function function, Object... args)
	{
		this.droidQuery = droidQuery;
		this.function = function;
		this.args = args;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		try
		{
			function.invoke(droidQuery, this.args);
			return true;
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			return null;
		}
	}

}
