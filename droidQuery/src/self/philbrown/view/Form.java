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

package self.philbrown.view;

import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Locale;

import self.philbrown.droidQuery.$;
import self.philbrown.droidQuery.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.AbsSpinner;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Form ViewGroup for simplifying form validation, encoding, and actions.
 * @author Phil Brown
 * @since 11:54:15 PM Nov 11, 2013
 *
 */
public class Form extends LinearLayout
{
	/**
	 * {@code true} if the form should validate (default). Otherwise {@code false}.
	 */
	protected boolean validate;
	/** Name of the method in the view's context to call. Parameters include this Form and the boolean 
	 * specifying if it succeeded to validate.*/
	protected String action;
	/** {@code true} to print debug info. Otherwise {@code false}. */
	protected boolean debug;
	
	/**
	 * Getter fields used to get the state of the input. Used for validation.
	 */
	protected SparseArray<String> getterFields;
	/**
	 * Form children that are marked as required. Used for validation
	 */
	protected SparseArray<Boolean> requiredFields;
	
	/**
	 * Names of the form children. Used for URL encoding via {@link #serialize()}.
	 */
	protected SparseArray<String> childNames;

	/**
	 * Constructor
	 * @param context
	 */
	public Form(Context context) {
		super(context);
		initialize(context, null);
	}

