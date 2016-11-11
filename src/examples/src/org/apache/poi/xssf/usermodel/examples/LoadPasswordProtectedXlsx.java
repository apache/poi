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

package org.apache.poi.xssf.usermodel.examples;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.poi.crypt.examples.EncryptionUtils;
import org.apache.poi.examples.util.TempFileUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.temp.AesZipFileZipEntrySource;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * An example that loads a password protected workbook and counts the sheets.
 * <p><ul>
 * <li>The example demonstrates that all temp files are removed.
 * <li><code>AesZipFileZipEntrySource</code> is used to ensure that temp files are encrypted.
 * </ul><p>
 */
public class LoadPasswordProtectedXlsx {
    
    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            throw new IllegalArgumentException("Expected 2 params: filename and password");
        }
        TempFileUtils.checkTempFiles();
        String filename = args[0];
        String password = args[1];
        FileInputStream fis = new FileInputStream(filename);
        try {
            InputStream unencryptedStream = EncryptionUtils.decrypt(fis, password);
            try {
                printSheetCount(unencryptedStream);
            } finally {
                IOUtils.closeQuietly(unencryptedStream);
            }
        } finally {
            IOUtils.closeQuietly(fis);
        }
        TempFileUtils.checkTempFiles();
    }
    
    public static void printSheetCount(final InputStream inputStream) throws Exception {
        AesZipFileZipEntrySource source = AesZipFileZipEntrySource.createZipEntrySource(inputStream);
        try {
            OPCPackage pkg = OPCPackage.open(source);
            try {
                XSSFWorkbook workbook = new XSSFWorkbook(pkg);
                try {
                    System.out.println("sheet count: " + workbook.getNumberOfSheets());
                } finally {
                    IOUtils.closeQuietly(workbook);
                }
            } finally {
                IOUtils.closeQuietly(pkg);
            }
        } finally {
            IOUtils.closeQuietly(source);
        }
    }

}
