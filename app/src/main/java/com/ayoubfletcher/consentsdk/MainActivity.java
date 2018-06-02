package com.ayoubfletcher.consentsdk;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

public class MainActivity extends AppCompatActivity {

    private TextView messageTxt;
    private ConsentSDK consentSDK = null;
    private Button showIntesttitlaBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize views
        loadBanner();
        showIntesttitlaBtn = findViewById(R.id.show_interstitial);
        showIntesttitlaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadInterstitial();
            }
        });


        // Initialize Views
        messageTxt = findViewById(R.id.message);
        // Initialize ConsentSDK
        initConsentSDK(this);

        // Checking the status of the user
        if(ConsentSDK.isUserLocationWithinEea(this)) {
            String choice = ConsentSDK.isConsentPersonalized(this)? "Personalize": "Non-Personalize";
            messageTxt.setText(getString(R.string.user_within_eea_msg) + choice);
            findViewById(R.id.consent_setting_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Check Consent SDK
//                    // Request the consent without callback
//                    consentSDK.requestConsent(null);
                    //To get the result of the consent
                    consentSDK.requestConsent(new ConsentSDK.ConsentStatusCallback() {
                        @Override
                        public void onResult(boolean isRequestLocationInEeaOrUnknown, int isConsentPersonalized) {
                            String choice = "";
                            switch (isConsentPersonalized) {
                                case 0:
                                    choice = "Non-Personalize";
                                    break;
                                case 1:
                                    choice = "Personalized";
                                    break;
                                case -1:
                                    choice = "Error accrued";
                            }
                            // Update the message
                            messageTxt.setText(getString(R.string.user_within_eea_msg) + choice);
                        }
                    });
                }
            });
        } else {
            messageTxt.setText(getString(R.string.user_not_within_eea_msg));
            ((LinearLayout) findViewById(R.id.message_container)).removeView(findViewById(R.id.consent_setting_btn));
        }
    }

    // Load banner ads
    private void loadBanner() {
        AdView adView = findViewById(R.id.adView);
        // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
        adView.loadAd(ConsentSDK.getAdRequest(this));
    }

    // Load Interstitial
    private void loadInterstitial() {
        final InterstitialAd interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
        interstitialAd.loadAd(ConsentSDK.getAdRequest(this));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Show interstitial
                interstitialAd.show();
                super.onAdLoaded();
            }
        });
    }

    // Initialize consent
    private void initConsentSDK(Context context) {
        // Initialize ConsentSDK
        consentSDK = new ConsentSDK.Builder(context)
                .addTestDeviceId("your device id from logcat") // Add your test device id "Remove addTestDeviceId on production!"
                .addCustomLogTag("CUSTOM_TAG") // Add custom tag default: ID_LOG
                .addPrivacyPolicy("https://your.privacy.url/") // Add your privacy policy url
                .addPublisherId("pub-0123456789012345") // Add your admob publisher id
                .build();
    }
}
