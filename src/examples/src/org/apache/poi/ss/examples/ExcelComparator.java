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
package org.apache.poi.ss.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


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
 *  List<String> listOfDifferences = ExcelComparator.compare(wb1, wb2);
 *  for (String differences : listOfDifferences)
 *      System.out.println(differences);
 *  System.out.println("DifferenceFound = "+ excelFileDifference.isDifferenceFound);
 *  }
 * </pre>
 */
public class ExcelComparator {
    
    private static final String CELL_DATA_DOES_NOT_MATCH = "Cell Data does not Match ::";
    private static final String CELL_FONT_ATTRIBUTES_DOES_NOT_MATCH = "Cell Font Attributes does not Match ::";

    private static class Locator {
        Workbook workbook;
        Sheet sheet;
        Row row;
        Cell cell;
    }
    
    List<String> listOfDifferences = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        if (args.length != 2 || !(new File(args[0]).exists()) || !(new File(args[1]).exists())) {
            System.err.println("java -cp <classpath> "+ExcelComparator.class.getCanonicalName()+" <workbook1.xls/x> <workbook2.xls/x");
            System.exit(-1);
        }

        try (Workbook wb1 = WorkbookFactory.create(new File(args[0]), null, true)) {
            try (Workbook wb2 = WorkbookFactory.create(new File(args[1]), null, true)) {
                for (String d : ExcelComparator.compare(wb1, wb2)) {
                    System.out.println(d);
                }
            }
        }
    }
    
    /**
     * Utility to compare Excel File Contents cell by cell for all sheets.
     *
     * @param wb1 the workbook1
     * @param wb2 the workbook2
     * @return the Excel file difference containing a flag and a list of differences
     */
    public static List<String> compare(Workbook wb1, Workbook wb2) {
        Locator loc1 = new Locator();
        Locator loc2 = new Locator();
        loc1.workbook = wb1;
        loc2.workbook = wb2;

        ExcelComparator excelComparator = new ExcelComparator();
        excelComparator.compareNumberOfSheets(loc1, loc2 );
        excelComparator.compareSheetNames(loc1, loc2);
        excelComparator.compareSheetData(loc1, loc2);

        return excelComparator.listOfDifferences;
    }

    /**
     * Compare data in all sheets.
     */
    private void compareDataInAllSheets(Locator loc1, Locator loc2) {
        for (int i = 0; i < loc1.workbook.getNumberOfSheets(); i++) {
            if (loc2.workbook.getNumberOfSheets() <= i) {
                return;
            }

            loc1.sheet = loc1.workbook.getSheetAt(i);
            loc2.sheet = loc2.workbook.getSheetAt(i);

            compareDataInSheet(loc1, loc2);
        }
    }

    private void compareDataInSheet(Locator loc1, Locator loc2) {
        for (int j = 0; j <= loc1.sheet.getLastRowNum(); j++) {
            if (loc2.sheet.getLastRowNum() <= j) {
                return;
            }

            loc1.row = loc1.sheet.getRow(j);
            loc2.row = loc2.sheet.getRow(j);

            if ((loc1.row == null) || (loc2.row == null)) {
                continue;
            }

            compareDataInRow(loc1, loc2);
        }
    }

    private void compareDataInRow(Locator loc1, Locator loc2) {
        for (int k = 0; k <= loc1.row.getLastCellNum(); k++) {
            if (loc2.row.getLastCellNum() <= k) {
                return;
            }

            loc1.cell = loc1.row.getCell(k);
            loc2.cell = loc2.row.getCell(k);

            if ((loc1.cell == null) || (loc2.cell == null)) {
                continue;
            }

            compareDataInCell(loc1, loc2);
        }
    }

    private void compareDataInCell(Locator loc1, Locator loc2) {
        if (isCellTypeMatches(loc1, loc2)) {
            final CellType loc1cellType = loc1.cell.getCellType();
            switch(loc1cellType) {
                case BLANK:
                case STRING:
                case ERROR:
                    isCellContentMatches(loc1,loc2);
                    break;
                case BOOLEAN:
                    isCellContentMatchesForBoolean(loc1,loc2);
                    break;
                case FORMULA:
                    isCellContentMatchesForFormula(loc1,loc2);
                    break;
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(loc1.cell)) {
                        isCellContentMatchesForDate(loc1,loc2);
                    } else {
                        isCellContentMatchesForNumeric(loc1,loc2);
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected cell type: " + loc1cellType);
            }
        }

        isCellFillPatternMatches(loc1,loc2);
        isCellAlignmentMatches(loc1,loc2);
        isCellHiddenMatches(loc1,loc2);
        isCellLockedMatches(loc1,loc2);
        isCellFontFamilyMatches(loc1,loc2);
        isCellFontSizeMatches(loc1,loc2);
        isCellFontBoldMatches(loc1,loc2);
        isCellUnderLineMatches(loc1,loc2);
        isCellFontItalicsMatches(loc1,loc2);
        isCellBorderMatches(loc1,loc2,'t');
        isCellBorderMatches(loc1,loc2,'l');
        isCellBorderMatches(loc1,loc2,'b');
        isCellBorderMatches(loc1,loc2,'r');
        isCellFillBackGroundMatches(loc1,loc2);
    }

    /**
     * Compare number of columns in sheets.
     */
    private void compareNumberOfColumnsInSheets(Locator loc1, Locator loc2) {
        for (int i = 0; i < loc1.workbook.getNumberOfSheets(); i++) {
            if (loc2.workbook.getNumberOfSheets() <= i) {
                return;
            }
            
            loc1.sheet = loc1.workbook.getSheetAt(i);
            loc2.sheet = loc2.workbook.getSheetAt(i);

            Iterator<Row> ri1 = loc1.sheet.rowIterator();
            Iterator<Row> ri2 = loc2.sheet.rowIterator();
            
            int num1 = (ri1.hasNext()) ? ri1.next().getPhysicalNumberOfCells() : 0;
            int num2 = (ri2.hasNext()) ? ri2.next().getPhysicalNumberOfCells() : 0;
            
            if (num1 != num2) {
                String str = String.format(Locale.ROOT, "%s\nworkbook1 -> %s [%d] != workbook2 -> %s [%d]",
                    "Number Of Columns does not Match ::",
                    loc1.sheet.getSheetName(), num1,
                    loc2.sheet.getSheetName(), num2
                );
                listOfDifferences.add(str);
            }
        }
    }

    /**
     * Compare number of rows in sheets.
     */
    private void compareNumberOfRowsInSheets(Locator loc1, Locator loc2) {
        for (int i = 0; i < loc1.workbook.getNumberOfSheets(); i++) {
            if (loc2.workbook.getNumberOfSheets() <= i) {
                return;
            }

            loc1.sheet = loc1.workbook.getSheetAt(i);
            loc2.sheet = loc2.workbook.getSheetAt(i);
            
            int num1 = loc1.sheet.getPhysicalNumberOfRows();
            int num2 = loc2.sheet.getPhysicalNumberOfRows();

            if (num1 != num2) {
                String str = String.format(Locale.ROOT, "%s\nworkbook1 -> %s [%d] != workbook2 -> %s [%d]",
                    "Number Of Rows does not Match ::",
                    loc1.sheet.getSheetName(), num1,
                    loc2.sheet.getSheetName(), num2
                );
                listOfDifferences.add(str);
            }
        }

    }

    /**
     * Compare number of sheets.
     */
    private void compareNumberOfSheets(Locator loc1, Locator loc2) {
        int num1 = loc1.workbook.getNumberOfSheets();
        int num2 = loc2.workbook.getNumberOfSheets();
        if (num1 != num2) {
            String str = String.format(Locale.ROOT, "%s\nworkbook1 [%d] != workbook2 [%d]",
                "Number of Sheets do not match ::",
                num1, num2
            );

            listOfDifferences.add(str);
            
        }
    }

    /**
     * Compare sheet data.
     */
    private void compareSheetData(Locator loc1, Locator loc2) {
        compareNumberOfRowsInSheets(loc1, loc2);
        compareNumberOfColumnsInSheets(loc1, loc2);
        compareDataInAllSheets(loc1, loc2);

    }

    /**
     * Compare sheet names.
     */
    private void compareSheetNames(Locator loc1, Locator loc2) {
        for (int i = 0; i < loc1.workbook.getNumberOfSheets(); i++) {
            String name1 = loc1.workbook.getSheetName(i);
            String name2 = (loc2.workbook.getNumberOfSheets() > i) ? loc2.workbook.getSheetName(i) : "";
            
            if (!name1.equals(name2)) {
                String str = String.format(Locale.ROOT, "%s\nworkbook1 -> %s [%d] != workbook2 -> %s [%d]",
                    "Name of the sheets do not match ::", name1, i+1, name2, i+1
                );
                listOfDifferences.add(str);
            }
        }
    }

    /**
     * Formats the message.
     */
    private void addMessage(Locator loc1, Locator loc2, String messageStart, String value1, String value2) {
        String str =
            String.format(Locale.ROOT, "%s\nworkbook1 -> %s -> %s [%s] != workbook2 -> %s -> %s [%s]",
                messageStart,
                loc1.sheet.getSheetName(), new CellReference(loc1.cell).formatAsString(), value1,
                loc2.sheet.getSheetName(), new CellReference(loc2.cell).formatAsString(), value2
            );
        listOfDifferences.add(str);
    }

    /**
     * Checks if cell alignment matches.
     */
    private void isCellAlignmentMatches(Locator loc1, Locator loc2) {
        if(loc1.cell.getCellStyle() == null || loc2.cell.getCellStyle() == null) {
            return;
        }

        HorizontalAlignment align1 = loc1.cell.getCellStyle().getAlignment();
        HorizontalAlignment align2 = loc2.cell.getCellStyle().getAlignment();
        if (align1 != align2) {
            addMessage(loc1, loc2,
                "Cell Alignment does not Match ::",
                align1.name(),
                align2.name()
            );
        }
    }

    /**
     * Checks if cell border bottom matches.
     */
    private void isCellBorderMatches(Locator loc1, Locator loc2, char borderSide) {
        if (!(loc1.cell instanceof XSSFCell) ||
                loc1.cell.getCellStyle() == null || loc2.cell.getCellStyle() == null) {
            return;
        }

        XSSFCellStyle style1 = ((XSSFCell)loc1.cell).getCellStyle();
        XSSFCellStyle style2 = ((XSSFCell)loc2.cell).getCellStyle();
        boolean b1, b2;
        String borderName;
        switch (borderSide) {
            case 't': default:
                b1 = style1.getBorderTop() == BorderStyle.THIN;
                b2 = style2.getBorderTop() == BorderStyle.THIN;
                borderName = "TOP";
                break;
            case 'b':
                b1 = style1.getBorderBottom() == BorderStyle.THIN;
                b2 = style2.getBorderBottom() == BorderStyle.THIN;
                borderName = "BOTTOM";
                break;
            case 'l':
                b1 = style1.getBorderLeft() == BorderStyle.THIN;
                b2 = style2.getBorderLeft() == BorderStyle.THIN;
                borderName = "LEFT";
                break;
            case 'r':
                b1 = style1.getBorderRight() == BorderStyle.THIN;
                b2 = style2.getBorderRight() == BorderStyle.THIN;
                borderName = "RIGHT";
                break;
        }
        if (b1 != b2) {
            addMessage(loc1, loc2,
                "Cell Border Attributes does not Match ::",
                (b1 ? "" : "NOT ")+borderName+" BORDER",
                (b2 ? "" : "NOT ")+borderName+" BORDER"
            );
        }
    }

    /**
     * Checks if cell content matches.
     */
    private void isCellContentMatches(Locator loc1, Locator loc2) {
        String str1 = loc1.cell.toString();
        String str2 = loc2.cell.toString();
        if (!str1.equals(str2)) {
            addMessage(loc1,loc2,CELL_DATA_DOES_NOT_MATCH,str1,str2);
        }
    }

    /**
     * Checks if cell content matches for boolean.
     */
    private void isCellContentMatchesForBoolean(Locator loc1, Locator loc2) {
        boolean b1 = loc1.cell.getBooleanCellValue();
        boolean b2 = loc2.cell.getBooleanCellValue();
        if (b1 != b2) {
            addMessage(loc1,loc2,CELL_DATA_DOES_NOT_MATCH,Boolean.toString(b1),Boolean.toString(b2));
        }
    }

    /**
     * Checks if cell content matches for date.
     */
    private void isCellContentMatchesForDate(Locator loc1, Locator loc2) {
        Date date1 = loc1.cell.getDateCellValue();
        Date date2 = loc2.cell.getDateCellValue();
        if (!date1.equals(date2)) {
            addMessage(loc1, loc2, CELL_DATA_DOES_NOT_MATCH, date1.toString(), date2.toString());
        }
    }


    /**
     * Checks if cell content matches for formula.
     */
    private void isCellContentMatchesForFormula(Locator loc1, Locator loc2) {
        // TODO: actually evaluate the formula / NPE checks
        String form1 = loc1.cell.getCellFormula();
        String form2 = loc2.cell.getCellFormula();
        if (!form1.equals(form2)) {
            addMessage(loc1, loc2, CELL_DATA_DOES_NOT_MATCH, form1, form2);
        }
    }

    /**
     * Checks if cell content matches for numeric.
     */
    private void isCellContentMatchesForNumeric(Locator loc1, Locator loc2) {
        // TODO: Check for NaN
        double num1 = loc1.cell.getNumericCellValue();
        double num2 = loc2.cell.getNumericCellValue();
        if (num1 != num2) {
            addMessage(loc1, loc2, CELL_DATA_DOES_NOT_MATCH, Double.toString(num1), Double.toString(num2));
        }
    }

    private String getCellFillBackground(Locator loc) {
        Color col = loc.cell.getCellStyle().getFillForegroundColorColor();
        return (col instanceof XSSFColor) ? ((XSSFColor)col).getARGBHex() : "NO COLOR";
    }
    
    /**
     * Checks if cell file back ground matches.
     */
    private void isCellFillBackGroundMatches(Locator loc1, Locator loc2) {
        if(loc1.cell.getCellStyle() == null || loc2.cell.getCellStyle() == null) {
            return;
        }

        String col1 = getCellFillBackground(loc1);
        String col2 = getCellFillBackground(loc2);
        if (!col1.equals(col2)) {
            addMessage(loc1, loc2, "Cell Fill Color does not Match ::", col1, col2);
        }
    }
    /**
     * Checks if cell fill pattern matches.
     */
    private void isCellFillPatternMatches(Locator loc1, Locator loc2) {
        if(loc1.cell.getCellStyle() == null || loc2.cell.getCellStyle() == null) {
            return;
        }

        FillPatternType fill1 = loc1.cell.getCellStyle().getFillPattern();
        FillPatternType fill2 = loc2.cell.getCellStyle().getFillPattern();
        if (fill1 != fill2) {
            addMessage(loc1, loc2,
                "Cell Fill pattern does not Match ::",
                fill1.name(),
                fill2.name()
            );
        }
    }

    /**
     * Checks if cell font bold matches.
     */
    private void isCellFontBoldMatches(Locator loc1, Locator loc2) {
        if (!(loc1.cell instanceof XSSFCell) ||
                loc1.cell.getCellStyle() == null || loc2.cell.getCellStyle() == null) {
            return;
        }

        if(hasInvalidFontIndex(loc1, loc2)) {
            return;
        }

        boolean b1 = ((XSSFCell)loc1.cell).getCellStyle().getFont().getBold();
        boolean b2 = ((XSSFCell)loc2.cell).getCellStyle().getFont().getBold();
        if (b1 != b2) {
            addMessage(loc1, loc2,
                CELL_FONT_ATTRIBUTES_DOES_NOT_MATCH,
                (b1 ? "" : "NOT ")+"BOLD",
                (b2 ? "" : "NOT ")+"BOLD"
            );
        }
    }

    /**
     * Checks if cell font family matches.
     */
    private void isCellFontFamilyMatches(Locator loc1, Locator loc2) {
        if (!(loc1.cell instanceof XSSFCell) ||
                loc1.cell.getCellStyle() == null || loc2.cell.getCellStyle() == null) {
            return;
        }

        if(hasInvalidFontIndex(loc1, loc2)) {
            return;
        }

        String family1 = ((XSSFCell)loc1.cell).getCellStyle().getFont().getFontName();
        String family2 = ((XSSFCell)loc2.cell).getCellStyle().getFont().getFontName();
        if (!family1.equals(family2)) {
            addMessage(loc1, loc2, "Cell Font Family does not Match ::", family1, family2);
        }
    }

    private boolean hasInvalidFontIndex(Locator loc1, Locator loc2) {
        int fontIdx1 = loc1.cell.getCellStyle().getFontIndexAsInt();
        int fontCount1 = ((XSSFWorkbook)loc1.workbook).getStylesSource().getFonts().size();
        int fontIdx2 = loc2.cell.getCellStyle().getFontIndexAsInt();
        int fontCount2 = ((XSSFWorkbook)loc2.workbook).getStylesSource().getFonts().size();

        if(fontIdx1 >= fontCount1 || fontIdx2 >= fontCount2) {
            addMessage(loc1, loc2, "Corrupted file, cell style references a font which is not defined", Integer.toString(fontIdx1), Integer.toString(fontIdx2));
            return true;
        }

        return false;
    }

    /**
     * Checks if cell font italics matches.
     */
    private void isCellFontItalicsMatches(Locator loc1, Locator loc2) {
        if (!(loc1.cell instanceof XSSFCell) ||
                loc1.cell.getCellStyle() == null || loc2.cell.getCellStyle() == null) {
            return;
        }

        if(hasInvalidFontIndex(loc1, loc2)) {
            return;
        }

        boolean b1 = ((XSSFCell)loc1.cell).getCellStyle().getFont().getItalic();
        boolean b2 = ((XSSFCell)loc2.cell).getCellStyle().getFont().getItalic();
        if (b1 != b2) {
            addMessage(loc1, loc2,
                CELL_FONT_ATTRIBUTES_DOES_NOT_MATCH,
                (b1 ? "" : "NOT ")+"ITALICS",
                (b2 ? "" : "NOT ")+"ITALICS"
            );
        }
    }

    /**
     * Checks if cell font size matches.
     */
    private void isCellFontSizeMatches(Locator loc1, Locator loc2) {
        if (!(loc1.cell instanceof XSSFCell) ||
                loc1.cell.getCellStyle() == null || loc2.cell.getCellStyle() == null) {
            return;
        }

        if(hasInvalidFontIndex(loc1, loc2)) {
            return;
        }

        short size1 = ((XSSFCell)loc1.cell).getCellStyle().getFont().getFontHeightInPoints();
        short size2 = ((XSSFCell)loc2.cell).getCellStyle().getFont().getFontHeightInPoints();
        if (size1 != size2) {
            addMessage(loc1, loc2,
                "Cell Font Size does not Match ::",
                Short.toString(size1),
                Short.toString(size2)
            );
        }
    }

    /**
     * Checks if cell hidden matches.
     */
    private void isCellHiddenMatches(Locator loc1, Locator loc2) {
        if (loc1.cell.getCellStyle() == null || loc2.cell.getCellStyle() == null) {
            return;
        }

        boolean b1 = loc1.cell.getCellStyle().getHidden();
        boolean b2 = loc1.cell.getCellStyle().getHidden();
        if (b1 != b2) {
            addMessage(loc1, loc2,
                "Cell Visibility does not Match ::",
                (b1 ? "" : "NOT ")+"HIDDEN",
                (b2 ? "" : "NOT ")+"HIDDEN"
            );
        }
    }

    /**
     * Checks if cell locked matches.
     */
    private void isCellLockedMatches(Locator loc1, Locator loc2) {
        if (loc1.cell.getCellStyle() == null || loc2.cell.getCellStyle() == null) {
            return;
        }

        boolean b1 = loc1.cell.getCellStyle().getLocked();
        boolean b2 = loc1.cell.getCellStyle().getLocked();
        if (b1 != b2) {
            addMessage(loc1, loc2,
                    "Cell Protection does not Match ::",
                (b1 ? "" : "NOT ")+"LOCKED",
                (b2 ? "" : "NOT ")+"LOCKED"
            );
        }
    }

    /**
     * Checks if cell type matches.
     */
    private boolean isCellTypeMatches(Locator loc1, Locator loc2) {
        CellType type1 = loc1.cell.getCellType();
        CellType type2 = loc2.cell.getCellType();
        if (type1 == type2) {
            return true;
        }

        addMessage(loc1, loc2,
            "Cell Data-Type does not Match in :: ",
            type1.name(), type2.name()
        );
        return false;
    }

    /**
     * Checks if cell under line matches.
     */
    private void isCellUnderLineMatches(Locator loc1, Locator loc2) {
        // TODO: distinguish underline type

        if (!(loc1.cell instanceof XSSFCell) ||
                loc1.cell.getCellStyle() == null || loc2.cell.getCellStyle() == null) {
            return;
        }

        if(hasInvalidFontIndex(loc1, loc2)) {
            return;
        }

        byte b1 = ((XSSFCell)loc1.cell).getCellStyle().getFont().getUnderline();
        byte b2 = ((XSSFCell)loc2.cell).getCellStyle().getFont().getUnderline();
        if (b1 != b2) {
            addMessage(loc1, loc2,
                CELL_FONT_ATTRIBUTES_DOES_NOT_MATCH,
                (b1 == 1 ? "" : "NOT ")+"UNDERLINE",
                (b2 == 1 ? "" : "NOT ")+"UNDERLINE"
            );
        }
    }
}