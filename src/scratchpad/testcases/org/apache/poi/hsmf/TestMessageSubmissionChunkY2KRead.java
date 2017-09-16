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

package org.apache.poi.hsmf;

import java.io.IOException;

import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.poi.POIDataSamples;

import java.util.Calendar;

import junit.framework.TestCase;

public final class TestMessageSubmissionChunkY2KRead extends TestCase {
    
    private final MAPIMessage mapiMessage1979;
    private final MAPIMessage mapiMessage1980;
    private final MAPIMessage mapiMessage1981;

    /**
     * Initialise this test, load up the three test messages.
     * @throws Exception
     */
    public TestMessageSubmissionChunkY2KRead() throws IOException {
        POIDataSamples samples = POIDataSamples.getHSMFInstance();
        this.mapiMessage1979 = new MAPIMessage(samples.openResourceAsStream("message_1979.msg"));
        this.mapiMessage1980 = new MAPIMessage(samples.openResourceAsStream("message_1980.msg"));
        this.mapiMessage1981 = new MAPIMessage(samples.openResourceAsStream("message_1981.msg"));
    }

    // 1979 is one year before our pivot year (so this is an expected "failure")
    public void testReadMessageDate1979() throws ChunkNotFoundException {
        final Calendar date = mapiMessage1979.getMessageDate();
        final int year = date.get(Calendar.YEAR);
        TestCase.assertEquals(2079, year);
    }

    // 1980 is our pivot year (so this is an expected "failure")
    public void testReadMessageDate1980() throws ChunkNotFoundException {
        final Calendar date = mapiMessage1980.getMessageDate();
        final int year = date.get(Calendar.YEAR);
        TestCase.assertEquals(2080, year);
    }

    // 1981 is one year after our pivot year (so this starts working)
    public void testReadMessageDate1981() throws ChunkNotFoundException {
        final Calendar date = mapiMessage1981.getMessageDate();
        final int year = date.get(Calendar.YEAR);
        TestCase.assertEquals(1981, year);
    }
}
