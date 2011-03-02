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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.poi.hmef.Attachment;
import org.apache.poi.hmef.CompressedRTF;
import org.apache.poi.hmef.HMEFMessage;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.util.StringUtil;

/**
 * A pure-MAPI attribute holding RTF (compressed or not), which applies 
 *  to a {@link HMEFMessage} or one of its {@link Attachment}s.
 */
public final class MAPIRtfAttribute extends MAPIAttribute {
   private final String data;
   
   public MAPIRtfAttribute(MAPIProperty property, int type, byte[] data) throws IOException {
      super(property, type, data);
      
      CompressedRTF rtf = new CompressedRTF();
      byte[] decomp = rtf.decompress(new ByteArrayInputStream(data));
      
      this.data = StringUtil.getFromCompressedUnicode(decomp, 0, decomp.length);
   }
   
   public String getDataString() {
      return data;
   }
   
   public String toString() {
      return getProperty().toString() + " " + data;
   }
}
