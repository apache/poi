package org.apache.poi.hwpf;

import java.io.FileInputStream;

import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.filesystem.DocumentEntry;

import org.apache.poi.hwpf.model.hdftypes.*;


public class HWPFDocFixture
{
  public byte[] _tableStream;
  public byte[] _mainStream;
  public FileInformationBlock _fib;

  public HWPFDocFixture(Object obj)
  {

  }

  public void setUp()
  {
    try
    {

      String filename = System.getProperty("HSSF.testdata.path");

      filename = filename + "/test.doc";


      POIFSFileSystem filesystem = new POIFSFileSystem(new FileInputStream(
        "C:\\test.doc"));

      DocumentEntry documentProps =
        (DocumentEntry) filesystem.getRoot().getEntry("WordDocument");
      _mainStream = new byte[documentProps.getSize()];
      filesystem.createDocumentInputStream("WordDocument").read(_mainStream);

      // use the fib to determine the name of the table stream.
      _fib = new FileInformationBlock(_mainStream);

      String name = "0Table";
      if (_fib.isFWhichTblStm())
      {
        name = "1Table";
      }

      // read in the table stream.
      DocumentEntry tableProps =
        (DocumentEntry) filesystem.getRoot().getEntry(name);
      _tableStream = new byte[tableProps.getSize()];
      filesystem.createDocumentInputStream(name).read(_tableStream);
    }
    catch (Throwable t)
    {
      t.printStackTrace();
    }
  }

  public void tearDown()
  {
  }

}
