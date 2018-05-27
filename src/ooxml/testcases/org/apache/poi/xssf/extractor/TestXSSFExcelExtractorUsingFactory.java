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

package org.apache.poi.xssf.extractor;

import org.apache.poi.ooxml.extractor.ExtractorFactory;
import org.apache.poi.hssf.HSSFTestDataSamples;

/**
 * Tests for {@link XSSFExcelExtractor}
 */
public final class TestXSSFExcelExtractorUsingFactory extends TestXSSFExcelExtractor {
	@Override
	protected final XSSFExcelExtractor getExtractor(String sampleName) {
		ExtractorFactory.setAllThreadsPreferEventExtractors(false);
		ExtractorFactory.setThreadPrefersEventExtractors(false);
		try {
			return (XSSFExcelExtractor) ExtractorFactory.createExtractor(HSSFTestDataSamples.openSampleFileStream(sampleName));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    
    @Override
    public void tearDown() {
        // reset setting to not affect other tests
        ExtractorFactory.setAllThreadsPreferEventExtractors(null);
    }
}
