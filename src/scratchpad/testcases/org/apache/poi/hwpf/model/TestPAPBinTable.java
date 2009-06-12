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

import java.io.*;
import java.util.*;

public final class TestPAPBinTable
  extends TestCase
{
  private PAPBinTable _pAPBinTable = null;
  private HWPFDocFixture _hWPFDocFixture;

  private TextPieceTable fakeTPT = new TextPieceTable();

  public void testReadWrite()
    throws Exception
  {
    FileInformationBlock fib = _hWPFDocFixture._fib;
    byte[] mainStream = _hWPFDocFixture._mainStream;
    byte[] tableStream = _hWPFDocFixture._tableStream;
    int fcMin = fib.getFcMin();

    _pAPBinTable = new PAPBinTable(mainStream, tableStream, null, fib.getFcPlcfbtePapx(), fib.getLcbPlcfbtePapx(), fcMin, fakeTPT);

    HWPFFileSystem fileSys = new HWPFFileSystem();

    _pAPBinTable.writeTo(fileSys, 0);
    ByteArrayOutputStream tableOut = fileSys.getStream("1Table");
    ByteArrayOutputStream mainOut =  fileSys.getStream("WordDocument");

    byte[] newTableStream = tableOut.toByteArray();
    byte[] newMainStream = mainOut.toByteArray();

    PAPBinTable newBinTable = new PAPBinTable(newMainStream, newTableStream, null,0, newTableStream.length, 0, fakeTPT);

    ArrayList oldTextRuns = _pAPBinTable.getParagraphs();
    ArrayList newTextRuns = newBinTable.getParagraphs();

    assertEquals(oldTextRuns.size(), newTextRuns.size());

    int size = oldTextRuns.size();
    for (int x = 0; x < size; x++)
    {
     PropertyNode oldNode = (PropertyNode)oldTextRuns.get(x);
     PropertyNode newNode = (PropertyNode)newTextRuns.get(x);

     assertTrue(oldNode.equals(newNode));
    }




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
