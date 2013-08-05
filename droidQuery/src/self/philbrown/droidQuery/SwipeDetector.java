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

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Detect Swipes on a per-view basis. Based on original code by Thomas Fankhauser on StackOverflow.com,
 * with adaptations by other authors (see link).
 * @author Phil Brown
 * @see <a href="http://stackoverflow.com/questions/937313/android-basic-gesture-detection">android-basic-gesture-detection</a>
 */
public class SwipeDetector implements View.OnTouchListener
{
	/** Swipe Direction */
	public static enum Direction
	{
		RIGHT, DOWN, UP, LEFT, START, STOP
	}
	
	/**
	 * The minimum distance a finger must travel in order to register a swipe event.
	 */
	private int minSwipeDistance = 100;
	
	/** Maintains a reference to the first detected down touch event. */
    private float downX, downY;
    
    /** Maintains a reference to the first detected up touch event. */
    private float upX, upY;
    
    /** The View on which the swipe action is registered */
    private View view;
    
    /** provides access to size and dimension contants */
    private ViewConfiguration config;
    
    /**
     * provides callbacks to a listener class for various swipe gestures.
     */
    private SwipeListener listener;
    
    public SwipeDetector(SwipeListener listener)
    {
    	this.listener = listener;
    }
	
	/**
	 * {@inheritDoc}
	 */
    public boolean onTouch(View v, MotionEvent event) 
    {
    	this.view = v;
    	
    	//swipe distance is scaled on a per-view basis (maybe?)
    	if (config == null)
    	{
    		config = ViewConfiguration.get(v.getContext());
    		minSwipeDistance = config.getScaledTouchSlop();
    	}
    	
    	
    	
        switch(event.getAction())
        {
        case MotionEvent.ACTION_DOWN:
            downX = event.getX();
            downY = event.getY();
            if (listener != null)
        	{
        		listener.onStartSwipe(v);
        	}
            return true;
        case MotionEvent.ACTION_MOVE:
        case MotionEvent.ACTION_UP:
            upX = event.getX();
            upY = event.getY();

            float deltaX = downX - upX;
            float deltaY = downY - upY;
            
            boolean returnVal = false;

            // swipe horizontal?
            if(Math.abs(deltaX) > minSwipeDistance)
            {
                // left or right
                if (deltaX < 0) 
                { 
                	if (listener != null)
                	{
                		listener.onRightSwipe(v);
                		returnVal = true;
                	}
                }
                else if (deltaX > 0) 
                { 
                	if (listener != null)
                	{
                		listener.onLeftSwipe(v);
                		returnVal = true;
                	}
                }
            }

            // swipe vertical?
            else if(Math.abs(deltaY) > minSwipeDistance)
            {
                // top or down
                if (deltaY < 0) 
                { 
                	if (listener != null)
                	{
                		listener.onDownSwipe(v);
                		returnVal = true;
                	} 
                }
                else if (deltaY > 0) 
                { 
                	if (listener != null)
                	{
                		listener.onUpSwipe(v);
                		returnVal = true;
                	} 
                }
            }
            if (event.getAction() == MotionEvent.ACTION_UP)
            {
            	if (listener != null)
            	{
            		listener.onStopSwipe(v);
            	}
            }
            return returnVal;
        }
        return false;
    }
    
    /**
     * Simulate a swipe up event
     */
    public void performSwipeUp()
    {
    	if (listener != null)
    		listener.onUpSwipe(view);
    }
    
    /**
     * Simulate a swipe right event
     */
    public void performSwipeRight()
    {
    	if (listener != null)
    		listener.onRightSwipe(view);
    }
    
    /**
     * Simulate a swipe left event
     */
    public void performSwipeLeft()
    {
    	if (listener != null)
    		listener.onLeftSwipe(view);
    }
    
    /**
     * Simulate a swipe down event
     */
    public void performSwipeDown()
    {
    	if (listener != null)
    		listener.onDownSwipe(view);
    }
	
	/**
	 * Provides callbacks to a registered listener for swipe events in {@link SwipeDetector}
	 * @author Phil Brown
	 */
	public interface SwipeListener
	{
		/** Callback for registering a new swipe motion from the bottom of the view toward its top. */
		public void onUpSwipe(View v);
		/** Callback for registering a new swipe motion from the left of the view toward its right. */
	    public void onRightSwipe(View v);
	    /** Callback for registering a new swipe motion from the right of the view toward its left. */
	    public void onLeftSwipe(View v);
	    /** Callback for registering a new swipe motion from the top of the view toward its bottom. */
	    public void onDownSwipe(View v);
	    /** Callback for registering that a new swipe motion has begun */
	    public void onStartSwipe(View v);
	    /** Callback for registering that a swipe motion has ended */
	    public void onStopSwipe(View v);
	}
}
