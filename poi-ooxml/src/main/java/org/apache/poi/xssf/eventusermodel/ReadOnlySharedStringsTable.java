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
package org.apache.poi.xssf.eventusermodel;

import static org.apache.poi.xssf.usermodel.XSSFRelation.NS_SPREADSHEETML;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <p>This is a lightweight way to process the Shared Strings
 *  table. Most of the text cells will reference something
 *  from in here.
 * <p>Note that each SI entry can have multiple T elements, if the
 *  string is made up of bits with different formatting.
 * <p>Example input:
 * <pre>{@code
 * <?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
 *     <sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="2" uniqueCount="2">
 *         <si>
 *             <r>
 *                 <rPr>
 *                     <b />
 *                     <sz val="11" />
 *                     <color theme="1" />
 *                     <rFont val="Calibri" />
 *                     <family val="2" />
 *                     <scheme val="minor" />
 *                 </rPr>
 *                 <t>This:</t>
 *             </r>
 *             <r>
 *                 <rPr>
 *                     <sz val="11" />
 *                     <color theme="1" />
 *                     <rFont val="Calibri" />
 *                     <family val="2" />
 *                     <scheme val="minor" />
 *                 </rPr>
 *                 <t xml:space="preserve">Causes Problems</t>
 *             </r>
 *         </si>
 *         <si>
 *             <t>This does not</t>
 *         </si>
 *     </sst>
 *  }</pre>
 *
 */
public class ReadOnlySharedStringsTable extends DefaultHandler implements SharedStrings {

    protected final boolean includePhoneticRuns;

    /**
     * An integer representing the total count of strings in the workbook. This count does not
     * include any numbers, it counts only the total of text strings in the workbook.
     */
    protected int count;

    /**
     * An integer representing the total count of unique strings in the Shared String Table.
     * A string is unique even if it is a copy of another string, but has different formatting applied
     * at the character level.
     */
    protected int uniqueCount;

    /**
     * The shared strings table.
     */
    private List<String> strings;

    /**
     * Calls {{@link #ReadOnlySharedStringsTable(OPCPackage, boolean)}} with
     * a value of <code>true</code> for including phonetic runs
     *
     * @param pkg The {@link OPCPackage} to use as basis for the shared-strings table.
     * @throws IOException If reading the data from the package fails.
     * @throws SAXException if parsing the XML data fails.
     */
    public ReadOnlySharedStringsTable(OPCPackage pkg)
            throws IOException, SAXException {
        this(pkg, true);
    }

    /**
     *
     * @param pkg The {@link OPCPackage} to use as basis for the shared-strings table.
     * @param includePhoneticRuns whether or not to concatenate phoneticRuns onto the shared string
     * @since POI 3.14-Beta3
     * @throws IOException If reading the data from the package fails.
     * @throws SAXException if parsing the XML data fails.
     */
    public ReadOnlySharedStringsTable(OPCPackage pkg, boolean includePhoneticRuns)
            throws IOException, SAXException {
        this.includePhoneticRuns = includePhoneticRuns;
        ArrayList<PackagePart> parts =
                pkg.getPartsByContentType(XSSFRelation.SHARED_STRINGS.getContentType());

        // Some workbooks have no shared strings table.
        if (!parts.isEmpty()) {
            PackagePart sstPart = parts.get(0);
            try (InputStream stream = sstPart.getInputStream()) {
                readFrom(stream);
            }
        }
    }

    /**
     * Like POIXMLDocumentPart constructor
     *
     * Calls {@link #ReadOnlySharedStringsTable(PackagePart, boolean)}, with a
     * value of <code>true</code> to include phonetic runs.
     *
     * @since POI 3.14-Beta1
     */
    public ReadOnlySharedStringsTable(PackagePart part) throws IOException, SAXException {
        this(part, true);
    }

    /**
     * @since POI 3.14-Beta3
     */
    public ReadOnlySharedStringsTable(PackagePart part, boolean includePhoneticRuns)
        throws IOException, SAXException {
        this.includePhoneticRuns = includePhoneticRuns;
        try (InputStream stream = part.getInputStream()) {
            readFrom(stream);
        }
    }

