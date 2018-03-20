/*
 *   Copyright 2004 The Apache Software Foundation
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
package misc.detailed;

import misc.common.JiraTestBase;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.File;

import net.eads.space.scoexml.test.TestExponentDocument;
import junit.framework.Assert;

/**
 *
 */
public class JiraRegression151_200Test extends JiraTestBase
{
    public JiraRegression151_200Test(String name)
    {
        super(name);
    }

    /**
     * [XMLBEANS-175]   Validation of decimal in exponential representation fails
     * @throws Exception
     */
    public void test_jira_xmlbeans175() throws Exception{

        TestExponentDocument.TestExponent exponent = TestExponentDocument.TestExponent.Factory.newInstance();
        exponent.setDecimal(new BigDecimal("1E1"));

        ArrayList errors = new ArrayList();
        XmlOptions validationOptions = new XmlOptions();
        validationOptions.setErrorListener(errors);
        exponent.validate(validationOptions);

        for (Iterator iterator = errors.iterator(); iterator.hasNext();) {
            System.out.println("Validation Error:" + iterator.next());
        }

        // fails, IMHO should not!
        assertEquals(0, errors.size());
        /* note: the following uses JDK 1.5 API, not supported in 1.4
        // workaround
        exponent.setDecimal(new BigDecimal(new BigDecimal("1E1").toPlainString()));
        errors.removeAll(errors);
        exponent.validate(validationOptions);
        assertEquals(0, errors.size());
        */
    }

    /**
     * [XMLBEANS-179]   Saving xml with '&' and '<' characters in attribute values throws an ArrayIndexOutOfBoundsException
     */
    public void test_jira_xmlbeans179() throws Exception{
        String xmlWithIssues = "<Net id=\"dbid:66754220\" name=\"3&lt;.3V\" type=\"POWER\"/>";

        XmlObject xobj = XmlObject.Factory.parse(xmlWithIssues);
        File outFile = new File(schemaCompOutputDirPath + P + "jira_xmlbeans179.xml");
        assertNotNull(outFile);

        if(outFile.exists())
            outFile.delete();

        xobj.save(outFile);
    }

    /*
    * [XMLBEANS-184]: NPE when running scomp without nopvr option
    *
    */
    public void test_jira_xmlbeans184() throws Exception {
        List errors = new ArrayList();

        // compile with nopvr, goes thro fine
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[]{new File(scompTestFilesRoot + "xmlbeans_184_vdx_data_V1.04.xsd_")});
        params.setErrorListener(errors);
        params.setSrcDir(schemaCompSrcDir);
        params.setClassesDir(schemaCompClassesDir);
        params.setNoPvr(true);

        try {
            SchemaCompiler.compile(params);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            fail("NPE when executing scomp");
        }

        if (printOptionErrMsgs(errors)) {
            Assert.fail("test_jira_xmlbeans184() : Errors found when executing scomp");
        }

        // now compile without the pvr option and NPE is thrown
        params.setNoPvr(false);
        try {
            SchemaCompiler.compile(params);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            fail("NPE when executing scomp");
        }

        if (printOptionErrMsgs(errors)) {
            Assert.fail("test_jira_xmlbeans184() : Errors found when executing scomp ");
        }

    }


}
