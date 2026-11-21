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
import java.time.Instant;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

@Isolated // changes the user timezone in LocaleUtil, so other tests should not run at the same time
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

    /**
     * Bug 70365 - the submission-ID timestamp ends in 'Z' and is UTC,
     * so the parsed instant must not depend on the user timezone
     */
    @Test
    void testAcceptedAtTimeIsUtc() throws IOException, ChunkNotFoundException {
        // quick.msg has l=THHS2EXBE1X-070614094255Z-84723
        Instant expected = Instant.parse("2007-06-14T09:42:55Z");
        TimeZone userTZ = LocaleUtil.getUserTimeZone();
        for (String tz : new String[]{"UTC", "America/New_York", "Australia/Adelaide"}) {
            LocaleUtil.setUserTimeZone(TimeZone.getTimeZone(tz));
            // the timestamp is parsed while the message is read, so open it under the zone
            try (MAPIMessage msg = new MAPIMessage(POIDataSamples.getHSMFInstance().openResourceAsStream("quick.msg"))) {
                assertEquals(expected, msg.getMainChunks().getSubmissionChunk().getAcceptedAtTime().toInstant(), tz);
                assertEquals(expected, msg.getMessageDate().toInstant(), tz);
            } finally {
                LocaleUtil.setUserTimeZone(userTZ);
            }
        }
    }
}
