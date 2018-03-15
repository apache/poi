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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.poi.hwpf.HWPFDocFixture;
import org.apache.poi.hwpf.model.io.HWPFFileSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public final class TestStyleSheet {
  private StyleSheet _styleSheet;
  private HWPFDocFixture _hWPFDocFixture;

  @Test
  public void testReadWrite() throws IOException
  {
    HWPFFileSystem fileSys = new HWPFFileSystem();


    ByteArrayOutputStream tableOut = fileSys.getStream("1Table");
    ByteArrayOutputStream mainOut =  fileSys.getStream("WordDocument");

    _styleSheet.writeTo(tableOut);

    byte[] newTableStream = tableOut.toByteArray();

    StyleSheet newStyleSheet = new StyleSheet(newTableStream, 0);
    assertEquals(newStyleSheet, _styleSheet);
  }

  @Test
  public void testReadWriteFromNonZeroOffset() throws IOException
  {
    HWPFFileSystem fileSys = new HWPFFileSystem();
    ByteArrayOutputStream tableOut = fileSys.getStream("1Table");

    tableOut.write(new byte[20]); // 20 bytes of whatever at the front.
    _styleSheet.writeTo(tableOut);

    byte[] newTableStream = tableOut.toByteArray();

    StyleSheet newStyleSheet = new StyleSheet(newTableStream, 20);
    assertEquals(newStyleSheet, _styleSheet);
  }

  @Before
  public void setUp() throws IOException {
    /**@todo verify the constructors*/
    _hWPFDocFixture = new HWPFDocFixture(this, HWPFDocFixture.DEFAULT_TEST_FILE);
    _hWPFDocFixture.setUp();
    FileInformationBlock fib = _hWPFDocFixture._fib;
    byte[] mainStream = _hWPFDocFixture._mainStream;
    byte[] tableStream = _hWPFDocFixture._tableStream;

    _hWPFDocFixture.setUp();
    _styleSheet = new StyleSheet(tableStream, fib.getFcStshf());
  }

  @After
  public void tearDown() throws Exception {
    _styleSheet = null;
    _hWPFDocFixture.tearDown();

    _hWPFDocFixture = null;
  }
}
