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

import junit.framework.*;

import org.apache.poi.hwpf.*;
import org.apache.poi.hwpf.model.io.*;

public final class TestStyleSheet
  extends TestCase
{
  private StyleSheet _styleSheet = null;
  private HWPFDocFixture _hWPFDocFixture;

  public void testReadWrite()
    throws Exception
  {
    HWPFFileSystem fileSys = new HWPFFileSystem();


    HWPFOutputStream tableOut = fileSys.getStream("1Table");
    HWPFOutputStream mainOut =  fileSys.getStream("WordDocument");

    _styleSheet.writeTo(tableOut);

    byte[] newTableStream = tableOut.toByteArray();

    StyleSheet newStyleSheet = new StyleSheet(newTableStream, 0);
    assertEquals(newStyleSheet, _styleSheet);

  }

  protected void setUp()
    throws Exception
  {
    super.setUp();
    /**@todo verify the constructors*/
    _hWPFDocFixture = new HWPFDocFixture(this);
    _hWPFDocFixture.setUp();
    FileInformationBlock fib = _hWPFDocFixture._fib;
    byte[] mainStream = _hWPFDocFixture._mainStream;
    byte[] tableStream = _hWPFDocFixture._tableStream;

    _hWPFDocFixture.setUp();
    _styleSheet = new StyleSheet(tableStream, fib.getFcStshf());
  }

  protected void tearDown()
    throws Exception
  {
    _styleSheet = null;
    _hWPFDocFixture.tearDown();

    _hWPFDocFixture = null;
    super.tearDown();
  }

}
