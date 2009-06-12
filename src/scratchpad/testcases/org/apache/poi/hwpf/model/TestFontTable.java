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

public final class TestFontTable
  extends TestCase
{
  private FontTable _fontTable = null;
  private HWPFDocFixture _hWPFDocFixture;

  public void testReadWrite()
    throws Exception
  {
    FileInformationBlock fib = _hWPFDocFixture._fib;
    byte[] tableStream = _hWPFDocFixture._tableStream;

    int fcSttbfffn = fib.getFcSttbfffn();
    int lcbSttbfffn = fib.getLcbSttbfffn();

    _fontTable = new FontTable(tableStream, fcSttbfffn, lcbSttbfffn);

    HWPFFileSystem fileSys = new HWPFFileSystem();

    _fontTable.writeTo(fileSys);
    HWPFOutputStream  tableOut = fileSys.getStream("1Table");


    byte[] newTableStream = tableOut.toByteArray();


    FontTable newFontTable = new FontTable(newTableStream, 0, newTableStream.length);

	  assertTrue(_fontTable.equals(newFontTable));

  }

  protected void setUp()
    throws Exception
  {
    super.setUp();
    /**@todo verify the constructors*/
    _hWPFDocFixture = new HWPFDocFixture(this);

    _hWPFDocFixture.setUp();
  }

  protected void tearDown()
    throws Exception
  {
    _hWPFDocFixture.tearDown();

    _hWPFDocFixture = null;
    super.tearDown();
  }

}

