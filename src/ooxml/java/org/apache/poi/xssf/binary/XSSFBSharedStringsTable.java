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
package org.apache.poi.xssf.binary;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.SAXException;

/**
 * @since 3.16-beta3
 */
@Internal
public class XSSFBSharedStringsTable implements SharedStrings {

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
    private List<String> strings = new ArrayList<>();

    /**
     * @param pkg The {@link OPCPackage} to use as basis for the shared-strings table.
     * @throws IOException If reading the data from the package fails.
     * @throws SAXException if parsing the XML data fails.
     */
    public XSSFBSharedStringsTable(OPCPackage pkg)
            throws IOException, SAXException {
        ArrayList<PackagePart> parts =
                pkg.getPartsByContentType(XSSFBRelation.SHARED_STRINGS_BINARY.getContentType());

        // Some workbooks have no shared strings table.
        if (parts.size() > 0) {
            PackagePart sstPart = parts.get(0);

            readFrom(sstPart.getInputStream());
        }
    }

    /**
     * Like POIXMLDocumentPart constructor
     */
    XSSFBSharedStringsTable(PackagePart part) throws IOException, SAXException {
        readFrom(part.getInputStream());
    }

    private void readFrom(InputStream inputStream) throws IOException {
        SSTBinaryReader reader = new SSTBinaryReader(inputStream);
        reader.parse();
    }

    @Override
    public RichTextString getItemAt(int idx) {
        return new XSSFRichTextString(strings.get(idx));
    }

    /**
     * Return an integer representing the total count of strings in the workbook. This count does not
     * include any numbers, it counts only the total of text strings in the workbook.
     *
     * @return the total count of strings in the workbook
     */
    @Override
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
    @Override
    public int getUniqueCount() {
        return this.uniqueCount;
    }

    private class SSTBinaryReader extends XSSFBParser {

        SSTBinaryReader(InputStream is) {
            super(is);
        }

        @Override
        public void handleRecord(int recordType, byte[] data) throws XSSFBParseException {
            XSSFBRecordType type = XSSFBRecordType.lookup(recordType);

            switch (type) {
                case BrtSstItem:
                    XSSFBRichStr rstr = XSSFBRichStr.build(data, 0);
                    strings.add(rstr.getString());
                    break;
                case BrtBeginSst:
                    count = XSSFBUtils.castToInt(LittleEndian.getUInt(data,0));
                    uniqueCount = XSSFBUtils.castToInt(LittleEndian.getUInt(data, 4));
                    break;
            }

        }
    }

}
