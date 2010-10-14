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
package org.apache.poi.hwpf.usermodel;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.model.ListFormatOverride;
import org.apache.poi.hwpf.model.ListLevel;

import junit.framework.TestCase;

public class TestBug50075 extends TestCase
{

  public void test() {
    HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug50075.doc");
    Range range = doc.getRange();
    assertEquals(1, range.numParagraphs());
    ListEntry entry = (ListEntry) range.getParagraph(0);
    ListFormatOverride override = doc.getListTables().getOverride(entry.getIlfo());
    ListLevel level = doc.getListTables().getLevel(override.getLsid(), entry.getIlvl());
    
    // the bug reproduces, if this call fails with NullPointerException
    level.getNumberText();
  }
  
}
