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

import org.apache.poi.hwpf.*;
import org.apache.poi.hwpf.model.io.*;


public final class TestListTables
  extends HWPFTestCase
{

  public TestListTables()
  {
  }

  public void testReadWrite()
    throws Exception
  {
    FileInformationBlock fib = _hWPFDocFixture._fib;
    byte[] tableStream = _hWPFDocFixture._tableStream;

    int listOffset = fib.getFcPlcfLst();
    int lfoOffset = fib.getFcPlfLfo();
    if (listOffset != 0 && fib.getLcbPlcfLst() != 0)
    {
      ListTables listTables = new ListTables (tableStream, fib.getFcPlcfLst (),
                                              fib.getFcPlfLfo ());
      HWPFFileSystem fileSys = new HWPFFileSystem ();

      HWPFOutputStream tableOut = fileSys.getStream ("1Table");

      listTables.writeListDataTo (tableOut);
      int offset = tableOut.getOffset ();
      listTables.writeListOverridesTo (tableOut);

      ListTables newTables = new ListTables (tableOut.toByteArray (), 0, offset);

      assertEquals(listTables, newTables);

    }
  }

}
