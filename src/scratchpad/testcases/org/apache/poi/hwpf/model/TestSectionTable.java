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

public final class TestSectionTable
  extends TestCase
{
  private HWPFDocFixture _hWPFDocFixture;

  public void testReadWrite()
    throws Exception
  {
    FileInformationBlock fib = _hWPFDocFixture._fib;
    byte[] mainStream = _hWPFDocFixture._mainStream;
    byte[] tableStream = _hWPFDocFixture._tableStream;
    int fcMin = fib.getFcMin();

    CPSplitCalculator cps = new CPSplitCalculator(fib);

    ComplexFileTable cft = new ComplexFileTable(mainStream, tableStream, fib.getFcClx(), fcMin);
    TextPieceTable tpt = cft.getTextPieceTable();

    SectionTable sectionTable = new SectionTable(mainStream, tableStream,
                                                 fib.getFcPlcfsed(),
                                                 fib.getLcbPlcfsed(),
                                                 fcMin, tpt, cps);
    HWPFFileSystem fileSys = new HWPFFileSystem();

    sectionTable.writeTo(fileSys, 0);
    ByteArrayOutputStream tableOut = fileSys.getStream("1Table");
    ByteArrayOutputStream mainOut =  fileSys.getStream("WordDocument");

    byte[] newTableStream = tableOut.toByteArray();
    byte[] newMainStream = mainOut.toByteArray();

    SectionTable newSectionTable = new SectionTable(
    		newMainStream, newTableStream, 0,
    		newTableStream.length, 0, tpt, cps);

    ArrayList oldSections = sectionTable.getSections();
    ArrayList newSections = newSectionTable.getSections();

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
      PropertyNode oldNode = (PropertyNode)oldSections.get(x);
      PropertyNode newNode = (PropertyNode)newSections.get(x);
      assertEquals(oldNode, newNode);
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
