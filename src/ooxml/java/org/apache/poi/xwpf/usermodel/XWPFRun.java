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

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STOnOff;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

/**
 * XWPFRun object defines a region of text with a common set of properties
 *
 * @author Yegor Kozlov
 */
public class XWPFRun {
    private CTR run;
    private XWPFParagraph paragraph;

    /**
     *
     * @param r the CTR bean which holds the run attributes
     * @param p the parent paragraph
     */
    protected XWPFRun(CTR r, XWPFParagraph p){
        this.run = r;
        this.paragraph = p;
    }

    public CTR getCTR(){
        return run;
    }

    public XWPFParagraph getParagraph(){
        return paragraph;
    }

    /**
     * Whether the bold property shall be applied to all non-complex script characters in the
     * contents of this run when displayed in a document
     *
     * @return <code>true</code> if the bold property is applied
     */
    public boolean isBold(){
        CTRPr pr = run.getRPr();
        return pr != null && pr.isSetB();
    }

    /**
     * Whether the bold property shall be applied to all non-complex script characters in the
     * contents of this run when displayed in a document
     *
     * <p>
     * This formatting property is a toggle property, which specifies that its behavior differs between its use within a
     * style definition and its use as direct formatting. When used as part of a style definition, setting this property
     * shall toggle the current state of that property as specified up to this point in the hierarchy (i.e. applied to not
     * applied, and vice versa). Setting it to <code>false</code> (or an equivalent) shall result in the current
     * setting remaining unchanged. However, when used as direct formatting, setting this property to true or false
     * shall set the absolute state of the resulting property.
     * </p>
     * <p>
     * If this element is not present, the default value is to leave the formatting applied at previous level in the style
     * hierarchy. If this element is never applied in the style hierarchy, then bold shall not be applied to non-complex
     * script characters.
     * </p>
     *
     * @param value <code>true</code> if the bold property is applied to this run
     */
    public void setBold(boolean value){
        CTRPr pr = run.isSetRPr() ? run.getRPr() : run.addNewRPr();
        pr.addNewB().setVal(value ? STOnOff.TRUE : STOnOff.FALSE);
    }

    /**
     * Return the string content of this text run
     *
     * @return the text of this text run or <code>null</code> if not set
     */
    public String getText(){
        return run.sizeOfTArray() == 0 ? null : run.getTArray(0).getStringValue();
    }

    /**
     * Sets the text of this text run
     *
     * @param value the literal text which shall be displayed in the document
     */
    public void setText(String value){
        CTText t = run.sizeOfTArray() == 0 ? run.addNewT() : run.getTArray(0);
        t.setStringValue(value);
    }
}
