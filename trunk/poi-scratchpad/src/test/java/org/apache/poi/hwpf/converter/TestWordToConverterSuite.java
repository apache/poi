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
package org.apache.poi.hwpf.converter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.util.XMLHelper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TestWordToConverterSuite
{
    /**
     * YK: a quick hack to exclude failing documents from the suite.
     */
    private static final List<String> failingFiles = Arrays.asList(
        "ProblemExtracting.doc",
        // basic extraction works, but these extractors modify the document, which is a no-go for this Word 6.0 file
        "Bug50955.doc",
        // password protected files
        "PasswordProtected.doc",
        "password_tika_binaryrc4.doc",
        "password_password_cryptoapi.doc",
        // WORD 2.0 file
        "word2.doc"
    );

    public static Stream<Arguments> files() {
        File directory = POIDataSamples.getDocumentInstance().getFile("../document" );
        FilenameFilter ff = (dir, name) -> name.endsWith(".doc") && !failingFiles.contains(name);
        File[] docs = directory.listFiles(ff);
        assertNotNull(docs);
        return Arrays.stream(docs).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("files")
    void testFo(File child) throws Exception {
        HWPFDocumentCore hwpfDocument = AbstractWordUtils.loadDoc( child );

        WordToFoConverter wordToFoConverter = new WordToFoConverter(
                XMLHelper.newDocumentBuilder().newDocument() );
        wordToFoConverter.processDocument( hwpfDocument );

        StringWriter stringWriter = new StringWriter();

        Transformer transformer = XMLHelper.newTransformer();
        transformer.transform(
                new DOMSource( wordToFoConverter.getDocument() ),
                new StreamResult( stringWriter ) );

        // no exceptions
        assertNotNull(stringWriter.toString());
    }

    @ParameterizedTest
    @MethodSource("files")
    void testHtml(File child) throws Exception {
        HWPFDocumentCore hwpfDocument = AbstractWordUtils.loadDoc( child );

        WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                XMLHelper.newDocumentBuilder().newDocument() );
        wordToHtmlConverter.processDocument( hwpfDocument );

        StringWriter stringWriter = new StringWriter();

        Transformer transformer = XMLHelper.newTransformer();
        transformer.setOutputProperty( OutputKeys.METHOD, "html" );
        transformer.transform(
                new DOMSource( wordToHtmlConverter.getDocument() ),
                new StreamResult( stringWriter ) );

        // no exceptions
        assertNotNull(stringWriter.toString());
    }

    @ParameterizedTest
    @MethodSource("files")
    void testText(File child) throws Exception {
        HWPFDocumentCore wordDocument = AbstractWordUtils.loadDoc( child );

        WordToTextConverter wordToTextConverter = new WordToTextConverter(
                XMLHelper.newDocumentBuilder().newDocument() );
        wordToTextConverter.processDocument( wordDocument );

        StringWriter stringWriter = new StringWriter();

        Transformer transformer = XMLHelper.newTransformer();
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
        transformer.setOutputProperty( OutputKeys.METHOD, "text" );
        transformer.transform(
                new DOMSource( wordToTextConverter.getDocument() ),
                new StreamResult( stringWriter ) );

        // no exceptions
        assertNotNull(stringWriter.toString());
    }


}
