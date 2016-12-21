//package com.biankaroppelt.masterthesis;
//
//import android.net.Uri;
//import android.os.AsyncTask;
//
//import com.biankaroppelt.masterthesis.data.SensorDataPoint;
//import com.biankaroppelt.masterthesis.events.BusProvider;
//import com.biankaroppelt.masterthesis.events.OnDataSentToServerEvent;
//
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.io.OutputStreamWriter;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.ArrayList;
//
//public class SendDataToServletTask implements Runnable {
//
//   private static final String TAG = SendDataToServletTask.class.getSimpleName();
//   private HttpURLConnection urlConnection;
//   private URL url;
//   private SensorDataPoint sensorDataPoint;
//
//   SendDataToServletTask(String url, SensorDataPoint sensorDataPoint) {
//      this.sensorDataPoint = sensorDataPoint;
//      try {
//         this.url = new URL(url);
//      } catch (MalformedURLException e) {
//         e.printStackTrace();
//      }
//   }
//
//   @Override
//   public void run() {
//      sendDataPoint();
//   }
//
////   @Override
////   protected String doInBackground(Object... urls) {
////      return sendDataPoint(((String) urls[0]), ((SensorDataPoint) urls[1]));
////   }
////
////   // onPostExecute displays the results of the AsyncTask.
////   @Override
////   protected void onPostExecute(String result) {
////      BusProvider.postOnMainThread(new OnDataSentToServerEvent(result));
////   }
//
//   private String sendDataPoint() {
//      try {
//
//         urlConnection = (HttpURLConnection) url.openConnection();
//         urlConnection.setReadTimeout(50000);
//         urlConnection.setConnectTimeout(50000);
//         urlConnection.setRequestMethod("POST");
//
//         // setDoInput and setDoOutput method depict handling of both send and receive
//         urlConnection.setDoInput(true);
//         urlConnection.setDoOutput(true);
//
//         // Append parameters to URL
//         Uri.Builder builder = new Uri.Builder();
//         builder.appendQueryParameter("accuracy",
//               String.valueOf(sensorDataPoint.getAccuracy()));
//         builder.appendQueryParameter("timestamp",
//               String.valueOf(sensorDataPoint.getTimestamp()));
//         String valuesArrayString = "";
//         for (int j = 0; j < 6; j++) {
//            if (j >= sensorDataPoint.getValues().length) {
//               valuesArrayString += "null";
////               builder.appendQueryParameter("values", "null");
//            } else {
//               valuesArrayString += String.valueOf(sensorDataPoint.getValues()[j]);
////               builder.appendQueryParameter("values",
////                     String.valueOf(sensorDataPoint.getValues()[j]));
//            }
//            if(j != 5) {
//               valuesArrayString += ",";
//            }
//         }
//         builder.appendQueryParameter("values", valuesArrayString);
//         builder.appendQueryParameter("sensorId", String.valueOf(
//               sensorDataPoint.getSensor()
//                     .getId()));
//         builder.appendQueryParameter("sensorName", sensorDataPoint.getSensor()
//               .getName());
//         String query = builder.build()
//               .getEncodedQuery();
//
//         // Open connection for sending data
//         OutputStream os = urlConnection.getOutputStream();
//         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//         writer.write(query);
//         writer.flush();
//         writer.close();
//         os.close();
//         urlConnection.connect();
//
////         int response_code = urlConnection.getResponseCode();
////         System.out.println("Response Code: " + response_code);
//      } catch (IOException e) {
//         e.printStackTrace();
//         return "exception top";
//      }
//      try {
//
//         int response_code = urlConnection.getResponseCode();
//         // Check if successful connection made
//         if (response_code == HttpURLConnection.HTTP_OK) {
//
//            // Read data sent from server
//            InputStream input = urlConnection.getInputStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
//            StringBuilder result = new StringBuilder();
//            String line;
//
//            while ((line = reader.readLine()) != null) {
//               result.append(line);
//            }
//
//            // Pass data to onPostExecute method
//
//            BusProvider.postOnMainThread(new OnDataSentToServerEvent(result.toString()));
//            return (result.toString());
//         } else {
//            System.out.println(urlConnection.getResponseCode() + " - " + urlConnection.getResponseMessage());
//            return ("unsuccessful (most of the time localtunnel didn't work)");
//         }
//      } catch (IOException e) {
//         e.printStackTrace();
//         return "exception bottom";
//      } finally {
//         urlConnection.disconnect();
//      }
//   }
//}
