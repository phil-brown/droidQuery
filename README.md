## droidQuery

---------------------

### Introduction

__droidQuery__ is an Android *port* of [jQuery](https://github.com/jquery/jquery), and is designed to
be as syntactically alike as possible in Java.

For those not familiar with *jQuery*, it essentially provides magic for allowing the simultaneous
manipulation of a set of UI entities (using animations, attributes settings, etc), as well as to
perform complex tasks, such as asynchronous network tasks.__*droidQuery* can do all of these things.__

Essentially, *droidQuery* provides this same type of magic for the view hierarchy and `AsyncTasks`, and
can be used to perform other frequent jobs, such as showing alert messages. Also like *jQuery*,
*droidQuery* allows the addition of extensions to add to the power of the library.

Popular extensions currently available include [droidProgress](https://github.com/phil-brown/droidProgress), 
which can show a progress bar or spinner, and [droidMail](https://github.com/phil-brown/droidMail), 
which allows email to be configured and sent without using `Intent`. A full listing can be found on the
[wiki](https://github.com/phil-brown/droidQuery/wiki/Available-extensions). If you have created a new *droidQuery*
extension, please let me know, and I can add a link on the wiki.

### Target Audience

*droidQuery* is intended to be used by all Android developers, as it greatly simplifies the procedures 
for performing many common tasks. *droidQuery* can also be used to help web developers that are familiar
with *jQuery* to get into Android development.

### How to Use

The simplest way to include *droidQuery* in your project is to copy [droidquery.jar](https://github.com/phil-brown/droidQuery/blob/master/droidQuery/bin/droidquery.jar)
into your project's `libs` directory. If the `libs` folder does not exist, create it (this will be
automatically included in your build path).

### License

Copyright 2013 Phil Brown

*droidQuery* is licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

### Finally: What droidQuery can do

Below are some of the most common tasks that *droidQuery* can be used for. A full list, as well as 
examples, is currently under construction in the [wiki](https://github.com/phil-brown/droidQuery/wiki/Commands).
A sample application can also be found in the `droidQueryTest` directory. The relevant code can be found
in [ExampleActivity.java](https://github.com/phil-brown/droidQuery/blob/master/droidQueryTest/src/self/philbrown/droidQuery/Example/ExampleActivity.java).

