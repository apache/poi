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
import static org.apache.poi.POITestCase.assertNotContains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.regex.Matcher;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.POIDataSamples;
import org.apache.poi.POITestCase;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.XMLHelper;
import org.junit.jupiter.api.Test;
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
                POITestCase.assertNotContains(result, match.substring(1));
            } else {
                assertContains(result, match);
            }
        }
    }

    @Test
    void testFieldPatterns() {
        Matcher m = AbstractWordConverter.PATTERN_HYPERLINK_EXTERNAL.matcher(" HYPERLINK \"http://testuri.org/\" ");
        assertTrue(m.matches());
        assertEquals("http://testuri.org/", m.group(1));

        m = AbstractWordConverter.PATTERN_HYPERLINK_LOCAL.matcher("HYPERLINK \\l \"_Toc1\" text\"");
        assertTrue(m.matches());
        assertEquals("_Toc1", m.group(1));
        assertEquals("text\"", m.group(2));

        m = AbstractWordConverter.PATTERN_PAGEREF.matcher("\r\n PAGEREF _Toc2 \t\\h \\* MERGEFORMAT \r");
        assertTrue(m.find());
        assertEquals("_Toc2", m.group(1));

        m = AbstractWordConverter.PATTERN_PAGEREF.matcher("PAGEREF _Toc2\t\\h");
        assertTrue(m.find());
        assertEquals("_Toc2\t", m.group(1));
    }

    @Test
    void testFieldPatternsBacktracking() {
        // these used to take minutes, as every shorter match was retried before failing
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            assertFalse(AbstractWordConverter.PATTERN_HYPERLINK_EXTERNAL.matcher(
                "HYPERLINK \"" + "\"".repeat(100_000) + "\"\nx").matches());
            assertFalse(AbstractWordConverter.PATTERN_HYPERLINK_LOCAL.matcher(
                "HYPERLINK \\l \"" + "\" ".repeat(50_000) + "\nx").matches());
            assertFalse(AbstractWordConverter.PATTERN_PAGEREF.matcher(
                "PAGEREF " + "\t".repeat(100_000) + "x").find());
            assertFalse(AbstractWordConverter.PATTERN_PAGEREF.matcher(
                "PAGEREF " + "\\h".repeat(50_000) + "\n\n").find());
        });
    }

    @Test
    void testFontNameInStylesheet() throws Exception {
        // the font name is written into the text of the <style> element, which the html output
        // method does not escape, so a crafted name used to close the element and add markup
        String fontName = "</style><p>x";
        try (POIFSFileSystem fs = new POIFSFileSystem(SAMPLES.openResourceAsStream("SampleDoc.doc"))) {
            renameFont(fs, "Times New Roman", fontName);

            try (HWPFDocument hwpfDocument = new HWPFDocument(fs)) {
                String result = getHtmlText(hwpfDocument, true);
                assertNotContains(result, "</style><p>");
                assertContains(result, "font-family:\"\\3c /style\\3e \\3c p\\3e x\";");
            }
        }
    }

    @Test
    void testScriptHyperlink() throws Exception {
        // the target of a HYPERLINK field is copied into the href of the link, so a
        // javascript: target ran script in the page when the link was followed
        String result = getScriptHyperlinkHtml(null);
        assertNotContains(result, "javascript:");
        assertContains(result, "<span class=\"s1\">Hyperlink text</span>");
    }

    @Test
    void testCustomHyperlinkValidator() throws Exception {
        // a validator only applies to standard URLs, it can't let a javascript: target through
        String result = getScriptHyperlinkHtml(h -> true);
        assertNotContains(result, "javascript:");
        assertContains(result, "<span class=\"s1\">Hyperlink text</span>");

        try (HWPFDocument hwpfDocument = new HWPFDocument(SAMPLES.openResourceAsStream("hyperlink.doc"))) {
            result = getHtmlText(hwpfDocument, true, h -> h.startsWith("http://testuri.org/"));
            assertContains(result, "<a href=\"http://testuri.org/\">");
        }

        try (HWPFDocument hwpfDocument = new HWPFDocument(SAMPLES.openResourceAsStream("hyperlink.doc"))) {
            result = getHtmlText(hwpfDocument, true, h -> false);
            assertNotContains(result, "<a href");
            assertContains(result, "<span class=\"s1\">Hyperlink text</span>");
        }
    }

    @Test
    void testIsSafeHyperlink() {
        List<String> safe = Arrays.asList(
            "http://testuri.org/",
            "HTTPS://testuri.org/",
            "mailto:someone@testuri.org",
            "file:///C:/docs/a.doc",
            "C:\\docs\\a.doc",
            "\\\\server\\share\\a.doc",
            "docs/a.doc",
            "#_Toc1",
            "a.doc#x:y",
            "");
        List<String> unsafe = Arrays.asList(
            "javascript:alert(1)",
            "JavaScript:alert(1)",
            " \u0001javascript:alert(1)",
            "java\tscr\nipt:alert(1)",
            "vbscript:msgbox(1)",
            "data:text/html,<script>alert(1)</script>");

        assertThat("safe targets that are rejected",
            safe.stream().filter(h -> !WordToHtmlUtils.isSafeHyperlink(h)).collect(Collectors.toList()), empty());
        assertThat("unsafe targets that are accepted",
            unsafe.stream().filter(WordToHtmlUtils::isSafeHyperlink).collect(Collectors.toList()), empty());
    }

    @Test
    void testCssFontFamily() {
        assertEquals("Times New Roman", WordToHtmlUtils.cssFontFamily("Times New Roman"));
        assertEquals("MS-Mincho_2", WordToHtmlUtils.cssFontFamily("MS-Mincho_2"));
        assertEquals("\"3M Sans\"", WordToHtmlUtils.cssFontFamily("3M Sans"));
        assertEquals("\"Font -1\"", WordToHtmlUtils.cssFontFamily("Font -1"));
        assertEquals("\"a\\22 b\\5c c\\a d\"", WordToHtmlUtils.cssFontFamily("a\"b\\c\nd"));
    }

    /**
     * Overwrites the name of a font in the font table of the table stream, in place: the new
     * name must not be longer than the old one, the rest of the entry is padded with zeros.
     */
    private static void renameFont(POIFSFileSystem fs, String oldName, String newName) throws IOException {
        // not closed, as that would close the file system too
        HWPFDocument hwpfDocument = new HWPFDocument(fs);
        byte[] table = hwpfDocument.getTableStream();
        String tableName = hwpfDocument.getFileInformationBlock().getFibBase().isFWhichTblStm() ? "1Table" : "0Table";
        int start = hwpfDocument.getFileInformationBlock().getFcSttbfffn();
        int end = start + hwpfDocument.getFileInformationBlock().getLcbSttbfffn();

        byte[] oldBytes = (oldName + "\0").getBytes(StandardCharsets.UTF_16LE);
        byte[] newBytes = Arrays.copyOf(newName.getBytes(StandardCharsets.UTF_16LE), oldBytes.length);
        for (int i = start; i + oldBytes.length <= end; i++) {
            if (Arrays.equals(oldBytes, Arrays.copyOfRange(table, i, i + oldBytes.length))) {
                System.arraycopy(newBytes, 0, table, i, newBytes.length);
                fs.createOrUpdateDocument(new ByteArrayInputStream(table), tableName);
                return;
            }
        }
        throw new IllegalStateException("font " + oldName + " not found");
    }

    /**
     * Converts hyperlink.doc with the target of its HYPERLINK field changed to a javascript: one.
     *
     * @param hyperlinkValidator the validator to set on the converter, or null for the default
     */
    private static String getScriptHyperlinkHtml(Predicate<String> hyperlinkValidator) throws Exception {
        try (POIFSFileSystem fs = new POIFSFileSystem(SAMPLES.openResourceAsStream("hyperlink.doc"))) {
            replaceText(fs, "http://testuri.org/", "javascript:alert(1)");

            try (HWPFDocument hwpfDocument = new HWPFDocument(fs)) {
                return getHtmlText(hwpfDocument, true, hyperlinkValidator);
            }
        }
    }

    /**
     * Overwrites every occurrence of a text in the WordDocument stream, in place: the new
     * text must have the same length as the old one.
     */
    private static void replaceText(POIFSFileSystem fs, String oldText, String newText) throws IOException {
        byte[] oldBytes = oldText.getBytes(StandardCharsets.UTF_16LE);
        byte[] newBytes = newText.getBytes(StandardCharsets.UTF_16LE);
        assertEquals(oldBytes.length, newBytes.length);

        byte[] stream;
        try (InputStream is = fs.createDocumentInputStream("WordDocument")) {
            stream = IOUtils.toByteArray(is);
        }
        boolean found = false;
        for (int i = 0; i + oldBytes.length <= stream.length; i++) {
            if (Arrays.equals(oldBytes, Arrays.copyOfRange(stream, i, i + oldBytes.length))) {
                System.arraycopy(newBytes, 0, stream, i, newBytes.length);
                found = true;
            }
        }
        assertTrue(found, "text " + oldText + " not found");
        fs.createOrUpdateDocument(new ByteArrayInputStream(stream), "WordDocument");
    }

    private static String getHtmlText(final String sampleFileName, boolean emulatePictureStorage) throws Exception {
        try (HWPFDocument hwpfDocument = new HWPFDocument(SAMPLES.openResourceAsStream(sampleFileName))) {
            return getHtmlText(hwpfDocument, emulatePictureStorage);
        }
    }

    private static String getHtmlText(HWPFDocument hwpfDocument, boolean emulatePictureStorage) throws Exception {
        return getHtmlText(hwpfDocument, emulatePictureStorage, null);
    }

    private static String getHtmlText(HWPFDocument hwpfDocument, boolean emulatePictureStorage,
                                      Predicate<String> hyperlinkValidator) throws Exception {
        Document newDocument = XMLHelper.newDocumentBuilder().newDocument();
        WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(newDocument);

        if (hyperlinkValidator != null) {
            wordToHtmlConverter.setHyperlinkValidator(hyperlinkValidator);
        }

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
