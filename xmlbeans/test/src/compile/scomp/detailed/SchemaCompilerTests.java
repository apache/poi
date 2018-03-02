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
package compile.scomp.detailed;

import junit.framework.TestCase;

import java.util.List;
import java.util.ArrayList;
import java.io.File;

import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import common.Common;

/**
 * This class contains tests that need to invoke the SchemaCompiler class which is
 * equivalent to using scomp from the command line and capture errors
 *
 * The tests need to be run with cmd line param that points to the test case root directory and xmlbeans root
 * ex: -Dcases.location=D:\svnnew\xmlbeans\trunk\test\cases -Dxbean.rootdir=D:\svnnew\xmlbeans\trunk
 */
public class SchemaCompilerTests  extends Common
{
    public static String scompTestFilesRoot = XBEAN_CASE_ROOT + P + "compile" + P + "scomp" + P + "schemacompiler" + P;
    public static String schemaCompOutputDirPath = OUTPUTROOT + P + "compile" + P + "scomp" + P;

    public  SchemaCompilerTests(String name){
        super(name);
    }

    private void _testCompile(File[] xsdFiles,
                              String outputDirName,
                              String testName)
    {
        List errors = new ArrayList();
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(xsdFiles);
        params.setErrorListener(errors);
        params.setSrcDir(new File(schemaCompOutputDirPath + outputDirName + P + "src"));
        params.setClassesDir(new File(schemaCompOutputDirPath + outputDirName + P + "classes"));
        SchemaCompiler.compile(params);
        if (printOptionErrMsgs(errors))
        {
            fail(testName + "(): failure when executing scomp");
        }
    }

    public void testUnionRedefine()
    {
        File[] xsdFiles =
            new File[] { new File(scompTestFilesRoot + "union_initial.xsd"), 
                         new File(scompTestFilesRoot + "union_redefine.xsd") };
        String outputDirName = "unionred";
        String testname = "testUnionRedefine";
        _testCompile(xsdFiles, outputDirName, testname);
    }

    /** This tests a bug where compilation of a schema redefining a type
        involving an enumeration fails.
     */
    public void testEnumerationRedefine1()
    {
        File[] xsdFiles = 
            new File[] { new File(scompTestFilesRoot + "enum1.xsd_"),
                         new File(scompTestFilesRoot + "enum1_redefine.xsd_") };
        String outputDirName = "enumRedef1";
        String testname = "testEnumerationRedefine1";
        _testCompile(xsdFiles, outputDirName, testname);
    }

    /** This tests a bug where compilation of a schema redefining a type
        involving an enumeration fails.
     */
    public void testEnumerationRedefine2()
    {
        File[] xsdFiles = 
            new File[] { new File(scompTestFilesRoot + "enum2.xsd_"),
                         new File(scompTestFilesRoot + "enum2_redefine.xsd_") };
        String outputDirName = "enumRedef2";
        String testname = "testEnumerationRedefine2";
        _testCompile(xsdFiles, outputDirName, testname);
    }

    /** This tests a bug where compilation of a schema redefining a type
        involving an enumeration fails.
     */
    public void testEnumerationRedefine3()
    {
        File[] xsdFiles = 
            new File[] { new File(scompTestFilesRoot + "enum1.xsd_"),
                         new File(scompTestFilesRoot + "enum3.xsd_"),
                         new File(scompTestFilesRoot + "enum3_redefine.xsd_") };
        String outputDirName = "enumRedef3";
        String testname = "testEnumerationRedefine3";
        _testCompile(xsdFiles, outputDirName, testname);
    }

    /**
     * [XMLBEANS-205]:
     * using static handlers for extension interfaces with same method names
     */
    public void testExtensionHandlerMethodNameCollision()
    {
        File[] xsdFiles =
            new File[] { new File(scompTestFilesRoot + "methodsColide_jira205_278.xsd_") };
        File[] configFiles =
            new File[] { new File(scompTestFilesRoot + "methodsColide_jira205_278.xsdconfig_") };
        File[] javaFiles =
            new File[] { new File(scompTestFilesRoot + "ext" + P + "I1.java"),
                         new File(scompTestFilesRoot + "ext" + P + "H1.java"),
                         new File(scompTestFilesRoot + "ext" + P + "I2.java"),
                         new File(scompTestFilesRoot + "ext" + P + "H2.java")};
        String outputDirName = "methodsColide_jira205";

        List errors = new ArrayList();
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(xsdFiles);
        params.setConfigFiles(configFiles);
        params.setJavaFiles(javaFiles);
        params.setErrorListener(errors);
        params.setSrcDir(new File(schemaCompOutputDirPath + outputDirName + P + "src"));
        params.setClassesDir(new File(schemaCompOutputDirPath + outputDirName + P + "classes"));

        SchemaCompiler.compile(params);
        if (printOptionErrMsgs(errors))
        {
            fail("testExtensionHandlerMethodNameCollision(): failure when executing scomp");
        }
    }

    /**
     * [XMLBEANS-278]:
     * -noext flag for compilation
     */
    public void testNoExt()
    {
        File[] xsdFiles =
            new File[] { new File(scompTestFilesRoot + "methodsColide_jira205_278.xsd_") };
        File[] configFiles =
            new File[] { new File(scompTestFilesRoot + "methodsColide_jira205_278.xsdconfig_") };
        String outputDirName = "noExt";

        List errors = new ArrayList();
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setXsdFiles(xsdFiles);
        params.setConfigFiles(configFiles);
        // no java files, if noExt flag doesn't work should fail for not finding the java files  params.setJavaFiles(javaFiles);
        params.setErrorListener(errors);
        params.setSrcDir(new File(schemaCompOutputDirPath + outputDirName + P + "src"));
        params.setClassesDir(new File(schemaCompOutputDirPath + outputDirName + P + "classes"));

        // no extensions
        params.setNoExt(true);

        SchemaCompiler.compile(params);
        if (printOptionErrMsgs(errors))
        {
            fail("testNoExt(): failure when executing scomp");
        }
    }
}
