package org.apache.poi.hwpf.model.hdftypes;

import junit.framework.*;
import org.apache.poi.hwpf.*;

import java.lang.reflect.*;
import java.util.Arrays;

public class TestDocumentProperties
  extends TestCase
{
  private DocumentProperties _documentProperties = null;
  private HWPFDocFixture _hWPFDocFixture;

  public TestDocumentProperties(String name)
  {
    super(name);
  }


  public void testReadWrite()
    throws Exception
  {
    int size = _documentProperties.getSize();
    byte[] buf = new byte[size];

    _documentProperties.serialize(buf, 0);

    DocumentProperties newDocProperties =
      new DocumentProperties(buf, 0);

    Field[] fields = DocumentProperties.class.getSuperclass().getDeclaredFields();
    AccessibleObject.setAccessible(fields, true);

    for (int x = 0; x < fields.length; x++)
    {
      if (!fields[x].getType().isArray())
      {
        assertEquals(fields[x].get(_documentProperties),
                     fields[x].get(newDocProperties));
      }
      else
      {
        byte[] buf1 = (byte[])fields[x].get(_documentProperties);
        byte[] buf2 = (byte[])fields[x].get(newDocProperties);
        Arrays.equals(buf1, buf2);
      }
    }

  }

  protected void setUp()
    throws Exception
  {
    super.setUp();
    /**@todo verify the constructors*/

    _hWPFDocFixture = new HWPFDocFixture(this);

    _hWPFDocFixture.setUp();

    _documentProperties = new DocumentProperties(_hWPFDocFixture._tableStream, _hWPFDocFixture._fib.getFcDop());
  }

  protected void tearDown()
    throws Exception
  {
    _documentProperties = null;
    _hWPFDocFixture.tearDown();

    _hWPFDocFixture = null;
    super.tearDown();
  }

}
