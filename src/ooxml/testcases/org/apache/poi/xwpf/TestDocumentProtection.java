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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class TestDocumentProtection extends TestCase {

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

    public void testShouldEnforceForReadOnly() throws Exception {
        //		XWPFDocument document = createDocumentFromSampleFile("test-data/document/documentProtection_no_protection.docx");
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("documentProtection_no_protection.docx");
        assertFalse(document.isEnforcedReadonlyProtection());

        document.enforceReadonlyProtection();

        assertTrue(document.isEnforcedReadonlyProtection());
    }

    public void testShouldEnforceForFillingForms() throws Exception {
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("documentProtection_no_protection.docx");
        assertFalse(document.isEnforcedFillingFormsProtection());

        document.enforceFillingFormsProtection();

        assertTrue(document.isEnforcedFillingFormsProtection());
    }

    public void testShouldEnforceForComments() throws Exception {
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("documentProtection_no_protection.docx");
        assertFalse(document.isEnforcedCommentsProtection());

        document.enforceCommentsProtection();

        assertTrue(document.isEnforcedCommentsProtection());
    }

    public void testShouldEnforceForTrackedChanges() throws Exception {
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("documentProtection_no_protection.docx");
        assertFalse(document.isEnforcedTrackedChangesProtection());

        document.enforceTrackedChangesProtection();

        assertTrue(document.isEnforcedTrackedChangesProtection());
    }

    public void testShouldUnsetEnforcement() throws Exception {
        XWPFDocument document = XWPFTestDataSamples.openSampleDocument("documentProtection_readonly_no_password.docx");
        assertTrue(document.isEnforcedReadonlyProtection());

        document.removeProtectionEnforcement();

        assertFalse(document.isEnforcedReadonlyProtection());
    }

    public void testIntegration() throws Exception {
        XWPFDocument doc = new XWPFDocument();

        XWPFParagraph p1 = doc.createParagraph();

        XWPFRun r1 = p1.createRun();
        r1.setText("Lorem ipsum dolor sit amet.");
        doc.enforceCommentsProtection();

        File tempFile = File.createTempFile("documentProtectionFile", ".docx");
        FileOutputStream out = new FileOutputStream(tempFile);

        doc.write(out);
        out.close();

        FileInputStream inputStream = new FileInputStream(tempFile);
        XWPFDocument document = new XWPFDocument(inputStream);
        inputStream.close();

        assertTrue(document.isEnforcedCommentsProtection());
    }

}
