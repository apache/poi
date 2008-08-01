/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hssf.usermodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.eventmodel.ERFListener;
import org.apache.poi.hssf.eventmodel.EventRecordFactory;
import org.apache.poi.hssf.record.DVRecord;
import org.apache.poi.hssf.record.RecordFormatException;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFDataValidation;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * <p>Title: TestDataValidation</p>
 * <p>Description: Class for testing Excel's data validation mechanism
 *    Second test :
 *        -
 * </p>
 * @author Dragos Buleandra ( dragos.buleandra@trade2b.ro )
 */
public class TestDataValidation extends TestCase
{

  public void testDataValidation() throws Exception
  {
    System.out.println("\nTest no. 2 - Test Excel's Data validation mechanism");
    String resultFile   = System.getProperty("java.io.tmpdir")+File.separator+"TestDataValidation.xls";
    HSSFWorkbook wb = new HSSFWorkbook();

    HSSFCellStyle style_1 = this.createStyle( wb, HSSFCellStyle.ALIGN_LEFT );
    HSSFCellStyle style_2 = this.createStyle( wb, HSSFCellStyle.ALIGN_CENTER );
    HSSFCellStyle style_3 = this.createStyle( wb, HSSFCellStyle.ALIGN_CENTER, HSSFColor.GREY_25_PERCENT.index, true );
    HSSFCellStyle style_4 = this.createHeaderStyle(wb);
    HSSFDataValidation data_validation = null;

    //data validation's number types
    System.out.print("    Create sheet for Data Validation's number types ... ");
    HSSFSheet fSheet = wb.createSheet("Number types");

    //"Whole number" validation type
    this.createDVTypeRow( wb, 0, style_3, "Whole number");
    this.createHeaderRow( wb, 0, style_4 );

    short start_row = (short)fSheet.getPhysicalNumberOfRows();
    data_validation = new HSSFDataValidation((short)(start_row),(short)0,(short)(start_row),(short)0);
    data_validation.setDataValidationType(HSSFDataValidation.DATA_TYPE_INTEGER);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_BETWEEN);
    data_validation.setFirstFormula("2");
    data_validation.setSecondFormula("6");
    data_validation.createErrorBox("Invalid input !", "Something is wrong ; check condition !");
    data_validation.createPromptBox("Hi , dear user !", "So , you just selected me ! Thanks !");
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Between 2 and 6 ", true, true, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type = STOP" );

    data_validation.setFirstRow((short)(start_row+1));
    data_validation.setLastRow((short)(start_row+1));
    data_validation.setEmptyCellAllowed(false);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_NOT_BETWEEN);
    data_validation.setErrorStyle(HSSFDataValidation.ERROR_STYLE_INFO);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Not between 2 and 6 ", false, true, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type = INFO" );

    data_validation.setFirstRow((short)(start_row+2));
    data_validation.setLastRow((short)(start_row+2));
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(false);
    data_validation.setFirstFormula("3");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_EQUAL);
    data_validation.setErrorStyle(HSSFDataValidation.ERROR_STYLE_WARNING);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Equal to 3", false, false, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type = WARNING" );

    data_validation.setFirstRow((short)(start_row+3));
    data_validation.setLastRow((short)(start_row+3));
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(false);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula("3");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_NOT_EQUAL);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Not equal to 3", false, false, false );
    this.writeOtherSettings( fSheet, style_1, "-" );

    data_validation.setFirstRow((short)(start_row+4));
    data_validation.setLastRow((short)(start_row+4));
    data_validation.setEmptyCellAllowed(true);
    data_validation.setShowPromptBox(false);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula("3");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_GREATER_THAN);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Greater than 3", true, false, false );
    this.writeOtherSettings( fSheet, style_1, "-" );

    data_validation.setFirstRow((short)(start_row+5));
    data_validation.setLastRow((short)(start_row+5));
    data_validation.setEmptyCellAllowed(true);
    data_validation.setShowPromptBox(true);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula("3");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_LESS_THAN);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Less than 3", true, true, false );
    this.writeOtherSettings( fSheet, style_1, "-" );

    data_validation.setFirstRow((short)(start_row+6));
    data_validation.setLastRow((short)(start_row+6));
    data_validation.setEmptyCellAllowed(true);
    data_validation.setShowPromptBox(false);
    data_validation.setErrorStyle(HSSFDataValidation.ERROR_STYLE_STOP);
    data_validation.setShowErrorBox(true);
    data_validation.setFirstFormula("4");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_GREATER_OR_EQUAL);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Greater than or equal to 4", true, false, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type = STOP" );

    data_validation.setFirstRow((short)(start_row+7));
    data_validation.setLastRow((short)(start_row+7));
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(true);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula("4");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_LESS_OR_EQUAL);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Less than or equal to 4", false, true, false );
    this.writeOtherSettings( fSheet, style_1, "-" );

    //"Decimal" validation type
    this.createDVTypeRow( wb, 0, style_3, "Decimal");
    this.createHeaderRow( wb, 0, style_4 );

    start_row += (short)(8+4);
    data_validation = new HSSFDataValidation((short)(start_row),(short)0,(short)(start_row),(short)0);
    data_validation.setDataValidationType(HSSFDataValidation.DATA_TYPE_DECIMAL);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_BETWEEN);
    data_validation.setFirstFormula("2");
    data_validation.setSecondFormula("6");
    data_validation.createErrorBox("Invalid input !", "Something is wrong ; check condition !");
    data_validation.createPromptBox("Hi , dear user !", "So , you just selected me ! Thanks !");
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Between 2 and 6 ", true, true, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type = STOP" );

    data_validation.setFirstRow((short)(start_row+1));
    data_validation.setLastRow((short)(start_row+1));
    data_validation.setEmptyCellAllowed(false);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_NOT_BETWEEN);
    data_validation.setErrorStyle(HSSFDataValidation.ERROR_STYLE_INFO);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Not between 2 and 6 ", false, true, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type = INFO" );

    data_validation.setFirstRow((short)(start_row+2));
    data_validation.setLastRow((short)(start_row+2));
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(false);
    data_validation.setFirstFormula("3");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_EQUAL);
    data_validation.setErrorStyle(HSSFDataValidation.ERROR_STYLE_WARNING);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Equal to 3", false, false, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type = WARNING" );

    data_validation.setFirstRow((short)(start_row+3));
    data_validation.setLastRow((short)(start_row+3));
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(false);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula("3");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_NOT_EQUAL);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Not equal to 3", false, false, false );
    this.writeOtherSettings( fSheet, style_1, "-" );

    data_validation.setFirstRow((short)(start_row+4));
    data_validation.setLastRow((short)(start_row+4));
    data_validation.setEmptyCellAllowed(true);
    data_validation.setShowPromptBox(false);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula("3");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_GREATER_THAN);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Greater than 3", true, false, false );
    this.writeOtherSettings( fSheet, style_1, "-" );

    data_validation.setFirstRow((short)(start_row+5));
    data_validation.setLastRow((short)(start_row+5));
    data_validation.setEmptyCellAllowed(true);
    data_validation.setShowPromptBox(true);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula("3");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_LESS_THAN);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Less than 3", true, true, false );
    this.writeOtherSettings( fSheet, style_1, "-" );

    data_validation.setFirstRow((short)(start_row+6));
    data_validation.setLastRow((short)(start_row+6));
    data_validation.setEmptyCellAllowed(true);
    data_validation.setShowPromptBox(false);
    data_validation.setErrorStyle(HSSFDataValidation.ERROR_STYLE_STOP);
    data_validation.setShowErrorBox(true);
    data_validation.setFirstFormula("4");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_GREATER_OR_EQUAL);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Greater than or equal to 4", true, false, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type = STOP" );

    data_validation.setFirstRow((short)(start_row+7));
    data_validation.setLastRow((short)(start_row+7));
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(true);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula("4");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_LESS_OR_EQUAL);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Less than or equal to 4", false, true, false );
    this.writeOtherSettings( fSheet, style_1, "-" );

    System.out.println("done !");

    //"List" Data Validation type
    /** @todo  List*/
    System.out.print("    Create sheet for 'List' Data Validation type ... ");
    fSheet = wb.createSheet("Lists");

    this.createDVTypeRow( wb, 1, style_3, "Explicit lists - list items are explicitly provided");
    this.createDVDeescriptionRow( wb, 1, style_3, "Disadvantage - sum of item's length should be less than 255 characters");
    this.createHeaderRow( wb, 1, style_4 );

    start_row = (short)fSheet.getPhysicalNumberOfRows();
    data_validation = new HSSFDataValidation((short)(start_row),(short)0,(short)(start_row),(short)0);
    data_validation.setDataValidationType(HSSFDataValidation.DATA_TYPE_LIST);
    data_validation.setFirstFormula("1+2+3");
    data_validation.setSecondFormula(null);
    data_validation.setSurppressDropDownArrow(false);
    data_validation.createErrorBox("Invalid input !", "Something is wrong ; check condition !");
    data_validation.createPromptBox("Hi , dear user !", "So , you just selected me ! Thanks !");
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "POIFS,HSSF,HWPF,HPSF", true, true, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type=STOP ; In-cell dropdown=yes" );

    data_validation = new HSSFDataValidation((short)(start_row+1),(short)0,(short)(start_row+1),(short)0);
    data_validation.setDataValidationType(HSSFDataValidation.DATA_TYPE_LIST);
    data_validation.setFirstFormula("4+5+6+7");
    data_validation.setSecondFormula(null);
    data_validation.setSurppressDropDownArrow(false);
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(false);
    data_validation.createErrorBox("Invalid input !", "Something is wrong ; check condition !");
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "POIFS,HSSF,HWPF,HPSF", false, false, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type=STOP ; In-cell dropdown=yes" );

    data_validation = new HSSFDataValidation((short)(start_row+2),(short)0,(short)(start_row+2),(short)0);
    data_validation.setDataValidationType(HSSFDataValidation.DATA_TYPE_LIST);
    data_validation.setFirstFormula("7+21");
    data_validation.setSecondFormula(null);
    data_validation.setSurppressDropDownArrow(true);
    data_validation.createErrorBox("Invalid input !", "Something is wrong ; check condition !");
    data_validation.createPromptBox("Hi , dear user !", "So , you just selected me ! Thanks !");
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "POIFS,HSSF,HWPF,HPSF", true, true, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type=STOP ; In-cell dropdown=no" );

    data_validation = new HSSFDataValidation((short)(start_row+3),(short)0,(short)(start_row+3),(short)0);
    data_validation.setDataValidationType(HSSFDataValidation.DATA_TYPE_LIST);
    data_validation.setFirstFormula("8/2");
    data_validation.setSecondFormula(null);
    data_validation.setSurppressDropDownArrow(true);
    data_validation.createErrorBox("Invalid input !", "Something is wrong ; check condition !");
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(false);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "POIFS,HSSF,HWPF,HPSF", false, false, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type=STOP ; In-cell dropdown=no" );

    this.createDVTypeRow( wb, 1, style_3, "Reference lists - list items are taken from others cells");
    this.createDVDeescriptionRow( wb, 1, style_3, "Advantage - no restriction regarding the sum of item's length");
    this.createHeaderRow( wb, 1, style_4 );

    start_row += (short)(4+5);
    String cellStrValue = "a b c d e f g h i j k l m n o p r s t u v x y z w 0 1 2 3 4 "+
                          "a b c d e f g h i j k l m n o p r s t u v x y z w 0 1 2 3 4 "+
                          "a b c d e f g h i j k l m n o p r s t u v x y z w 0 1 2 3 4 "+
                          "a b c d e f g h i j k l m n o p r s t u v x y z w 0 1 2 3 4 ";

    String strFormula = "$A$100:$A$120";
    data_validation = new HSSFDataValidation((short)(start_row),(short)0,(short)(start_row),(short)0);
    data_validation.setDataValidationType(HSSFDataValidation.DATA_TYPE_LIST);
    data_validation.setFirstFormula(strFormula);
    data_validation.setSecondFormula(null);
    data_validation.setSurppressDropDownArrow(false);
    data_validation.createErrorBox("Invalid input !", "Something is wrong ; check condition !");
    data_validation.createPromptBox("Hi , dear user !", "So , you just selected me ! Thanks !");
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, strFormula, true, true, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type=STOP ; In-cell dropdown=yes" );

    data_validation = new HSSFDataValidation((short)(start_row+1),(short)0,(short)(start_row+1),(short)0);
    data_validation.setDataValidationType(HSSFDataValidation.DATA_TYPE_LIST);
    data_validation.setFirstFormula(strFormula);
    data_validation.setSecondFormula(null);
    data_validation.setSurppressDropDownArrow(false);
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(false);
    data_validation.createErrorBox("Invalid input !", "Something is wrong ; check condition !");
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, strFormula, false, false, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type=STOP ; In-cell dropdown=yes" );

    data_validation = new HSSFDataValidation((short)(start_row+2),(short)0,(short)(start_row+2),(short)0);
    data_validation.setDataValidationType(HSSFDataValidation.DATA_TYPE_LIST);
    data_validation.setFirstFormula(strFormula);
    data_validation.setSecondFormula(null);
    data_validation.setSurppressDropDownArrow(true);
    data_validation.createErrorBox("Invalid input !", "Something is wrong ; check condition !");
    data_validation.createPromptBox("Hi , dear user !", "So , you just selected me ! Thanks !");
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, strFormula, true, true, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type=STOP ; In-cell dropdown=no" );

    data_validation = new HSSFDataValidation((short)(start_row+3),(short)0,(short)(start_row+3),(short)0);
    data_validation.setDataValidationType(HSSFDataValidation.DATA_TYPE_LIST);
    data_validation.setFirstFormula(strFormula);
    data_validation.setSecondFormula(null);
    data_validation.setSurppressDropDownArrow(true);
    data_validation.createErrorBox("Invalid input !", "Something is wrong ; check condition !");
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(false);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, strFormula, false, false, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type=STOP ; In-cell dropdown=no" );

    for (int i=100; i<=120; i++)
    {
       HSSFRow currRow = fSheet.createRow(i);
       currRow.createCell((short)0).setCellValue(cellStrValue);
//       currRow.hide( true );
    }

    System.out.println("done !");

    //Date/Time Validation type
    System.out.print("    Create sheet for 'Date' and 'Time' Data Validation types ... ");
    fSheet = wb.createSheet("Date_Time");
    SimpleDateFormat df = new SimpleDateFormat("m/d/yyyy");
    HSSFDataFormat dataFormat = wb.createDataFormat();
    short fmtDate = dataFormat.getFormat("m/d/yyyy");
    short fmtTime = dataFormat.getFormat("h:mm");
    HSSFCellStyle cellStyle_data = wb.createCellStyle();
    cellStyle_data.setDataFormat(fmtDate);
    HSSFCellStyle cellStyle_time = wb.createCellStyle();
    cellStyle_time.setDataFormat(fmtTime);

    this.createDVTypeRow( wb, 2, style_3, "Date ( cells are already formated as date - m/d/yyyy)");
    this.createHeaderRow( wb, 2, style_4 );

    start_row = (short)fSheet.getPhysicalNumberOfRows();
    data_validation = new HSSFDataValidation((short)(start_row),(short)0,(short)(start_row),(short)0);
    data_validation.setDataValidationType(HSSFDataValidation.DATA_TYPE_DATE);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_BETWEEN);

    data_validation.setFirstFormula( String.valueOf((int)HSSFDateUtil.getExcelDate(df.parse("1/2/2004"))) );
    data_validation.setSecondFormula( String.valueOf((int)HSSFDateUtil.getExcelDate(df.parse("1/6/2004"))) );

    data_validation.createErrorBox("Invalid input !", "Something is wrong ; check condition !");
    data_validation.createPromptBox("Hi , dear user !", "So , you just selected me ! Thanks !");
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Between 1/2/2004 and 1/6/2004 ", true, true, true );
    this.setCellFormat( fSheet, cellStyle_data );
    this.writeOtherSettings( fSheet, style_1, "Error box type = STOP" );

    data_validation.setFirstRow((short)(start_row+1));
    data_validation.setLastRow((short)(start_row+1));
    data_validation.setEmptyCellAllowed(false);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_NOT_BETWEEN);
    data_validation.setErrorStyle(HSSFDataValidation.ERROR_STYLE_INFO);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Not between 1/2/2004 and 1/6/2004 ", false, true, true );
    this.setCellFormat( fSheet, cellStyle_data );
    this.writeOtherSettings( fSheet, style_1, "Error box type = INFO" );

    data_validation.setFirstRow((short)(start_row+2));
    data_validation.setLastRow((short)(start_row+2));
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(false);
    data_validation.setFirstFormula(String.valueOf((int)HSSFDateUtil.getExcelDate(df.parse("3/2/2004"))));
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_EQUAL);
    data_validation.setErrorStyle(HSSFDataValidation.ERROR_STYLE_WARNING);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Equal to 3/2/2004", false, false, true );
    this.setCellFormat( fSheet, cellStyle_data );
    this.writeOtherSettings( fSheet, style_1, "Error box type = WARNING" );

    data_validation.setFirstRow((short)(start_row+3));
    data_validation.setLastRow((short)(start_row+3));
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(false);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula(String.valueOf((int)HSSFDateUtil.getExcelDate(df.parse("3/2/2004"))));
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_NOT_EQUAL);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Not equal to 3/2/2004", false, false, false );
    this.setCellFormat( fSheet, cellStyle_data );
    this.writeOtherSettings( fSheet, style_1, "-" );

    data_validation.setFirstRow((short)(start_row+4));
    data_validation.setLastRow((short)(start_row+4));
    data_validation.setEmptyCellAllowed(true);
    data_validation.setShowPromptBox(false);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula(String.valueOf((int)HSSFDateUtil.getExcelDate(df.parse("3/2/2004"))));
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_GREATER_THAN);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Greater than 3/2/2004", true, false, false );
    this.setCellFormat( fSheet, cellStyle_data );
    this.writeOtherSettings( fSheet, style_1, "-" );

    data_validation.setFirstRow((short)(start_row+5));
    data_validation.setLastRow((short)(start_row+5));
    data_validation.setEmptyCellAllowed(true);
    data_validation.setShowPromptBox(true);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula(String.valueOf((int)HSSFDateUtil.getExcelDate(df.parse("3/2/2004"))));
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_LESS_THAN);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Less than 3/2/2004", true, true, false );
    this.setCellFormat( fSheet, cellStyle_data );
    this.writeOtherSettings( fSheet, style_1, "-" );

    data_validation.setFirstRow((short)(start_row+6));
    data_validation.setLastRow((short)(start_row+6));
    data_validation.setEmptyCellAllowed(true);
    data_validation.setShowPromptBox(false);
    data_validation.setErrorStyle(HSSFDataValidation.ERROR_STYLE_STOP);
    data_validation.setShowErrorBox(true);
    data_validation.setFirstFormula(String.valueOf((int)HSSFDateUtil.getExcelDate(df.parse("3/2/2004"))));
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_GREATER_OR_EQUAL);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Greater than or equal to 3/2/2004", true, false, true );
    this.setCellFormat( fSheet, cellStyle_data );
    this.writeOtherSettings( fSheet, style_1, "Error box type = STOP" );

    data_validation.setFirstRow((short)(start_row+7));
    data_validation.setLastRow((short)(start_row+7));
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(true);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula(String.valueOf((int)HSSFDateUtil.getExcelDate(df.parse("3/4/2004"))));
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_LESS_OR_EQUAL);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Less than or equal to 3/4/2004", false, true, false );
    this.setCellFormat( fSheet, cellStyle_data );
    this.writeOtherSettings( fSheet, style_1, "-" );

    //"Time" validation type
    this.createDVTypeRow( wb, 2, style_3, "Time ( cells are already formated as time - h:mm)");
    this.createHeaderRow( wb, 2, style_4 );

    df = new SimpleDateFormat("hh:mm");

    start_row += (short)(8+4);
    data_validation = new HSSFDataValidation((short)(start_row),(short)0,(short)(start_row),(short)0);
    data_validation.setDataValidationType(HSSFDataValidation.DATA_TYPE_TIME);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_BETWEEN);
    data_validation.setFirstFormula(String.valueOf(HSSFDateUtil.getExcelDate(df.parse("12:00"))));
    data_validation.setSecondFormula(String.valueOf(HSSFDateUtil.getExcelDate(df.parse("16:00"))));
    data_validation.createErrorBox("Invalid input !", "Something is wrong ; check condition !");
    data_validation.createPromptBox("Hi , dear user !", "So , you just selected me ! Thanks !");
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Between 12:00 and 16:00 ", true, true, true );
    this.setCellFormat( fSheet, cellStyle_time );
    this.writeOtherSettings( fSheet, style_1, "Error box type = STOP" );

    data_validation.setFirstRow((short)(start_row+1));
    data_validation.setLastRow((short)(start_row+1));
    data_validation.setEmptyCellAllowed(false);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_NOT_BETWEEN);
    data_validation.setErrorStyle(HSSFDataValidation.ERROR_STYLE_INFO);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Not between 12:00 and 16:00 ", false, true, true );
    this.setCellFormat( fSheet, cellStyle_time );
    this.writeOtherSettings( fSheet, style_1, "Error box type = INFO" );

    data_validation.setFirstRow((short)(start_row+2));
    data_validation.setLastRow((short)(start_row+2));
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(false);
    data_validation.setFirstFormula(String.valueOf((int)HSSFDateUtil.getExcelDate(df.parse("13:35"))));
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_EQUAL);
    data_validation.setErrorStyle(HSSFDataValidation.ERROR_STYLE_WARNING);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Equal to 13:35", false, false, true );
    this.setCellFormat( fSheet, cellStyle_time );
    this.writeOtherSettings( fSheet, style_1, "Error box type = WARNING" );

    data_validation.setFirstRow((short)(start_row+3));
    data_validation.setLastRow((short)(start_row+3));
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(false);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula(String.valueOf(HSSFDateUtil.getExcelDate(df.parse("13:35"))));
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_NOT_EQUAL);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Not equal to 13:35", false, false, false );
    this.setCellFormat( fSheet, cellStyle_time );
    this.writeOtherSettings( fSheet, style_1, "-" );

    data_validation.setFirstRow((short)(start_row+4));
    data_validation.setLastRow((short)(start_row+4));
    data_validation.setEmptyCellAllowed(true);
    data_validation.setShowPromptBox(false);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula(String.valueOf(HSSFDateUtil.getExcelDate(df.parse("12:00"))));
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_GREATER_THAN);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Greater than 12:00", true, false, false );
    this.setCellFormat( fSheet, cellStyle_time );
    this.writeOtherSettings( fSheet, style_1, "-" );

    data_validation.setFirstRow((short)(start_row+5));
    data_validation.setLastRow((short)(start_row+5));
    data_validation.setEmptyCellAllowed(true);
    data_validation.setShowPromptBox(true);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula(String.valueOf(HSSFDateUtil.getExcelDate(df.parse("12:00"))));
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_LESS_THAN);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Less than 12:00", true, true, false );
    this.setCellFormat( fSheet, cellStyle_time );
    this.writeOtherSettings( fSheet, style_1, "-" );

    data_validation.setFirstRow((short)(start_row+6));
    data_validation.setLastRow((short)(start_row+6));
    data_validation.setEmptyCellAllowed(true);
    data_validation.setShowPromptBox(false);
    data_validation.setErrorStyle(HSSFDataValidation.ERROR_STYLE_STOP);
    data_validation.setShowErrorBox(true);
    data_validation.setFirstFormula(String.valueOf(HSSFDateUtil.getExcelDate(df.parse("14:00"))));
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_GREATER_OR_EQUAL);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Greater than or equal to 14:00", true, false, true );
    this.setCellFormat( fSheet, cellStyle_time );
    this.writeOtherSettings( fSheet, style_1, "Error box type = STOP" );

    data_validation.setFirstRow((short)(start_row+7));
    data_validation.setLastRow((short)(start_row+7));
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(true);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula(String.valueOf(HSSFDateUtil.getExcelDate(df.parse("14:00"))));
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_LESS_OR_EQUAL);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Less than or equal to 14:00", false, true, false );
    this.setCellFormat( fSheet, cellStyle_time );
    this.writeOtherSettings( fSheet, style_1, "-" );

    System.out.println("done !");

    //"Text length" validation type
    System.out.print("    Create sheet for 'Text length' Data Validation type... ");
    fSheet = wb.createSheet("Text length");
    this.createHeaderRow( wb, 3, style_4 );

    data_validation = new HSSFDataValidation((short)1,(short)0,(short)1,(short)0);
    data_validation.setDataValidationType(HSSFDataValidation.DATA_TYPE_TEXT_LENGTH);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_BETWEEN);
    data_validation.setFirstFormula("2");
    data_validation.setSecondFormula("6");
    data_validation.createErrorBox("Invalid input !", "Something is wrong ; check condition !");
    data_validation.createPromptBox("Hi , dear user !", "So , you just selected me ! Thanks !");
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Between 2 and 6 ", true, true, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type = STOP" );

    data_validation.setFirstRow((short)2);
    data_validation.setLastRow((short)2);
    data_validation.setEmptyCellAllowed(false);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_NOT_BETWEEN);
    data_validation.setErrorStyle(HSSFDataValidation.ERROR_STYLE_INFO);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Not between 2 and 6 ", false, true, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type = INFO" );

    data_validation.setFirstRow((short)3);
    data_validation.setLastRow((short)3);
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(false);
    data_validation.setFirstFormula("3");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_EQUAL);
    data_validation.setErrorStyle(HSSFDataValidation.ERROR_STYLE_WARNING);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Equal to 3", false, false, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type = WARNING" );

    data_validation.setFirstRow((short)4);
    data_validation.setLastRow((short)4);
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(false);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula("3");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_NOT_EQUAL);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Not equal to 3", false, false, false );
    this.writeOtherSettings( fSheet, style_1, "-" );

    data_validation.setFirstRow((short)5);
    data_validation.setLastRow((short)5);
    data_validation.setEmptyCellAllowed(true);
    data_validation.setShowPromptBox(false);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula("3");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_GREATER_THAN);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Greater than 3", true, false, false );
    this.writeOtherSettings( fSheet, style_1, "-" );

    data_validation.setFirstRow((short)6);
    data_validation.setLastRow((short)6);
    data_validation.setEmptyCellAllowed(true);
    data_validation.setShowPromptBox(true);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula("3");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_LESS_THAN);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Less than 3", true, true, false );
    this.writeOtherSettings( fSheet, style_1, "-" );

    data_validation.setFirstRow((short)7);
    data_validation.setLastRow((short)7);
    data_validation.setEmptyCellAllowed(true);
    data_validation.setShowPromptBox(false);
    data_validation.setErrorStyle(HSSFDataValidation.ERROR_STYLE_STOP);
    data_validation.setShowErrorBox(true);
    data_validation.setFirstFormula("4");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_GREATER_OR_EQUAL);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Greater than or equal to 4", true, false, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type = STOP" );

    data_validation.setFirstRow((short)8);
    data_validation.setLastRow((short)8);
    data_validation.setEmptyCellAllowed(false);
    data_validation.setShowPromptBox(true);
    data_validation.setShowErrorBox(false);
    data_validation.setFirstFormula("4");
    data_validation.setSecondFormula(null);
    data_validation.setOperator(HSSFDataValidation.OPERATOR_LESS_OR_EQUAL);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "Less than or equal to 4", false, true, false );
    this.writeOtherSettings( fSheet, style_1, "-" );
    System.out.println("done !");

    //Custom Validation type
    System.out.print("    Create sheet for 'Custom' Data Validation type ... ");
    fSheet = wb.createSheet("Custom");
    this.createHeaderRow( wb, 4, style_4 );

    data_validation = new HSSFDataValidation((short)1,(short)0,(short)1,(short)0);
    data_validation.setDataValidationType(HSSFDataValidation.DATA_TYPE_FORMULA);
    data_validation.setFirstFormula("ISNUMBER($A2)");
    data_validation.setSecondFormula(null);
    data_validation.setShowPromptBox(true);
    data_validation.setShowErrorBox(true);
    data_validation.createErrorBox("Invalid input !", "Something is wrong ; check condition !");
    data_validation.createPromptBox("Hi , dear user !", "So , you just selected me ! Thanks !");
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "ISNUMBER(A2)", true, true, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type = STOP" );

    data_validation = new HSSFDataValidation((short)2,(short)0,(short)2,(short)0);
    data_validation.setDataValidationType(HSSFDataValidation.DATA_TYPE_FORMULA);
    data_validation.setFirstFormula("IF(SUM(A2:A3)=5,TRUE,FALSE)");
    data_validation.setSecondFormula(null);
    data_validation.setShowPromptBox(false);
    data_validation.setShowErrorBox(true);
    data_validation.setErrorStyle(HSSFDataValidation.ERROR_STYLE_WARNING);
    data_validation.createErrorBox("Invalid input !", "Something is wrong ; check condition !");
    data_validation.setEmptyCellAllowed(false);
    fSheet.addValidationData(data_validation);
    this.writeDataValidationSettings( fSheet, style_1, style_2, "IF(SUM(A2:A3)=5,TRUE,FALSE)", false, false, true );
    this.writeOtherSettings( fSheet, style_1, "Error box type = WARNING" );

    System.out.println("done !");

    //so , everything it's ok for now ; it remains for you to open the file
    System.out.println("\n    Everything it's ok since we've got so far -:) !\n"+
                       "    In order to complete the test , it remains for you to open the file \n"+
                       "    and see if there are four sheets , as described !");
    System.out.println("        File was saved in \""+resultFile+"\"");

    FileOutputStream fileOut = new FileOutputStream(resultFile);
    wb.write(fileOut);
    fileOut.close();
  }

  private void createDVTypeRow(  HSSFWorkbook wb, int sheetNo , HSSFCellStyle cellStyle, String strTypeDescription)
  {
    HSSFSheet sheet = wb.getSheetAt(sheetNo);
    HSSFRow row = sheet.createRow(sheet.getPhysicalNumberOfRows());
    row = sheet.createRow(sheet.getPhysicalNumberOfRows());
    sheet.addMergedRegion(new Region((short)(sheet.getPhysicalNumberOfRows()-1),(short)0,(short)(sheet.getPhysicalNumberOfRows()-1),(short)5));
    HSSFCell cell = row.createCell((short)0);
    cell.setCellValue(strTypeDescription);
    cell.setCellStyle(cellStyle);
    row = sheet.createRow(sheet.getPhysicalNumberOfRows());
  }

  private void createDVDeescriptionRow(  HSSFWorkbook wb, int sheetNo , HSSFCellStyle cellStyle, String strTypeDescription )
  {
    HSSFSheet sheet = wb.getSheetAt(sheetNo);
    HSSFRow row = sheet.getRow(sheet.getPhysicalNumberOfRows()-1);
    sheet.addMergedRegion(new Region((short)(sheet.getPhysicalNumberOfRows()-1),(short)0,(short)(sheet.getPhysicalNumberOfRows()-1),(short)5));
    HSSFCell cell = row.createCell((short)0);
    cell.setCellValue(strTypeDescription);
    cell.setCellStyle(cellStyle);
    row = sheet.createRow(sheet.getPhysicalNumberOfRows());
  }

  private void createHeaderRow( HSSFWorkbook wb, int sheetNo , HSSFCellStyle cellStyle )
  {
      HSSFSheet sheet = wb.getSheetAt(sheetNo);
      HSSFRow row = sheet.createRow(sheet.getPhysicalNumberOfRows());
      row.setHeight((short)400);
      for ( int i=0; i<6; i++ )
      {
         row.createCell((short)i).setCellStyle( cellStyle );
         if ( i==2 || i==3 || i==4 )
         {
            sheet.setColumnWidth( (short) i, (short) 3500);
         }
         else if ( i== 5)
         {
            sheet.setColumnWidth( (short) i, (short) 10000);
         }
         else
         {
            sheet.setColumnWidth( (short) i, (short) 8000);
         }
      }
      HSSFCell cell = row.getCell((short)0);
      cell.setCellValue("Data validation cells");
      cell = row.getCell((short)1);
      cell.setCellValue("Condition");
      cell = row.getCell((short)2);
      cell.setCellValue("Allow blank");
      cell = row.getCell((short)3);
      cell.setCellValue("Prompt box");
      cell = row.getCell((short)4);
      cell.setCellValue("Error box");
      cell = row.getCell((short)5);
      cell.setCellValue("Other settings");
  }

  private HSSFCellStyle createHeaderStyle(HSSFWorkbook wb)
  {
    HSSFFont font = wb.createFont();
    font.setColor( HSSFColor.WHITE.index );
    font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

    HSSFCellStyle cellStyle = wb.createCellStyle();
    cellStyle.setFillForegroundColor(HSSFColor.BLUE_GREY.index);
    cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
    cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
    cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
    cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
    cellStyle.setLeftBorderColor(HSSFColor.WHITE.index);
    cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
    cellStyle.setTopBorderColor(HSSFColor.WHITE.index);
    cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
    cellStyle.setRightBorderColor(HSSFColor.WHITE.index);
    cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
    cellStyle.setBottomBorderColor(HSSFColor.WHITE.index);
    cellStyle.setFont(font);
    return cellStyle;
  }

  private HSSFCellStyle createStyle( HSSFWorkbook wb, short h_align, short color, boolean bold )
  {
    HSSFFont font = wb.createFont();
    if ( bold )
    {
      font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
    }

    HSSFCellStyle cellStyle = wb.createCellStyle();
    cellStyle.setFont(font);
    cellStyle.setFillForegroundColor(color);
    cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
    cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
    cellStyle.setAlignment(h_align);
    cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
    cellStyle.setLeftBorderColor(HSSFColor.BLACK.index);
    cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
    cellStyle.setTopBorderColor(HSSFColor.BLACK.index);
    cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
    cellStyle.setRightBorderColor(HSSFColor.BLACK.index);
    cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
    cellStyle.setBottomBorderColor(HSSFColor.BLACK.index);

    return cellStyle;
  }

  private HSSFCellStyle createStyle( HSSFWorkbook wb, short h_align )
  {
     return this.createStyle(wb, h_align, HSSFColor.WHITE.index, false);
  }

  private void writeDataValidationSettings( HSSFSheet sheet, HSSFCellStyle style_1, HSSFCellStyle style_2, String strCondition, boolean allowEmpty, boolean inputBox, boolean errorBox  )
  {
    HSSFRow row = sheet.createRow( sheet.getPhysicalNumberOfRows() );
    //condition's string
    HSSFCell cell = row.createCell((short)1);
    cell.setCellStyle(style_1);
    cell.setCellValue(strCondition);
    //allow empty cells
    cell = row.createCell((short)2);
    cell.setCellStyle(style_2);
    cell.setCellValue( ((allowEmpty) ? "yes" : "no") );
    //show input box
    cell = row.createCell((short)3);
    cell.setCellStyle(style_2);
    cell.setCellValue( ((inputBox) ? "yes" : "no") );
    //show error box
    cell = row.createCell((short)4);
    cell.setCellStyle(style_2);
    cell.setCellValue( ((errorBox) ? "yes" : "no") );
  }

  private void setCellFormat( HSSFSheet sheet, HSSFCellStyle cell_style )
  {
    HSSFRow row = sheet.getRow( sheet.getPhysicalNumberOfRows() -1 );
    HSSFCell cell = row.createCell((short)0);
    cell.setCellStyle(cell_style);
  }

  private void writeOtherSettings( HSSFSheet sheet, HSSFCellStyle style, String strStettings )
  {
     HSSFRow row = sheet.getRow( sheet.getPhysicalNumberOfRows() -1 );
     HSSFCell cell = row.createCell((short)5);
     cell.setCellStyle(style);
     cell.setCellValue(strStettings);
  }

  
	public void testAddToExistingSheet() {

		// dvEmpty.xls is a simple one sheet workbook.  With a DataValidations header record but no 
		// DataValidations.  It's important that the example has one SHEETPROTECTION record.
		// Such a workbook can be created in Excel (2007) by adding datavalidation for one cell
		// and then deleting the row that contains the cell.
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("dvEmpty.xls");  
		int dvRow = 0;
		HSSFSheet sheet = wb.getSheetAt(0);
		sheet.createRow(dvRow).createCell((short)0);
		HSSFDataValidation dv = new HSSFDataValidation((short)dvRow, (short)0, (short)dvRow, (short)0);
		
		dv.setDataValidationType(HSSFDataValidation.DATA_TYPE_INTEGER);
		dv.setEmptyCellAllowed(false);
		dv.setOperator(HSSFDataValidation.OPERATOR_EQUAL);
		dv.setFirstFormula("42");
		dv.setErrorStyle(HSSFDataValidation.ERROR_STYLE_STOP);
		dv.setShowPromptBox(true);
		dv.createErrorBox("Error", "The value is wrong");
		dv.setSurppressDropDownArrow(true);

		sheet.addValidationData(dv);
		wb.toString();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			wb.write(baos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		byte[] wbData = baos.toByteArray();
		
		if (false) { // TODO (Jul 2008) fix EventRecordFactory to process unknown records, (and DV records for that matter)
			EventRecordFactory erf = new EventRecordFactory();
			ERFListener erfListener = null; // new MyERFListener();
			erf.registerListener(erfListener, null);
			try {
				POIFSFileSystem fs = new POIFSFileSystem(new ByteArrayInputStream(baos.toByteArray()));
				erf.processRecords(fs.createDocumentInputStream("Workbook"));
			} catch (RecordFormatException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		// else verify record ordering by navigating the raw bytes
		
		byte[] dvHeaderRecStart= { (byte)0xB2, 0x01, 0x12, 0x00, };
		int dvHeaderOffset = findIndex(wbData, dvHeaderRecStart);
		assertTrue(dvHeaderOffset > 0);
		int nextRecIndex = dvHeaderOffset + 22;
		int nextSid 
			= ((wbData[nextRecIndex + 0] << 0) & 0x00FF) 
			+ ((wbData[nextRecIndex + 1] << 8) & 0xFF00)
			;
		// nextSid should be for a DVRecord.  If anything comes between the DV header record 
		// and the DV records, Excel will not be able to open the workbook without error.
		
		if (nextSid == 0x0867) {
			throw new AssertionFailedError("Identified bug 45519");
		}
		assertEquals(DVRecord.sid, nextSid);
	}
	private int findIndex(byte[] largeData, byte[] searchPattern) {
		byte firstByte = searchPattern[0];
		for (int i = 0; i < largeData.length; i++) {
			if(largeData[i] != firstByte) {
				continue;
			}
			boolean match = true;
			for (int j = 1; j < searchPattern.length; j++) {
				if(searchPattern[j] != largeData[i+j]) {
					match = false;
					break;
				}
			}
			if (match) {
				return i;
			}
		}
		return -1;
	}
}
