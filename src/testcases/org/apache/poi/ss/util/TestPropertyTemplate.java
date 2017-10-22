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
import static org.junit.Assert.assertNotSame;

import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
        pt.drawBorders(a1, BorderStyle.THIN, BorderExtent.TOP);
        assertEquals(1, pt.getNumBorders(0, 0));
        pt.drawBorders(a1, BorderStyle.MEDIUM, BorderExtent.BOTTOM);
        assertEquals(2, pt.getNumBorders(0, 0));
        pt.drawBorders(a1, BorderStyle.MEDIUM, BorderExtent.NONE);
        assertEquals(0, pt.getNumBorders(0, 0));
    }

    @Test
    public void getNumBorderColors() throws IOException {
        CellRangeAddress a1 = new CellRangeAddress(0, 0, 0, 0);
        PropertyTemplate pt = new PropertyTemplate();
        pt.drawBorderColors(a1, IndexedColors.RED.getIndex(), BorderExtent.TOP);
        assertEquals(1, pt.getNumBorderColors(0, 0));
        pt.drawBorderColors(a1, IndexedColors.RED.getIndex(), BorderExtent.BOTTOM);
        assertEquals(2, pt.getNumBorderColors(0, 0));
        pt.drawBorderColors(a1, IndexedColors.RED.getIndex(), BorderExtent.NONE);
        assertEquals(0, pt.getNumBorderColors(0, 0));
    }

    @Test
    public void getTemplateProperties() throws IOException {
        CellRangeAddress a1 = new CellRangeAddress(0, 0, 0, 0);
        PropertyTemplate pt = new PropertyTemplate();
        pt.drawBorders(a1, BorderStyle.THIN, BorderExtent.TOP);
        assertEquals(BorderStyle.THIN,
                pt.getBorderStyle(0, 0, CellUtil.BORDER_TOP));
        pt.drawBorders(a1, BorderStyle.MEDIUM, BorderExtent.BOTTOM);
        assertEquals(BorderStyle.MEDIUM,
                pt.getBorderStyle(0, 0, CellUtil.BORDER_BOTTOM));
        pt.drawBorderColors(a1, IndexedColors.RED.getIndex(), BorderExtent.TOP);
        assertEquals(IndexedColors.RED.getIndex(),
                pt.getTemplateProperty(0, 0, CellUtil.TOP_BORDER_COLOR));
        pt.drawBorderColors(a1, IndexedColors.BLUE.getIndex(), BorderExtent.BOTTOM);
        assertEquals(IndexedColors.BLUE.getIndex(),
                pt.getTemplateProperty(0, 0, CellUtil.BOTTOM_BORDER_COLOR));
    }

    @Test
    public void drawBorders() throws IOException {
        CellRangeAddress a1c3 = new CellRangeAddress(0, 2, 0, 2);
        PropertyTemplate pt = new PropertyTemplate();
        pt.drawBorders(a1c3, BorderStyle.THIN,
                BorderExtent.ALL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, pt.getNumBorders(i, j));
                assertEquals(BorderStyle.THIN,
                        pt.getBorderStyle(i, j, CellUtil.BORDER_TOP));
                assertEquals(BorderStyle.THIN,
                        pt.getBorderStyle(i, j, CellUtil.BORDER_BOTTOM));
                assertEquals(BorderStyle.THIN,
                        pt.getBorderStyle(i, j, CellUtil.BORDER_LEFT));
                assertEquals(BorderStyle.THIN,
                        pt.getBorderStyle(i, j, CellUtil.BORDER_RIGHT));
            }
        }
        pt.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.OUTSIDE);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, pt.getNumBorders(i, j));
                if (i == 0) {
                    if (j == 0) {
                        assertEquals(BorderStyle.MEDIUM,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.MEDIUM,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_RIGHT));
                    } else if (j == 2) {
                        assertEquals(BorderStyle.MEDIUM,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.MEDIUM,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_RIGHT));
                    } else {
                        assertEquals(BorderStyle.MEDIUM,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_RIGHT));
                    }
                } else if (i == 2) {
                    if (j == 0) {
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.MEDIUM,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.MEDIUM,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_RIGHT));
                    } else if (j == 2) {
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.MEDIUM,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.MEDIUM,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_RIGHT));
                    } else {
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.MEDIUM,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_RIGHT));
                    }
                } else {
                    if (j == 0) {
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.MEDIUM,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_RIGHT));
                    } else if (j == 2) {
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.MEDIUM,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_RIGHT));
                    } else {
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.THIN,
                                pt.getBorderStyle(i, j,
                                        CellUtil.BORDER_RIGHT));
                    }
                }
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(0, pt.getNumBorders(i, j));
            }
        }
        pt.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.TOP);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM,
                            pt.getBorderStyle(i, j, CellUtil.BORDER_TOP));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                }
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.BOTTOM);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM, pt
                            .getBorderStyle(i, j, CellUtil.BORDER_BOTTOM));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                }
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.LEFT);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM,
                            pt.getBorderStyle(i, j, CellUtil.BORDER_LEFT));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                }
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.RIGHT);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM, pt
                            .getBorderStyle(i, j, CellUtil.BORDER_RIGHT));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                }
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(2, pt.getNumBorders(i, j));
                assertEquals(BorderStyle.MEDIUM,
                        pt.getBorderStyle(i, j, CellUtil.BORDER_TOP));
                assertEquals(BorderStyle.MEDIUM,
                        pt.getBorderStyle(i, j, CellUtil.BORDER_BOTTOM));
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.INSIDE_HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM, pt
                            .getBorderStyle(i, j, CellUtil.BORDER_BOTTOM));
                } else if (i == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM,
                            pt.getBorderStyle(i, j, CellUtil.BORDER_TOP));
                } else {
                    assertEquals(2, pt.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM,
                            pt.getBorderStyle(i, j, CellUtil.BORDER_TOP));
                    assertEquals(BorderStyle.MEDIUM, pt
                            .getBorderStyle(i, j, CellUtil.BORDER_BOTTOM));
                }
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.OUTSIDE_HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM,
                            pt.getBorderStyle(i, j, CellUtil.BORDER_TOP));
                } else if (i == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM, pt
                            .getBorderStyle(i, j, CellUtil.BORDER_BOTTOM));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                }
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(2, pt.getNumBorders(i, j));
                assertEquals(BorderStyle.MEDIUM,
                        pt.getBorderStyle(i, j, CellUtil.BORDER_LEFT));
                assertEquals(BorderStyle.MEDIUM,
                        pt.getBorderStyle(i, j, CellUtil.BORDER_RIGHT));
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.INSIDE_VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM, pt
                            .getBorderStyle(i, j, CellUtil.BORDER_RIGHT));
                } else if (j == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM,
                            pt.getBorderStyle(i, j, CellUtil.BORDER_LEFT));
                } else {
                    assertEquals(2, pt.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM,
                            pt.getBorderStyle(i, j, CellUtil.BORDER_LEFT));
                    assertEquals(BorderStyle.MEDIUM, pt
                            .getBorderStyle(i, j, CellUtil.BORDER_RIGHT));
                }
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.OUTSIDE_VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM,
                            pt.getBorderStyle(i, j, CellUtil.BORDER_LEFT));
                } else if (j == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM, pt
                            .getBorderStyle(i, j, CellUtil.BORDER_RIGHT));
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
        pt.drawBorderColors(a1c3, IndexedColors.RED.getIndex(),
                BorderExtent.ALL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, pt.getNumBorders(i, j));
                assertEquals(4, pt.getNumBorderColors(i, j));
                assertEquals(IndexedColors.RED.getIndex(), pt
                        .getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                assertEquals(IndexedColors.RED.getIndex(),
                        pt.getTemplateProperty(i, j,
                                CellUtil.BOTTOM_BORDER_COLOR));
                assertEquals(IndexedColors.RED.getIndex(), pt
                        .getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                assertEquals(IndexedColors.RED.getIndex(),
                        pt.getTemplateProperty(i, j,
                                CellUtil.RIGHT_BORDER_COLOR));
            }
        }
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.OUTSIDE);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, pt.getNumBorders(i, j));
                assertEquals(4, pt.getNumBorderColors(i, j));
                if (i == 0) {
                    if (j == 0) {
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    } else if (j == 2) {
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    } else {
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    }
                } else if (i == 2) {
                    if (j == 0) {
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    } else if (j == 2) {
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    } else {
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    }
                } else {
                    if (j == 0) {
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    } else if (j == 2) {
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    } else {
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                pt.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    }
                }
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(0, pt.getNumBorders(i, j));
                assertEquals(0, pt.getNumBorderColors(i, j));
            }
        }
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.TOP);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            pt.getTemplateProperty(i, j,
                                    CellUtil.TOP_BORDER_COLOR));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                    assertEquals(0, pt.getNumBorderColors(i, j));
                }
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.BOTTOM);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            pt.getTemplateProperty(i, j,
                                    CellUtil.BOTTOM_BORDER_COLOR));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                    assertEquals(0, pt.getNumBorderColors(i, j));
                }
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.LEFT);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            pt.getTemplateProperty(i, j,
                                    CellUtil.LEFT_BORDER_COLOR));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                    assertEquals(0, pt.getNumBorderColors(i, j));
                }
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.RIGHT);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            pt.getTemplateProperty(i, j,
                                    CellUtil.RIGHT_BORDER_COLOR));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                    assertEquals(0, pt.getNumBorderColors(i, j));
                }
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(2, pt.getNumBorders(i, j));
                assertEquals(2, pt.getNumBorderColors(i, j));
                assertEquals(IndexedColors.BLUE.getIndex(), pt
                        .getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                assertEquals(IndexedColors.BLUE.getIndex(),
                        pt.getTemplateProperty(i, j,
                                CellUtil.BOTTOM_BORDER_COLOR));
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.INSIDE_HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            pt.getTemplateProperty(i, j,
                                    CellUtil.BOTTOM_BORDER_COLOR));
                } else if (i == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            pt.getTemplateProperty(i, j,
                                    CellUtil.TOP_BORDER_COLOR));
                } else {
                    assertEquals(2, pt.getNumBorders(i, j));
                    assertEquals(2, pt.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            pt.getTemplateProperty(i, j,
                                    CellUtil.TOP_BORDER_COLOR));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            pt.getTemplateProperty(i, j,
                                    CellUtil.BOTTOM_BORDER_COLOR));
                }
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.OUTSIDE_HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            pt.getTemplateProperty(i, j,
                                    CellUtil.TOP_BORDER_COLOR));
                } else if (i == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            pt.getTemplateProperty(i, j,
                                    CellUtil.BOTTOM_BORDER_COLOR));
                } else {
                    assertEquals(0, pt.getNumBorders(i, j));
                    assertEquals(0, pt.getNumBorderColors(i, j));
                }
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(2, pt.getNumBorders(i, j));
                assertEquals(2, pt.getNumBorderColors(i, j));
                assertEquals(IndexedColors.BLUE.getIndex(), pt
                        .getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                assertEquals(IndexedColors.BLUE.getIndex(),
                        pt.getTemplateProperty(i, j,
                                CellUtil.RIGHT_BORDER_COLOR));
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.INSIDE_VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            pt.getTemplateProperty(i, j,
                                    CellUtil.RIGHT_BORDER_COLOR));
                } else if (j == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            pt.getTemplateProperty(i, j,
                                    CellUtil.LEFT_BORDER_COLOR));
                } else {
                    assertEquals(2, pt.getNumBorders(i, j));
                    assertEquals(2, pt.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            pt.getTemplateProperty(i, j,
                                    CellUtil.LEFT_BORDER_COLOR));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            pt.getTemplateProperty(i, j,
                                    CellUtil.RIGHT_BORDER_COLOR));
                }
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        pt.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.OUTSIDE_VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            pt.getTemplateProperty(i, j,
                                    CellUtil.LEFT_BORDER_COLOR));
                } else if (j == 2) {
                    assertEquals(1, pt.getNumBorders(i, j));
                    assertEquals(1, pt.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            pt.getTemplateProperty(i, j,
                                    CellUtil.RIGHT_BORDER_COLOR));
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
        
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, IndexedColors.RED.getIndex(), BorderExtent.ALL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, pt.getNumBorders(i, j));
                assertEquals(4, pt.getNumBorderColors(i, j));
                assertEquals(BorderStyle.MEDIUM,
                        pt.getBorderStyle(i, j, CellUtil.BORDER_TOP));
                assertEquals(BorderStyle.MEDIUM,
                        pt.getBorderStyle(i, j, CellUtil.BORDER_BOTTOM));
                assertEquals(BorderStyle.MEDIUM,
                        pt.getBorderStyle(i, j, CellUtil.BORDER_LEFT));
                assertEquals(BorderStyle.MEDIUM,
                        pt.getBorderStyle(i, j, CellUtil.BORDER_RIGHT));
                assertEquals(IndexedColors.RED.getIndex(), pt
                        .getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                assertEquals(IndexedColors.RED.getIndex(),
                        pt.getTemplateProperty(i, j,
                                CellUtil.BOTTOM_BORDER_COLOR));
                assertEquals(IndexedColors.RED.getIndex(), pt
                        .getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                assertEquals(IndexedColors.RED.getIndex(),
                        pt.getTemplateProperty(i, j,
                                CellUtil.RIGHT_BORDER_COLOR));
            }
        }
        pt.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        pt.drawBorders(a1c3, BorderStyle.NONE, IndexedColors.RED.getIndex(), BorderExtent.ALL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, pt.getNumBorders(i, j));
                assertEquals(0, pt.getNumBorderColors(i, j));
                assertEquals(BorderStyle.NONE,
                        pt.getBorderStyle(i, j, CellUtil.BORDER_TOP));
                assertEquals(BorderStyle.NONE,
                        pt.getBorderStyle(i, j, CellUtil.BORDER_BOTTOM));
                assertEquals(BorderStyle.NONE,
                        pt.getBorderStyle(i, j, CellUtil.BORDER_LEFT));
                assertEquals(BorderStyle.NONE,
                        pt.getBorderStyle(i, j, CellUtil.BORDER_RIGHT));
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
        
        pt.drawBorders(a1c3, BorderStyle.THIN, IndexedColors.RED.getIndex(), BorderExtent.ALL);
        pt.applyBorders(sheet);
        
        for (Row row: sheet) {
            for (Cell cell: row) {
                CellStyle cs = cell.getCellStyle();
                assertEquals(BorderStyle.THIN, cs.getBorderTop());
                assertEquals(IndexedColors.RED.getIndex(), cs.getTopBorderColor());
                assertEquals(BorderStyle.THIN, cs.getBorderBottom());
                assertEquals(IndexedColors.RED.getIndex(), cs.getBottomBorderColor());
                assertEquals(BorderStyle.THIN, cs.getBorderLeft());
                assertEquals(IndexedColors.RED.getIndex(), cs.getLeftBorderColor());
                assertEquals(BorderStyle.THIN, cs.getBorderRight());
                assertEquals(IndexedColors.RED.getIndex(), cs.getRightBorderColor());
            }
        }
        
        pt.drawBorders(b2, BorderStyle.NONE, BorderExtent.ALL);
        pt.applyBorders(sheet);
        
        for (Row row: sheet) {
            for (Cell cell: row) {
                CellStyle cs = cell.getCellStyle();
                if (cell.getColumnIndex() != 1 || row.getRowNum() == 0) {
                assertEquals(BorderStyle.THIN, cs.getBorderTop());
                assertEquals(IndexedColors.RED.getIndex(), cs.getTopBorderColor());
                } else {
                    assertEquals(BorderStyle.NONE, cs.getBorderTop());
                }
                if (cell.getColumnIndex() != 1 || row.getRowNum() == 2) {
                assertEquals(BorderStyle.THIN, cs.getBorderBottom());
                assertEquals(IndexedColors.RED.getIndex(), cs.getBottomBorderColor());
                } else {
                    assertEquals(BorderStyle.NONE, cs.getBorderBottom());
                }
                if (cell.getColumnIndex() == 0 || row.getRowNum() != 1) {
                assertEquals(BorderStyle.THIN, cs.getBorderLeft());
                assertEquals(IndexedColors.RED.getIndex(), cs.getLeftBorderColor());
                } else {
                    assertEquals(BorderStyle.NONE, cs.getBorderLeft());
                }
                if (cell.getColumnIndex() == 2 || row.getRowNum() != 1) {
                assertEquals(BorderStyle.THIN, cs.getBorderRight());
                assertEquals(IndexedColors.RED.getIndex(), cs.getRightBorderColor());
                } else {
                    assertEquals(BorderStyle.NONE, cs.getBorderRight());
                }
            }
        }
        
        wb.close();
    }
    
    @Test
    public void clonePropertyTemplate() throws IOException {
        CellRangeAddress a1c3 = new CellRangeAddress(0, 2, 0, 2);
        PropertyTemplate pt = new PropertyTemplate();
        pt.drawBorders(a1c3, BorderStyle.MEDIUM, IndexedColors.RED.getIndex(), BorderExtent.ALL);
        PropertyTemplate pt2 = new PropertyTemplate(pt);
        assertNotSame(pt2, pt);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, pt2.getNumBorderColors(i, j));
                assertEquals(4, pt2.getNumBorderColors(i, j));
            }
        }
        
        CellRangeAddress b2 = new CellRangeAddress(1,1,1,1);
        pt2.drawBorders(b2, BorderStyle.THIN, BorderExtent.ALL);
        
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        pt.applyBorders(sheet);
        
        for (Row row : sheet) {
            for (Cell cell : row) {
                CellStyle cs = cell.getCellStyle();
                assertEquals(BorderStyle.MEDIUM, cs.getBorderTop());
                assertEquals(BorderStyle.MEDIUM, cs.getBorderBottom());
                assertEquals(BorderStyle.MEDIUM, cs.getBorderLeft());
                assertEquals(BorderStyle.MEDIUM, cs.getBorderRight());
                assertEquals(IndexedColors.RED.getIndex(), cs.getTopBorderColor());
                assertEquals(IndexedColors.RED.getIndex(), cs.getBottomBorderColor());
                assertEquals(IndexedColors.RED.getIndex(), cs.getLeftBorderColor());
                assertEquals(IndexedColors.RED.getIndex(), cs.getRightBorderColor());
            }
        }
        
        wb.close();
    }
}
