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

import java.util.ArrayList;

import org.apache.poi.xwpf.XWPFDocument;
import org.apache.poi.xwpf.model.XMLParagraph;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPTab;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPicture;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtContentRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Sketch of XWPF paragraph class
 */
public class XWPFParagraph extends XMLParagraph
{
    protected XWPFDocument docRef; // XXX: we'd like to have access to document's hyperlink, comments and other tables
    /**
     * TODO - replace with RichText String
     */
    private StringBuffer text = new StringBuffer();
    private StringBuffer pictureText = new StringBuffer();
    
    public XWPFParagraph(CTP prgrph, XWPFDocument docRef)
    {
        super(prgrph);
        this.docRef = docRef;
        
        // All the runs to loop over
        // TODO - replace this with some sort of XPath expression
        //  to directly find all the CTRs, in the right order
        ArrayList<CTR> rs = new ArrayList<CTR>();
        CTR[] tmp;
        
        // Get the main text runs
        tmp = paragraph.getRArray();
        for(int i=0; i<tmp.length; i++) {
        	rs.add(tmp[i]);
        }
        
        // Not sure quite what these are, but they hold 
        //  more text runs
        CTSdtRun[] sdts = paragraph.getSdtArray();
        for(int i=0; i<sdts.length; i++) {
        	CTSdtContentRun run = sdts[i].getSdtContent();
        	tmp = run.getRArray();
            for(int j=0; j<tmp.length; j++) {
            	rs.add(tmp[j]);
            }
        }
    
        
        // Get text of the paragraph
        for (int j = 0; j < rs.size(); j++) {
            // Grab the text and tabs of the paragraph
        	// Do so in a way that preserves the ordering
        	XmlCursor c = rs.get(j).newCursor();
        	c.selectPath( "./*" );
        	while(c.toNextSelection()) {
        		XmlObject o = c.getObject();
        		if(o instanceof CTText) {
        			text.append( ((CTText)o).getStringValue() );
        		}
        		if(o instanceof CTPTab) {
        			text.append("\t");
        		}
        	}
        	
            // Loop over pictures inside our
            //  paragraph, looking for text in them
            CTPicture[] picts = rs.get(j).getPictArray();
            for (int k = 0; k < picts.length; k++) {
                XmlObject[] t = picts[k].selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:t");
                for (int m = 0; m < t.length; m++) {
                    NodeList kids = t[m].getDomNode().getChildNodes();
                    for (int n = 0; n < kids.getLength(); n++) {
                        if (kids.item(n) instanceof Text) {
                            pictureText.append("\n" + kids.item(n).getNodeValue());
                        }
                    }
                }
            }
        }
    }
    
    public XWPFParagraph(CTP prgrph) {
        this(prgrph, null);
    }
    
    public XWPFParagraph(XMLParagraph paragraph) {
        this(paragraph.getCTP());
    }
    
    
    public boolean isEmpty() {
    	return !paragraph.getDomNode().hasChildNodes();
    }
    
    
    public XWPFDocument getDocRef() {
        return docRef;
    }
    
    /**
     * Return the textual content of the paragraph, 
     *  including text from pictures in it.
     */
    public String getText() {
        return getParagraphText() + getPictureText();
    }
    /**
     * Returns the text of the paragraph, but not
     *  of any objects in the paragraph
     */
    public String getParagraphText() {
        return text.toString();
    }
    /**
     * Returns any text from any suitable
     *  pictures in the paragraph
     */
    public String getPictureText() {
    	return pictureText.toString();
    }
}
