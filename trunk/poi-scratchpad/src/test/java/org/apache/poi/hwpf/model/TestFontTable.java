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

package org.apache.poi.hwpf.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.hwpf.HWPFDocFixture;
import org.apache.poi.hwpf.model.io.HWPFFileSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class TestFontTable {
  private FontTable _fontTable;
  private HWPFDocFixture _hWPFDocFixture;

  @Test
  void testReadWrite() throws IOException {
    FileInformationBlock fib = _hWPFDocFixture._fib;
    byte[] tableStream = _hWPFDocFixture._tableStream;

    int fcSttbfffn = fib.getFcSttbfffn();
    int lcbSttbfffn = fib.getLcbSttbfffn();

    _fontTable = new FontTable(tableStream, fcSttbfffn, lcbSttbfffn);

    HWPFFileSystem fileSys = new HWPFFileSystem();

    _fontTable.writeTo(fileSys);
    ByteArrayOutputStream  tableOut = fileSys.getStream("1Table");


    byte[] newTableStream = tableOut.toByteArray();


    FontTable newFontTable = new FontTable(newTableStream, 0, newTableStream.length);

      assertEquals(_fontTable, newFontTable);

  }

  @BeforeEach
  void setUp() throws IOException {
    _hWPFDocFixture = new HWPFDocFixture(this, HWPFDocFixture.DEFAULT_TEST_FILE);
    _hWPFDocFixture.setUp();
  }

  @AfterEach
  void tearDown() throws IOException  {
    _hWPFDocFixture.tearDown();
  }

}

