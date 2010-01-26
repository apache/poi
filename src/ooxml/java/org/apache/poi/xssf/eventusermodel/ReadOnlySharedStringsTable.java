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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.xssf.usermodel.XSSFRelation;
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
 * <pre>
&lt;?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
&lt;sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="2" uniqueCount="2">
 &lt;si>
   &lt;r>
     &lt;rPr>
       &lt;b />
       &lt;sz val="11" />
       &lt;color theme="1" />
       &lt;rFont val="Calibri" />
       &lt;family val="2" />
       &lt;scheme val="minor" />
     &lt;/rPr>
     &lt;t>This:&lt;/t>
   &lt;/r>
   &lt;r>
     &lt;rPr>
       &lt;sz val="11" />
       &lt;color theme="1" />
       &lt;rFont val="Calibri" />
       &lt;family val="2" />
       &lt;scheme val="minor" />
     &lt;/rPr>
     &lt;t xml:space="preserve">Causes Problems&lt;/t>
   &lt;/r>
 &lt;/si>
 &lt;si>
   &lt;t>This does not&lt;/t>
 &lt;/si>
&lt;/sst>
* </pre>
 *
 */
public class ReadOnlySharedStringsTable extends DefaultHandler {
    /**
     * An integer representing the total count of strings in the workbook. This count does not
     * include any numbers, it counts only the total of text strings in the workbook.
     */
    private int count;

    /**
     * An integer representing the total count of unique strings in the Shared String Table.
     * A string is unique even if it is a copy of another string, but has different formatting applied
     * at the character level.
     */
    private int uniqueCount;

    /**
     * The shared strings table.
     */
    private String[] strings;

    /**
     * @param pkg
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public ReadOnlySharedStringsTable(OPCPackage pkg)
            throws IOException, SAXException {
        ArrayList<PackagePart> parts =
                pkg.getPartsByContentType(XSSFRelation.SHARED_STRINGS.getContentType());

        // Some workbooks have no shared strings table.
        if (parts.size() > 0) {
            PackagePart sstPart = parts.get(0);
            readFrom(sstPart.getInputStream());
        }
    }

    /**
     * Like POIXMLDocumentPart constructor
     *
     * @param part
     * @param rel_ignored
     * @throws IOException
     */
    public ReadOnlySharedStringsTable(PackagePart part, PackageRelationship rel_ignored)
            throws IOException, SAXException {
        readFrom(part.getInputStream());
    }

    /**
     * Read this shared strings table from an XML file.
     *
     * @param is The input stream containing the XML document.
     * @throws IOException                  if an error occurs while reading.
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public void readFrom(InputStream is) throws IOException, SAXException {
        InputSource sheetSource = new InputSource(is);
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        try {
           SAXParser saxParser = saxFactory.newSAXParser();
           XMLReader sheetParser = saxParser.getXMLReader();
           sheetParser.setContentHandler(this);
           sheetParser.parse(sheetSource);
        } catch(ParserConfigurationException e) {
           throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
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

    /**
     * Return the string at a given index.
     * Formatting is ignored.
     *
     * @param idx index of item to return.
     * @return the item at the specified position in this Shared String table.
     */
    public String getEntryAt(int idx) {
        return strings[idx];
    }

    //// ContentHandler methods ////

    private StringBuffer characters;
    private boolean tIsOpen;
    private int index;

    public void startElement(String uri, String localName, String name,
                             Attributes attributes) throws SAXException {
        if ("sst".equals(name)) {
            String count = attributes.getValue("count");
            String uniqueCount = attributes.getValue("uniqueCount");
            this.count = Integer.parseInt(count);
            this.uniqueCount = Integer.parseInt(uniqueCount);
            this.strings = new String[this.uniqueCount];
            index = 0;
            characters = new StringBuffer();
        } else if ("si".equals(name)) {
            characters.setLength(0);
        } else if ("t".equals(name)) {
            tIsOpen = true;
        }
    }

    public void endElement(String uri, String localName, String name)
            throws SAXException {
        if ("si".equals(name)) {
            strings[index] = characters.toString();              
            ++index;
        } else if ("t".equals(name)) {
           tIsOpen = false;
        }
    }

    /**
     * Captures characters only if a t(ext) element is open.
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (tIsOpen)
            characters.append(ch, start, length);
    }

}
