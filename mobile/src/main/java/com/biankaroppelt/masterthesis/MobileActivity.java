package com.biankaroppelt.masterthesis;

import android.content.Context;
import android.content.DialogInterface;
import android.icu.text.DecimalFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.biankaroppelt.masterthesis.data.SensorDataPoint;
import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.DataPointAddedEvent;
import com.biankaroppelt.masterthesis.events.NewSensorEvent;
import com.biankaroppelt.masterthesis.events.NoNodesAvailableEvent;
import com.biankaroppelt.masterthesis.events.OnDataSentToServerEvent;
import com.biankaroppelt.masterthesis.events.SensorUpdatedEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MobileActivity extends AppCompatActivity {

   private RemoteSensorManager remoteSensorManager;

   private static final String TAG = MobileActivity.class.getSimpleName();

   private Button startCollectingDataButton;
   private Button stopCollectingDataButton;
//   private RecyclerView sensorDataRecyclerView;
   private LinearLayout sensorDataListHeader;
   private CoordinatorLayout coordinatorLayout;
   private TextView dataCountTextView;
   private ProgressBar loadingIndicator;
   private MenuItem menuItemDeleteData;
   private MenuItem menuItemSendData;

   private boolean isCollectingData;
   //   private SensorDataListAdapter adapter;
   private ArrayList<SensorDataPoint> mItems;
   private Date startTime;
   private Date endTime;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_mobile);
      startCollectingDataButton = ((Button) findViewById(R.id.button_start_collecting_data));
      stopCollectingDataButton = ((Button) findViewById(R.id.button_stop_collecting_data));
