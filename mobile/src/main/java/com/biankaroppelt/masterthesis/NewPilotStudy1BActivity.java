package com.biankaroppelt.masterthesis;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;

import com.biankaroppelt.masterthesis.data.SensorDataPoint;
import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.DataPointAddedEvent;
import com.biankaroppelt.masterthesis.events.NewSensorEvent;
import com.biankaroppelt.masterthesis.events.NoNodesAvailableEvent;
import com.biankaroppelt.masterthesis.events.OnDataSentToServerEvent;
import com.biankaroppelt.masterthesis.events.OnPS1BDataSentToServerEvent;
import com.biankaroppelt.masterthesis.events.SensorUpdatedEvent;
import com.squareup.otto.Subscribe;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;

public class NewPilotStudy1BActivity extends AppCompatActivity {

   private RemoteSensorManager remoteSensorManager;

   private static final String TAG = NewPilotStudy1BActivity.class.getSimpleName();

   //   private Button startCollectingDataButton;
   //   private Button stopCollectingDataButton;
   private MaterialBetterSpinner spinnerTapType;
   private Button buttonDataSend;
   private Button buttonDataStart;
   private Button buttonDataStop;
   private TextInputEditText inputParticipant;
   private CheckBox checkBoxHandedness;
   private CoordinatorLayout coordinatorLayout;
   private ProgressBar loadingIndicator;

   private WebSocketClient mWebSocketClient;
   private int selectedTapTypeId;
   private ArrayList<SensorDataPoint>[] mItems;
   private boolean[] mItemsSaved;

