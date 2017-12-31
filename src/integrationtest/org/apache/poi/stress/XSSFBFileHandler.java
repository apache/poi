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
package org.apache.poi.stress;

import java.io.*;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.XLSBUnsupportedException;
import org.apache.poi.xssf.extractor.XSSFBEventBasedExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

public class XSSFBFileHandler extends AbstractFileHandler {

    static {
        //add expected failures here:
        AbstractFileHandler.EXPECTED_EXTRACTOR_FAILURES.add("spreadsheet/protected_passtika.xlsb");
    }

    @Override
    public void handleFile(InputStream stream, String path) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtils.copy(stream, out);

        final byte[] bytes = out.toByteArray();
        try (OPCPackage opcPackage = OPCPackage.open(new ByteArrayInputStream(bytes))) {
            testOne(opcPackage);
        }

        testNotHandledByWorkbookException(OPCPackage.open(new ByteArrayInputStream(bytes)));
    }

    private void testNotHandledByWorkbookException(OPCPackage pkg) throws IOException {
        try {
            new XSSFWorkbook(pkg).close();
        } catch (XLSBUnsupportedException e) {
            //this is what we'd expect
            //swallow
        }
    }

    @Override
    public void handleExtracting(File file) throws Exception {
        OPCPackage pkg = OPCPackage.open(file, PackageAccess.READ);
        try {
            testOne(pkg);
        } finally {
            pkg.close();
        }

        pkg = OPCPackage.open(file, PackageAccess.READ);
        try {
            testNotHandledByWorkbookException(pkg);
        } finally {
            pkg.close();
        }
    }

    private void testOne(OPCPackage pkg) throws Exception {
        XSSFBEventBasedExcelExtractor ex = new XSSFBEventBasedExcelExtractor(pkg);
        String txt = ex.getText();
        if (txt.length() < 1) {
            throw new RuntimeException("Should have gotten some text.");
        }
    }

    @Test
    public void testLocal() throws Exception {
        File file = new File("test-data/spreadsheet/Simple.xlsb");
        try (FileInputStream stream = new FileInputStream(file)) {
            handleFile(stream, file.getPath());
        }
        handleExtracting(file);
    }
}
