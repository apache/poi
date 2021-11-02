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

package org.apache.poi.hslf.usermodel;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.poi.hslf.HSLFTestDataSamples;
import org.junit.jupiter.api.Test;

/**
 * Test reading sound data from a ppt
 */
public final class TestSoundData {
    /**
     * Read a reference sound file from disk and compare it from the data extracted from the slide show
     */
    @Test
    void testSounds() throws Exception {
        //read the reference sound file
        byte[] ref_data = HSLFTestDataSamples.getTestDataFileContent("ringin.wav");

        HSLFSlideShow ppt = HSLFTestDataSamples.getSlideShow("sound.ppt");

        HSLFSoundData[] sound = ppt.getSoundData();
        assertEquals(1, sound.length, "Expected 1 sound");

        assertArrayEquals(ref_data, sound[0].getData());
        ppt.close();
    }
}
