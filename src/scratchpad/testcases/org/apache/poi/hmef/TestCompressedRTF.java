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

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hmef.attribute.MAPIAttribute;
import org.apache.poi.hmef.attribute.MAPIRtfAttribute;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

public final class TestCompressedRTF extends TestCase {
    private static final POIDataSamples _samples = POIDataSamples.getHMEFInstance();

    private static final String block1 = "{\\rtf1\\adeflang102";
    private static final String block2 = block1 + "5\\ansi\\ansicpg1252";
    
    /**
     * Check that things are as we expected. If this fails,
     *  then decoding has no hope...  
     */
    public void testQuickBasics() throws Exception {
       HMEFMessage msg = new HMEFMessage(
             _samples.openResourceAsStream("quick-winmail.dat")
       );
       
       MAPIAttribute rtfAttr = msg.getMessageMAPIAttribute(MAPIProperty.RTF_COMPRESSED);
       assertNotNull(rtfAttr);
       assertTrue(rtfAttr instanceof MAPIRtfAttribute);
       
       // Check the start of the compressed version
       byte[] data = ((MAPIRtfAttribute)rtfAttr).getRawData();
       assertEquals(5907, data.length);
       
       // First 16 bytes is header stuff
       // Check it has the length + compressed marker
       assertEquals(5907-4, LittleEndian.getShort(data));
       assertEquals(
             "LZFu", 
             StringUtil.getFromCompressedUnicode(data, 8, 4)
       );
             
       
       // Now Look at the code
       assertEquals((byte)0x07, data[16+0]);  // Flag: cccUUUUU
       assertEquals((byte)0x00, data[16+1]);  //  c1a: offset 0 / 0x000
       assertEquals((byte)0x06, data[16+2]);  //  c1b: length 6+2  -> {\rtf1\a
       assertEquals((byte)0x01, data[16+3]);  //  c2a: offset 16 / 0x010
       assertEquals((byte)0x01, data[16+4]);  //  c2b: length 1+2  ->  def
       assertEquals((byte)0x0b, data[16+5]);  //  c3a: offset 182 / 0xb6
       assertEquals((byte)0x60, data[16+6]);  //  c3b: length 0+2  -> la 
       assertEquals((byte)0x6e, data[16+7]);  // n
       assertEquals((byte)0x67, data[16+8]);  // g
       assertEquals((byte)0x31, data[16+9]);  // 1
       assertEquals((byte)0x30, data[16+10]); // 0
       assertEquals((byte)0x32, data[16+11]); // 2
       
       assertEquals((byte)0x66, data[16+12]); // Flag:  UccUUccU
       assertEquals((byte)0x35, data[16+13]); // 5 
       assertEquals((byte)0x00, data[16+14]); //  c2a: offset 6 / 0x006
       assertEquals((byte)0x64, data[16+15]); //  c2b: length 4+2  -> \ansi\a
       assertEquals((byte)0x00, data[16+16]); //  c3a: offset 7 / 0x007
       assertEquals((byte)0x72, data[16+17]); //  c3b: length 2+2  -> nsi
       assertEquals((byte)0x63, data[16+18]); // c 
       assertEquals((byte)0x70, data[16+19]); // p
       assertEquals((byte)0x0d, data[16+20]); //  c6a: offset 221 / 0x0dd
       assertEquals((byte)0xd0, data[16+21]); //  c6b: length 0+2  -> g1
       assertEquals((byte)0x0e, data[16+22]); //  c7a: offset 224 / 0x0e0
       assertEquals((byte)0x00, data[16+23]); //  c7b: length 0+2  -> 25
       assertEquals((byte)0x32, data[16+24]); // 2
    }

    /**
     * Check that we can decode the first 8 codes
     * (1 flag byte + 8 codes)  
     */
    public void testFirstBlock() throws Exception {
       HMEFMessage msg = new HMEFMessage(
             _samples.openResourceAsStream("quick-winmail.dat")
       );
       
       MAPIAttribute attr = msg.getMessageMAPIAttribute(MAPIProperty.RTF_COMPRESSED);
       assertNotNull(attr);
       MAPIRtfAttribute rtfAttr = (MAPIRtfAttribute)attr;

       // Truncate to header + flag + data for flag
       byte[] data = new byte[16+12];
       System.arraycopy(rtfAttr.getRawData(), 0, data, 0, data.length);
       
       // Decompress it
       CompressedRTF comp = new CompressedRTF();
       byte[] decomp = comp.decompress(new ByteArrayInputStream(data));
       String decompStr = new String(decomp, "ASCII");
       
       // Test
       assertEquals(block1.length(), decomp.length);
       assertEquals(block1, decompStr);
    }

    /**
     * Check that we can decode the first 16 codes
     * (flag + 8 codes, flag + 8 codes)  
     */
    public void testFirstTwoBlocks() throws Exception {
       HMEFMessage msg = new HMEFMessage(
             _samples.openResourceAsStream("quick-winmail.dat")
       );

       MAPIAttribute attr = msg.getMessageMAPIAttribute(MAPIProperty.RTF_COMPRESSED);
       assertNotNull(attr);
       MAPIRtfAttribute rtfAttr = (MAPIRtfAttribute)attr;

       // Truncate to header + flag + data for flag + flag + data
       byte[] data = new byte[16+12+13];
       System.arraycopy(rtfAttr.getRawData(), 0, data, 0, data.length);
       
       // Decompress it
       CompressedRTF comp = new CompressedRTF();
       byte[] decomp = comp.decompress(new ByteArrayInputStream(data));
       String decompStr = new String(decomp, "ASCII");
       
       // Test
       assertEquals(block2.length(), decomp.length);
       assertEquals(block2, decompStr);
    }

    /**
     * Check that we can correctly decode the whole file
     * @throws Exception
     */
    public void testFull() throws Exception {
       HMEFMessage msg = new HMEFMessage(
             _samples.openResourceAsStream("quick-winmail.dat")
       );
       
       // TODO
    }
}
