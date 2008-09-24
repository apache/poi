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

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Enumeration;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;

/**
 * Utility class which dumps the contents of a *.xlsx file into file system.
 *
 * @author Yegor Kozlov
 */
public class XSSFDump {

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            System.out.println("Dumping " + args[i]);
            ZipFile zip = new ZipFile(args[i]);
            dump(zip);
        }
    }

    public static void dump(ZipFile zip) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        String zipname = zip.getName();
        int sep = zipname.lastIndexOf('.');
        File root = new File(zipname.substring(0, sep));
        root.mkdir();

        Enumeration en = zip.entries();
        while(en.hasMoreElements()){
            ZipEntry entry = (ZipEntry)en.nextElement();
            String name = entry.getName();
            int idx = name.lastIndexOf('/');
            if(idx != -1){
                File bs = new File(root, name.substring(0, idx));
                bs.mkdirs();
            }

            File f = new File(root, entry.getName());
            FileOutputStream out = new FileOutputStream(f);

            if(entry.getName().endsWith(".xml") || entry.getName().endsWith(".vml") || entry.getName().endsWith(".rels")){
                //pass the xml through the Xerces serializer to produce nicely formatted output
                Document doc = builder.parse(zip.getInputStream(entry));

                OutputFormat  format  = new OutputFormat( doc );
                format.setIndenting(true);

                XMLSerializer serial = new  XMLSerializer( out, format );
                serial.asDOMSerializer();
                serial.serialize( doc.getDocumentElement() );

            } else {
                int pos;
                byte[] chunk = new byte[2048];
                InputStream is = zip.getInputStream(entry);
                while((pos = is.read(chunk)) > 0) out.write(chunk, 0, pos);
            }
            out.close();

        }
    }
}
