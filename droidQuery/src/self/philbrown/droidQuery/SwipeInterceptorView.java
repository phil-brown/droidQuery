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

import self.philbrown.droidQuery.SwipeDetector.SwipeListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * This View catches both swipe events and normal view events, and is a drop-in replacement for 
 * RelativeLayout.
 * @author Phil Brown
 *
 */
public class SwipeInterceptorView extends RelativeLayout
{


	private SwipeDetector swiper = null;
	
	public void setSwipeListener(SwipeListener listener)
	{
		if (swiper == null)
			swiper = new SwipeDetector(listener);
	}
	
	public SwipeInterceptorView(Context context) {
		super(context);
	}

	public SwipeInterceptorView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SwipeInterceptorView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		boolean swipe = false, touch = false;
		if (swiper != null)
			swipe = swiper.onTouch(this, e);
		touch = super.onTouchEvent(e);
		return swipe || touch;
	}

}
