package com.biankaroppelt.masterthesis;

import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

public class WearActivity extends WearableActivity {

   private TextView mTextView;

   private static final String TAG = WearActivity.class.getSimpleName();


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setupUi();
   }

   private void setupUi() {
      setContentView(R.layout.activity_wear);
      setAmbientEnabled();
      final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
      stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
         @Override
         public void onLayoutInflated(WatchViewStub stub) {
            mTextView = (TextView) stub.findViewById(R.id.text);
         }
      });

   }

   @Override
   public void onEnterAmbient(Bundle ambientDetails) {
      super.onEnterAmbient(ambientDetails);
      mTextView.setTextColor(Color.WHITE);
      mTextView.getPaint()
            .setAntiAlias(false);
   }

   @Override
   public void onExitAmbient() {
      super.onExitAmbient();
      mTextView.setTextColor(Color.GREEN);
      mTextView.getPaint()
            .setAntiAlias(true);
   }

   @Override
   public void onUpdateAmbient() {
      super.onUpdateAmbient();

      // Update the content (once a minute)
   }
}
