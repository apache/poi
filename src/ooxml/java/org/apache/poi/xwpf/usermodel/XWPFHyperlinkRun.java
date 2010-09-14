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

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHyperlink;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;

/**
 * A run of text with a Hyperlink applied to it.
 * Any given Hyperlink may be made up of multiple of these.
 */
public class XWPFHyperlinkRun extends XWPFRun
{
   private CTHyperlink hyperlink;
   
   public XWPFHyperlinkRun(CTHyperlink hyperlink, CTR run, XWPFParagraph p) {
      super(run, p);
      this.hyperlink = hyperlink;
   }
   
   public CTHyperlink getCTHyperlink() {
      return hyperlink;
   }
   
   public String getAnchor() {
      return hyperlink.getAnchor();
   }
   
   /**
    * Returns the ID of the hyperlink, if one is set.
    */
   public String getHyperlinkId() {
      return hyperlink.getId();
   }
   public void setHyperlinkId(String id) {
      hyperlink.setId(id);
   }
   
   /**
    * If this Hyperlink is an external reference hyperlink,
    *  return the object for it.
    */
   public XWPFHyperlink getHyperlink(XWPFDocument document) {
      String id = getHyperlinkId();
      if(id == null)
         return null;
      
      return document.getHyperlinkByID(id);
   }
}
