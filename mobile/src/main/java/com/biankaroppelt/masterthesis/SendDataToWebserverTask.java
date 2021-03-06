package com.biankaroppelt.masterthesis;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.biankaroppelt.masterthesis.data.SensorDataPoint;
import com.biankaroppelt.masterthesis.events.BusProvider;
import com.biankaroppelt.masterthesis.events.OnDataSentToServerEvent;

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

public class SendDataToWebserverTask extends AsyncTask<Object, Void, String> {

   private static final String TAG = SendDataToWebserverTask.class.getSimpleName();
   private HttpURLConnection urlConnection;
   private URL url;

   @Override
   protected String doInBackground(Object... urls) {
      return sendDataCollection(((String) urls[0]), ((String) urls[1]),
            ((ArrayList<SensorDataPoint>) urls[2]));
   }

   // onPostExecute displays the results of the AsyncTask.
   @Override
   protected void onPostExecute(String result) {
      BusProvider.postOnMainThread(new OnDataSentToServerEvent(result));
   }

   private String sendDataCollection(String myurl, String dataSetTitle,
         ArrayList<SensorDataPoint> data) {
      try {
         url = new URL(myurl);
      } catch (MalformedURLException e) {
         e.printStackTrace();
      }
      try {

         urlConnection = (HttpURLConnection) url.openConnection();
         urlConnection.setReadTimeout(15000);
         urlConnection.setConnectTimeout(10000);
         urlConnection.setRequestMethod("POST");

         // setDoInput and setDoOutput method depict handling of both send and receive
         urlConnection.setDoInput(true);
         urlConnection.setDoOutput(true);

         // Append parameters to URL
         Uri.Builder builder = new Uri.Builder();

         builder.appendQueryParameter("name", dataSetTitle);
         builder.appendQueryParameter("dataLength", String.valueOf(data.size()));
         for (int i = 0; i < data.size(); i++) {
            SensorDataPoint sensorDataPoint = data.get(i);
            builder.appendQueryParameter("data" + i + "[]",
                  String.valueOf(sensorDataPoint.getAccuracy()));
            builder.appendQueryParameter("data" + i + "[]",
                  String.valueOf(sensorDataPoint.getTimestamp()));
            for (int j = 0; j < 6; j++) {
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
         return "exception";
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

            // Pass data to onPostExecute method
            return (result.toString());
         } else {
            System.out.println(urlConnection.getResponseCode() + " - " + urlConnection.getResponseMessage());
            return ("unsuccessful (most of the time localtunnel didn't work)");
         }
      } catch (IOException e) {
         e.printStackTrace();
         return "exception";
      } finally {
         urlConnection.disconnect();
      }
   }
}
