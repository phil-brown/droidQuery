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

package self.philbrown.css;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import self.philbrown.droidQuery.$;
import self.philbrown.droidQuery.Function;
import android.content.Context;
import android.util.Log;
import android.view.View;

/**
 * 
 * @author Phil Brown
 * @since 1:39:29 PM Dec 3, 2013
 *
 * <h1>Valid Selectors:</h1>
 * <table border="1">
 * 	<tr>
 * 		<td><b>Selector</b></td>
 * 		<td><b>Example</b></td>
 * 		<td><b>Example Description</b></td>
 * 		<td><b>Original CSS Version</b></td>
 * 	</tr>
 * 	<tr>
 * 		<td>.class</td>
 * 		<td>.TextView</td>
 * 		<td>Selects all elements with class="android.widget.TextView". This is the same as <i>element</i>.</td>
 * 		<td>1</td>
 * 	</tr>
 * 	<tr>
 * 		<td>#id</td>
 * 		<td>#firstname</td>
 * 		<td>Selects the element with id <i>R.id.firstname</i></td>
 * 		<td>1</td>
 * 	</tr>
 * 	<tr>
 * 		<td>*</td>
 * 		<td>*</td>
 * 		<td>Selects all elements</td>
 * 		<td>2</td>
 * 	</tr>
 * 	<tr>
 * 		<td>element</td>
 * 		<td>com.example.MyView</td>
 * 		<td>Selects all elements with class="com.example.MyView". This is the same as <i>.class</i>.</td>
 * 		<td>1</td>
 * 	</tr>
 * 	<tr>
 * 		<td>[attribute]</td>
 * 		<td>[text]</td>
 * 		<td>Selects all elements with a <i>text</i> attribute.</td>
 * 		<td>2</td>
 * 	</tr>
 * 	<tr>
 * 		<td>[attribute=value]</td>
 * 		<td>[text=Name]</td>
 * 		<td>Selects all elements with a <i>text</i> attribute equal to "Name".</td>
 * 		<td>2</td>
 * 	</tr>
 * 	<tr>
 * 		<td>[attribute~=value]</td>
 * 		<td>[text~=flower]</td>
 * 		<td>Selects all elements with a <i>text</i> attribute containing the word "flower". This is the same as <i>[attribute*=value]</i></td>
 * 		<td>2</td>
 * 	</tr>
 * 	<tr>
 * 		<td>[attribute|=value]</td>
 * 		<td>[text|=en]</td>
 * 		<td>Selects all elements with a <i>text</i> attribute starting with "en". This is the same as <i>[attribute^=value]</i></td>
 * 		<td>2</td>
 * 	</tr>
 * 	<tr>
 * 		<td>[attribute$=value]</td>
 * 		<td>[text$=Name]</td>
 * 		<td>Selects all elements with a <i>text</i> attribute ending with "Name".</td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>[attribute*=value]</td>
 * 		<td>[text*=flower]</td>
 * 		<td>Selects all elements with a <i>text</i> attribute containing the word "flower". This is the same as <i>[attribute~=value]</i></td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>[attribute^=value]</td>
 * 		<td>[text^=en]</td>
 * 		<td>Selects all elements with a <i>text</i> attribute starting with "en". This is the same as <i>[attribute|=value]</i></td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>::selection</td>
 * 		<td>::selection</td>
 * 		<td>Selects all currently-selected views.</td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:focus</td>
 * 		<td>EditText:focus</td>
 * 		<td>Selects the EditText with focus.</td>
 * 		<td>2</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:first-letter</td>
 * 		<td>TextView:first-letter</td>
 * 		<td>Selects the first letter of every TextView element.</td>
 * 		<td>1</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:first-line</td>
 * 		<td>TextView:first-line</td>
 * 		<td>Selects the first line of every TextView element (as determined by the new line character).</td>
 * 		<td>1</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:only-child</td>
 * 		<td>LinearLayout:only-child</td>
 * 		<td>Selects every <i>LinearLayout</i> element that is the only child of its parent.</td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:first-child</td>
 * 		<td>LinearLayout:first-child</td>
 * 		<td>Selects every <i>LinearLayout</i> element that is the first child of its parent.</td>
 * 		<td>2</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:last-child</td>
 * 		<td>LinearLayout:last-child</td>
 * 		<td>Selects every <i>LinearLayout</i> element that is the last child of its parent.</td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:nth-child(n)</td>
 * 		<td>LinearLayout:nth-child(2)</td>
 * 		<td>Selects every <i>LinearLayout</i> element that is the second child of its parent.</td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:nth-last-child(n)</td>
 * 		<td>LinearLayout:nth-last-child(2)</td>
 * 		<td>Selects every <i>LinearLayout</i> element that is the second from last child of its parent.</td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:first-of-type</td>
 * 		<td>LinearLayout:first-child</td>
 * 		<td>Selects every <i>LinearLayout</i> element that is the first <i>LinearLayout</i> element of its parent.</td>
 * 		<td>2</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:last-of-type</td>
 * 		<td>LinearLayout:last-child</td>
 * 		<td>Selects every <i>LinearLayout</i> element that is the last <i>LinearLayout</i> element of its parent.</td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:nth-of-type(n)</td>
 * 		<td>LinearLayout:nth-child(2)</td>
 * 		<td>Selects every <i>LinearLayout</i> element that is the second <i>LinearLayout</i> element of its parent.</td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:nth-last-of-type(n)</td>
 * 		<td>LinearLayout:nth-last-child(2)</td>
 * 		<td>Selects every <i>LinearLayout</i> element that is the second from last <i>LinearLayout</i> element of its parent.</td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:root</td>
 * 		<td>:root</td>
 * 		<td>Selects the root view of the current context.</td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:empty</td>
 * 		<td>LinearLayout:empty</td>
 * 		<td>Selects every LinearLayout element that has no children.</td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:enabled</td>
 * 		<td>Button:enabled</td>
 * 		<td>Selects every enabled <i>Button</i> element.</td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:disabled</td>
 * 		<td>Button:disabled</td>
 * 		<td>Selects every disabled <i>Button</i> element.</td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:checked</td>
 * 		<td>Button:checked</td>
 * 		<td>Selects every checked <i>Button</i> element.</td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:not(selector)</td>
 * 		<td>:not(Button)</td>
 * 		<td>Selects every element that is not a <i>Button</i> element.</td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>element,element</td>
 * 		<td>EditText,Button,#foobar</td>
 * 		<td>Selects all <i>EditText</i> and <i>Button</i> elements, as well as the element with id <i>R.id.foobar</i>.</td>
 * 		<td>1</td>
 * 	</tr>
 * 	<tr>
 * 		<td>element element</td>
 * 		<td>LinearLayout Button</td>
 * 		<td>Selects all <i>Button</i> elements inside of <i>LinearLayout</i> elements.</td>
 * 		<td>1</td>
 * 	</tr>
 * 	<tr>
 * 		<td>element>element</td>
 * 		<td>LinearLayout Button</td>
 * 		<td>Selects all <i>Button</i> elements where the parent is a <i>LinearLayout</i>.</td>
 * 		<td>2</td>
 * 	</tr>
 * 	<tr>
 * 		<td>{@literal @}keyframes</td>
 * 		<td>@keyframes myAnimation { 0% {0px} 100% {200px}}</td>
 * 		<td>Specifies that the keyframes for myAnimation start at 0px and end at 200px.</td>
 * 		<td>2</td>
 * 	</tr>
 * </table>
 * <br>
 * <h1>The following selectors are known CSS Selectors that are not currently implemented:</h1>
 * <table border="1">
 * 	<tr>
 * 		<td><b>Selector</b></td>
 * 		<td><b>Example</b></td>
 * 		<td><b>Example Description</b></td>
 * 		<td><b>Original CSS Version</b></td>
 * 	</tr>
 * 	<tr>
 * 		<td>element+element</td>
 * 		<td>ScrollView+LinearLayout</td>
 * 		<td>Selects all <i>LinearLayout</i> elements that are placed immediately after <i>ScrollView</i> elements.</td>
 * 		<td>2</td>
 * 	</tr>
 * 	<tr>
 * 		<td>element~element</td>
 * 		<td>ScrollView~LinearLayout</td>
 * 		<td>Selects all <i>LinearLayout</i> elements that are preceded by a <i>ScrollView</i> element.</td>
 * 		<td>3</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:link</td>
 * 		<td>n/a</td>
 * 		<td>Not applicable for mobile apps. Used for websites to select all unvisited links.</td>
 * 		<td>1</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:visted</td>
 * 		<td>n/a</td>
 * 		<td>Not applicable for mobile apps. Used for websites to select all visited links.</td>
 * 		<td>1</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:active</td>
 * 		<td>n/a</td>
 * 		<td>Not applicable for mobile apps. Used for websites to select all active links.</td>
 * 		<td>1</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:hover</td>
 * 		<td>n/a</td>
 * 		<td>Not applicable for mobile apps. Used for websites to select all links on mouse over.</td>
 * 		<td>1</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:lang</td>
 * 		<td>n/a</td>
 * 		<td>Not applicable for mobile apps. Used for websites to select all elements set to a particular language.</td>
 * 		<td>1</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:before</td>
 * 		<td>Button:before</td>
 * 		<td>Inserts content before every <i>Button</i> element.</td>
 * 		<td>2</td>
 * 	</tr>
 * 	<tr>
 * 		<td>:after</td>
 * 		<td>Button:after</td>
 * 		<td>Inserts content after every <i>Button</i> element.</td>
 * 		<td>2</td>
 * 	</tr>
 * </table>
 */
