/*
 * Copyright (c) 2016 Krumbs Inc
 * All rights reserved.
 *
 */
package io.krumbs.sdk.starter;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.Map;

import io.krumbs.sdk.KrumbsSDK;
import io.krumbs.sdk.krumbscapture.KCaptureCompleteListener;


public class MainActivity extends AppCompatActivity {
  private View cameraView;
  private View startCaptureButton;
  private View helloText;
  LocationManager locManager;
  Button reco;
    private MockLocationProvider mock;
    double fromLat=0; double fromLong=0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
    startCaptureButton = findViewById(R.id.kcapturebutton);
    reco = (Button) findViewById(R.id.recobutton);
    helloText = findViewById(R.id.hello_world);
    cameraView = findViewById(R.id.camera_container);

      // Set Mock location
      mock = new MockLocationProvider(LocationManager.GPS_PROVIDER, this);
      //Set test location
      mock.pushLocation(33.648458, -117.841960); // Starbucks location

    // user location
    try{
      locManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
      locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000L,500.0f, new LocationFinder());
      Location location = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      if(location!=null){
          fromLat = location.getLatitude();
          fromLong =location.getLongitude();
          Log.d("NEXTGEN","Location is lat "+fromLat+"long "+fromLong);
      } else {
          fromLat =33.6839470;
          fromLong= -117.7946940;
          Log.d("NEXTGEN","NO Location preset to "+fromLat+" "+fromLong);
      }
        startCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCaptureButton.setVisibility(View.INVISIBLE);
                helloText.setVisibility(View.INVISIBLE);
                cameraView.setVisibility(View.VISIBLE);
                startCapture();
            }
        });

        reco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRecommendations();
            }
        });

        //call APIS
        /*ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new FoursquareAPI(fromLat,fromLong,"").execute();
            //new EventfulAPI(fromLat,fromLong).execute();
        } else {
            fromLat=33.6839470;
            fromLong = -117.7946940;
            Log.d("NEXTGEN","No network connection available.");
        }*/

    } catch (SecurityException e){
        e.printStackTrace();
    }
  }

  private void getRecommendations(){
      ConnectivityManager connMgr = (ConnectivityManager)
              getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
      if (networkInfo != null && networkInfo.isConnected()) {
          new RecommendationAPI(this,fromLat,fromLong).execute();
      }
  }
  private void startCapture() {
    int containerId = R.id.camera_container;
// SDK usage step 4 - Start the K-Capture component and add a listener to handle returned images and URLs
      KrumbsSDK.startCapture(containerId, this, new KCaptureCompleteListener() {
          @Override
          public void captureCompleted(CompletionState completionState, boolean audioCaptured, Map<String, Object> map) {
              // DEBUG LOG
              if (completionState != null) {
                  Log.d("KRUMBS-CALLBACK", completionState.toString());
              }
              if (completionState == CompletionState.CAPTURE_SUCCESS) {
// The local image url for your capture
                  String imagePath = (String) map.get(KCaptureCompleteListener.CAPTURE_MEDIA_IMAGE_PATH);
                  if (audioCaptured) {
// The local audio url for your capture (if user decided to record audio)
                      String audioPath = (String) map.get(KCaptureCompleteListener.CAPTURE_MEDIA_AUDIO_PATH);
                  }
// The mediaJSON url for your capture
                  String mediaJSONUrl = (String) map.get(KCaptureCompleteListener.CAPTURE_MEDIA_JSON_URL);
                  postData(mediaJSONUrl);
                  Log.i("KRUMBS-CALLBACK", mediaJSONUrl + ", " + imagePath);
                  cameraView.setVisibility(View.INVISIBLE);
                  startCaptureButton.setVisibility(View.VISIBLE);
                  helloText.setVisibility(View.VISIBLE);
              } else if (completionState == CompletionState.CAPTURE_CANCELLED ||
                      completionState == CompletionState.SDK_NOT_INITIALIZED) {
                  cameraView.setVisibility(View.INVISIBLE);
                  startCaptureButton.setVisibility(View.VISIBLE);
                  helloText.setVisibility(View.VISIBLE);
              }
          }
      });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

   public void postData(String mediaJSONURL)
   {
       ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
       NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
       if (networkInfo != null && networkInfo.isConnected()) {
           new FoursquareAPI(fromLat,fromLong,mediaJSONURL).execute();
       }
   }



}
