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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.poifs.crypt.HashAlgorithm;
import org.apache.poi.util.TempFile;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.Test;

public class TestDocumentProtection {

    @Test
    public void testShouldReadEnforcementProperties() throws Exception {

        XWPFDocument documentWithoutDocumentProtectionTag = XWPFTestDataSamples.openSampleDocument("documentProtection_no_protection.docx");
        assertFalse(documentWithoutDocumentProtectionTag.isEnforcedReadonlyProtection());
        assertFalse(documentWithoutDocumentProtectionTag.isEnforcedFillingFormsProtection());
        assertFalse(documentWithoutDocumentProtectionTag.isEnforcedCommentsProtection());
        assertFalse(documentWithoutDocumentProtectionTag.isEnforcedTrackedChangesProtection());

        XWPFDocument documentWithoutEnforcement = XWPFTestDataSamples.openSampleDocument("documentProtection_no_protection_tag_existing.docx");
        assertFalse(documentWithoutEnforcement.isEnforcedReadonlyProtection());
        assertFalse(documentWithoutEnforcement.isEnforcedFillingFormsProtection());
        assertFalse(documentWithoutEnforcement.isEnforcedCommentsProtection());
        assertFalse(documentWithoutEnforcement.isEnforcedTrackedChangesProtection());

        XWPFDocument documentWithReadonlyEnforcement = XWPFTestDataSamples.openSampleDocument("documentProtection_readonly_no_password.docx");
        assertTrue(documentWithReadonlyEnforcement.isEnforcedReadonlyProtection());
        assertFalse(documentWithReadonlyEnforcement.isEnforcedFillingFormsProtection());
        assertFalse(documentWithReadonlyEnforcement.isEnforcedCommentsProtection());
        assertFalse(documentWithReadonlyEnforcement.isEnforcedTrackedChangesProtection());

        XWPFDocument documentWithFillingFormsEnforcement = XWPFTestDataSamples.openSampleDocument("documentProtection_forms_no_password.docx");
        assertTrue(documentWithFillingFormsEnforcement.isEnforcedFillingFormsProtection());
        assertFalse(documentWithFillingFormsEnforcement.isEnforcedReadonlyProtection());
        assertFalse(documentWithFillingFormsEnforcement.isEnforcedCommentsProtection());
        assertFalse(documentWithFillingFormsEnforcement.isEnforcedTrackedChangesProtection());

        XWPFDocument documentWithCommentsEnforcement = XWPFTestDataSamples.openSampleDocument("documentProtection_comments_no_password.docx");
        assertFalse(documentWithCommentsEnforcement.isEnforcedFillingFormsProtection());
        assertFalse(documentWithCommentsEnforcement.isEnforcedReadonlyProtection());
        assertTrue(documentWithCommentsEnforcement.isEnforcedCommentsProtection());
        assertFalse(documentWithCommentsEnforcement.isEnforcedTrackedChangesProtection());

        XWPFDocument documentWithTrackedChangesEnforcement = XWPFTestDataSamples.openSampleDocument("documentProtection_trackedChanges_no_password.docx");
        assertFalse(documentWithTrackedChangesEnforcement.isEnforcedFillingFormsProtection());
        assertFalse(documentWithTrackedChangesEnforcement.isEnforcedReadonlyProtection());
        assertFalse(documentWithTrackedChangesEnforcement.isEnforcedCommentsProtection());
        assertTrue(documentWithTrackedChangesEnforcement.isEnforcedTrackedChangesProtection());

    }

    @Test
    public void testShouldEnforceForReadOnly() throws Exception {
        //		XWPFDocument document = createDocumentFromSampleFile("test-data/document/documentProtection_no_protection.docx");
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("documentProtection_no_protection.docx");
        assertFalse(document.isEnforcedReadonlyProtection());

        document.enforceReadonlyProtection();

        assertTrue(document.isEnforcedReadonlyProtection());
    }

    @Test
    public void testShouldEnforceForFillingForms() throws Exception {
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("documentProtection_no_protection.docx");
        assertFalse(document.isEnforcedFillingFormsProtection());

        document.enforceFillingFormsProtection();

        assertTrue(document.isEnforcedFillingFormsProtection());
    }

    @Test
    public void testShouldEnforceForComments() throws Exception {
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("documentProtection_no_protection.docx");
        assertFalse(document.isEnforcedCommentsProtection());

        document.enforceCommentsProtection();

        assertTrue(document.isEnforcedCommentsProtection());
    }

    @Test
    public void testShouldEnforceForTrackedChanges() throws Exception {
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("documentProtection_no_protection.docx");
        assertFalse(document.isEnforcedTrackedChangesProtection());

        document.enforceTrackedChangesProtection();

        assertTrue(document.isEnforcedTrackedChangesProtection());
    }

    @Test
    public void testShouldUnsetEnforcement() throws Exception {
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("documentProtection_readonly_no_password.docx");
        assertTrue(document.isEnforcedReadonlyProtection());

        document.removeProtectionEnforcement();

        assertFalse(document.isEnforcedReadonlyProtection());
    }

    @Test
    public void testIntegration() throws Exception {
        XWPFDocument doc = new XWPFDocument();

        XWPFParagraph p1 = doc.createParagraph();

        XWPFRun r1 = p1.createRun();
        r1.setText("Lorem ipsum dolor sit amet.");
        doc.enforceCommentsProtection();

        File tempFile = TempFile.createTempFile("documentProtectionFile", ".docx");
        FileOutputStream out = new FileOutputStream(tempFile);

        doc.write(out);
        out.close();

        FileInputStream inputStream = new FileInputStream(tempFile);
        XWPFDocument document = new XWPFDocument(inputStream);
        inputStream.close();

        assertTrue(document.isEnforcedCommentsProtection());
    }

    @Test
    public void testUpdateFields() throws Exception {
        XWPFDocument doc = new XWPFDocument();
        assertFalse(doc.isEnforcedUpdateFields());
        doc.enforceUpdateFields();
        assertTrue(doc.isEnforcedUpdateFields());
    }

    @Test
    public void bug56076_read() throws Exception {
        // test legacy xored-hashed password
        assertEquals("64CEED7E", CryptoFunctions.xorHashPassword("Example"));
        // check leading 0
        assertEquals("0005CB00", CryptoFunctions.xorHashPassword("34579"));

        // test document write protection with password
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("bug56076.docx");
        boolean isValid = document.validateProtectionPassword("Example");
        assertTrue(isValid);
    }

    @Test
    public void bug56076_write() throws Exception {
        // test document write protection with password
        XWPFDocument document = new XWPFDocument();
        document.enforceCommentsProtection("Example", HashAlgorithm.sha512);
        document = XWPFTestDataSamples.writeOutAndReadBack(document);
        boolean isValid = document.validateProtectionPassword("Example");
        assertTrue(isValid);
    }
}
