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
package org.apache.poi.hwpf.converter;

import static org.apache.poi.POITestCase.assertContains;

import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.util.XMLHelper;
import org.junit.jupiter.api.Test;

/**
 * Test cases for {@link WordToFoConverter}
 */
public class TestWordToFoConverter
{
    private static String getFoText( final String sampleFileName )
            throws Exception
    {
        HWPFDocument hwpfDocument = new HWPFDocument( POIDataSamples
                .getDocumentInstance().openResourceAsStream( sampleFileName ) );

        WordToFoConverter wordToFoConverter = new WordToFoConverter(
                XMLHelper.newDocumentBuilder().newDocument() );
        wordToFoConverter.processDocument( hwpfDocument );

        StringWriter stringWriter = new StringWriter();

        Transformer transformer = XMLHelper.newTransformer();
        transformer.transform(
                new DOMSource( wordToFoConverter.getDocument() ),
                new StreamResult( stringWriter ) );

        return stringWriter.toString();
    }

    @Test
    void testDocumentProperties() throws Exception
    {
        String result = getFoText( "documentProperties.doc" );

        assertContains(
                result,
                "<dc:title xmlns:dc=\"http://purl.org/dc/elements/1.1/\">This is document title</dc:title>" );
        assertContains(
                result,
                "<pdf:Keywords xmlns:pdf=\"http://ns.adobe.com/pdf/1.3/\">This is document keywords</pdf:Keywords>" );
    }

    @Test
    void testEndnote() throws Exception
    {
        String result = getFoText( "endingnote.doc" );

        assertContains( result,
                "<fo:basic-link id=\"endnote_back_1\" internal-destination=\"endnote_1\">" );
        assertContains( result,
                "<fo:inline baseline-shift=\"super\" font-size=\"smaller\">1</fo:inline>" );
        assertContains( result,
                "<fo:basic-link id=\"endnote_1\" internal-destination=\"endnote_back_1\">" );
        assertContains( result,
                "<fo:inline baseline-shift=\"super\" font-size=\"smaller\">1 </fo:inline>" );
        assertContains( result, "Ending note text" );
    }

    @Test
    void testEquation() throws Exception
    {
        final String sampleFileName = "equation.doc";
        String result = getFoText( sampleFileName );

        assertContains( result, "<!--Image link to '0.emf' can be here-->" );
    }

    @Test
    void testHyperlink() throws Exception
    {
        final String sampleFileName = "hyperlink.doc";
        String result = getFoText( sampleFileName );

        assertContains( result,
                "<fo:basic-link external-destination=\"http://testuri.org/\">" );
        assertContains( result, "Hyperlink text" );
    }

    @Test
    void testInnerTable() throws Exception
    {
        final String sampleFileName = "innertable.doc";
        String result = getFoText( sampleFileName );

        assertContains( result,
                "padding-end=\"0.0in\" padding-start=\"0.0in\" width=\"1.0770833in\"" );
    }

    @Test
    void testPageBreak() throws Exception
    {
        final String sampleFileName = "page-break.doc";
        String result = getFoText( sampleFileName );

        assertContains( result, "<fo:block break-before=\"page\"" );
    }

    @Test
    void testPageBreakBefore() throws Exception
    {
        final String sampleFileName = "page-break-before.doc";
        String result = getFoText( sampleFileName );

        assertContains( result, "<fo:block break-before=\"page\"" );
    }

    @Test
    void testPageref() throws Exception
    {
        final String sampleFileName = "pageref.doc";
        String result = getFoText( sampleFileName );

        assertContains( result,
                "<fo:basic-link internal-destination=\"bookmark_userref\">" );
        assertContains( result, "1" );
        assertContains( result, "<fo:inline id=\"bookmark_userref\">" );
    }
}
