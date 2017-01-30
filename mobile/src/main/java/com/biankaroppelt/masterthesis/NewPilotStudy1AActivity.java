package com.biankaroppelt.masterthesis;

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
import com.biankaroppelt.masterthesis.events.OnPS1ADataSentToServerEvent;
import com.biankaroppelt.masterthesis.events.SensorUpdatedEvent;
import com.squareup.otto.Subscribe;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;

public class NewPilotStudy1AActivity extends AppCompatActivity {

   private RemoteSensorManager remoteSensorManager;

   private static final String TAG = NewPilotStudy1AActivity.class.getSimpleName();

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

   //   private ArrayList<SensorDataPoint> mItems;

   private WebSocketClient mWebSocketClient;
   private int selectedRotationDimensionId;
   private int selectedFingerId;

   private ArrayList<SensorDataPoint>[][] mItems;
   private boolean[][] mItemsSaved;

   String[] SPINNER_FINGER_LIST =
         { "Pinky", "Ring finger", "Middle finger", "Index finger", "Thumb" };
   String[] SPINNER_ROTATION_DIMENSION_LIST =
         { "Lift up", "Lift down", "Roll left", "Roll right", "Rotate left", "Rotate right" };

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_pilot_study_1_a);
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
      mItems = new ArrayList[SPINNER_FINGER_LIST.length][SPINNER_ROTATION_DIMENSION_LIST.length];
      mItemsSaved = new boolean[SPINNER_FINGER_LIST.length][SPINNER_ROTATION_DIMENSION_LIST.length];
      //      mItems = new ArrayList<>();
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
      //      mItems = new ArrayList<>();
      buttonDataStop.setVisibility(View.VISIBLE);
      buttonDataStart.setVisibility(View.GONE);
      //      buttonDataSend.setVisibility(View.GONE);
      loadingIndicator.setVisibility(View.GONE);
      remoteSensorManager.startMeasurementOrientationPilotStudy1A();
   }

   private void stopCollectingData() {
      //      buttonDataSend.setVisibility(View.VISIBLE);
      buttonDataStop.setVisibility(View.GONE);
      buttonDataStart.setVisibility(View.VISIBLE);
      loadingIndicator.setVisibility(View.GONE);
      remoteSensorManager.stopMeasurementOrientationPilotStudy1();
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
      if (mItems[selectedFingerId][selectedRotationDimensionId] == null) {
         mItems[selectedFingerId][selectedRotationDimensionId] = new ArrayList<>();
      }
      mItems[selectedFingerId][selectedRotationDimensionId].addAll(event.getDataPointList());
      System.out.println("new things");
      boolean enableSendButton = true;
      for (int finger = 0; finger < mItems.length; finger++) {
         for (int rotationDimension = 0; rotationDimension < mItems[finger].length;
              rotationDimension++) {
            if (mItems[finger][rotationDimension] != null &&
                  mItems[finger][rotationDimension].size() > 0) {
               System.out.println("new " + finger + " - " + rotationDimension + ": " +
                     mItems[finger][rotationDimension].size());
            } else {
               enableSendButton = false;
               System.out.println("ENABLED FALSE: " + finger + " - " + rotationDimension);
            }
         }
      }
      if (enableSendButton) {
         //         buttonDataSend.setEnabled(true);
      }
      //      ArrayList<SensorDataPoint> mItemsTemp;
      //      mItems.addAll(event.getDataPointList());
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
   public void onOnPS1ADataSentToServerEvent(final OnPS1ADataSentToServerEvent event) {
      boolean success = event.isSuccess();
      System.out.println(success);
      String snackbarString = "The dataset is stored in the database";
      if (success) {
         mItemsSaved[event.getFinger()][event.getRotationDimension()] = true;
      } else {
         sendData(event.getFinger(), event.getRotationDimension());
         snackbarString = "unsuccessful (most of the time localtunnel didn't work) - Trying again";
      }
      Snackbar snackbar = Snackbar.make(coordinatorLayout, snackbarString, Snackbar.LENGTH_LONG);
      snackbar.show();

      boolean resetButtons = true;
      for (int i = 0; i < mItemsSaved.length; i++) {
         for (int j = 0; j < mItemsSaved[i].length; j++) {
            if (!mItemsSaved[i][j]) {
               resetButtons = false;
            }
         }
      }
      if (resetButtons) {
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
      for (int finger = 0; finger < mItems.length; finger++) {
         for (int rotationDimension = 0; rotationDimension < mItems[finger].length;
              rotationDimension++) {
            System.out.println("send " + finger + " - " + rotationDimension + ": " +
                  mItems[finger][rotationDimension]);
            sendData(finger, rotationDimension);
         }
      }
   }

   private void sendData(int finger, int rotationDimension) {
      int participantId = Integer.parseInt(inputParticipant.getText()
            .toString());
      String handSizeString = inputHandSize.getText()
            .toString();
      boolean rightHanded = checkBoxHandedness.isChecked();
      String stringUrl =
            "http://192.168.43.27:8080/master/new_data_collection_pilot_study_1A.php";
      if (mItems[finger][rotationDimension] != null) {
         new SendPilotStudy1ADataToWebserverTask().execute(stringUrl, participantId, finger,
               rotationDimension, handSizeString, rightHanded, mItems[finger][rotationDimension]);
      } else {
         System.out.println("NULL: " + finger + " - " + rotationDimension);
      }
   }

   //   private void sendData() {
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
   //         Snackbar snackbar = Snackbar.make(coordinatorLayout, "No network connection
   // available.",
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
            runOnUiThread(new Runnable() {
               @Override
               public void run() {
                  if (message.equals("start_orientation")) {
                     startCollectingData();
                  } else if (message.equals("stop_orientation")) {
                     stopCollectingData();
                  } else if (message.startsWith("study_1a_finger")) {
                     String[] values = message.split(",");
                     int fingerIdFromProcessing = Integer.parseInt(values[1]);
                     System.out.println("FingerId: " + fingerIdFromProcessing);
                     spinnerFinger.setText(SPINNER_FINGER_LIST[fingerIdFromProcessing]);
                     selectedFingerId = fingerIdFromProcessing;
                     System.out.println("FING: selectedFingerId: " + selectedFingerId +
                           " - selectedRotationDimensionId: " + selectedRotationDimensionId);
                  } else if (message.startsWith("study_1a_rotation_dimension")) {
                     String[] values = message.split(",");
                     int rotationDimensionIdFromProcessing = Integer.parseInt(values[1]);
                     System.out.println(
                           "RotationDimensionId: " + rotationDimensionIdFromProcessing);
                     spinnerDimension.setText(
                           SPINNER_ROTATION_DIMENSION_LIST[rotationDimensionIdFromProcessing]);
                     selectedRotationDimensionId = rotationDimensionIdFromProcessing;
                     System.out.println("RT: selectedFingerId: " + selectedFingerId +
                           " - selectedRotationDimensionId: " + selectedRotationDimensionId);
                  } else if (message.startsWith("study_1a_back")) {
                     String[] values = message.split(",");
                     int backFingerIdFromProcessing = Integer.parseInt(values[1]);
                     int backRotationDimensionIdFromProcessing = Integer.parseInt(values[2]);
                     mItems[backFingerIdFromProcessing][backRotationDimensionIdFromProcessing] =
                           new ArrayList<>();
                     System.out.println("BACK: selectedFingerId: " + selectedFingerId +
                           " - selectedRotationDimensionId: " + selectedRotationDimensionId);

                     System.out.println("BACK: backfingerid: " + backFingerIdFromProcessing +
                           " - backrotationdimensionid: " + backRotationDimensionIdFromProcessing);
                  } else if (message.startsWith("study_1a_participant_id")) {
                     String[] values = message.split(",");
                     inputParticipant.setText(values[1]);
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
            Snackbar snackbar =
                  Snackbar.make(coordinatorLayout, "Websocket error - retry", Snackbar.LENGTH_LONG);
            snackbar.show();
            connectWebSocket();
         }
      };
      mWebSocketClient.connect();
   }
}
