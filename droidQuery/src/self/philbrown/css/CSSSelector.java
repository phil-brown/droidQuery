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

import self.philbrown.cssparser.ParserConstants;
import self.philbrown.cssparser.Token;
import self.philbrown.cssparser.TokenSequence;
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
public class CSSSelector implements ParserConstants
{
	private static Token DOT_TOKEN = new Token(DOT);
	private static Token HASH_TOKEN = new Token(HASH);
	private static Token TIMES_TOKEN = new Token(TIMES);
	private static Token LEFTSQ_TOKEN = new Token(LEFTSQ);
	private static Token DBL_COLON_TOKEN = new Token(DOUBLE_COLON);
	private static Token COLON_TOKEN = new Token(COLON);
	private static Token COMMA_TOKEN = new Token(COMMA);
	private static Token SPACE_TOKEN = new Token(SPACE);
	private static Token GT_TOKEN = new Token(GT);
	
	private static Token EQ = new Token(EQUAL);
	private static Token OR_EQ = new Token(OR_EQUAL);
	private static Token CARET_EQ = new Token(CARET_EQUAL);
	private static Token NOT_EQ = new Token(NOT_EQUAL);
	private static Token TIMES_EQ = new Token(TIMES_EQUAL);
	private static Token DOLLAR_EQ = new Token(DOLLAR_EQUAL);
	
	private static Token LEFTPAREN_TOKEN = new Token(LEFTPAREN);
	private static Token RIGHTPAREN_TOKEN = new Token(RIGHTPAREN);
	
	public $ makeSelection(Context context, TokenSequence selector)
	{
		return makeSelection($.with(context), selector);
	}
	
	public $ makeSelection(View parent, TokenSequence selector)
	{
		return makeSelection($.with(parent.getContext()), selector);
	}
	
