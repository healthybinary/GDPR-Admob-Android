package com.ayoubfletcher.consentsdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.consent.DebugGeography;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.net.MalformedURLException;
import java.net.URL;

public class ConsentSDK {

    public abstract static class ConsentSDKCallback {
        abstract public void onResult();
    }

    public abstract static class ConsentInformationCallback {
        abstract public void onResult(ConsentInformation consentInformation, ConsentStatus consentStatus);
        abstract public void onFailed(ConsentInformation consentInformation, String reason);
    }

    public abstract static class LocationIsEeaOrUnknownCallback {
        abstract public void onResult(boolean isRequestLocationInEeaOrUnknown);
    }

    private static String ads_preference = "ads_preference";
    private static String user_status = "user_status";
    private static boolean PERSONALIZED = true;
    private static boolean NON_PERSONALIZED = false;
    private static String preferences_name = "com.ayoubfletcher.consentsdk";

    private Context context;
    private ConsentForm form;
    private String LOG_TAG = "ID_LOG";
    private String DEVICE_ID = "";
    private boolean DEBUG = false;
    private String privacyURL;
    private String publisherId;
    public ConsentSDK consentSDK;

    // Admob banner test id
    private static String DUMMY_BANNER = "ca-app-pub-3940256099942544/6300978111";

    private SharedPreferences settings;


    // Builder class
    public static class Builder {

        private Context context;
        private String LOG_TAG = "ID_LOG";
        private String DEVICE_ID = "";
        private boolean DEBUG = false;
        private String privacyURL;
        private String publisherId;

        // Initialize Builder
        public Builder(Context context) {
            this.context = context;
        }

        // Add test device id
        public Builder addTestDeviceId(String device_id) {
            this.DEVICE_ID = device_id;
            this.DEBUG = true;
            return this;
        }

        // Add privacy policy
        public Builder addPrivacyPolicy(String privacyURL) {
            this.privacyURL = privacyURL;
            return this;
        }

        // Add Publisher Id
        public Builder addPublisherId(String publisherId) {
            this.publisherId = publisherId;
            return this;
        }

        // Add Logcat id
        public Builder addCustomLogTag(String LOG_TAG) {
            this.LOG_TAG = LOG_TAG;
            return this;
        }

        // Build
        public ConsentSDK build() {
            if(this.DEBUG) {
                ConsentSDK consentSDK = new ConsentSDK(context, publisherId, privacyURL, true);
                consentSDK.LOG_TAG = this.LOG_TAG;
                consentSDK.DEVICE_ID = this.DEVICE_ID;
                return consentSDK;
            } else {
                return new ConsentSDK(context, publisherId, privacyURL);
            }
        }
    }

    // Initialize dummy banner
    public static void initDummyBanner(Context context) {
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(DUMMY_BANNER);
        adView.loadAd(new AdRequest.Builder().build());
    }

    // Initialize debug
    public ConsentSDK(Context context, String publisherId, String privacyURL, boolean DEBUG) {
        this.context = context;
        this.settings = context.getSharedPreferences(preferences_name, Context.MODE_PRIVATE);
        this.publisherId = publisherId;
        this.privacyURL = privacyURL;
        this.DEBUG = DEBUG;
        this.consentSDK = this;
    }

    // Initialize production
    public ConsentSDK(Context context, String publisherId, String privacyURL) {
        this.context = context;
        this.settings = context.getSharedPreferences(preferences_name, Context.MODE_PRIVATE);
        this.publisherId = publisherId;
        this.privacyURL = privacyURL;
        this.consentSDK = this;
    }

    // Consent status
    public static boolean isConsentPersonalized(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getBoolean(ads_preference, PERSONALIZED);
    }

    // Consent is personalized
    private void consentIsPersonalized() {
        settings.edit().putBoolean(ads_preference, PERSONALIZED).apply();
    }

    // Consent is non personalized
    private void consentIsNonPersonalized() {
        settings.edit().putBoolean(ads_preference, NON_PERSONALIZED).apply();
    }

    // Consent is within
    private void updateUserStatus(boolean status) {
        settings.edit().putBoolean(user_status, status).apply();
    }

