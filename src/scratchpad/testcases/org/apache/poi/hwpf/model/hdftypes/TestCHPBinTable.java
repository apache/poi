package org.apache.poi.hwpf.model.hdftypes;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import junit.framework.*;

import org.apache.poi.hwpf.*;
import org.apache.poi.hwpf.model.io.*;

public class TestCHPBinTable
  extends TestCase
{
  private CHPBinTable _cHPBinTable = null;
  private HWPFDocFixture _hWPFDocFixture;

  public TestCHPBinTable(String name)
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

    _cHPBinTable = new CHPBinTable(mainStream, tableStream, fib.getFcPlcfbteChpx(), fib.getLcbPlcfbteChpx(), fcMin);

    HWPFFileSystem fileSys = new HWPFFileSystem();

    _cHPBinTable.writeTo(fileSys, 0);
    ByteArrayOutputStream tableOut = fileSys.getStream("1Table");
    ByteArrayOutputStream mainOut =  fileSys.getStream("WordDocument");

    byte[] newTableStream = tableOut.toByteArray();
    byte[] newMainStream = mainOut.toByteArray();

    CHPBinTable newBinTable = new CHPBinTable(newMainStream, newTableStream, 0, newTableStream.length, 0);

    ArrayList oldTextRuns = _cHPBinTable._textRuns;
    ArrayList newTextRuns = newBinTable._textRuns;

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
    _hWPFDocFixture = new HWPFDocFixture(this);

    _hWPFDocFixture.setUp();
  }

  protected void tearDown()
    throws Exception
  {
    _cHPBinTable = null;
    _hWPFDocFixture.tearDown();

    _hWPFDocFixture = null;
    super.tearDown();
  }

}
