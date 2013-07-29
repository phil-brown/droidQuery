## Reddit Client

Reddit Client provides a basic interface for displaying the most recent posts in a given *subreddit*.
The search field allows users to search for these subreddits.

Other search types are not permitted in this basic version, because the URL will return `XML` instead of
`JSON`. Although XML is trivial to parse, it is not needed in this example application.

This example also showcases a lot of features of my latest open source project, [droidQuery](http://bit.ly/droidQuery).
Using droidQuery, one can set event listeners, handle attributes and animations, add UI elements (like images and masks)
and perform asynchronous HTTP tasks (via `Ajax`).

One challenge with this application was providing support for `SearchView` while still supporting API 10
(SearchView is introduced in API 11). This challenge was overcome using `ActionBarSherlock`, which will
fall back to the native Search View if the API level 11 or higher, or will use a custom implementation
for API 10. This searchView also had some limitations, like not allowing suggestions.

The final time it took for me to complete development was around 6-7 hours. The 3 days/12 hours limit was
beyond plenty, but the time up front was nice to use to update my SDKs and research some of the requirements.

I found this project to be fairly simple. In fact, it is quite a lot like some other clients I have made
recently either for job applications or for `droidQuery` sample applications, so on a scale of 1-10, I would
say this was around a 3. There were some nice complexities, and backward-compatibilities I needed to work
with, but it was very straight forward how to proceed.

During development, I added some calls to `droidQuery` which made development even easier. These include
the call `mask` methods, which allow a developer to mask any View with the given Image source (Bitmap/Drawable/
Resource/URL/asset/File path), making masking as simple as:

    $.with(view).mask(R.id.myMask);
    
All in all, it was a good challenge to show off my skills working with libraries, async tasks, UI components,
etc. I will probably be adding it as an example droidQuery app soon too.

