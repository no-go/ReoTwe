# Twthaar (mini)

Twthaar is a very low level just reading twitter client. You can use the code for build your own cool client or to learn how *Twitter Kit* and API works.

![logo](app/src/main/res/mipmap-hdpi/ic_launcher.png)

## Features

 -  Ads free!
 -  get and add your own API key and secret on apps.twitter.com to authorize the App
 -  lists timeline without images of a given username (+time, likes, retweets)
 -  Browse timelines
     -  extracts username from `RT @username:` and but it to the search field by click on the tweetdate
     -  if it is not a retweet, it try to extract the first `@username`
     -  if there is no `@username`, it uses the username of the posting user / publisher
     -  ATTENTION: there is still no cache, history or browse back function
- multisearch (comma separated) for `@userTimelines` or `#otherStuff`
- very simple android code
- switch images on and of in preferences

## Get the App

You can get a signed APK from here (Android 4.4+): [APK](https://raw.githubusercontent.com/no-go/Twthaar/mini01/app/app-release.apk)

## Screenshots

![Screenshot of Twthaar - mini](screenshot1.png)
![Screenshot of Twthaar - mini with images](screenshot2.png)

## How to get API Codes

... to do 

## License

This is free and unencumbered software released into the public domain.

Anyone is free to copy, modify, publish, use, compile, sell, or distribute this software, either in source code form or as a compiled binary, for any purpose, commercial or non-commercial, and by any means.

In jurisdictions that recognize copyright laws, the author or authors of this software dedicate any and all copyright interest in the software to the public domain. We make this dedication for the benefit of the public at large and to the detriment of our heirs and successors. We intend this dedication to be an overt act of relinquishment in perpetuity of all present and future rights to this software under copyright law.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

For more information, please refer to [http://unlicense.org](http://unlicense.org)

## Privacy Policy

### Personal information.

Personal information is data that can be used to uniquely identify or contact a single person. I DO NOT collect, transmit, store or use any personal information while you use this app.

### Non-Personal information.

I DO NOT collect non-personal information like user's behavior:

 -  to solve App problems
 -  to show personalized ads

The google play store collect non-personal information such as the data of install (country and equipment). 

### Twitter Kit and Twitter API

I use both to build the app. I did not add any google or ad keys or codes for marketing feedback, Ads or payment systems. BUT PLEASE read the *Privacy Policy* of the twitter API, too!

[https://dev.twitter.com/overview/terms/agreement-and-policy](https://dev.twitter.com/overview/terms/agreement-and-policy)

### Privacy Questions.

If you have any questions or concerns about my Privacy Policy or data processing, please contact me.
