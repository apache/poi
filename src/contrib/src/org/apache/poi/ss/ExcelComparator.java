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
package org.apache.poi.ss;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

/**
 * Utility to compare Excel File Contents cell by cell for all sheets.
 * 
 * <p>This utility will be used to compare Excel File Contents cell by cell for all sheets programmatically.</p>
 * 
 * <p>Below are the list of Attribute comparison supported in this version.</p>
 * 
 * <ul>
 * <li>Cell Alignment</li>
 * <li>Cell Border Attributes</li>
 * <li>Cell Data</li>
 * <li>Cell Data-Type</li>
 * <li>Cell Fill Color</li>
 * <li>Cell Fill pattern</li>
 * <li>Cell Font Attributes</li>
 * <li>Cell Font Family</li>
 * <li>Cell Font Size</li>
 * <li>Cell Protection</li>
 * <li>Name of the sheets</li>
 * <li>Number of Columns</li>
 * <li>Number of Rows</li>
 * <li>Number of Sheet</li>
 * </ul>
 * 
 * <p>(Some of the above attribute comparison only work for *.xlsx format currently. In future it can be enhanced.)</p>
 * 
 * <p><b>Usage:</b></p>
 * 
 * <pre>
 * {@code
 *  Workbook wb1 = WorkbookFactory.create(new File("workBook1.xls"));
 *  Workbook wb2 = WorkbookFactory.create(new File("workBook2.xls"));
 *  ExcelFileDifference excelFileDifference = ExcelComparator.compare(wb1, wb2);
 *  for (String differences : excelFileDifference.listOfDifferences)
 *      System.out.println(differences);
 *  System.out.println("DifferenceFound = "+ excelFileDifference.isDifferenceFound);
 *  }
 * </pre>
 */
public class ExcelComparator {

    private static final String BOLD = "BOLD";
    private static final String BOTTOM_BORDER = "BOTTOM BORDER";
    private static final String BRACKET_END = "]";
    private static final String BRACKET_START = " [";
    private static final String CELL_ALIGNMENT_DOES_NOT_MATCH = "Cell Alignment does not Match ::";
    private static final String CELL_BORDER_ATTRIBUTES_DOES_NOT_MATCH = "Cell Border Attributes does not Match ::";
    private static final String CELL_DATA_DOES_NOT_MATCH = "Cell Data does not Match ::";
    private static final String CELL_DATA_TYPE_DOES_NOT_MATCH = "Cell Data-Type does not Match in :: ";
    private static final String CELL_FILL_COLOR_DOES_NOT_MATCH = "Cell Fill Color does not Match ::";
    private static final String CELL_FILL_PATTERN_DOES_NOT_MATCH = "Cell Fill pattern does not Match ::";
    private static final String CELL_FONT_ATTRIBUTES_DOES_NOT_MATCH = "Cell Font Attributes does not Match ::";
    private static final String CELL_FONT_FAMILY_DOES_NOT_MATCH = "Cell Font Family does not Match ::";
    private static final String CELL_FONT_SIZE_DOES_NOT_MATCH = "Cell Font Size does not Match ::";
    private static final String CELL_PROTECTION_DOES_NOT_MATCH = "Cell Protection does not Match ::";
    private static final String ITALICS = "ITALICS";
    private static final String LEFT_BORDER = "LEFT BORDER";
    private static final String LINE_SEPARATOR = "line.separator";
    private static final String NAME_OF_THE_SHEETS_DO_NOT_MATCH = "Name of the sheets do not match :: ";
    private static final String NEXT_STR = " -> ";
    private static final String NO_BOTTOM_BORDER = "NO BOTTOM BORDER";
    private static final String NO_COLOR = "NO COLOR";
    private static final String NO_LEFT_BORDER = "NO LEFT BORDER";
    private static final String NO_RIGHT_BORDER = "NO RIGHT BORDER";
    private static final String NO_TOP_BORDER = "NO TOP BORDER";
    private static final String NOT_BOLD = "NOT BOLD";
    private static final String NOT_EQUALS = " != ";
    private static final String NOT_ITALICS = "NOT ITALICS";
    private static final String NOT_UNDERLINE = "NOT UNDERLINE";
    private static final String NUMBER_OF_COLUMNS_DOES_NOT_MATCH = "Number Of Columns does not Match :: ";
    private static final String NUMBER_OF_ROWS_DOES_NOT_MATCH = "Number Of Rows does not Match :: ";
    private static final String NUMBER_OF_SHEETS_DO_NOT_MATCH = "Number of Sheets do not match :: ";
    private static final String RIGHT_BORDER = "RIGHT BORDER";
    private static final String TOP_BORDER = "TOP BORDER";
    private static final String UNDERLINE = "UNDERLINE";
    private static final String WORKBOOK1 = "workbook1";
    private static final String WORKBOOK2 = "workbook2";

    /**
     * Utility to compare Excel File Contents cell by cell for all sheets.
     *
     * @param workbook1
     *            the workbook1
     * @param workbook2
     *            the workbook2
     * @return the Excel file difference containing a flag and a list of
     *         differences
     * @throws ExcelCompareException
     *             the excel compare exception
     */
    public static ExcelFileDifference compare(Workbook workbook1,
            Workbook workbook2) {
        List<String> listOfDifferences = compareWorkBookContents(workbook1,
                workbook2);
        return populateListOfDifferences(listOfDifferences);
    }