    // Get AdRequest
    public static AdRequest getAdRequest(Context context) {
        if(isConsentPersonalized(context)) {
            return new AdRequest.Builder().build();
        } else {
            return new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, getNonPersonalizedAdsBundle())
                    .build();
        }
    }

    // Get Non Personalized Ads Bundle
    private static Bundle getNonPersonalizedAdsBundle() {
        Bundle extras = new Bundle();
        extras.putString("npa", "1");
        return extras;
    }

    // Consent information
    private void initConsentInformation(final ConsentInformationCallback callback) {
        final ConsentInformation consentInformation = ConsentInformation.getInstance(context);
        if(DEBUG) {
            if(!DEVICE_ID.isEmpty()) {
                consentInformation.addTestDevice(DEVICE_ID);
            }
            consentInformation.setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        }
        String[] publisherIds = {publisherId};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                if(callback != null) {
                    callback.onResult(consentInformation, consentStatus);
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String reason) {
                callback.onFailed(consentInformation, reason);
            }
        });
    }

    // Check if the location is EEA
    public void isRequestLocationIsEeaOrUnknown(final LocationIsEeaOrUnknownCallback callback) {
        // Get Consent information
        initConsentInformation(new ConsentInformationCallback() {
            @Override
            public void onResult(ConsentInformation consentInformation, ConsentStatus consentStatus) {
                callback.onResult(consentInformation.isRequestLocationInEeaOrUnknown());
            }

            @Override
            public void onFailed(ConsentInformation consentInformation, String reason) {
                callback.onResult(false);
            }
        });
    }

    // Check the user location
    public boolean isUserLocationWithinEea() {
        return this.settings.getBoolean(user_status, false);
    }


    // Toggle View
    public void consentToggleView(View view, boolean toggleView) {
        toggleView(view, null, toggleView);
    }

    // Toggle View
    public void consentToggleView(View view, ConsentSDKCallback callback, boolean toggleView) {
        toggleView(view, callback, toggleView);
    }

    // Toggle View
    private void toggleView(final View view, final ConsentSDKCallback callback, final boolean toggleView) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Request consent
                consentSDK.requestConsent(callback);
            }
        });
        // Hide Consent SDK
        consentSDK.isRequestLocationIsEeaOrUnknown(new LocationIsEeaOrUnknownCallback() {
            @Override
            public void onResult(boolean isRequestLocationInEeaOrUnknown) {
                // If toggle view turned on
                if(toggleView) {
                    if(isRequestLocationInEeaOrUnknown) {
                        // Request consent
                        view.setVisibility(View.VISIBLE);
                        view.setClickable(true);
                        view.setFocusable(true);
                    } else {
                        view.setVisibility(View.GONE);
                        view.setClickable(false);
                        view.setFocusable(false);
                    }
                }
            }
        });
    }

    // Initialize Consent SDK
    public void checkConsent(final ConsentSDKCallback callback) {
        // Initialize consent information
        initConsentInformation(new ConsentInformationCallback() {
            @Override
            public void onResult(ConsentInformation consentInformation, ConsentStatus consentStatus) {
                // Switch consent
                switch(consentStatus) {
                    case UNKNOWN:
                        // Debugging
                        if (DEBUG) {
                            Log.d(LOG_TAG, "Unknown Consent");
                            Log.d(LOG_TAG, "User location within EEA: " + consentInformation.isRequestLocationInEeaOrUnknown());
                        }
                        // Check the user status
                        if(consentInformation.isRequestLocationInEeaOrUnknown()) {
                            requestConsent(callback);
                        } else {
                            consentIsPersonalized();
                            // Callback
                            callback.onResult();
                        }
                        break;
                    case NON_PERSONALIZED:
                        consentIsNonPersonalized();
                        // Callback
                        callback.onResult();
                        break;
                    default:
                        consentIsPersonalized();
                        // Callback
                        callback.onResult();
                        break;
                }
                // Update user status
                updateUserStatus(consentInformation.isRequestLocationInEeaOrUnknown());
            }

            @Override
            public void onFailed(ConsentInformation consentInformation, String reason) {
                if(DEBUG) {
                    Log.d(LOG_TAG, "Failed to update: $reason");
                }
                // Update user status
                updateUserStatus(consentInformation.isRequestLocationInEeaOrUnknown());
            }
        });
    }

    // Request Consent
    public void requestConsent(final ConsentSDKCallback callback) {
        URL privacyUrl = null;
        try {
            privacyUrl = new URL(privacyURL);
        } catch (MalformedURLException e) {
//            e.printStackTrace();
        }
        form = new ConsentForm.Builder(context, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        if(DEBUG) {
                            Log.d(LOG_TAG, "Consent Form is loaded!");
                        }
                        form.show();
                    }

                    @Override
                    public void onConsentFormError(String reason) {
                        if(DEBUG) {
                            Log.d(LOG_TAG, "Consent Form ERROR: $reason");
                        }
                        // Callback on Error
                        if (callback != null) {
                            callback.onResult();
                        }
                    }

                    @Override
                    public void onConsentFormOpened() {
                        if(DEBUG) {
                            Log.d(LOG_TAG, "Consent Form is opened!");
                        }
                    }

                    @Override
                    public void onConsentFormClosed(ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        if(DEBUG) {
                            Log.d(LOG_TAG, "Consent Form Closed!");
                        }
                        // Check the consent status and save it
                        switch (consentStatus) {
                            case NON_PERSONALIZED:
                                consentIsNonPersonalized();
                                break;
                            default:
                                consentIsPersonalized();
                                break;
                        }
                        // Callback
                        if(callback != null) {
                            callback.onResult();
                        }
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .build();
        form.load();
    }

}
