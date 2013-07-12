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
 * Represents a command that can be executed.
 * @author Phil Brown
 */
public interface Function 
{
	/**
	 * Variable arguments method that can be called on a Function
	 * @param droidQuery the {@code droidQuery} instance with the selection on the relevant view(s). 
	 * May be {@code null} in some cases, such as static methods, or places where no {@code View} or 
	 * {@code Context} is used.
	 * @param params optional arguments
	 */
	public void invoke($ droidQuery, Object... params);
}