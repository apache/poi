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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.Calendar;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class TestMessageSubmissionChunk {

    private MAPIMessage mapiMessageExtraHyphenSubmissionChunk;
    private MAPIMessage mapiMessageNormalSubmissionChunk;

    /**
     * Initialise this test, load up the test messages.
     */
    @BeforeEach
    void setup() throws IOException {
        POIDataSamples samples = POIDataSamples.getHSMFInstance();
        this.mapiMessageExtraHyphenSubmissionChunk = new MAPIMessage(samples.openResourceAsStream("message_extra_hyphen_submission_chunk.msg"));
        this.mapiMessageNormalSubmissionChunk = new MAPIMessage(samples.openResourceAsStream("message_normal_submission_chunk.msg"));
    }

    @Test
    void testReadMessageDateExtraHyphenSubmissionChunk() throws ChunkNotFoundException {
        final Calendar date = mapiMessageExtraHyphenSubmissionChunk.getMessageDate();
        assertNotNull(date);
        final int year = date.get(Calendar.YEAR);
        assertEquals(2007, year);
    }

    @Test
    void testReadMessageDateNormalSubmissionChunk() throws ChunkNotFoundException {
        final Calendar date = mapiMessageNormalSubmissionChunk.getMessageDate();
        assertNotNull(date);
        final int year = date.get(Calendar.YEAR);
        assertEquals(2007, year);
    }
}
