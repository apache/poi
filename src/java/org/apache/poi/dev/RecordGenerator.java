/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.dev;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class RecordGenerator
{
    public static void main(String[] args)
            throws Exception
    {
        // Force load so that we don't start generating records and realise this hasn't compiled yet.
        Class.forName("org.apache.poi.generator.FieldIterator");

        if (args.length != 4)
        {
            System.out.println("Usage:");
            System.out.println("  java org.apache.poi.hssf.util.RecordGenerator RECORD_DEFINTIONS RECORD_STYLES DEST_SRC_PATH TEST_SRC_PATH");
        } else
        {
            generateRecords(args[0], args[1], args[2], args[3]);
        }
    }

    private static void generateRecords(String defintionsDir, String recordStyleDir, String destSrcPathDir, String testSrcPathDir)
            throws Exception
    {
        File definitionsFile = new File(defintionsDir);

        for (int i = 0; i < definitionsFile.listFiles().length; i++)
        {
            File file = definitionsFile.listFiles()[i];
            if (file.isFile() &&
                    (file.getName().endsWith("_record.xml") ||
                    file.getName().endsWith("_type.xml")
                    )
            )
            {
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
                if (new File(destinationFilepath).exists() == false)
                {
                    String temp = (recordStyleDir + "/" + extendstg.toLowerCase() + "_test.xsl");
                    args = new String[]{"-in", file.getAbsolutePath(), "-xsl",
                                        temp,
                                        "-out", destinationFilepath,
                                        "-TEXT"};
                    org.apache.xalan.xslt.Process.main(args);
                    System.out.println("Generated test: " + destinationFilepath);
                } else
                {
                    System.out.println("Skipped test generation: " + destinationFilepath);
                }
            }
        }
    }
}
