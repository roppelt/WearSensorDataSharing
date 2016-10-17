package com.biankaroppelt.masterthesis;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.biankaroppelt.masterthesis.data.Sensor;
import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.NewSensorEvent;
import com.biankaroppelt.masterthesis.events.SensorUpdatedEvent;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.squareup.otto.Subscribe;

import java.util.List;

public class MobileActivity extends AppCompatActivity {


   private RemoteSensorManager remoteSensorManager;

   private static final String TAG = MobileActivity.class.getSimpleName();

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_mobile);
      remoteSensorManager = RemoteSensorManager.getInstance(this);
   }


   @Override
   protected void onResume() {
      super.onResume();
      BusProvider.getInstance().register(this);
      List<Sensor> sensors = RemoteSensorManager.getInstance(this).getSensors();
//      pager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager(), sensors));
      remoteSensorManager.startMeasurement();

   }

   @Override
   protected void onPause() {
      super.onPause();
      BusProvider.getInstance().unregister(this);

      remoteSensorManager.stopMeasurement();
   }
   @Subscribe
   public void onNewSensorEvent(final NewSensorEvent event) {
      System.out.println("onNewSensorEvent: " + event.getSensor());
   }
   @Subscribe
   public void onSensorUpdatedEvent(final SensorUpdatedEvent event) {
      System.out.println("onSensorUpdatedEvent: " + event.getSensor() + " - " + event.getDataPoint());
   }

}
