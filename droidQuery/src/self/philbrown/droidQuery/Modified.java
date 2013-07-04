/*
 * Copyright (c) 2013 Phil Brown
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT 
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package self.philbrown.droidQuery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a modification to original source code. This may be useful for keeping track of modifications
 * to licensed source code (such as under the Apache License). This annotation can be applied anywhere
 * annotations are allowed.
 * @author <a href="https://github.com/phil-brown">Phil Brown</a>
 */
@Target({ElementType.FIELD, 
	    ElementType.CONSTRUCTOR, 
	    ElementType.METHOD, 
	    ElementType.LOCAL_VARIABLE, 
	    ElementType.PARAMETER, 
	    ElementType.TYPE, 
	    ElementType.PACKAGE,
	    ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface Modified 
{
	/** Specifies who made the changes. */
	public String author();
	
	/** Summary of modifications */
	public String summary();
	
}