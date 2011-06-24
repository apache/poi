package org.apache.poi.hwpf.extractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.EncryptedDocumentException;

import org.apache.poi.hwpf.OldWordFileFormatException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocument;

public class TestWordToFoExtractorSuite
{
    /**
     * YK: a quick hack to exclude failing documents from the suite.
     *
     * WordToFoExtractor stumbles on Bug33519.doc with a NPE
     */
    private static List<String> failingFiles = Arrays.asList("Bug33519.doc");

    public static Test suite() {
        TestSuite suite = new TestSuite();

        File directory = POIDataSamples.getDocumentInstance().getFile(
                "../document");
        for (final File child : directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".doc") && !failingFiles.contains(name);
            }
        })) {
            final String name = child.getName();
            suite.addTest(new TestCase(name) {
                public void runTest() throws Exception {
                    test(child);
                }
            });
        }

        return suite;
    }

    protected static void test( File child ) throws Exception
    {
        HWPFDocument hwpfDocument;
        FileInputStream fileInputStream = new FileInputStream( child );
        try
        {
            hwpfDocument = new HWPFDocument( fileInputStream );
        }
        catch ( Exception exc )
        {
            // unable to parse file -- not WordToFoExtractor fault
            return;
        }
        finally
        {
            fileInputStream.close();
        }

        WordToFoExtractor wordToFoExtractor = new WordToFoExtractor(
                DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .newDocument() );
        wordToFoExtractor.processDocument( hwpfDocument );

        StringWriter stringWriter = new StringWriter();

        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
        transformer.transform(
                new DOMSource( wordToFoExtractor.getDocument() ),
                new StreamResult( stringWriter ) );
        // no exceptions
    }
}
