package com.example.tanmay.firebaseconfig;

import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.appcompat.*;
import android.support.v7.appcompat.BuildConfig;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class MainActivity extends AppCompatActivity {

    private static final String TAG="MainActivity";

    //Remote Config keys
    private static final String LOADING_PHRASE_CONFIG_KEY="loading_phrase";
    private static final String WELCOME_MESSAGE_KEY="welcome_message";
    private static final String WELCOME_MESSAGE_CAPS_KEY="welcome_message_caps";

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private TextView mWelcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWelcomeTextView=(TextView)findViewById(R.id.welcomeTextView);

        Button fetchButton=(Button)findViewById(R.id.fetchButton);
        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchWelcome();
            }
        });

        //get Remote Config instance
        //[Start get_remote_congif_instance]
        mFirebaseRemoteConfig=FirebaseRemoteConfig.getInstance();
        //[End get_remote_config_instance]

        //Create a Remote Config setting to enable developer mode, which you can use to increase
        //the number of fetches available per hour during development.
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        //Set default Remote Config parameter values. An app uses the in-app default values, and
        //when you need to adjust those defaults, you set an updated value for only the values you
        //want to change in the Firebase console.

        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        fetchWelcome();
    }

    //Fetch a Welcome from the Remote Config service and then activate it.
    private void fetchWelcome() {
        mWelcomeTextView.setText(mFirebaseRemoteConfig.getString(LOADING_PHRASE_CONFIG_KEY));
        long cacheExpiration = 3600;
        //I don't know why dev mode is not being detected so
        //you can set long cacheExpiration =0; for testing purpose

        //if app is in developer mode the cache expiration is set to 0, so each fetch
        //retrives values from the service

        if(mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()){
            cacheExpiration=0;
            Toast.makeText(getApplicationContext(),"Dev mode active",Toast.LENGTH_SHORT).show();
            Log.v(TAG,"Dev mode is activated");
        }

        //cacheExpirationSeconds is set to cacheExpiration here, indicating the next fetch request
        //will use fetch data from the Remote Config service, rather than cached parameter values,
        //if cached parameter values are more than cacheExpiration seconds old.

        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this,"Fetch Succeeded",
                                    Toast.LENGTH_SHORT).show();

                            //After config data is successfully fetched, it must be activated before newly fetched
                            //values are returned
                            mFirebaseRemoteConfig.activateFetched();
                        }else {
                            Toast.makeText(MainActivity.this,"Fetch Failed",Toast.LENGTH_SHORT).show();
                        }
                        displayWelcomeMessage();
                    }
                });
    }

    // Display welcome message in all caps if welcome_message_caps is set to true
    //Otherwise, display a welcome message as fetched from welcome_message
    private void displayWelcomeMessage() {
        String welcomeMessage=mFirebaseRemoteConfig.getString(WELCOME_MESSAGE_KEY);
        if(mFirebaseRemoteConfig.getBoolean(WELCOME_MESSAGE_CAPS_KEY)){
            mWelcomeTextView.setAllCaps(true);
        }else {
            mWelcomeTextView.setAllCaps(false);
        }
        mWelcomeTextView.setText(welcomeMessage);
    }
}
