/*   Copyright 2006 The Apache Software Foundation
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
 *   limitations under the License.
 */
package misc.detailed;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;

import misc.common.JiraTestBase;

public class JiraRegressionSchemaCompilerTest extends JiraTestBase
{
    public JiraRegressionSchemaCompilerTest(String name)
    {
        super(name);
    }

    private List _testCompile(File[] xsdFiles,
                              String outputDirName)
    {
        System.out.println(xsdFiles[0].getAbsolutePath());
        List errors = new ArrayList();
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(xsdFiles);
        params.setErrorListener(errors);
        params.setSrcDir(new File(schemaCompOutputDirPath + outputDirName + P + "src"));
        params.setClassesDir(new File(schemaCompOutputDirPath + outputDirName + P + "classes"));
        SchemaCompiler.compile(params);
        return errors;
    }

    private boolean findErrMsg(Collection errors, String msg)
    {
        boolean errFound = false;
        if (!errors.isEmpty())
        {
            for (Iterator i = errors.iterator(); i.hasNext();)
            {
                XmlError err = (XmlError) i.next();
                int errSeverity = err.getSeverity();
                if (errSeverity == XmlError.SEVERITY_ERROR)
                {
                    if (msg.equals(err.getMessage()))
                        errFound = true;
                }
            }
        }
        errors.clear();
        return errFound;
    }

    public void test_jira_xmlbeans236()
    {
        File[] xsdFiles =
            new File[] { new File(scompTestFilesRoot + "xmlbeans_236.xsd_") };
        String outputDirName = "xmlbeans_236";
        List errors = _testCompile(xsdFiles, outputDirName);
        if (printOptionErrMsgs(errors))
        {
            fail("test_jira_xmlbeans236(): failure when executing scomp");
        }
    }

    public void test_jira_xmlbeans239()
    {
        /* complexType with complexContent extending base type with 
           simpleContent is valid */
        File[] xsdFiles =
            new File[] { new File(scompTestFilesRoot + "xmlbeans_239a.xsd_") };
        String outputDirName = "xmlbeans_239";
        List errors = _testCompile(xsdFiles, outputDirName);
        if (printOptionErrMsgs(errors))
        {
            fail("test_jira_xmlbeans239(): failure when executing scomp");
        }

        /* complexType with complexContent extending simpleType is not valid */
        xsdFiles =
            new File[] { new File(scompTestFilesRoot + "xmlbeans_239b.xsd_") };
        errors = _testCompile(xsdFiles, outputDirName);
        String msg = "Type 'dtSTRING@http://www.test.bmecat.org' is being used as the base type for a complexContent definition. To do this the base type must be a complex type.";
        assertTrue(findErrMsg(errors, msg));

        /* complexType with complexContent extending base type with
           simpleContent cannot add particles */
        xsdFiles =
            new File[] { new File(scompTestFilesRoot + "xmlbeans_239c.xsd_") };
        errors = _testCompile(xsdFiles, outputDirName);
        msg = "This type extends a base type 'dtMLSTRING@http://www.test.bmecat.org' which has simpleContent. In that case this type cannot add particles.";
        assertTrue(findErrMsg(errors, msg));
    }

    public void test_jira_xmlbeans251()
    {
        File[] xsdFiles =
            new File[] { new File(scompTestFilesRoot + "xmlbeans_251.xsd_") };
        String outputDirName = "xmlbeans_251";
        List errors = _testCompile(xsdFiles, outputDirName);
        if (printOptionErrMsgs(errors))
        {
            fail("test_jira_xmlbeans251(): failure when executing scomp");
        }
    }
}