    /**
     * Compare work book contents.
     *
     * @param workbook1
     *            the workbook1
     * @param workbook2
     *            the workbook2
     * @return the list
     */
    private static List<String> compareWorkBookContents(Workbook workbook1,
            Workbook workbook2) {
        ExcelComparator excelComparator = new ExcelComparator();
        List<String> listOfDifferences = new ArrayList<String>();
        excelComparator.compareNumberOfSheets(workbook1, workbook2,
                listOfDifferences);
        excelComparator.compareSheetNames(workbook1, workbook2,
                listOfDifferences);
        excelComparator.compareSheetData(workbook1, workbook2,
                listOfDifferences);
        return listOfDifferences;
    }

    /**
     * Populate list of differences.
     *
     * @param listOfDifferences
     *            the list of differences
     * @return the excel file difference
     */
    private static ExcelFileDifference populateListOfDifferences(
            List<String> listOfDifferences) {
        ExcelFileDifference excelFileDifference = new ExcelFileDifference();
        excelFileDifference.isDifferenceFound = listOfDifferences.size() > 0;
        excelFileDifference.listOfDifferences = listOfDifferences;
        return excelFileDifference;
    }

    /**
     * Compare data in all sheets.
     *
     * @param workbook1
     *            the workbook1
     * @param workbook2
     *            the workbook2
     * @param listOfDifferences
     *            the list of differences
     * @throws ExcelCompareException
     *             the excel compare exception
     */
    private void compareDataInAllSheets(Workbook workbook1, Workbook workbook2,
            List<String> listOfDifferences) {
        for (int i = 0; i < workbook1.getNumberOfSheets(); i++) {
            Sheet sheetWorkBook1 = workbook1.getSheetAt(i);
            Sheet sheetWorkBook2;
            if (workbook2.getNumberOfSheets() > i) {
                sheetWorkBook2 = workbook2.getSheetAt(i);
            } else {
                sheetWorkBook2 = null;
            }

            for (int j = 0; j < sheetWorkBook1.getPhysicalNumberOfRows(); j++) {
                Row rowWorkBook1 = sheetWorkBook1.getRow(j);
                Row rowWorkBook2;
                if (sheetWorkBook2 != null) {
                    rowWorkBook2 = sheetWorkBook2.getRow(j);
                } else {
                    rowWorkBook2 = null;
                }

                if ((rowWorkBook1 == null) || (rowWorkBook2 == null)) {
                    continue;
                }
                for (int k = 0; k < rowWorkBook1.getLastCellNum(); k++) {
                    Cell cellWorkBook1 = rowWorkBook1.getCell(k);
                    Cell cellWorkBook2 = rowWorkBook2.getCell(k);

                    if (!((null == cellWorkBook1) || (null == cellWorkBook2))) {
                        if (isCellTypeMatches(cellWorkBook1, cellWorkBook2)) {

                            listOfDifferences.add(getMessage(workbook1,
                                    workbook2, i, cellWorkBook1, cellWorkBook2,
                                    CELL_DATA_TYPE_DOES_NOT_MATCH,
                                    cellWorkBook1.getCellType() + "",
                                    cellWorkBook2.getCellType() + ""));
                        }

                        if (isCellContentTypeBlank(cellWorkBook1)) {
                            if (isCellContentMatches(cellWorkBook1,
                                    cellWorkBook2)) {

                                listOfDifferences.add(getMessage(workbook1,
                                        workbook2, i, cellWorkBook1,
                                        cellWorkBook2,
                                        CELL_DATA_DOES_NOT_MATCH,
                                        cellWorkBook1.getRichStringCellValue()
                                                + "",
                                        cellWorkBook2.getRichStringCellValue()
                                                + ""));

                            }

                        } else if (isCellContentTypeBoolean(cellWorkBook1)) {
                            if (isCellContentMatchesForBoolean(cellWorkBook1,
                                    cellWorkBook2)) {
                                listOfDifferences.add(getMessage(workbook1,
                                        workbook2, i, cellWorkBook1,
                                        cellWorkBook2,
                                        CELL_DATA_DOES_NOT_MATCH,
                                        cellWorkBook1.getBooleanCellValue()
                                                + "",
                                        cellWorkBook2.getBooleanCellValue()
                                                + ""));

                            }

                        } else if (isCellContentInError(cellWorkBook1)) {
                            if (isCellContentMatches(cellWorkBook1,
                                    cellWorkBook2)) {

                                listOfDifferences.add(getMessage(workbook1,
                                        workbook2, i, cellWorkBook1,
                                        cellWorkBook2,
                                        CELL_DATA_DOES_NOT_MATCH,
                                        cellWorkBook1.getRichStringCellValue()
                                                + "",
                                        cellWorkBook2.getRichStringCellValue()
                                                + ""));

                            }
                        } else if (isCellContentFormula(cellWorkBook1)) {
                            if (isCellContentMatchesForFormula(cellWorkBook1,
                                    cellWorkBook2)) {

                                listOfDifferences.add(getMessage(workbook1,
                                        workbook2, i, cellWorkBook1,
                                        cellWorkBook2,
                                        CELL_DATA_DOES_NOT_MATCH,
                                        cellWorkBook1.getCellFormula() + "",
                                        cellWorkBook2.getCellFormula() + ""));

                            }

                        } else if (isCellContentTypeNumeric(cellWorkBook1)) {
                            if (DateUtil.isCellDateFormatted(cellWorkBook1)) {
                                if (isCellContentMatchesForDate(cellWorkBook1,
                                        cellWorkBook2)) {
                                    listOfDifferences.add(getMessage(workbook1,
                                            workbook2, i, cellWorkBook1,
                                            cellWorkBook2,
                                            CELL_DATA_DOES_NOT_MATCH,
                                            cellWorkBook1.getDateCellValue()
                                                    + "",
                                            cellWorkBook2.getDateCellValue()
                                                    + ""));

                                }
                            } else {
                                if (isCellContentMatchesForNumeric(
                                        cellWorkBook1, cellWorkBook2)) {
                                    listOfDifferences.add(getMessage(workbook1,
                                            workbook2, i, cellWorkBook1,
                                            cellWorkBook2,
                                            CELL_DATA_DOES_NOT_MATCH,
                                            cellWorkBook1.getNumericCellValue()
                                                    + "",
                                            cellWorkBook2.getNumericCellValue()
                                                    + ""));

                                }
                            }

                        } else if (isCellContentTypeString(cellWorkBook1)) {
                            if (isCellContentMatches(cellWorkBook1,
                                    cellWorkBook2)) {
                                listOfDifferences.add(getMessage(workbook1,
                                        workbook2, i, cellWorkBook1,
                                        cellWorkBook2,
                                        CELL_DATA_DOES_NOT_MATCH, cellWorkBook1
                                                .getRichStringCellValue()
                                                .getString(), cellWorkBook2
                                                .getRichStringCellValue()
                                                .getString()));
                            }
                        }

                        if (isCellFillPatternMatches(cellWorkBook1,
                                cellWorkBook2)) {
                            listOfDifferences.add(getMessage(workbook1,
                                    workbook2, i, cellWorkBook1, cellWorkBook2,
                                    CELL_FILL_PATTERN_DOES_NOT_MATCH,
                                    cellWorkBook1.getCellStyle()
                                            .getFillPattern() + "",
                                    cellWorkBook2.getCellStyle()
                                            .getFillPattern() + ""));

                        }

                        if (isCellAlignmentMatches(cellWorkBook1, cellWorkBook2)) {
                            listOfDifferences.add(getMessage(workbook1,
                                    workbook2, i, cellWorkBook1, cellWorkBook2,
                                    CELL_ALIGNMENT_DOES_NOT_MATCH,
                                    cellWorkBook1.getRichStringCellValue()
                                            .getString(), cellWorkBook2
                                            .getRichStringCellValue()
                                            .getString()));

                        }

                        if (isCellHiddenMatches(cellWorkBook1, cellWorkBook2)) {
                            listOfDifferences
                                    .add(getMessage(workbook1, workbook2, i,
                                            cellWorkBook1, cellWorkBook2,
                                            CELL_PROTECTION_DOES_NOT_MATCH,
                                            cellWorkBook1.getCellStyle()
                                                    .getHidden() ? "HIDDEN"
                                                    : "NOT HIDDEN",
                                            cellWorkBook2.getCellStyle()
                                                    .getHidden() ? "HIDDEN"
                                                    : "NOT HIDDEN"));

                        }

                        if (isCellLockedMatches(cellWorkBook1, cellWorkBook2)) {
                            listOfDifferences
                                    .add(getMessage(workbook1, workbook2, i,
                                            cellWorkBook1, cellWorkBook2,
                                            CELL_PROTECTION_DOES_NOT_MATCH,
                                            cellWorkBook1.getCellStyle()
                                                    .getLocked() ? "LOCKED"
                                                    : "NOT LOCKED",
                                            cellWorkBook2.getCellStyle()
                                                    .getLocked() ? "LOCKED"
                                                    : "NOT LOCKED"));

                        }

                        if (isCellFontFamilyMatches(cellWorkBook1,
                                cellWorkBook2)) {
                            listOfDifferences.add(getMessage(workbook1,
                                    workbook2, i, cellWorkBook1, cellWorkBook2,
                                    CELL_FONT_FAMILY_DOES_NOT_MATCH,
                                    ((XSSFCellStyle) cellWorkBook1
                                            .getCellStyle()).getFont()
                                            .getFontName(),
                                    ((XSSFCellStyle) cellWorkBook2
                                            .getCellStyle()).getFont()
                                            .getFontName()));

                        }

                        if (isCellFontSizeMatches(cellWorkBook1, cellWorkBook2)) {
                            listOfDifferences.add(getMessage(
                                    workbook1,
                                    workbook2,
                                    i,
                                    cellWorkBook1,
                                    cellWorkBook2,
                                    CELL_FONT_SIZE_DOES_NOT_MATCH,
                                    ((XSSFCellStyle) cellWorkBook1
                                            .getCellStyle()).getFont()
                                            .getFontHeightInPoints()
                                            + "",
                                    ((XSSFCellStyle) cellWorkBook2
                                            .getCellStyle()).getFont()
                                            .getFontHeightInPoints()
                                            + ""));

                        }

                        if (isCellFontBoldMatches(cellWorkBook1, cellWorkBook2)) {
                            listOfDifferences.add(getMessage(workbook1,
                                    workbook2, i, cellWorkBook1, cellWorkBook2,
                                    CELL_FONT_ATTRIBUTES_DOES_NOT_MATCH,
                                    ((XSSFCellStyle) cellWorkBook1
                                            .getCellStyle()).getFont()
                                            .getBold() ? BOLD : NOT_BOLD,
                                    ((XSSFCellStyle) cellWorkBook2
                                            .getCellStyle()).getFont()
                                            .getBold() ? BOLD : NOT_BOLD));

                        }

                        if (isCellUnderLineMatches(cellWorkBook1, cellWorkBook2)) {
                            listOfDifferences.add(getMessage(workbook1,
                                    workbook2, i, cellWorkBook1, cellWorkBook2,
                                    CELL_FONT_ATTRIBUTES_DOES_NOT_MATCH,
                                    ((XSSFCellStyle) cellWorkBook1
                                            .getCellStyle()).getFont()
                                            .getUnderline() == 1 ? UNDERLINE
                                            : NOT_UNDERLINE,
                                    ((XSSFCellStyle) cellWorkBook2
                                            .getCellStyle()).getFont()
                                            .getUnderline() == 1 ? UNDERLINE
                                            : NOT_UNDERLINE));

                        }

                        if (isCellFontItalicsMatches(cellWorkBook1,
                                cellWorkBook2)) {
                            listOfDifferences.add(getMessage(workbook1,
                                    workbook2, i, cellWorkBook1, cellWorkBook2,
                                    CELL_FONT_ATTRIBUTES_DOES_NOT_MATCH,
                                    ((XSSFCellStyle) cellWorkBook1
                                            .getCellStyle()).getFont()
                                            .getItalic() ? ITALICS
                                            : NOT_ITALICS,
                                    ((XSSFCellStyle) cellWorkBook2
                                            .getCellStyle()).getFont()
                                            .getItalic() ? ITALICS
                                            : NOT_ITALICS));

                        }

                        if (isCellBorderBottomMatches(cellWorkBook1,
                                cellWorkBook2)) {
                            listOfDifferences
                                    .add(getMessage(
                                            workbook1,
                                            workbook2,
                                            i,
                                            cellWorkBook1,
                                            cellWorkBook2,
                                            CELL_BORDER_ATTRIBUTES_DOES_NOT_MATCH,
                                            ((XSSFCellStyle) cellWorkBook1
                                                    .getCellStyle())
                                                    .getBorderBottom() == 1 ? BOTTOM_BORDER
                                                    : NO_BOTTOM_BORDER,
                                            ((XSSFCellStyle) cellWorkBook2
                                                    .getCellStyle())
                                                    .getBorderBottom() == 1 ? BOTTOM_BORDER
                                                    : NO_BOTTOM_BORDER));

                        }

                        if (isCellBorderLeftMatches(cellWorkBook1,
                                cellWorkBook2)) {
                            listOfDifferences
                                    .add(getMessage(
                                            workbook1,
                                            workbook2,
                                            i,
                                            cellWorkBook1,
                                            cellWorkBook2,
                                            CELL_BORDER_ATTRIBUTES_DOES_NOT_MATCH,
                                            ((XSSFCellStyle) cellWorkBook1
                                                    .getCellStyle())
                                                    .getBorderLeft() == 1 ? LEFT_BORDER
                                                    : NO_LEFT_BORDER,
                                            ((XSSFCellStyle) cellWorkBook2
                                                    .getCellStyle())
                                                    .getBorderLeft() == 1 ? LEFT_BORDER
                                                    : NO_LEFT_BORDER));

                        }

                        if (isCellBorderRightMatches(cellWorkBook1,
                                cellWorkBook2)) {
                            listOfDifferences
                                    .add(getMessage(
                                            workbook1,
                                            workbook2,
                                            i,
                                            cellWorkBook1,
                                            cellWorkBook2,
                                            CELL_BORDER_ATTRIBUTES_DOES_NOT_MATCH,
                                            ((XSSFCellStyle) cellWorkBook1
                                                    .getCellStyle())
                                                    .getBorderRight() == 1 ? RIGHT_BORDER
                                                    : NO_RIGHT_BORDER,
                                            ((XSSFCellStyle) cellWorkBook2
                                                    .getCellStyle())
                                                    .getBorderRight() == 1 ? RIGHT_BORDER
                                                    : NO_RIGHT_BORDER));

                        }

                        if (isCellBorderTopMatches(cellWorkBook1, cellWorkBook2)) {
                            listOfDifferences
                                    .add(getMessage(
                                            workbook1,
                                            workbook2,
                                            i,
                                            cellWorkBook1,
                                            cellWorkBook2,
                                            CELL_BORDER_ATTRIBUTES_DOES_NOT_MATCH,
                                            ((XSSFCellStyle) cellWorkBook1
                                                    .getCellStyle())
                                                    .getBorderTop() == 1 ? TOP_BORDER
                                                    : NO_TOP_BORDER,
                                            ((XSSFCellStyle) cellWorkBook2
                                                    .getCellStyle())
                                                    .getBorderTop() == 1 ? TOP_BORDER
                                                    : NO_TOP_BORDER));

                        }

                        if (isCellBackGroundFillMatchesAndEmpty(cellWorkBook1,
                                cellWorkBook2)) {
                            continue;
                        } else if (isCellFillBackGroundMatchesAndEitherEmpty(
                                cellWorkBook1, cellWorkBook2)) {
                            listOfDifferences.add(getMessage(
                                    workbook1,
                                    workbook2,
                                    i,
                                    cellWorkBook1,
                                    cellWorkBook2,
                                    CELL_FILL_COLOR_DOES_NOT_MATCH,
                                    NO_COLOR,
                                    ((XSSFColor) cellWorkBook2.getCellStyle()
                                            .getFillForegroundColorColor())
                                            .getARGBHex()
                                            + ""));

                        } else if (isCellFillBackGroundMatchesAndSecondEmpty(
                                cellWorkBook1, cellWorkBook2)) {
                            listOfDifferences.add(getMessage(
                                    workbook1,
                                    workbook2,
                                    i,
                                    cellWorkBook1,
                                    cellWorkBook2,
                                    CELL_FILL_COLOR_DOES_NOT_MATCH,
                                    ((XSSFColor) cellWorkBook1.getCellStyle()
                                            .getFillForegroundColorColor())
                                            .getARGBHex()
                                            + "", NO_COLOR));

                        } else {
                            if (isCellFileBackGroundMatches(cellWorkBook1,
                                    cellWorkBook2)) {
                                listOfDifferences.add(getMessage(
                                        workbook1,
                                        workbook2,
                                        i,
                                        cellWorkBook1,
                                        cellWorkBook2,
                                        CELL_FILL_COLOR_DOES_NOT_MATCH,
                                        ((XSSFColor) cellWorkBook1
                                                .getCellStyle()
                                                .getFillForegroundColorColor())
                                                .getARGBHex()
                                                + "",
                                        ((XSSFColor) cellWorkBook2
                                                .getCellStyle()
                                                .getFillForegroundColorColor())
                                                .getARGBHex()
                                                + ""));

                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * Compare number of columns in sheets.
     *
     * @param workbook1
     *            the workbook1
     * @param workbook2
     *            the workbook2
     * @param listOfDifferences
     *            the list of differences
     * @throws ExcelCompareException
     *             the excel compare exception
     */
    private void compareNumberOfColumnsInSheets(Workbook workbook1,
            Workbook workbook2, List<String> listOfDifferences) {
        for (int i = 0; i < workbook1.getNumberOfSheets(); i++) {
            Sheet sheetWorkBook1 = workbook1.getSheetAt(i);
            Sheet sheetWorkBook2;
            if (workbook2.getNumberOfSheets() > i) {
                sheetWorkBook2 = workbook2.getSheetAt(i);
            } else {
                sheetWorkBook2 = null;
            }
            if (isWorkBookEmpty(sheetWorkBook1, sheetWorkBook2)) {
                if (isNumberOfColumnsMatches(sheetWorkBook1, sheetWorkBook2)) {
                    String noOfCols;
                    String sheetName;
                    if (sheetWorkBook2 != null) {
                        noOfCols = sheetWorkBook2.getRow(0).getLastCellNum()
                                + "";
                        sheetName = workbook2.getSheetName(i);
                    } else {
                        noOfCols = "";
                        sheetName = "";
                    }
                    short lastCellNumForWbk1 = sheetWorkBook1.getRow(0) != null ? sheetWorkBook1
                            .getRow(0).getLastCellNum() : 0;
                    listOfDifferences.add(NUMBER_OF_COLUMNS_DOES_NOT_MATCH
                            + System.getProperty(LINE_SEPARATOR) + WORKBOOK1
                            + NEXT_STR + workbook1.getSheetName(i) + NEXT_STR
                            + lastCellNumForWbk1 + NOT_EQUALS + WORKBOOK2
                            + NEXT_STR + sheetName + NEXT_STR + noOfCols);
                }
            }
        }
    }

    /**
     * Compare number of rows in sheets.
     *
     * @param workbook1
     *            the workbook1
     * @param workbook2
     *            the workbook2
     * @param listOfDifferences
     *            the list of differences
     * @throws ExcelCompareException
     *             the excel compare exception
     */
    private void compareNumberOfRowsInSheets(Workbook workbook1,
            Workbook workbook2, List<String> listOfDifferences) {
        for (int i = 0; i < workbook1.getNumberOfSheets(); i++) {
            Sheet sheetWorkBook1 = workbook1.getSheetAt(i);
            Sheet sheetWorkBook2;
            if (workbook2.getNumberOfSheets() > i) {
                sheetWorkBook2 = workbook2.getSheetAt(i);
            } else {
                sheetWorkBook2 = null;
            }
            if (isNumberOfRowsMatches(sheetWorkBook1, sheetWorkBook2)) {
                String noOfRows;
                String sheetName;
                if (sheetWorkBook2 != null) {
                    noOfRows = sheetWorkBook2.getPhysicalNumberOfRows() + "";
                    sheetName = workbook2.getSheetName(i);
                } else {
                    noOfRows = "";
                    sheetName = "";
                }
                listOfDifferences.add(NUMBER_OF_ROWS_DOES_NOT_MATCH
                        + System.getProperty(LINE_SEPARATOR) + WORKBOOK1
                        + NEXT_STR + workbook1.getSheetName(i) + NEXT_STR
                        + sheetWorkBook1.getPhysicalNumberOfRows() + NOT_EQUALS
                        + WORKBOOK2 + NEXT_STR + sheetName + NEXT_STR
                        + noOfRows);
            }
        }

    }

    /**
     * Compare number of sheets.
     *
     * @param workbook1
     *            the workbook1
     * @param workbook2
     *            the workbook2
     * @param listOfDifferences
     *
     * @throws ExcelCompareException
     *             the excel compare exception
     */
    private void compareNumberOfSheets(Workbook workbook1, Workbook workbook2,
            List<String> listOfDifferences) {
        if (isNumberOfSheetsMatches(workbook1, workbook2)) {
            listOfDifferences.add(NUMBER_OF_SHEETS_DO_NOT_MATCH
                    + System.getProperty(LINE_SEPARATOR) + WORKBOOK1 + NEXT_STR
                    + workbook1.getNumberOfSheets() + NOT_EQUALS + WORKBOOK2
                    + NEXT_STR + workbook2.getNumberOfSheets());
        }
    }

    /**
     * Compare sheet data.
     *
     * @param workbook1
     *            the workbook1
     * @param workbook2
     *            the workbook2
     * @param listOfDifferences
     *
     * @throws ExcelCompareException
     *             the excel compare exception
     */
    private void compareSheetData(Workbook workbook1, Workbook workbook2,
            List<String> listOfDifferences) {
        compareNumberOfRowsInSheets(workbook1, workbook2, listOfDifferences);
        compareNumberOfColumnsInSheets(workbook1, workbook2, listOfDifferences);
        compareDataInAllSheets(workbook1, workbook2, listOfDifferences);

    }

    /**
     * Compare sheet names.
     *
     * @param workbook1
     *            the workbook1
     * @param workbook2
     *            the workbook2
     * @param listOfDifferences
     *
     * @throws ExcelCompareException
     *             the excel compare exception
     */
    private void compareSheetNames(Workbook workbook1, Workbook workbook2,
            List<String> listOfDifferences) {
        for (int i = 0; i < workbook1.getNumberOfSheets(); i++) {
            if (isNameOfSheetMatches(workbook1, workbook2, i)) {
                String sheetname = workbook2.getNumberOfSheets() > i ? workbook2
                        .getSheetName(i) : "";
                listOfDifferences.add(NAME_OF_THE_SHEETS_DO_NOT_MATCH
                        + System.getProperty(LINE_SEPARATOR) + WORKBOOK1
                        + NEXT_STR + workbook1.getSheetName(i) + BRACKET_START
                        + (i + 1) + BRACKET_END + NOT_EQUALS + WORKBOOK2
                        + NEXT_STR + sheetname + BRACKET_START + (i + 1)
                        + BRACKET_END);
            }
        }
    }

    /**
     * Gets the message.
     *
     * @param workbook1
     *            the workbook1
     * @param workbook2
     *            the workbook2
     * @param i
     *            the i
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @param messageStart
     *            the message start
     * @param workBook1Value
     *            the work book1 value
     * @param workBook2Value
     *            the work book2 value
     * @return the message
     */
    private String getMessage(Workbook workbook1, Workbook workbook2, int i,
            Cell cellWorkBook1, Cell cellWorkBook2, String messageStart,
            String workBook1Value, String workBook2Value) {
        StringBuilder sb = new StringBuilder();
        return sb
                .append(messageStart)
                .append(System.getProperty(LINE_SEPARATOR))
                .append(WORKBOOK1)
                .append(NEXT_STR)
                .append(workbook1.getSheetName(i))
                .append(NEXT_STR)
                .append(new CellReference(cellWorkBook1.getRowIndex(),
                        cellWorkBook1.getColumnIndex()).formatAsString())
                .append(BRACKET_START)
                .append(workBook1Value)
                .append(BRACKET_END)
                .append(NOT_EQUALS)
                .append(WORKBOOK2)
                .append(NEXT_STR)
                .append(workbook2.getSheetName(i))
                .append(NEXT_STR)
                .append(new CellReference(cellWorkBook2.getRowIndex(),
                        cellWorkBook2.getColumnIndex()).formatAsString())
                .append(BRACKET_START).append(workBook2Value)
                .append(BRACKET_END).toString();
    }

    /**
     * Checks if cell alignment matches.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell alignment matches
     */
    private boolean isCellAlignmentMatches(Cell cellWorkBook1,
            Cell cellWorkBook2) {
        return cellWorkBook1.getCellStyle().getAlignment() != cellWorkBook2
                .getCellStyle().getAlignment();
    }

    /**
     * Checks if cell back ground fill matches and empty.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell back ground fill matches and empty
     */
    private boolean isCellBackGroundFillMatchesAndEmpty(Cell cellWorkBook1,
            Cell cellWorkBook2) {
        return (cellWorkBook1.getCellStyle().getFillForegroundColorColor() == null)
                && (cellWorkBook2.getCellStyle().getFillForegroundColorColor() == null);
    }

    /**
     * Checks if cell border bottom matches.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell border bottom matches
     */
    private boolean isCellBorderBottomMatches(Cell cellWorkBook1,
            Cell cellWorkBook2) {
        if (cellWorkBook1.getCellStyle() instanceof XSSFCellStyle) {
            return ((XSSFCellStyle) cellWorkBook1.getCellStyle())
                    .getBorderBottom() != ((XSSFCellStyle) cellWorkBook2
                    .getCellStyle()).getBorderBottom();
        } else {
            return false;
        }

    }

    /**
     * Checks if cell border left matches.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell border left matches
     */
    private boolean isCellBorderLeftMatches(Cell cellWorkBook1,
            Cell cellWorkBook2) {
        if (cellWorkBook1.getCellStyle() instanceof XSSFCellStyle) {
            return ((XSSFCellStyle) cellWorkBook1.getCellStyle())
                    .getBorderLeft() != ((XSSFCellStyle) cellWorkBook2
                    .getCellStyle()).getBorderLeft();
        } else {
            return false;
        }

    }

    /**
     * Checks if cell border right matches.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell border right matches
     */
    private boolean isCellBorderRightMatches(Cell cellWorkBook1,
            Cell cellWorkBook2) {
        if (cellWorkBook1.getCellStyle() instanceof XSSFCellStyle) {
            return ((XSSFCellStyle) cellWorkBook1.getCellStyle())
                    .getBorderRight() != ((XSSFCellStyle) cellWorkBook2
                    .getCellStyle()).getBorderRight();
        } else {
            return false;
        }

    }

    /**
     * Checks if cell border top matches.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell border top matches
     */
    private boolean isCellBorderTopMatches(Cell cellWorkBook1,
            Cell cellWorkBook2) {
        if (cellWorkBook1.getCellStyle() instanceof XSSFCellStyle) {
            return ((XSSFCellStyle) cellWorkBook1.getCellStyle())
                    .getBorderTop() != ((XSSFCellStyle) cellWorkBook2
                    .getCellStyle()).getBorderTop();
        } else {
            return false;
        }

    }

    /**
     * Checks if cell content formula.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @return true, if cell content formula
     */
    private boolean isCellContentFormula(Cell cellWorkBook1) {
        return cellWorkBook1.getCellType() == Cell.CELL_TYPE_FORMULA;
    }

    /**
     * Checks if cell content in error.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @return true, if cell content in error
     */
    private boolean isCellContentInError(Cell cellWorkBook1) {
        return cellWorkBook1.getCellType() == Cell.CELL_TYPE_ERROR;
    }

    /**
     * Checks if cell content matches.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell content matches
     */
    private boolean isCellContentMatches(Cell cellWorkBook1, Cell cellWorkBook2) {
        return !(cellWorkBook1.getRichStringCellValue().getString()
                .equals(cellWorkBook2.getRichStringCellValue().getString()));
    }

    /**
     * Checks if cell content matches for boolean.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell content matches for boolean
     */
    private boolean isCellContentMatchesForBoolean(Cell cellWorkBook1,
            Cell cellWorkBook2) {
        return !(cellWorkBook1.getBooleanCellValue() == cellWorkBook2
                .getBooleanCellValue());
    }

    /**
     * Checks if cell content matches for date.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell content matches for date
     */
    private boolean isCellContentMatchesForDate(Cell cellWorkBook1,
            Cell cellWorkBook2) {
        return !(cellWorkBook1.getDateCellValue().equals(cellWorkBook2
                .getDateCellValue()));
    }

    /**
     * Checks if cell content matches for formula.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell content matches for formula
     */
    private boolean isCellContentMatchesForFormula(Cell cellWorkBook1,
            Cell cellWorkBook2) {
        return !(cellWorkBook1.getCellFormula().equals(cellWorkBook2
                .getCellFormula()));
    }

    /**
     * Checks if cell content matches for numeric.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell content matches for numeric
     */
    private boolean isCellContentMatchesForNumeric(Cell cellWorkBook1,
            Cell cellWorkBook2) {
        return !(cellWorkBook1.getNumericCellValue() == cellWorkBook2
                .getNumericCellValue());
    }

    /**
     * Checks if cell content type blank.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @return true, if cell content type blank
     */
    private boolean isCellContentTypeBlank(Cell cellWorkBook1) {
        return cellWorkBook1.getCellType() == Cell.CELL_TYPE_BLANK;
    }

    /**
     * Checks if cell content type boolean.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @return true, if cell content type boolean
     */
    private boolean isCellContentTypeBoolean(Cell cellWorkBook1) {
        return cellWorkBook1.getCellType() == Cell.CELL_TYPE_BOOLEAN;
    }

    /**
     * Checks if cell content type numeric.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @return true, if cell content type numeric
     */
    private boolean isCellContentTypeNumeric(Cell cellWorkBook1) {
        return cellWorkBook1.getCellType() == Cell.CELL_TYPE_NUMERIC;
    }

    /**
     * Checks if cell content type string.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @return true, if cell content type string
     */
    private boolean isCellContentTypeString(Cell cellWorkBook1) {
        return cellWorkBook1.getCellType() == Cell.CELL_TYPE_STRING;
    }

    /**
     * Checks if cell file back ground matches.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell file back ground matches
     */
    private boolean isCellFileBackGroundMatches(Cell cellWorkBook1,
            Cell cellWorkBook2) {
        if (cellWorkBook1.getCellStyle() instanceof XSSFCellStyle) {
            return !((XSSFColor) cellWorkBook1.getCellStyle()
                    .getFillForegroundColorColor()).getARGBHex().equals(
                    ((XSSFColor) cellWorkBook2.getCellStyle()
                            .getFillForegroundColorColor()).getARGBHex());
        } else {
            return false;
        }

    }

    /**
     * Checks if cell fill back ground matches and either empty.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell fill back ground matches and either empty
     */
    private boolean isCellFillBackGroundMatchesAndEitherEmpty(
            Cell cellWorkBook1, Cell cellWorkBook2) {
        return (cellWorkBook1.getCellStyle().getFillForegroundColorColor() == null)
                && (cellWorkBook2.getCellStyle().getFillForegroundColorColor() != null);
    }

    /**
     * Checks if cell fill back ground matches and second empty.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell fill back ground matches and second empty
     */
    private boolean isCellFillBackGroundMatchesAndSecondEmpty(
            Cell cellWorkBook1, Cell cellWorkBook2) {
        return (cellWorkBook1.getCellStyle().getFillForegroundColorColor() != null)
                && (cellWorkBook2.getCellStyle().getFillForegroundColorColor() == null);
    }

    /**
     * Checks if cell fill pattern matches.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell fill pattern matches
     */
    private boolean isCellFillPatternMatches(Cell cellWorkBook1,
            Cell cellWorkBook2) {
        return cellWorkBook1.getCellStyle().getFillPattern() != cellWorkBook2
                .getCellStyle().getFillPattern();
    }

    /**
     * Checks if cell font bold matches.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell font bold matches
     */
    private boolean isCellFontBoldMatches(Cell cellWorkBook1, Cell cellWorkBook2) {
        if (cellWorkBook1.getCellStyle() instanceof XSSFCellStyle) {
            return ((XSSFCellStyle) cellWorkBook1.getCellStyle()).getFont()
                    .getBold() != ((XSSFCellStyle) cellWorkBook2.getCellStyle())
                    .getFont().getBold();
        } else {
            return false;
        }

    }

    /**
     * Checks if cell font family matches.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell font family matches
     */
    private boolean isCellFontFamilyMatches(Cell cellWorkBook1,
            Cell cellWorkBook2) {
        if (cellWorkBook1.getCellStyle() instanceof XSSFCellStyle) {
            return !(((XSSFCellStyle) cellWorkBook1.getCellStyle()).getFont()
                    .getFontName().equals(((XSSFCellStyle) cellWorkBook2
                    .getCellStyle()).getFont().getFontName()));
        } else {
            return false;
        }
    }

    /**
     * Checks if cell font italics matches.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell font italics matches
     */
    private boolean isCellFontItalicsMatches(Cell cellWorkBook1,
            Cell cellWorkBook2) {
        if (cellWorkBook1.getCellStyle() instanceof XSSFCellStyle) {
            return ((XSSFCellStyle) cellWorkBook1.getCellStyle()).getFont()
                    .getItalic() != ((XSSFCellStyle) cellWorkBook2
                    .getCellStyle()).getFont().getItalic();
        } else {
            return false;
        }

    }

    /**
     * Checks if cell font size matches.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell font size matches
     */
    private boolean isCellFontSizeMatches(Cell cellWorkBook1, Cell cellWorkBook2) {
        if (cellWorkBook1.getCellStyle() instanceof XSSFCellStyle) {
            return ((XSSFCellStyle) cellWorkBook1.getCellStyle()).getFont()
                    .getFontHeightInPoints() != ((XSSFCellStyle) cellWorkBook2
                    .getCellStyle()).getFont().getFontHeightInPoints();
        } else {
            return false;
        }

    }

    /**
     * Checks if cell hidden matches.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell hidden matches
     */
    private boolean isCellHiddenMatches(Cell cellWorkBook1, Cell cellWorkBook2) {
        return cellWorkBook1.getCellStyle().getHidden() != cellWorkBook2
                .getCellStyle().getHidden();
    }

    /**
     * Checks if cell locked matches.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell locked matches
     */
    private boolean isCellLockedMatches(Cell cellWorkBook1, Cell cellWorkBook2) {
        return cellWorkBook1.getCellStyle().getLocked() != cellWorkBook2
                .getCellStyle().getLocked();
    }

    /**
     * Checks if cell type matches.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell type matches
     */
    private boolean isCellTypeMatches(Cell cellWorkBook1, Cell cellWorkBook2) {
        return !(cellWorkBook1.getCellType() == cellWorkBook2.getCellType());
    }

    /**
     * Checks if cell under line matches.
     *
     * @param cellWorkBook1
     *            the cell work book1
     * @param cellWorkBook2
     *            the cell work book2
     * @return true, if cell under line matches
     */
    private boolean isCellUnderLineMatches(Cell cellWorkBook1,
            Cell cellWorkBook2) {
        if (cellWorkBook1.getCellStyle() instanceof XSSFCellStyle) {
            return ((XSSFCellStyle) cellWorkBook1.getCellStyle()).getFont()
                    .getUnderline() != ((XSSFCellStyle) cellWorkBook2
                    .getCellStyle()).getFont().getUnderline();
        } else {
            return false;
        }

    }

    /**
     * Checks if name of sheet matches.
     *
     * @param workbook1
     *            the workbook1
     * @param workbook2
     *            the workbook2
     * @param i
     *            the i
     * @return true, if name of sheet matches
     */
    private boolean isNameOfSheetMatches(Workbook workbook1,
            Workbook workbook2, int i) {
        if (workbook2.getNumberOfSheets() > i) {
            return !(workbook1.getSheetName(i)
                    .equals(workbook2.getSheetName(i)));
        } else {
            return true;
        }

    }

    /**
     * Checks if number of columns matches.
     *
     * @param sheetWorkBook1
     *            the sheet work book1
     * @param sheetWorkBook2
     *            the sheet work book2
     * @return true, if number of columns matches
     */
    private boolean isNumberOfColumnsMatches(Sheet sheetWorkBook1,
            Sheet sheetWorkBook2) {
        if (sheetWorkBook2 != null) {
            return !(sheetWorkBook1.getRow(0).getLastCellNum() == sheetWorkBook2
                    .getRow(0).getLastCellNum());
        } else {
            return true;
        }

    }

    /**
     * Checks if number of rows matches.
     *
     * @param sheetWorkBook1
     *            the sheet work book1
     * @param sheetWorkBook2
     *            the sheet work book2
     * @return true, if number of rows matches
     */
    private boolean isNumberOfRowsMatches(Sheet sheetWorkBook1,
            Sheet sheetWorkBook2) {
        if (sheetWorkBook2 != null) {
            return !(sheetWorkBook1.getPhysicalNumberOfRows() == sheetWorkBook2
                    .getPhysicalNumberOfRows());
        } else {
            return true;
        }

    }

    /**
     * Checks if number of sheets matches.
     *
     * @param workbook1
     *            the workbook1
     * @param workbook2
     *            the workbook2
     * @return true, if number of sheets matches
     */
    private boolean isNumberOfSheetsMatches(Workbook workbook1,
            Workbook workbook2) {
        return !(workbook1.getNumberOfSheets() == workbook2.getNumberOfSheets());
    }

    private boolean isWorkBookEmpty(Sheet sheetWorkBook1, Sheet sheetWorkBook2) {
        if (sheetWorkBook2 != null) {
            return !((null == sheetWorkBook1.getRow(0)) || (null == sheetWorkBook2
                    .getRow(0)));
        } else {
            return true;
        }

    }

}

class ExcelFileDifference {
    boolean isDifferenceFound;
    List<String> listOfDifferences;
}
