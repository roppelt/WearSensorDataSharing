package com.biankaroppelt.masterthesis.data;

public class Log {
   private long timestamp;
   private int logType;

   public Log(long timestamp, int logType) {
      this.timestamp = timestamp;
      this.logType = logType;
   }

   public int getLogType() {
      return logType;
   }

   public long getTimestamp() {
      return timestamp;
   }
}
