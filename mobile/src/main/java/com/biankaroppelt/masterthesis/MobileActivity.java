package com.biankaroppelt.masterthesis;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.biankaroppelt.masterthesis.data.Sensor;
import com.biankaroppelt.masterthesis.data.SensorDataPoint;
import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.NewSensorEvent;
import com.biankaroppelt.masterthesis.events.SensorDataReceiving;
import com.biankaroppelt.masterthesis.events.SensorDataReceivingStop;
import com.biankaroppelt.masterthesis.events.SensorUpdatedEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class MobileActivity extends Activity {

   private RemoteSensorManager remoteSensorManager;

   private static final String TAG = MobileActivity.class.getSimpleName();

   private TextView dataStatusIdleTextView;
   private TextView dataStatusReceivingTextView;
   private Button startCollectingDataButton;
   private Button stopCollectingDataButton;
   private RecyclerView sensorDataRecyclerView;

   private boolean isCollectingData;
   private SensorDataListAdapter adapter;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_mobile);
      dataStatusIdleTextView = ((TextView) findViewById(R.id.data_status_idle));
      dataStatusReceivingTextView = ((TextView) findViewById(R.id.data_status_receiving));
      startCollectingDataButton = ((Button) findViewById(R.id.button_start_collecting_data));
      stopCollectingDataButton = ((Button) findViewById(R.id.button_stop_collecting_data));
      sensorDataRecyclerView = ((RecyclerView)findViewById(R.id.sensor_data_list));
      remoteSensorManager = RemoteSensorManager.getInstance(this);
      isCollectingData = false;
      setupButtonListener();
      setupRecyclerView();
   }

   private void setupRecyclerView() {
      sensorDataRecyclerView.setHasFixedSize(true);
      LinearLayoutManager layoutManager = new LinearLayoutManager(this);
      sensorDataRecyclerView.setLayoutManager(layoutManager);
      adapter = new SensorDataListAdapter(this, new ArrayList<SensorDataPoint>());
//      DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(sensorDataRecyclerView.getContext(),
//            layoutManager.getOrientation());
//      sensorDataRecyclerView.addItemDecoration(dividerItemDecoration);
      sensorDataRecyclerView.setAdapter(adapter);
   }

   private void setupButtonListener() {
      startCollectingDataButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            remoteSensorManager.startMeasurement();
            isCollectingData = true;
            startCollectingDataButton.setVisibility(View.GONE);
            stopCollectingDataButton.setVisibility(View.VISIBLE);
         }
      });
      stopCollectingDataButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            remoteSensorManager.stopMeasurement();
            isCollectingData = false;
            startCollectingDataButton.setVisibility(View.VISIBLE);
            stopCollectingDataButton.setVisibility(View.GONE);
         }
      });
   }

   @Override
   protected void onResume() {
      super.onResume();
      BusProvider.getInstance()
            .register(this);
      List<Sensor> sensors = RemoteSensorManager.getInstance(this)
            .getSensors();
      //      pager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager(), sensors));
      if (isCollectingData) {
         remoteSensorManager.startMeasurement();
      }
   }

   @Override
   protected void onPause() {
      super.onPause();
      BusProvider.getInstance()
            .unregister(this);

      remoteSensorManager.stopMeasurement();
   }

   @Subscribe
   public void onNewSensorEvent(final NewSensorEvent event) {
      System.out.println("onNewSensorEvent: " + event.getSensor());
   }

   @Subscribe
   public void onSensorUpdatedEvent(final SensorUpdatedEvent event) {
      System.out.println(
            "onSensorUpdatedEvent: " + event.getSensor() + " - " + event.getDataPoint());
      adapter.addDataPoint(event.getDataPoint());
      sensorDataRecyclerView.scrollToPosition(0);
   }

   @Subscribe
   public void onSensorDataReceiving(final SensorDataReceiving event) {
      System.out.println("onSensorDataReceiving");
      //      if (dataStatusTextView != null) {
      dataStatusIdleTextView.setVisibility(View.GONE);
      dataStatusReceivingTextView.setVisibility(View.VISIBLE);
      //      }
   }

   @Subscribe
   public void onSensorDataReceivingStop(final SensorDataReceivingStop event) {
      System.out.println("onSensorDataReceivingStop");
      //      if (dataStatusTextView != null) {
      dataStatusIdleTextView.setVisibility(View.VISIBLE);
      dataStatusReceivingTextView.setVisibility(View.GONE);
      //      }
   }
}
