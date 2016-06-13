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

package org.apache.poi.ss.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests that the common RegionUtil works as we need it to.
 */
public final class TestRegionUtil {
    private static final CellRangeAddress A1C3 = new CellRangeAddress(0, 2, 0, 2);
    private static short THIN = BorderStyle.THIN.getCode();
    private Workbook wb;
    private Sheet sheet;
    
    @Before
    public void setUp() {
        wb = new HSSFWorkbook();
        sheet = wb.createSheet();
    }
    
    @After
    public void tearDown() throws IOException {
        wb.close();
    }
    
    // TODO: fill this in with meaningful unit tests
    // Right now this just makes sure that RegionUtil is compiled into poi schemas
    // and that the code doesn't run in an infinite loop.
    // Don't spend too much time getting this unit test to work as this class
    // will likely be replaced by BorderPropertyTemplate soon.
    @Test
    public void setBorderTop() {
        RegionUtil.setBorderTop(THIN, A1C3, sheet);
    }
    @Test
    public void setBorderBottom() {
        RegionUtil.setBorderBottom(THIN, A1C3, sheet);
    }
    @Test
    public void setBorderRight() {
        RegionUtil.setBorderRight(THIN, A1C3, sheet);
    }
    @Test
    public void setBorderLeft() {
        RegionUtil.setBorderLeft(THIN, A1C3, sheet);
    }
    
    @Test
    public void setTopBorderColor() {
        RegionUtil.setTopBorderColor(THIN, A1C3, sheet);
    }
    @Test
    public void setBottomBorderColor() {
        RegionUtil.setBottomBorderColor(THIN, A1C3, sheet);
    }
    @Test
    public void setRightBorderColor() {
        RegionUtil.setRightBorderColor(THIN, A1C3, sheet);
    }
    @Test
    public void setLeftBorderColor() {
        RegionUtil.setLeftBorderColor(THIN, A1C3, sheet);
    }
}
