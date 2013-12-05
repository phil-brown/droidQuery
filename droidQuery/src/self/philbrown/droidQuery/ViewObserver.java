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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import android.view.View;

/**
 * Provides Property Change Listening to simulate bindings
 * @author Phil Brown
 *
 */
public class ViewObserver implements PropertyChangeListener
{

	/** The function to call when the interface's method is invoked. */
	private Function function;
	
	/**
	 * Constructor
	 * @param droidQuery an instance of droidQuery
	 * @param function the function to call when the value changes. Will include a {@link Observation}
	 * Object with information about the KVO operation.
	 */
	public ViewObserver(Function function)
	{
		this.function = function;
	}
	

	@Override
	public void propertyChange(PropertyChangeEvent event) 
	{
		Observation observation = new Observation(event);
		function.invoke($.with((View) event.getSource()), observation);
	}
	
	/**
	 * Represents an observation event that occured.
	 */
	public static class Observation
	{
		/** The old value prior the this Observation */
		public Object oldValue;
		/** The new value */
		public Object newValue;
		/** The name of the property that has changed from {@code oldValue} to {@code newValue}. */
		public String property;
		
		/**
		 * Constructor. Private since it is only used locally.
		 * @param event
		 */
		private Observation(PropertyChangeEvent event)
		{
			oldValue = event.getOldValue();
			newValue = event.getNewValue();
			property = event.getPropertyName();
		}
	}

}
