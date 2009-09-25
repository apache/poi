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
package org.apache.poi.hssf.usermodel.examples;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.SlideShow;

import java.io.FileInputStream;
import java.util.Iterator;

/**
 * Demonstrates how you can extract embedded data from a .xls file
 */
public class EmeddedObjects {
    public static void main(String[] args) throws Exception {
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(args[0]));
        HSSFWorkbook workbook = new HSSFWorkbook(fs);
        for (HSSFObjectData obj : workbook.getAllEmbeddedObjects()) {
            //the OLE2 Class Name of the object
            String oleName = obj.getOLE2ClassName();
            if (oleName.equals("Worksheet")) {
                DirectoryNode dn = (DirectoryNode) obj.getDirectory();
                HSSFWorkbook embeddedWorkbook = new HSSFWorkbook(dn, fs, false);
                //System.out.println(entry.getName() + ": " + embeddedWorkbook.getNumberOfSheets());
            } else if (oleName.equals("Document")) {
                DirectoryNode dn = (DirectoryNode) obj.getDirectory();
                HWPFDocument embeddedWordDocument = new HWPFDocument(dn, fs);
                //System.out.println(entry.getName() + ": " + embeddedWordDocument.getRange().text());
            }  else if (oleName.equals("Presentation")) {
                DirectoryNode dn = (DirectoryNode) obj.getDirectory();
                SlideShow embeddedPowerPointDocument = new SlideShow(new HSLFSlideShow(dn, fs));
                //System.out.println(entry.getName() + ": " + embeddedPowerPointDocument.getSlides().length);
            } else {
                if(obj.hasDirectoryEntry()){
                    // The DirectoryEntry is a DocumentNode. Examine its entries to find out what it is
                    DirectoryNode dn = (DirectoryNode) obj.getDirectory();
                    for (Iterator entries = dn.getEntries(); entries.hasNext();) {
                        Entry entry = (Entry) entries.next();
                        //System.out.println(oleName + "." + entry.getName());
                    }
                } else {
                    // There is no DirectoryEntry
                    // Recover the object's data from the HSSFObjectData instance.
                    byte[] objectData = obj.getObjectData();
                }
            }
        }
    }
}
