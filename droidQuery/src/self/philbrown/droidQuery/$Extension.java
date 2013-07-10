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

/**
 * Superclass for all droidQuery extensions. For examples of working extensions, see 
 * <a href="https://github.com/phil-brown/droidProgress">droidProgress</a> and 
 * <a href="https://github.com/phil-brown/droidMail">droidMail</a>.
 * @author Phil Brown
 *
 */
public abstract class $Extension 
{
	/**
	 * Constructor
	 * @param droidQuery
	 */
	public $Extension($ droidQuery)
	{
		
	}
	
	/**
	 * Called when the extension is loaded.
	 * @param args
	 */
	protected abstract void invoke(Object... args);
}
