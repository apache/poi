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

package org.apache.poi.xssf.dev;

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.poi.openxml4j.opc.internal.ZipHelper;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.util.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;

/**
 * Utility class which dumps the contents of a *.xlsx file into file system.
 *
 * @author Yegor Kozlov
 */
public final class XSSFDump {

    private XSSFDump() {}

    public static void main(String[] args) throws Exception {
        for (String arg : args) {
            System.out.println("Dumping " + arg);
            try (ZipSecureFile zip = ZipHelper.openZipFile(arg)) {
                dump(zip);
            }
        }
    }
    
    private static void createDirIfMissing(File directory) throws RuntimeException {
        if (!directory.exists()) {
            boolean dirWasCreated = directory.mkdir();
            if (!dirWasCreated) {
                throw new RuntimeException("Unable to create directory: " + directory);
            }
        }
    }
    
    private static void recursivelyCreateDirIfMissing(File directory) throws RuntimeException {
        if (!directory.exists()) {
            boolean dirsWereCreated = directory.mkdirs();
            if (!dirsWereCreated) {
                throw new RuntimeException("Unable to recursively create directory: " + directory);
            }
        }
    }
    

    public static void dump(ZipSecureFile zip) throws Exception {
        String zipname = zip.getName();
        int sep = zipname.lastIndexOf('.');
        File root = new File(zipname.substring(0, sep));
        createDirIfMissing(root);
        System.out.println("Dumping to directory " + root);

        Enumeration<? extends ZipArchiveEntry> en = zip.getEntries();
        while (en.hasMoreElements()) {
            ZipArchiveEntry entry = en.nextElement();
            String name = entry.getName();
            int idx = name.lastIndexOf('/');
            if (idx != -1) {
                File bs = new File(root, name.substring(0, idx));
                recursivelyCreateDirIfMissing(bs);
            }

            File f = new File(root, entry.getName());
            try (final OutputStream out = new FileOutputStream(f)) {
                if (entry.getName().endsWith(".xml") || entry.getName().endsWith(".vml") || entry.getName().endsWith(".rels")) {
                    try {
                        Document doc = DocumentHelper.readDocument(zip.getInputStream(entry));
                        XmlObject xml = XmlObject.Factory.parse(doc, DEFAULT_XML_OPTIONS);
                        XmlOptions options = new XmlOptions();
                        options.setSavePrettyPrint();
                        xml.save(out, options);
                    } catch (XmlException e) {
                        System.err.println("Failed to parse " + entry.getName() + ", dumping raw content");
                        IOUtils.copy(zip.getInputStream(entry), out);
                    }
                } else {
                    IOUtils.copy(zip.getInputStream(entry), out);
                }
            }
        }
    }
}
