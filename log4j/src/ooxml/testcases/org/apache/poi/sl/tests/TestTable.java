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

package org.apache.poi.sl.tests;

import static org.apache.poi.sl.tests.SLCommonUtils.openSampleSlideshow;
import static org.apache.poi.sl.tests.SLCommonUtils.xslfOnly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.sl.usermodel.TableCell;
import org.apache.poi.sl.usermodel.TableShape;
import org.apache.poi.sl.usermodel.TextShape.TextDirection;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.junit.jupiter.api.Test;

class TestTable {

    @Test
    void colWidthRowHeight() throws IOException {
        assumeFalse(xslfOnly());

        // Test of table dimensions of same slideshow saved as ppt/x
        // to check if both return similar (points) value
        SlideShow<?,?> ppt = openSampleSlideshow("table_test.ppt");
        TableShape<?,?> ts = (TableShape<?,?>)ppt.getSlides().get(0).getShapes().get(0);

        SlideShow<?,?> pptx = openSampleSlideshow("table_test.pptx");
        TableShape<?,?> tsx = (TableShape<?,?>)pptx.getSlides().get(0).getShapes().get(0);

        // assume table shape should be equal to itself
        confirmTableShapeEqual(ts, ts);
        confirmTableShapeEqual(tsx, tsx);

        // assert ppt and pptx versions of the same table have the same shape
        confirmTableShapeEqual(ts, tsx);

        // change row height and validate again
        tsx.setRowHeight(1, 50);
        ts.setRowHeight(1, 50);
        confirmTableShapeEqual(ts, tsx);

        pptx.close();
        ppt.close();
    }

    private void confirmTableShapeEqual(TableShape<?,?> tableA, TableShape<?,?> tableB) {
        int cols = tableA.getNumberOfColumns();
        int rows = tableA.getNumberOfRows();

        int colsx = tableB.getNumberOfColumns();
        int rowsx = tableB.getNumberOfRows();

        assertEquals(cols, colsx, "tables should have same number of columns");
        assertEquals(rows, rowsx, "tables should have same number of rows");

        for (int i=0; i<cols; i++) {
            assertEquals(tableA.getColumnWidth(i), tableB.getColumnWidth(i), 0.2, "Width of column " + i + " should be equal");
        }

        for (int i=0; i<rows; i++) {
            assertEquals(tableA.getRowHeight(i), tableB.getRowHeight(i), 0.3, "Height of row " + i + " should be equal");
        }
    }

    @Test
    void directionHSLF() throws IOException {
        assumeFalse(xslfOnly());
        SlideShow<?,?> ppt1 = SlideShowFactory.create(false);
        testTextDirection(ppt1);
        ppt1.close();
    }

    @Test
    void directionXSLF() throws IOException {
        SlideShow<?,?> ppt1 = new XMLSlideShow();
        testTextDirection(ppt1);
        ppt1.close();
    }

    private void testTextDirection(SlideShow<?,?> ppt1) throws IOException {

        TextDirection[] tds = {
                TextDirection.HORIZONTAL,
                TextDirection.VERTICAL,
                TextDirection.VERTICAL_270,
                // TextDirection.STACKED is not supported on HSLF
        };

        TableShape<?,?> tbl1 = ppt1.createSlide().createTable(1, 3);
        tbl1.setAnchor(new Rectangle2D.Double(50, 50, 200, 200));

        int col = 0;
        for (TextDirection td : tds) {
            TableCell<?,?> c = tbl1.getCell(0, col++);
            if (c != null) {
                c.setTextDirection(td);
                c.setText("bla");
            }
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ppt1.write(bos);
        ppt1.close();

        InputStream is = new ByteArrayInputStream(bos.toByteArray());
        SlideShow<?,?> ppt2 = SlideShowFactory.create(is);
        TableShape<?,?> tbl2 = (TableShape<?,?>)ppt2.getSlides().get(0).getShapes().get(0);

        col = 0;
        for (TextDirection td : tds) {
            TableCell<?,?> c = tbl2.getCell(0, col++);
            assertEquals(td, c.getTextDirection());
        }
        ppt2.close();
    }

    @Test
    void tableSpan() throws IOException {
        String[] files = (xslfOnly()) ? new String[]{"bug60993.pptx"} : new String[]{"bug60993.pptx", "bug60993.ppt"};
        for (String f : files) {
            SlideShow<?,?> ppt = openSampleSlideshow(f);
            Slide<?,?> slide = ppt.getSlides().get(0);
            TableShape<?,?> ts = (TableShape<?,?>)slide.getShapes().get(0);
            int cols = ts.getNumberOfColumns();
            int rows = ts.getNumberOfRows();
            for (int r=0; r<rows; r++) {
                for (int c=0; c<cols; c++) {
                    TableCell<?,?> tc = ts.getCell(r, c);
                    int rc = r*10+c;
                    String msg = f+" (r"+r+",c"+c+")";
                    switch (rc) {
                        case 22:
                        case 51:
                            if (f.endsWith("ppt")) {
                                assertNull(tc, msg);
                            } else {
                                assertNotNull(tc, msg);
                                assertTrue(tc.isMerged(), msg);
                            }
                            break;
                        case 21:
                            assertNotNull(tc, msg);
                            assertEquals(1, tc.getRowSpan(), msg);
                            assertEquals(2, tc.getGridSpan(), msg);
                            assertFalse(tc.isMerged(), msg);
                            break;
                        case 41:
                            assertNotNull(tc, msg);
                            assertEquals(2, tc.getRowSpan(), msg);
                            assertEquals(1, tc.getGridSpan(), msg);
                            assertFalse(tc.isMerged(), msg);
                            break;
                        default:
                            assertNotNull(tc, msg);
                            assertEquals(1, tc.getRowSpan(), msg);
                            assertEquals(1, tc.getGridSpan(), msg);
                            assertFalse(tc.isMerged(), msg);
                            break;
                    }
                }
            }
            ppt.close();
        }
    }
}