   String[] SPINNER_TAP_TYPE_LIST = { "Normal Taps", "Force Taps", "Pound out Taps" };

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_pilot_study_1_b);
      buttonDataSend = ((Button) findViewById(R.id.button_data_send));
      buttonDataStart = ((Button) findViewById(R.id.button_data_start));
      buttonDataStop = ((Button) findViewById(R.id.button_data_stop));
      spinnerTapType = ((MaterialBetterSpinner) findViewById(R.id.input_tap_type));
      inputParticipant = ((TextInputEditText) findViewById(R.id.input_participant));
      checkBoxHandedness = ((CheckBox) findViewById(R.id.input_handedness));
      coordinatorLayout = ((CoordinatorLayout) findViewById(R.id.coordinator_layout));
      loadingIndicator = ((ProgressBar) findViewById(R.id.loading_indicator));

      remoteSensorManager = RemoteSensorManager.getInstance(this);
      mItems = new ArrayList[SPINNER_TAP_TYPE_LIST.length];
      mItemsSaved = new boolean[SPINNER_TAP_TYPE_LIST.length];
      setupToolbar();
      setupListener();
   }

   private void setupToolbar() {
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      //      getMenuInflater().inflate(R.menu.menu_mobile, menu);
      return false;
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      return super.onPrepareOptionsMenu(menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      return super.onOptionsItemSelected(item);
   }

   private void setupListener() {
      ArrayAdapter<String> arrayAdapterFinger =
            new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                  SPINNER_TAP_TYPE_LIST);
      spinnerTapType.setAdapter(arrayAdapterFinger);
      spinnerTapType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            selectedTapTypeId = i;
         }
      });

      buttonDataSend.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            sendCollectedData();
         }
      });
      buttonDataStart.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            startCollectingData();
         }
      });
      buttonDataStop.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            stopCollectingData();
         }
      });
   }

   private void startCollectingData() {
//      mItems = new ArrayList<>();
      buttonDataStop.setVisibility(View.VISIBLE);
      buttonDataStart.setVisibility(View.GONE);
//      buttonDataSend.setVisibility(View.GONE);
      loadingIndicator.setVisibility(View.GONE);
      remoteSensorManager.startMeasurementAccelerometerGyroscope();
   }

   private void stopCollectingData() {
//      buttonDataSend.setVisibility(View.VISIBLE);
      buttonDataStop.setVisibility(View.GONE);
      buttonDataStart.setVisibility(View.VISIBLE);
      loadingIndicator.setVisibility(View.GONE);
      remoteSensorManager.stopMeasurementAccelerometerGyroscope();
   }

   private void sendCollectedData() {
      loadingIndicator.setVisibility(View.VISIBLE);
//      buttonDataSend.setVisibility(View.GONE);
      buttonDataStart.setVisibility(View.GONE);
      buttonDataStop.setVisibility(View.GONE);

      sendAllData();
   }

   @Override
   protected void onResume() {
      super.onResume();
      BusProvider.getInstance()
            .register(this);
      if (mWebSocketClient == null || !mWebSocketClient.getReadyState()
            .equals(WebSocket.READYSTATE.OPEN)) {
         connectWebSocket();
      }
   }

   @Override
   protected void onPause() {
      super.onPause();
      BusProvider.getInstance()
            .unregister(this);
      stopCollectingData();
   }

   @Subscribe
   public void onNewSensorEvent(final NewSensorEvent event) {
   }

   @Subscribe
   public void onSensorUpdatedEvent(final SensorUpdatedEvent event) {
      System.out.println("onSensorUpdatedEvent");
      if (mItems[selectedTapTypeId] == null) {
         mItems[selectedTapTypeId] = new ArrayList<>();
      }
      mItems[selectedTapTypeId].addAll(event.getDataPointList());
      boolean enableSendButton = true;
      for (int tapType = 0; tapType < mItems.length; tapType++) {
            if (mItems[tapType] != null &&
                  mItems[tapType].size() > 0) {
               System.out.println("new " + tapType + ": " +
                     mItems[tapType].size());
            } else {
               enableSendButton = false;
            }
      }
      if (enableSendButton) {
//         buttonDataSend.setEnabled(true);
      }
      if (mWebSocketClient != null && mWebSocketClient.getReadyState()
            .equals(WebSocket.READYSTATE.OPEN)) {
         mWebSocketClient.send(buildSendString(event.getDataPointList()));
      } else {
         connectWebSocket();
      }
   }

   private String buildSendString(ArrayList<SensorDataPoint> dataPointList) {
      String string = "";
      //      for (int i = 0; i < dataPointList.size(); i++) {
      // Building the string only of the last dataPoint
      SensorDataPoint sensorDataPoint = dataPointList.get(dataPointList.size() - 1);
      string += sensorDataPoint.getSensor()
            .getId() + ",";
      string += sensorDataPoint.getTimestamp() + ",";
      int valuesLength = Math.min(3, sensorDataPoint.getValues().length);
      for (int j = 0; j < valuesLength; j++) {
         string += sensorDataPoint.getValues()[j];
         if (j + 1 != valuesLength) {
            string += ",";
         }
      }
      //         if (i + 1 != dataPointList.size()) {
      //            string += ";";
      //         }
      //      }
      return string;
   }

   @Subscribe
   public void onOnDataSentToServerEvent(final OnPS1BDataSentToServerEvent event) {
      boolean success = event.isSuccess();
      System.out.println(success);
      String snackbarString = "The dataset is stored in the database";
      if(success) {
         mItemsSaved[event.getTapType()] = true;
      } else {
         sendData(event.getTapType());
         snackbarString = "unsuccessful (most of the time localtunnel didn't work) - try again";
      }
      Snackbar snackbar = Snackbar.make(coordinatorLayout, snackbarString, Snackbar.LENGTH_LONG);
      snackbar.show();
      boolean resetButtons = true;
      for(int i = 0; i < mItemsSaved.length; i++) {
         if(!mItemsSaved[i]) {
            resetButtons = false;
         }
      }
      if(resetButtons) {
         buttonDataStart.setVisibility(View.VISIBLE);
         loadingIndicator.setVisibility(View.GONE);
         buttonDataStop.setVisibility(View.GONE);
      }
   }

   @Subscribe
   public void onDataPointAddedEvent(final DataPointAddedEvent event) {
   }

   @Subscribe
   public void onNoNodesAvailableEvent(final NoNodesAvailableEvent event) {
      Snackbar snackbar = Snackbar.make(coordinatorLayout, "No watch paired", Snackbar.LENGTH_LONG);
      snackbar.show();
      stopCollectingData();
   }

   private void sendAllData() {
      for(int tapType = 0; tapType < SPINNER_TAP_TYPE_LIST.length; tapType++) {
         sendData(tapType);
      }
   }

   private void sendData(int tapType) {
      int participantId = Integer.parseInt(inputParticipant.getText()
            .toString());
      boolean rightHanded = checkBoxHandedness.isChecked();
      String stringUrl =
            "http://192.168.43.27:8080/master/new_data_collection_pilot_study_1B.php";
      System.out.println("taptype items: " + mItems[tapType].size());
//      new SendPilotStudy1BDataToWebserverTask().execute(stringUrl, participantId,
//            tapType, rightHanded, mItems[tapType]);


            final int dataPartitioningSize = mItems[tapType].size() / 500;
            for(int i = 0; i < dataPartitioningSize; i++) {
               final ArrayList<SensorDataPoint> tempList =
                     new ArrayList<>(mItems[tapType].subList(i * 500, ((i + 1) * 500)));

                  new SendPilotStudy1BDataToWebserverTask().execute(stringUrl, participantId,
                        tapType, rightHanded, tempList);

            }
            final ArrayList<SensorDataPoint> tempList = new ArrayList<>(mItems[tapType].subList
    (dataPartitioningSize*500, mItems[tapType].size()));
            new SendPilotStudy1BDataToWebserverTask().execute(stringUrl, participantId,
                  tapType, rightHanded, tempList);
//            new SendDataToWebserverTask().execute(stringUrl, dataSetTitle, tempList);

   }