    /**
     * @since POI 5.2.0
     */
    public ReadOnlySharedStringsTable(InputStream stream)
            throws IOException, SAXException {
        this(stream, true);
    }

    /**
     * @since POI 5.2.0
     */
    public ReadOnlySharedStringsTable(InputStream stream, boolean includePhoneticRuns)
            throws IOException, SAXException {
        this.includePhoneticRuns = includePhoneticRuns;
        readFrom(stream);
    }

    /**
     * Read this shared strings table from an XML file.
     *
     * @param is The input stream containing the XML document.
     * @throws IOException if an error occurs while reading.
     * @throws SAXException if parsing the XML data fails.
     */
    public void readFrom(InputStream is) throws IOException, SAXException {
        // test if the file is empty, otherwise parse it
        PushbackInputStream pis = new PushbackInputStream(is, 1);
        int emptyTest = pis.read();
        if (emptyTest > -1) {
            pis.unread(emptyTest);
            InputSource sheetSource = new InputSource(pis);
            try {
                XMLReader sheetParser = XMLHelper.newXMLReader();
                sheetParser.setContentHandler(this);
                sheetParser.parse(sheetSource);
            } catch(ParserConfigurationException e) {
                throw new SAXException("SAX parser appears to be broken - " + e.getMessage());
            }
        }
    }

    /**
     * Return an integer representing the total count of strings in the workbook. This count does not
     * include any numbers, it counts only the total of text strings in the workbook.
     *
     * @return the total count of strings in the workbook
     */
    public int getCount() {
        return this.count;
    }

    /**
     * Returns an integer representing the total count of unique strings in the Shared String Table.
     * A string is unique even if it is a copy of another string, but has different formatting applied
     * at the character level.
     *
     * @return the total count of unique strings in the workbook
     */
    public int getUniqueCount() {
        return this.uniqueCount;
    }

    @Override
    public RichTextString getItemAt(int idx) {
        if (strings == null || idx >= strings.size()) {
            throw new IllegalStateException("Cannot get item at " + idx + " with strings: " + strings);
        }
        return new XSSFRichTextString(strings.get(idx));
    }

    //// ContentHandler methods ////

    private StringBuilder characters;
    private boolean tIsOpen;
    private boolean inRPh;

    public void startElement(String uri, String localName, String name,
                             Attributes attributes) throws SAXException {
        if (uri != null && ! uri.equals(NS_SPREADSHEETML)) {
            return;
        }

        if ("sst".equals(localName)) {
            String count = attributes.getValue("count");
            if(count != null) this.count = (int) Long.parseLong(count);
            String uniqueCount = attributes.getValue("uniqueCount");
            if(uniqueCount != null) this.uniqueCount = (int) Long.parseLong(uniqueCount);

            this.strings = new ArrayList<>(this.uniqueCount);
            characters = new StringBuilder(64);
        } else if ("si".equals(localName)) {
            if (characters != null) {
                characters.setLength(0);
            }
        } else if ("t".equals(localName)) {
            tIsOpen = true;
        } else if ("rPh".equals(localName)) {
            inRPh = true;
            //append space...this assumes that rPh always comes after regular <t>
            if (includePhoneticRuns && characters != null && characters.length() > 0) {
                characters.append(" ");
            }
        }
    }

    public void endElement(String uri, String localName, String name) throws SAXException {
        if (uri != null && ! uri.equals(NS_SPREADSHEETML)) {
            return;
        }

        if ("si".equals(localName)) {
            if (strings != null && characters != null) {
                strings.add(characters.toString());
            }
        } else if ("t".equals(localName)) {
            tIsOpen = false;
        } else if ("rPh".equals(localName)) {
            inRPh = false;
        }
    }

    /**
     * Captures characters only if a t(ext) element is open.
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (tIsOpen) {
            if (inRPh && includePhoneticRuns) {
                if (characters != null) {
                    characters.append(ch, start, length);
                }
            } else if (! inRPh){
                if (characters != null) {
                    characters.append(ch, start, length);
                }
            }
        }
    }
}
