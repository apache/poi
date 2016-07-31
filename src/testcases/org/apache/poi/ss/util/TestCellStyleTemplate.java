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
import org.junit.Test;

/**
 * Tests Spreadsheet CellStyleTemplate
 *
 * @see org.apache.poi.ss.util.CellStyleTemplate
 */
public final class TestCellStyleTemplate {
    @Test
    public void getNumBorders() throws IOException {
        CellRangeAddress a1 = CellRangeAddress.valueOf("A1");
        CellStyleTemplate cst = new CellStyleTemplate();
        cst.drawBorders(a1, BorderStyle.THIN, BorderExtent.TOP);
        assertEquals(1, cst.getNumBorders(0, 0));
        cst.drawBorders(a1, BorderStyle.MEDIUM, BorderExtent.BOTTOM);
        assertEquals(2, cst.getNumBorders(0, 0));
        cst.drawBorders(a1, BorderStyle.MEDIUM, BorderExtent.NONE);
        assertEquals(0, cst.getNumBorders(0, 0));
    }

    @Test
    public void getNumBorderColors() throws IOException {
        CellRangeAddress a1 = CellRangeAddress.valueOf("A1");
        CellStyleTemplate cst = new CellStyleTemplate();
        cst.drawBorderColors(a1, IndexedColors.RED.getIndex(), BorderExtent.TOP);
        assertEquals(1, cst.getNumBorderColors(0, 0));
        cst.drawBorderColors(a1, IndexedColors.RED.getIndex(), BorderExtent.BOTTOM);
        assertEquals(2, cst.getNumBorderColors(0, 0));
        cst.drawBorderColors(a1, IndexedColors.RED.getIndex(), BorderExtent.NONE);
        assertEquals(0, cst.getNumBorderColors(0, 0));
    }

    @Test
    public void getTemplateProperties() throws IOException {
        CellRangeAddress a1 = CellRangeAddress.valueOf("A1");
        CellStyleTemplate cst = new CellStyleTemplate();
        cst.drawBorders(a1, BorderStyle.THIN, BorderExtent.TOP);
        assertEquals(BorderStyle.THIN,
                cst.getTemplateProperty(0, 0, CellUtil.BORDER_TOP));
        cst.drawBorders(a1, BorderStyle.MEDIUM, BorderExtent.BOTTOM);
        assertEquals(BorderStyle.MEDIUM,
                cst.getTemplateProperty(0, 0, CellUtil.BORDER_BOTTOM));
        cst.drawBorderColors(a1, IndexedColors.RED.getIndex(), BorderExtent.TOP);
        assertEquals(IndexedColors.RED.getIndex(),
                cst.getTemplateProperty(0, 0, CellUtil.TOP_BORDER_COLOR));
        cst.drawBorderColors(a1, IndexedColors.BLUE.getIndex(), BorderExtent.BOTTOM);
        assertEquals(IndexedColors.BLUE.getIndex(),
                cst.getTemplateProperty(0, 0, CellUtil.BOTTOM_BORDER_COLOR));
    }

