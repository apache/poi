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
package org.apache.poi.xwpf.usermodel;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTAbstractNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNum;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumbering;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.NumberingDocument;

/**
 * @author Philipp Epp
 */
public class XWPFNumbering extends POIXMLDocumentPart {
    protected List<XWPFAbstractNum> abstractNums = new ArrayList<>();
    protected List<XWPFNum> nums = new ArrayList<>();
    boolean isNew;
    private CTNumbering ctNumbering;

    /**
     * create a new styles object with an existing document
     *
     * @since POI 3.14-Beta1
     */
    public XWPFNumbering(PackagePart part) {
        super(part);
        isNew = true;
    }

    /**
     * create a new XWPFNumbering object for use in a new document
     */
    public XWPFNumbering() {
        abstractNums = new ArrayList<>();
        nums = new ArrayList<>();
        isNew = true;
    }

    /**
     * read numbering form an existing package
     */
    @Override
    protected void onDocumentRead() throws IOException {
        NumberingDocument numberingDoc = null;
        InputStream is;
        is = getPackagePart().getInputStream();
        try {
            numberingDoc = NumberingDocument.Factory.parse(is, DEFAULT_XML_OPTIONS);
            ctNumbering = numberingDoc.getNumbering();
            //get any Nums
            for (CTNum ctNum : ctNumbering.getNumArray()) {
                nums.add(new XWPFNum(ctNum, this));
            }
            for (CTAbstractNum ctAbstractNum : ctNumbering.getAbstractNumArray()) {
                abstractNums.add(new XWPFAbstractNum(ctAbstractNum, this));
            }
            isNew = false;
        } catch (XmlException e) {
            throw new POIXMLException();
        } finally {
            is.close();
        }
    }

