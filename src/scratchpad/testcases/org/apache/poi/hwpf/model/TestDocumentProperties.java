/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hwpf.model;

import junit.framework.*;
import org.apache.poi.hwpf.*;

import java.lang.reflect.*;
import java.util.Arrays;

public final class TestDocumentProperties
  extends TestCase
{
  private DocumentProperties _documentProperties = null;
  private HWPFDocFixture _hWPFDocFixture;

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
