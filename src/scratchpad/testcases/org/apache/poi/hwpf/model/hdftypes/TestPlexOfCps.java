package org.apache.poi.hwpf.model.hdftypes;

import junit.framework.*;
import org.apache.poi.hwpf.*;

import org.apache.poi.util.LittleEndian;

public class TestPlexOfCps
  extends TestCase
{
  private PlexOfCps _plexOfCps = null;
  private HWPFDocFixture _hWPFDocFixture;

  public TestPlexOfCps(String name)
  {
    super(name);
  }
  public void testWriteRead()
    throws Exception
  {
    _plexOfCps = new PlexOfCps(4);

    int last = 0;
    for (int x = 0; x < 110; x++)
    {
      byte[] intHolder = new byte[4];
      int span = (int)(110.0f * Math.random());
      LittleEndian.putInt(intHolder, span);
      _plexOfCps.addProperty(new PropertyNode(last, last + span, intHolder));
      last += span;
    }

    byte[] output = _plexOfCps.toByteArray();
    _plexOfCps = new PlexOfCps(output, 0, output.length, 4);
    int len = _plexOfCps.length();
    assertEquals(len, 110);

    last = 0;
    for (int x = 0; x < len; x++)
    {
      PropertyNode node = _plexOfCps.getProperty(x);
      assertEquals(node.getStart(), last);
      last = node.getEnd();
      int span = LittleEndian.getInt(node.getBuf());
      assertEquals(node.getEnd()-node.getStart(), span);
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
    _plexOfCps = null;
    _hWPFDocFixture.tearDown();

    _hWPFDocFixture = null;
    super.tearDown();
  }

}
