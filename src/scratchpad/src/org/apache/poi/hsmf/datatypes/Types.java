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

/**
 * The types list and details are available from
 *  http://msdn.microsoft.com/en-us/library/microsoft.exchange.data.contenttypes.tnef.tnefpropertytype%28v=EXCHG.140%29.aspx
 */
public final class Types {
   /** Unspecified */
   public static final int UNSPECIFIED = 0x0000;

   /** Null - NULL property value */
   public static final int NULL = 0x0001;
   /** I2 - signed 16-bit value */
   public static final int SHORT = 0x0002;
   /** Long - signed 32-bit value */
   public static final int LONG = 0x0003;
   /** R4 - 4-byte floating point value */
   public static final int FLOAT = 0x0004;
   /** Double - floating point double */
   public static final int DOUBLE = 0x0005;
   /** Currency - signed 64-bit integer that represents a base ten decimal with four digits to the right of the decimal point */
   public static final int CURRENCY = 0x0006;
   /** AppTime - application time value */
   public static final int APP_TIME = 0x0007;
   /** Error - 32-bit error value */
   public static final int ERROR = 0x000A;
   /** Boolean - 16-bit Boolean value. '0' is false. Non-zero is true */
   public static final int BOOLEAN = 0x000B;
   /** Object/Directory - embedded object in a property */
   public static final int DIRECTORY = 0x000D;
   /** I8 - 8-byte signed integer */
   public static final int LONG_LONG = 0x0014;
   /** SysTime - FILETIME 64-bit integer specifying the number of 100ns periods since Jan 1, 1601 */
   public static final int TIME = 0x0040;
   /** ClassId - OLE GUID */
   public static final int CLS_ID = 0x0048;

   /** Binary - counted byte array */
   public static final int BINARY = 0x0102;

   /** 
    * An 8-bit string, probably in CP1252, but don't quote us...
    * Normally used for everything before Outlook 3.0, and some
    *  fields in Outlook 3.0.
    */
   public static final int ASCII_STRING = 0x001E;
   /** A string, from Outlook 3.0 onwards. Normally unicode */
   public static final int UNICODE_STRING = 0x001F;

   /** MultiValued - Value part contains multiple values */
   public static final int MULTIVALUED_FLAT = 0x1000;


   public static String asFileEnding(int type) {
      String str = Integer.toHexString(type).toUpperCase();
      while(str.length() < 4) {
         str = "0" + str;
      }
      return str;
   }
   public static String asName(int type) {
      switch(type) {
      case BINARY:
         return "Binary";
      case ASCII_STRING:
         return "ASCII String";
      case UNICODE_STRING:
         return "Unicode String";
      case UNSPECIFIED:
         return "Unspecified";
      case NULL:
         return "Null";
      case SHORT:
         return "Short";
      case LONG:
         return "Long";
      case LONG_LONG:
         return "Long Long";
      case FLOAT:
         return "Float";
      case DOUBLE:
         return "Double";
      case CURRENCY:
         return "Currency";
      case APP_TIME:
         return "Application Time";
      case ERROR:
         return "Error";
      case TIME:
         return "Time";
      case BOOLEAN:
         return "Boolean";
      case CLS_ID:
         return "CLS ID GUID";
      case DIRECTORY:
         return "Directory";
      case -1:
         return "Unknown";
      }
      return "0x" + Integer.toHexString(type);
   }
}
