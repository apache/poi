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

package org.apache.poi.hslf.record;

import static org.junit.Assert.assertArrayEquals;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;

/**
 * Tests Sound-related records: SoundCollection(2020), Sound(2022) and
 * SoundData(2023)).
 *
 * @author Yegor Kozlov
 */
public final class TestSound extends TestCase {
	public void testRealFile() throws Exception {
        POIDataSamples slTests = POIDataSamples.getSlideShowInstance();

		HSLFSlideShow ppt = new HSLFSlideShow(slTests.openResourceAsStream("sound.ppt"));

		// Get the document
		Document doc = ppt.getDocumentRecord();
		SoundCollection soundCollection = null;
		Record[] doc_ch = doc.getChildRecords();
		for (Record rec : doc_ch) {
			if (rec instanceof SoundCollection) {
				soundCollection = (SoundCollection) rec;
				break;
			}
		}
		if (soundCollection == null) {
			throw new AssertionFailedError("soundCollection must not be null");
		}

		Sound sound = null;
		Record[] sound_ch = soundCollection.getChildRecords();
		int k = 0;
		for (Record rec : sound_ch) {
			if (rec instanceof Sound) {
				sound = (Sound) rec;
				k++;
			}
		}
		if (sound == null) {
			throw new AssertionFailedError("sound must not be null");
		}
		assertEquals(1, k);

		assertEquals("ringin.wav", sound.getSoundName());
		assertEquals(".WAV", sound.getSoundType());
		assertNotNull(sound.getSoundData());

		byte[] ref_data = slTests.readFile("ringin.wav");
		assertArrayEquals(ref_data, sound.getSoundData());
	}
}
