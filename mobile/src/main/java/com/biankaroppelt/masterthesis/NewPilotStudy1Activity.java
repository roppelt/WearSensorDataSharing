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
import com.biankaroppelt.masterthesis.events.SensorUpdatedEvent;
import com.squareup.otto.Subscribe;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;

public class NewPilotStudy1Activity extends AppCompatActivity {

   private RemoteSensorManager remoteSensorManager;

   private static final String TAG = NewPilotStudy1Activity.class.getSimpleName();

   //   private Button startCollectingDataButton;
   //   private Button stopCollectingDataButton;
   private MaterialBetterSpinner spinnerFinger;
   private MaterialBetterSpinner spinnerDimension;
   private Button buttonDataSend;
   private Button buttonDataStart;
   private Button buttonDataStop;
   private TextInputEditText inputParticipant;
   private TextInputEditText inputHandSize;
   private CheckBox checkBoxHandedness;
   private CoordinatorLayout coordinatorLayout;
   private ProgressBar loadingIndicator;

   private ArrayList<SensorDataPoint> mItems;

   private WebSocketClient mWebSocketClient;
   private int selectedRotationDimensionId;
   private int selectedFingerId;

   String[] SPINNER_FINGER_LIST =
         { "Pinky", "Ring finger", "Middle finger", "Index finger", "Thumb" };
   String[] SPINNER_ROTATION_DIMENSION_LIST =
         { "Lift up", "Lift down", "Roll left", "Roll right", "Rotate left", "Rotate right" };

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_pilot_study_1);
      buttonDataSend = ((Button) findViewById(R.id.button_data_send));
      buttonDataStart = ((Button) findViewById(R.id.button_data_start));
      buttonDataStop = ((Button) findViewById(R.id.button_data_stop));
      spinnerFinger = ((MaterialBetterSpinner) findViewById(R.id.input_finger));
      spinnerDimension = ((MaterialBetterSpinner) findViewById(R.id.input_rotation_dimension));
      inputParticipant = ((TextInputEditText) findViewById(R.id.input_participant));
      inputHandSize = ((TextInputEditText) findViewById(R.id.input_hand_size));
      checkBoxHandedness = ((CheckBox) findViewById(R.id.input_handedness));
      coordinatorLayout = ((CoordinatorLayout) findViewById(R.id.coordinator_layout));
      loadingIndicator = ((ProgressBar) findViewById(R.id.loading_indicator));

      remoteSensorManager = RemoteSensorManager.getInstance(this);
      mItems = new ArrayList<>();
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
                  SPINNER_FINGER_LIST);
      spinnerFinger.setAdapter(arrayAdapterFinger);
      spinnerFinger.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            selectedFingerId = i;
         }
      });

      ArrayAdapter<String> arrayAdapterRotationDimension =
            new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                  SPINNER_ROTATION_DIMENSION_LIST);
      spinnerDimension.setAdapter(arrayAdapterRotationDimension);
      spinnerDimension.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            selectedRotationDimensionId = i;
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
      mItems = new ArrayList<>();
      buttonDataStop.setVisibility(View.VISIBLE);
      buttonDataStart.setVisibility(View.GONE);
      buttonDataSend.setVisibility(View.GONE);
      loadingIndicator.setVisibility(View.GONE);
      remoteSensorManager.startMeasurementOrientationPilotStudy1();
   }

   private void stopCollectingData() {
      buttonDataSend.setVisibility(View.VISIBLE);
      buttonDataStop.setVisibility(View.GONE);
      buttonDataStart.setVisibility(View.GONE);
      loadingIndicator.setVisibility(View.GONE);
      remoteSensorManager.stopMeasurementOrientationPilotStudy1();
   }

   private void sendCollectedData() {
      loadingIndicator.setVisibility(View.VISIBLE);
      buttonDataSend.setVisibility(View.GONE);
      buttonDataStart.setVisibility(View.GONE);
      buttonDataStop.setVisibility(View.GONE);

      sendData();
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
      mItems.addAll(event.getDataPointList());
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
   public void onOnDataSentToServerEvent(final OnDataSentToServerEvent event) {
      //      loadingIndicator.setVisibility(View.GONE);
      System.out.println(event.getResultInfo());
      Snackbar snackbar =
            Snackbar.make(coordinatorLayout, event.getResultInfo(), Snackbar.LENGTH_LONG);
      snackbar.show();
      buttonDataStart.setVisibility(View.VISIBLE);
      loadingIndicator.setVisibility(View.GONE);
      buttonDataSend.setVisibility(View.GONE);
      buttonDataStop.setVisibility(View.GONE);
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

   private void sendData() {
      long diffInMs = (mItems.get(mItems.size() - 1)
            .getTimestamp() - mItems.get(0)
            .getTimestamp());
      if (diffInMs > 0) {
         System.out.println("Count sensor items: " + mItems.size() + " - Sample rate: " +
               (mItems.size() * 1000.0 / diffInMs));
      }

      int participantId = Integer.parseInt(inputParticipant.getText()
            .toString());
      String handSizeString = inputHandSize.getText()
            .toString();
      Boolean rightHanded = checkBoxHandedness.isChecked();

      ConnectivityManager connMgr =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
      if (networkInfo != null && networkInfo.isConnected()) {
         // fetch data
         String stringUrl =
               "http://192.168.43.27:8080/master/new_data_collection_pilot_study_1_wrong.php";

         //         final int dataPartitioningSize = mItems.size() / 500;
         //         for(int i = 0; i < dataPartitioningSize; i++) {
         //            final ArrayList<SensorDataPoint> tempList =
         //                  new ArrayList<>(mItems.subList(i * 500, ((i + 1) * 500)));
         new SendPilotStudy1DataToWebserverTask().execute(stringUrl, participantId,
               selectedFingerId, selectedRotationDimensionId, handSizeString, rightHanded, mItems);

         //         }
         //         final ArrayList<SensorDataPoint> tempList = new ArrayList<>(mItems.subList
         // (dataPartitioningSize*500, mItems.size()));
         //         new SendDataToWebserverTask().execute(stringUrl, dataSetTitle, tempList);

      } else {
         // display error
         Snackbar snackbar = Snackbar.make(coordinatorLayout, "No network connection available.",
               Snackbar.LENGTH_LONG);
         snackbar.show();
      }
   }

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
            runOnUiThread(new Runnable() {
               @Override
               public void run() {
                  if (message.equals("start_orientation")) {
                     startCollectingData();
                  } else if (message.equals("stop_orientation")) {
                     stopCollectingData();
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
         }
      };
      mWebSocketClient.connect();
   }
}
