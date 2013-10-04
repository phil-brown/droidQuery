## droidQuery

### Introduction

__droidQuery__ is an Android *port* of [jQuery](https://github.com/jquery/jquery), and is designed to
be as syntactically alike as possible in Java.

For those not familiar with *jQuery*, it essentially provides magic for allowing the simultaneous
manipulation of a set of UI entities (using animations, attributes settings, etc), as well as to
perform complex tasks, such as asynchronous network tasks. *droidQuery* can do all of these things.

Essentially, *droidQuery* provides this same type of magic for the view hierarchy and `AsyncTasks`, and
can be used to perform other frequent jobs, such as showing alert messages. Also like *jQuery*,
*droidQuery* allows the addition of extensions to add to the power of the library.

Popular extensions currently available include [droidProgress](https://github.com/phil-brown/droidProgress), 
which can show a progress bar or spinner, and [droidMail](https://github.com/phil-brown/droidMail), 
which allows email to be configured and sent without using `Intent`. A full listing can be found on the
[wiki](https://github.com/phil-brown/droidQuery/wiki/Available-extensions). If you have created a new *droidQuery*
extension, please let me know, and I can add a link on the wiki.

*droidQuery* is intended to be used by all Android developers, as it greatly simplifies the procedures 
for performing many common tasks. *droidQuery* can also be used to help web developers that are familiar
with *jQuery* to get into Android development.

### How to Include droidQuery in your Project

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

### How to Use

> Note: droidQuery is a work in progress. If you find any bugs or would like functionality that is missing, please create a new issue (https://github.com/phil-brown/droidQuery/issues).

Below are some of the most common tasks that *droidQuery* can be used for. A full list, as well as 
examples, is currently under construction in the [wiki](https://github.com/phil-brown/droidQuery/wiki/API-Documentation).
A sample application can also be found in the `droidQueryTest` directory. The relevant code can be found
in [ExampleActivity.java](https://github.com/phil-brown/droidQuery/blob/master/droidQueryTest/src/self/philbrown/droidQuery/Example/ExampleActivity.java).
You may also browse the *javadocs* [here](http://phil-brown.github.io/droidQuery/doc/).
Finally, most of the [jQuery API Documentation](http://api.jquery.com) is sufficient to explain the *droidQuery* API.

To **instantiate** a new *droidQuery*, you need to pass in a `Context`, a `View`, or set of `View`s. The
simplest way to create the instance is using the `with` static methods:

    $.with(Context);
    $.with(View);
    $.with(List<View>);
    $.with(View[]);
    
If `Context` is passed, *droidQuery* will attempt to manipulate the root view. For example, if `Context`
is an `Activity`, the content view will be selected. There is also a way to select a `View` using it's id:

    $.with(Context).id(Integer);
    
or, for short:
    
    $.with(Context, Integer);
    
Once you have the *droidQuery* instance, you can either save it as a variable, or chain calls to manipulate
the selected `View` or `View`s.

**Ajax**

To perform an asynchronous network task, you can use *ajax*. The most straight-forward way to create and
start an ajax task is with the `$.ajax(AjaxOptions)` method. For example:

    $.ajax(new AjaxOptions().url("http://www.example.com")
                            .type("GET")
                            .dataType("text")
                            .context(this)
                            .success(new Function() {
                                @Override
                                public void invoke($ droidQuery, Object... params) {
                                    droidQuery.alert((String) params[0]);
                                }
                            }).error(new Function() {
                                @Override
                                public void invoke($ droidQuery, Object... params) {
                                    int statusCode = (Integer) params[1];
                                    String error = (String) params[2];
                                    Log.e("Ajax", statusCode + " " + error);
                                }
                            }));

**Attributes**

*droidQuery* can be used to get or change the attributes of its selected `View`s. The most common
methods include `attr()` to get an attribute, `attr(String, Object)` to set an attribute, `val()` to
get the value of a UI element (such as `CharSequence` for `TextView`s, `Drawable`s for `ImageView`s, etc),
and `val(Object)` to set the value.

**Callbacks**

The *Callbacks* Object provides a simple way to manage and fire sets of callbacks. To get an instance
of this Object, use `$.Callbacks(this)`.

**Effects**

*droidQuery* can be used to animate the selected `View`s. The simplest way to perform a custom animation
is by using the `animate(String, long, Easing, Function)` method. For example:

    $.with(myView).children().animate("{left: 100px, top: 100, width: 50%, height: 50% }", 400, Easing.LINEAR, new Function() {
    	@Override
    	public void invoke($ droidQuery, Object... params)
    	{
    		droidQuery.toast("animation complete", Toast.LENGTH_SHORT);
    	}
    });

It can also be used to perform pre-configured animations, such as fades (using `fadeIn`, `fadeOut`, 
`fadeTo`, and `fadeToggle`) and slides (`slideUp`, `slideDown`, `slideLeft`, and `slideRight`).

**Events**

*droidQuery* can be used to register events (such user input or view changes) on the selected UI elements.
This can be done using the following methods: `bind`, `on`, `one`, `change`, `click`, `longclick`, `swipe`,
`swipeUp`, `swipeLeft`, `swipeDown`, `swipeRight`, `focus`, `focusOut`, `keyDown`, `keyUp`, `keyPress`,
`select`, and `unbind`. For example:

    //Register a click event
    $.with(this, R.id.btn_refresh).click(new Function() {
		@Override
		public void invoke($ droidQuery, Object... params) {
			droidQuery.alert("refresh");
			refresh();
		}
    });
    
    //or use the "on" method to register a click event.
    $.with(this, R.id.btn_refresh).on("click", new Function() {
    	@Override
		public void invoke($ droidQuery, Object... params) {
    		droidQuery.toast("Refresh", Toast.LENGTH_LONG);
    		refresh();
		}
    });

**Selectors**

The real magic behind *droidQuery* is its ability to manipulate a set of UI elements at one instance.
a `View` or a set of `View`s can be passed to a *droidQuery* instance using any of the *with* methods,
or a new instance of *droidQuery* containing a set of *View*s can be created using any of the selector
methods, including `view`, `child`, `parent`, `children`, `siblings`, `slice`, `selectAll`, `selectByType`,
`selectChildren`, `selectEmpties`, `selectFocused`, `selectHidden`, `selectVisible`, `id`, `selectImages`,
`selectOnlyChilds`, and `selectParents`.

**Miscellaneous**

*droidQuery* also comes with several methods that simplify a lot of common tasks. including:

* __each(Function)__ - invokes the given function for each selected View
* __map(String)/map(JSONObject)__ - converts a JSON String or a JSONObject to a Map Object
* __map(Entry...)__ - quickly make a Map Object
* __entry(String, Object)__ - quickly make a Map Entry Object
* __alert__ - show an alert dialog
* __toast__ - show a `Toast` message
* __write__ - write text to a file
* __parseJSON__ - parses a JSON string and returns a JSONObject
* __parseXML__ - parses an XML string and returns a Document Object

**A note about Scripts**

In *jQuery*, there is an `Ajax` type called `Script`, which can be used to download a `Javascript` file.
This type also exists in *droidQuery*, but instead of `Javascript`, it expects a `Bourne` script, which
is runnable on the Android command line. Common usage for such a feature include running an existing script,
without the need to port to `Java`, or to run `Android Debug Bridge` (adb) commands. For example, say the
*POST* request to `http://www.example.com/settings` returns a `bourne` script as a response to issue a 
command based on the current application settings. The command, for example, could broadcast an `Intent`
to open an app:

    am broadcast -a android.intent.action.CAMERA_BUTTON
    
The request would likely look like this:

    $.ajax("{url: 'http://www.example.com/settings', type: 'post', dataType: 'script', data: '{id: 4, setting: 1}' }");
    
and as long as the request was successful, the native camera app would open once the response came back.

If the script does not issue an *adb* command, but instead calculates some data, the response would include the script
output.

**Special Thanks**

This project uses [AsyncTaskEx](https://github.com/commonsguy/cwac-task) to allow an unlimited number
of simultaneous network tasks. It also uses [NineOldAndroids](http://nineoldandroids.com) to provide 
animation support for Pre-Honeycomb versions of Android. Finally, this project would not exist if it 
were not for the excellent *jQuery* library, and its excellent documentation.

    