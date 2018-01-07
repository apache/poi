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

package org.apache.poi.xssf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Centralises logic for finding/opening sample files in the test-data/spreadsheet folder. 
 * 
 * @author Josh Micich
 */
public class XSSFTestDataSamples {
    /**
     * Used by {@link #writeOutAndReadBack(Workbook, String)}.  If a
     * value is set for this in the System Properties, the xlsx file
     * will be written out to that directory.
     */
    public static final String TEST_OUTPUT_DIR = "poi.test.xssf.output.dir";

    public static File getSampleFile(String sampleFileName) {
        return HSSFTestDataSamples.getSampleFile(sampleFileName);
    }
    public static OPCPackage openSamplePackage(String sampleName) {
        try {
            return OPCPackage.open(HSSFTestDataSamples.openSampleFileStream(sampleName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static XSSFWorkbook openSampleWorkbook(String sampleName) {
        InputStream is = HSSFTestDataSamples.openSampleFileStream(sampleName);
        try {
            return new XSSFWorkbook(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Write out workbook <code>wb</code> to {@link #TEST_OUTPUT_DIR}/testName.xlsx
     * (or create a temporary file if <code>TEST_OUTPUT_DIR</code> is not defined).
     *
     * @param wb the workbook to write
     * @param testName a fragment of the filename
     * @return the location where the workbook was saved
     * @throws IOException
     */
    public static <R extends Workbook> File writeOut(R wb, String testName) throws IOException {
        final File file = getOutputFile(testName);
        writeOut(wb, file);
        return file;
    }
    
    private static <R extends Workbook> void writeOut(R wb, File file) throws IOException {
        IOUtils.write(wb,  new FileOutputStream(file));
    }
    
    // Anticipates the location of where a workbook will be written to
    // Note that if TEST_OUTPUT_DIR is not set, this will create temporary files
    // with unique names. Subsequent calls with the same argument may return a different file.
    // Gets a test data sample file, deleting the file if it exists.
    // This is used in preparation for writing a workbook out to the returned output file.
    // testName is a filename fragment and should not include the extension
    private static File getOutputFile(String testName) throws IOException {
        final String testOutputDir = System.getProperty(TEST_OUTPUT_DIR);
        final File file;
        if (testOutputDir != null) {
            // In case user provided testName with a file extension, don't repeat the file extension a second time
            final String testNameWithExtension = testName.endsWith(".xlsx") ? testName : testName + ".xlsx";
            // FIXME: may want to defer to the TempFile with a persistent file creation strategy to the test output dir
            // This would add the random value in the middle of the filename so that test runs wouldn't overwrite files
            file = new File(testOutputDir, testNameWithExtension);
        }
        else {
            file = TempFile.createTempFile(testName, ".xlsx");
        }
        if (file.exists()) {
            file.delete();
        }
        return file;
    }
    
    /**
     * Write out workbook <code>wb</code> to a memory buffer
     *
     * @param wb the workbook to write
     * @return the memory buffer
     * @throws IOException
     */
    public static <R extends Workbook> ByteArrayOutputStream writeOut(R wb) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
        wb.write(out);
        return out;
    }
    
    /**
     * Write out the workbook then closes the workbook. 
     * This should be used when there is insufficient memory to have
     * both workbooks open.
     * 
     * Make sure there are no references to any objects in the workbook
     * so that garbage collection may free the workbook.
     * 
     * After calling this method, null the reference to <code>wb</code>,
     * then call {@link #readBack(File)} or {@link #readBackAndDelete(File)} to re-read the file.
     * 
     * Alternatively, use {@link #writeOutAndClose(Workbook)} to use a ByteArrayOutputStream/ByteArrayInputStream
     * to avoid creating a temporary file. However, this may complicate the calling
     * code to avoid having the workbook, BAOS, and BAIS open at the same time.
     *
     * @param wb
     * @param testName file name to be used to write to a file. This file will be cleaned up by a call to readBack(String)
     * @return workbook location
     * @throws RuntimeException if {@link #TEST_OUTPUT_DIR} System property is not set
     */
    public static <R extends Workbook> File writeOutAndClose(R wb, String testName) {
        try {
            File file = writeOut(wb, testName);
            // Do not close the workbook if there was a problem writing the workbook
            wb.close();
            return file;
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    /**
     * Write out workbook <code>wb</code> to a memory buffer,
     * then close the workbook
     *
     * @param wb the workbook to write
     * @return the memory buffer
     * @throws IOException
     */
    public static <R extends Workbook> ByteArrayOutputStream writeOutAndClose(R wb) {
        try {
            ByteArrayOutputStream out = writeOut(wb);
            // Do not close the workbook if there was a problem writing the workbook
            wb.close();
            return out;
        }
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Read back a workbook that was written out to a file with
     * {@link #writeOut(Workbook, String))} or {@link #writeOutAndClose(Workbook, String)}.
     * Deletes the file after reading back the file.
     * Does not delete the file if an exception is raised.
     *
     * @param file the workbook file to read and delete
     * @return the read back workbook
     * @throws IOException
     */
    public static XSSFWorkbook readBackAndDelete(File file) throws IOException {
        XSSFWorkbook wb = readBack(file);
        // do not delete the file if there's an error--might be helpful for debugging 
        file.delete();
        return wb;
    }
    
    /**
     * Read back a workbook that was written out to a file with
     * {@link #writeOut(Workbook, String)} or {@link #writeOutAndClose(Workbook, String)}.
     *
     * @param file the workbook file to read
     * @return the read back workbook
     * @throws IOException
     */
    public static XSSFWorkbook readBack(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            return new XSSFWorkbook(in);
        }
        finally {
            in.close();
        }
    }
    
    /**
     * Read back a workbook that was written out to a memory buffer with
     * {@link #writeOut(Workbook)} or {@link #writeOutAndClose(Workbook)}.
     *
     * @param file the workbook file to read
     * @return the read back workbook
     * @throws IOException
     */
    public static XSSFWorkbook readBack(ByteArrayOutputStream out) throws IOException {
        InputStream is = new ByteArrayInputStream(out.toByteArray());
        out.close();
        try {
            return new XSSFWorkbook(is);
        }
        finally {
            is.close();
        }
    }
    
    /**
     * Write out and read back using a memory buffer to avoid disk I/O.
     * If there is not enough memory to have two workbooks open at the same time,
     * consider using:
     * 
     * Workbook wb = new XSSFWorkbook();
     * String testName = "example";
     * 
     * <code>
     * File file = writeOutAndClose(wb, testName);
     * // clear all references that would prevent the workbook from getting garbage collected
     * wb = null;
     * Workbook wbBack = readBackAndDelete(file);
     * </code>
     *
     * @param wb the workbook to write out
     * @return the read back workbook
     */
    public static <R extends Workbook> R writeOutAndReadBack(R wb) {
        Workbook result;
        try {
            result = readBack(writeOut(wb));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        @SuppressWarnings("unchecked")
        R r = (R) result;
        return r;
    }
    
    /**
     * Write out, close, and read back the workbook using a memory buffer to avoid disk I/O.
     *
     * @param wb the workbook to write out and close
     * @return the read back workbook
     */
    public static <R extends Workbook> R writeOutCloseAndReadBack(R wb) {
        Workbook result;
        try {
            result = readBack(writeOutAndClose(wb));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        @SuppressWarnings("unchecked")
        R r = (R) result;
        return r;
        
    }
    
    /**
     * Writes the Workbook either into a file or into a byte array, depending on presence of 
     * the system property {@value #TEST_OUTPUT_DIR}, and reads it in a new instance of the Workbook back.
     * If TEST_OUTPUT_DIR is set, the file will NOT be deleted at the end of this function.
     * @param wb workbook to write
     * @param testName file name to be used if writing into a file. The old file with the same name will be overridden.
     * @return new instance read from the stream written by the wb parameter.
     */
    
    public static <R extends Workbook> R writeOutAndReadBack(R wb, String testName) {
        if (System.getProperty(TEST_OUTPUT_DIR) == null) {
            return writeOutAndReadBack(wb);
        } else {
            try {
                Workbook result = readBack(writeOut(wb, testName));
                @SuppressWarnings("unchecked")
                R r = (R) result;
                return r;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
