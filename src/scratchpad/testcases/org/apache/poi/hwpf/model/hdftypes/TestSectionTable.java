package org.apache.poi.hwpf.model.hdftypes;

import junit.framework.*;
import org.apache.poi.hwpf.*;

public class TestSectionTable
  extends TestCase
{
  private SectionTable _sectionTable = null;
  private HWPFDocFixture _hWPFDocFixture;

  public TestSectionTable(String name)
  {
    super(name);
  }

  protected void setUp()
    throws Exception
  {
    super.setUp();
    /**@todo verify the constructors*/
    _sectionTable = new SectionTable(null, null, 0, 0, 0);
    _hWPFDocFixture = new HWPFDocFixture(this);

    _hWPFDocFixture.setUp();
  }

  protected void tearDown()
    throws Exception
  {
    _sectionTable = null;
    _hWPFDocFixture.tearDown();

    _hWPFDocFixture = null;
    super.tearDown();
  }

}
