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

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.BorderPropertyTemplate.BorderExtent;
import org.junit.Test;

/**
 * Tests Spreadsheet BorderPropertyTemplate
 *
 * @see org.apache.poi.ss.util.BorderPropertyTemplate
 */
public final class TestBorderPropertyTemplate {
    @Test
    public void getNumBorders() throws IOException {
        CellRangeAddress a1a1 = new CellRangeAddress(0, 0, 0, 0); //A1:A1
        BorderPropertyTemplate pt = new BorderPropertyTemplate();
        
        pt.drawBorders(a1a1, BorderStyle.THIN, BorderExtent.TOP);
        assertEquals(1, pt.getNumBorders(0, 0));
        
        pt.drawBorders(a1a1, BorderStyle.MEDIUM, BorderExtent.BOTTOM);
        assertEquals(2, pt.getNumBorders(0, 0));
        
        pt.drawBorders(a1a1, BorderStyle.MEDIUM, BorderExtent.NONE);
        assertEquals(0, pt.getNumBorders(0, 0));
    }

    @Test
    public void getNumBorderColors() throws IOException {
        CellRangeAddress a1a1 = new CellRangeAddress(0, 0, 0, 0); //A1:A1
        BorderPropertyTemplate pt = new BorderPropertyTemplate();
        
        pt.drawBorderColors(a1a1, IndexedColors.RED.getIndex(), BorderExtent.TOP);
        assertEquals(1, pt.getNumBorderColors(0, 0));
        
        pt.drawBorderColors(a1a1, IndexedColors.RED.getIndex(), BorderExtent.BOTTOM);
        assertEquals(2, pt.getNumBorderColors(0, 0));
        
        pt.drawBorderColors(a1a1, IndexedColors.RED.getIndex(), BorderExtent.NONE);
        assertEquals(0, pt.getNumBorderColors(0, 0));
    }

    @Test
    public void getTemplateProperties() throws IOException {
        CellRangeAddress a1a1 = new CellRangeAddress(0, 0, 0, 0); //A1:A1
        BorderPropertyTemplate pt = new BorderPropertyTemplate();
        
        pt.drawBorders(a1a1, BorderStyle.THIN, BorderExtent.TOP);
        assertThin(pt.getTemplateProperty(0, 0, CellUtil.BORDER_TOP));
        
        pt.drawBorders(a1a1, BorderStyle.MEDIUM, BorderExtent.BOTTOM);
        assertMedium(pt.getTemplateProperty(0, 0, CellUtil.BORDER_BOTTOM));
        
        pt.drawBorderColors(a1a1, IndexedColors.RED.getIndex(), BorderExtent.TOP);
        assertRed(pt.getTemplateProperty(0, 0, CellUtil.TOP_BORDER_COLOR));
        
        pt.drawBorderColors(a1a1, IndexedColors.BLUE.getIndex(), BorderExtent.BOTTOM);
        assertBlue(pt.getTemplateProperty(0, 0, CellUtil.BOTTOM_BORDER_COLOR));
    }

