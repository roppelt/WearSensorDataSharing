package com.biankaroppelt.masterthesis;

import android.net.Uri;
import android.os.AsyncTask;

import com.biankaroppelt.masterthesis.data.Log;
import com.biankaroppelt.masterthesis.data.SensorDataPoint;
import com.biankaroppelt.masterthesis.events.BusProvider;

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

public class SendMainStudyDataToWebserverTask extends AsyncTask<Object, Void, Boolean> {

   private static final String TAG = SendMainStudyDataToWebserverTask.class.getSimpleName();
   private HttpURLConnection urlConnection;
   private URL url;

   @Override
   protected Boolean doInBackground(Object... urls) {
//      target = ((int) urls[2]);
//      rotationDimension = ((int) urls[3]);
//      taskSuccess = ((boolean) urls[6]);
      return sendDataCollection(((String) urls[0]), ((int) urls[1]), ((int) urls[2]),
            ((int) urls[3]), ((String) urls[4]), ((boolean) urls[5]), ((boolean) urls[6]), ((float) urls[7]),
            ((float) urls[8]), ((float) urls[9]), ((ArrayList<SensorDataPoint>) urls[10]),
            ((ArrayList<Log>) urls[11]));
   }

   private Boolean sendDataCollection(String myurl, int participantId, int targetId,
         int rotationDimensionId, String handSize, boolean rightHanded, boolean taskSuccess,
         float targetAngle, float maxAngle, float varianceInPercent,
         ArrayList<SensorDataPoint> data, ArrayList<Log> logs) {
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

         // setDoInput and setDoOutput method depict handling of both send and receive
         urlConnection.setDoInput(true);
         urlConnection.setDoOutput(true);

         // Append parameters to URL
         Uri.Builder builder = new Uri.Builder();

         builder.appendQueryParameter("participantId", String.valueOf(participantId));
         builder.appendQueryParameter("targetId", String.valueOf(targetId));
         builder.appendQueryParameter("rotationDimensionId", String.valueOf(rotationDimensionId));
         builder.appendQueryParameter("handSize", handSize);
         builder.appendQueryParameter("rightHanded", String.valueOf(rightHanded));
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
         builder.appendQueryParameter("logLength", String.valueOf(logs.size()));
         for (int i = 0; i < logs.size(); i++) {
            Log log = logs.get(i);
            builder.appendQueryParameter("logs" + i + "[]", String.valueOf(log.getTimestamp()));
            builder.appendQueryParameter("logs" + i + "[]", String.valueOf(log.getLogType()));
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

         //         urlConnection.setDoOutput(true);
         //         urlConnection.setChunkedStreamingMode(0);
         //
         ////         OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
         ////         writeStream(out);
         ////
         ////         InputStream in = new BufferedInputStream(urlConnection.getInputStream());
         ////         readStream(in);
         //
         //
         //         List<Pair<String, String>> params = new ArrayList<>();
         //         params.add(new Pair<String, String>("name", "test"));
         //
         //         InputStream is = new BufferedInputStream(urlConnection.getInputStream());
         //         OutputStream os = new BufferedOutputStream(urlConnection.getOutputStream());
         //         BufferedWriter writer = new BufferedWriter(
         //               new OutputStreamWriter(os, "UTF-8"));
         //         writer.write(getQuery(params));
         //         writer.flush();
         //         writer.close();
         //         os.close();
         //
         //         urlConnection.connect();
         //         return readIt(is, 500);
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
            // Pass data to onPostExecute method
            System.out.println(result);
            return true;
         } else {
            System.out.println(
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
