package org.apache.poi.hwpf.model;

import junit.framework.*;
import org.apache.poi.hwpf.*;
import org.apache.poi.hwpf.model.io.*;

import java.io.*;
import java.util.*;


public class TestListTables
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

    ListTables listTables = new ListTables(tableStream, fib.getFcPlcfLst(), fib.getFcPlfLfo());

    HWPFFileSystem fileSys = new HWPFFileSystem();

    HWPFOutputStream  tableOut = fileSys.getStream("1Table");

    listTables.writeListDataTo(tableOut);
    int offset = tableOut.getOffset();
    listTables.writeListOverridesTo(tableOut);

    ListTables newTables = new ListTables(tableOut.toByteArray(), 0, offset);

    assertEquals(listTables, newTables);
  }

}
