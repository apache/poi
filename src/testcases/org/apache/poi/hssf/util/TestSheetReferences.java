
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
        

package org.apache.poi.hssf.util;

import junit.framework.TestCase;

/**
 * Tests the SheetReferences class.
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
public class TestSheetReferences
        extends TestCase
{
    public TestSheetReferences(String s)
    {
        super(s);
    }

    /**
     * Test that the SheetReferences class can add references and give them
     * out
     */
    public void testSheetRefs()
            throws Exception
    {
        SheetReferences refs = new SheetReferences();
        refs.addSheetReference("A", 0);
        refs.addSheetReference("B", 1);
        refs.addSheetReference("C", 3);
        assertTrue("ref 0 == A", refs.getSheetName(0).equals("A"));
        assertTrue("ref 1 == B", refs.getSheetName(1).equals("B"));
        assertTrue("ref 3 == C", refs.getSheetName(3).equals("C"));
    }
}
