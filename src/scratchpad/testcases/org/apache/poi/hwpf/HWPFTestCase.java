
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        
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
