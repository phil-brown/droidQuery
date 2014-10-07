package self.philbrown.droidQuery;

/**
 * Relates to the interpolator used for <em>droidQuery</em> animations
 * @author Phil Brown
 */
public enum Easing
{
	/** Rate of change starts out slowly and then accelerates. */
	ACCELERATE,
	/** Rate of change starts and ends slowly but accelerates through the middle. */
	ACCELERATE_DECELERATE,
	/** change starts backward then flings forward. */
	ANTICIPATE,
	/** change starts backward, flings forward and overshoots the target value, then finally goes back to the final value. */
	ANTICIPATE_OVERSHOOT,
	/** change bounces at the end. */
	BOUNCE,
	/** Rate of change starts out quickly and and then decelerates. */
	DECELERATE,
	/** Rate of change is constant. */
	LINEAR,
	/** change flings forward and overshoots the last value then comes back. */
	OVERSHOOT
}