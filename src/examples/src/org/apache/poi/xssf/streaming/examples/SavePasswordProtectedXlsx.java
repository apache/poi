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

package org.apache.poi.xssf.streaming.examples;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import org.apache.poi.examples.util.TempFileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.crypt.temp.EncryptedTempData;
import org.apache.poi.poifs.crypt.temp.SXSSFWorkbookWithCustomZipEntrySource;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;

/**
 * An example that outputs a simple generated workbook that is password protected.
 * The example highlights how to do this in streaming way.
 * <p><ul>
 * <li>The example demonstrates that all temp files are removed.
 * <li><code>SXSSFWorkbookWithCustomZipEntrySource</code> extends SXSSFWorkbook to ensure temp files are encrypted.
 * </ul><p>
 */
public class SavePasswordProtectedXlsx {

    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            throw new IllegalArgumentException("Expected 2 params: filename and password");
        }
        TempFileUtils.checkTempFiles();
        String filename = args[0];
        String password = args[1];
        SXSSFWorkbookWithCustomZipEntrySource wb = new SXSSFWorkbookWithCustomZipEntrySource();
        try {
            for(int i = 0; i < 10; i++) {
                SXSSFSheet sheet = wb.createSheet("Sheet" + i);
                for(int r = 0; r < 1000; r++) {
                    SXSSFRow row = sheet.createRow(r);
                    for(int c = 0; c < 100; c++) {
                        SXSSFCell cell = row.createCell(c);
                        cell.setCellValue("abcd");
                    }
                }
            }
            EncryptedTempData tempData = new EncryptedTempData();
            try {
                wb.write(tempData.getOutputStream());
                save(tempData.getInputStream(), filename, password);
                System.out.println("Saved " + filename);
            } finally {
                tempData.dispose();
            }
        } finally {
            wb.close();
            wb.dispose();
        }
        TempFileUtils.checkTempFiles();
    }
    
    public static void save(final InputStream inputStream, final String filename, final String pwd)
            throws InvalidFormatException, IOException, GeneralSecurityException {

        try (POIFSFileSystem fs = new POIFSFileSystem();
             OPCPackage opc = OPCPackage.open(inputStream);
             FileOutputStream fos = new FileOutputStream(filename)) {
            EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
            Encryptor enc = Encryptor.getInstance(info);
            enc.confirmPassword(pwd);
            opc.save(enc.getDataStream(fs));
            fs.writeFilesystem(fos);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

}
