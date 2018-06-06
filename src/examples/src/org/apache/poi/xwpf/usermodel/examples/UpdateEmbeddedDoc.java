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

package org.apache.poi.xwpf.usermodel.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/**
 * Tests whether it is possible to successfully update an Excel workbook that is
 * embedded into a WordprocessingML document. Note that the test has currently
 * only been conducted with a binary Excel workbook and NOT yet with a
 * SpreadsheetML workbook embedded into the document.<p>
 * 
 * This code was successfully tested with the following file from the POI test collection:
 * http://svn.apache.org/repos/asf/poi/trunk/test-data/document/EmbeddedDocument.docx
 */
public class UpdateEmbeddedDoc {

    private XWPFDocument doc;
    private File docFile;

    private static final int SHEET_NUM = 0;
    private static final int ROW_NUM = 0;
    private static final int CELL_NUM = 0;
    private static final double NEW_VALUE = 100.98D;
    private static final String BINARY_EXTENSION = "xls";
    private static final String OPENXML_EXTENSION = "xlsx";

    /**
     * Create a new instance of the UpdateEmbeddedDoc class using the following
     * parameters;
     *
     * @param filename An instance of the String class that encapsulates the name
     *                 of and path to a WordprocessingML Word document that contains an
     *                 embedded binary Excel workbook.
     * @throws java.io.FileNotFoundException Thrown if the file cannot be found
     *                                       on the underlying file system.
     * @throws java.io.IOException           Thrown if a problem occurs in the underlying
     *                                       file system.
     */
    public UpdateEmbeddedDoc(String filename) throws FileNotFoundException, IOException {
        this.docFile = new File(filename);
        if (!this.docFile.exists()) {
            throw new FileNotFoundException("The Word document " + filename + " does not exist.");
        }
        try (FileInputStream fis = new FileInputStream(this.docFile)) {
            // Open the Word document file and instantiate the XWPFDocument class.
            this.doc = new XWPFDocument(fis);
        }
    }

    /**
     * Called to update the embedded Excel workbook. As the format and structure
     * of the workbook are known in advance, all this code attempts to do is
     * write a new value into the first cell on the first row of the first
     * worksheet. Prior to executing this method, that cell will contain the
     * value 1.
     *
     * @throws org.apache.poi.openxml4j.exceptions.OpenXML4JException
     *                             Rather
     *                             than use the specific classes (HSSF/XSSF) to handle the embedded
     *                             workbook this method uses those defined in the SS stream. As
     *                             a result, it might be the case that a SpreadsheetML file is
     *                             opened for processing, throwing this exception if that file is
     *                             invalid.
     * @throws java.io.IOException Thrown if a problem occurs in the underlying
     *                             file system.
     */
    public void updateEmbeddedDoc() throws OpenXML4JException, IOException {
        List<PackagePart> embeddedDocs = this.doc.getAllEmbeddedParts();
        for (PackagePart pPart : embeddedDocs) {
            String ext = pPart.getPartName().getExtension();
            if (BINARY_EXTENSION.equals(ext) || OPENXML_EXTENSION.equals(ext)) {
                // Get an InputStream from the package part and pass that
                // to the create method of the WorkbookFactory class. Update
                // the resulting Workbook and then stream that out again
                // using an OutputStream obtained from the same PackagePart.
                try (InputStream is = pPart.getInputStream();
                     Workbook workbook = WorkbookFactory.create(is);
                     OutputStream os = pPart.getOutputStream()) {
                    Sheet sheet = workbook.getSheetAt(SHEET_NUM);
                    Row row = sheet.getRow(ROW_NUM);
                    Cell cell = row.getCell(CELL_NUM);
                    cell.setCellValue(NEW_VALUE);
                    workbook.write(os);
                }
            }
        }

        if (!embeddedDocs.isEmpty()) {
            // Finally, write the newly modified Word document out to file.
            try (FileOutputStream fos = new FileOutputStream(this.docFile)) {
                this.doc.write(fos);
            }
        }
    }

    /**
     * Called to test whether or not the embedded workbook was correctly
     * updated. This method simply recovers the first cell from the first row
     * of the first workbook and tests the value it contains.
     * <p>
     * Note that execution will not continue up to the assertion as the
     * embedded workbook is now corrupted and causes an IllegalArgumentException
     * with the following message
     * <p>
     * <em>java.lang.IllegalArgumentException: Your InputStream was neither an
     * OLE2 stream, nor an OOXML stream</em>
     * <p>
     * to be thrown when the WorkbookFactory.createWorkbook(InputStream) method
     * is executed.
     *
     * @throws org.apache.poi.openxml4j.exceptions.OpenXML4JException
     *                             Rather
     *                             than use the specific classes (HSSF/XSSF) to handle the embedded
     *                             workbook this method uses those defined in the SS stream. As
     *                             a result, it might be the case that a SpreadsheetML file is
     *                             opened for processing, throwing this exception if that file is
     *                             invalid.
     * @throws java.io.IOException Thrown if a problem occurs in the underlying
     *                             file system.
     */
    public void checkUpdatedDoc() throws OpenXML4JException, IOException {
        for (PackagePart pPart : this.doc.getAllEmbeddedParts()) {
            String ext = pPart.getPartName().getExtension();
            if (BINARY_EXTENSION.equals(ext) || OPENXML_EXTENSION.equals(ext)) {
                try (InputStream is = pPart.getInputStream();
                     Workbook workbook = WorkbookFactory.create(is)) {
                    Sheet sheet = workbook.getSheetAt(SHEET_NUM);
                    Row row = sheet.getRow(ROW_NUM);
                    Cell cell = row.getCell(CELL_NUM);
                    if(cell.getNumericCellValue() != NEW_VALUE) {
                        throw new IllegalStateException("Failed to validate document content.");
                    }
                }
            }
        }
    }

    /**
     * Code to test updating of the embedded Excel workbook.
     */
    public static void main(String[] args) throws IOException, OpenXML4JException {
        UpdateEmbeddedDoc ued = new UpdateEmbeddedDoc(args[0]);
        ued.updateEmbeddedDoc();
        ued.checkUpdatedDoc();
    }
}
