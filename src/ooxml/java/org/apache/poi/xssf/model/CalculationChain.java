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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;

/**
 * The cells in a workbook can be calculated in different orders depending on various optimizations and
 * dependencies. The calculation chain object specifies the order in which the cells in a workbook were last calculated.
 *
 * @author Yegor Kozlov
 */
public class CalculationChain extends POIXMLDocumentPart {
    private CTCalcChain chain;

    public CalculationChain() {
        super();
        chain = CTCalcChain.Factory.newInstance();
    }

    public CalculationChain(PackagePart part, PackageRelationship rel) throws IOException {
        super(part, rel);
        readFrom(part.getInputStream());
    }

    public void readFrom(InputStream is) throws IOException {
        try {
            CalcChainDocument doc = CalcChainDocument.Factory.parse(is);
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
        OutputStream out = part.getOutputStream();
        writeTo(out);
        out.close();
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