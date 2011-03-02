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

package org.apache.poi.hmef;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hmef.attribute.MAPIAttribute;
import org.apache.poi.hmef.attribute.TNEFAttribute;
import org.apache.poi.hmef.attribute.TNEFMAPIAttribute;
import org.apache.poi.hmef.attribute.TNEFProperty;
import org.apache.poi.hsmf.datatypes.MAPIProperty;


/**
 * An attachment within a {@link HMEFMessage}. Provides both helper
 *  methods to get at common parts and attributes of the attachment,
 *  and list methods to get all of them.
 */
public final class Attachment {
   private final List<TNEFAttribute> attributes = new ArrayList<TNEFAttribute>();
   private final List<MAPIAttribute> mapiAttributes = new ArrayList<MAPIAttribute>();
   
   protected void addAttribute(TNEFAttribute attr) {
      attributes.add(attr);
      
      if(attr instanceof TNEFMAPIAttribute) {
         TNEFMAPIAttribute tnefMAPI = (TNEFMAPIAttribute)attr;
         mapiAttributes.addAll( tnefMAPI.getMAPIAttributes() );
      }
   }
   
   /**
    * Return the attachment attribute with the given ID,
    *  or null if there isn't one. 
    */
   public TNEFAttribute getMessageAttribute(TNEFProperty id) {
      for(TNEFAttribute attr : attributes) {
         if(attr.getProperty() == id) {
            return attr;
         }
      }
      return null;
   }
   
   /**
    * Return the attachment MAPI Attribute with the given ID,
    *  or null if there isn't one. 
    */
   public MAPIAttribute getMessageMAPIAttribute(MAPIProperty id) {
      for(MAPIAttribute attr : mapiAttributes) {
         if(attr.getProperty() == id) {
            return attr;
         }
      }
      return null;
   }
   
   /**
    * Returns all HMEF/TNEF attributes of the attachment, 
    *  such as filename, icon and contents
    */
   public List<TNEFAttribute> getAttributes() {
      return attributes;
   }
   
   /**
    * Returns all MAPI attributes of the attachment, 
    *  such as extension, encoding, size and position
    */
   public List<MAPIAttribute> getMAPIAttributes() {
      return mapiAttributes;
   }
   
   public String getFilename() {
      TNEFAttribute attr = null;
      return null;
   }
}
