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

package org.apache.poi.openxml4j.opc.compliance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.poi.POIDataSamples;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.PartAlreadyExistsException;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.poifs.crypt.temp.AesZipFileZipEntrySource;
import org.apache.poi.util.TempFile;
import org.junit.jupiter.api.Test;

/**
 * Test Open Packaging Convention package model compliance.
 *
 * M1.11 : A package implementer shall neither create nor recognize a part with
 * a part name derived from another part name by appending segments to it.
 */
class TestOPCCompliancePackageModel {

    private static File getTempFile() throws IOException {
        File tf = TempFile.createTempFile("TODELETEIFEXIST", ".docx");
        assertTrue(tf::delete);
        return tf;
    }

    /**
     * A package implementer shall neither create nor recognize a part with a
     * part name derived from another part name by appending segments to it.
     * [M1.11]
     */
    @Test
    void testPartNameDerivationAdditionFailure() throws InvalidFormatException, IOException {
        File tf = getTempFile();
        try (OPCPackage pkg = OPCPackage.create(tf)) {
            PackagePartName name = PackagingURIHelper.createPartName("/word/document.xml");
            PackagePartName nameDerived = PackagingURIHelper.createPartName("/word/document.xml/image1.gif");
            pkg.createPart(name, ContentTypes.XML);

            assertThrows(InvalidOperationException.class, () -> pkg.createPart(nameDerived, ContentTypes.EXTENSION_GIF),
                    "A package implementer shall neither create nor recognize a part with a part name derived from another " +
                            "part name by appending segments to it. [M1.11]");
            pkg.revert();
        } finally {
            assertTrue(tf::delete);
        }
    }

    /**
     * A package implementer shall neither create nor recognize a part with a
     * part name derived from another part name by appending segments to it.
     * [M1.11]
     */
    @Test
    void testPartNameDerivationReadingFailure() {
        String filename = "OPCCompliance_DerivedPartNameFAIL.docx";
        assertThrows(InvalidFormatException.class, () ->
                        OPCPackage.open(POIDataSamples.getOpenXML4JInstance().openResourceAsStream(filename)),
                "A package implementer shall neither create nor recognize a part with a part name derived from another" +
                        " part name by appending segments to it. [M1.11]"
        );
    }

    @Test
    void testInvalidformatExceptionZipSource() throws IOException {
        try (AesZipFileZipEntrySource source = AesZipFileZipEntrySource.createZipEntrySource(
                POIDataSamples.getOpenXML4JInstance().openResourceAsStream("OPCCompliance_DerivedPartNameFAIL.docx"))) {
            assertThrows(InvalidFormatException.class, () ->
                            OPCPackage.open(source),
                    "Should fail for invalid file"
            );
        }
    }

    @Test
    void testInvalidformatExceptionFile() {
        assertThrows(InvalidFormatException.class,
                () -> OPCPackage.open(POIDataSamples.getOpenXML4JInstance().
                                getFile("OPCCompliance_DerivedPartNameFAIL.docx"), PackageAccess.READ),
                "Should fail for invalid file"
        );
    }

    /**
     * Rule M1.12 : Packages shall not contain equivalent part names and package
     * implementers shall neither create nor recognize packages with equivalent
     * part names.
     */
    @Test
    void testAddPackageAlreadyAddFailure() throws IOException, InvalidFormatException {
        File tf = getTempFile();
        try (OPCPackage pkg = OPCPackage.create(tf)) {
            PackagePartName name1 = PackagingURIHelper.createPartName("/word/document.xml");
            PackagePartName name2 = PackagingURIHelper.createPartName("/word/document.xml");

            pkg.createPart(name1, ContentTypes.XML);
            assertThrows(PartAlreadyExistsException.class, () -> pkg.createPart(name2, ContentTypes.XML),
                    "Packages shall not contain equivalent part names and package implementers shall neither create nor " +
                            "recognize packages with equivalent part names. [M1.12]"
            );
            pkg.revert();
        } finally {
            assertTrue(tf::delete);
        }
    }

    /**
     * Rule M1.12 : Packages shall not contain equivalent part names and package
     * implementers shall neither create nor recognize packages with equivalent
     * part names.
     */
    @Test
    void testAddPackageAlreadyAddFailure2() throws IOException, InvalidFormatException {
        File tf = getTempFile();
        try (OPCPackage pkg = OPCPackage.create(tf)) {
            PackagePartName partName = PackagingURIHelper.createPartName("/word/document.xml");
            pkg.createPart(partName, ContentTypes.XML);
            assertThrows(InvalidOperationException.class, () -> pkg.createPart(partName, ContentTypes.XML),
                    "Packages shall not contain equivalent part names and package implementers shall neither create nor " +
                            "recognize packages with equivalent part names. [M1.12]"
            );
            pkg.revert();
        } finally {
            assertTrue(tf::delete);
        }
    }

    /**
     * Try to add a relationship to a relationship part.
     * <p>
     * Check rule M1.25: The Relationships part shall not have relationships to
     * any other part. Package implementers shall enforce this requirement upon
     * the attempt to create such a relationship and shall treat any such
     * relationship as invalid.
     */
    @Test
    void testAddRelationshipRelationshipsPartFailure() throws IOException, InvalidFormatException {
        File tf = getTempFile();
        try (OPCPackage pkg = OPCPackage.create(tf)) {
            PackagePartName name1 = PackagingURIHelper.createPartName("/test/_rels/document.xml.rels");

            assertThrows(InvalidOperationException.class,
                    () -> pkg.addRelationship(name1, TargetMode.INTERNAL, PackageRelationshipTypes.CORE_DOCUMENT),
                    "The Relationships part shall not have relationships to any other part [M1.25]"
            );
            pkg.revert();
        } finally {
            assertTrue(tf::delete);
        }
    }

    @Test
    void testToString() throws IOException {
        File tf = getTempFile();
        try (OPCPackage pkg = OPCPackage.create(tf)) {
            assertEquals("OPCPackage{" +
                        "packageAccess=READ_WRITE, " +
                        "relationships=null, " +
                        "packageProperties=Name: /docProps/core.xml - Content Type: application/vnd.openxmlformats-package.core-properties+xml, " +
                        "isDirty=false}", pkg.toString());
        } finally {
            assertTrue(tf::delete);
        }
    }
}
