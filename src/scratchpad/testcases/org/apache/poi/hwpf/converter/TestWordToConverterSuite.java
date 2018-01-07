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

import static org.junit.Assert.assertNotNull;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.util.XMLHelper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class TestWordToConverterSuite
{
    /**
     * YK: a quick hack to exclude failing documents from the suite.
     */
    private static List<String> failingFiles = Arrays
            .asList( "ProblemExtracting.doc",
                    "Bug50955.doc" //basic extraction works,
                                    // but these extractors modify the document,
                                    // which is a no-go for this Word 6.0 file
            );

    @Parameterized.Parameters(name="{index}: {0}")
    public static Iterable<Object[]> files() {
        List<Object[]> files = new ArrayList<>();
        File directory = POIDataSamples.getDocumentInstance().getFile(
                "../document" );
        for ( final File child : directory.listFiles( new FilenameFilter()
        {
            @Override
            public boolean accept( File dir, String name )
            {
                return name.endsWith( ".doc" ) && !failingFiles.contains( name );
            }
        } ) )
        {
            files.add(new Object[] { child });
        }

        return files;
    }

    @Parameterized.Parameter
    public File child;

    @Test
    public void testFo() throws Exception {
        HWPFDocumentCore hwpfDocument;
        try {
            hwpfDocument = AbstractWordUtils.loadDoc( child );
        } catch ( Exception exc ) {
            return;
        }

        WordToFoConverter wordToFoConverter = new WordToFoConverter(
                XMLHelper.getDocumentBuilderFactory().newDocumentBuilder().newDocument() );
        wordToFoConverter.processDocument( hwpfDocument );

        StringWriter stringWriter = new StringWriter();

        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();
        transformer.setOutputProperty( OutputKeys.ENCODING, "utf-8" );
        transformer.setOutputProperty( OutputKeys.INDENT, "false" );
        transformer.transform(
                new DOMSource( wordToFoConverter.getDocument() ),
                new StreamResult( stringWriter ) );

        // no exceptions
        assertNotNull(stringWriter.toString());
    }

    @Test
    public void testHtml() throws Exception
    {
        HWPFDocumentCore hwpfDocument;
        try {
            hwpfDocument = AbstractWordUtils.loadDoc( child );
        } catch ( Exception exc ) {
            return;
        }

        WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                XMLHelper.getDocumentBuilderFactory().newDocumentBuilder().newDocument() );
        wordToHtmlConverter.processDocument( hwpfDocument );

        StringWriter stringWriter = new StringWriter();

        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();
        transformer.setOutputProperty( OutputKeys.ENCODING, "utf-8" );
        transformer.setOutputProperty( OutputKeys.INDENT, "false" );
        transformer.setOutputProperty( OutputKeys.METHOD, "html" );
        transformer.transform(
                new DOMSource( wordToHtmlConverter.getDocument() ),
                new StreamResult( stringWriter ) );

        // no exceptions
        assertNotNull(stringWriter.toString());
    }

    @Test
    public void testText() throws Exception
    {
        HWPFDocumentCore wordDocument;
        try {
            wordDocument = AbstractWordUtils.loadDoc( child );
        } catch ( Exception exc ) {
            return;
        }

        WordToTextConverter wordToTextConverter = new WordToTextConverter(
                XMLHelper.getDocumentBuilderFactory().newDocumentBuilder().newDocument() );
        wordToTextConverter.processDocument( wordDocument );

        StringWriter stringWriter = new StringWriter();

        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();
        transformer.setOutputProperty( OutputKeys.ENCODING, "utf-8" );
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
        transformer.setOutputProperty( OutputKeys.METHOD, "text" );
        transformer.transform(
                new DOMSource( wordToTextConverter.getDocument() ),
                new StreamResult( stringWriter ) );

        // no exceptions
        assertNotNull(stringWriter.toString());
    }
}
