/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.dev;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 *  Description of the Class
 *
 *@author     andy
 *@created    May 10, 2002
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
                String args[] = new String[]{"-in", file.getAbsolutePath(), "-xsl", recordStyleDir + "/" + extendstg.toLowerCase() + ".xsl",
                        "-out", destinationFilepath,
                        "-TEXT"};

                org.apache.xalan.xslt.Process.main(args);
                System.out.println("Generated " + suffix + ": " + destinationFilepath);

                // Generate test (if not already generated)
                destinationPath = testSrcPathDir + "/" + packageName;
                destinationPathFile = new File(destinationPath);
                destinationPathFile.mkdirs();
                destinationFilepath = destinationPath + "/Test" + recordName + suffix + ".java";
                if (new File(destinationFilepath).exists() == false) {
                    String temp = (recordStyleDir + "/" + extendstg.toLowerCase() + "_test.xsl");
                    args = new String[]{"-in", file.getAbsolutePath(), "-xsl",
                            temp,
                            "-out", destinationFilepath,
                            "-TEXT"};
                    org.apache.xalan.xslt.Process.main(args);
                    System.out.println("Generated test: " + destinationFilepath);
                } else {
                    System.out.println("Skipped test generation: " + destinationFilepath);
                }
            }
        }
    }
}
