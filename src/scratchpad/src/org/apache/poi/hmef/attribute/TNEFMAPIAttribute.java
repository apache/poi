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
import java.util.List;

import org.apache.poi.hmef.Attachment;
import org.apache.poi.hmef.HMEFMessage;

/**
 * A TNEF Attribute holding MAPI Attributes, which applies to a 
 *  {@link HMEFMessage} or one of its {@link Attachment}s.
 */
public final class TNEFMAPIAttribute extends TNEFAttribute {
   private final List<MAPIAttribute> attributes;
   
   /**
    * Constructs a single new mapi containing attribute from the 
    *  id, type, and the contents of the stream
    */
   protected TNEFMAPIAttribute(int id, int type, InputStream inp) throws IOException {
      super(id, type, inp);
      
      attributes = MAPIAttribute.create(this);
   }

   public List<MAPIAttribute> getMAPIAttributes() {
      return attributes;
   }
   
   public String toString() {
      return "Attribute " + getProperty() + ", type=" + getType() +
             ", " + attributes.size() + " MAPI Attributes"; 
   }
}
