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

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.TestCase;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocument;

/**
 * Test cases for {@link WordToFoConverter}
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
public class TestWordToHtmlConverter extends TestCase
{
    private static String getHtmlText( final String sampleFileName )
            throws Exception
    {
        HWPFDocument hwpfDocument = new HWPFDocument( POIDataSamples
                .getDocumentInstance().openResourceAsStream( sampleFileName ) );

        WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .newDocument() );
        wordToHtmlConverter.processDocument( hwpfDocument );

        StringWriter stringWriter = new StringWriter();

        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
        transformer.setOutputProperty( OutputKeys.ENCODING, "utf-8" );
        transformer.setOutputProperty( OutputKeys.METHOD, "html" );
        transformer.transform(
                new DOMSource( wordToHtmlConverter.getDocument() ),
                new StreamResult( stringWriter ) );

        String result = stringWriter.toString();
        return result;
    }

    public void testBug33519() throws Exception
    {
        getHtmlText( "Bug33519.doc" );
    }

    public void testBug46610_2() throws Exception
    {
        String result = getHtmlText( "Bug46610_2.doc" );
        assertTrue( result
                .contains( "012345678911234567892123456789312345678941234567890123456789112345678921234567893123456789412345678" ) );
    }

    public void testEquation() throws Exception
    {
        String result = getHtmlText( "equation.doc" );

        assertTrue( result
                .contains( "<!--Image link to '0.emf' can be here-->" ) );
    }

    public void testAIOOBTap() throws Exception
    {
        String result = getHtmlText( "AIOOB-Tap.doc" );

        assertTrue( result.substring( 0, 2000 ).contains( "<table>" ) );
    }

    public void testHyperlink() throws Exception
    {
        String result = getHtmlText( "hyperlink.doc" );

        assertTrue( result.contains( "<a href=\"http://testuri.org/\">" ) );
        assertTrue( result.contains( "Hyperlink text" ) );
    }

    public void testInnerTable() throws Exception
    {
        getHtmlText( "innertable.doc" );
    }

    public void testPageref() throws Exception
    {
        String result = getHtmlText( "pageref.doc" );

        assertTrue( result.contains( "<a href=\"#userref\">" ) );
        assertTrue( result.contains( "1" ) );
    }
}
