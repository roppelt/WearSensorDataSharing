package com.biankaroppelt.masterthesis;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.biankaroppelt.masterthesis.data.SensorDataPoint;
import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.OnPS2DataSentToServerEvent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class SendPilotStudy2DataToWebserverTask extends AsyncTask<Object, Void, Boolean> {

   private static final String TAG = SendPilotStudy2DataToWebserverTask.class.getSimpleName();
   private int rotationDimension;
   private int target;
   private boolean taskSuccess;
   private URL url;
   private HttpURLConnection urlConnection;

   @Override
   protected Boolean doInBackground(Object... urls) {
      target = ((int) urls[2]);
      rotationDimension = ((int) urls[3]);
      taskSuccess = ((boolean) urls[4]);
      return sendDataCollection(((String) urls[0]), ((int) urls[1]), ((int) urls[2]),
            ((int) urls[3]), ((boolean) urls[4]), ((float) urls[5]), ((float) urls[6]),
            ((float) urls[7]), ((ArrayList<SensorDataPoint>) urls[8]));
   }

   @Override
   protected void onPostExecute(Boolean success) {
      BusProvider.postOnMainThread(
            new OnPS2DataSentToServerEvent(success, target, rotationDimension, taskSuccess));
   }

   private Boolean sendDataCollection(String myurl, int participantId, int targetId,
         int rotationDimensionId, boolean taskSuccess, float targetAngle, float maxAngle,
         float varianceInPercent, ArrayList<SensorDataPoint> data) {
      try {
         url = new URL(myurl);
      } catch (MalformedURLException e) {
         e.printStackTrace();
      }
      try {
         urlConnection = (HttpURLConnection) url.openConnection();
         urlConnection.setReadTimeout(15000);
         urlConnection.setConnectTimeout(50000);
         urlConnection.setRequestMethod("POST");
         urlConnection.setDoInput(true);
         urlConnection.setDoOutput(true);

         // Append parameters to URL
         Uri.Builder builder = new Uri.Builder();
         builder.appendQueryParameter("participantId", String.valueOf(participantId));
         builder.appendQueryParameter("targetId", String.valueOf(targetId));
         builder.appendQueryParameter("rotationDimensionId", String.valueOf(rotationDimensionId));
         builder.appendQueryParameter("taskSuccess", String.valueOf(taskSuccess));
         builder.appendQueryParameter("targetAngle", String.valueOf(targetAngle));
         builder.appendQueryParameter("maxAngle", String.valueOf(maxAngle));
         builder.appendQueryParameter("varianceInPercent", String.valueOf(varianceInPercent));
         builder.appendQueryParameter("dataLength", String.valueOf(data.size()));
         for (int i = 0; i < data.size(); i++) {
            SensorDataPoint sensorDataPoint = data.get(i);
            builder.appendQueryParameter("data" + i + "[]",
                  String.valueOf(sensorDataPoint.getAccuracy()));
            builder.appendQueryParameter("data" + i + "[]",
                  String.valueOf(sensorDataPoint.getTimestamp()));
            builder.appendQueryParameter("data" + i + "[]",
                  String.valueOf(sensorDataPoint.isAbsolute()));
            for (int j = 0; j < 9; j++) {
               if (j >= sensorDataPoint.getValues().length) {
                  builder.appendQueryParameter("data" + i + "[]", "null");
               } else {
                  builder.appendQueryParameter("data" + i + "[]",
                        String.valueOf(sensorDataPoint.getValues()[j]));
               }
            }
            builder.appendQueryParameter("data" + i + "[]", String.valueOf(
                  sensorDataPoint.getSensor()
                        .getId()));
            builder.appendQueryParameter("data" + i + "[]", sensorDataPoint.getSensor()
                  .getName());
         }
         String query = builder.build()
               .getEncodedQuery();

         // Open connection for sending data
         OutputStream os = urlConnection.getOutputStream();
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
         writer.write(query);
         writer.flush();
         writer.close();
         os.close();
         urlConnection.connect();
      } catch (IOException e) {
         e.printStackTrace();
         return false;
      }
      try {

         int response_code = urlConnection.getResponseCode();

         // Check if successful connection made
         if (response_code == HttpURLConnection.HTTP_OK) {
            // Read data sent from server
            InputStream input = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder result = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
               result.append(line);
            }
            input.close();
            Log.d(TAG, result.toString());
            return true;
         } else {
            Log.d(TAG,
                  urlConnection.getResponseCode() + " - " + urlConnection.getResponseMessage());
            return false;
         }
      } catch (IOException e) {
         e.printStackTrace();
         return false;
      } finally {
         urlConnection.disconnect();
      }
   }
}