public class CSSSelector 
{
	public $ makeSelection(Context context, String selector)
	{
		return makeSelection($.with(context), selector);
	}
	
	public $ makeSelection(View parent, String selector)
	{
		return makeSelection($.with(parent.getContext()), selector);
	}
	
	public $ makeSelection($ droidQuery, String selector)
	{
		//### class selector
		if (selector.startsWith("."))
		{
			String sel = selector.substring(1);
			if (sel == null) sel = "";
			if (sel.contains("."))
			{
				//expected that the full class is provided
				droidQuery.selectByType(sel);
			}
			else
			{
				//check android.view
				try {
					Class<?> clazz = Class.forName(String.format(Locale.US, "android.view%s", selector));
					droidQuery.selectByType(clazz);
				}
				catch (Throwable t)
				{
					//check android.widget
					try {
						Class<?> clazz = Class.forName(String.format(Locale.US, "android.widget%s", selector));
						droidQuery.selectByType(clazz);
					}
					catch (Throwable t2)
					{
						//check android.webkit
						try {
							Class<?> clazz = Class.forName(String.format(Locale.US, "android.webkit%s", selector));
							droidQuery.selectByType(clazz);
						}
						catch (Throwable t3)
						{
							//could not select class
							Log.w("droidQuery", String.format(Locale.US, "Could not select class %s.", sel));
						}
					}
				}
				
			}
			
			
		}
		//### id selector
		else if (selector.startsWith("#"))
		{
			String classname = "";
			try
			{
				classname = String.format(Locale.US, "%s.R.id", droidQuery.context().getPackageName());
				Class<?> clazz = Class.forName(classname);
				String sel = selector.substring(1);
				if (sel == null) sel = "";
				try
				{
					Field f = clazz.getField(sel);
					int id = (Integer) f.get(null);
					droidQuery.id(id);
				}
				catch (Throwable t)
				{
					Log.w("droidQuery", String.format(Locale.US, "No resource found for R.id.%s", sel));
				}
			}
			catch (Throwable t)
			{
				Log.w("droidQuery", String.format(Locale.US, "No class found for %s", classname));
			}
		}
		//### all selector
		else if (selector.equals("*"))
		{
			droidQuery.selectAll();
		}
		//### attribute selectors. Added for android: attributes can be named like: @color/green
		else if (selector.startsWith("["))
		{
			if (!selector.endsWith("]"))
			{
				Log.w("droidQuery", String.format(Locale.US, "Invalid css selector: %s", selector));
				return droidQuery;
			}
			String sel = selector.substring(1, selector.length()-1);
			
			List<View> selection = new ArrayList<View>();
			
			//### startsWith (|=)
			if (sel.contains("|="))
			{
				String[] split = sel.split("|=");
				String attribute = split[0];
				String value = split[1];
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute)));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute);
							val = f.get(v);
						}
						catch (Throwable t2)
						{
							//don't print
						}
					}
					if (val != null)
					{
						if (val.toString().startsWith(value))
						{
							selection.add(v);
						}
					}
				}
				
				droidQuery = $.with(selection);
				
			}
			//### startsWith (^=)
			else if (sel.contains("^="))
			{
				String[] split = sel.split("^=");
				String attribute = split[0];
				String value = split[1];
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute)));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute);
							val = f.get(v);
						}
						catch (Throwable t2)
						{
							//don't print
						}
					}
					if (val != null)
					{
						if (val.toString().startsWith(value))
						{
							selection.add(v);
						}
					}
				}
				
				droidQuery = $.with(selection);
				
			}
			//### contains (~=)
			else if (sel.contains("~="))
			{
				String[] split = sel.split("~=");
				String attribute = split[0];
				String value = split[1];
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute)));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute);
							val = f.get(v);
						}
						catch (Throwable t2)
						{
							//don't print
						}
					}
					if (val != null)
					{
						if (value.contains(val.toString()))
						{
							selection.add(v);
						}
					}
				}
				
				droidQuery = $.with(selection);
			}
			//### contains (*=)
			else if (sel.contains("*="))
			{
				String[] split = sel.split("*=");
				String attribute = split[0];
				String value = split[1];
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute)));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute);
							val = f.get(v);
						}
						catch (Throwable t2)
						{
							//don't print
						}
					}
					if (val != null)
					{
						if (value.contains(val.toString()))
						{
							selection.add(v);
						}
					}
				}
				
				droidQuery = $.with(selection);
			}
			//### equals
			else if (sel.contains("="))
			{
				String[] split = sel.split("=");
				String attribute = split[0];
				String value = split[1];
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute)));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute);
							val = f.get(v);
						}
						catch (Throwable t2)
						{
							//don't print
						}
					}
					if (val != null)
					{
						if (val.toString().equals(value))
						{
							selection.add(v);
						}
					}
				}
				
				droidQuery = $.with(selection);
			}
			//### ends with
			else if (sel.contains("$="))
			{
				String[] split = sel.split("$=");
				String attribute = split[0];
				String value = split[1];
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute)));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute);
							val = f.get(v);
						}
						catch (Throwable t2)
						{
							//don't print
						}
					}
					if (val != null)
					{
						if (val.toString().endsWith(value))
						{
							selection.add(v);
						}
					}
				}
				
				droidQuery = $.with(selection);
			}
			//### has attribute
			else
			{
				String attribute = sel;
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute)));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute);
							val = f.get(v);
						}
						catch (Throwable t2)
						{
							//don't print
						}
					}
					if (val != null)
					{
						selection.add(v);
					}
				}
				
				droidQuery = $.with(selection);
			}
			
		}
		else if (selector.contains("::"))
		{
			//check the left side
			String[] split = selector.split("::");
			if (split.length > 1)
			{
				//if the left side is its own selector, then make that selection first to narrow down the filter
				if (split.length == 2)
					droidQuery = makeSelection(droidQuery, split[0]);
				else
				{
					StringBuilder sel = new StringBuilder();
					for (int i = 0; i < split.length-1; i++)
					{
						sel.append(split[i]).append("::");
					}
					droidQuery = makeSelection(droidQuery, sel.toString());
				}
			}
			
			//then make selection
			droidQuery.selectSelected();
		}
		else if (selector.contains(":"))
		{
			if (selector.startsWith(":"))
			{
				String sel = selector.substring(1);
				if (sel.equals("first-child"))
				{
					return droidQuery.selectNthChilds(0);
				}
				else if (sel.startsWith("nth-child"))
				{
					if (sel.contains("(") && sel.contains(")"))
					{
						String _sel = sel.substring(sel.indexOf("("), sel.indexOf(")"));
						int n = 0;
						try
						{
							n = Integer.parseInt(_sel);
						}
						catch (Throwable t)
						{
							Log.w("CSS", String.format(Locale.US, "Invalid selector %s (can't parse Integer \"%s\").", selector, _sel));
						}
						n = n-1;
						if (n < 0)
							n = 0;
						return droidQuery.selectNthChilds(n);
					}
					else
					{
						//not a valid format.
						Log.w("CSS", "Invalid selector " + selector);
					}
					
				}
				else if (sel.startsWith("nth-last-child"))
				{
					if (sel.contains("(") && sel.contains(")"))
					{
						String _sel = sel.substring(sel.indexOf("("), sel.indexOf(")"));
						int n = 0;
						try
						{
							n = Integer.parseInt(_sel);
						}
						catch (Throwable t)
						{
							Log.w("CSS", String.format(Locale.US, "Invalid selector %s (can't parse Integer \"%s\").", selector, _sel));
						}
						n = n-1;
						if (n < 0)
							n = 0;
						return droidQuery.selectNthFromEndChilds(n);
					}
					else
					{
						//not a valid format.
						Log.w("CSS", "Invalid selector " + selector);
					}
				}
				else if (sel.equals("last-child"))
				{
					if (sel.contains("(") && sel.contains(")"))
					{
						String _sel = sel.substring(sel.indexOf("("), sel.indexOf(")"));
						int n = 0;
						try
						{
							n = Integer.parseInt(_sel);
						}
						catch (Throwable t)
						{
							Log.w("CSS", String.format(Locale.US, "Invalid selector %s (can't parse Integer \"%s\").", selector, _sel));
						}
						n = n-1;
						if (n < 0)
							n = 0;
						return droidQuery.selectLastChilds(n);
					}
					else
					{
						//not a valid format.
						Log.w("CSS", "Invalid selector " + selector);
					}
				}
			}
			
			String[] split = selector.split(":");
			String sel = split[1];
			if (split.length == 2)
			{
				if (sel.equals("first-child"))
				{
					droidQuery.selectNthChilds(0);
					return makeSelection(droidQuery, split[0]);
				}
				else if (sel.equals("first-of-type"))
				{
					return droidQuery.selectNthChildsOfType(0, split[0]);
				}
				else if (sel.equals("last-of-type"))
				{
					return droidQuery.selectNthFromEndChildsOfType(0, split[0]);
				}
				else if (sel.startsWith("nth-child"))
				{
					if (sel.contains("(") && sel.contains(")"))
					{
						String _sel = sel.substring(sel.indexOf("("), sel.indexOf(")"));
						int n = 0;
						try
						{
							n = Integer.parseInt(_sel);
						}
						catch (Throwable t)
						{
							Log.w("CSS", String.format(Locale.US, "Invalid selector %s (can't parse Integer \"%s\").", selector, _sel));
						}
						n = n-1;
						if (n < 0)
							n = 0;
						droidQuery.selectNthChilds(n);
						return makeSelection(droidQuery, split[0]);
					}
					else
					{
						//not a valid format.
						Log.w("CSS", "Invalid selector " + selector);
					}
					
				}
				else if (sel.startsWith("nth-last-child"))
				{
					if (sel.contains("(") && sel.contains(")"))
					{
						String _sel = sel.substring(sel.indexOf("("), sel.indexOf(")"));
						int n = 0;
						try
						{
							n = Integer.parseInt(_sel);
						}
						catch (Throwable t)
						{
							Log.w("CSS", String.format(Locale.US, "Invalid selector %s (can't parse Integer \"%s\").", selector, _sel));
						}
						n = n-1;
						if (n < 0)
							n = 0;
						droidQuery.selectNthFromEndChilds(n);
						return makeSelection(droidQuery, split[0]);
					}
					else
					{
						//not a valid format.
						Log.w("CSS", "Invalid selector " + selector);
					}
				}
				else if (sel.startsWith("nth-of-type"))
				{
					String _sel = sel.substring(sel.indexOf("("), sel.indexOf(")"));
					int n = 0;
					try
					{
						n = Integer.parseInt(_sel);
					}
					catch (Throwable t)
					{
						Log.w("CSS", String.format(Locale.US, "Invalid selector %s (can't parse Integer \"%s\").", selector, _sel));
					}
					n = n-1;
					if (n < 0)
						n = 0;
					return droidQuery.selectNthChildsOfType(n, split[0]);
				}
				else if (sel.startsWith("nth-last-of-type"))
				{
					String _sel = sel.substring(sel.indexOf("("), sel.indexOf(")"));
					int n = 0;
					try
					{
						n = Integer.parseInt(_sel);
					}
					catch (Throwable t)
					{
						Log.w("CSS", String.format(Locale.US, "Invalid selector %s (can't parse Integer \"%s\").", selector, _sel));
					}
					n = n-1;
					if (n < 0)
						n = 0;
					return droidQuery.selectNthChildsOfType(n, split[0]);
				}
				else if (sel.equals("last-child"))
				{
					if (sel.contains("(") && sel.contains(")"))
					{
						String _sel = sel.substring(sel.indexOf("("), sel.indexOf(")"));
						int n = 0;
						try
						{
							n = Integer.parseInt(_sel);
						}
						catch (Throwable t)
						{
							Log.w("CSS", String.format(Locale.US, "Invalid selector %s (can't parse Integer \"%s\").", selector, _sel));
						}
						n = n-1;
						if (n < 0)
							n = 0;
						droidQuery.selectLastChilds(n);
						return makeSelection(droidQuery, split[0]);
					}
					else
					{
						//not a valid format.
						Log.w("CSS", "Invalid selector " + selector);
					}
				}
				//first handle first half
				makeSelection(droidQuery, split[0]);
			}
			
			
			if (sel.equals("focus"))
			{
				droidQuery.selectFocused();
			}
			else if (sel.equals("first-letter"))
			{
				//sets this droidQuery object's data Object to the first letter of every element in selection.
				final StringBuilder builder = new StringBuilder();
				droidQuery.each(new Function() {
					
					@Override
					public void invoke($ droidQuery, Object... params) {
						Object text = droidQuery.attr("text");
						if (text != null)
							builder.append(text.toString().charAt(0));
					}
				});
				droidQuery.data(builder.toString());
			}
			else if (sel.equals("first-line"))
			{
				//sets this droidQuery object's data Object to the first line of every element in selection.
				final StringBuilder builder = new StringBuilder();
				droidQuery.each(new Function() {
					
					@Override
					public void invoke($ droidQuery, Object... params) {
						Object text = droidQuery.attr("text");
						if (text != null)
							builder.append(text.toString().split("\n")[0]);
					}
				});
				droidQuery.data(builder.toString());
			}
			else if (sel.equals("only-child"))
			{
				droidQuery.selectOnlyChilds();
			}
			
			else if (sel.equals("root"))
			{
				droidQuery = $.with(droidQuery.context());
			}
			else if (sel.equals("empty"))
			{
				droidQuery.selectEmpties();
			}
			else if (sel.equals("enabled"))
			{
				final List<View> list = new ArrayList<View>();
				droidQuery.each(new Function() {
					
					@Override
					public void invoke($ droidQuery, Object... params) {
						View view = droidQuery.view(0);
						if (view.isEnabled())
							list.add(view);
					}
				});
				if (!list.isEmpty())
				{
					droidQuery = $.with(list);
				}
				else
				{
					Log.w("CSS", "null Selection not allowed");
				}
			}
			else if (sel.equals("disabled"))
			{
				final List<View> list = new ArrayList<View>();
				droidQuery.each(new Function() {
					
					@Override
					public void invoke($ droidQuery, Object... params) {
						View view = droidQuery.view(0);
						if (!view.isEnabled())
							list.add(view);
					}
				});
				if (!list.isEmpty())
				{
					droidQuery = $.with(list);
				}
				else
				{
					Log.w("CSS", "null Selection not allowed");
				}
			}
			else if (sel.equals("checked"))
			{
				final List<View> list = new ArrayList<View>();
				droidQuery.each(new Function() {
					
					@Override
					public void invoke($ droidQuery, Object... params) {
						Object checked = droidQuery.attr("checked");
						boolean isChecked = false;
						if (checked != null)
							isChecked = (Boolean) checked;
						if (isChecked)
							list.add(droidQuery.view(0));
					}
				});
				if (!list.isEmpty())
				{
					droidQuery = $.with(list);
				}
				else
				{
					Log.w("CSS", "null Selection not allowed");
				}
			}
			else if (sel.startsWith("not"))
			{
				if (sel.contains("("))
				{
					if (sel.endsWith(")"))
					{
						sel = sel.split("(")[1];
						sel = sel.substring(0, sel.length());
						
						Class<?> clazz = null;
						if (sel.contains("."))
						{
							//expected that the full class is provided
							try {
								clazz = Class.forName(sel);
							}
							catch (Throwable t)
							{
								
							}
						}
						else
						{
							//check android.view
							try {
								clazz = Class.forName(String.format(Locale.US, "android.view.%s", sel));
								
							}
							catch (Throwable t)
							{
								//check android.widget
								try {
									clazz = Class.forName(String.format(Locale.US, "android.widget.%s", sel));
								}
								catch (Throwable t2)
								{
									//check android.webkit
									try {
										clazz = Class.forName(String.format(Locale.US, "android.webkit.%s", sel));
									}
									catch (Throwable t3)
									{
										
									}
								}
							}
							
						}
						if (clazz != null)
						{
							final List<View> list = new ArrayList<View>();
							final Class<?> _clazz = clazz;
							droidQuery.each(new Function() {
								
								@Override
								public void invoke($ droidQuery, Object... params) {
									View view = droidQuery.view(0);
									if (!_clazz.isInstance(view))
									{
										list.add(view);
									}
								}
							});
							if (!list.isEmpty())
							{
								droidQuery = $.with(list);
							}
							else
							{
								Log.w("CSS", "null Selection not allowed");
							}
						}
						else
						{
							//could not select class
							Log.w("droidQuery", String.format(Locale.US, "Could not 'not select' class %s.", sel));
						}
					}
				}
			}
		}
		else if (selector.contains("["))
		{
			String[] split = selector.split("[");
			String element = split[0];
			String brackets = split[1];
			//first select the class
			makeSelection(droidQuery, element);
			//then handle the bracketed selection
			makeSelection(droidQuery, String.format(Locale.US, "[%s", brackets));
		}
		else if (selector.contains(","))
		{
			String[] split = selector.split(",");
			$[] array = new $[split.length];
			for (int i = 0; i < split.length; i++)
			{
				array[i] = makeSelection(new $(droidQuery), split[i]);
			}
			droidQuery = array[0];
			for (int i = 1; i < array.length; i++)
			{
				droidQuery = droidQuery.union(array[i]);
			}
		}