	/**
	 * Constructor
	 * @param context
	 * @param attrs
	 */
	public Form(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context, attrs);
	}

	/**
	 * Constructor
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public Form(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize(context, attrs);
	}
	
	/**
	 * Initializer
	 * @param c
	 * @param attrs
	 */
	protected void initialize(Context c, AttributeSet attrs)
	{
		getterFields = new SparseArray<String>();
		requiredFields = new SparseArray<Boolean>();
		childNames = new SparseArray<String>();
		
		TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.Form_Layout);
		
		action = a.getString(R.styleable.Form_action);
		validate = a.getBoolean(R.styleable.Form_validate, true);
		debug = a.getBoolean(R.styleable.Form_debug, false);
		
		a.recycle();
	}
	
	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof Form.LayoutParams;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int count = getChildCount();
		for (int i = 0; i < count; i++)
		{
			final View child = getChildAt(i);
			Form.LayoutParams lp = (Form.LayoutParams) child.getLayoutParams();
			
			String getter = lp.formField;
			if (getter == null)
			{
				//check known getters
				if (child instanceof CompoundButton)
				{
					if (child instanceof CheckBox)
						getter = "checked";
					else
						getter = "selected";
				}
				else if (child instanceof TextView)
				{
					getter = "text";
				}
				else if (child instanceof AbsSpinner)
				{
					getter = "selectedView";
				}
				else
				{
					Log.w("Form", String.format(Locale.US, "Unknown action for child view %d. \"formField\" attribute required.", i));
				}
			}
			getterFields.append(i, getter);
			requiredFields.append(i, lp.required);
			if (lp.name != null)
				childNames.append(i, lp.name);
			
			boolean submitOnClick = lp.submitOnClick;
			if (submitOnClick)
			{
				child.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if (validate)
							validate(true, v);
					}
				});
			}
		}
	}
	
	/**
	 * Complete a simple form validation.
	 * @return
	 */
	public boolean validate()
	{
		return validate(false, null);
	}
	
	/**
	 * Validate the form.
	 * @param sendAction
	 * @param v
	 * @return
	 */
	private boolean validate(boolean sendAction, final View v)
	{
		Method actionMethod = null;
		if (sendAction && action != null && v != null)
		{
			try {
				actionMethod = v.getContext().getClass().getMethod(action, new Class<?>[]{Form.class, boolean.class});
			}
			catch (Throwable t)
			{
				if (debug)
					t.printStackTrace();
			}
		}
		int count = getChildCount();
		for (int i = 0; i < count; i++)
		{
			String getter = getterFields.get(i);
			if (getter == null)
				continue;
			boolean inputIsRequired = requiredFields.get(i);
			Object o = $.with(getChildAt(i)).attr(getter);
			if (o == null)
			{
				if (inputIsRequired)
				{
					if (actionMethod != null)
					{
						try {
							actionMethod.invoke(v.getContext(), Form.this, false);
						}
						catch (Throwable t)
						{
							if (debug)
								t.printStackTrace();
						}
					}
					return false;
				}
				else
					continue;
			}
				
			if (o instanceof Boolean)
			{
				if (!(Boolean) o)
				{
					if (inputIsRequired)
					{
						if (actionMethod != null)
						{
							try {
								actionMethod.invoke(v.getContext(), Form.this, false);
							}
							catch (Throwable t)
							{
								if (debug)
									t.printStackTrace();
							}
						}
						return false;
					}
					else
						continue;
				}
			}
			else //grouping all else (including CharSequence) here
			{
				String str = o.toString().trim();
				if (str.length() == 0)
				{
					if (inputIsRequired)
					{
						if (actionMethod != null)
						{
							try {
								actionMethod.invoke(v.getContext(), Form.this, false);
							}
							catch (Throwable t)
							{
								if (debug)
									t.printStackTrace();
							}
						}
						return false;
					}
					else
						continue;
				}
			}
		}
		if (actionMethod != null)
		{
			try {
				actionMethod.invoke(v.getContext(), Form.this, true);
			}
			catch (Throwable t)
			{
				if (debug)
					t.printStackTrace();
			}
		}
		return true;
	}
	
	/**
	 * Serialize the fields as if this form will be sent as URL parameters.
	 * @return
	 */
	public String serialize()
	{
		int count = getChildCount();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < count; i++)
		{
			String getter = getterFields.get(i);
			if (getter == null)
				continue;
			Object o = $.with(getChildAt(i)).attr(getter);
			if (o == null)
			{
				continue;
			}
			String name = childNames.get(i);
			if (name == null)
				continue;
				
			if (o instanceof Boolean)
			{
				builder.append(name).append("=").append(o);
			}
			else //grouping all else (including CharSequence) here
			{
				String str = o.toString().trim();
				if (str.length() != 0)
				{
					builder.append(name).append("=").append(str);
				}
			}
			if (i != count-1)
			{
				builder.append("&");
			}
		}
		try {
			return URLEncoder.encode(builder.toString(), "UTF-8");
		}
		catch (Throwable t)
		{
			if (debug)
				t.printStackTrace();
			return builder.toString();
		}
	}
	
	public boolean shouldValidate() {
		return validate;
	}

	public void setShouldValidate(boolean validate) {
		this.validate = validate;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public boolean isDebugEnabled() {
		return debug;
	}

	public void setDebugEnabled(boolean debug) {
		this.debug = debug;
	}

	/**
     * Per-child layout information associated with ViewForm.
     * 
     * @attr ref R.styleable#Form_Layout_form_field
     * @attr ref R.styleable#Form_Layout_form_required
     * @attr ref R.styleable#Form_Layout_form_submit_onclick
     */
    public static class LayoutParams extends LinearLayout.LayoutParams {
        
        /**
         * Whether or not this field is required by the form
         */
        @ViewDebug.ExportedProperty
        public boolean required;

        /**
         * Specifies that the view should submit the form if clicked.
         */
        @ViewDebug.ExportedProperty
        public String formField;
        
        /**
         * Specifies that on click, this view will call the validator and the submit function.
         */
        @ViewDebug.ExportedProperty
        public boolean submitOnClick;

        /**
         * Specifies the view name. This is used for URL serialization.
         */
        @ViewDebug.ExportedProperty
        public String name;

        /**
         * {@inheritDoc}
         */
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.Form_Layout);

            required = a.getBoolean(R.styleable.Form_Layout_form_required, false);
            formField = a.getString(R.styleable.Form_Layout_form_field);
            submitOnClick = a.getBoolean(R.styleable.Form_Layout_form_submit_onclick, false);
            name = a.getString(R.styleable.Form_Layout_form_name);

            a.recycle();
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(int width, int height) {
            super(width, height);
        }

        /**
         * Creates a new set of layout parameters with the specified width, height
         * and weight.
         *
         * @param width the width, either {@link #FILL_PARENT},
         *        {@link #WRAP_CONTENT} or a fixed size in pixels
         * @param height the height, either {@link #FILL_PARENT},
         *        {@link #WRAP_CONTENT} or a fixed size in pixels
         * @param weight the weight
         */
        public LayoutParams(int width, int height, float weight) {
            super(width, height);
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        /**
         * {@inheritDoc}
         */
        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }
    }

}
