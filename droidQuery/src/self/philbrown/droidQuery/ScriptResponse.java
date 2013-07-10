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
 * Object used with ScriptResponseHandler to return the resulting output of running the script,
 * as well as the text of the script itself.
 * @author Phil Brown
 * @see ScriptResponseHandler
 */
public class ScriptResponse 
{
	/** Script Output after it is run in the Android shell */
	public String output;
	/** The raw Script Object */
	public Script script;
	/** The textual representation of the Script Object */
	public String text;
}
