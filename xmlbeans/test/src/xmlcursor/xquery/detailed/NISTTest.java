/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package xmlcursor.xquery.detailed;

import junit.framework.TestCase;
import junit.framework.TestResult;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import tools.util.JarUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

import xmlcursor.xpath.common.XPathCommon;
import xmlcursor.xquery.common.XQTestCase;

/**
 *
 */
public class NISTTest extends XQTestCase
{


    public NISTTest(String s)
    {
        super(s);
    }
    public NISTTest(noNamespace.TestCase test)
    {
        this(test.getName());
        mTest=test;
    }

   public void testRun() throws Exception
    {
        String query=null;
        String outFile=null;
        XmlObject obj=XmlObject.Factory.parse("<xml-fragment/>");


            //NIST BUG: folder is called testSuite but appears
            //as testsuite in desc. file
            String temp = mTest.getFilePath();
            temp=temp.replaceAll("testsuite","testSuite");
            temp=temp.replace( (char)92,'/');
            query = ZipUtil.getStringFromZip(
                    pathToZip,zipName,temp + mTest.getName()+".xq") ;
            //bad comment syntax in suite
            query =  query.replaceAll("\\{--","(:");
            query =  query.replaceAll("--\\}",":)");
            noNamespace.TestCase.OutputFile[] outFiles
                    = mTest.getOutputFileArray();
            noNamespace.TestCase.InputFile[] inFiles
                    = mTest.getInputFileArray();


            for (int i=0; i < inFiles.length; i++)
            {
                if ( !inFiles[i].getStringValue().equals("emptyDoc"))
                throw new RuntimeException ("Fix this code. Input file: "+
                        inFiles[i]);

//               assertEquals(inFiles.length, outFiles.length);

                XmlObject[] queryRes = obj.execQuery(query);
                String input = ZipUtil.getStringFromZip(
                    pathToZip,zipName,temp+outFiles[i].getStringValue());
                XmlObject expRes=XmlObject.Factory.parse( input );
                XPathCommon.compare(queryRes,new XmlObject[]{expRes});
            }

    }

    noNamespace.TestCase mTest;
     static String pathToZip;
    static final String zipName="xmlQuery.zip";
}
