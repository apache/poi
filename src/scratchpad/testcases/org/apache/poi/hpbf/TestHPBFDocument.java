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

import java.io.File;
import java.io.FileInputStream;

import junit.framework.TestCase;
import org.apache.poi.POIDataSamples;

public final class TestHPBFDocument extends TestCase {
    private static final POIDataSamples _samples = POIDataSamples.getPublisherInstance();

	public void testOpen() throws Exception {
		HPBFDocument doc = new HPBFDocument(
                _samples.openResourceAsStream("Sample.pub")
		);

		assertNotNull(doc);
	}

	public void testBits() throws Exception {
		HPBFDocument doc = new HPBFDocument(
                _samples.openResourceAsStream("Sample.pub")
		);

		assertNotNull(doc.getMainContents());
		assertNotNull(doc.getQuillContents());
		assertNotNull(doc.getEscherStm());
		assertNotNull(doc.getEscherDelayStm());

		assertTrue(doc.getMainContents().getData().length > 0);
		assertTrue(doc.getQuillContents().getData().length > 0);
		assertTrue(doc.getEscherStm().getData().length > 0);
		assertTrue(doc.getEscherDelayStm().getData().length == 0);
	}

	// TODO
//	public void testWrite() throws Exception {
//	}
}
