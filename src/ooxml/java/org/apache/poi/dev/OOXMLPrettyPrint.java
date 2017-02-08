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
package org.apache.poi.dev;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.openxml4j.opc.internal.ZipHelper;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.util.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Reads a zipped OOXML file and produces a copy with the included 
 * pretty-printed XML files.
 * 
 *  This is useful for comparing OOXML files produced by different tools as the often 
 *  use different formatting of the XML.
 */
public class OOXMLPrettyPrint {
    private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private final DocumentBuilder documentBuilder;

    public OOXMLPrettyPrint() throws ParserConfigurationException {
        // allow files with much lower inflation rate here as there is no risk of Zip Bomb attacks in this developer tool
        ZipSecureFile.setMinInflateRatio(0.00001);
        
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
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
    			System.err.println("\t" + f.toString());
    			System.exit(2);
    		}

    		handleFile(f, new File(args[i+1]));
		}
		System.out.println("Done.");
	}

    private static void handleFile(File file, File outFile) throws ZipException,
            IOException, TransformerException, ParserConfigurationException {
        System.out.println("Reading zip-file " + file + " and writing pretty-printed XML to " + outFile);

        ZipFile zipFile = ZipHelper.openZipFile(file);
		try {
		    ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));
		    try {
		        new OOXMLPrettyPrint().handle(zipFile, out);
		    } finally {
		        out.close();
		    }
		} finally {
		    zipFile.close();

		    System.out.println();
		}
    }

	private void handle(ZipFile file, ZipOutputStream out) throws IOException, TransformerException {
        Enumeration<? extends ZipEntry> entries = file.entries();
        while(entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();

            String name = entry.getName();
            out.putNextEntry(new ZipEntry(name));
            try {
                if(name.endsWith(".xml") || name.endsWith(".rels")) {
                    Document document = documentBuilder.parse(new InputSource(file.getInputStream(entry)));
                    document.setXmlStandalone(true);
                    pretty(document, out, 2);
                } else {
                    System.out.println("Not pretty-printing non-XML file " + name);
                    IOUtils.copy(file.getInputStream(entry), out);
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
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = transformerFactory.newTransformer();
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    if (indent > 0) {
	        // set properties to indent the resulting XML nicely
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));
	    }
	    Result result = new StreamResult(outputStream);
	    Source source = new DOMSource(document);
	    transformer.transform(source, result);
	}	
}
