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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.common.UnicodeString;
import org.apache.poi.util.HexRead;
import org.apache.poi.util.IntMapper;
import org.junit.Test;

/**
 * Exercise the SSTDeserializer class.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class TestSSTDeserializer {
	private static final int FAKE_SID = -5555;

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    
    private static byte[] readSampleHexData(String sampleFileName, String sectionName, int recSid)
    throws IOException {
        InputStream is = HSSFTestDataSamples.openSampleFileStream(sampleFileName);
        byte[] data = HexRead.readData(is, sectionName);
        byte[] result = TestcaseRecordInputStream.mergeDataAndSid(recSid, data.length, data);
        is.close();
        return result;
    }

    @Test
    public void testSpanRichTextToPlainText() throws IOException {
        byte[] header = readSampleHexData("richtextdata.txt", "header", FAKE_SID);
        byte[] continueBytes = readSampleHexData("richtextdata.txt", "continue1", ContinueRecord.sid);
        RecordInputStream in = TestcaseRecordInputStream.create(concat(header, continueBytes));
      

        IntMapper<UnicodeString> strings = new IntMapper<>();
        SSTDeserializer deserializer = new SSTDeserializer( strings );
        deserializer.manufactureStrings(1, in );

        assertEquals( "At a dinner party orAt At At ", strings.get( 0 ) + "" );
    }

    @Test
    public void testContinuationWithNoOverlap() throws IOException {
        byte[] header = readSampleHexData("evencontinuation.txt", "header", FAKE_SID);
        byte[] continueBytes = readSampleHexData("evencontinuation.txt", "continue1", ContinueRecord.sid);
        RecordInputStream in = TestcaseRecordInputStream.create(concat(header, continueBytes));

        IntMapper<UnicodeString> strings = new IntMapper<>();
        SSTDeserializer deserializer = new SSTDeserializer( strings );
        deserializer.manufactureStrings( 2, in);

        assertEquals( "At a dinner party or", strings.get( 0 ) + "" );
        assertEquals( "At a dinner party", strings.get( 1 ) + "" );
    }

    /**
     * Strings can actually span across more than one continuation.
     */
    @Test
    public void testStringAcross2Continuations() throws IOException {
        byte[] header = readSampleHexData("stringacross2continuations.txt", "header", FAKE_SID);
        byte[] continue1 = readSampleHexData("stringacross2continuations.txt", "continue1", ContinueRecord.sid);
        byte[] continue2 = readSampleHexData("stringacross2continuations.txt", "continue2", ContinueRecord.sid);
        
        RecordInputStream in = TestcaseRecordInputStream.create(concat(header, concat(continue1, continue2)));

        IntMapper<UnicodeString> strings = new IntMapper<>();
        SSTDeserializer deserializer = new SSTDeserializer( strings );
        deserializer.manufactureStrings( 2, in);

        assertEquals( "At a dinner party or", strings.get(  0 ) + "" );
        assertEquals( "At a dinner partyAt a dinner party", strings.get( 1 ) + "" );
    }

    @Test
    public void testExtendedStrings() throws IOException {
        byte[] header = readSampleHexData("extendedtextstrings.txt", "rich-header", FAKE_SID);
        byte[] continueBytes = readSampleHexData("extendedtextstrings.txt", "rich-continue1", ContinueRecord.sid);
        RecordInputStream in = TestcaseRecordInputStream.create(concat(header, continueBytes));
        
        IntMapper<UnicodeString> strings = new IntMapper<>();
        SSTDeserializer deserializer = new SSTDeserializer( strings );
        deserializer.manufactureStrings( 1, in);

        assertEquals( "At a dinner party orAt At At ", strings.get( 0  ) + "" );


        header = readSampleHexData("extendedtextstrings.txt", "norich-header", FAKE_SID);
        continueBytes = readSampleHexData("extendedtextstrings.txt", "norich-continue1", ContinueRecord.sid);
        in = TestcaseRecordInputStream.create(concat(header, continueBytes));
        
        strings = new IntMapper<>();
        deserializer = new SSTDeserializer( strings );
        deserializer.manufactureStrings( 1, in);

        assertEquals( "At a dinner party orAt At At ", strings.get( 0 ) + "" );
    }
}
