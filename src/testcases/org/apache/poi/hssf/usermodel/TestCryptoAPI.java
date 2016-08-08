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

package org.apache.poi.hssf.usermodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.junit.AfterClass;
import org.junit.Test;

public class TestCryptoAPI {
    final HSSFITestDataProvider ssTests = HSSFITestDataProvider.instance;

    @AfterClass
    public static void resetPW() {
        Biff8EncryptionKey.setCurrentUserPassword(null);
    }

    @Test
    public void bug59857() throws IOException {
        Biff8EncryptionKey.setCurrentUserPassword("abc");
        HSSFWorkbook wb1 = ssTests.openSampleWorkbook("xor-encryption-abc.xls");
        String textExpected = "Sheet1\n1\n2\n3\n";
        String textActual = new ExcelExtractor(wb1).getText();
        assertEquals(textExpected, textActual);
        wb1.close();

        Biff8EncryptionKey.setCurrentUserPassword("password");
        HSSFWorkbook wb2 = ssTests.openSampleWorkbook("password.xls");
        textExpected = "A ZIP bomb is a variant of mail-bombing. After most commercial mail servers began checking mail with anti-virus software and filtering certain malicious file types, trojan horse viruses tried to send themselves compressed into archives, such as ZIP, RAR or 7-Zip. Mail server software was then configured to unpack archives and check their contents as well. That gave black hats the idea to compose a \"bomb\" consisting of an enormous text file, containing, for example, only the letter z repeated millions of times. Such a file compresses into a relatively small archive, but its unpacking (especially by early versions of mail servers) would use a high amount of processing power, RAM and swap space, which could result in denial of service. Modern mail server computers usually have sufficient intelligence to recognize such attacks as well as sufficient processing power and memory space to process malicious attachments without interruption of service, though some are still susceptible to this technique if the ZIP bomb is mass-mailed.";
        textActual = new ExcelExtractor(wb2).getText();
        assertTrue(textActual.contains(textExpected));
        wb2.close();

        Biff8EncryptionKey.setCurrentUserPassword("freedom");
        HSSFWorkbook wb3 = ssTests.openSampleWorkbook("35897-type4.xls");
        textExpected = "Sheet1\nhello there!\n";
        textActual = new ExcelExtractor(wb3).getText();
        assertEquals(textExpected, textActual);
        wb3.close();
    }
}
