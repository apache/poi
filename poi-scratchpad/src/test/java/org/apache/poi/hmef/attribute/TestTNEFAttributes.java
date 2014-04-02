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
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hmef.Attachment;
import org.apache.poi.hmef.HMEFMessage;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.util.LittleEndian;

public final class TestTNEFAttributes extends TestCase {
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
	   // The message should have 4 attributes 
      assertEquals(4, quick.getMessageAttributes().size());
      
      // Each attachment should have 6 attributes
      for(Attachment attach : quick.getAttachments()) {
         assertEquals(6, attach.getAttributes().size());
      }
	}
	
	/** 
	 * Test the basics
	 */
	public void testBasics() throws Exception {
	   // An int one
      assertEquals(
            0x010000, 
            LittleEndian.getInt( quick.getMessageAttribute(TNEFProperty.ID_TNEFVERSION).getData() )
      );
      
      // Claims not to be text, but really is
      assertEquals(
            "IPM.Microsoft Mail.Note\0", 
            new String(quick.getMessageAttribute(TNEFProperty.ID_MESSAGECLASS).getData(), "ASCII")
      );
	   
	   // Try constructing two attributes
      byte[] data = new byte[] {
            // Level one, id 36870, type 8
            0x01,    0x06, (byte)0x90,   0x08, 0x00,
            // Length 4
            0x04, 0x00, 0x00, 0x00,
            // Data
            0x00, 0x00, 0x01, 0x00,
            // Checksum
            0x01, 0x00, 
            
            // level one, id 36871, type 6
            0x01,    0x07, (byte)0x90,   0x06, 0x00,
            // Length 8
            0x08, 0x00, 0x00, 0x00,
            // Data
            (byte)0xe4, 0x04, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            // Checksum
            (byte)0xe8, 0x00
      };
      ByteArrayInputStream bais = new ByteArrayInputStream(data);
      
      // Create them
      int level = bais.read();
      assertEquals(1, level);
      TNEFAttribute attr1 = TNEFAttribute.create(bais);
      
      level = bais.read();
      assertEquals(1, level);
      TNEFAttribute attr2 = TNEFAttribute.create(bais);
      
      assertEquals(-1, bais.read());
      
      // Check them
      assertEquals(TNEFProperty.ID_TNEFVERSION, attr1.getProperty());
      assertEquals(8, attr1.getType());
      assertEquals(4, attr1.getData().length);
      assertEquals(0x010000, LittleEndian.getInt( attr1.getData() ));
      
      assertEquals(TNEFProperty.ID_OEMCODEPAGE, attr2.getProperty());
      assertEquals(6, attr2.getType());
      assertEquals(8, attr2.getData().length);
      assertEquals(0x04e4, LittleEndian.getInt( attr2.getData() ));
	}
	
	/**
	 * Test string based ones
	 */
	public void testString() throws Exception {
	   TNEFAttribute attr = quick.getAttachments().get(0).getAttribute(
	         TNEFProperty.ID_ATTACHTITLE
	   );
	   assertNotNull(attr);
	   assertEquals(TNEFStringAttribute.class, attr.getClass());
	   
	   // It is a null terminated string
	   assertEquals("quick.doc\u0000", new String(attr.getData(), "ASCII"));
	   
	   // But when we ask for the string, that is sorted for us
	   TNEFStringAttribute str = (TNEFStringAttribute)attr;
	   assertEquals("quick.doc", str.getString());
	}

	/**
	 * Test date based ones
	 */
	public void testDate() throws Exception {
      TNEFAttribute attr = quick.getAttachments().get(0).getAttribute(
            TNEFProperty.ID_ATTACHMODIFYDATE
      );
      assertNotNull(attr);
      assertEquals(TNEFDateAttribute.class, attr.getClass());
      
      // It is a series of date parts
      // Weds 28th April 2010 @ 12:40:56 UTC
      assertEquals(2010, LittleEndian.getUShort(attr.getData(), 0));
      assertEquals(04, LittleEndian.getUShort(attr.getData(), 2));
      assertEquals(28, LittleEndian.getUShort(attr.getData(), 4));
      assertEquals(12, LittleEndian.getUShort(attr.getData(), 6));
      assertEquals(40, LittleEndian.getUShort(attr.getData(), 8));
      assertEquals(56, LittleEndian.getUShort(attr.getData(), 10));
      assertEquals(3, LittleEndian.getUShort(attr.getData(), 12)); // Weds
      
      // Ask for it as a Java date, and have it converted
      // Pick a predictable format + location + timezone
      TNEFDateAttribute date = (TNEFDateAttribute)attr;
      DateFormat fmt = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.UK
      );
      fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
      assertEquals("28-Apr-2010 12:40:56", fmt.format(date.getDate()));
	}
	
	/** 
	 * Test a bit of mapi
	 */
	public void testMAPI() throws Exception {
	   // Message MAPI
      TNEFAttribute attr = quick.getMessageAttribute(
            TNEFProperty.ID_MAPIPROPERTIES
      );
      assertNotNull(attr);
      assertEquals(TNEFMAPIAttribute.class, attr.getClass());
	   
      TNEFMAPIAttribute mapi = (TNEFMAPIAttribute)attr;
      assertEquals(54, mapi.getMAPIAttributes().size());
      assertEquals(
            MAPIProperty.ALTERNATE_RECIPIENT_ALLOWED, 
            mapi.getMAPIAttributes().get(0).getProperty()
      );
      
      
	   // Attribute MAPI
      attr = quick.getAttachments().get(0).getAttribute(
            TNEFProperty.ID_ATTACHMENT
      );
      assertNotNull(attr);
      assertEquals(TNEFMAPIAttribute.class, attr.getClass());
      
      mapi = (TNEFMAPIAttribute)attr;
      assertEquals(22, mapi.getMAPIAttributes().size());
      assertEquals(
            MAPIProperty.ATTACH_SIZE, 
            mapi.getMAPIAttributes().get(0).getProperty()
      );
	}
	
	/**
	 * Test common ones via helpers
	 */
	public void testCommon() throws Exception {
	   assertEquals("This is a test message", quick.getSubject());
	   assertEquals("quick.doc", quick.getAttachments().get(0).getFilename());
	}
}
