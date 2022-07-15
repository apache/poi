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

package org.apache.poi.examples.ss;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hssf.usermodel.HSSFObjectData;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.xmlbeans.XmlException;

/**
 * Loads embedded resources from Workbooks. Code taken from the website:
 *  https://poi.apache.org/spreadsheet/quick-guide.html#Embedded
 */
@SuppressWarnings({"java:S106","java:S4823"})
public final class LoadEmbedded {
    private LoadEmbedded() {}

    public static void main(String[] args) throws IOException, EncryptedDocumentException, OpenXML4JException, XmlException {
       Workbook wb = WorkbookFactory.create(new File(args[0]));
       loadEmbedded(wb);
   }

   public static void loadEmbedded(Workbook wb) throws IOException, InvalidFormatException, OpenXML4JException, XmlException {
       if (wb instanceof HSSFWorkbook) {
           loadEmbedded((HSSFWorkbook)wb);
       }
       else if (wb instanceof XSSFWorkbook) {
           loadEmbedded((XSSFWorkbook)wb);
       }
       else {
           throw new IllegalArgumentException(wb.getClass().getName());
       }
   }

   public static void loadEmbedded(HSSFWorkbook workbook) throws IOException {
       for (HSSFObjectData obj : workbook.getAllEmbeddedObjects()) {
           //the OLE2 Class Name of the object
           String oleName = obj.getOLE2ClassName();
           switch (oleName) {
               case "Worksheet": {
                   DirectoryNode dn = (DirectoryNode) obj.getDirectory();
                   HSSFWorkbook embeddedWorkbook = new HSSFWorkbook(dn, false);
                   embeddedWorkbook.close();
                   break;
               }
               case "Document": {
                   DirectoryNode dn = (DirectoryNode) obj.getDirectory();
                   HWPFDocument embeddedWordDocument = new HWPFDocument(dn);
                   embeddedWordDocument.close();
                   break;
               }
               case "Presentation": {
                   DirectoryNode dn = (DirectoryNode) obj.getDirectory();
                   SlideShow<?, ?> embeddedSlieShow = new HSLFSlideShow(dn);
                   embeddedSlieShow.close();
                   break;
               }
               default:
                   if (obj.hasDirectoryEntry()) {
                       // The DirectoryEntry is a DocumentNode. Examine its entries to find out what it is
                       DirectoryNode dn = (DirectoryNode) obj.getDirectory();
                       for (Entry entry : dn) {
                           //System.out.println(oleName + "." + entry.getName());
                       }
                   } else {
                       // There is no DirectoryEntry
                       // Recover the object's data from the HSSFObjectData instance.
                       byte[] objectData = obj.getObjectData();
                   }
                   break;
           }
       }
   }

   public static void loadEmbedded(XSSFWorkbook workbook) throws IOException, InvalidFormatException,
           OpenXML4JException, XmlException {
       for (PackagePart pPart : workbook.getAllEmbeddedParts()) {
           String contentType = pPart.getContentType();
           switch (contentType) {
               case "application/vnd.ms-excel":
                   // Excel Workbook - either binary or OpenXML
                   try (InputStream stream = pPart.getInputStream()) {
                       HSSFWorkbook embeddedWorkbook = new HSSFWorkbook(stream);
                       embeddedWorkbook.close();
                   }
                   break;
               case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                   // Excel Workbook - OpenXML file format
                   try (InputStream stream = pPart.getInputStream()) {
                       XSSFWorkbook embeddedWorkbook = new XSSFWorkbook(stream);
                       embeddedWorkbook.close();
                   }
                   break;
               case "application/msword":
                   // Word Document - binary (OLE2CDF) file format
                   try (InputStream stream = pPart.getInputStream()) {
                       HWPFDocument document = new HWPFDocument(stream);
                       document.close();
                   }
                   break;
               case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                   // Word Document - OpenXML file format
                   try (InputStream stream = pPart.getInputStream()) {
                       XWPFDocument document = new XWPFDocument(stream);
                       document.close();
                   }
                   break;
               case "application/vnd.ms-powerpoint":
                   // PowerPoint Document - binary file format
                   try (InputStream stream = pPart.getInputStream()) {
                       HSLFSlideShow slideShow = new HSLFSlideShow(stream);
                       slideShow.close();
                   }
                   break;
               case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                   // PowerPoint Document - OpenXML file format
                   try (InputStream stream = pPart.getInputStream()) {
                       XMLSlideShow slideShow = new XMLSlideShow(stream);
                       slideShow.close();
                   }
                   break;
               default:
                   // Any other type of embedded object.
                   System.out.println("Unknown Embedded Document: " + contentType);
                   try (InputStream inputStream = pPart.getInputStream()) {

                   }
                   break;
           }
       }
   }
}
