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


import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * Demonstrates <em>one</em> way to convert an Excel spreadsheet into a CSV
 * file. This class makes the following assumptions;
 * <list>
 * <li>1. Where the Excel workbook contains more that one worksheet, then a single
 *    CSV file will contain the data from all of the worksheets.</li>
 * <li>2. The data matrix contained in the CSV file will be square. This means that
 *    the number of fields in each record of the CSV file will match the number
 *    of cells in the longest row found in the Excel workbook. Any short records
 *    will be 'padded' with empty fields - an empty field is represented in the
 *    the CSV file in this way - ,,.</li>
 * <li>3. Empty fields will represent missing cells.</li>
 * <li>4. A record consisting of empty fields will be used to represent an empty row
 *    in the Excel workbook.</li>
 * </list>
 * Therefore, if the worksheet looked like this;
 *
 * <pre>
 *  ___________________________________________
 *     |       |       |       |       |       |
 *     |   A   |   B   |   C   |   D   |   E   |
 *  ___|_______|_______|_______|_______|_______|
 *     |       |       |       |       |       |
 *   1 |   1   |   2   |   3   |   4   |   5   |
 *  ___|_______|_______|_______|_______|_______|
 *     |       |       |       |       |       |
 *   2 |       |       |       |       |       |
 *  ___|_______|_______|_______|_______|_______|
 *     |       |       |       |       |       |
 *   3 |       |   A   |       |   B   |       |
 *  ___|_______|_______|_______|_______|_______|
 *     |       |       |       |       |       |
 *   4 |       |       |       |       |   Z   |
 *  ___|_______|_______|_______|_______|_______|
 *     |       |       |       |       |       |
 *   5 | 1,400 |       |  250  |       |       |
 *  ___|_______|_______|_______|_______|_______|
 *
 * </pre>
 *
 * Then, the resulting CSV file will contain the following lines (records);
 * <pre>
 * 1,2,3,4,5
 * ,,,,
 * ,A,,B,
 * ,,,,Z
 * "1,400",,250,,
 * </pre><p>
 * Typically, the comma is used to separate each of the fields that, together,
 * constitute a single record or line within the CSV file. This is not however
 * a hard and fast rule and so this class allows the user to determine which
 * character is used as the field separator and assumes the comma if none other
 * is specified.
 * </p><p>
 * If a field contains the separator then it will be escaped. If the file should
 * obey Excel's CSV formatting rules, then the field will be surrounded with
 * speech marks whilst if it should obey UNIX conventions, each occurrence of
 * the separator will be preceded by the backslash character.
 * </p><p>
 * If a field contains an end of line (EOL) character then it too will be
 * escaped. If the file should obey Excel's CSV formatting rules then the field
 * will again be surrounded by speech marks. On the other hand, if the file
 * should follow UNIX conventions then a single backslash will precede the
 * EOL character. There is no single applicable standard for UNIX and some
 * appications replace the CR with \r and the LF with \n but this class will
 * not do so.
 * </p><p>
 * If the field contains double quotes then that character will be escaped. It
 * seems as though UNIX does not define a standard for this whilst Excel does.
 * Should the CSV file have to obey Excel's formmating rules then the speech
 * mark character will be escaped with a second set of speech marks. Finally, an
 * enclosing set of speah marks will also surround the entire field. Thus, if
 * the following line of text appeared in a cell - "Hello" he said - it would
 * look like this when converted into a field within a CSV file - """Hello"" he
 * said".
 * </p><p>
 * Finally, it is worth noting that talk of CSV 'standards' is really slightly
 * missleading as there is no such thing. It may well be that the code in this
 * class has to be modified to produce files to suit a specific application
 * or requirement.
 * </p>
 * @author Mark B
 * @version 1.00 9th April 2010
 *          1.10 13th April 2010 - Added support for processing all Excel
 *                                 workbooks in a folder along with the ability
 *                                 to specify a field separator character.
 *          2.00 14th April 2010 - Added support for embedded characters; the
 *                                 field separator, EOL and double quotes or
 *                                 speech marks. In addition, gave the client
 *                                 the ability to select how these are handled,
 *                                 either obeying Excel's or UNIX formatting
 *                                 conventions.
 */
public class ToCSV {

    private Workbook workbook = null;
    private ArrayList<ArrayList> csvData = null;
    private int maxRowWidth = 0;
    private int formattingConvention = 0;
    private DataFormatter formatter = null;
    private FormulaEvaluator evaluator = null;
    private String separator = null;

    private static final String CSV_FILE_EXTENSION = ".csv";
    private static final String DEFAULT_SEPARATOR = ",";

    /**
     * Identifies that the CSV file should obey Excel's formatting conventions
     * with regard to escaping certain embedded characters - the field separator,
     * speech mark and end of line (EOL) character
     */
    public static final int EXCEL_STYLE_ESCAPING = 0;

    /**
     * Identifies that the CSV file should obey UNIX formatting conventions
     * with regard to escaping certain embedded characters - the field separator
     * and end of line (EOL) character
     */
    public static final int UNIX_STYLE_ESCAPING = 1;

    /**
     * Process the contents of a folder, convert the contents of each Excel
     * workbook into CSV format and save the resulting file to the specified
     * folder using the same name as the original workbook with the .xls or
     * .xlsx extension replaced by .csv. This method will ensure that the
     * CSV file created contains the comma field separator and that embedded
     * characters such as the field separator, the EOL and double quotes are
     * escaped in accordance with Excel's convention.
     *
     * @param strSource An instance of the String class that encapsulates the
     *        name of and path to either a folder containing those Excel
     *        workbook(s) or the name of and path to an individual Excel workbook
     *        that is/are to be converted.
     * @param strDestination An instance of the String class encapsulating the
     *        name of and path to a folder that will contain the resulting CSV
     *        files.
     * @throws java.io.FileNotFoundException Thrown if any file cannot be located
     *         on the filesystem during processing.
     * @throws java.io.IOException Thrown if the filesystem encounters any
     *         problems during processing.
     * @throws java.lang.IllegalArgumentException Thrown if the values passed
     *         to the strSource parameter refers to a file or folder that does not
     *         exist or if the value passed to the strDestination paramater refers
     *         to a folder that does not exist or simply does not refer to a
     *         folder.
     * @throws org.apache.poi.openxml4j.exceptions.InvalidFormatException Thrown
     *         if the xml markup encountered whilst parsing a SpreadsheetML
     *         file (.xlsx) is invalid.
     */
    public void convertExcelToCSV(String strSource, String strDestination)
                       throws FileNotFoundException, IOException,
                              IllegalArgumentException, InvalidFormatException {

        // Simply chain the call to the overloaded convertExcelToCSV(String,
        // String, String, int) method, pass the default separator and ensure
        // that certain embedded characters are escaped in accordance with
        // Excel's formatting conventions
        this.convertExcelToCSV(strSource, strDestination,
                ToCSV.DEFAULT_SEPARATOR, ToCSV.EXCEL_STYLE_ESCAPING);
    }

    /**
     * Process the contents of a folder, convert the contents of each Excel
     * workbook into CSV format and save the resulting file to the specified
     * folder using the same name as the original workbook with the .xls or
     * .xlsx extension replaced by .csv. This method allows the client to
     * define the field separator but will ensure that embedded characters such
     * as the field separator, the EOL and double quotes are escaped in
     * accordance with Excel's convention.
     *
     * @param strSource An instance of the String class that encapsulates the
     *        name of and path to either a folder containing those Excel
     *        workbook(s) or the name of and path to an individual Excel workbook
     *        that is/are to be converted.
     * @param strDestination An instance of the String class encapsulating the
     *        name of and path to a folder that will contain the resulting CSV
     *        files.
     * @param separator An instance of the String class that encapsulates the
     *        character or characters the client wishes to use as the field
     *        separator.
     * @throws java.io.FileNotFoundException Thrown if any file cannot be located
     *         on the filesystem during processing.
     * @throws java.io.IOException Thrown if the filesystem encounters any
     *         problems during processing.
     * @throws java.lang.IllegalArgumentException Thrown if the values passed
     *         to the strSource parameter refers to a file or folder that does not
     *         exist or if the value passed to the strDestination paramater refers
     *         to a folder that does not exist or simply does not refer to a
     *         folder.
     * @throws org.apache.poi.openxml4j.exceptions.InvalidFormatException Thrown
     *         if the xml markup encounetered whilst parsing a SpreadsheetML
     *         file (.xlsx) is invalid.
     */
    public void convertExcelToCSV(String strSource, String strDestination,
                                  String separator)
                       throws FileNotFoundException, IOException,
                              IllegalArgumentException, InvalidFormatException {

        // Simply chain the call to the overloaded convertExcelToCSV(String,
        // String, String, int) method and ensure that certain embedded
        // characters are escaped in accordance with Excel's formatting
        // conventions
        this.convertExcelToCSV(strSource, strDestination,
                separator, ToCSV.EXCEL_STYLE_ESCAPING);
    }

    /**
     * Process the contents of a folder, convert the contents of each Excel
     * workbook into CSV format and save the resulting file to the specified
     * folder using the same name as the original workbook with the .xls or
     * .xlsx extension replaced by .csv
     *
     * @param strSource An instance of the String class that encapsulates the
     *        name of and path to either a folder containing those Excel
     *        workbook(s) or the name of and path to an individual Excel workbook
     *        that is/are to be converted.
     * @param strDestination An instance of the String class encapsulating the name
     *        of and path to a folder that will contain the resulting CSV files.
     * @param formattingConvention A primitive int whose value will determine
     *        whether certain embedded characters should be escaped in accordance
     *        with Excel's or UNIX formatting conventions. Two constants are
     *        defined to support this option; ToCSV.EXCEL_STYLE_ESCAPING and
     *        ToCSV.UNIX_STYLE_ESCAPING
     * @param separator An instance of the String class encapsulating the
     *        characters or characters that should be used to separate items
     *        on a line within the CSV file.
     * @throws java.io.FileNotFoundException Thrown if any file cannot be located
     *         on the filesystem during processing.
     * @throws java.io.IOException Thrown if the filesystem encounters any
     *         problems during processing.
     * @throws java.lang.IllegalArgumentException Thrown if the values passed
     *         to the strSource parameter refers to a file or folder that does not
     *         exist, if the value passed to the strDestination paramater refers
     *         to a folder that does not exist,  if the value passed to the
     *         strDestination parameter does not refer to a folder or if the
     *         value passed to the formattingConvention parameter is other than
     *         one of the values defined by the constants ToCSV.EXCEL_STYLE_ESCAPING
     *         and ToCSV.UNIX_STYLE_ESCAPING.
     * @throws org.apache.poi.openxml4j.exceptions.InvalidFormatException Thrown
     *         if the xml markup encounetered whilst parsing a SpreadsheetML
     *         file (.xlsx) is invalid.
     */
    public void convertExcelToCSV(String strSource, String strDestination,
                                  String separator, int formattingConvention)
                       throws FileNotFoundException, IOException,
                              IllegalArgumentException, InvalidFormatException {
        File source = new File(strSource);
        File destination = new File(strDestination);
        File[] filesList = null;
        String destinationFilename = null;

        // Check that the source file/folder exists.
        if(!source.exists()) {
            throw new IllegalArgumentException("The source for the Excel " +
                    "file(s) cannot be found.");
        }

        // Ensure thaat the folder the user has chosen to save the CSV files
        // away into firstly exists and secondly is a folder rather than, for
        // instance, a data file.
        if(!destination.exists()) {
            throw new IllegalArgumentException("The folder/directory for the " +
                    "converted CSV file(s) does not exist.");
        }
        if(!destination.isDirectory()) {
            throw new IllegalArgumentException("The destination for the CSV " +
                    "file(s) is not a directory/folder.");
        }

        // Ensure the value passed to the formattingConvention parameter is
        // within range.
        if(formattingConvention != ToCSV.EXCEL_STYLE_ESCAPING &&
           formattingConvention != ToCSV.UNIX_STYLE_ESCAPING) {
            throw new IllegalArgumentException("The value passed to the " +
                    "formattingConvention parameter is out of range.");
        }

        // Copy the spearator character and formatting convention into local
        // variables for use in other methods.
        this.separator = separator;
        this.formattingConvention = formattingConvention;

        // Check to see if the sourceFolder variable holds a reference to
        // a file or a folder full of files.
        if(source.isDirectory()) {
            // Get a list of all of the Excel spreadsheet files (workbooks) in
            // the source folder/directory
            filesList = source.listFiles(new ExcelFilenameFilter());
        }
        else {
            // Assume that it must be a file handle - although there are other
            // options the code should perhaps check - and store the reference
            // into the filesList variable.
            filesList = new File[]{source};
        }

        // Step through each of the files in the source folder and for each
        // open the workbook, convert it's contents to CSV format and then
        // save the resulting file away into the folder specified by the
        // contents of the destination variable. Note that the name of the
        // csv file will be created by taking the name of the Excel file,
        // removing the extension and replacing it with .csv. Note that there
        // is one drawback with this approach; if the folder holding the files
        // contains two workbooks whose names match but one is a binary file
        // (.xls) and the other a SpreadsheetML file (.xlsx), then the names
        // for both CSV files will be identical and one CSV file will,
        // therefore, over-write the other.
        for(File excelFile : filesList) {
            // Open the workbook
            this.openWorkbook(excelFile);

            // Convert it's contents into a CSV file
            this.convertToCSV();

            // Build the name of the csv folder from that of the Excel workbook.
            // Simply replace the .xls or .xlsx file extension with .csv
            destinationFilename = excelFile.getName();
            destinationFilename = destinationFilename.substring(
                    0, destinationFilename.lastIndexOf(".")) +
                    ToCSV.CSV_FILE_EXTENSION;

            // Save the CSV file away using the newly constricted file name
            // and to the specified directory.
            this.saveCSVFile(new File(destination, destinationFilename));
        }
    }

    /**
     * Open an Excel workbook ready for conversion.
     *
     * @param file An instance of the File class that encapsulates a handle
     *        to a valid Excel workbook. Note that the workbook can be in
     *        either binary (.xls) or SpreadsheetML (.xlsx) format.
     * @throws java.io.FileNotFoundException Thrown if the file cannot be located.
     * @throws java.io.IOException Thrown if a problem occurs in the file system.
     * @throws org.apache.poi.openxml4j.exceptions.InvalidFormatException Thrown
     *         if invalid xml is found whilst parsing an input SpreadsheetML
     *         file.
     */
    private void openWorkbook(File file) throws FileNotFoundException,
                                           IOException, InvalidFormatException {
        FileInputStream fis = null;
        try {
            System.out.println("Opening workbook [" + file.getName() + "]");

            fis = new FileInputStream(file);

            // Open the workbook and then create the FormulaEvaluator and
            // DataFormatter instances that will be needed to, respectively,
            // force evaluation of forumlae found in cells and create a
            // formatted String encapsulating the cells contents.
            this.workbook = WorkbookFactory.create(fis);
            this.evaluator = this.workbook.getCreationHelper().createFormulaEvaluator();
            this.formatter = new DataFormatter();
        }
        finally {
            if(fis != null) {
                fis.close();
            }
        }
    }

    /**
     * Called to convert the contents of the currently opened workbook into
     * a CSV file.
     */
    private void convertToCSV() {
        Sheet sheet = null;
        Row row = null;
        int lastRowNum = 0;
        this.csvData = new ArrayList<ArrayList>();

        System.out.println("Converting files contents to CSV format.");

        // Discover how many sheets there are in the workbook....
        int numSheets = this.workbook.getNumberOfSheets();

        // and then iterate through them.
        for(int i = 0; i < numSheets; i++) {

            // Get a reference to a sheet and check to see if it contains
            // any rows.
            sheet = this.workbook.getSheetAt(i);
            if(sheet.getPhysicalNumberOfRows() > 0) {

                // Note down the index number of the bottom-most row and
                // then iterate through all of the rows on the sheet starting
                // from the very first row - number 1 - even if it is missing.
                // Recover a reference to the row and then call another method
                // which will strip the data from the cells and build lines
                // for inclusion in the resylting CSV file.
                lastRowNum = sheet.getLastRowNum();
                for(int j = 0; j <= lastRowNum; j++) {
                    row = sheet.getRow(j);
                    this.rowToCSV(row);
                }
            }
        }
    }

    /**
     * Called to actually save the data recovered from the Excel workbook
     * as a CSV file.
     *
     * @param file An instance of the File class that encapsulates a handle
     *             referring to the CSV file.
     * @throws java.io.FileNotFoundException Thrown if the file cannot be found.
     * @throws java.io.IOException Thrown to indicate and error occurred in the
     *                             underylying file system.
     */
    private void saveCSVFile(File file)
                                     throws FileNotFoundException, IOException {
        FileWriter fw = null;
        BufferedWriter bw = null;
        ArrayList<String> line = null;
        StringBuffer buffer = null;
        String csvLineElement = null;
        try {

            System.out.println("Saving the CSV file [" + file.getName() + "]");

            // Open a writer onto the CSV file.
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);

            // Step through the elements of the ArrayList that was used to hold
            // all of the data recovered from the Excel workbooks' sheets, rows
            // and cells.
            for(int i = 0; i < this.csvData.size(); i++) {
                buffer = new StringBuffer();

                // Get an element from the ArrayList that contains the data for
                // the workbook. This element will itself be an ArrayList
                // containing Strings and each String will hold the data recovered
                // from a single cell. The for() loop is used to recover elements
                // from this 'row' ArrayList one at a time and to write the Strings
                // away to a StringBuffer thus assembling a single line for inclusion
                // in the CSV file. If a row was empty or if it was short, then
                // the ArrayList that contains it's data will also be shorter than
                // some of the others. Therefore, it is necessary to check within
                // the for loop to ensure that the ArrayList contains data to be
                // processed. If it does, then an element will be recovered and
                // appended to the StringBuffer.
                line = this.csvData.get(i);
                for(int j = 0; j < this.maxRowWidth; j++) {
                    if(line.size() > j) {
                        csvLineElement = line.get(j);
                        if(csvLineElement != null) {
                            buffer.append(this.escapeEmbeddedCharacters(
                                    csvLineElement));
                        }
                    }
                    if(j < (this.maxRowWidth - 1)) {
                        buffer.append(this.separator);
                    }
                }

                // Once the line is built, write it away to the CSV file.
                bw.write(buffer.toString().trim());

                // Condition the inclusion of new line characters so as to
                // avoid an additional, superfluous, new line at the end of
                // the file.
                if(i < (this.csvData.size() - 1)) {
                    bw.newLine();
                }
            }
        }
        finally {
            if(bw != null) {
                bw.flush();
                bw.close();
            }
        }
    }

    /**
     * Called to convert a row of cells into a line of data that can later be
     * output to the CSV file.
     *
     * @param row An instance of either the HSSFRow or XSSFRow classes that
     *            encapsulates information about a row of cells recovered from
     *            an Excel workbook.
     */
    private void rowToCSV(Row row) {
        Cell cell = null;
        int lastCellNum = 0;
        ArrayList<String> csvLine = new ArrayList<String>();

        // Check to ensure that a row was recovered from the sheet as it is
        // possible that one or more rows between other populated rows could be
        // missing - blank. If the row does contain cells then...
        if(row != null) {

            // Get the index for the right most cell on the row and then
            // step along the row from left to right recovering the contents
            // of each cell, converting that into a formatted String and
            // then storing the String into the csvLine ArrayList.
            lastCellNum = row.getLastCellNum();
            for(int i = 0; i <= lastCellNum; i++) {
                cell = row.getCell(i);
                if(cell == null) {
                    csvLine.add("");
                }
                else {
                    if(cell.getCellType() != Cell.CELL_TYPE_FORMULA) {
                        csvLine.add(this.formatter.formatCellValue(cell));
                    }
                    else {
                        csvLine.add(this.formatter.formatCellValue(cell, this.evaluator));
                    }
                }
            }
            // Make a note of the index number of the right most cell. This value
            // will later be used to ensure that the matrix of data in the CSV file
            // is square.
            if(lastCellNum > this.maxRowWidth) {
                this.maxRowWidth = lastCellNum;
            }
        }
        this.csvData.add(csvLine);
    }

    /**
     * Checks to see whether the field - which consists of the formatted
     * contents of an Excel worksheet cell encapsulated within a String - contains
     * any embedded characters that must be escaped. The method is able to
     * comply with either Excel's or UNIX formatting conventions in the
     * following manner;
     *
     * With regard to UNIX conventions, if the field contains any embedded
     * field separator or EOL characters they will each be escaped by prefixing
     * a leading backspace character. These are the only changes that have yet
     * emerged following some research as being required.
     *
     * Excel has other embedded character escaping requirements, some that emerged
     * from empirical testing, other through research. Firstly, with regards to
     * any embedded speech marks ("), each occurrence should be escaped with
     * another speech mark and the whole field then surrounded with speech marks.
     * Thus if a field holds <em>"Hello" he said</em> then it should be modified
     * to appear as <em>"""Hello"" he said"</em>. Furthermore, if the field
     * contains either embedded separator or EOL characters, it should also
     * be surrounded with speech marks. As a result <em>1,400</em> would become
     * <em>"1,400"</em> assuming that the comma is the required field separator.
     * This has one consequence in, if a field contains embedded speech marks
     * and embedded separator characters, checks for both are not required as the
     * additional set of speech marks that should be placed around ay field
     * containing embedded speech marks will also account for the embedded
     * separator.
     *
     * It is worth making one further note with regard to embedded EOL
     * characters. If the data in a worksheet is exported as a CSV file using
     * Excel itself, then the field will be surounded with speech marks. If the
     * resulting CSV file is then re-imports into another worksheet, the EOL
     * character will result in the original simgle field occupying more than
     * one cell. This same 'feature' is replicated in this classes behaviour.
     *
     * @param field An instance of the String class encapsulating the formatted
     *        contents of a cell on an Excel worksheet.
     * @return A String that encapsulates the formatted contents of that
     *         Excel worksheet cell but with any embedded separator, EOL or
     *         speech mark characters correctly escaped.
     */
    private String escapeEmbeddedCharacters(String field) {
        StringBuffer buffer = null;

        // If the fields contents should be formatted to confrom with Excel's
        // convention....
        if(this.formattingConvention == ToCSV.EXCEL_STYLE_ESCAPING) {

            // Firstly, check if there are any speech marks (") in the field;
            // each occurrence must be escaped with another set of spech marks
            // and then the entire field should be enclosed within another
            // set of speech marks. Thus, "Yes" he said would become
            // """Yes"" he said"
            if(field.contains("\"")) {
                buffer = new StringBuffer(field.replaceAll("\"", "\\\"\\\""));
                buffer.insert(0, "\"");
                buffer.append("\"");
            }
            else {
                // If the field contains either embedded separator or EOL
                // characters, then escape the whole field by surrounding it
                // with speech marks.
                buffer = new StringBuffer(field);
                if((buffer.indexOf(this.separator)) > -1 ||
                         (buffer.indexOf("\n")) > -1) {
                    buffer.insert(0, "\"");
                    buffer.append("\"");
                }
            }
            return(buffer.toString().trim());
        }
        // The only other formatting convention this class obeys is the UNIX one
        // where any occurrence of the field separator or EOL character will
        // be escaped by preceding it with a backslash.
        else {
            if(field.contains(this.separator)) {
                field = field.replaceAll(this.separator, ("\\\\" + this.separator));
            }
            if(field.contains("\n")) {
                field = field.replaceAll("\n", "\\\\\n");
            }
            return(field);
        }
    }

    /**
     * The main() method contains code that demonstrates how to use the class.
     *
     * @param args An array containing zero, one or more elements all of type
     *        String. Each element will encapsulate an argument specified by the
     *        user when running the program from the command prompt.
     */
    public static void main(String[] args) {
        // Check the number of arguments passed to the main method. There
        // must be two, three or four; the name of and path to either the folder
        // containing the Excel files or an individual Excel workbook that is/are
        // to be converted, the name of and path to the folder to which the CSV
        // files should be written, - optionally - the separator character
        // that should be used to separate individual items (fields) on the
        // lines (records) of the CSV file and - again optionally - an integer
        // that idicates whether the CSV file ought to obey Excel's or UNIX
        // convnetions with regard to formatting fields that contain embedded
        // separator, Speech mark or EOL character(s).
        //
        // Note that the names of the CSV files will be derived from those
        // of the Excel file(s). Put simply the .xls or .xlsx extension will be
        // replaced with .csv. Therefore, if the source folder contains files
        // with matching names but different extensions - Test.xls and Test.xlsx
        // for example - then the CSV file generated from one will overwrite
        // that generated from the other.
        ToCSV converter = null;
        try {
            converter = new ToCSV();
            if(args.length == 2) {
                // Just the Source File/Folder and Destination Folder were
                // passed to the main method.
                converter.convertExcelToCSV(args[0], args[1]);
            }
            else if(args.length == 3){
                // The Source File/Folder, Destination Folder and Separator
                // were passed to the main method.
                converter.convertExcelToCSV(args[0], args[1], args[2]);
            }
            else if(args.length == 4) {
                // The Source File/Folder, Destination Folder, Separator and
                // Formatting Convnetion were passed to the main method.
                converter.convertExcelToCSV(args[0], args[1],
                                            args[2], Integer.parseInt(args[3]));
            }
            else {
                // None or more than four parameters were passed so display
                //a Usage message.
                System.out.println("Usage: java ToCSV [Source File/Folder] " +
                    "[Destination Folder] [Separator] [Formatting Convention]\n" +
                    "\tSource File/Folder\tThis argument should contain the name of and\n" +
                    "\t\t\t\tpath to either a single Excel workbook or a\n" +
                    "\t\t\t\tfolder containing one or more Excel workbooks.\n" +
                    "\tDestination Folder\tThe name of and path to the folder that the\n" +
                    "\t\t\t\tCSV files should be written out into. The\n" +
                    "\t\t\t\tfolder must exist before running the ToCSV\n" +
                    "\t\t\t\tcode as it will not check for or create it.\n" +
                    "\tSeparator\t\tOptional. The character or characters that\n" +
                    "\t\t\t\tshould be used to separate fields in the CSV\n" +
                    "\t\t\t\trecord. If no value is passed then the comma\n" +
                    "\t\t\t\twill be assumed.\n" +
                    "\tFormatting Convention\tOptional. This argument can take one of two\n" +
                    "\t\t\t\tvalues. Passing 0 (zero) will result in a CSV\n" +
                    "\t\t\t\tfile that obeys Excel's formatting conventions\n" +
                    "\t\t\t\twhilst passing 1 (one) will result in a file\n" +
                    "\t\t\t\tthat obeys UNIX formatting conventions. If no\n" +
                    "\t\t\t\tvalue is passed, then the CSV file produced\n" +
                    "\t\t\t\twill obey Excel's formatting conventions.");
            }
        }
        // It is not wise to have such a wide catch clause - Exception is very
        // close to being at the top of the inheritance hierarchy - though it
        // will suffice for this example as it is really not possible to recover
        // easilly from an exceptional set of circumstances at this point in the
        // program. It should however, ideally be replaced with one or more
        // catch clauses optimised to handle more specific problems.
        catch(Exception ex) {
            System.out.println("Caught an: " + ex.getClass().getName());
            System.out.println("Message: " + ex.getMessage());
            System.out.println("Stacktrace follows:.....");
            ex.printStackTrace(System.out);
        }
    }

    /**
     * An instance of this class can be used to control the files returned
     * be a call to the listFiles() method when made on an instance of the
     * File class and that object refers to a folder/directory
     */
    class ExcelFilenameFilter implements FilenameFilter {

        /**
         * Determine those files that will be returned by a call to the
         * listFiles() method. In this case, the name of the file must end with
         * either of the following two extension; '.xls' or '.xlsx'. For the
         * future, it is very possible to parameterise this and allow the
         * containing class to pass, for example, an array of Strings to this
         * class on instantiation. Each element in that array could encapsulate
         * a valid file extension - '.xls', '.xlsx', '.xlt', '.xlst', etc. These
         * could then be used to control which files were returned by the call
         * to the listFiles() method.
         *
         * @param file An instance of the File class that encapsulates a handle
         *             referring to the folder/directory that contains the file.
         * @param name An instance of the String class that encapsulates the
         *             name of the file.
         * @return A boolean value that indicates whether the file should be
         *         included in the array retirned by the call to the listFiles()
         *         method. In this case true will be returned if the name of the
         *         file ends with either '.xls' or '.xlsx' and false will be
         *         returned in all other instances.
         */
        public boolean accept(File file, String name) {
            return(name.endsWith(".xls") || name.endsWith(".xlsx"));
        }
    }
}