//   private void sendData() {
//      if (mItems1.isEmpty() || mItems2.isEmpty() || mItems3.isEmpty()) {
//         return;
//      }
//      long diffInMs = (mItems.get(mItems.size() - 1)
//            .getTimestamp() - mItems.get(0)
//            .getTimestamp());
//      if (diffInMs > 0) {
//         System.out.println("Count sensor items: " + mItems.size() + " - Sample rate: " +
//               (mItems.size() * 1000.0 / diffInMs));
//      }

//      ConnectivityManager connMgr =
//            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//      NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//      if (networkInfo != null && networkInfo.isConnected()) {
         // fetch data

         //         final int dataPartitioningSize = mItems.size() / 500;
         //         for(int i = 0; i < dataPartitioningSize; i++) {
         //            final ArrayList<SensorDataPoint> tempList =
         //                  new ArrayList<>(mItems.subList(i * 500, ((i + 1) * 500)));


         //         }
         //         final ArrayList<SensorDataPoint> tempList = new ArrayList<>(mItems.subList
         // (dataPartitioningSize*500, mItems.size()));
         //         new SendDataToWebserverTask().execute(stringUrl, dataSetTitle, tempList);

//      } else {
//         // display error
//         Snackbar snackbar = Snackbar.make(coordinatorLayout, "No network connection available.",
//               Snackbar.LENGTH_LONG);
//         snackbar.show();
//      }
//   }

   private void connectWebSocket() {

      URI uri;
      uri = URI.create("ws://192.168.43.27:1234/");

      mWebSocketClient = new WebSocketClient(uri) {
         @Override
         public void onOpen(ServerHandshake serverHandshake) {
            System.out.println("websocket open");
         }

         @Override
         public void onMessage(String s) {
            final String message = s.trim();
            System.out.println("Message: " + message);
            runOnUiThread(new Runnable() {
               @Override
               public void run() {
                  if (message.equals("start_accelerometer_gyroscope")) {
                     startCollectingData();
                  } else if (message.equals("stop_accelerometer_gyroscope")) {
                     stopCollectingData();
                  } else if (message.startsWith("study_1b_tap_type")) {
                     String[] values = message.split(",");
                     int tapTypeFromProcessing = Integer.parseInt(values[1]);
                     System.out.println("TapType: " + tapTypeFromProcessing);
                     spinnerTapType.setText(SPINNER_TAP_TYPE_LIST[tapTypeFromProcessing]);
                     selectedTapTypeId = tapTypeFromProcessing;
                  }
               }
            });
         }

         @Override
         public void onClose(int i, String s, boolean b) {
            Log.i("Websocket", "Closed " + s);
         }

         @Override
         public void onError(Exception e) {
            Log.i("Websocket", "Error " + e.getMessage());
            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Websocket error - retry", Snackbar.LENGTH_LONG);
            snackbar.show();
            connectWebSocket();
         }
      };
      mWebSocketClient.connect();
   }
}
