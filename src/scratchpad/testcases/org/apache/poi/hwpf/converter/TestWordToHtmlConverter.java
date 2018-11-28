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

import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.util.XMLHelper;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

import static org.apache.poi.POITestCase.assertContains;
import static org.junit.Assert.assertFalse;

/**
 * Test cases for {@link WordToHtmlConverter}
 */
public class TestWordToHtmlConverter {
    private static String getHtmlText(final String sampleFileName) throws Exception {
        return getHtmlText(sampleFileName, false);
    }

    private static String getHtmlText(final String sampleFileName,
            boolean emulatePictureStorage) throws Exception {
        HWPFDocument hwpfDocument = new HWPFDocument(POIDataSamples
                .getDocumentInstance().openResourceAsStream(sampleFileName));

        Document newDocument = XMLHelper.getDocumentBuilderFactory().newDocumentBuilder().newDocument();
        WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                newDocument);

        if (emulatePictureStorage)
        {
            wordToHtmlConverter.setPicturesManager((content, pictureType, suggestedName, widthInches, heightInches) -> suggestedName);
        }

        wordToHtmlConverter.processDocument(hwpfDocument);

        StringWriter stringWriter = new StringWriter();

        Transformer transformer = TransformerFactory.newInstance()
                .newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        transformer.transform(
                new DOMSource(wordToHtmlConverter.getDocument()),
                new StreamResult(stringWriter));

        return stringWriter.toString();
    }

    @Test
    public void testAIOOBTap() throws Exception {
        String result = getHtmlText("AIOOB-Tap.doc");
        assertContains(result, "<table class=\"t1\">");
    }

    @Test
    public void testBug33519() throws Exception {
        String result = getHtmlText("Bug33519.doc");
        assertContains(
                result,
                "\u041F\u043B\u0430\u043D\u0438\u043D\u0441\u043A\u0438 \u0442\u0443\u0440\u043E\u0432\u0435");
        assertContains(result,
                "\u042F\u0432\u043E\u0440 \u0410\u0441\u0435\u043D\u043E\u0432");
    }

    @Test
    public void testBug46610_2() throws Exception {
        String result = getHtmlText("Bug46610_2.doc");
        assertContains(
                result,
                "012345678911234567892123456789312345678941234567890123456789112345678921234567893123456789412345678");
    }

    @Test
    public void testBug46817() throws Exception {
        String result = getHtmlText("Bug46817.doc");
        final String substring = "<table class=\"t1\">";
        assertContains(result, substring);
    }

    @Test
    public void testBug47286() throws Exception {
        String result = getHtmlText("Bug47286.doc");

        assertFalse(result.contains("FORMTEXT"));

        assertContains(result, "color:#4f6228;");
        assertContains(result, "Passport No and the date of expire");
        assertContains(result, "mfa.gov.cy");
    }

    @Test
    public void testBug48075() throws Exception {
        getHtmlText("Bug48075.doc");
    }

    @Test
    public void testBug52583() throws Exception {
        String result = getHtmlText("Bug52583.doc");
        assertContains(
                result,
                "<select><option selected>riri</option><option>fifi</option><option>loulou</option></select>");
    }

    @Test
    public void testBug53182() throws Exception {
        String result = getHtmlText("Bug53182.doc");
        assertFalse(result.contains("italic"));
    }

    @Test
    public void testDocumentProperties() throws Exception {
        String result = getHtmlText("documentProperties.doc");

        assertContains(result, "<title>This is document title</title>");
        assertContains(result,
                "<meta content=\"This is document keywords\" name=\"keywords\">");
    }

    @Test
    public void testEmailhyperlink() throws Exception {
        String result = getHtmlText("Bug47286.doc");
        final String substring = "provisastpet@mfa.gov.cy";
        assertContains(result, substring);
    }

    @Test
    public void testEndnote() throws Exception {
        String result = getHtmlText("endingnote.doc");

        assertContains(
                result,
                "<a class=\"a1 endnoteanchor\" href=\"#endnote_1\" name=\"endnote_back_1\">1</a>");
        assertContains(
                // starting with JDK 9 such unimportant whitespaces may be trimmed
                result.replace("</a> <span", "</a><span"),
                "<a class=\"a1 endnoteindex\" href=\"#endnote_back_1\" name=\"endnote_1\">1</a><span");
        assertContains(result, "Ending note text");
    }

    @Test
    public void testEquation() throws Exception {
        String result = getHtmlText("equation.doc");

        assertContains(result, "<!--Image link to '0.emf' can be here-->");
    }

    @Test
    public void testHyperlink() throws Exception {
        String result = getHtmlText("hyperlink.doc");

        assertContains(result, "<span>Before text; </span><a ");
        assertContains(result,
                "<a href=\"http://testuri.org/\"><span class=\"s1\">Hyperlink text</span></a>");
        assertContains(result, "</a><span>; after text</span>");
    }

    @Test
    public void testInnerTable() throws Exception {
        getHtmlText("innertable.doc");
    }

    @Test
    public void testListsMargins() throws Exception {
        String result = getHtmlText("lists-margins.doc");

        assertContains(result,
                ".s1{display: inline-block; text-indent: 0; min-width: 0.4861111in;}");
        assertContains(result,
                ".s2{display: inline-block; text-indent: 0; min-width: 0.23055555in;}");
        assertContains(result,
                ".s3{display: inline-block; text-indent: 0; min-width: 0.28541666in;}");
        assertContains(result,
                ".s4{display: inline-block; text-indent: 0; min-width: 0.28333333in;}");
        assertContains(result,
                ".p4{text-indent:-0.59652776in;margin-left:-0.70069444in;");
    }

    @Test
    public void testO_kurs_doc() throws Exception {
        getHtmlText("o_kurs.doc");
    }

    @Test
    public void testPageref() throws Exception {
        String result = getHtmlText("pageref.doc");

        assertContains(result, "<a href=\"#userref\">");
        assertContains(result, "<a name=\"userref\">");
        assertContains(result, "1");
    }

    @Test
    public void testPicture() throws Exception {
        String result = getHtmlText("picture.doc", true);

        // picture
        assertContains(result, "src=\"0.emf\"");
        // visible size
        assertContains(result, "width:3.1293333in;height:1.7247736in;");
        // shift due to crop
        assertContains(result, "left:-0.09433333;top:-0.2573611;");
        // size without crop
        assertContains(result, "width:3.4125in;height:2.3253334in;");
    }

    @Test
    public void testPicturesEscher() throws Exception {
        String result = getHtmlText("pictures_escher.doc", true);
        assertContains(result, "<img src=\"s0.PNG\">");
        assertContains(result, "<img src=\"s808.PNG\">");
    }

    @Test
    public void testTableMerges() throws Exception {
        String result = getHtmlText("table-merges.doc");

        assertContains(result, "<td class=\"td1\" colspan=\"3\">");
        assertContains(result, "<td class=\"td2\" colspan=\"2\">");
    }

    @Test
    public void testBug52420() throws Exception {
        String result = getHtmlText("52420.doc");

        assertFalse(result.contains("FORMTEXT"));

        assertContains(result, "\u0417\u0410\u0414\u0410\u041d\u0418\u0415");
        assertContains(result, "\u041f\u0440\u0435\u043f\u043e\u0434\u0430\u0432\u0430\u0442\u0435\u043b\u044c");
    }
}
