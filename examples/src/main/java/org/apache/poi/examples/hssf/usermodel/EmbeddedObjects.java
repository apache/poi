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
package org.apache.poi.examples.hssf.usermodel;

import java.io.Closeable;
import java.io.FileInputStream;

import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hssf.usermodel.HSSFObjectData;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Demonstrates how you can extract embedded data from a .xls file
 */
@SuppressWarnings({"java:S106","java:S4823","java:S1192"})
public final class EmbeddedObjects {
    private EmbeddedObjects() {}

    @SuppressWarnings("unused")
    public static void main(String[] args) throws Exception {
        try (
            FileInputStream fis = new FileInputStream(args[0]);
            POIFSFileSystem fs = new POIFSFileSystem(fis);
            HSSFWorkbook workbook = new HSSFWorkbook(fs)
        ) {
            for (HSSFObjectData obj : workbook.getAllEmbeddedObjects()) {
                //the OLE2 Class Name of the object
                String oleName = obj.getOLE2ClassName();
                DirectoryNode dn = (obj.hasDirectoryEntry()) ? (DirectoryNode) obj.getDirectory() : null;
                Closeable document = null;
                switch (oleName) {
                    case "Worksheet":
                        document = new HSSFWorkbook(dn, fs, false);
                        break;
                    case "Document":
                        document = new HWPFDocument(dn);
                        break;
                    case "Presentation":
                        document = new HSLFSlideShow(dn);
                        break;
                    default:
                        if (dn != null) {
                            // The DirectoryEntry is a DocumentNode. Examine its entries to find out what it is
                            for (Entry entry : dn) {
                                String name = entry.getName();
                            }
                        } else {
                            // There is no DirectoryEntry
                            // Recover the object's data from the HSSFObjectData instance.
                            byte[] objectData = obj.getObjectData();
                        }
                        break;
                }
                if (document != null) {
                    document.close();
                }
            }
        }
    }
}