//		else if (selector.contains("+"))
//		{
//			//TODO elements
//		}
		else if (selector.contains(" "))
		{
			String[] split = selector.split(" ");
			for (String sel : split)
			{
				makeSelection(droidQuery, sel);
			}
		}
		else if (selector.contains(">"))
		{
			String[] split = selector.split(">");
			String sel = split[0];
			makeSelection(droidQuery, sel);
			for (int i = 1; i < split.length; i++)
			{
				makeSelection(droidQuery.selectChildren(), split[i]);
			}
		}
//		else if (selector.contains("~"))
//		{
//			//TODO elements
//		}
		//no special characters-> Select by Type.
		else
		{
			//no special characters.
			if (selector.contains("."))
			{
				//expected that the full class is provided
				droidQuery.selectByType(selector);
			}
			else
			{
				//check android.view
				try {
					Class<?> clazz = Class.forName(String.format(Locale.US, "android.view.%s", selector));
					droidQuery.selectByType(clazz);
				}
				catch (Throwable t)
				{
					//check android.widget
					try {
						Class<?> clazz = Class.forName(String.format(Locale.US, "android.widget.%s", selector));
						droidQuery.selectByType(clazz);
					}
					catch (Throwable t2)
					{
						//check android.webkit
						try {
							Class<?> clazz = Class.forName(String.format(Locale.US, "android.webkit.%s", selector));
							droidQuery.selectByType(clazz);
						}
						catch (Throwable t3)
						{
							//could not select class
							Log.w("droidQuery", String.format(Locale.US, "Could not select class %s.", selector));
						}
					}
				}
				
			}
		}
		
		return droidQuery;
		
	}
	
	
	
	/** 
	 * Capitalizes the first letter of the given string.
	 * @param string the string whose first letter should be capitalized
	 * @return the given string with its first letter capitalized
	 * @throws NullPointerException if the string is null or empty
	 */
	private static String capitalize(String string)
	{
		if (string == null || string.trim().length() == 0)
			throw new NullPointerException("Cannot handle null or empty string");
		
		StringBuilder strBuilder = new StringBuilder(string);
		strBuilder.setCharAt(0, Character.toUpperCase(strBuilder.charAt(0)));
		return strBuilder.toString();
	}
}
