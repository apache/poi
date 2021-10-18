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

import static org.apache.poi.POITestCase.assertContains;
import static org.apache.poi.POITestCase.assertNotContained;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.util.XMLHelper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.w3c.dom.Document;

/**
 * Test cases for {@link WordToHtmlConverter}
 */
public class TestWordToHtmlConverter {
    private static final POIDataSamples SAMPLES = POIDataSamples.getDocumentInstance();

    @ParameterizedTest
    @CsvSource({
        "AIOOB-Tap.doc, <table class=\"t1\">",
        "Bug33519.doc, " +
            "\u041F\u043B\u0430\u043D\u0438\u043D\u0441\u043A\u0438 \u0442\u0443\u0440\u043E\u0432\u0435|" +
            "\u042F\u0432\u043E\u0440 \u0410\u0441\u0435\u043D\u043E\u0432",
        "Bug46610_2.doc, 012345678911234567892123456789312345678941234567890123456789112345678921234567893123456789412345678",
        "Bug46817.doc, <table class=\"t1\">",
        "Bug47286.doc, " +
            "!FORMTEXT|" +
            "color:#4f6228;|" +
            "Passport No and the date of expire|" +
            "mfa.gov.cy",
        "Bug48075.doc, \u041F\u0440\u0438\u043B\u043E\u0436\u0435\u043D\u0438\u0435 \u21162",
        "innertable.doc, <span>A</span>",
        "o_kurs.doc, \u0412\u0441\u0435 \u0441\u0442\u0440\u0430\u043D\u0438\u0446\u044B \u043D\u0443\u043C\u0435\u0440\u0443\u044E\u0442\u0441\u044F",
        "Bug52583.doc, <select><option selected>riri</option><option>fifi</option><option>loulou</option></select>",
        "Bug53182.doc, !italic",
        "documentProperties.doc, " +
            "<title>This is document title</title>|" +
            "<meta content=\"This is document keywords\" name=\"keywords\">",
        // email hyperlink
        "Bug47286.doc, provisastpet@mfa.gov.cy",
        "endingnote.doc, " +
            "<a class=\"a1 endnoteanchor\" href=\"#endnote_1\" name=\"endnote_back_1\">1</a>|" +
            "<a class=\"a1 endnoteindex\" href=\"#endnote_back_1\" name=\"endnote_1\">1</a><span|" +
            "Ending note text",
        "equation.doc, <!--Image link to '0.emf' can be here-->",
        "hyperlink.doc, " +
            "<span>Before text; </span><a |" +
            "<a href=\"http://testuri.org/\"><span class=\"s1\">Hyperlink text</span></a>|" +
            "</a><span>; after text</span>",
        "lists-margins.doc, " +
            ".s1{display: inline-block; text-indent: 0; min-width: 0.4861111in;}|" +
            ".s2{display: inline-block; text-indent: 0; min-width: 0.23055555in;}|" +
            ".s3{display: inline-block; text-indent: 0; min-width: 0.28541666in;}|" +
            ".s4{display: inline-block; text-indent: 0; min-width: 0.28333333in;}|" +
            ".p4{text-indent:-0.59652776in;margin-left:-0.70069444in;",
        "pageref.doc, " +
            "<a href=\"#userref\">|" +
            "<a name=\"userref\">|" +
            "1",
        "table-merges.doc, " +
            "<td class=\"td1\" colspan=\"3\">|" +
            "<td class=\"td2\" colspan=\"2\">",
        "52420.doc, " +
            "!FORMTEXT|" +
            "\u0417\u0410\u0414\u0410\u041d\u0418\u0415|" +
            "\u041f\u0440\u0435\u043f\u043e\u0434\u0430\u0432\u0430\u0442\u0435\u043b\u044c",
        "picture.doc, " +
            "src=\"0.emf\"|" +
            "width:3.1293333in;height:1.7247736in;|" +
            "left:-0.09433333;top:-0.2573611;|" +
            "width:3.4125in;height:2.3253334in;",
        "pictures_escher.doc, " +
            "<img src=\"s0.PNG\">|" +
            "<img src=\"s808.PNG\">",
        "bug65255.doc, meta content=\"王久君\""
    })
    void testFile(String file, String contains) throws Exception {
        boolean emulatePictureStorage = !file.contains("equation");

        String result = getHtmlText(file, emulatePictureStorage);
        assertNotNull(result);
        // starting with JDK 9 such unimportant whitespaces may be trimmed
        result = result.replace("</a> <span", "</a><span");

        for (String match : contains.split("\\|")) {
            if (match.startsWith("!")) {
                assertNotContained(result, match.substring(1));
            } else {
                assertContains(result, match);
            }
        }
    }

    private static String getHtmlText(final String sampleFileName, boolean emulatePictureStorage) throws Exception {
        Document newDocument = XMLHelper.newDocumentBuilder().newDocument();
        WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(newDocument);

        try (HWPFDocument hwpfDocument = new HWPFDocument(SAMPLES.openResourceAsStream(sampleFileName))) {
            if (emulatePictureStorage) {
                wordToHtmlConverter.setPicturesManager((content, pictureType, suggestedName, widthInches, heightInches) -> suggestedName);
            }

            wordToHtmlConverter.processDocument(hwpfDocument);

            StringWriter stringWriter = new StringWriter();

            Transformer transformer = XMLHelper.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            transformer.transform(
                new DOMSource(wordToHtmlConverter.getDocument()),
                new StreamResult(stringWriter));

            return stringWriter.toString();
        }
    }


}
