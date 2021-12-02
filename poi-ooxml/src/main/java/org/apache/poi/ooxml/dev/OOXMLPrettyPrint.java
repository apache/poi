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
package org.apache.poi.ooxml.dev;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.openxml4j.opc.internal.ZipHelper;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.XMLHelper;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Reads a zipped OOXML file and produces a copy with the included
 * pretty-printed XML files.
 *
 *  This is useful for comparing OOXML files produced by different tools as they often
 *  use different formatting of the XML.
 */
public class OOXMLPrettyPrint {
    private static final String XML_INDENT_AMOUNT = "{http://xml.apache.org/xslt}indent-amount";

    private final DocumentBuilder documentBuilder;

    public OOXMLPrettyPrint() {
        // allow files with much lower inflation rate here as there is no risk of Zip Bomb attacks in this developer tool
        ZipSecureFile.setMinInflateRatio(0.00001);

        documentBuilder = DocumentHelper.newDocumentBuilder();
    }

    public static void main(String[] args) throws Exception {
        if(args.length <= 1 || args.length % 2 != 0) {
            System.err.println("Use:");
            System.err.println("\tjava OOXMLPrettyPrint [<filename> <outfilename>] ...");
            System.exit(1);
        }

        for(int i = 0;i < args.length;i+=2) {
            File f = new File(args[i]);
            if(! f.exists()) {
                System.err.println("Error, file not found!");
                System.err.println("\t" + f);
                System.exit(2);
            }

            handleFile(f, new File(args[i+1]));
        }
        System.out.println("Done.");
    }

    private static void handleFile(File file, File outFile) throws IOException {
        System.out.println("Reading zip-file " + file + " and writing pretty-printed XML to " + outFile);

        try (ZipSecureFile zipFile = ZipHelper.openZipFile(file)) {
            try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)))) {
                new OOXMLPrettyPrint().handle(zipFile, out);
            }
        } finally {
            System.out.println();
        }
    }

    private void handle(ZipSecureFile file, ZipOutputStream out) throws IOException {
        Enumeration<? extends ZipArchiveEntry> entries = file.getEntries();
        while(entries.hasMoreElements()) {
            ZipArchiveEntry entry = entries.nextElement();

            String name = entry.getName();
            out.putNextEntry(new ZipEntry(name));
            try {
                if(name.endsWith(".xml") || name.endsWith(".rels")) {
                    Document document = documentBuilder.parse(new InputSource(file.getInputStream(entry)));
                    document.setXmlStandalone(true);
                    pretty(document, out, 2);
                } else {
                    System.out.println("Not pretty-printing non-XML file " + name);
                    try (InputStream in = file.getInputStream(entry)) {
                        IOUtils.copy(in, out);
                    }
                }
            } catch (Exception e) {
                throw new IOException("While handling entry " + name, e);
            } finally {
                out.closeEntry();
            }
            System.out.print(".");
        }
    }

    private static void pretty(Document document, OutputStream outputStream, int indent) throws TransformerException {
        Transformer transformer = XMLHelper.newTransformer();
        if (indent > 0) {
            // set properties to indent the resulting XML nicely
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(XML_INDENT_AMOUNT, Integer.toString(indent));
        }
        Result result = new StreamResult(outputStream);
        Source source = new DOMSource(document);
        transformer.transform(source, result);
    }
}
