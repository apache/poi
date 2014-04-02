
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *  Description of the Class
 *
 *@author     andy
 *@since      May 10, 2002
 */
public class RecordGenerator {
    /**
     *  The main program for the RecordGenerator class
     *
     *@param  args           The command line arguments
     *@exception  Exception  Description of the Exception
     */
    public static void main(String[] args)
             throws Exception {
        // Force load so that we don't start generating records and realise this hasn't compiled yet.
        Class.forName("org.apache.poi.generator.FieldIterator");

        if (args.length != 4) {
            System.out.println("Usage:");
            System.out.println("  java org.apache.poi.hssf.util.RecordGenerator RECORD_DEFINTIONS RECORD_STYLES DEST_SRC_PATH TEST_SRC_PATH");
        } else {
            generateRecords(args[0], args[1], args[2], args[3]);
        }
    }


    private static void generateRecords(String defintionsDir, String recordStyleDir, String destSrcPathDir, String testSrcPathDir)
             throws Exception {
        File definitionsFile = new File(defintionsDir);

        for (int i = 0; i < definitionsFile.listFiles().length; i++) {
            File file = definitionsFile.listFiles()[i];
            if (file.isFile() &&
                    (file.getName().endsWith("_record.xml") ||
                    file.getName().endsWith("_type.xml")
                    )
                    ) {
                // Get record name and package
                DocumentBuilderFactory factory =
                        DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);
                Element record = document.getDocumentElement();
                String extendstg = record.getElementsByTagName("extends").item(0).getFirstChild().getNodeValue();
                String suffix = record.getElementsByTagName("suffix").item(0).getFirstChild().getNodeValue();
                String recordName = record.getAttributes().getNamedItem("name").getNodeValue();
                String packageName = record.getAttributes().getNamedItem("package").getNodeValue();
                packageName = packageName.replace('.', '/');

                // Generate record
                String destinationPath = destSrcPathDir + "/" + packageName;
                File destinationPathFile = new File(destinationPath);
                destinationPathFile.mkdirs();
                String destinationFilepath = destinationPath + "/" + recordName + suffix + ".java";
                transform(file, new File(destinationFilepath), new File(recordStyleDir + "/" + extendstg.toLowerCase() + ".xsl"));
                System.out.println("Generated " + suffix + ": " + destinationFilepath);

                // Generate test (if not already generated)
                destinationPath = testSrcPathDir + "/" + packageName;
                destinationPathFile = new File(destinationPath);
                destinationPathFile.mkdirs();
                destinationFilepath = destinationPath + "/Test" + recordName + suffix + ".java";
                if (new File(destinationFilepath).exists() == false) {
                    String temp = (recordStyleDir + "/" + extendstg.toLowerCase() + "_test.xsl");
                    transform(file, new File(destinationFilepath), new File(temp));
                    System.out.println("Generated test: " + destinationFilepath);
                } else {
                    System.out.println("Skipped test generation: " + destinationFilepath);
                }
            }
        }
    }

    
    
    /**
     * <p>Executes an XSL transformation. This process transforms an XML input
     * file into a text output file controlled by an XSLT specification.</p>
     * 
     * @param in the XML input file
     * @param out the text output file
     * @param xslt the XSLT specification, i.e. an XSL style sheet
     * @throws FileNotFoundException 
     * @throws TransformerException 
     */
    private static void transform(final File in, final File out, final File xslt)
    throws FileNotFoundException, TransformerException
    {
        final Reader r = new FileReader(xslt);
        final StreamSource ss = new StreamSource(r);
        final TransformerFactory tf = TransformerFactory.newInstance();
        final Transformer t;
        try
        {
            t = tf.newTransformer(ss);
        }
        catch (TransformerException ex)
        {
            System.err.println("Error compiling XSL style sheet " + xslt);
            throw ex;
        }
        final Properties p = new Properties();
        p.setProperty(OutputKeys.METHOD, "text");
        t.setOutputProperties(p);
        final Result result = new StreamResult(out);
        t.transform(new StreamSource(in), result);        
    }

}
