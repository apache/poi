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
import java.util.List;

import org.apache.poi.hwpf.HWPFDocFixture;
import org.apache.poi.hwpf.model.io.HWPFFileSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class TestCHPBinTable {
  private CHPBinTable _cHPBinTable;
  private HWPFDocFixture _hWPFDocFixture;

  private final TextPieceTable fakeTPT = new TextPieceTable() {
      @Override
      public boolean isIndexInTable(int bytePos) {
          return true;
      }
  };

  @Test
  void testReadWrite() throws Exception {
    FileInformationBlock fib = _hWPFDocFixture._fib;
    byte[] mainStream = _hWPFDocFixture._mainStream;
    byte[] tableStream = _hWPFDocFixture._tableStream;
    int fcMin = fib.getFibBase().getFcMin();

    _cHPBinTable = new CHPBinTable(mainStream, tableStream, fib.getFcPlcfbteChpx(), fib.getLcbPlcfbteChpx(), fakeTPT);

    HWPFFileSystem fileSys = new HWPFFileSystem();

    _cHPBinTable.writeTo(fileSys, 0, fakeTPT);
    ByteArrayOutputStream tableOut = fileSys.getStream("1Table");
    ByteArrayOutputStream mainOut =  fileSys.getStream("WordDocument");

    byte[] newTableStream = tableOut.toByteArray();
    byte[] newMainStream = mainOut.toByteArray();

    CHPBinTable newBinTable = new CHPBinTable(newMainStream, newTableStream, 0, newTableStream.length, fakeTPT);

    List<CHPX> oldTextRuns = _cHPBinTable._textRuns;
    List<CHPX> newTextRuns = newBinTable._textRuns;

    assertEquals(oldTextRuns.size(), newTextRuns.size());

    int size = oldTextRuns.size();
    for (int x = 0; x < size; x++)
    {
      CHPX oldNode = oldTextRuns.get(x);
      CHPX newNode = newTextRuns.get(x);
        assertEquals(oldNode, newNode);
    }

  }
  @BeforeEach
  void setUp() throws Exception {
    _hWPFDocFixture = new HWPFDocFixture(this, HWPFDocFixture.DEFAULT_TEST_FILE);

    _hWPFDocFixture.setUp();
  }

  @AfterEach
  void tearDown() throws Exception {
    _cHPBinTable = null;
    _hWPFDocFixture.tearDown();

    _hWPFDocFixture = null;
  }

}
