package org.apache.poi.hwpf.model.hdftypes;

import junit.framework.*;
import org.apache.poi.hwpf.*;
import org.apache.poi.hwpf.model.io.*;

import java.io.*;
import java.util.*;

public class TestPAPBinTable
  extends TestCase
{
  private PAPBinTable _pAPBinTable = null;
  private HWPFDocFixture _hWPFDocFixture;

  public TestPAPBinTable(String name)
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

    _pAPBinTable = new PAPBinTable(mainStream, tableStream, fib.getFcPlcfbtePapx(), fib.getLcbPlcfbtePapx(), fcMin);

    HWPFFileSystem fileSys = new HWPFFileSystem();

    _pAPBinTable.writeTo(fileSys, 0);
    ByteArrayOutputStream tableOut = fileSys.getStream("1Table");
    ByteArrayOutputStream mainOut =  fileSys.getStream("WordDocument");

    byte[] newTableStream = tableOut.toByteArray();
    byte[] newMainStream = mainOut.toByteArray();

    PAPBinTable newBinTable = new PAPBinTable(newMainStream, newTableStream, 0, newTableStream.length, 0);

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
