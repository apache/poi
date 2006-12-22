
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import org.apache.poi.hwpf.*;
import org.apache.poi.hwpf.model.io.*;


public class TestTextPieceTable
  extends TestCase
{
  private HWPFDocFixture _hWPFDocFixture;

  public TestTextPieceTable(String name)
  {
    super(name);
  }

  public void testReadWrite()
    throws Exception
  {
    FileInformationBlock fib = _hWPFDocFixture._fib;
    byte[] mainStream = _hWPFDocFixture._mainStream;
    byte[] tableStream = _hWPFDocFixture._tableStream;
    int fcMin = fib.getFcMin();

    ComplexFileTable cft = new ComplexFileTable(mainStream, tableStream, fib.getFcClx(), fcMin);


    HWPFFileSystem fileSys = new HWPFFileSystem();

    cft.writeTo(fileSys);
    ByteArrayOutputStream tableOut = fileSys.getStream("1Table");
    ByteArrayOutputStream mainOut =  fileSys.getStream("WordDocument");

    byte[] newTableStream = tableOut.toByteArray();
    byte[] newMainStream = mainOut.toByteArray();

    ComplexFileTable newCft = new ComplexFileTable(newMainStream, newTableStream, 0,0);

    TextPieceTable oldTextPieceTable = cft.getTextPieceTable();
    TextPieceTable newTextPieceTable = newCft.getTextPieceTable();

    assertEquals(oldTextPieceTable, newTextPieceTable);


  }
  protected void setUp()
    throws Exception
  {
    super.setUp();

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
