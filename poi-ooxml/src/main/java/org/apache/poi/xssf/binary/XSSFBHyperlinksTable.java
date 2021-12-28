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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.zaxxer.sparsebits.SparseBitSet;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeUtil;
import org.apache.poi.util.Internal;
import org.apache.poi.xssf.usermodel.XSSFRelation;

/**
 * @since 3.16-beta3
 */
@Internal
public class XSSFBHyperlinksTable {

    private static final SparseBitSet RECORDS = new SparseBitSet();


    static {
        RECORDS.set(XSSFBRecordType.BrtHLink.getId());
    }


    private final List<XSSFHyperlinkRecord> hyperlinkRecords = new ArrayList<>();

    //cache the relId to hyperlink url from the sheet's .rels
    private Map<String, String> relIdToHyperlink = new HashMap<>();

    public XSSFBHyperlinksTable(PackagePart sheetPart) throws IOException {
        //load the urls from the sheet .rels
        loadUrlsFromSheetRels(sheetPart);
        //now load the hyperlinks from the bottom of the sheet
        try (InputStream stream = sheetPart.getInputStream()) {
            HyperlinkSheetScraper scraper = new HyperlinkSheetScraper(stream);
            scraper.parse();
        }
    }

    /**
     *
     * @return a map of the hyperlinks. The key is the top left cell address in their CellRange
     */
    public Map<CellAddress, List<XSSFHyperlinkRecord>> getHyperLinks() {
        Map<CellAddress, List<XSSFHyperlinkRecord>> hyperlinkMap =
                new TreeMap<>(new TopLeftCellAddressComparator());
        for (XSSFHyperlinkRecord hyperlinkRecord : hyperlinkRecords) {
            CellAddress cellAddress = new CellAddress(hyperlinkRecord.getCellRangeAddress().getFirstRow(),
                    hyperlinkRecord.getCellRangeAddress().getFirstColumn());
            List<XSSFHyperlinkRecord> list = hyperlinkMap.get(cellAddress);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(hyperlinkRecord);
            hyperlinkMap.put(cellAddress, list);
        }
        return hyperlinkMap;
    }


    /**
     *
     * @param cellAddress cell address to find
     * @return null if not a hyperlink
     */
    public List<XSSFHyperlinkRecord> findHyperlinkRecord(CellAddress cellAddress) {
        List<XSSFHyperlinkRecord> overlapping = null;
        CellRangeAddress targetCellRangeAddress = new CellRangeAddress(cellAddress.getRow(),
                cellAddress.getRow(),
                cellAddress.getColumn(),
                cellAddress.getColumn());
        for (XSSFHyperlinkRecord record : hyperlinkRecords) {
            if (CellRangeUtil.intersect(targetCellRangeAddress, record.getCellRangeAddress()) != CellRangeUtil.NO_INTERSECTION) {
                if (overlapping == null) {
                    overlapping = new ArrayList<>();
                }
                overlapping.add(record);
            }
        }
        return overlapping;
    }

    private void loadUrlsFromSheetRels(PackagePart sheetPart) {
        try {
            for (PackageRelationship rel : sheetPart.getRelationshipsByType(XSSFRelation.SHEET_HYPERLINKS.getRelation())) {
                relIdToHyperlink.put(rel.getId(), rel.getTargetURI().toString());
            }
        } catch (InvalidFormatException e) {
            //swallow
        }
    }

    private class HyperlinkSheetScraper extends XSSFBParser {

        private XSSFBCellRange hyperlinkCellRange = new XSSFBCellRange();
        private final StringBuilder xlWideStringBuffer = new StringBuilder();

        HyperlinkSheetScraper(InputStream is) {
            super(is, RECORDS);
        }

        @Override
        public void handleRecord(int recordType, byte[] data) throws XSSFBParseException {
            if (recordType != XSSFBRecordType.BrtHLink.getId()) {
                return;
            }
            int offset = 0;

            hyperlinkCellRange = XSSFBCellRange.parse(data, offset, hyperlinkCellRange);
            offset += XSSFBCellRange.length;
            xlWideStringBuffer.setLength(0);
            offset += XSSFBUtils.readXLNullableWideString(data, offset, xlWideStringBuffer);
            String relId = xlWideStringBuffer.toString();
            xlWideStringBuffer.setLength(0);
            offset += XSSFBUtils.readXLWideString(data, offset, xlWideStringBuffer);
            String location = xlWideStringBuffer.toString();
            xlWideStringBuffer.setLength(0);
            offset += XSSFBUtils.readXLWideString(data, offset, xlWideStringBuffer);
            String toolTip = xlWideStringBuffer.toString();
            xlWideStringBuffer.setLength(0);
            /*offset +=*/ XSSFBUtils.readXLWideString(data, offset, xlWideStringBuffer);
            String display = xlWideStringBuffer.toString();
            CellRangeAddress cellRangeAddress = new CellRangeAddress(hyperlinkCellRange.firstRow, hyperlinkCellRange.lastRow, hyperlinkCellRange.firstCol, hyperlinkCellRange.lastCol);

            String url = relIdToHyperlink.get(relId);
            if (location.length() == 0) {
                location = url;
            }

            hyperlinkRecords.add(
                    new XSSFHyperlinkRecord(cellRangeAddress, relId, location, toolTip, display)
            );
        }
    }

    private static class TopLeftCellAddressComparator implements Comparator<CellAddress>, Serializable {
        private static final long serialVersionUID = 1L;
        
        @Override
        public int compare(CellAddress o1, CellAddress o2) {
            if (o1.getRow() < o2.getRow()) {
                return -1;
            } else if (o1.getRow() > o2.getRow()) {
                return 1;
            }
            if (o1.getColumn() < o2.getColumn()) {
                return -1;
            } else if (o1.getColumn() > o2.getColumn()) {
                return 1;
            }
            return 0;
        }
    }

}
