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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * @since 3.16-beta3
 */
@Internal
public class XSSFBCommentsTable extends XSSFBParser {

    private Map<CellAddress, XSSFBComment> comments = new TreeMap<>();
    private Queue<CellAddress> commentAddresses = new LinkedList<>();
    private List<String> authors = new ArrayList<>();

    //these are all used only during parsing, and they are mutable!
    private int authorId = -1;
    private CellAddress cellAddress;
    private XSSFBCellRange cellRange;
    private String comment;
    private StringBuilder authorBuffer = new StringBuilder();


    public XSSFBCommentsTable(InputStream is) throws IOException {
        super(is);
        parse();
        commentAddresses.addAll(comments.keySet());
    }

    @Override
    public void handleRecord(int id, byte[] data) throws XSSFBParseException {
        XSSFBRecordType recordType = XSSFBRecordType.lookup(id);
        switch (recordType) {
            case BrtBeginComment:
                int offset = 0;
                authorId = XSSFBUtils.castToInt(LittleEndian.getUInt(data)); offset += LittleEndianConsts.INT_SIZE;
                cellRange = XSSFBCellRange.parse(data, offset, cellRange);
                offset+= XSSFBCellRange.length;
                //for strict parsing; confirm that firstRow==lastRow and firstCol==colLats (2.4.28)
                cellAddress = new CellAddress(cellRange.firstRow, cellRange.firstCol);
                break;
            case BrtCommentText:
                XSSFBRichStr xssfbRichStr = XSSFBRichStr.build(data, 0);
                comment = xssfbRichStr.getString();
                break;
            case BrtEndComment:
                comments.put(cellAddress, new XSSFBComment(cellAddress, authors.get(authorId), comment));
                authorId = -1;
                cellAddress = null;
                break;
            case BrtCommentAuthor:
                authorBuffer.setLength(0);
                XSSFBUtils.readXLWideString(data, 0, authorBuffer);
                authors.add(authorBuffer.toString());
                break;
        }
    }


    public Queue<CellAddress> getAddresses() {
        return commentAddresses;
    }

    public XSSFBComment get(CellAddress cellAddress) {
        if (cellAddress == null) {
            return null;
        }
        return comments.get(cellAddress);
    }
}
