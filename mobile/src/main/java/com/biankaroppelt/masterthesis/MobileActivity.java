package com.biankaroppelt.masterthesis;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;

public class MobileActivity extends AppCompatActivity {

   private RemoteSensorManager remoteSensorManager;

   private static final String TAG = MobileActivity.class.getSimpleName();

   private Button buttonNewPilotStudy1B;
   private Button buttonNewPilotStudy1A;
   private Button buttonNewPilotStudy2;
   private Button buttonNewMainStudy;
   private Button startCollectingDataOrientationButton;
   private Button stopCollectingDataOrientationButton;
   private Button startollectingDataAccelerometerGyroscope;
   private Button stopollectingDataAccelerometerGyroscope;
   private TextView textViewStatusIdle;
   private TextView textViewStatusData;
   private CoordinatorLayout coordinatorLayout;
   private ProgressBar loadingIndicator;
   private MenuItem menuItemSendData;

   private boolean isCollectingData;
   private ArrayList<SensorDataPoint> mItems;

   private WebSocketClient mWebSocketClient;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_mobile);
      buttonNewPilotStudy1A = ((Button) findViewById(R.id.button_new_pilot_study_1a));
      buttonNewPilotStudy1B = ((Button) findViewById(R.id.button_new_pilot_study_1b));
      buttonNewPilotStudy2 = ((Button) findViewById(R.id.button_new_pilot_study_2));
      buttonNewMainStudy = ((Button) findViewById(R.id.button_new_main_study));
      startCollectingDataOrientationButton =
            ((Button) findViewById(R.id.button_start_collecting_data_orientation));
      stopCollectingDataOrientationButton =
            ((Button) findViewById(R.id.button_stop_collecting_data_orientation));
      startollectingDataAccelerometerGyroscope =
            ((Button) findViewById(R.id.button_start_collecting_data_acc_gyr));
      stopollectingDataAccelerometerGyroscope =
            ((Button) findViewById(R.id.button_stop_collecting_data_acc_gyr));
      textViewStatusIdle = ((TextView) findViewById(R.id.text_view_status_idle));
      textViewStatusData = ((TextView) findViewById(R.id.text_view_status_data));
      coordinatorLayout = ((CoordinatorLayout) findViewById(R.id.coordinator_layout));
      loadingIndicator = ((ProgressBar) findViewById(R.id.loading_indicator));
      loadingIndicator.setVisibility(View.GONE);
      remoteSensorManager = RemoteSensorManager.getInstance(this);
      isCollectingData = false;
      mItems = new ArrayList<>();
      setupToolbar();
      setupButtonListener();
      connectWebSocket();
   }

   private void setupToolbar() {
      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.menu_mobile, menu);
      return true;
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      menuItemSendData = menu.findItem(R.id.action_send_data);
      menuItemSendData.setVisible(false);
      return super.onPrepareOptionsMenu(menu);
   }

   private void hideMenuItems() {
      if (menuItemSendData != null) {
         menuItemSendData.setVisible(false);
      }
   }

   private void showMenuItems() {
      if (menuItemSendData != null) {
         menuItemSendData.setVisible(true);
      }
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case R.id.action_send_data:
            showSendDataDialog();
            return true;
      }

      return super.onOptionsItemSelected(item);
   }

   private void deleteData() {
      mItems = new ArrayList<>();
      hideMenuItems();
   }

   private void setupButtonListener() {
      buttonNewPilotStudy1A.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Intent intent = new Intent(MobileActivity.this, NewPilotStudy1AActivity.class);
            startActivity(intent);
         }
      });
      buttonNewPilotStudy1B.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Intent intent = new Intent(MobileActivity.this, NewPilotStudy1BActivity.class);
            startActivity(intent);
         }
      });
      buttonNewPilotStudy2.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Intent intent = new Intent(MobileActivity.this, NewPilotStudy2Activity.class);
            startActivity(intent);
         }
      });
      buttonNewMainStudy.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            Intent intent = new Intent(MobileActivity.this, NewMainStudyActivity.class);
            startActivity(intent);
         }
      });
      startCollectingDataOrientationButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            startCollectingDataOrientation();
         }
      });
      stopCollectingDataOrientationButton.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            stopCollectingDataOrientation();
         }
      });
      startollectingDataAccelerometerGyroscope.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            startCollectingDataAccelerometerGyroscope();
         }
      });
      stopollectingDataAccelerometerGyroscope.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View view) {
            stopCollectingDataAccelerometerGyroscope();
         }
      });
   }

   private void startCollectingDataAccelerometerGyroscope() {
      if (isCollectingData) {
         stopCollectingDataOrientation();
      }
      remoteSensorManager.startMeasurementAccelerometerGyroscope();
      setDataCollecting(true);
      startollectingDataAccelerometerGyroscope.setVisibility(View.GONE);
      stopollectingDataAccelerometerGyroscope.setVisibility(View.VISIBLE);
      startCollectingDataOrientationButton.setEnabled(false);
   }

   private void stopCollectingDataAccelerometerGyroscope() {
      showMenuItems();
      remoteSensorManager.stopMeasurementAccelerometerGyroscope();
      setDataCollecting(false);
      startollectingDataAccelerometerGyroscope.setVisibility(View.VISIBLE);
      stopollectingDataAccelerometerGyroscope.setVisibility(View.GONE);
      startCollectingDataOrientationButton.setEnabled(true);
   }


   private void setDataCollecting(boolean dataCollecting) {
      if (dataCollecting) {
         deleteData();
         loadingIndicator.setVisibility(View.VISIBLE);
         textViewStatusData.setVisibility(View.VISIBLE);
         textViewStatusIdle.setVisibility(View.GONE);
         isCollectingData = true;
      } else {
         textViewStatusIdle.setVisibility(View.VISIBLE);
         textViewStatusData.setVisibility(View.GONE);
         loadingIndicator.setVisibility(View.GONE);
         isCollectingData = false;
      }
   }

   private void startCollectingDataOrientation() {
      if (isCollectingData) {
         stopCollectingDataAccelerometerGyroscope();
      }
      remoteSensorManager.startMeasurementOrientation();
      setDataCollecting(true);
      startCollectingDataOrientationButton.setVisibility(View.GONE);
      stopCollectingDataOrientationButton.setVisibility(View.VISIBLE);
      startollectingDataAccelerometerGyroscope.setEnabled(false);
   }

   private void stopCollectingDataOrientation() {
      showMenuItems();
      remoteSensorManager.stopMeasurementOrientation();
      setDataCollecting(false);
      startCollectingDataOrientationButton.setVisibility(View.VISIBLE);
      stopCollectingDataOrientationButton.setVisibility(View.GONE);
      startollectingDataAccelerometerGyroscope.setEnabled(true);
   }

   @Override
   protected void onResume() {
      super.onResume();
      BusProvider.getInstance()
            .register(this);
//      if (mWebSocketClient == null || !mWebSocketClient.getReadyState()
//            .equals(WebSocket.READYSTATE.OPEN)) {
//         connectWebSocket();
//      }
   }

   @Override
   protected void onPause() {
      super.onPause();
      BusProvider.getInstance()
            .unregister(this);
      stopCollectingDataOrientation();
      stopCollectingDataAccelerometerGyroscope();
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
      string += sensorDataPoint.getSensor().getId() + ",";
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
      System.out.println(event.isSuccess());
      String snackbarString = event.isSuccess() ? "The dataset is stored in the database" :
            "unsuccessful (most of the time localtunnel didn't work)";
      Snackbar snackbar = Snackbar.make(coordinatorLayout, snackbarString, Snackbar.LENGTH_LONG);
      snackbar.show();
   }

   @Subscribe
   public void onDataPointAddedEvent(final DataPointAddedEvent event) {
   }

   @Subscribe
   public void onNoNodesAvailableEvent(final NoNodesAvailableEvent event) {
      Snackbar snackbar = Snackbar.make(coordinatorLayout, "No watch paired", Snackbar.LENGTH_LONG);
      snackbar.show();
      stopCollectingDataOrientation();
      stopCollectingDataAccelerometerGyroscope();
   }

   protected void showSendDataDialog() {
      long diffInMs = (mItems.get(mItems.size()-1).getTimestamp() - mItems.get(0).getTimestamp());
      if (diffInMs > 0) {
         System.out.println("Count sensor items: " + mItems.size() + " - Sample rate: " +
               (mItems.size() * 1000.0 / diffInMs));
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

         //         final int dataPartitioningSize = mItems.size() / 500;
         //         for(int i = 0; i < dataPartitioningSize; i++) {
         //            final ArrayList<SensorDataPoint> tempList =
         //                  new ArrayList<>(mItems.subList(i * 500, ((i + 1) * 500)));
         new SendDataToWebserverTask().execute(stringUrl, dataSetTitle, mItems);

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
                     startCollectingDataOrientation();
                  } else if (message.equals("stop_orientation")) {
                     stopCollectingDataOrientation();
                  } else if (message.equals("start_accelerometer_gyroscope")) {
                     startCollectingDataAccelerometerGyroscope();
                  } else if (message.equals("stop_accelerometer_gyroscope")) {
                     stopCollectingDataAccelerometerGyroscope();
                  } else if(message.startsWith("study_1b_tap_type")) {
                     String[] values = message.split(",");
                     int tapType = Integer.parseInt(values[1]);
                     System.out.println("TapType: " + tapType);
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
      System.gc();
   }
}
