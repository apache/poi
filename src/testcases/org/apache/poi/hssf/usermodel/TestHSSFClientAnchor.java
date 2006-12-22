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

package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;

/**
 * Various tests for HSSFClientAnchor.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class TestHSSFClientAnchor extends TestCase
{
    public void testGetAnchorHeightInPoints() throws Exception
    {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("test");
        HSSFClientAnchor a = new HSSFClientAnchor(0,0,1023,255,(short)0,0,(short)0,0);
        float p = a.getAnchorHeightInPoints(sheet);
        assertEquals(11.953,p,0.001);

        sheet.createRow(0).setHeightInPoints(14);
        a = new HSSFClientAnchor(0,0,1023,255,(short)0,0,(short)0,0);
        p = a.getAnchorHeightInPoints(sheet);
        assertEquals(13.945,p,0.001);

        a = new HSSFClientAnchor(0,0,1023,127,(short)0,0,(short)0,0);
        p = a.getAnchorHeightInPoints(sheet);
        assertEquals(6.945,p,0.001);

        a = new HSSFClientAnchor(0,126,1023,127,(short)0,0,(short)0,0);
        p = a.getAnchorHeightInPoints(sheet);
        assertEquals(0.054,p,0.001);

        a = new HSSFClientAnchor(0,0,1023,0,(short)0,0,(short)0,1);
        p = a.getAnchorHeightInPoints(sheet);
        assertEquals(14.0,p,0.001);

        sheet.createRow(0).setHeightInPoints(12);
        a = new HSSFClientAnchor(0,127,1023,127,(short)0,0,(short)0,1);
        p = a.getAnchorHeightInPoints(sheet);
        assertEquals(12.0,p,0.001);

    }

}
