/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hsmf.datatypes;

import java.util.Calendar;
import java.util.TimeZone;

import org.apache.poi.util.LittleEndian;

/**
 * An instance of a {@link MAPIProperty} inside a {@link PropertiesChunk}.
 * Where the {@link Types} type is a fixed length one, this will contain the
 *  actual value.
 * Where the {@link Types} type is a variable length one, this will contain
 *  the length of the property, and the value will be in the associated {@link Chunk}.
 */
public class PropertyValue {
   private MAPIProperty property;
   private long flags;
   protected byte[] data;
   
   public PropertyValue(MAPIProperty property, long flags, byte[] data) {
      this.property = property;
      this.flags = flags;
      this.data = data;
   }
   
   public MAPIProperty getProperty() {
      return property;
   }

   /**
    * Get the raw value flags.
    * TODO Also provide getters for the flag meanings
    */
   public long getFlags() {
      return flags;
   }

   public Object getValue() {
      return data;
   }
   public void setRawValue(byte[] value) {
      this.data = value;
   }
   
   public String toString() {
      Object v = getValue();
      if (v == null)
          return "(No value available)";
      
      if (v instanceof byte[]) {
          return ByteChunk.toDebugFriendlyString((byte[])v);
      } else {
          // Just use the normal toString on the value
          return v.toString();
      }
   }
   
   // TODO classes for the other important value types
   
   public static class ShortPropertyValue extends PropertyValue {
       public ShortPropertyValue(MAPIProperty property, long flags, byte[] data) {
          super(property, flags, data);
       }
       
       public Short getValue() {
          return LittleEndian.getShort(data);
       }
       public void setValue(short value) {
          if (data.length != 2) {
             data = new byte[2];
          }
          LittleEndian.putShort(data, 0, value);
       }
    }
    
   public static class LongPropertyValue extends PropertyValue {
       public LongPropertyValue(MAPIProperty property, long flags, byte[] data) {
          super(property, flags, data);
       }
       
       public Integer getValue() {
          return LittleEndian.getInt(data);
       }
       public void setValue(int value) {
          if (data.length != 4) {
             data = new byte[4];
          }
          LittleEndian.putInt(data, 0, value);
       }
    }
    
   public static class LongLongPropertyValue extends PropertyValue {
      public LongLongPropertyValue(MAPIProperty property, long flags, byte[] data) {
         super(property, flags, data);
      }
      
      public Long getValue() {
         return LittleEndian.getLong(data);
      }
      public void setValue(long value) {
         if (data.length != 8) {
            data = new byte[8];
         }
         LittleEndian.putLong(data, 0, value);
      }
   }
   
   /**
    * 64-bit integer specifying the number of 100ns periods since Jan 1, 1601
    */
   public static class TimePropertyValue extends PropertyValue {
      private static final long OFFSET = 1000L * 60L * 60L * 24L * (365L * 369L + 89L);
      public TimePropertyValue(MAPIProperty property, long flags, byte[] data) {
         super(property, flags, data);
      }
      
      public Calendar getValue() {
         long time = LittleEndian.getLong(data);
         time = (time / 10 / 1000) - OFFSET;

         Calendar timeC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
         timeC.setTimeInMillis(time);

         return timeC;
      }
      public void setValue(Calendar value) {
         if (data.length != 8) {
            data = new byte[8];
         }
         long time = value.getTimeInMillis();
         time = (time + OFFSET) *10*1000;
         LittleEndian.putLong(data, 0, time);
      }
   }
}
