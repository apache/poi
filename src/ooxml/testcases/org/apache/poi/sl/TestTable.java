/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.sl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.sl.usermodel.TableShape;
import org.junit.Test;

public class TestTable {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();
    
    @Test
    public void testColWidthRowHeight() throws IOException {
        // Test of table dimensions of same slideshow saved as ppt/x
        // to check if both return similar (points) value
        SlideShow<?,?> ppt = SlideShowFactory.create(_slTests.getFile("table_test.ppt"));
        TableShape<?,?> ts = (TableShape<?,?>)ppt.getSlides().get(0).getShapes().get(0);
        int cols = ts.getNumberOfColumns();
        int rows = ts.getNumberOfRows();

        SlideShow<?,?> pptx = SlideShowFactory.create(_slTests.getFile("table_test.pptx"));
        TableShape<?,?> tsx = (TableShape<?,?>)pptx.getSlides().get(0).getShapes().get(0);
        int colsx = tsx.getNumberOfColumns();
        int rowsx = tsx.getNumberOfRows();
        
        assertEquals(cols, colsx);
        assertEquals(rows, rowsx);
        
        for (int i=0; i<cols; i++) {
            assertEquals(ts.getColumnWidth(i), tsx.getColumnWidth(i), 0.2);
        }

        for (int i=0; i<rows; i++) {
            assertEquals(ts.getRowHeight(i), tsx.getRowHeight(i), 0.3);
        }

        pptx.close();
        ppt.close();
    }

}
