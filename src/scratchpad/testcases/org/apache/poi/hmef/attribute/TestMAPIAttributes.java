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
import java.text.DateFormat;
import java.util.Locale;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hmef.HMEFMessage;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.util.LittleEndian;

public final class TestMAPIAttributes extends TestCase {
   private static final POIDataSamples _samples = POIDataSamples.getHMEFInstance();
   private HMEFMessage quick;
   
   @Override
   protected void setUp() throws Exception {
      super.setUp();
      
      quick = new HMEFMessage(
            _samples.openResourceAsStream("quick-winmail.dat")
      );
   }
   
   /** 
    * Test counts
    */
   public void testCounts() throws Exception {
      // Message should have 54
      assertEquals(54, quick.getMessageMAPIAttributes().size());
      
      // First attachment should have 22
      assertEquals(22, quick.getAttachments().get(0).getMAPIAttributes().size());
   }
	
   /**
    * Test various general ones
    */
   public void testBasics() throws Exception {
      // Try constructing two attributes
      byte[] data = new byte[] {
            // Level one, id 36867, type 6
            0x01,    0x03, (byte)0x90,   0x06, 0x00,
            // Length 24
            0x24, 0x00, 0x00, 0x00,
            
            // Three attributes
            0x03, 0x00, 0x00, 0x00,
            // AlternateRecipientAllowed = 01 00
            0x0B, 0x00, 0x02, 0x00,
            0x01, 0x00, 0x00, 0x00,
            // Priority = 00 00 00 00
            0x03, 0x00, 0x26, 0x00,
            0x00, 0x00, 0x00, 0x00,
            // ConversationTopic = Test
            0x1E, 0x00, 0x70, 0x00,
            0x01, 0x00, 0x00, 0x00,
            0x04, 0x00, 0x00, 0x00,
            (byte)'T', (byte)'e',
            (byte)'s', (byte)'t',
            // Checksum (may be wrong...)
            0x01, 0x00
      };
      ByteArrayInputStream bais = new ByteArrayInputStream(data);
      
      // Create it
      int level = bais.read();
      assertEquals(1, level);
      TNEFAttribute attr = TNEFAttribute.create(bais);
      
      // Check it
      assertNotNull(attr);
      assertEquals(TNEFMAPIAttribute.class, attr.getClass());
      
      TNEFMAPIAttribute mapi = (TNEFMAPIAttribute)attr;
      assertEquals(3, mapi.getMAPIAttributes().size());
      
      assertEquals(
            MAPIProperty.ALTERNATE_RECIPIENT_ALLOWED, 
            mapi.getMAPIAttributes().get(0).getProperty()
      );
      assertEquals(1, LittleEndian.getUShort(
               mapi.getMAPIAttributes().get(0).getData()
      ));
      
      assertEquals(
            MAPIProperty.PRIORITY, 
            mapi.getMAPIAttributes().get(1).getProperty()
      );
      assertEquals(0, LittleEndian.getUShort(
               mapi.getMAPIAttributes().get(1).getData()
      ));
      
      assertEquals(
            MAPIProperty.CONVERSATION_TOPIC, 
            mapi.getMAPIAttributes().get(2).getProperty()
      );
      assertEquals(
            "Test",
            ((MAPIStringAttribute)mapi.getMAPIAttributes().get(2)).getDataString()
      );
   }

   /**
    * Test String, Date and RTF ones
    */
   public void testTyped() throws Exception {
      MAPIAttribute attr;
      
      // String
      //  ConversationTopic -> This is a test message
      attr = quick.getMessageMAPIAttribute(MAPIProperty.CONVERSATION_TOPIC);
      assertNotNull(attr);
      assertEquals(MAPIStringAttribute.class, attr.getClass());
      
      MAPIStringAttribute str = (MAPIStringAttribute)attr;
      assertEquals("This is a test message", str.getDataString());
      
      // Date
      //  (Unknown/Custom) 32955 -> Wed Dec 15 2010 @ 14:46:31 UTC
      attr = null;
      for(MAPIAttribute a : quick.getMessageMAPIAttributes()) {
         if(a.getProperty().id == 32955) {
            attr = a;
            break;
         }
      }
      assertNotNull(attr);
      assertEquals(MAPIDateAttribute.class, attr.getClass());
      
      MAPIDateAttribute date = (MAPIDateAttribute)attr;
      DateFormat fmt = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.UK
      );
      assertEquals("15-Dec-2010 14:46:31", fmt.format(date.getDate()));
      
      // RTF
      //   RtfCompressed -> {\rtf1...
      attr = quick.getMessageMAPIAttribute(MAPIProperty.RTF_COMPRESSED);
      assertNotNull(attr);
      assertEquals(MAPIRtfAttribute.class, attr.getClass());
      
      MAPIRtfAttribute rtf = (MAPIRtfAttribute)attr;
      assertEquals("{\\rtf1", rtf.getDataString().substring(0, 6));
   }

   /**
    * Check common ones via helper accessors
    */
   public void testCommon() throws Exception {
      assertEquals("This is a test message", quick.getSubject());
      
      assertEquals("quick.doc", quick.getAttachments().get(0).getLongFilename());
      assertEquals(".doc", quick.getAttachments().get(0).getExtension());
   }
}