//      sensorDataRecyclerView = ((RecyclerView) findViewById(R.id.sensor_data_list));
      sensorDataListHeader = ((LinearLayout) findViewById(R.id.sensor_data_list_header));
      coordinatorLayout = ((CoordinatorLayout) findViewById(R.id.coordinator_layout));
      dataCountTextView = ((TextView) findViewById(R.id.data_count));
      loadingIndicator = ((ProgressBar) findViewById(R.id.loading_indicator));
      loadingIndicator.setVisibility(View.GONE);
      sensorDataListHeader.setVisibility(View.GONE);
      remoteSensorManager = RemoteSensorManager.getInstance(this);
      isCollectingData = false;
      mItems = new ArrayList<>();
      setupToolbar();
      setupButtonListener();
      setupRecyclerView();
   }

   private void setupToolbar() {
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.menu_mobile, menu);
      if (!isCollectingData) {
         hideMenuItems();
      }
      return true;
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      menuItemDeleteData = menu.findItem(R.id.action_delete_data);
      menuItemSendData = menu.findItem(R.id.action_send_data);
      return super.onPrepareOptionsMenu(menu);
   }

   private void hideMenuItems() {
      if (menuItemDeleteData != null && menuItemSendData != null) {
         menuItemDeleteData.setVisible(false);
         menuItemSendData.setVisible(false);
      }
   }

   private void showMenuItems() {
      if (menuItemDeleteData != null && menuItemSendData != null) {
         menuItemDeleteData.setVisible(true);
         menuItemSendData.setVisible(true);
      }
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.action_delete_data:
            deleteData();
            return true;
         case R.id.action_send_data:
            showSendDataDialog();
            return true;
      }

      return super.onOptionsItemSelected(item);
   }

   private void deleteData() {
      if (isCollectingData) {
         stopCollectingData();
      }
      mItems = new ArrayList<>();
      //      adapter.deleteData();
      sensorDataListHeader.setVisibility(View.GONE);
      hideMenuItems();
   }

   private void setupRecyclerView() {
//      sensorDataRecyclerView.setHasFixedSize(true);
//      LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//      sensorDataRecyclerView.setLayoutManager(layoutManager);
      //      adapter = new SensorDataListAdapter(this, new ArrayList<SensorDataPoint>());
      //      sensorDataRecyclerView.setAdapter(adapter);
   }

   private void setupButtonListener() {
      startCollectingDataButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            startCollectingData();
         }
      });
      stopCollectingDataButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            stopCollectingData();
         }
      });
   }

   private void startCollectingData() {
      startTime = new Date();
      remoteSensorManager.startMeasurement();
      startCollectingDataButton.setVisibility(View.GONE);
      stopCollectingDataButton.setVisibility(View.VISIBLE);
   }

   private void stopCollectingData() {
      loadingIndicator.setVisibility(View.GONE);
      endTime = new Date();
      remoteSensorManager.stopMeasurement();
      isCollectingData = false;
      startCollectingDataButton.setVisibility(View.VISIBLE);
      stopCollectingDataButton.setVisibility(View.GONE);
   }

   @Override
   protected void onResume() {
      super.onResume();
      BusProvider.getInstance()
            .register(this);
      //      List<Sensor> sensors = RemoteSensorManager.getInstance(this)
      //            .getSensors();
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
      //      System.out.println("onNewSensorEvent: " + event.getSensor());
   }

   @Subscribe
   public void onSensorUpdatedEvent(final SensorUpdatedEvent event) {
      if(loadingIndicator.getVisibility() == View.GONE) {
         loadingIndicator.setVisibility(View.VISIBLE);
      }
      //      System.out.println(
      //            "onSensorUpdatedEvent: " + event.getDataPointList().toString());
      //      adapter.addDataPoints(event.getDataPointList());
      mItems.addAll(event.getDataPointList());
//      sensorDataRecyclerView.scrollToPosition(0);
      if (!isCollectingData) {
         isCollectingData = true;
         sensorDataListHeader.setVisibility(View.VISIBLE);
         showMenuItems();
      }
   }

   @Subscribe
   public void onOnDataSentToServerEvent(final OnDataSentToServerEvent event) {
      loadingIndicator.setVisibility(View.GONE);
      System.out.println(event.getResultInfo());
      Snackbar snackbar =
            Snackbar.make(coordinatorLayout, event.getResultInfo(), Snackbar.LENGTH_LONG);
      snackbar.show();
   }

   @Subscribe
   public void onDataPointAddedEvent(final DataPointAddedEvent event) {
      //      System.out.println("onDataPointAddedEvent: " + event.getCount());
      dataCountTextView.setText(String.valueOf(event.getCount()));
   }

   @Subscribe
   public void onNoNodesAvailableEvent(final NoNodesAvailableEvent event) {
      Snackbar snackbar = Snackbar.make(coordinatorLayout, "No watch paired", Snackbar.LENGTH_LONG);
      snackbar.show();
      stopCollectingData();
   }

   protected void showSendDataDialog() {

      long diffInMs = endTime.getTime() - startTime.getTime();

      long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs);
      if (diffInSec > 0) {
         System.out.println("Count sensor items: " + mItems.size() + " - Sample rate: " + (mItems.size() * 1000.0 / diffInMs));
      }
      // get prompts.xml view
      LayoutInflater layoutInflater = LayoutInflater.from(this);
      View promptView = layoutInflater.inflate(R.layout.popup_title, null);
      AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
      alertDialogBuilder.setView(promptView);

      final EditText editText = (EditText) promptView.findViewById(R.id.input_data_name);
      // setup a dialog window
      alertDialogBuilder.setCancelable(false)
            .setPositiveButton(getString(R.string.popup_button_ok),
                  new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                        String title = editText.getText()
                              .toString();
                        if (TextUtils.isEmpty(title)) {
                           title = "Test";
                        }
                        sendData(title);
                     }
                  })
            .setNegativeButton(getString(R.string.popup_button_cancel),
                  new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                     }
                  });

      // create an alert dialog
      AlertDialog alert = alertDialogBuilder.create();
      alert.show();
   }

   private void sendData(String dataSetTitle) {
      loadingIndicator.setVisibility(View.VISIBLE);
      ConnectivityManager connMgr =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
      if (networkInfo != null && networkInfo.isConnected()) {
         // fetch data
         String stringUrl = "http://master.localtunnel.me/master/new_data_collection.php";
         new SendDataToWebserverTask().execute(stringUrl, dataSetTitle, mItems);
      } else {
         // display error

         Snackbar snackbar = Snackbar.make(coordinatorLayout, "No network connection available.",
               Snackbar.LENGTH_LONG);
         snackbar.show();
      }
   }
}
