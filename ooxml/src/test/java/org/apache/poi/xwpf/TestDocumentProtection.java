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
package org.apache.poi.xwpf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.util.TempFile;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;

class TestDocumentProtection {

    @Test
    void testShouldReadEnforcementProperties() throws IOException {

        XWPFDocument documentWithoutDocumentProtectionTag = XWPFTestDataSamples.openSampleDocument("documentProtection_no_protection.docx");
        assertFalse(documentWithoutDocumentProtectionTag.isEnforcedReadonlyProtection());
        assertFalse(documentWithoutDocumentProtectionTag.isEnforcedFillingFormsProtection());
        assertFalse(documentWithoutDocumentProtectionTag.isEnforcedCommentsProtection());
        assertFalse(documentWithoutDocumentProtectionTag.isEnforcedTrackedChangesProtection());
        documentWithoutDocumentProtectionTag.close();

        XWPFDocument documentWithoutEnforcement = XWPFTestDataSamples.openSampleDocument("documentProtection_no_protection_tag_existing.docx");
        assertFalse(documentWithoutEnforcement.isEnforcedReadonlyProtection());
        assertFalse(documentWithoutEnforcement.isEnforcedFillingFormsProtection());
        assertFalse(documentWithoutEnforcement.isEnforcedCommentsProtection());
        assertFalse(documentWithoutEnforcement.isEnforcedTrackedChangesProtection());
        documentWithoutEnforcement.close();

        XWPFDocument documentWithReadonlyEnforcement = XWPFTestDataSamples.openSampleDocument("documentProtection_readonly_no_password.docx");
        assertTrue(documentWithReadonlyEnforcement.isEnforcedReadonlyProtection());
        assertFalse(documentWithReadonlyEnforcement.isEnforcedFillingFormsProtection());
        assertFalse(documentWithReadonlyEnforcement.isEnforcedCommentsProtection());
        assertFalse(documentWithReadonlyEnforcement.isEnforcedTrackedChangesProtection());
        documentWithReadonlyEnforcement.close();

        XWPFDocument documentWithFillingFormsEnforcement = XWPFTestDataSamples.openSampleDocument("documentProtection_forms_no_password.docx");
        assertTrue(documentWithFillingFormsEnforcement.isEnforcedFillingFormsProtection());
        assertFalse(documentWithFillingFormsEnforcement.isEnforcedReadonlyProtection());
        assertFalse(documentWithFillingFormsEnforcement.isEnforcedCommentsProtection());
        assertFalse(documentWithFillingFormsEnforcement.isEnforcedTrackedChangesProtection());
        documentWithFillingFormsEnforcement.close();

        XWPFDocument documentWithCommentsEnforcement = XWPFTestDataSamples.openSampleDocument("documentProtection_comments_no_password.docx");
        assertFalse(documentWithCommentsEnforcement.isEnforcedFillingFormsProtection());
        assertFalse(documentWithCommentsEnforcement.isEnforcedReadonlyProtection());
        assertTrue(documentWithCommentsEnforcement.isEnforcedCommentsProtection());
        assertFalse(documentWithCommentsEnforcement.isEnforcedTrackedChangesProtection());
        documentWithCommentsEnforcement.close();

        XWPFDocument documentWithTrackedChangesEnforcement = XWPFTestDataSamples.openSampleDocument("documentProtection_trackedChanges_no_password.docx");
        assertFalse(documentWithTrackedChangesEnforcement.isEnforcedFillingFormsProtection());
        assertFalse(documentWithTrackedChangesEnforcement.isEnforcedReadonlyProtection());
        assertFalse(documentWithTrackedChangesEnforcement.isEnforcedCommentsProtection());
        assertTrue(documentWithTrackedChangesEnforcement.isEnforcedTrackedChangesProtection());
        documentWithTrackedChangesEnforcement.close();
    }

    @Test
    void testShouldEnforceForReadOnly() throws IOException {
        //		XWPFDocument document = createDocumentFromSampleFile("test-data/document/documentProtection_no_protection.docx");
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("documentProtection_no_protection.docx");
        assertFalse(document.isEnforcedReadonlyProtection());

        document.enforceReadonlyProtection();

        assertTrue(document.isEnforcedReadonlyProtection());
        document.close();
    }

    @Test
    void testShouldEnforceForFillingForms() throws IOException {
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("documentProtection_no_protection.docx");
        assertFalse(document.isEnforcedFillingFormsProtection());

        document.enforceFillingFormsProtection();

        assertTrue(document.isEnforcedFillingFormsProtection());
        document.close();
    }

    @Test
    void testShouldEnforceForComments() throws IOException {
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("documentProtection_no_protection.docx");
        assertFalse(document.isEnforcedCommentsProtection());

        document.enforceCommentsProtection();

        assertTrue(document.isEnforcedCommentsProtection());
        document.close();
    }

    @Test
    void testShouldEnforceForTrackedChanges() throws IOException {
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("documentProtection_no_protection.docx");
        assertFalse(document.isEnforcedTrackedChangesProtection());

        document.enforceTrackedChangesProtection();

        assertTrue(document.isEnforcedTrackedChangesProtection());
        document.close();
    }

    @Test
    void testShouldUnsetEnforcement() throws IOException {
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("documentProtection_readonly_no_password.docx");
        assertTrue(document.isEnforcedReadonlyProtection());

        document.removeProtectionEnforcement();

        assertFalse(document.isEnforcedReadonlyProtection());
        document.close();
    }

    @Test
    void testIntegration() throws IOException {
        XWPFDocument doc1 = new XWPFDocument();

        XWPFParagraph p1 = doc1.createParagraph();

        XWPFRun r1 = p1.createRun();
        r1.setText("Lorem ipsum dolor sit amet.");
        doc1.enforceCommentsProtection();

        File tempFile = TempFile.createTempFile("documentProtectionFile", ".docx");
        FileOutputStream out = new FileOutputStream(tempFile);

        doc1.write(out);
        out.close();

        FileInputStream inputStream = new FileInputStream(tempFile);
        XWPFDocument doc2 = new XWPFDocument(inputStream);
        inputStream.close();

        assertTrue(doc2.isEnforcedCommentsProtection());
        doc2.close();
        doc1.close();
    }

    @Test
    void testUpdateFields() throws IOException {
        XWPFDocument doc = new XWPFDocument();
        assertFalse(doc.isEnforcedUpdateFields());
        doc.enforceUpdateFields();
        assertTrue(doc.isEnforcedUpdateFields());
        doc.close();
    }

    @Test
    void bug56076_read() throws IOException {
        // test legacy xored-hashed password
        assertEquals("64CEED7E", CryptoFunctions.xorHashPassword("Example"));
        // check leading 0
        assertEquals("0005CB00", CryptoFunctions.xorHashPassword("34579"));

        // test document write protection with password
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("bug56076.docx");
        boolean isValid = document.validateProtectionPassword("Example");
        assertTrue(isValid);
        document.close();
    }

    @Test
    void bug56076_write() throws IOException {
        // test document write protection with password
        XWPFDocument doc1 = new XWPFDocument();
        doc1.enforceCommentsProtection("Example", HashAlgorithm.sha512);
        XWPFDocument doc2 = XWPFTestDataSamples.writeOutAndReadBack(doc1);
        doc1.close();
        boolean isValid = doc2.validateProtectionPassword("Example");
        assertTrue(isValid);
        doc2.close();
    }
}
