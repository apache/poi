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
        
package org.apache.poi.hssf.record;


import junit.framework.TestCase;

/**
 * Tests the serialization and deserialization of the FontRecord
 * class works correctly.  Test data taken directly from a real
 * Excel file.
 */
public final class TestFontRecord extends TestCase {
	byte[] header = new byte[] {
    		0x31, 00, 0x1a, 00, // sid=31, 26 bytes long
	};
    byte[] data = new byte[] {
    		0xC8-256, 00,       // font height = xc8
    		00, 00,             // attrs = 0 
    		0xFF-256, 0x7F,     // colour palette = x7fff 
    		0x90-256, 0x01,     // bold weight = x190
    		00, 00,  // supersubscript
    		00, 00,  // underline, family
    		00, 00,  // charset, padding
    		05, 01,  // name length, unicode flag
    		0x41, 0x00, 0x72, 0x00, 0x69, // Arial, as unicode 
    		0x00, 0x61, 0x00, 0x6C, 0x00
    };

    public void testLoad() {

        FontRecord record = new FontRecord(new TestcaseRecordInputStream((short)0x31, (short)data.length, data));
        assertEquals( 0xc8, record.getFontHeight());
        assertEquals( 0x00, record.getAttributes());
        assertFalse( record.isItalic());
        assertFalse( record.isStruckout());
        assertFalse( record.isMacoutlined());
        assertFalse( record.isMacshadowed());
        assertEquals( 0x7fff, record.getColorPaletteIndex());
        assertEquals( 0x190, record.getBoldWeight());
        assertEquals( 0x00, record.getSuperSubScript());
        assertEquals( 0x00, record.getUnderline());
        assertEquals( 0x00, record.getFamily());
        assertEquals( 0x00, record.getCharset());
        assertEquals( 0x05, record.getFontNameLength());
        assertEquals( "Arial", record.getFontName());


        assertEquals( 26 + 4, record.getRecordSize() );
    }

    public void testStore()
    {
//      .fontheight      = c8
//      .attributes      = 0
//           .italic     = false
//           .strikout   = false
//           .macoutlined= false
//           .macshadowed= false
//      .colorpalette    = 7fff
//      .boldweight      = 190
//      .supersubscript  = 0
//      .underline       = 0
//      .family          = 0
//      .charset         = 0
//      .namelength      = 5
//      .fontname        = Arial

        FontRecord record = new FontRecord();
        record.setFontHeight((short)0xc8);
        record.setAttributes((short)0);
        record.setColorPaletteIndex((short)0x7fff);
        record.setBoldWeight((short)0x190);
        record.setSuperSubScript((short)0);
        record.setUnderline((byte)0);
        record.setFamily((byte)0);
        record.setCharset((byte)0);
        record.setFontNameLength((byte)5);
        record.setFontName("Arial");

        byte [] recordBytes = record.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
    
    public void testCloneOnto() throws Exception {
        FontRecord base = new FontRecord(new TestcaseRecordInputStream((short)0x31, (short)data.length, data));
    	
        FontRecord other = new FontRecord();
        other.cloneStyleFrom(base);

        byte [] recordBytes = other.serialize();
        assertEquals(recordBytes.length - 4, data.length);
        for (int i = 0; i < data.length; i++)
            assertEquals("At offset " + i, data[i], recordBytes[i+4]);
    }
    
    public void testSameProperties() throws Exception {
        FontRecord f1 = new FontRecord(new TestcaseRecordInputStream((short)0x31, (short)data.length, data));
        FontRecord f2 = new FontRecord(new TestcaseRecordInputStream((short)0x31, (short)data.length, data));
    	
        assertTrue(f1.sameProperties(f2));
        
        f2.setFontName("Arial2");
        assertFalse(f1.sameProperties(f2));
        f2.setFontName("Arial");
        assertTrue(f1.sameProperties(f2));
        
        f2.setFontHeight((short)11);
        assertFalse(f1.sameProperties(f2));
        f2.setFontHeight((short)0xc8);
        assertTrue(f1.sameProperties(f2));
    }
}
