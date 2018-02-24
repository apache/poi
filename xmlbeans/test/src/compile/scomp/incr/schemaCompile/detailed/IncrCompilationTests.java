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

package compile.scomp.incr.schemaCompile.detailed;

import junit.framework.TestCase;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.apache.xmlbeans.impl.tool.CodeGenUtil;
import org.apache.xmlbeans.impl.tool.SchemaCodeGenerator;
import org.apache.xmlbeans.impl.tool.Diff;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;
import org.apache.xmlbeans.impl.xb.xsdschema.TopLevelComplexType;
import org.apache.xmlbeans.impl.util.FilerImpl;
import org.apache.xmlbeans.*;

import java.io.*;
import java.util.*;

//import tools.util.TestRunUtil;
import compile.scomp.common.CompileCommon;
import compile.scomp.common.CompileTestBase;
import compile.scomp.common.mockobj.TestFiler;

import javax.xml.namespace.QName;


/**
 *
 *
 */
public class IncrCompilationTests extends CompileTestBase {


    public IncrCompilationTests(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(IncrCompilationTests.class);
    }

    public void setUp() throws IOException {
        CompileCommon.deltree(CompileCommon.xbeanOutput(outputDir));
        out = CompileCommon.xbeanOutput(outPath);
        sanity = CompileCommon.xbeanOutput(sanityPath);
        outincr = CompileCommon.xbeanOutput(incrPath);

        errors = new ArrayList();
        xm = new XmlOptions();
        xm.setErrorListener(errors);
        xm.setSavePrettyPrint();
    }

    public void tearDown() throws Exception
    {
            super.tearDown();
            if (errors.size() > 0)
            errors.clear();
    }


    public void test_dupetype_diffns() throws Exception {
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getBaseSchema("bar", "elName", "string", "attrName", "string"));
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj2");

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr;
        try {
            incr = incrCompileXsd(base, schemas2, builtin, xm);
        } catch (XmlException xmlEx) {
            throw new Exception(xmlEx.getError().toString());
        }
        if (base.findElement(new QName("http://baz", "elName")) == null)
            throw new Exception("BASE: Baz elName was not found");

        if (incr.findElement(new QName("http://baz", "elName")) == null)
            throw new Exception("INCR: Baz elName was not found");
        for (int i = 0; i < incr.globalElements().length; i++) {
            System.out.println("[" + i + "]-" + incr.globalElements()[i].getName());
        }

        for (int i = 0; i < base.globalElements().length; i++) {
            System.out.println("[" + i + "]-" + base.globalElements()[i].getName());
        }

