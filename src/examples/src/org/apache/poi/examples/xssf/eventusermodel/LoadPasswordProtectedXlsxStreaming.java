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

package org.apache.poi.examples.xssf.eventusermodel;

import java.io.InputStream;

import org.apache.poi.examples.xssf.usermodel.LoadPasswordProtectedXlsx;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.crypt.temp.AesZipFileZipEntrySource;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFReader.SheetIterator;

/**
 * An example that loads a password protected workbook and counts the sheets.
 * The example highlights how to do this in streaming way.
 * <p><ul>
 * <li>The example demonstrates that all temp files are removed.
 * <li><code>AesZipFileZipEntrySource</code> is used to ensure that temp files are encrypted.
 * </ul><p>
 */
@SuppressWarnings({"java:S106","java:S4823","java:S1192"})
public final class LoadPasswordProtectedXlsxStreaming {

    private LoadPasswordProtectedXlsxStreaming() {}

    public static void main(String[] args) throws Exception {
        LoadPasswordProtectedXlsx.execute(args, LoadPasswordProtectedXlsxStreaming::printSheetCount);
    }

    private static void printSheetCount(final InputStream inputStream) throws Exception {
        try (AesZipFileZipEntrySource source = AesZipFileZipEntrySource.createZipEntrySource(inputStream);
             OPCPackage pkg = OPCPackage.open(source)) {
            XSSFReader reader = new XSSFReader(pkg);
            SheetIterator iter = (SheetIterator)reader.getSheetsData();
            int count = 0;
            while(iter.hasNext()) {
                iter.next();
                count++;
            }
            System.out.println("sheet count: " + count);
        }
    }
}
