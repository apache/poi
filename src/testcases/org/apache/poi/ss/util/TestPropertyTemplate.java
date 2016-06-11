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
import org.apache.poi.ss.util.PropertyTemplate.Extent;
import org.junit.Test;

/**
 * Tests Spreadsheet PropertyTemplate
 *
 * @see org.apache.poi.ss.util.PropertyTemplate
 */
public final class TestPropertyTemplate {
    @Test
    public void getNumBorders() throws IOException {
        CellRangeAddress a1 = new CellRangeAddress(0, 0, 0, 0);
        PropertyTemplate pt = new PropertyTemplate();
        
        pt.drawBorders(a1, BorderStyle.THIN, Extent.TOP);
        assertEquals(1, pt.getNumBorders(0, 0));
        
        pt.drawBorders(a1, BorderStyle.MEDIUM, Extent.BOTTOM);
        assertEquals(2, pt.getNumBorders(0, 0));
        
        pt.drawBorders(a1, BorderStyle.MEDIUM, Extent.NONE);
        assertEquals(0, pt.getNumBorders(0, 0));
    }

    @Test
    public void getNumBorderColors() throws IOException {
        CellRangeAddress a1 = new CellRangeAddress(0, 0, 0, 0);
        PropertyTemplate pt = new PropertyTemplate();
        
        pt.drawBorderColors(a1, IndexedColors.RED.getIndex(), Extent.TOP);
        assertEquals(1, pt.getNumBorderColors(0, 0));
        
        pt.drawBorderColors(a1, IndexedColors.RED.getIndex(), Extent.BOTTOM);
        assertEquals(2, pt.getNumBorderColors(0, 0));
        
        pt.drawBorderColors(a1, IndexedColors.RED.getIndex(), Extent.NONE);
        assertEquals(0, pt.getNumBorderColors(0, 0));
    }

    @Test
    public void getTemplateProperties() throws IOException {
        CellRangeAddress a1 = new CellRangeAddress(0, 0, 0, 0);
        PropertyTemplate pt = new PropertyTemplate();
        
        pt.drawBorders(a1, BorderStyle.THIN, Extent.TOP);
        assertThin(pt.getTemplateProperty(0, 0, CellUtil.BORDER_TOP));
        
        pt.drawBorders(a1, BorderStyle.MEDIUM, Extent.BOTTOM);
        assertMedium(pt.getTemplateProperty(0, 0, CellUtil.BORDER_BOTTOM));
        
        pt.drawBorderColors(a1, IndexedColors.RED.getIndex(), Extent.TOP);
        assertRed(pt.getTemplateProperty(0, 0, CellUtil.TOP_BORDER_COLOR));
        
        pt.drawBorderColors(a1, IndexedColors.BLUE.getIndex(), Extent.BOTTOM);
        assertBlue(pt.getTemplateProperty(0, 0, CellUtil.BOTTOM_BORDER_COLOR));
    }

