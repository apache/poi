package org.apache.poi.hwpf;

import junit.framework.*;
import org.apache.poi.hwpf.*;
import org.apache.poi.hwpf.model.io.*;

import java.io.*;
import java.util.*;


public class HWPFTestCase
  extends TestCase
{
  protected HWPFDocFixture _hWPFDocFixture;

  public HWPFTestCase()
  {
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