        if (incr.findElement(new QName("http://bar", "elName")) == null)
            throw new Exception("INCR: Bar elName was not found");

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }


    public void test_dupens_difftypename() throws Exception {
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getBaseSchema("baz", "elName2", "string", "attrName2", "string"));
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj2");

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName"),
                                        new QName("http://baz", "elName2")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }


    /**
     * This test should not change sts since xmlobject is same
     * @throws Exception
     */
    public void test_dupens_dupetypename() throws Exception {
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "string"));
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj2");

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        if(!base.equals(incr))
            throw new Exception("This type system should not have changed");

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    public void test_dupens_attrnamechange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName2", "string"));
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj2");

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        Assert.assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    public void test_dupens_attrtypechange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "int"));
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj2");

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        Assert.assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }


    public void test_dupens_eltypechange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "int", "attrName", "string"));
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj2");

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        //        if (incr.findElement(incrTypes[0]).getType().g
        Assert.assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    public void test_typechange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "string", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(getBaseSchema("baz", "elName", "int", "attrName2", "string"));
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj2");

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://baz", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        Assert.assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    // test regeneration of generated java files by the Filer
    public void test_schemaFilesRegeneration_01() throws Exception {

        // incremental compile with the same file again. There should be no regeneration of src files
        XmlObject obj1 = XmlObject.Factory.parse(schemaFilesRegeneration_schema1);
        XmlObject obj2 = XmlObject.Factory.parse(schemaFilesRegeneration_schema1);

        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};

        // the source name should be set the same for incremental compile
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj1");

        // create a new filer here with the incrCompile flag value set to 'true'
        Filer filer = new FilerImpl(out, out, null, true, true);
        SchemaTypeSystem base = XmlBeans.compileXmlBeans("teststs",null,schemas,null,builtin,filer,xm);
        Assert.assertNotNull("Compilation failed during Incremental Compile.", base);
        base.saveToDirectory(out);

        // get timestamps for first compile
        HashMap initialTimeStamps = new HashMap();
        recordTimeStamps(out, initialTimeStamps);

        // the incr compile - provide the same name for the STS as the initial compile
        // note: providing a null or different name results in regeneration of generated Interface java src files
        HashMap recompileTimeStamps = new HashMap();
        Filer filer2 = new FilerImpl(out, out, null, true, true);
        SchemaTypeSystem incr = XmlBeans.compileXmlBeans("teststs",base,schemas2,null,builtin,filer2,xm);
        Assert.assertNotNull("Compilation failed during Incremental Compile.", incr);
        incr.saveToDirectory(out);
        recordTimeStamps(out, recompileTimeStamps);

        // compare generated source timestamps here
        assertEquals("Number of Files not equal for Incremental Schema Compilation using Filer",initialTimeStamps.size(), recompileTimeStamps.size());
        Set keyset = initialTimeStamps.keySet();
        for (Iterator iterator = keyset.iterator(); iterator.hasNext();) {
            String eachFile = (String) iterator.next();
            assertEquals("Mismatch for File " + eachFile,initialTimeStamps.get(eachFile),recompileTimeStamps.get(eachFile));
        }

        handleErrors(errors);
    }


    public void test_schemaFilesRegeneration_02() throws Exception {
        // incremental compile with the changes. Specific files should be regenerated
        XmlObject obj1 = XmlObject.Factory.parse(schemaFilesRegeneration_schema1);
        XmlObject obj2 = XmlObject.Factory.parse(schemaFilesRegeneration_schema1_modified);

        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};

        // the source name should be set the same for incremental compile
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj1");

        // create a new filer here with the incrCompile flag value set to 'true'
        Filer filer = new FilerImpl(out, out, null, true, true);
        SchemaTypeSystem base = XmlBeans.compileXmlBeans("test",null,schemas,null,builtin,filer,xm);
        Assert.assertNotNull("Compilation failed during Incremental Compile.", base);
        base.saveToDirectory(out);

        // get timestamps for first compile
        HashMap initialTimeStamps = new HashMap();
        recordTimeStamps(out, initialTimeStamps);

        // the incr compile
        HashMap recompileTimeStamps = new HashMap();
        Filer filer2 = new FilerImpl(out, out, null, true, true);
        SchemaTypeSystem incr = XmlBeans.compileXmlBeans("test",base,schemas2,null,builtin,filer2,xm);
        Assert.assertNotNull("Compilation failed during Incremental Compile.", incr);
        incr.saveToDirectory(out);
        recordTimeStamps(out, recompileTimeStamps);

        // compare generated source timestamps here
        assertEquals("Number of Files not equal for Incremental Schema Compilation using Filer",initialTimeStamps.size(), recompileTimeStamps.size());
        Set keyset = initialTimeStamps.keySet();

        // Atype has been modified, BType has been removed
        String modifiedFileName = out.getCanonicalFile() + "\\org\\openuri\\impl\\ATypeImpl.java";
        String modifiedFileName2 = out.getCanonicalFile() + "\\org\\openuri\\AType.java";

        for (Iterator iterator = keyset.iterator(); iterator.hasNext();) {
            String eachFile = (String) iterator.next();

            if(eachFile.equalsIgnoreCase(modifiedFileName)){
                assertNotSame("File Should have been regenerated by Filer but has the same timestamp",initialTimeStamps.get(eachFile),recompileTimeStamps.get(eachFile));
                continue;
            }
            if(eachFile.equalsIgnoreCase(modifiedFileName2)){
                assertNotSame("File Should have been regenerated by Filer but has the same timestamp",initialTimeStamps.get(eachFile),recompileTimeStamps.get(eachFile));
                continue;
            }
            assertEquals("Mismatch for File " + eachFile,initialTimeStamps.get(eachFile),recompileTimeStamps.get(eachFile));
        }

        handleErrors(errors);
    }

    public boolean recordTimeStamps(File inputDir, HashMap timeStampResults) throws Exception
    {
        if (timeStampResults == null){
            return false;
        }

        if(inputDir == null)
            return false;
        if(!inputDir.exists())
            return false;
        if(inputDir.isFile())
        {
            //System.out.println("File:" + inputDir.getCanonicalPath() + "\t:" + inputDir.lastModified());
            return true;
        }

        File[] child  = inputDir.listFiles();
        for(int i=0;i<child.length;i++)
        {
            //System.out.println("Dir :"+ child[i].getCanonicalPath() + "\t:" + child[i].lastModified());
            if(child[i].getName().endsWith(".java")){
                timeStampResults.put(child[i].getCanonicalPath(),new Long(child[i].lastModified()));
            }
            recordTimeStamps(child[i], timeStampResults);
        }

        return true;
    }

    private static String schemaFilesRegeneration_schema1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                    "<xs:schema " +
                    "attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
                    "targetNamespace=\"http://openuri.org\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
                    "<xs:element name=\"TestElement\" type=\"bas:aType\" xmlns:bas=\"http://openuri.org\" />" +
                    "<xs:element name=\"NewTestElement\" type=\"bas:bType\" xmlns:bas=\"http://openuri.org\" />" +
                    "<xs:complexType name=\"aType\">" +
                    "<xs:simpleContent>" +
                    "<xs:extension base=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
                    "<xs:attribute type=\"xs:string\" name=\"stringAttr\" />" +
                    "</xs:extension>" +
                    "</xs:simpleContent>" +
                    "</xs:complexType>" +
                    "<xs:complexType name=\"bType\">" +
                    "<xs:simpleContent>" +
                    "<xs:extension base=\"xs:integer\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
                    "</xs:extension>" +
                    "</xs:simpleContent>" +
                    "</xs:complexType>" +
                    "</xs:schema>";

    private static String schemaFilesRegeneration_schema1_modified = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                    "<xs:schema " +
                    "attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
                    "targetNamespace=\"http://openuri.org\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
                    "<xs:element name=\"TestElement\" type=\"bas:aType\" xmlns:bas=\"http://openuri.org\" />" +
                    "<xs:complexType name=\"aType\">" +
                    "<xs:simpleContent>" +
                    "<xs:extension base=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
                    "<xs:attribute type=\"xs:token\" name=\"tokenAttr\" />" +
                    "</xs:extension>" +
                    "</xs:simpleContent>" +
                    "</xs:complexType>" +
                    "</xs:schema>";


}
