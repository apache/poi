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

package org.apache.poi.xssf.model;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.LimitInputStream;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.helpers.XSSFSingleXmlCell;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSingleXmlCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSingleXmlCells;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.SingleXmlCellsDocument;


/**
 *
 * This class implements the Single Cell Tables Part (Open Office XML Part 4:
 * chapter 3.5.2)
 */
public class SingleXmlCells extends POIXMLDocumentPart {

    private static long INPUT_STREAM_READ_LIMIT = -1; // negative means no limit

    /**
     * Sets the read limit for input streams used to read Single Cell Tables.
     * Negative values mean no limit. The default is -1 (no limit).
     * @param limit
     * @since POI 5.4.2
     */
    public static void setInputStreamReadLimit(long limit) {
        INPUT_STREAM_READ_LIMIT = limit;
    }

    /**
     * Gets the read limit for input streams used to read styles.
     * Negative values mean no limit. The default is -1 (no limit).
     * @return the read limit
     * @since POI 5.4.2
     */
    public static long getInputStreamReadLimit() {
        return INPUT_STREAM_READ_LIMIT;
    }

    private CTSingleXmlCells singleXMLCells;

    public SingleXmlCells() {
        super();
        singleXMLCells = CTSingleXmlCells.Factory.newInstance();
    }

    /**
     * @since POI 3.14-Beta1
     */
    public SingleXmlCells(PackagePart part) throws IOException {
        super(part);
        if (INPUT_STREAM_READ_LIMIT >= 0 && part.getSize() > INPUT_STREAM_READ_LIMIT) {
            throw new IOException(String.format(
                    Locale.ROOT,
                    "Single Cell Tables part size (%s) exceeds the read limit (%s)",
                    part.getSize(),
                    INPUT_STREAM_READ_LIMIT));
        }
        try (InputStream stream = part.getInputStream()) {
            readFrom(stream);
        }
    }

    public void readFrom(final InputStream is) throws IOException {
        final InputStream stream = INPUT_STREAM_READ_LIMIT >= 0
                ? new LimitInputStream(is, INPUT_STREAM_READ_LIMIT)
                : is;
        try {
            SingleXmlCellsDocument doc = SingleXmlCellsDocument.Factory.parse(stream, DEFAULT_XML_OPTIONS);
            singleXMLCells = doc.getSingleXmlCells();
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }

    public XSSFSheet getXSSFSheet(){
        return (XSSFSheet) getParent();
    }

    protected void writeTo(OutputStream out) throws IOException {
        SingleXmlCellsDocument doc = SingleXmlCellsDocument.Factory.newInstance();
        doc.setSingleXmlCells(singleXMLCells);
        doc.save(out, DEFAULT_XML_OPTIONS);
    }

    @Override
    protected void commit() throws IOException {
        PackagePart part = getPackagePart();
        try (OutputStream out = part.getOutputStream()) {
            writeTo(out);
        }
    }

    public CTSingleXmlCells getCTSingleXMLCells(){
        return singleXMLCells;
    }

    /**
     *
     * @return all the SimpleXmlCell contained in this SingleXmlCells element
     */
    public List<XSSFSingleXmlCell> getAllSimpleXmlCell(){
        List<XSSFSingleXmlCell> list = new Vector<>();

        for(CTSingleXmlCell singleXmlCell: singleXMLCells.getSingleXmlCellArray()){
            list.add(new XSSFSingleXmlCell(singleXmlCell,this));
        }
        return list;
    }
}
