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
   
   // TODO classes for the other important value types
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
}
