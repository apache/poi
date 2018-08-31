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

package org.apache.poi.hwpf.extractor;

import org.apache.poi.POIDataSamples;
import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.extractor.OLE2ExtractorFactory;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for bugs with the WordExtractor
 */
public final class TestWordExtractorBugs {
    private static final POIDataSamples SAMPLES = POIDataSamples.getDocumentInstance();

    @Test
    public void testProblemMetadata() throws IOException {
        InputStream is = SAMPLES.openResourceAsStream("ProblemExtracting.doc");
		WordExtractor extractor = new WordExtractor(is);
		is.close();

		// Check it gives text without error
		extractor.getText();
		extractor.getParagraphText();
		extractor.getTextFromPieces();
		extractor.close();
	}

    @Test
    public void testBug50688() throws Exception {
        InputStream is = SAMPLES.openResourceAsStream("parentinvguid.doc");
        WordExtractor extractor = new WordExtractor(is);
        is.close();

        // Check it gives text without error
        extractor.getText();
        extractor.close();
    }

    @Test
    public void testBug60374() throws Exception {
        POIFSFileSystem fs = new POIFSFileSystem(SAMPLES.openResourceAsStream("cn.orthodox.www_divenbog_APRIL_30-APRIL.DOC"));
        final POITextExtractor extractor = OLE2ExtractorFactory.createExtractor(fs);

        // Check it gives text without error
        assertNotNull(extractor.getText());

        extractor.close();
    }
}
