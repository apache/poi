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

import java.io.IOException;

import xbeans.cases.xmlcursor.xquery.useCases.XmlQueryUseCasesDocument;
import xbeans.cases.xmlcursor.xquery.useCases.QueryT;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;
import xmlcursor.xpath.common.XPathCommon;
import xmlcursor.xquery.common.XQTestCase;

/**
 *
 */
public class UseCaseTest
    extends XQTestCase
{

    public UseCaseTest(String s)
    {
        super("testRun");
    }

    public UseCaseTest(
        XmlQueryUseCasesDocument.XmlQueryUseCases.Group.Test test,
        String groupName)
    {
        this("");
        super.setName(groupName+"."+test.getQuery().getFile());
        mTest = test;
        this.groupName = groupName;
    }


    public void testRun() throws Exception
    {
        String query = null;
        String outFile = null;
        String inFile =null;
        QueryT input = mTest.getInput();
        inFile =
            input == null ? "input.xml" : input.getFile();

        inFile = topDir+"/"+groupName + "/"+ inFile;
        TestResult res=new TestResult();
        try
        {
            String sXml = ZipUtil.getStringFromZip(pathToZip,zipName,inFile);
            XmlObject obj = XmlObject.Factory.parse(sXml);
            query = ZipUtil.getStringFromZip(pathToZip, zipName,
               topDir+"/"+groupName + "/"+ mTest.getQuery().getFile());

            outFile
                = mTest.getOutput().getFile();
            String expXml = ZipUtil.getStringFromZip(pathToZip,zipName,
                topDir+"/"+groupName + "/"+ outFile);
            XmlObject[] queryRes = obj.execQuery(query);
            XmlObject expRes = XmlObject.Factory.parse(expXml);
            XPathCommon.compare(queryRes, new XmlObject[]{expRes});

        }
        catch (IOException e)
        {
            res.addError(this, e);
        }
        catch (XmlException e)
        {
            res.addError(this, e);
        }
        catch (Exception e)
        {
            res.addError(this, e);
        }
        return;

    }
     static void setPathToZip(String path)
      {
          pathToZip=path;
      }

    XmlQueryUseCasesDocument.XmlQueryUseCases.Group.Test mTest;
    private String groupName;
     static String pathToZip;
    static final String topDir="xmlUseCases";
    static final String zipName = "useCases.zip";
}
