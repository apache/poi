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

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.IntMapper;

/**
 * Exercise the SSTDeserializer class.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestSSTDeserializer extends TestCase {


    private byte[] joinArray(byte[] array1, byte[] array2) {
        byte[] bigArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, bigArray, 0, array1.length);
        System.arraycopy(array2, 0, bigArray, array1.length, array2.length);
        return bigArray;
    }
    
    private static byte[] readSampleHexData(String sampleFileName, String sectionName) {
        InputStream is = HSSFTestDataSamples.openSampleFileStream(sampleFileName);
        try {
            return HexRead.readData(is, sectionName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void testSpanRichTextToPlainText()
            throws Exception
    {
      byte[] header = readSampleHexData("richtextdata.txt", "header" );
        byte[] continueBytes = readSampleHexData("richtextdata.txt", "continue1" );
      continueBytes = TestcaseRecordInputStream.mergeDataAndSid(ContinueRecord.sid, (short)continueBytes.length, continueBytes);
      TestcaseRecordInputStream in = new TestcaseRecordInputStream((short)0, (short)header.length, joinArray(header, continueBytes));
      

        IntMapper strings = new IntMapper();
        SSTDeserializer deserializer = new SSTDeserializer( strings );
        deserializer.manufactureStrings(1, in );

        assertEquals( "At a dinner party orAt At At ", strings.get( 0 ) + "" );
    }

    public void testContinuationWithNoOverlap()
            throws Exception
    {
        byte[] header = readSampleHexData("evencontinuation.txt", "header" );
        byte[] continueBytes = readSampleHexData("evencontinuation.txt", "continue1" );
        continueBytes = TestcaseRecordInputStream.mergeDataAndSid(ContinueRecord.sid, (short)continueBytes.length, continueBytes);
        TestcaseRecordInputStream in = new TestcaseRecordInputStream((short)0, (short)header.length, joinArray(header, continueBytes));

        IntMapper strings = new IntMapper();
        SSTDeserializer deserializer = new SSTDeserializer( strings );
        deserializer.manufactureStrings( 2, in);

        assertEquals( "At a dinner party or", strings.get( 0 ) + "" );
        assertEquals( "At a dinner party", strings.get( 1 ) + "" );
    }

    /**
     * Strings can actually span across more than one continuation.
     */
    public void testStringAcross2Continuations()
            throws Exception
    {
        byte[] header = readSampleHexData("stringacross2continuations.txt", "header" );
        byte[] continue1 = readSampleHexData("stringacross2continuations.txt", "continue1" );
        continue1 = TestcaseRecordInputStream.mergeDataAndSid(ContinueRecord.sid, (short)continue1.length, continue1);
        byte[] continue2 = readSampleHexData("stringacross2continuations.txt", "continue2" );
        continue2 = TestcaseRecordInputStream.mergeDataAndSid(ContinueRecord.sid, (short)continue2.length, continue2);
        
        byte[] bytes = joinArray(header, continue1);
        bytes = joinArray(bytes, continue2);
        TestcaseRecordInputStream in = new TestcaseRecordInputStream((short)0, (short)header.length, bytes);

        IntMapper strings = new IntMapper();
        SSTDeserializer deserializer = new SSTDeserializer( strings );
        deserializer.manufactureStrings( 2, in);

        assertEquals( "At a dinner party or", strings.get(  0 ) + "" );
        assertEquals( "At a dinner partyAt a dinner party", strings.get( 1 ) + "" );
    }

    public void testExtendedStrings() {
        byte[] header = readSampleHexData("extendedtextstrings.txt", "rich-header" );
        byte[] continueBytes = readSampleHexData("extendedtextstrings.txt", "rich-continue1" );
        continueBytes = TestcaseRecordInputStream.mergeDataAndSid(ContinueRecord.sid, (short)continueBytes.length, continueBytes);
        TestcaseRecordInputStream in = new TestcaseRecordInputStream((short)0, (short)header.length, joinArray(header, continueBytes));
        
        IntMapper strings = new IntMapper();
        SSTDeserializer deserializer = new SSTDeserializer( strings );
        deserializer.manufactureStrings( 1, in);

        assertEquals( "At a dinner party orAt At At ", strings.get( 0  ) + "" );


        header = readSampleHexData("extendedtextstrings.txt", "norich-header" );
        continueBytes = readSampleHexData("extendedtextstrings.txt", "norich-continue1" );
        continueBytes = TestcaseRecordInputStream.mergeDataAndSid(ContinueRecord.sid, (short)continueBytes.length, continueBytes);
        in = new TestcaseRecordInputStream((short)0, (short)header.length, joinArray(header, continueBytes));
        
        strings = new IntMapper();
        deserializer = new SSTDeserializer( strings );
        deserializer.manufactureStrings( 1, in);

        assertEquals( "At a dinner party orAt At At ", strings.get( 0 ) + "" );
    }
}
