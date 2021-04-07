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

package org.apache.poi.hpbf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.junit.jupiter.api.Test;

public final class TestHPBFDocument {
    private static final POIDataSamples _samples = POIDataSamples.getPublisherInstance();

    @Test
	void testOpen() throws IOException {
	    InputStream is = _samples.openResourceAsStream("Sample.pub");
		HPBFDocument doc = new HPBFDocument(is);
		is.close();
		assertNotNull(doc);
		doc.close();
	}

    @Test
	void testBits() throws IOException {
        InputStream is = _samples.openResourceAsStream("Sample.pub");
		HPBFDocument doc = new HPBFDocument(is);
        is.close();

		assertNotNull(doc.getMainContents());
		assertNotNull(doc.getQuillContents());
		assertNotNull(doc.getEscherStm());
		assertNotNull(doc.getEscherDelayStm());

		assertTrue(doc.getMainContents().getData().length > 0);
		assertTrue(doc.getQuillContents().getData().length > 0);
		assertTrue(doc.getEscherStm().getData().length > 0);
        assertEquals(0, doc.getEscherDelayStm().getData().length);

       doc.close();
	}

	// TODO
//	void testWrite() throws Exception {
//	}
}
