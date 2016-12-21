//package com.biankaroppelt.masterthesis;
//
//import android.net.Uri;
//import android.os.AsyncTask;
//
//import com.biankaroppelt.masterthesis.data.SensorDataPoint;
//import com.biankaroppelt.masterthesis.events.BusProvider;
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
//import java.net.URI;
//import java.net.URL;
//import java.util.ArrayList;
//
//public class SendDataToServletTask implements Runnable {
//
//   private static final String TAG = SendDataToServletTask.class.getSimpleName();
//   private HttpURLConnection urlConnection;
//   private URL url;
//   private ArrayList<SensorDataPoint> sensorDataPoints;
//
//   SendDataToServletTask(String url, ArrayList<SensorDataPoint> sensorDataPoints) {
//      this.sensorDataPoints = sensorDataPoints;
//      try {
//         this.url = new URL(url);
//      } catch (MalformedURLException e) {
//         e.printStackTrace();
//      }
//   }
//
//
//   SendDataToServletTask() {
//   }
//
//   public void setData(String url, ArrayList<SensorDataPoint> sensorDataPoints) {
//      this.sensorDataPoints = sensorDataPoints;
//      try {
//         this.url = URI.create(url).toURL();
//
//      } catch (MalformedURLException e) {
//         e.printStackTrace();
//      }
//   }
//
//   @Override
//   public void run() {
//      sendDataPoints();
//   }
//
//   //   @Override
////   protected String doInBackground(Object... urls) {
////      return sendDataPoints();
////   }
////
////   // onPostExecute displays the results of the AsyncTask.
////   @Override
////   protected void onPostExecute(String result) {
////      System.out.println("RESULT: " + result);
//////      BusProvider.postOnMainThread(new OnDataSentToServerEvent(result));
////   }
//
//
//
//   private String sendDataPoints() {
//      System.out.println(url);
//      try {
//
//         System.out.println("SensorDataPoints Length: " + sensorDataPoints.size());
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
////         builder.appendQueryParameter("accuracy",
////               String.valueOf(sensorDataPoint.getAccuracy()));
////         builder.appendQueryParameter("timestamp",
////               String.valueOf(sensorDataPoint.getTimestamp()));
////         String valuesArrayString = "";
////         for (int j = 0; j < 6; j++) {
////            if (j >= sensorDataPoint.getValues().length) {
////               valuesArrayString += "null";
//////               builder.appendQueryParameter("values", "null");
////            } else {
////               valuesArrayString += String.valueOf(sensorDataPoint.getValues()[j]);
//////               builder.appendQueryParameter("values",
//////                     String.valueOf(sensorDataPoint.getValues()[j]));
////            }
////            if(j != 5) {
////               valuesArrayString += ",";
////            }
////         }
////         builder.appendQueryParameter("values", valuesArrayString);
////         builder.appendQueryParameter("sensorId", "15");
////         builder.appendQueryParameter("sensorName", sensorDataPoint.getSensor()
////               .getName());
////         String query = builder.build()
////               .getEncodedQuery();
//
//
//         builder.appendQueryParameter("dataLength", String.valueOf(sensorDataPoints.size()));
//         SensorDataPoint sensorDataPoint;
//         for (int i = 0; i < sensorDataPoints.size(); i++) {
//            sensorDataPoint = sensorDataPoints.get(i);
////            builder.appendQueryParameter("data" + i + "[]",
////                  String.valueOf(sensorDataPoint.getAccuracy()));
//            builder.appendQueryParameter("data" + i + "[]",
//                  String.valueOf(sensorDataPoint.getTimestamp()));
//            for (int j = 0; j < 6; j++) {
//               if (j >= sensorDataPoint.getValues().length) {
//                  builder.appendQueryParameter("data" + i + "[]", "null");
//               } else {
//                  builder.appendQueryParameter("data" + i + "[]",
//                        String.valueOf(sensorDataPoint.getValues()[j]));
//               }
//            }
////            builder.appendQueryParameter("data" + i + "[]", String.valueOf(
////                  sensorDataPoint.getSensor()
////                        .getId()));
//            builder.appendQueryParameter("data" + i + "[]", sensorDataPoint.getSensor()
//                  .getName());
//         }
//         String query = builder.build()
//               .getEncodedQuery();
//
//         System.out.println("query: " + query);
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
//      } catch (IOException e) {
//         e.printStackTrace();
//         return "exception top";
//      }
//      try {
//
//         int response_code = urlConnection.getResponseCode();
//         System.out.println("Response Code: " + response_code);
//         // Check if successful connection made
//         if (response_code == HttpURLConnection.HTTP_OK) {
//
////          Read data sent from server
//            InputStream input = urlConnection.getInputStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
//            StringBuilder result = new StringBuilder();
//            String line;
//
//            while ((line = reader.readLine()) != null) {
//               result.append(line);
//            }
//
//            input.close();
//            // Pass data to onPostExecute method
//            System.out.println("RESULT:");
//            System.out.println(result.toString());
//            return (result.toString());
////            return "success";
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
