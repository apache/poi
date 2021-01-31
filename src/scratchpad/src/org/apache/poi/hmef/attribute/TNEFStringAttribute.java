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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hmef.Attachment;
import org.apache.poi.hmef.HMEFMessage;
import org.apache.poi.util.StringUtil;

/**
 * A String attribute which applies to a {@link HMEFMessage}
 *  or one of its {@link Attachment}s.
 */
public final class TNEFStringAttribute extends TNEFAttribute {
   private static final Logger LOG = LogManager.getLogger(TNEFStringAttribute.class);
   private final String data;

   /**
    * Constructs a single new string attribute from the id, type,
    *  and the contents of the stream
    */
   protected TNEFStringAttribute(int id, int type, InputStream inp) throws IOException {
      super(id, type, inp);

      String tmpData = null;
      byte[] data = getData();
      if(getType() == TNEFProperty.TYPE_TEXT) {
         tmpData = StringUtil.getFromUnicodeLE(data);
      } else {
         tmpData = StringUtil.getFromCompressedUnicode(
              data, 0, data.length
         );
      }

      // Strip off the null terminator if present
      if(tmpData.endsWith("\0")) {
         tmpData = tmpData.substring(0, tmpData.length()-1);
      }
      this.data = tmpData;
   }

   public String getString() {
      return this.data;
   }

   public String toString() {
      return "Attribute " + getProperty() + ", type=" + getType() +
             ", data=" + getString();
   }

   /**
    * Returns the string of a Attribute, converting as appropriate
    */
   public static String getAsString(TNEFAttribute attr) {
      if(attr == null) {
         return null;
      }
      if(attr instanceof TNEFStringAttribute) {
         return ((TNEFStringAttribute)attr).getString();
      }

      LOG.atWarn().log("Warning, non string property found: {}", attr);
      return null;
  }
}
