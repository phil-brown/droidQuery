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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;

import self.philbrown.droidQuery.$;
import self.philbrown.droidQuery.AnimationOptions;
import self.philbrown.droidQuery.Function;
import android.content.Context;
import android.view.View;

import com.osbcp.cssparser.CSSParser;
import com.osbcp.cssparser.PropertyValue;
import com.osbcp.cssparser.Rule;
import com.osbcp.cssparser.Selector;

public class StyleSheet {

	/**
	 * keeps track of styles by type
	 */
	private List<Rule> rules;
	
	private StyleSheet(List<Rule> rules){
		this.rules = rules;
		//TODO: add optimization by setting all values now
	}
	
	public static StyleSheet fromAsset(Context context, String assetPath) throws Exception
	{
		InputStream in = context.getAssets().open(assetPath);
		
		InputStreamReader stream = new InputStreamReader(in);
		
		BufferedReader inputReader = new BufferedReader(stream);
		String line = null;
		
		StringBuilder builder = new StringBuilder();
        while ((line = inputReader.readLine()) != null) 
        {
        	builder.append(line);
        }
        inputReader.close();
        stream.close();
        in.close();

        return new StyleSheet(CSSParser.parse(builder.toString()));
	}
	
	public static StyleSheet fromString(String css) throws Exception
	{
		return new StyleSheet(CSSParser.parse(css));
	}
	
	/**
	 * Recursively apply rules to view and subviews
	 * @param layout
	 * @param rules
	 * @see CSSSelector
	 */
	public void applyRules(View layout)
	{
		//clean approach using CSSSelector.java
		
		for (Rule r : rules)
		{
			List<Selector> selectors = r.getSelectors();
			for (Selector s : selectors)
			{
				applyProperties(CSSSelector.makeSelection(layout, s.toString()), r.getPropertyValues());
			}
			
		}
	}
	
	/**
	 * Recursively apply rules to view and subviews
	 * @param layout
	 * @param rules
	 * @see CSSSelector
	 */
	public void applyRules($ droidQuery)
	{
		//clean approach using CSSSelector.java
		
		for (Rule r : rules)
		{
			List<Selector> selectors = r.getSelectors();
			for (Selector s : selectors)
			{
				applyProperties(CSSSelector.makeSelection(droidQuery, s.toString()), r.getPropertyValues());
			}
			
		}
	}
	
	/**
	 * Apply selector to the given view. This is not recursive.
	 * @param v
	 * @param properties
	 */
	public void applyProperties($ droidQuery, List<PropertyValue> properties)
	{
		//apply actual css properties (big step here)
		for (PropertyValue prop : properties)
		{
			final String property = prop.getProperty();
			final String value = prop.getValue();
			droidQuery.each(new Function() {
				
				@Override
				public void invoke($ droidQuery, Object... params) {
					Object attribute = droidQuery.getAnimationValue(droidQuery.view(0), property, value);
					droidQuery.attr(property, attribute);
				}
			});
		}
	}

}
