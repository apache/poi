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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.model.LFO;
import org.apache.poi.hwpf.model.ListLevel;
import org.junit.jupiter.api.Test;

public class TestBug50075 {

  @Test
  void test() throws IOException {
    try (HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug50075.doc")) {
      Range range = doc.getRange();
      assertEquals(1, range.numParagraphs());
      ListEntry entry = (ListEntry) range.getParagraph(0);
      LFO override = doc.getListTables().getLfo(entry.getIlfo());
      ListLevel level = doc.getListTables().getLevel(override.getLsid(), entry.getIlvl());
      assertNotNull(level);
      // the bug reproduces, if this call fails with NullPointerException
      assertNotNull(level.getNumberText());
    }
  }

}