    @Test
    public void drawBorders() throws IOException {
        CellRangeAddress a1c3 = CellRangeAddress.valueOf("A1:C3");
        CellStyleTemplate cst = new CellStyleTemplate();
        cst.drawBorders(a1c3, BorderStyle.THIN,
                BorderExtent.ALL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, cst.getNumBorders(i, j));
                assertEquals(BorderStyle.THIN,
                        cst.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                assertEquals(BorderStyle.THIN,
                        cst.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                assertEquals(BorderStyle.THIN,
                        cst.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                assertEquals(BorderStyle.THIN,
                        cst.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
            }
        }
        cst.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.OUTSIDE);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, cst.getNumBorders(i, j));
                if (i == 0) {
                    if (j == 0) {
                        assertEquals(BorderStyle.MEDIUM,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.MEDIUM,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_RIGHT));
                    } else if (j == 2) {
                        assertEquals(BorderStyle.MEDIUM,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.MEDIUM,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_RIGHT));
                    } else {
                        assertEquals(BorderStyle.MEDIUM,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_RIGHT));
                    }
                } else if (i == 2) {
                    if (j == 0) {
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.MEDIUM,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.MEDIUM,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_RIGHT));
                    } else if (j == 2) {
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.MEDIUM,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.MEDIUM,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_RIGHT));
                    } else {
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.MEDIUM,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_RIGHT));
                    }
                } else {
                    if (j == 0) {
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.MEDIUM,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_RIGHT));
                    } else if (j == 2) {
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.MEDIUM,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_RIGHT));
                    } else {
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_TOP));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_BOTTOM));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_LEFT));
                        assertEquals(BorderStyle.THIN,
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BORDER_RIGHT));
                    }
                }
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(0, cst.getNumBorders(i, j));
            }
        }
        cst.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.TOP);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM,
                            cst.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                } else {
                    assertEquals(0, cst.getNumBorders(i, j));
                }
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.BOTTOM);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 2) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM, cst
                            .getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                } else {
                    assertEquals(0, cst.getNumBorders(i, j));
                }
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.LEFT);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM,
                            cst.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                } else {
                    assertEquals(0, cst.getNumBorders(i, j));
                }
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.RIGHT);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 2) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM, cst
                            .getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                } else {
                    assertEquals(0, cst.getNumBorders(i, j));
                }
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(2, cst.getNumBorders(i, j));
                assertEquals(BorderStyle.MEDIUM,
                        cst.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                assertEquals(BorderStyle.MEDIUM,
                        cst.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.INSIDE_HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM, cst
                            .getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                } else if (i == 2) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM,
                            cst.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                } else {
                    assertEquals(2, cst.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM,
                            cst.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                    assertEquals(BorderStyle.MEDIUM, cst
                            .getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                }
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.OUTSIDE_HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM,
                            cst.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                } else if (i == 2) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM, cst
                            .getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                } else {
                    assertEquals(0, cst.getNumBorders(i, j));
                }
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(2, cst.getNumBorders(i, j));
                assertEquals(BorderStyle.MEDIUM,
                        cst.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                assertEquals(BorderStyle.MEDIUM,
                        cst.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.INSIDE_VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM, cst
                            .getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                } else if (j == 2) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM,
                            cst.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                } else {
                    assertEquals(2, cst.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM,
                            cst.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                    assertEquals(BorderStyle.MEDIUM, cst
                            .getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                }
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorders(a1c3, BorderStyle.MEDIUM,
                BorderExtent.OUTSIDE_VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM,
                            cst.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                } else if (j == 2) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(BorderStyle.MEDIUM, cst
                            .getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                } else {
                    assertEquals(0, cst.getNumBorders(i, j));
                }
            }
        }
    }

    @Test
    public void drawBorderColors() throws IOException {
        CellRangeAddress a1c3 = CellRangeAddress.valueOf("A1:C3");
        CellStyleTemplate cst = new CellStyleTemplate();
        cst.drawBorderColors(a1c3, IndexedColors.RED.getIndex(),
                BorderExtent.ALL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, cst.getNumBorders(i, j));
                assertEquals(4, cst.getNumBorderColors(i, j));
                assertEquals(IndexedColors.RED.getIndex(), cst
                        .getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                assertEquals(IndexedColors.RED.getIndex(),
                        cst.getTemplateProperty(i, j,
                                CellUtil.BOTTOM_BORDER_COLOR));
                assertEquals(IndexedColors.RED.getIndex(), cst
                        .getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                assertEquals(IndexedColors.RED.getIndex(),
                        cst.getTemplateProperty(i, j,
                                CellUtil.RIGHT_BORDER_COLOR));
            }
        }
        cst.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.OUTSIDE);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, cst.getNumBorders(i, j));
                assertEquals(4, cst.getNumBorderColors(i, j));
                if (i == 0) {
                    if (j == 0) {
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    } else if (j == 2) {
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    } else {
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    }
                } else if (i == 2) {
                    if (j == 0) {
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    } else if (j == 2) {
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    } else {
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    }
                } else {
                    if (j == 0) {
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    } else if (j == 2) {
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.BLUE.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    } else {
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.TOP_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.BOTTOM_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.LEFT_BORDER_COLOR));
                        assertEquals(IndexedColors.RED.getIndex(),
                                cst.getTemplateProperty(i, j,
                                        CellUtil.RIGHT_BORDER_COLOR));
                    }
                }
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(0, cst.getNumBorders(i, j));
                assertEquals(0, cst.getNumBorderColors(i, j));
            }
        }
        cst.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.TOP);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(1, cst.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            cst.getTemplateProperty(i, j,
                                    CellUtil.TOP_BORDER_COLOR));
                } else {
                    assertEquals(0, cst.getNumBorders(i, j));
                    assertEquals(0, cst.getNumBorderColors(i, j));
                }
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.BOTTOM);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 2) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(1, cst.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            cst.getTemplateProperty(i, j,
                                    CellUtil.BOTTOM_BORDER_COLOR));
                } else {
                    assertEquals(0, cst.getNumBorders(i, j));
                    assertEquals(0, cst.getNumBorderColors(i, j));
                }
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.LEFT);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(1, cst.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            cst.getTemplateProperty(i, j,
                                    CellUtil.LEFT_BORDER_COLOR));
                } else {
                    assertEquals(0, cst.getNumBorders(i, j));
                    assertEquals(0, cst.getNumBorderColors(i, j));
                }
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.RIGHT);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 2) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(1, cst.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            cst.getTemplateProperty(i, j,
                                    CellUtil.RIGHT_BORDER_COLOR));
                } else {
                    assertEquals(0, cst.getNumBorders(i, j));
                    assertEquals(0, cst.getNumBorderColors(i, j));
                }
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(2, cst.getNumBorders(i, j));
                assertEquals(2, cst.getNumBorderColors(i, j));
                assertEquals(IndexedColors.BLUE.getIndex(), cst
                        .getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                assertEquals(IndexedColors.BLUE.getIndex(),
                        cst.getTemplateProperty(i, j,
                                CellUtil.BOTTOM_BORDER_COLOR));
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.INSIDE_HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(1, cst.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            cst.getTemplateProperty(i, j,
                                    CellUtil.BOTTOM_BORDER_COLOR));
                } else if (i == 2) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(1, cst.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            cst.getTemplateProperty(i, j,
                                    CellUtil.TOP_BORDER_COLOR));
                } else {
                    assertEquals(2, cst.getNumBorders(i, j));
                    assertEquals(2, cst.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            cst.getTemplateProperty(i, j,
                                    CellUtil.TOP_BORDER_COLOR));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            cst.getTemplateProperty(i, j,
                                    CellUtil.BOTTOM_BORDER_COLOR));
                }
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.OUTSIDE_HORIZONTAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (i == 0) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(1, cst.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            cst.getTemplateProperty(i, j,
                                    CellUtil.TOP_BORDER_COLOR));
                } else if (i == 2) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(1, cst.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            cst.getTemplateProperty(i, j,
                                    CellUtil.BOTTOM_BORDER_COLOR));
                } else {
                    assertEquals(0, cst.getNumBorders(i, j));
                    assertEquals(0, cst.getNumBorderColors(i, j));
                }
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(2, cst.getNumBorders(i, j));
                assertEquals(2, cst.getNumBorderColors(i, j));
                assertEquals(IndexedColors.BLUE.getIndex(), cst
                        .getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                assertEquals(IndexedColors.BLUE.getIndex(),
                        cst.getTemplateProperty(i, j,
                                CellUtil.RIGHT_BORDER_COLOR));
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.INSIDE_VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(1, cst.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            cst.getTemplateProperty(i, j,
                                    CellUtil.RIGHT_BORDER_COLOR));
                } else if (j == 2) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(1, cst.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            cst.getTemplateProperty(i, j,
                                    CellUtil.LEFT_BORDER_COLOR));
                } else {
                    assertEquals(2, cst.getNumBorders(i, j));
                    assertEquals(2, cst.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            cst.getTemplateProperty(i, j,
                                    CellUtil.LEFT_BORDER_COLOR));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            cst.getTemplateProperty(i, j,
                                    CellUtil.RIGHT_BORDER_COLOR));
                }
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE,
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.AUTOMATIC.getIndex(),
                BorderExtent.NONE);
        cst.drawBorderColors(a1c3, IndexedColors.BLUE.getIndex(),
                BorderExtent.OUTSIDE_VERTICAL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                if (j == 0) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(1, cst.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            cst.getTemplateProperty(i, j,
                                    CellUtil.LEFT_BORDER_COLOR));
                } else if (j == 2) {
                    assertEquals(1, cst.getNumBorders(i, j));
                    assertEquals(1, cst.getNumBorderColors(i, j));
                    assertEquals(IndexedColors.BLUE.getIndex(),
                            cst.getTemplateProperty(i, j,
                                    CellUtil.RIGHT_BORDER_COLOR));
                } else {
                    assertEquals(0, cst.getNumBorders(i, j));
                    assertEquals(0, cst.getNumBorderColors(i, j));
                }
            }
        }
    }
    
    @Test
    public void drawBordersWithColors() throws IOException {
        CellRangeAddress a1c3 = CellRangeAddress.valueOf("A1:C3");
        CellStyleTemplate cst = new CellStyleTemplate();
        
        cst.drawBorders(a1c3, BorderStyle.MEDIUM, IndexedColors.RED.getIndex(), BorderExtent.ALL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, cst.getNumBorders(i, j));
                assertEquals(4, cst.getNumBorderColors(i, j));
                assertEquals(BorderStyle.MEDIUM,
                        cst.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                assertEquals(BorderStyle.MEDIUM,
                        cst.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                assertEquals(BorderStyle.MEDIUM,
                        cst.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                assertEquals(BorderStyle.MEDIUM,
                        cst.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
                assertEquals(IndexedColors.RED.getIndex(), cst
                        .getTemplateProperty(i, j, CellUtil.TOP_BORDER_COLOR));
                assertEquals(IndexedColors.RED.getIndex(),
                        cst.getTemplateProperty(i, j,
                                CellUtil.BOTTOM_BORDER_COLOR));
                assertEquals(IndexedColors.RED.getIndex(), cst
                        .getTemplateProperty(i, j, CellUtil.LEFT_BORDER_COLOR));
                assertEquals(IndexedColors.RED.getIndex(),
                        cst.getTemplateProperty(i, j,
                                CellUtil.RIGHT_BORDER_COLOR));
            }
        }
        cst.drawBorders(a1c3, BorderStyle.NONE, BorderExtent.NONE);
        cst.drawBorders(a1c3, BorderStyle.NONE, IndexedColors.RED.getIndex(), BorderExtent.ALL);
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                assertEquals(4, cst.getNumBorders(i, j));
                assertEquals(0, cst.getNumBorderColors(i, j));
                assertEquals(BorderStyle.NONE,
                        cst.getTemplateProperty(i, j, CellUtil.BORDER_TOP));
                assertEquals(BorderStyle.NONE,
                        cst.getTemplateProperty(i, j, CellUtil.BORDER_BOTTOM));
                assertEquals(BorderStyle.NONE,
                        cst.getTemplateProperty(i, j, CellUtil.BORDER_LEFT));
                assertEquals(BorderStyle.NONE,
                        cst.getTemplateProperty(i, j, CellUtil.BORDER_RIGHT));
            }
        }
    }

    @Test
    public void applyBorders() throws IOException {
        CellRangeAddress a1c3 = CellRangeAddress.valueOf("A1:C3");
        CellRangeAddress b2 = CellRangeAddress.valueOf("B2");
        CellStyleTemplate cst = new CellStyleTemplate();
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet();
        
        cst.drawBorders(a1c3, BorderStyle.THIN, IndexedColors.RED.getIndex(), BorderExtent.ALL);
        cst.applyBorders(sheet);
        
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
        
        cst.drawBorders(b2, BorderStyle.NONE, BorderExtent.ALL);
        cst.applyBorders(sheet);
        
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
}
