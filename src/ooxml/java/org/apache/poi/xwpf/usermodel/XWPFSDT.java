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

import java.util.List;

import org.apache.poi.POIXMLDocumentPart;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTString;

/**
 * Experimental class to offer rudimentary read-only processing of 
 *  of StructuredDocumentTags/ContentControl
 *  
 *
 *
 * WARNING - APIs expected to change rapidly
 * 
 */
public class XWPFSDT implements IBodyElement, IRunBody, ISDTContents, IRunElement {
   private final String title;
   private final String tag;
   private final XWPFSDTContent content;
   private final IBody part;

   public XWPFSDT(CTSdtRun sdtRun, IBody part){
       this.part = part;
       this.content = new XWPFSDTContent(sdtRun.getSdtContent(), part, this);
       CTSdtPr pr = sdtRun.getSdtPr();
       List<CTString> aliases = pr.getAliasList();
       if (aliases != null && aliases.size() > 0){
          title = aliases.get(0).getVal();
       } else {
          title = "";
       }
       @SuppressWarnings("deprecation")
       CTString[] array = pr.getTagArray();
       if (array != null && array.length > 0){
          tag = array[0].getVal();
       } else {
          tag = "";
       }
  
   }
   public XWPFSDT(CTSdtBlock block, IBody part){
      this.part = part;
      this.content = new XWPFSDTContent( block.getSdtContent(), part, this);
      CTSdtPr pr = block.getSdtPr();
      List<CTString> aliases = pr.getAliasList();
      if (aliases != null && aliases.size() > 0){
         title = aliases.get(0).getVal();
      } else {
         title = "";
      }
      @SuppressWarnings("deprecation")
      CTString[] array = pr.getTagArray();
      if (array != null && array.length > 0){
         tag = array[0].getVal();
      } else {
         tag = "";
      }
 
   }
   public String getTitle(){
      return title;
   }
   public String getTag(){
      return tag;
   }
   public XWPFSDTContent getContent(){
      return content;
   }

   public IBody getBody() {
      // TODO Auto-generated method stub
      return null;
   }

   public POIXMLDocumentPart getPart() {
      return part.getPart();
   }

   public BodyType getPartType() {
      return BodyType.CONTENTCONTROL;
   }

   public BodyElementType getElementType() {
      return BodyElementType.CONTENTCONTROL;
   }

   public XWPFDocument getDocument() {
      return part.getXWPFDocument();
   }
}