	public $ makeSelection($ droidQuery, TokenSequence selector)
	{
		//### class selector
		if (selector.startsWith(DOT_TOKEN))
		{
			TokenSequence sel = selector.subSequence(1);
			if (sel == null) sel = new TokenSequence(new ArrayList<Token>(), null);
			if (sel.contains(new Token(DOT, null)))
			{
				//expected that the full class is provided
				droidQuery.selectByType(sel.toString());
			}
			else
			{
				//check android.view
				try {
					Class<?> clazz = Class.forName(String.format(Locale.US, "android.view%s", selector.toString()));
					droidQuery.selectByType(clazz);
				}
				catch (Throwable t)
				{
					//check android.widget
					try {
						Class<?> clazz = Class.forName(String.format(Locale.US, "android.widget%s", selector.toString()));
						droidQuery.selectByType(clazz);
					}
					catch (Throwable t2)
					{
						//check android.webkit
						try {
							Class<?> clazz = Class.forName(String.format(Locale.US, "android.webkit%s", selector.toString()));
							droidQuery.selectByType(clazz);
						}
						catch (Throwable t3)
						{
							//could not select class
							Log.w("droidQuery", String.format(Locale.US, "Could not select class %s.", sel.toString()));
						}
					}
				}
				
			}
			
			
		}
		//### id selector
		else if (selector.startsWith(HASH_TOKEN))
		{
			String classname = "";
			try
			{
				classname = String.format(Locale.US, "%s.R.id", droidQuery.context().getPackageName());
				Class<?> clazz = Class.forName(classname);
				TokenSequence sel = selector.subSequence(1);
				if (sel == null) sel = new TokenSequence(new ArrayList<Token>(), null);
				try
				{
					Field f = clazz.getField(sel.toString());
					int id = (Integer) f.get(null);
					droidQuery.id(id);
				}
				catch (Throwable t)
				{
					Log.w("droidQuery", String.format(Locale.US, "No resource found for R.id.%s", sel.toString()));
				}
			}
			catch (Throwable t)
			{
				Log.w("droidQuery", String.format(Locale.US, "No class found for %s", classname));
			}
		}
		//### all selector
		else if (selector.equals(TIMES_TOKEN))
		{
			droidQuery.selectAll();
		}
		//### attribute selectors. Added for android: attributes can be named like: @color/green
		else if (selector.startsWith(LEFTSQ_TOKEN))
		{
			if (!selector.endsWith(new Token(RIGHTSQ)))
			{
				Log.w("droidQuery", String.format(Locale.US, "Invalid css selector: %s", selector.toString()));
				return droidQuery;
			}
			TokenSequence sel = selector.subSequence(1, selector.length()-1);
			
			List<View> selection = new ArrayList<View>();
			
			//### startsWith (|=)
			if (sel.contains(OR_EQ))
			{
				TokenSequence[] split = sel.split(OR_EQ);
				TokenSequence attribute = split[0];
				TokenSequence value = split[1];
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute.toString())));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute.toString());
							val = f.get(v);
						}
						catch (Throwable t2)
						{
							//don't print
						}
					}
					if (val != null)
					{
						if (val.toString().startsWith(value.toString()))
						{
							selection.add(v);
						}
					}
				}
				
				droidQuery = $.with(selection);
				
			}
			//### startsWith (^=)
			else if (sel.contains(CARET_EQ))
			{
				TokenSequence[] split = sel.split(CARET_EQ);
				TokenSequence attribute = split[0];
				TokenSequence value = split[1];
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute.toString())));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute.toString());
							val = f.get(v);
						}
						catch (Throwable t2)
						{
							//don't print
						}
					}
					if (val != null)
					{
						if (val.toString().startsWith(value.toString()))
						{
							selection.add(v);
						}
					}
				}
				
				droidQuery = $.with(selection);
				
			}
			//### contains (~=)
			else if (sel.contains(NOT_EQ))
			{
				TokenSequence[] split = sel.split(NOT_EQ);
				TokenSequence attribute = split[0];
				TokenSequence value = split[1];
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute.toString())));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute.toString());
							val = f.get(v);
						}
						catch (Throwable t2)
						{
							//don't print
						}
					}
					if (val != null)
					{
						if (value.toString().contains(val.toString()))
						{
							selection.add(v);
						}
					}
				}
				
				droidQuery = $.with(selection);
			}
			//### contains (*=)
			else if (sel.contains(TIMES_EQ))
			{
				TokenSequence[] split = sel.split(TIMES_EQ);
				TokenSequence attribute = split[0];
				TokenSequence value = split[1];
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute.toString())));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute.toString());
							val = f.get(v);
						}
						catch (Throwable t2)
						{
							//don't print
						}
					}
					if (val != null)
					{
						if (value.toString().contains(val.toString()))
						{
							selection.add(v);
						}
					}
				}
				
				droidQuery = $.with(selection);
			}
			//### equals
			else if (sel.contains(EQ))
			{
				TokenSequence[] split = sel.split(EQ);
				TokenSequence attribute = split[0];
				TokenSequence value = split[1];
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute.toString())));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute.toString());
							val = f.get(v);
						}
						catch (Throwable t2)
						{
							//don't print
						}
					}
					if (val != null)
					{
						if (val.toString().equals(value.toString()))
						{
							selection.add(v);
						}
					}
				}
				
				droidQuery = $.with(selection);
			}
			//### ends with
			else if (sel.contains(DOLLAR_EQ))
			{
				TokenSequence[] split = sel.split(DOLLAR_EQ);
				TokenSequence attribute = split[0];
				TokenSequence value = split[1];
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute.toString())));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute.toString());
							val = f.get(v);
						}
						catch (Throwable t2)
						{
							//don't print
						}
					}
					if (val != null)
					{
						if (val.toString().endsWith(value.toString()))
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
				TokenSequence attribute = sel;
				$ d = $.with(droidQuery.view(0)).selectAll();
				for (int i = 0; i < d.size(); i++)
				{
					View v = d.view(i);
					Object val = null;
					try {
						//check getters
						Method m = v.getClass().getMethod(String.format("get%s", capitalize(attribute.toString())));
						val = m.invoke(v);
					}
					catch (Throwable t)
					{
						try {
							//check fields
							Field f = v.getClass().getField(attribute.toString());
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
		else if (selector.contains(COLON_TOKEN) || selector.contains(DBL_COLON_TOKEN))
		{
			if (selector.startsWith(COLON_TOKEN) || selector.contains(DBL_COLON_TOKEN))
			{
				TokenSequence sel = null;
				if (selector.contains(COLON_TOKEN))
					sel = selector.subSequence(1);
				else
					sel = selector.subSequence(2);
				
				if (sel.equals("first-child"))
				{
					return droidQuery.selectNthChilds(0);
				}
				else if (sel.startsWith("nth-child"))
				{
					if (sel.contains(LEFTPAREN_TOKEN) && sel.contains(RIGHTPAREN_TOKEN))
					{
						TokenSequence _sel = sel.subSequence(sel.indexOf(LEFTPAREN_TOKEN), sel.indexOf(RIGHTPAREN_TOKEN));
						int n = 0;
						try
						{
							n = Integer.parseInt(_sel.toString());
						}
						catch (Throwable t)
						{
							Log.w("CSS", String.format(Locale.US, "Invalid selector %s (can't parse Integer \"%s\").", selector.toString(), _sel.toString()));
						}
						n = n-1;
						if (n < 0)
							n = 0;
						return droidQuery.selectNthChilds(n);
					}
					else
					{
						//not a valid format.
						Log.w("CSS", "Invalid selector " + selector.toString());
					}
					
				}
				else if (sel.startsWith("nth-last-child"))
				{
					if (sel.contains(LEFTPAREN_TOKEN) && sel.contains(RIGHTPAREN_TOKEN))
					{
						TokenSequence _sel = sel.subSequence(sel.indexOf(LEFTPAREN_TOKEN), sel.indexOf(RIGHTPAREN_TOKEN));
						int n = 0;
						try
						{
							n = Integer.parseInt(_sel.toString());
						}
						catch (Throwable t)
						{
							Log.w("CSS", String.format(Locale.US, "Invalid selector %s (can't parse Integer \"%s\").", selector.toString(), _sel.toString()));
						}
						n = n-1;
						if (n < 0)
							n = 0;
						return droidQuery.selectNthFromEndChilds(n);
					}
					else
					{
						//not a valid format.
						Log.w("CSS", "Invalid selector " + selector.toString());
					}
				}
				else if (sel.equals("last-child"))
				{
					if (sel.contains(LEFTPAREN_TOKEN) && sel.contains(RIGHTPAREN_TOKEN))
					{
						TokenSequence _sel = sel.subSequence(sel.indexOf(LEFTPAREN_TOKEN), sel.indexOf(RIGHTPAREN_TOKEN));
						int n = 0;
						try
						{
							n = Integer.parseInt(_sel.toString());
						}
						catch (Throwable t)
						{
							Log.w("CSS", String.format(Locale.US, "Invalid selector %s (can't parse Integer \"%s\").", selector.toString(), _sel.toString()));
						}
						n = n-1;
						if (n < 0)
							n = 0;
						return droidQuery.selectLastChilds(n);
					}
					else
					{
						//not a valid format.
						Log.w("CSS", "Invalid selector " + selector.toString());
					}
				}
			}
			
			TokenSequence[] split = selector.splitOnAny(new Token[]{COLON_TOKEN, DBL_COLON_TOKEN});
			
			TokenSequence sel = split[1];
			if (split.length == 2)
			{
				if (sel.equals("first-child"))
				{
					droidQuery.selectNthChilds(0);
					return makeSelection(droidQuery, split[0]);
				}
				else if (sel.equals("first-of-type"))
				{
					return droidQuery.selectNthChildsOfType(0, split[0].toString());
				}
				else if (sel.equals("last-of-type"))
				{
					return droidQuery.selectNthFromEndChildsOfType(0, split[0].toString());
				}
				else if (sel.startsWith("nth-child"))
				{
					if (sel.contains(LEFTPAREN_TOKEN) && sel.contains(RIGHTPAREN_TOKEN))
					{
						TokenSequence _sel = sel.subSequence(sel.indexOf(LEFTPAREN_TOKEN), sel.indexOf(RIGHTPAREN_TOKEN));
						int n = 0;
						try
						{
							n = Integer.parseInt(_sel.toString());
						}
						catch (Throwable t)
						{
							Log.w("CSS", String.format(Locale.US, "Invalid selector %s (can't parse Integer \"%s\").", selector.toString(), _sel.toString()));
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
						Log.w("CSS", "Invalid selector " + selector.toString());
					}
					
				}
				else if (sel.startsWith("nth-last-child"))
				{
					if (sel.contains(LEFTPAREN_TOKEN) && sel.contains(RIGHTPAREN_TOKEN))
					{
						TokenSequence _sel = sel.subSequence(sel.indexOf(LEFTPAREN_TOKEN), sel.indexOf(RIGHTPAREN_TOKEN));
						int n = 0;
						try
						{
							n = Integer.parseInt(_sel.toString());
						}
						catch (Throwable t)
						{
							Log.w("CSS", String.format(Locale.US, "Invalid selector %s (can't parse Integer \"%s\").", selector.toString(), _sel.toString()));
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
						Log.w("CSS", "Invalid selector " + selector.toString());
					}
				}
				else if (sel.startsWith("nth-of-type"))
				{
					TokenSequence _sel = sel.subSequence(sel.indexOf(LEFTPAREN_TOKEN), sel.indexOf(RIGHTPAREN_TOKEN));
					int n = 0;
					try
					{
						n = Integer.parseInt(_sel.toString());
					}
					catch (Throwable t)
					{
						Log.w("CSS", String.format(Locale.US, "Invalid selector %s (can't parse Integer \"%s\").", selector.toString(), _sel.toString()));
					}
					n = n-1;
					if (n < 0)
						n = 0;
					return droidQuery.selectNthChildsOfType(n, split[0].toString());
				}
				else if (sel.startsWith("nth-last-of-type"))
				{
					TokenSequence _sel = sel.subSequence(sel.indexOf(LEFTPAREN_TOKEN), sel.indexOf(RIGHTPAREN_TOKEN));
					int n = 0;
					try
					{
						n = Integer.parseInt(_sel.toString());
					}
					catch (Throwable t)
					{
						Log.w("CSS", String.format(Locale.US, "Invalid selector %s (can't parse Integer \"%s\").", selector.toString(), _sel.toString()));
					}
					n = n-1;
					if (n < 0)
						n = 0;
					return droidQuery.selectNthChildsOfType(n, split[0].toString());
				}
				else if (sel.equals("last-child"))
				{
					if (sel.contains(LEFTPAREN_TOKEN) && sel.contains(RIGHTPAREN_TOKEN))
					{
						TokenSequence _sel = sel.subSequence(sel.indexOf(LEFTPAREN_TOKEN), sel.indexOf(RIGHTPAREN_TOKEN));
						int n = 0;
						try
						{
							n = Integer.parseInt(_sel.toString());
						}
						catch (Throwable t)
						{
							Log.w("CSS", String.format(Locale.US, "Invalid selector %s (can't parse Integer \"%s\").", selector.toString(), _sel.toString()));
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
						Log.w("CSS", "Invalid selector " + selector.toString());
					}
				}
				//first handle first half
				makeSelection(droidQuery, split[0]);
			}
			
			
			if (sel.equals("focus"))
			{
				droidQuery.selectFocused();
			}
			else if (sel.equals("selection"))
			{
				droidQuery.selectSelected();
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
				if (sel.contains(LEFTPAREN_TOKEN))
				{
					if (sel.endsWith(RIGHTPAREN_TOKEN))
					{
						sel = sel.split(LEFTPAREN_TOKEN)[1];
						sel = sel.subSequence(0, sel.length());
						
						Class<?> clazz = null;
						if (sel.contains(DOT_TOKEN))
						{
							//expected that the full class is provided
							try {
								clazz = Class.forName(sel.toString());
							}
							catch (Throwable t)
							{
								
							}
						}
						else
						{
							//check android.view
							try {
								clazz = Class.forName(String.format(Locale.US, "android.view.%s", sel.toString()));
								
							}
							catch (Throwable t)
							{
								//check android.widget
								try {
									clazz = Class.forName(String.format(Locale.US, "android.widget.%s", sel.toString()));
								}
								catch (Throwable t2)
								{
									//check android.webkit
									try {
										clazz = Class.forName(String.format(Locale.US, "android.webkit.%s", sel.toString()));
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
							Log.w("droidQuery", String.format(Locale.US, "Could not 'not select' class %s.", sel.toString()));
						}
					}
				}
			}
		}
		else if (selector.contains(LEFTSQ_TOKEN))
		{
			TokenSequence[] split = selector.split(LEFTSQ_TOKEN);
			TokenSequence element = split[0];
			TokenSequence brackets = split[1];
			//first select the class
			makeSelection(droidQuery, element);
			//then handle the bracketed selection
			TokenSequence seq = new TokenSequence.Builder().append(LEFTSQ_TOKEN).append(brackets).create();
			makeSelection(droidQuery, seq);
		}
		else if (selector.contains(COMMA_TOKEN))
		{
			TokenSequence[] split = selector.split(COMMA_TOKEN);
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
		else if (selector.contains(SPACE_TOKEN))
		{
			TokenSequence[] split = selector.split(SPACE_TOKEN);
			for (TokenSequence sel : split)
			{
				makeSelection(droidQuery, sel);
			}
		}
		else if (selector.contains(GT_TOKEN))
		{
			TokenSequence[] split = selector.split(GT_TOKEN);
			TokenSequence sel = split[0];
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
			if (selector.contains(DOT_TOKEN))
			{
				//expected that the full class is provided
				droidQuery.selectByType(selector.toString());
			}
			else
			{
				//check android.view
				try {
					Class<?> clazz = Class.forName(String.format(Locale.US, "android.view.%s", selector.toString()));
					droidQuery.selectByType(clazz);
				}
				catch (Throwable t)
				{
					//check android.widget
					try {
						Class<?> clazz = Class.forName(String.format(Locale.US, "android.widget.%s", selector.toString()));
						droidQuery.selectByType(clazz);
					}
					catch (Throwable t2)
					{
						//check android.webkit
						try {
							Class<?> clazz = Class.forName(String.format(Locale.US, "android.webkit.%s", selector.toString()));
							droidQuery.selectByType(clazz);
						}
						catch (Throwable t3)
						{
							//could not select class
							Log.w("droidQuery", String.format(Locale.US, "Could not select class %s.", selector.toString()));
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
