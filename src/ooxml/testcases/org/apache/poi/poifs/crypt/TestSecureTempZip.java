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

package org.apache.poi.poifs.crypt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.util.ZipEntrySource;
import org.apache.poi.poifs.crypt.temp.AesZipFileZipEntrySource;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.extractor.XSSFBEventBasedExcelExtractor;
import org.apache.poi.xssf.extractor.XSSFEventBasedExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlException;
import org.junit.Assume;
import org.junit.Test;

public class TestSecureTempZip {

    /**
     * Test case for #59841 - this is an example on how to use encrypted temp files,
     * which are streamed into POI opposed to having everything in memory
     */
    @Test
    public void protectedTempZip() throws IOException, GeneralSecurityException, XmlException, OpenXML4JException {
        File tikaProt = XSSFTestDataSamples.getSampleFile("protected_passtika.xlsx");
        FileInputStream fis = new FileInputStream(tikaProt);
        POIFSFileSystem poifs = new POIFSFileSystem(fis);
        EncryptionInfo ei = new EncryptionInfo(poifs);
        Decryptor dec = ei.getDecryptor();
        boolean passOk = dec.verifyPassword("tika");
        assertTrue(passOk);

        // extract encrypted ooxml file and write to custom encrypted zip file 
        InputStream is = dec.getDataStream(poifs);
        
        // provide ZipEntrySource to poi which decrypts on the fly
        ZipEntrySource source = AesZipFileZipEntrySource.createZipEntrySource(is);

        // test the source
        OPCPackage opc = OPCPackage.open(source);
        String expected = "This is an Encrypted Excel spreadsheet.";
        
        XSSFEventBasedExcelExtractor extractor = new XSSFEventBasedExcelExtractor(opc);
        extractor.setIncludeSheetNames(false);
        String txt = extractor.getText();
        assertEquals(expected, txt.trim());
        
        XSSFWorkbook wb = new XSSFWorkbook(opc);
        txt = wb.getSheetAt(0).getRow(0).getCell(0).getStringCellValue();
        assertEquals(expected, txt);

        extractor.close();
        
        wb.close();
        opc.close();
        source.close();
        poifs.close();
        fis.close();
    }

    /**
     * Now try with xlsb.
     */
    @Test
    public void protectedXLSBZip() throws IOException, GeneralSecurityException, XmlException, OpenXML4JException {
        //The test file requires that JCE unlimited be installed.
        //If it isn't installed, skip this test.
        int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
        Assume.assumeTrue("Please install JCE Unlimited Strength Jurisdiction Policy files for AES 256",
                maxKeyLen == 2147483647);

        File tikaProt = XSSFTestDataSamples.getSampleFile("protected_passtika.xlsb");
        FileInputStream fis = new FileInputStream(tikaProt);
        POIFSFileSystem poifs = new POIFSFileSystem(fis);
        EncryptionInfo ei = new EncryptionInfo(poifs);
        Decryptor dec = ei.getDecryptor();
        boolean passOk = dec.verifyPassword("tika");
        assertTrue(passOk);

        // extract encrypted ooxml file and write to custom encrypted zip file
        InputStream is = dec.getDataStream(poifs);

        // provide ZipEntrySource to poi which decrypts on the fly
        ZipEntrySource source = AesZipFileZipEntrySource.createZipEntrySource(is);

        // test the source
        OPCPackage opc = OPCPackage.open(source);
        String expected = "You can't see me";

        XSSFBEventBasedExcelExtractor extractor = new XSSFBEventBasedExcelExtractor(opc);
        extractor.setIncludeSheetNames(false);
        String txt = extractor.getText();
        assertEquals(expected, txt.trim());

        extractor.close();
        opc.close();
        poifs.close();
        fis.close();
    }

}
