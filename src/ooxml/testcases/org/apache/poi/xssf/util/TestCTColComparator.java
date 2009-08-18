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

package org.apache.poi.xssf.util;

import java.util.Arrays;

import junit.framework.TestCase;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCol;


public final class TestCTColComparator extends TestCase {

    public void testCompare() {
        CTColComparator comparator = new CTColComparator();
        CTCol o1 = CTCol.Factory.newInstance();
        o1.setMin(1);
        o1.setMax(10);
        CTCol o2 = CTCol.Factory.newInstance();
        o2.setMin(11);
        o2.setMax(12);
        assertEquals(-1, comparator.compare(o1, o2));
        CTCol o3 = CTCol.Factory.newInstance();
        o3.setMin(5);
        o3.setMax(8);
        CTCol o4 = CTCol.Factory.newInstance();
        o4.setMin(5);
        o4.setMax(80);
        assertEquals(-1, comparator.compare(o3, o4));
    }

    public void testArraysSort() {
        CTColComparator comparator = new CTColComparator();
        CTCol o1 = CTCol.Factory.newInstance();
        o1.setMin(1);
        o1.setMax(10);
        CTCol o2 = CTCol.Factory.newInstance();
        o2.setMin(11);
        o2.setMax(12);
        assertEquals(-1, comparator.compare(o1, o2));
        CTCol o3 = CTCol.Factory.newInstance();
        o3.setMin(5);
        o3.setMax(80);
        CTCol o4 = CTCol.Factory.newInstance();
        o4.setMin(5);
        o4.setMax(8);
        assertEquals(1, comparator.compare(o3, o4));
        CTCol[] cols = new CTCol[4];
        cols[0] = o1;
        cols[1] = o2;
        cols[2] = o3;
        cols[3] = o4;
        assertEquals(80, cols[2].getMax());
        assertEquals(8, cols[3].getMax());
        Arrays.sort(cols, comparator);
        assertEquals(12, cols[3].getMax());
        assertEquals(8, cols[1].getMax());
        assertEquals(80, cols[2].getMax());
    }
}
