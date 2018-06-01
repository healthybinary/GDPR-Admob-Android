# GDPR Android Admob Library
Class helper to easily interact with google consent SDK easily made with LOVE :heart: :earth_africa:.

# Required Dependencies:
> Add it in your root build.gradle at the end of repositories:
```gradle
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
> Step 2. Add the dependency
```gradle
    dependencies {
	        implementation 'com.github.ayoubfletcher.GDPR-Admob-Android:consentsdk:0.1.3'
	}
```
---
# Get requirement you need:
> **Get your publisher id from admob:**
![Publisher Id](http://lh3.googleusercontent.com/jbo5TVXuXU0DEHD3dfyutomLUTtsKTkg9LunCXh8R_DTv7T__91P0e4KLtAt9foPzQ=w895)
[Source](https://support.google.com/admob/answer/2784578?hl=en)

> To get the device id you have to initialize an adrequest
```java
    // Creating a dummy adview
    AdView adView = new AdView(context);
    adView.setAdSize(AdSize.BANNER);
    adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111"); // Default test banner id
    adView.loadAd((new com.google.android.gms.ads.AdRequest.Builder()).build());
    
    // Or using this code is the same code above it uses the test banner id provided by admob. 'ca-app-pub-3940256099942544/6300978111'
    ConsentSDK.initDummyBanner(context);
```
---
# How to use library:
`1.a Initializing using Builder class`
```java
    ConsentSDK consentSDK = new ConsentSDK.Builder(this)
                .addTestDeviceId("your device id from logcat") // Add your test device id "Remove addTestDeviceId on production!"
                .addCustomLogTag("CUSTOM_TAG") // Add custom tag default: ID_LOG
                .addPrivacyPolicy("https://your.privacy.url/") // Add your privacy policy url
                .addPublisherId("pub-0123456789012345") // Add your admob publisher id
                .build();
```
`1.b Initializing without Builder class`
```java
    String deviceId = "your device id from logcat";
    String publisherId = "pub-0123456789012345";
    String privacyUrl = "https://your.privacy.url/";
    ConsentSDK consentSDK = new ConsentSDK(this, publisherId, privacyUrl, true);
```
> Choose one of the methods to initialize the ConsentSDK from above.

`2. Then check the consent using:`
```java
    consentSDK.checkConsent(new ConsentSDK.ConsentSDKCallback() {
            @Override
            public void onResult() {
                // Your code
            }
        });
```
> How checkConsent works:
```
- Check the location of the user if it's within EEA and with unknown status.
- If the user is within EEA and with unknown status show the dialog for consent with two options to see relevant ads or to show less relevant ads.
- The function retrieve a callback after the consent has been submitted or if it's not necessary not show the dialog.
- It's save the consent of the user and if the user is not within EEA it saves show relevant ads status.
```

`3. You should load the ads (banner or interstitial ..) using the function below:`
```java
    // To Load the banner
    adView.loadAd(ConsentSDK.getAdRequest(this));
    // To Load Interstitial
    interstitialAd.loadAd(ConsentSDK.getAdRequest(this));
```
`4. To Check if the user is within EEA or not.`
```java
    // But you must call consent.checkConsent(callback) before to update the status
    consentSDK.isUserLocationWithinEea(); // Returns true if within false if not.
```
`5. To request the consent form to re-edit it for the users within EEA.`
```java
    // To request the consent form to re-edit it for the users within EEA
    consentSDK.requestConsent(new ConsentSDK.ConsentSDKCallback() {
            @Override
            public void onResult() {
                // Your code after the consent is submitted if needed
            }
        });
```
---
# Note:
> This library is just an editing of the official [Google Consent SDK Android](https://github.com/googleads/googleads-consent-sdk-android) with some tweaks and with some helper class to simplify the comply of the app to GDPR policy.

> That's it, if you followed the steps above your app will be comply with the **GDPR** policy.

Made by Ayoub Fletcher with LOVE :heart:.