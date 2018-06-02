package com.ayoubfletcher.consentsdkexample;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.ayoubfletcher.consentsdk.ConsentSDK;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

//        // Initialize a dummy banner using the default test banner id provided by google to get the device id from logcat using 'Ads' tag
//        ConsentSDK.initDummyBanner(this);

        // Initialize ConsentSDK
        ConsentSDK consentSDK = new ConsentSDK.Builder(this)
                .addTestDeviceId("your device id from logcat") // Add your test device id "Remove addTestDeviceId on production!"
                .addCustomLogTag("CUSTOM_TAG") // Add custom tag default: ID_LOG
                .addPrivacyPolicy("https://your.privacy.url/") // Add your privacy policy url
                .addPublisherId("pub-0123456789012345") // Add your admob publisher id
                .build();

        // To check the consent and to move to MainActivity after everything is fine :).
        consentSDK.checkConsent(new ConsentSDK.ConsentCallback() {
            @Override
            public void onResult(boolean isRequestLocationInEeaOrUnknown) {
                goToMain();
            }
        });

        // Loading indicator
        loadingHandler();
    }

    // Go to MainActivity
    private void goToMain() {
        // Wait few seconds just to show my stunning loading indication, you like it right :P.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Go to main after the consent is done.
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);
    }

    /**
     * Some stuff to tell that your app is loading and it's not lagging.
     */
    // Loading indicator handler
    private void loadingHandler() {
        final TextView loadingTxt = findViewById(R.id.loadingTxt);
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                // Loading Txt
                if(loadingTxt.getText().length() > 10) {
                    loadingTxt.setText("Loading ");
                } else {
                    loadingTxt.setText(loadingTxt.getText()+".");
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(runnable, 500);
    }
}
