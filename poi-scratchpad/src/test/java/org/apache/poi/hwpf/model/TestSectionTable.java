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

public final class TestSectionTable {
  private HWPFDocFixture _hWPFDocFixture;

  @Test
  void testReadWrite() throws Exception {
    FileInformationBlock fib = _hWPFDocFixture._fib;
    byte[] mainStream = _hWPFDocFixture._mainStream;
    byte[] tableStream = _hWPFDocFixture._tableStream;
    int fcMin = fib.getFibBase().getFcMin();

    ComplexFileTable cft = new ComplexFileTable(mainStream, tableStream, fib.getFcClx(), fcMin);
    TextPieceTable tpt = cft.getTextPieceTable();

    SectionTable sectionTable = new SectionTable(mainStream, tableStream,
                                                 fib.getFcPlcfsed(),
                                                 fib.getLcbPlcfsed(),
                                                 fcMin, tpt, fib.getSubdocumentTextStreamLength( SubdocumentType.MAIN ));
    HWPFFileSystem fileSys = new HWPFFileSystem();

    sectionTable.writeTo(fileSys, 0);
    ByteArrayOutputStream tableOut = fileSys.getStream("1Table");
    ByteArrayOutputStream mainOut =  fileSys.getStream("WordDocument");

    byte[] newTableStream = tableOut.toByteArray();
    byte[] newMainStream = mainOut.toByteArray();

    SectionTable newSectionTable = new SectionTable(
    		newMainStream, newTableStream, 0,
    		newTableStream.length, 0, tpt, fib.getSubdocumentTextStreamLength( SubdocumentType.MAIN ));

    List<SEPX> oldSections = sectionTable.getSections();
    List<SEPX> newSections = newSectionTable.getSections();

    assertEquals(oldSections.size(), newSections.size());

    //test for proper char offset conversions
    PlexOfCps oldSedPlex = new PlexOfCps(tableStream, fib.getFcPlcfsed(),
                                                      fib.getLcbPlcfsed(), 12);
    PlexOfCps newSedPlex = new PlexOfCps(newTableStream, 0,
                                         newTableStream.length, 12);
    assertEquals(oldSedPlex.length(), newSedPlex.length());

    for (int x = 0; x < oldSedPlex.length(); x++)
    {
      assertEquals(oldSedPlex.getProperty(x).getStart(), newSedPlex.getProperty(x).getStart());
      assertEquals(oldSedPlex.getProperty(x).getEnd(), newSedPlex.getProperty(x).getEnd());
    }

    int size = oldSections.size();
    for (int x = 0; x < size; x++)
    {
	  SEPX oldNode = oldSections.get(x);
	  SEPX newNode = newSections.get(x);
      assertEquals(oldNode, newNode);
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    /*@todo verify the constructors*/
    _hWPFDocFixture = new HWPFDocFixture(this, HWPFDocFixture.DEFAULT_TEST_FILE);

    _hWPFDocFixture.setUp();
  }

  @AfterEach
  void tearDown()  {
    _hWPFDocFixture.tearDown();

    _hWPFDocFixture = null;
  }

}