    @Test
    public void drawBorders() throws IOException {
        CellRangeAddress a1c3 = new CellRangeAddress(0, 2, 0, 2);
        PropertyTemplate pt = new PropertyTemplate();
        
        pt.drawBorders(a1c3, BorderStyle.THIN, Extent.ALL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, pt.getNumBorders(i, j));
                assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, Extent.OUTSIDE);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, pt.getNumBorders(i, j));
                if (i == 0) {
                    if (j == 0) {
                        assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                        assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                    } else if (j == 2) {
                        assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                        assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                    } else {
                        assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                    }
                } else if (i == 2) {
                    if (j == 0) {
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                        assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                        assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                    } else if (j == 2) {
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                        assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                        assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                    } else {
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                        assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                    }
                } else {
                    if (j == 0) {
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                        assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                    } else if (j == 2) {
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                        assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                    } else {
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                        assertThin(pt.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                    }
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(0, pt.getNumBorders(i, j));
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, Extent.TOP);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, Extent.BOTTOM);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, Extent.LEFT);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, Extent.RIGHT);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, Extent.HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(2, pt.getNumBorders(i, j));
                assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, Extent.INSIDE_HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                } else if (i == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                } else {
                    assertEquals(2, pt.getNumBorders(i, j));
                    assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                    assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, Extent.OUTSIDE_HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                } else if (i == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, Extent.VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(2, pt.getNumBorders(i, j));
                assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, Extent.INSIDE_VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                } else if (j == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                } else {
                    assertEquals(2, pt.getNumBorders(i, j));
                    assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                    assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, Extent.OUTSIDE_VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                } else if (j == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertMedium(pt.getTemplateProperty(i ,j, CellUtil.BORDER_RIGHT));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                }
            }
        }
    }

    @Test
    public void drawBorderColors() throws IOException {
        CellRangeAddress a1c3 = new CellRangeAddress(0, 2, 0, 2);
        PropertyTemplate pt = new PropertyTemplate();
        
        pt.drawBorderColors(a1c3, IndexedColors.RED.getIndex(), Extent.ALL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                CellAddress addr = new CellAddress(i, j);
                String msg = addr.formatAsString();
                assertEquals(msg, 4, pt.getNumBorders(i, j));
                assertEquals(msg, 4, pt.getNumBorderColors(i, j));
                assertRed(pt.getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                assertRed(pt.getTemplateProperty(i, j, CellUtil.BOTTOM_BORDER_COLOR));
                assertRed(pt.getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                assertRed(pt.getTemplateProperty(i, j, CellUtil.RIGHT_BORDER_COLOR));
            }
        }
        
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), Extent.OUTSIDE);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, pt.getNumBorders(i, j));
                assertEquals(4, pt.getNumBorderColors(i, j));
                if (i == 0) {
                    if (j == 0) {
                        assertBlue(pt.getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.BOTTOM_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.RIGHT_BORDER_COLOR));
                    } else if (j == 2) {
                        assertBlue(pt.getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.BOTTOM_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(i, j, CellUtil.RIGHT_BORDER_COLOR));
                    } else {
                        assertBlue(pt.getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.BOTTOM_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.RIGHT_BORDER_COLOR));
                    }
                } else if (i == 2) {
                    if (j == 0) {
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(i, j, CellUtil.BOTTOM_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.RIGHT_BORDER_COLOR));
                    } else if (j == 2) {
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(i, j, CellUtil.BOTTOM_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(i, j, CellUtil.RIGHT_BORDER_COLOR));
                    } else {
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(i, j, CellUtil.BOTTOM_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.RIGHT_BORDER_COLOR));
                    }
                } else {
                    if (j == 0) {
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.BOTTOM_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.RIGHT_BORDER_COLOR));
                    } else if (j == 2) {
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.BOTTOM_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                        assertBlue(pt.getTemplateProperty(i, j, CellUtil.RIGHT_BORDER_COLOR));
                    } else {
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.BOTTOM_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                        assertRed(pt.getTemplateProperty(i, j, CellUtil.RIGHT_BORDER_COLOR));
                    }
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), Extent.NONE);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(0, pt.getNumBorders(i, j));
                assertEquals(0, pt.getNumBorderColors(i, j));
            }
        }
        
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), Extent.TOP);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertBlue(pt.getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                    assertEquals(0, pt.getNumBorderColors(i, j));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), Extent.BOTTOM);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertBlue(pt.getTemplateProperty(i, j, CellUtil.BOTTOM_BORDER_COLOR));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                    assertEquals(0, pt.getNumBorderColors(i, j));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), Extent.LEFT);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertBlue(pt.getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                    assertEquals(0, pt.getNumBorderColors(i, j));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), Extent.RIGHT);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertBlue(pt.getTemplateProperty(i, j, CellUtil.RIGHT_BORDER_COLOR));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                    assertEquals(0, pt.getNumBorderColors(i, j));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), Extent.HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(2, pt.getNumBorders(i, j));
                assertEquals(2, pt.getNumBorderColors(i, j));
                assertBlue(pt.getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                assertBlue(pt.getTemplateProperty(i, j, CellUtil.BOTTOM_BORDER_COLOR));
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), Extent.INSIDE_HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertBlue(pt.getTemplateProperty(i, j, CellUtil.BOTTOM_BORDER_COLOR));
                } else if (i == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertBlue(pt.getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                } else {
                    assertEquals(2, pt.getNumBorders(i, j));
                    assertEquals(2, pt.getNumBorderColors(i, j));
                    assertBlue(pt.getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                    assertBlue(pt.getTemplateProperty(i, j, CellUtil.BOTTOM_BORDER_COLOR));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), Extent.OUTSIDE_HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertBlue(pt.getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                } else if (i == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertBlue(pt.getTemplateProperty(i, j, CellUtil.BOTTOM_BORDER_COLOR));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                    assertEquals(0, pt.getNumBorderColors(i, j));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), Extent.VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(2, pt.getNumBorders(i, j));
                assertEquals(2, pt.getNumBorderColors(i, j));
                assertBlue(pt.getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                assertBlue(pt.getTemplateProperty(i, j, CellUtil.RIGHT_BORDER_COLOR));
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), Extent.INSIDE_VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertBlue(pt.getTemplateProperty(i, j, CellUtil.RIGHT_BORDER_COLOR));
                } else if (j == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertBlue(pt.getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                } else {
                    assertEquals(2, pt.getNumBorders(i, j));
                    assertEquals(2, pt.getNumBorderColors(i, j));
                    assertBlue(pt.getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                    assertBlue(pt.getTemplateProperty(i, j, CellUtil.RIGHT_BORDER_COLOR));
                }
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(), Extent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(), Extent.OUTSIDE_VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertBlue(pt.getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                } else if (j == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertBlue(pt.getTemplateProperty(i, j, CellUtil.RIGHT_BORDER_COLOR));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                    assertEquals(0, pt.getNumBorderColors(i, j));
                }
            }
        }
    }
    
    @Test
    public void drawBordersWithColors() throws IOException {
        CellRangeAddress a1c3 = new CellRangeAddress(0, 2, 0, 2);
        PropertyTemplate pt = new PropertyTemplate();
        
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, IndexedColors.RED.getIndex(), Extent.ALL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, pt.getNumBorders(i, j));
                assertEquals(4, pt.getNumBorderColors(i, j));
                assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                assertMedium(pt.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                assertRed(pt.getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                assertRed(pt.getTemplateProperty(i, j, CellUtil.BOTTOM_BORDER_COLOR));
                assertRed(pt.getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                assertRed(pt.getTemplateProperty(i, j, CellUtil.RIGHT_BORDER_COLOR));
            }
        }
        
        pt.drawBorders(a1c3, BorderStyle.NONE, Extent.NONE);
        pt.drawBorders(a1c3, BorderStyle.NONE, IndexedColors.RED.getIndex(), Extent.ALL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, pt.getNumBorders(i, j));
                assertEquals(0, pt.getNumBorderColors(i, j));
                assertNone(pt.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                assertNone(pt.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                assertNone(pt.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                assertNone(pt.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
            }
        }
    }

    @Test
    public void applyBorders() throws IOException {
        CellRangeAddress a1c3 = new CellRangeAddress(0, 2, 0, 2);
        CellRangeAddress b2 = new CellRangeAddress(1, 1, 1, 1);
        PropertyTemplate pt = new PropertyTemplate();
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        
        pt.drawBorders(a1c3, BorderStyle.THIN, IndexedColors.RED.getIndex(), Extent.ALL);
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
        
        pt.drawBorders(b2, BorderStyle.NONE, Extent.ALL);
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