    /**
     * save and commit numbering
     */
    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTNumbering.type.getName().getNamespaceURI(), "numbering"));
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        ctNumbering.save(out, xmlOptions);
        out.close();
    }


    /**
     * Sets the ctNumbering
     *
     * @param numbering
     */
    public void setNumbering(CTNumbering numbering) {
        ctNumbering = numbering;
    }


    /**
     * Checks whether number with numID exists
     *
     * @param numID
     * @return boolean        true if num exist, false if num not exist
     */
    public boolean numExist(BigInteger numID) {
        for (XWPFNum num : nums) {
            if (num.getCTNum().getNumId().equals(numID))
                return true;
        }
        return false;
    }

    /**
     * add a new number to the numbering document
     *
     * @param num
     */
    public BigInteger addNum(XWPFNum num) {
        ctNumbering.addNewNum();
        int pos = ctNumbering.sizeOfNumArray() - 1;
        ctNumbering.setNumArray(pos, num.getCTNum());
        nums.add(num);
        return num.getCTNum().getNumId();
    }

    /**
     * Add a new num with an abstractNumID
     *
     * @return return NumId of the added num
     */
    public BigInteger addNum(BigInteger abstractNumID) {
        CTNum ctNum = this.ctNumbering.addNewNum();
        ctNum.addNewAbstractNumId();
        ctNum.getAbstractNumId().setVal(abstractNumID);
        ctNum.setNumId(BigInteger.valueOf(nums.size() + 1L));
        XWPFNum num = new XWPFNum(ctNum, this);
        nums.add(num);
        return ctNum.getNumId();
    }

    /**
     * Add a new num with an abstractNumID and a numID
     *
     * @param abstractNumID
     * @param numID
     */
    public void addNum(BigInteger abstractNumID, BigInteger numID) {
        CTNum ctNum = this.ctNumbering.addNewNum();
        ctNum.addNewAbstractNumId();
        ctNum.getAbstractNumId().setVal(abstractNumID);
        ctNum.setNumId(numID);
        XWPFNum num = new XWPFNum(ctNum, this);
        nums.add(num);
    }

    /**
     * get Num by NumID
     *
     * @param numID
     * @return abstractNum with NumId if no Num exists with that NumID
     * null will be returned
     */
    public XWPFNum getNum(BigInteger numID) {
        for (XWPFNum num : nums) {
            if (num.getCTNum().getNumId().equals(numID))
                return num;
        }
        return null;
    }

    /**
     * get AbstractNum by abstractNumID
     *
     * @param abstractNumID
     * @return abstractNum with abstractNumId if no abstractNum exists with that abstractNumID
     * null will be returned
     */
    public XWPFAbstractNum getAbstractNum(BigInteger abstractNumID) {
        for (XWPFAbstractNum abstractNum : abstractNums) {
            if (abstractNum.getAbstractNum().getAbstractNumId().equals(abstractNumID)) {
                return abstractNum;
            }
        }
        return null;
    }

    /**
     * Compare AbstractNum with abstractNums of this numbering document.
     * If the content of abstractNum equals with an abstractNum of the List in numbering
     * the BigInteger Value of it will be returned.
     * If no equal abstractNum is existing null will be returned
     *
     * @param abstractNum
     * @return BigInteger
     */
    public BigInteger getIdOfAbstractNum(XWPFAbstractNum abstractNum) {
        CTAbstractNum copy = (CTAbstractNum) abstractNum.getCTAbstractNum().copy();
        XWPFAbstractNum newAbstractNum = new XWPFAbstractNum(copy, this);
        int i;
        for (i = 0; i < abstractNums.size(); i++) {
            newAbstractNum.getCTAbstractNum().setAbstractNumId(BigInteger.valueOf(i));
            newAbstractNum.setNumbering(this);
            if (newAbstractNum.getCTAbstractNum().valueEquals(abstractNums.get(i).getCTAbstractNum())) {
                return newAbstractNum.getCTAbstractNum().getAbstractNumId();
            }
        }
        return null;
    }


    /**
     * add a new AbstractNum and return its AbstractNumID
     *
     * @param abstractNum
     */
    public BigInteger addAbstractNum(XWPFAbstractNum abstractNum) {
        int pos = abstractNums.size();
        if (abstractNum.getAbstractNum() != null) { // Use the current CTAbstractNum if it exists
            ctNumbering.addNewAbstractNum().set(abstractNum.getAbstractNum());
        } else {
            abstractNum.setCtAbstractNum(ctNumbering.addNewAbstractNum());
            abstractNum.getAbstractNum().setAbstractNumId(BigInteger.valueOf(pos));
            ctNumbering.setAbstractNumArray(pos, abstractNum.getAbstractNum());
        }
        abstractNums.add(abstractNum);
        return abstractNum.getCTAbstractNum().getAbstractNumId();
    }

    /**
     * remove an existing abstractNum
     *
     * @param abstractNumID
     * @return true if abstractNum with abstractNumID exists in NumberingArray,
     * false if abstractNum with abstractNumID not exists
     */
    public boolean removeAbstractNum(BigInteger abstractNumID) {
        for (XWPFAbstractNum abstractNum : abstractNums) {
            BigInteger foundNumId = abstractNum.getAbstractNum().getAbstractNumId();
            if(abstractNumID.equals(foundNumId)) {
                ctNumbering.removeAbstractNum(foundNumId.byteValue());
                abstractNums.remove(abstractNum);
                return true;
            }
        }

        return false;
    }

    /**
     * return the abstractNumID
     * If the AbstractNumID not exists
     * return null
     *
     * @param numID
     * @return abstractNumID
     */
    public BigInteger getAbstractNumID(BigInteger numID) {
        XWPFNum num = getNum(numID);
        if (num == null)
            return null;
        if (num.getCTNum() == null)
            return null;
        if (num.getCTNum().getAbstractNumId() == null)
            return null;
        return num.getCTNum().getAbstractNumId().getVal();
    }

    /**
     * @return all abstractNums
     */
    public List<XWPFAbstractNum> getAbstractNums() {
        return Collections.unmodifiableList(abstractNums);
    }

    /**
     * @return all nums
     */
    public List<XWPFNum> getNums() {
        return Collections.unmodifiableList(nums);
    }

}

