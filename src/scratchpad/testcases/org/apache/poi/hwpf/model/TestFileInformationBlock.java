package org.apache.poi.hwpf.model;

import junit.framework.*;
import org.apache.poi.hwpf.*;

import java.lang.reflect.*;

public class TestFileInformationBlock
  extends TestCase
{
  private FileInformationBlock _fileInformationBlock = null;
  private HWPFDocFixture _hWPFDocFixture;

  public TestFileInformationBlock(String name)
  {
    super(name);
  }

  public void testReadWrite()
    throws Exception
  {
    int size = _fileInformationBlock.getSize();
    byte[] buf = new byte[size];

    _fileInformationBlock.serialize(buf, 0);

    FileInformationBlock newFileInformationBlock =
      new FileInformationBlock(buf);

    Field[] fields = FileInformationBlock.class.getSuperclass().getDeclaredFields();
    AccessibleObject.setAccessible(fields, true);

    for (int x = 0; x < fields.length; x++)
    {
      assertEquals(fields[x].get(_fileInformationBlock), fields[x].get(newFileInformationBlock));
    }
  }

  protected void setUp()
    throws Exception
  {
    super.setUp();
    /**@todo verify the constructors*/
    _hWPFDocFixture = new HWPFDocFixture(this);

    _hWPFDocFixture.setUp();
    _fileInformationBlock = _hWPFDocFixture._fib;
  }

  protected void tearDown()
    throws Exception
  {
    _fileInformationBlock = null;
    _hWPFDocFixture.tearDown();

    _hWPFDocFixture = null;
    super.tearDown();
  }

}
