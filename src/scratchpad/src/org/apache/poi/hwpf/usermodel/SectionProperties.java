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

package org.apache.poi.hwpf.usermodel;

import org.apache.poi.hwpf.model.types.SEPAbstractType;

public final class SectionProperties extends SEPAbstractType implements Cloneable
{
    private short field_60_rncftn;
    private short field_61_rncedn;
    private int field_62_nftn;
    @SuppressWarnings("RedundantFieldInitialization")
    private int field_63_nfcftnref = 0x00; // initialize with default value; msonfcArabic
    private int field_64_nedn;
    private int field_65_nfcednref = 0x02; // initialize with default value; msonfcLCRoman

    public SectionProperties()
    {
        field_20_brcTop = new BorderCode();
        field_21_brcLeft = new BorderCode();
        field_22_brcBottom = new BorderCode();
        field_23_brcRight = new BorderCode();
        field_26_dttmPropRMark = new DateAndTime();
    }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        SectionProperties copy = (SectionProperties) super.clone();
        copy.field_20_brcTop = (BorderCode) field_20_brcTop.clone();
        copy.field_21_brcLeft = (BorderCode) field_21_brcLeft.clone();
        copy.field_22_brcBottom = (BorderCode) field_22_brcBottom.clone();
        copy.field_23_brcRight = (BorderCode) field_23_brcRight.clone();
        copy.field_26_dttmPropRMark = (DateAndTime) field_26_dttmPropRMark
                .clone();

        return copy;
    }
    
    /**
     * sprmSRncFtn, [MS-DOC], 20140721, 2.6.4
     * 
     * @param field_60_rncftn unsigned 8-bit integer specifying the footnote numbering restart condition
     */
    public void setRncFtn(final short field_60_rncftn) {
        this.field_60_rncftn = field_60_rncftn;
    }
    
    /**
     * @see #setRncFtn(short)
     * @return an Rnc value specifying when and where footnote numbering restarts
     */
    public short getRncFtn() {
        return this.field_60_rncftn;
    }
    
    /**
     * sprmSRncEdn, [MS-DOC], 20140721, 2.6.4
     * 
     * @param field_61_rncedn unsigned 8-bit integer specifying the endnote numbering restart condition
     */
    public void setRncEdn(final short field_61_rncedn) {
        this.field_61_rncedn = field_61_rncedn;
    }
    
    /**
     * @see #setRncEdn(short)
     * @return an Rnc value specifying when and where endnote numbering restarts
     */
    public short getRncEdn() {
        return this.field_61_rncedn;
    }
    
    /**
     * sprmSNftn, [MS-DOC], v20140721, 2.6.4
     * 
     * @param field_62_nftn a number specifying the offset to add to footnote numbers
     */
    public void setNFtn(final int field_62_nftn) {
        this.field_62_nftn = field_62_nftn;
    }
    
    /**
     * @see #setNFtn(int)
     * @return a 16-bit integer specifying the offset to add to footnote numbering
     */
    public int getNFtn() {
        return this.field_62_nftn;
    }
    
    /**
     * sprmSNfcFtnRef, [MS-DOC], v20140721
     * 
     * @param field_63_nfcftnref an Nfc specifying the numbering format for footnotes
     */
    public void setNfcFtnRef(final int field_63_nfcftnref) {
        this.field_63_nfcftnref = field_63_nfcftnref;
    }
    
    /**
     * 
     * @see #setNfcFtnRef(int)
     * @return a 16-bit integer with an Nfc specifying the numbering format for footnotes
     */
    public int getNfcFtnRef() {
        return this.field_63_nfcftnref;
    }
    
    /**
     * sprmSNEdn, [MS-DOC], v20140721, 2.6.4
     * 
     * @param field_64_nedn a number specifying the offset to add to footnote numbers
     */
    public void setNEdn(final int field_64_nedn) {
        this.field_64_nedn = field_64_nedn;
    }
    
    /**
     * @see #setNEdn(int)
     * @return a 16-bit integer specifying the offset to add to endnote numbering
     */
    public int getNEdn() {
        return this.field_64_nedn;
    }
    
    /**
     * sprmSNfcEdnRef, [MS-DOC], v20140721
     * 
     * @param field_65_nfcednref an Nfc specifying the numbering format for endnotes
     */
    public void setNfcEdnRef(final int field_65_nfcednref) {
        this.field_65_nfcednref = field_65_nfcednref;
    }
    
    /**
     * 
     * @see #setNfcEdnRef(int)
     * @return a 16-bit integer with an Nfc specifying the numbering format for endnotes
     */
    public int getNfcEdnRef() {
        return this.field_65_nfcednref;
    }
}
