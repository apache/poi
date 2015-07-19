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

package org.apache.poi.hdf.extractor;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.junit.Test;


public class TestWordDocument {
    @SuppressWarnings("deprecation")
    @Test
    public void testMain() {
        // fails, but exception is caught and only printed
        //WordDocument.main(new String[] {});
        
        //WordDocument.main(new String[] {"test-data/document/Word95.doc", "/tmp/test.doc"});
        //WordDocument.main(new String[] {"test-data/document/Word6.doc", "/tmp/test.doc"});
        WordDocument.main(new String[] {"test-data/document/53446.doc", "/tmp/test.doc"});
    }

    @SuppressWarnings("deprecation")
	@Test
    public void test47304() throws IOException {
    	HWPFDocument doc = HWPFTestDataSamples.openSampleFile("47304.doc");
    	assertNotNull(doc);
    	
    	WordExtractor extractor = new WordExtractor(doc);
        String text = extractor.getText();
        //System.out.println(text);
        assertTrue("Had: " + text, text.contains("Just  a \u201Ctest\u201D"));
        extractor.close();
        
		WordDocument wordDoc = new WordDocument("test-data/document/47304.doc");
        
        StringWriter docTextWriter = new StringWriter();
        PrintWriter out = new PrintWriter(docTextWriter);
        try {
        	wordDoc.writeAllText(out);
        } finally {
        	out.close();
        }
        docTextWriter.close();

        //System.out.println(docTextWriter.toString());
        assertTrue("Had: " + docTextWriter.toString(), docTextWriter.toString().contains("Just  a \u201Ctest\u201D"));
    }
}
