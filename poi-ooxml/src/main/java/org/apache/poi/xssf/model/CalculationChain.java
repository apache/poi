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
import static org.apache.poi.xssf.model.SharedStringsTable.getInputStreamReadLimit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.util.LimitInputStream;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCalcCell;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCalcChain;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CalcChainDocument;

/**
 * The cells in a workbook can be calculated in different orders depending on various optimizations and
 * dependencies. The calculation chain object specifies the order in which the cells in a workbook were last calculated.
 */
public class CalculationChain extends POIXMLDocumentPart {

    private static long INPUT_STREAM_READ_LIMIT = -1; // negative means no limit

    /**
     * Sets the read limit for input streams used to read calculation chain data.
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

    private CTCalcChain chain;

    public CalculationChain() {
        super();
        chain = CTCalcChain.Factory.newInstance();
    }

    /**
     * @since POI 3.14-Beta1
     */
    public CalculationChain(PackagePart part) throws IOException {
        super(part);
        if (INPUT_STREAM_READ_LIMIT >= 0 && part.getSize() > INPUT_STREAM_READ_LIMIT) {
            throw new IOException(String.format(
                    Locale.ROOT,
                    "Calculation Chain part size (%s) exceeds the read limit (%s)",
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
            CalcChainDocument doc = CalcChainDocument.Factory.parse(stream, DEFAULT_XML_OPTIONS);
            chain = doc.getCalcChain();
        } catch (XmlException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }
    public void writeTo(OutputStream out) throws IOException {
        CalcChainDocument doc = CalcChainDocument.Factory.newInstance();
        doc.setCalcChain(chain);
        doc.save(out, DEFAULT_XML_OPTIONS);
    }

    @Override
    protected void commit() throws IOException {
        PackagePart part = getPackagePart();
        try (OutputStream out = part.getOutputStream()) {
            writeTo(out);
        }
    }


    public CTCalcChain getCTCalcChain(){
        return chain;
    }

    /**
     * Remove a formula reference from the calculation chain
     *
     * @param sheetId  the sheet Id of a sheet the formula belongs to.
     * @param ref  A1 style reference to the cell containing the formula.
     */
    public void removeItem(int sheetId, String ref){
        //sheet Id of a sheet the cell belongs to
        int id = -1;
        CTCalcCell[] c = chain.getCArray();

        for (int i = 0; i < c.length; i++){
            //If sheet Id  is omitted, it is assumed to be the same as the value of the previous cell.
            if(c[i].isSetI()) id = c[i].getI();

            if(id == sheetId && c[i].getR().equals(ref)){
                if(c[i].isSetI() && i < c.length - 1 && !c[i+1].isSetI()) {
                    c[i+1].setI(id);
                }
                chain.removeC(i);
                break;
            }
        }
    }
}