    @Test
    public void drawBorders() throws IOException {
        CellRangeAddress a1c3 = new CellRangeAddress(0, 2, 0, 2); //A1:C3
        BorderPropertyTemplate pt = new BorderPropertyTemplate();
        
        pt.drawBorders(a1c3, BorderStyle.THIN, BorderExtent.ALL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                assertEquals(4, pt.getNumBorders(addr));
                assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_TOP));
                assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_BOTTOM));
                assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_LEFT));
                assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_RIGHT));
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                assertEquals(4, pt.getNumBorders(addr));
                if (i == 0) {
                    if (j == 0) {
                        assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_TOP));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_BOTTOM));
                        assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_LEFT));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_RIGHT));
                    } else if (j == 2) {
                        assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_TOP));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_BOTTOM));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_LEFT));
                        assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_RIGHT));
                    } else {
                        assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_TOP));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_BOTTOM));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_LEFT));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_RIGHT));
                    }
                } else if (i == 2) {
                    if (j == 0) {
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_TOP));
                        assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_BOTTOM));
                        assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_LEFT));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_RIGHT));
                    } else if (j == 2) {
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_TOP));
                        assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_BOTTOM));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_LEFT));
                        assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_RIGHT));
                    } else {
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_TOP));
                        assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_BOTTOM));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_LEFT));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_RIGHT));
                    }
                } else {
                    if (j == 0) {
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_TOP));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_BOTTOM));
                        assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_LEFT));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_RIGHT));
                    } else if (j == 2) {
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_TOP));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_BOTTOM));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_LEFT));
                        assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_RIGHT));
                    } else {
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_TOP));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_BOTTOM));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_LEFT));
                        assertThin(pt.getTemplateProperty(addr, CellUtil.BORDER_RIGHT));
                    }
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                assertEquals(0, pt.getNumBorders(addr));
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, BorderExtent.TOP);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_TOP));
                } else {
                    assertEquals(0, pt.getNumBorders(addr));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, BorderExtent.BOTTOM);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                if (i == 2) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_BOTTOM));
                } else {
                    assertEquals(0, pt.getNumBorders(addr));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, BorderExtent.LEFT);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_LEFT));
                } else {
                    assertEquals(0, pt.getNumBorders(addr));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, BorderExtent.RIGHT);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                if (j == 2) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_RIGHT));
                } else {
                    assertEquals(0, pt.getNumBorders(addr));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, BorderExtent.HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                assertEquals(2, pt.getNumBorders(addr));
                assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_TOP));
                assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_BOTTOM));
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, BorderExtent.INSIDE_HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_BOTTOM));
                } else if (i == 2) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_TOP));
                } else {
                    assertEquals(2, pt.getNumBorders(addr));
                    assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_TOP));
                    assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_BOTTOM));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, BorderExtent.OUTSIDE_HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_TOP));
                } else if (i == 2) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_BOTTOM));
                } else {
                    assertEquals(0, pt.getNumBorders(addr));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, BorderExtent.VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                assertEquals(2, pt.getNumBorders(addr));
                assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_LEFT));
                assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_RIGHT));
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, BorderExtent.INSIDE_VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_RIGHT));
                } else if (j == 2) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_LEFT));
                } else {
                    assertEquals(2, pt.getNumBorders(addr));
                    assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_LEFT));
                    assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_RIGHT));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, BorderExtent.OUTSIDE_VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_LEFT));
                } else if (j == 2) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertMedium(pt.getTemplateProperty(i ,j, CellUtil.BORDER_RIGHT));
                } else {
                    assertEquals(0, pt.getNumBorders(addr));
                }
            }
        }
    }

    @Test
    public void drawBorderColors() throws IOException {
        CellRangeAddress a1c3 = new CellRangeAddress(0, 2, 0, 2);
        BorderPropertyTemplate pt = new BorderPropertyTemplate();
        
        pt.drawBorderColors(a1c3, IndexedColors.RED.getIndex(), BorderExtent.ALL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                String msg = addr.formatAsString();
                assertEquals(msg, 4, pt.getNumBorders(addr));
                assertEquals(msg, 4, pt.getNumBorderColors(addr));
                assertRed(pt.getTemplateProperty(addr, CellUtil.TOP_BORDER_COLOR));
                assertRed(pt.getTemplateProperty(addr, CellUtil.BOTTOM_BORDER_COLOR));
                assertRed(pt.getTemplateProperty(addr, CellUtil.LEFT_BORDER_COLOR));
                assertRed(pt.getTemplateProperty(addr, CellUtil.RIGHT_BORDER_COLOR));
            }
        }
        
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), BorderExtent.OUTSIDE);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                assertEquals(4, pt.getNumBorders(addr));
                assertEquals(4, pt.getNumBorderColors(addr));
                if (i == 0) {
                    if (j == 0) {
                        assertBlue(pt.getTemplateProperty(addr, CellUtil.TOP_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.BOTTOM_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(addr, CellUtil.LEFT_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.RIGHT_BORDER_COLOR));
                    } else if (j == 2) {
                        assertBlue(pt.getTemplateProperty(addr, CellUtil.TOP_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.BOTTOM_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.LEFT_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(addr, CellUtil.RIGHT_BORDER_COLOR));
                    } else {
                        assertBlue(pt.getTemplateProperty(addr, CellUtil.TOP_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.BOTTOM_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.LEFT_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.RIGHT_BORDER_COLOR));
                    }
                } else if (i == 2) {
                    if (j == 0) {
                        assertRed(pt.getTemplateProperty(addr, CellUtil.TOP_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(addr, CellUtil.BOTTOM_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(addr, CellUtil.LEFT_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.RIGHT_BORDER_COLOR));
                    } else if (j == 2) {
                        assertRed(pt.getTemplateProperty(addr, CellUtil.TOP_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(addr, CellUtil.BOTTOM_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.LEFT_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(addr, CellUtil.RIGHT_BORDER_COLOR));
                    } else {
                        assertRed(pt.getTemplateProperty(addr, CellUtil.TOP_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(addr, CellUtil.BOTTOM_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.LEFT_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.RIGHT_BORDER_COLOR));
                    }
                } else {
                    if (j == 0) {
                        assertRed(pt.getTemplateProperty(addr, CellUtil.TOP_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.BOTTOM_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(addr, CellUtil.LEFT_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.RIGHT_BORDER_COLOR));
                    } else if (j == 2) {
                        assertRed(pt.getTemplateProperty(addr, CellUtil.TOP_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.BOTTOM_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.LEFT_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(addr, CellUtil.RIGHT_BORDER_COLOR));
                    } else {
                        assertRed(pt.getTemplateProperty(addr, CellUtil.TOP_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.BOTTOM_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.LEFT_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(addr, CellUtil.RIGHT_BORDER_COLOR));
                    }
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), BorderExtent.NONE);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                assertEquals(0, pt.getNumBorders(addr));
                assertEquals(0, pt.getNumBorderColors(addr));
            }
        }
        
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), BorderExtent.TOP);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertEquals(1, pt.getNumBorderColors(addr));
                    assertBlue(pt.getTemplateProperty(addr, CellUtil.TOP_BORDER_COLOR));
                } else {
                    assertEquals(0, pt.getNumBorders(addr));
                    assertEquals(0, pt.getNumBorderColors(addr));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), BorderExtent.BOTTOM);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                if (i == 2) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertEquals(1, pt.getNumBorderColors(addr));
                    assertBlue(pt.getTemplateProperty(addr, CellUtil.BOTTOM_BORDER_COLOR));
                } else {
                    assertEquals(0, pt.getNumBorders(addr));
                    assertEquals(0, pt.getNumBorderColors(addr));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), BorderExtent.LEFT);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertEquals(1, pt.getNumBorderColors(addr));
                    assertBlue(pt.getTemplateProperty(addr, CellUtil.LEFT_BORDER_COLOR));
                } else {
                    assertEquals(0, pt.getNumBorders(addr));
                    assertEquals(0, pt.getNumBorderColors(addr));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), BorderExtent.RIGHT);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                if (j == 2) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertEquals(1, pt.getNumBorderColors(addr));
                    assertBlue(pt.getTemplateProperty(addr, CellUtil.RIGHT_BORDER_COLOR));
                } else {
                    assertEquals(0, pt.getNumBorders(addr));
                    assertEquals(0, pt.getNumBorderColors(addr));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), BorderExtent.HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                assertEquals(2, pt.getNumBorders(addr));
                assertEquals(2, pt.getNumBorderColors(addr));
                assertBlue(pt.getTemplateProperty(addr, CellUtil.TOP_BORDER_COLOR));
                assertBlue(pt.getTemplateProperty(addr, CellUtil.BOTTOM_BORDER_COLOR));
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), BorderExtent.INSIDE_HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertEquals(1, pt.getNumBorderColors(addr));
                    assertBlue(pt.getTemplateProperty(addr, CellUtil.BOTTOM_BORDER_COLOR));
                } else if (i == 2) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertEquals(1, pt.getNumBorderColors(addr));
                    assertBlue(pt.getTemplateProperty(addr, CellUtil.TOP_BORDER_COLOR));
                } else {
                    assertEquals(2, pt.getNumBorders(addr));
                    assertEquals(2, pt.getNumBorderColors(addr));
                    assertBlue(pt.getTemplateProperty(addr, CellUtil.TOP_BORDER_COLOR));
                    assertBlue(pt.getTemplateProperty(addr, CellUtil.BOTTOM_BORDER_COLOR));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), BorderExtent.OUTSIDE_HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertEquals(1, pt.getNumBorderColors(addr));
                    assertBlue(pt.getTemplateProperty(addr, CellUtil.TOP_BORDER_COLOR));
                } else if (i == 2) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertEquals(1, pt.getNumBorderColors(addr));
                    assertBlue(pt.getTemplateProperty(addr, CellUtil.BOTTOM_BORDER_COLOR));
                } else {
                    assertEquals(0, pt.getNumBorders(addr));
                    assertEquals(0, pt.getNumBorderColors(addr));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), BorderExtent.VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                assertEquals(2, pt.getNumBorders(addr));
                assertEquals(2, pt.getNumBorderColors(addr));
                assertBlue(pt.getTemplateProperty(addr, CellUtil.LEFT_BORDER_COLOR));
                assertBlue(pt.getTemplateProperty(addr, CellUtil.RIGHT_BORDER_COLOR));
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), BorderExtent.INSIDE_VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertEquals(1, pt.getNumBorderColors(addr));
                    assertBlue(pt.getTemplateProperty(addr, CellUtil.RIGHT_BORDER_COLOR));
                } else if (j == 2) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertEquals(1, pt.getNumBorderColors(addr));
                    assertBlue(pt.getTemplateProperty(addr, CellUtil.LEFT_BORDER_COLOR));
                } else {
                    assertEquals(2, pt.getNumBorders(addr));
                    assertEquals(2, pt.getNumBorderColors(addr));
                    assertBlue(pt.getTemplateProperty(addr, CellUtil.LEFT_BORDER_COLOR));
                    assertBlue(pt.getTemplateProperty(addr, CellUtil.RIGHT_BORDER_COLOR));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), BorderExtent.OUTSIDE_VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertEquals(1, pt.getNumBorderColors(addr));
                    assertBlue(pt.getTemplateProperty(addr, CellUtil.LEFT_BORDER_COLOR));
                } else if (j == 2) {
                    assertEquals(1, pt.getNumBorders(addr));
                    assertEquals(1, pt.getNumBorderColors(addr));
                    assertBlue(pt.getTemplateProperty(addr, CellUtil.RIGHT_BORDER_COLOR));
                } else {
                    assertEquals(0, pt.getNumBorders(addr));
                    assertEquals(0, pt.getNumBorderColors(addr));
                }
            }
        }
    }
    
    @Test
    public void drawBordersWithColors() throws IOException {
        CellRangeAddress a1c3 = new CellRangeAddress(0, 2, 0, 2);
        BorderPropertyTemplate pt = new BorderPropertyTemplate();
        
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, IndexedColors.RED.getIndex(), BorderExtent.ALL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                assertEquals(4, pt.getNumBorders(addr));
                assertEquals(4, pt.getNumBorderColors(addr));
                assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_TOP));
                assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_BOTTOM));
                assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_LEFT));
                assertMedium(pt.getTemplateProperty(addr, CellUtil.BORDER_RIGHT));
                assertRed(pt.getTemplateProperty(addr, CellUtil.TOP_BORDER_COLOR));
                assertRed(pt.getTemplateProperty(addr, CellUtil.BOTTOM_BORDER_COLOR));
                assertRed(pt.getTemplateProperty(addr, CellUtil.LEFT_BORDER_COLOR));
                assertRed(pt.getTemplateProperty(addr, CellUtil.RIGHT_BORDER_COLOR));
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.NONE, IndexedColors.RED.getIndex(), BorderExtent.ALL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                assertEquals(4, pt.getNumBorders(addr));
                assertEquals(0, pt.getNumBorderColors(addr));
                assertNone(pt.getTemplateProperty(addr, CellUtil.BORDER_TOP));
                assertNone(pt.getTemplateProperty(addr, CellUtil.BORDER_BOTTOM));
                assertNone(pt.getTemplateProperty(addr, CellUtil.BORDER_LEFT));
                assertNone(pt.getTemplateProperty(addr, CellUtil.BORDER_RIGHT));
            }
        }
    }

    @Test
    public void applyBorders() throws IOException {
        CellRangeAddress a1c3 = new CellRangeAddress(0, 2, 0, 2);
        CellRangeAddress b2 = new CellRangeAddress(1, 1, 1, 1);
        BorderPropertyTemplate pt = new BorderPropertyTemplate();
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        
        pt.drawBorders(a1c3, BorderStyle.THIN, IndexedColors.RED.getIndex(), BorderExtent.ALL);
        pt.applyBorders(sheet);
        
        for (Row row: sheet) {
            for (Cell cell: row) {
                CellStyle cs = cell.getCellStyle();
                
                assertThin(cs.getBorderTop());
                assertRed(cs.getTopBorderColor());
                
                assertThin(cs.getBorderBottom());
                assertRed(cs.getBottomBorderColor());
                
                assertThin(cs.getBorderLeft());
                assertRed(cs.getLeftBorderColor());
                
                assertThin(cs.getBorderRight());
                assertRed(cs.getRightBorderColor());
            }
        }
        
        pt.drawBorders(b2, BorderStyle.NONE, BorderExtent.ALL);
        pt.applyBorders(sheet);
        
        for (Row row: sheet) {
            for (Cell cell: row) {
                CellStyle cs = cell.getCellStyle();
                if (cell.getColumnIndex() != 1 || row.getRowNum() == 0) {
                    assertThin(cs.getBorderTop());
                    assertRed(cs.getTopBorderColor());
                } else {
                    assertNone(cs.getBorderTop());
                }
                if (cell.getColumnIndex() != 1 || row.getRowNum() == 2) {
                    assertThin(cs.getBorderBottom());
                    assertRed(cs.getBottomBorderColor());
                } else {
                    assertNone(cs.getBorderBottom());
                }
                if (cell.getColumnIndex() == 0 || row.getRowNum() != 1) {
                    assertThin(cs.getBorderLeft());
                    assertRed(cs.getLeftBorderColor());
                } else {
                    assertNone(cs.getBorderLeft());
                }
                if (cell.getColumnIndex() == 2 || row.getRowNum() != 1) {
                    assertThin(cs.getBorderRight());
                    assertRed(cs.getRightBorderColor());
                } else {
                    assertNone(cs.getBorderRight());
                }
            }
        }
        
        wb.close();
    }
    
    // helper functions to make sure template properties were set correctly
    
    //////// Border Styles ///////////
    private static void assertNone(Object actual) {
        assertNone((BorderStyle) actual);
    }
    private static void assertNone(BorderStyle actual) {
        assertEquals(BorderStyle.NONE, actual);
    }
    
    private static void assertThin(Object actual) {
        assertThin((BorderStyle) actual);
    }
    private static void assertThin(BorderStyle actual) {
        assertEquals(BorderStyle.THIN, actual);
    }
    
    private static void assertMedium(Object actual) {
        assertMedium((BorderStyle) actual);
    }
    private static void assertMedium(BorderStyle actual) {
        assertEquals(BorderStyle.MEDIUM, actual);
    }
    
    //////// Border Colors (use IndexedColor codes, for now ///////////
    private static void assertRed(Object actual) {
        IndexedColors actualColor = IndexedColors.fromInt((Short) actual);
        assertRed(actualColor);
    }
    private static void assertRed(IndexedColors actual) {
        assertEquals(IndexedColors.RED, actual);
    }

    private static void assertBlue(Object actual) {
        IndexedColors actualColor = IndexedColors.fromInt((Short) actual);
        assertBlue(actualColor);
    }
    private static void assertBlue(IndexedColors actual) {
        assertEquals(IndexedColors.BLUE, actual);
    }
}
