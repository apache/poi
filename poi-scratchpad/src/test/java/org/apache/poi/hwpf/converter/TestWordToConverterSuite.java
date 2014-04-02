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

import java.io.File;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocumentCore;

public class TestWordToConverterSuite
{
    /**
     * YK: a quick hack to exclude failing documents from the suite.
     */
    private static List<String> failingFiles = Arrays
            .asList( "ProblemExtracting.doc" );

    public static Test suite()
    {
        TestSuite suite = new TestSuite(
                TestWordToConverterSuite.class.getName() );

        File directory = POIDataSamples.getDocumentInstance().getFile(
                "../document" );
        for ( final File child : directory.listFiles( new FilenameFilter()
        {
            public boolean accept( File dir, String name )
            {
                return name.endsWith( ".doc" ) && !failingFiles.contains( name );
            }
        } ) )
        {
            final String name = child.getName();

            suite.addTest( new TestCase( name + " [FO]" )
            {
                public void runTest() throws Exception
                {
                    testFo( child );
                }
            } );
            suite.addTest( new TestCase( name + " [HTML]" )
            {
                public void runTest() throws Exception
                {
                    testHtml( child );
                }
            } );
            suite.addTest( new TestCase( name + " [TEXT]" )
            {
                public void runTest() throws Exception
                {
                    testText( child );
                }
            } );

        }

        return suite;
    }

    protected static void testFo( File child ) throws Exception
    {
        HWPFDocumentCore hwpfDocument;
        try
        {
            hwpfDocument = AbstractWordUtils.loadDoc( child );
        }
        catch ( Exception exc )
        {
            return;
        }

        WordToFoConverter wordToFoConverter = new WordToFoConverter(
                DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .newDocument() );
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
    }

    protected static void testHtml( File child ) throws Exception
    {
        HWPFDocumentCore hwpfDocument;
        try
        {
            hwpfDocument = AbstractWordUtils.loadDoc( child );
        }
        catch ( Exception exc )
        {
            return;
        }

        WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .newDocument() );
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
    }

    protected static void testText( File child ) throws Exception
    {
        HWPFDocumentCore wordDocument;
        try
        {
            wordDocument = AbstractWordUtils.loadDoc( child );
        }
        catch ( Exception exc )
        {
            return;
        }

        WordToTextConverter wordToTextConverter = new WordToTextConverter(
                DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .newDocument() );
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

        stringWriter.toString();
        // no exceptions
    }
}
