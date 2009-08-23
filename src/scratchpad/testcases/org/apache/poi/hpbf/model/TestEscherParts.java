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

package org.apache.poi.hpbf.model;

import java.io.File;
import java.io.FileInputStream;

import org.apache.poi.hpbf.HPBFDocument;
import org.apache.poi.POIDataSamples;

import junit.framework.TestCase;

public final class TestEscherParts extends TestCase {
    private static final POIDataSamples _samples = POIDataSamples.getPublisherInstance();

	public void testBasics() throws Exception {
		HPBFDocument doc = new HPBFDocument(
		    _samples.openResourceAsStream("Sample.pub")
		);

		EscherStm es = doc.getEscherStm();
		EscherDelayStm eds = doc.getEscherDelayStm();

		assertNotNull(es);
		assertNotNull(eds);

		assertEquals(13, es.getEscherRecords().length);
		assertEquals(0, eds.getEscherRecords().length);

		// TODO - check the contents
	}

	public void testComplex() throws Exception {
		HPBFDocument doc = new HPBFDocument(
                _samples.openResourceAsStream("SampleBrochure.pub")
		);

		EscherStm es = doc.getEscherStm();
		EscherDelayStm eds = doc.getEscherDelayStm();

		assertNotNull(es);
		assertNotNull(eds);

		assertEquals(30, es.getEscherRecords().length);
		assertEquals(19, eds.getEscherRecords().length);

		// TODO - check contents


		// Now do another complex file
		doc = new HPBFDocument(
                _samples.openResourceAsStream("SampleNewsletter.pub")
		);

		es = doc.getEscherStm();
		eds = doc.getEscherDelayStm();

		assertNotNull(es);
		assertNotNull(eds);

		assertEquals(51, es.getEscherRecords().length);
		assertEquals(92, eds.getEscherRecords().length);
	}
}
