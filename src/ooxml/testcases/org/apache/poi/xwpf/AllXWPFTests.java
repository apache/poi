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

package org.apache.poi.xwpf;

import org.apache.poi.xwpf.extractor.TestXWPFWordExtractor;
import org.apache.poi.xwpf.model.TestXWPFHeaderFooterPolicy;
import org.apache.poi.xwpf.usermodel.TestXWPFChart;
import org.apache.poi.xwpf.usermodel.TestXWPFDocument;
import org.apache.poi.xwpf.usermodel.TestXWPFHeader;
import org.apache.poi.xwpf.usermodel.TestXWPFHeadings;
import org.apache.poi.xwpf.usermodel.TestXWPFNumbering;
import org.apache.poi.xwpf.usermodel.TestXWPFParagraph;
import org.apache.poi.xwpf.usermodel.TestXWPFPictureData;
import org.apache.poi.xwpf.usermodel.TestXWPFRun;
import org.apache.poi.xwpf.usermodel.TestXWPFStyles;
import org.apache.poi.xwpf.usermodel.TestXWPFTable;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Collects all tests for <tt>org.apache.poi.xwpf</tt> and sub-packages.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestXWPFBugs.class,
        org.apache.poi.xwpf.usermodel.TestXWPFBugs.class,
        TestXWPFChart.class,
        TestXWPFDocument.class,
        TestXWPFWordExtractor.class,
        TestXWPFHeaderFooterPolicy.class,
        TestXWPFHeader.class,
        TestXWPFHeadings.class,
        TestXWPFParagraph.class,
        TestXWPFRun.class,
        TestXWPFTable.class,
        TestXWPFStyles.class,
        TestXWPFPictureData.class,
        TestXWPFNumbering.class,
        TestAllExtendedProperties.class,
        TestPackageCorePropertiesGetKeywords.class
})
public final class AllXWPFTests {
}
