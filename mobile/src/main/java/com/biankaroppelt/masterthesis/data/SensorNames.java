package com.biankaroppelt.masterthesis.data;

import android.util.SparseArray;

public class SensorNames {
   public SparseArray<String> names;

   public SensorNames() {
      names = new SparseArray<String>();

      names.append(0, "Debug Sensor");
      names.append(android.hardware.Sensor.TYPE_ACCELEROMETER, "Accelerometer");
      names.append(android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE, "Ambient temperatur");
      names.append(android.hardware.Sensor.TYPE_GAME_ROTATION_VECTOR, "Game Rotation Vector");
      names.append(android.hardware.Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR,
            "Geomagnetic Rotation Vector");
      names.append(android.hardware.Sensor.TYPE_GRAVITY, "Gravity");
      names.append(android.hardware.Sensor.TYPE_GYROSCOPE, "Gyroscope");
      names.append(android.hardware.Sensor.TYPE_GYROSCOPE_UNCALIBRATED, "Gyroscope (Uncalibrated)");
      names.append(android.hardware.Sensor.TYPE_HEART_RATE, "Heart Rate");
      names.append(android.hardware.Sensor.TYPE_LIGHT, "Light");
      names.append(android.hardware.Sensor.TYPE_LINEAR_ACCELERATION, "Linear Acceleration");
      names.append(android.hardware.Sensor.TYPE_MAGNETIC_FIELD, "Magnetic Field");
      names.append(android.hardware.Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED,
            "Magnetic Field (Uncalibrated)");
      names.append(android.hardware.Sensor.TYPE_PRESSURE, "Pressure");
      names.append(android.hardware.Sensor.TYPE_PROXIMITY, "Proximity");
      names.append(android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY, "Relative Humidity");
      names.append(android.hardware.Sensor.TYPE_ROTATION_VECTOR, "Rotation Vector");
      names.append(android.hardware.Sensor.TYPE_SIGNIFICANT_MOTION, "Significant Motion");
      names.append(android.hardware.Sensor.TYPE_STEP_COUNTER, "Step Counter");
      names.append(android.hardware.Sensor.TYPE_STEP_DETECTOR, "Step Detector");
   }

   public String getName(int sensorId) {
      String name = names.get(sensorId);

      if (name == null) {
         name = "Unknown";
      }

      return name;
   }
}
