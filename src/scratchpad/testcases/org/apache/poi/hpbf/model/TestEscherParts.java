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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hpbf.HPBFDocument;
import org.junit.Test;

public final class TestEscherParts {
    private static final POIDataSamples _samples = POIDataSamples.getPublisherInstance();

    @Test
	public void testBasics() throws IOException {
        InputStream is = _samples.openResourceAsStream("Sample.pub");
		HPBFDocument doc = new HPBFDocument(is);
		is.close();

		EscherStm es = doc.getEscherStm();
		EscherDelayStm eds = doc.getEscherDelayStm();

		assertNotNull(es);
		assertNotNull(eds);

		assertEquals(13, es.getEscherRecords().length);
		assertEquals(0, eds.getEscherRecords().length);

		// TODO - check the contents
		doc.close();
	}

    @Test
    public void testComplex() throws Exception {
        InputStream is = _samples.openResourceAsStream("SampleBrochure.pub"); 
		HPBFDocument doc1 = new HPBFDocument(is);
		is.close();

		EscherStm es = doc1.getEscherStm();
		EscherDelayStm eds = doc1.getEscherDelayStm();

		assertNotNull(es);
		assertNotNull(eds);

		assertEquals(30, es.getEscherRecords().length);
		assertEquals(19, eds.getEscherRecords().length);

		// TODO - check contents
		doc1.close();

		// Now do another complex file
		InputStream is2 = _samples.openResourceAsStream("SampleNewsletter.pub"); 
        HPBFDocument doc2 = new HPBFDocument(is2);
		is2.close();

		es = doc2.getEscherStm();
		eds = doc2.getEscherDelayStm();

		assertNotNull(es);
		assertNotNull(eds);

		assertEquals(51, es.getEscherRecords().length);
		assertEquals(92, eds.getEscherRecords().length);
		doc2.close();
	}
}
