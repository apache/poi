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
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.poi.hmef.Attachment;
import org.apache.poi.hmef.HMEFMessage;
import org.apache.poi.hpsf.Filetime;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * A Date attribute which applies to a {@link HMEFMessage}
 *  or one of its {@link Attachment}s.
 */
public final class TNEFDateAttribute extends TNEFAttribute {
   private final static POILogger logger = POILogFactory.getLogger(TNEFDateAttribute.class);
   private Date data;
   
   /**
    * Constructs a single new date attribute from the id, type,
    *  and the contents of the stream
    */
   protected TNEFDateAttribute(int id, int type, InputStream inp) throws IOException {
      super(id, type, inp);
      
      byte[] binData = getData();
      if(binData.length == 8) {
         // The value is a 64 bit Windows Filetime
         this.data = Filetime.filetimeToDate(
               LittleEndian.getLong(getData(), 0)
         );
      } else if(binData.length == 14) {
         // It's the 7 date fields. We think it's in UTC...
         Calendar c = LocaleUtil.getLocaleCalendar(LocaleUtil.TIMEZONE_UTC);
         c.set(Calendar.YEAR, LittleEndian.getUShort(binData, 0));
         c.set(Calendar.MONTH, LittleEndian.getUShort(binData, 2) - 1); // Java months are 0 based!
         c.set(Calendar.DAY_OF_MONTH, LittleEndian.getUShort(binData, 4));
         c.set(Calendar.HOUR_OF_DAY, LittleEndian.getUShort(binData, 6));
         c.set(Calendar.MINUTE, LittleEndian.getUShort(binData, 8));
         c.set(Calendar.SECOND, LittleEndian.getUShort(binData, 10));
         // The 7th field is day of week, which we don't require
         c.clear(Calendar.MILLISECOND); // Not set in the file
         this.data = c.getTime();
      } else {
         throw new IllegalArgumentException("Invalid date, found " + binData.length + " bytes");
      }
   }

   public Date getDate() {
      return this.data;
   }
   
   public String toString() {
       DateFormatSymbols dfs = DateFormatSymbols.getInstance(Locale.ROOT);
       DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", dfs);
       df.setTimeZone(LocaleUtil.TIMEZONE_UTC);       
      return "Attribute " + getProperty() + ", type=" + getType() +
             ", date=" + df.format(data); 
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
      
      logger.log(POILogger.WARN, "Warning, non date property found: ", attr);
      return null;
  }
}
