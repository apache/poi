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

package org.apache.poi.hmef.attribute;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.poi.hmef.Attachment;
import org.apache.poi.hmef.HMEFMessage;
import org.apache.poi.hpsf.Util;
import org.apache.poi.util.LittleEndian;

/**
 * A Date attribute which applies to a {@link HMEFMessage}
 *  or one of its {@link Attachment}s.
 */
public final class TNEFDateAttribute extends TNEFAttribute {
   private Date data;
   
   /**
    * Constructs a single new date attribute from the id, type,
    *  and the contents of the stream
    */
   protected TNEFDateAttribute(int id, int type, InputStream inp) throws IOException {
      super(id, type, inp);
      
      byte[] data = getData();
      if(data.length == 8) {
         // The value is a 64 bit Windows Filetime
         this.data = Util.filetimeToDate(
               LittleEndian.getLong(getData(), 0)
         );
      } else if(data.length == 14) {
         // It's the 7 date fields. We think it's in UTC...
         Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
         c.set(Calendar.YEAR, LittleEndian.getUShort(data, 0));
         c.set(Calendar.MONTH, LittleEndian.getUShort(data, 2) - 1); // Java months are 0 based!
         c.set(Calendar.DAY_OF_MONTH, LittleEndian.getUShort(data, 4));
         c.set(Calendar.HOUR_OF_DAY, LittleEndian.getUShort(data, 6));
         c.set(Calendar.MINUTE, LittleEndian.getUShort(data, 8));
         c.set(Calendar.SECOND, LittleEndian.getUShort(data, 10));
         // The 7th field is day of week, which we don't require
         c.set(Calendar.MILLISECOND, 0); // Not set in the file
         this.data = c.getTime();
      } else {
         throw new IllegalArgumentException("Invalid date, found " + data.length + " bytes");
      }
   }

   public Date getDate() {
      return this.data;
   }
   
   public String toString() {
      return "Attribute " + getProperty().toString() + ", type=" + getType() + 
             ", date=" + data.toString(); 
   }
   
   /**
    * Returns the Date of a Attribute, converting as appropriate
    */
   public static Date getAsDate(TNEFAttribute attr) {
      if(attr == null) {
         return null;
      }
      if(attr instanceof TNEFDateAttribute) {
         return ((TNEFDateAttribute)attr).getDate();
      }
      
      System.err.println("Warning, non date property found: " + attr.toString());
      return null;
  }
}
