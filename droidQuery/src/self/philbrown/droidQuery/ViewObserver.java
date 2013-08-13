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
