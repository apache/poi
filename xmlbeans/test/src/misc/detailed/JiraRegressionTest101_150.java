package misc.detailed;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import misc.common.JiraTestBase;

/**
 *
 */
public class JiraRegressionTest101_150 extends JiraTestBase
{
    public JiraRegressionTest101_150(String name)
    {
        super(name);
    }

    /**
     * [XMLBEANS-103]   XMLBeans - QName thread cache, cause memory leaks
     * @throws Exception
     */
    public void test_jira_xmlbeans102a() throws Exception{
        // set the parameters similar to those in the bug
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(new File[]{new File(JIRA_CASES + "xmlbeans_102.xsd")});
        params.setOutputJar(new File(outputroot+P+"xmlbeans_102.jar"));
        File outputDir = new File(outputroot + P + "xmlbeans_102");
        outputDir.mkdirs();
        params.setClassesDir(outputDir);
        params.setSrcDir(outputDir);
        // compile schema
        SchemaCompiler.compile(params);
        // check for jar - compilation success
        if(!(new File(outputroot + P + "xmlbeans_102.jar").exists()) )
            throw new Exception("Jar File was not found");
        //cleanup
        deltree(outputroot);
    }

    /*
    * [XMLBEANS-102]: scomp - infinite loop during jar for specific xsd and params for netui_config.xsd
    *
    */
    public void test_jira_xmlbeans102b() {
        //Assert.fail("test_jira_xmlbeans102: Infinite loop after completion of parsing" );

        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setOutputJar(new File(schemaCompOutputDirPath + "jira102.jar"));
        params.setClassesDir(schemaCompClassesDir);

        params.setXsdFiles(new File[]{new File(scompTestFilesRoot + "xmlbeans_102_netui-config.xsd_")});
        List errors = new ArrayList();
        params.setErrorListener(errors);
        params.setSrcDir(schemaCompSrcDir);
        params.setClassesDir(schemaCompClassesDir);

        SchemaCompiler.compile(params);
        if (printOptionErrMsgs(errors)) {
            Assert.fail("test_jira_xmlbeans102() : Errors found when executing scomp");
        }

    }

    /**
     * NPE while initializing a type system w/ a type that extends
     * an a complex type from a different type system
     *
     * @throws Exception
     */
    public void test_jira_xmlbeans105() throws Exception {
        //run untyped parse
        XmlObject obj = XmlObject.Factory.parse(new File(JIRA_CASES + "xmlbeans_105.xml"));

        //run Typed Parse
        jira.xmlbeans105.ResourceUnknownFaultDocument rud =
                jira.xmlbeans105.ResourceUnknownFaultDocument.Factory.parse(new File(JIRA_CASES + "xmlbeans_105.xml"));

        // / we know the instance is invalid
        // make sure the error message is what is expected
        rud.validate(xmOpts);
        Assert.assertTrue("More Errors than expected", errorList.size() == 1);
        Assert.assertTrue("Did not recieve the expected error code: " + ((XmlError) errorList.get(0)).getErrorCode(),
                ((XmlError) errorList.get(0)).getErrorCode().compareToIgnoreCase("cvc-complex-type.2.4a") == 0);

    }







}
