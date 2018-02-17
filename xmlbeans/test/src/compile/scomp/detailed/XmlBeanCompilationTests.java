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

import junit.framework.Assert;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.xb.xmlconfig.ConfigDocument;

import java.io.*;
import java.util.*;

import compile.scomp.common.CompileCommon;
import compile.scomp.common.CompileTestBase;
import compile.scomp.common.mockobj.TestFiler;
import compile.scomp.common.mockobj.TestBindingConfig;

import javax.xml.namespace.QName;



/**
 * Ensure that several compilation mechanisms all generate
 * the same schematypesystem
 *
 */
public class XmlBeanCompilationTests extends CompileTestBase
{
    public List xm_errors;
    public XmlOptions xm_opts;
    Vector expBinType;
    Vector expSrcType;

    public XmlBeanCompilationTests(String name)
    {
        super(name);
        expBinType = new Vector();
        expBinType.add("schema/system/apiCompile/atypedb57type.xsb");
        expBinType.add("schema/system/apiCompile/elname429edoctype.xsb");
        expBinType.add("schema/system/apiCompile/elnameelement.xsb");
        expBinType.add("schema/system/apiCompile/index.xsb");
        expBinType.add("schema/element/http_3A_2F_2Fbaz/elName.xsb");
        expBinType.add("schema/type/http_3A_2F_2Fbaz/aType.xsb");
        expBinType.add("schema/namespace/http_3A_2F_2Fbaz/xmlns.xsb");
        expBinType.add("schema/javaname/baz/ElNameDocument.xsb");
        expBinType.add("schema/javaname/baz/AType.xsb");
        expBinType.add("schema/system/apiCompile/TypeSystemHolder.class");

        expSrcType = new Vector();
        expSrcType.add("baz.AType");
        expSrcType.add("baz.impl.ATypeImpl");
        expSrcType.add("baz.ElNameDocument");
        expSrcType.add("baz.impl.ElNameDocumentImpl");

        xm_errors = new ArrayList();
        xm_opts = new XmlOptions();
        xm_opts.setErrorListener(xm_errors);
        xm_opts.setSavePrettyPrint();

    }


    public void tearDown() throws Exception
    {
        super.tearDown();
        if (xm_errors.size() > 0)
            xm_errors.clear();
    }

    /**
     * Filer != null for BindingConfig to be used
     *
     * @throws Exception
     */
    public void test_bindingconfig_extension_compilation() throws Exception
    {
        TestFiler f = new TestFiler();
        //initialize all of the values
        String extCaseDir = XBEAN_CASE_ROOT + P + "extensions" + P;
        String extSrcDir = CASEROOT + P +
                ".." + P + "src" + P + "xmlobject" + P + "extensions" + P;
        File[] cPath = CompileTestBase.getClassPath();
        String dir = extCaseDir + P + "interfaceFeature" + P + "averageCase";
        String dir2 = extCaseDir + P + "prePostFeature" + P +
                "ValueRestriction";

        ConfigDocument.Config bConf = ConfigDocument.Factory.parse(
                new File(dir + P + "po.xsdconfig"))
                .getConfig();
        ConfigDocument.Config cConf = ConfigDocument.Factory.parse(
                new File(dir2 + P + "company.xsdconfig"))
                .getConfig();

        String simpleConfig = "<xb:config " +
                "xmlns:xb=\"http://xml.apache.org/xmlbeans/2004/02/xbean/config\"\n" +
                " xmlns:ep=\"http://xbean.interface_feature/averageCase/PurchaseOrder\">\n" +
                "<xb:namespace uri=\"http://xbean.interface_feature/averageCase/PurchaseOrder\">\n" +
                "<xb:package>com.easypo</xb:package>\n" +
                "</xb:namespace></xb:config>";
        ConfigDocument.Config confDoc = ConfigDocument.Factory.parse(simpleConfig).getConfig();
        ConfigDocument.Config[] confs = new ConfigDocument.Config[]{bConf, confDoc, cConf};

        String fooHandlerPath = extSrcDir + P + "interfaceFeature" + P +
                "averageCase" + P + "existing" + P + "FooHandler.java";
        String iFooPath = extSrcDir + P + "interfaceFeature" + P +
                "averageCase" + P + "existing" + P + "IFoo.java";
        String iSetterPath = extSrcDir + P + "prePostFeature" + P +
                "ValueRestriction" + P + "existing" + P + "ISetter.java";
        String setterHandlerPath = extSrcDir + P + "prePostFeature" + P +
                "ValueRestriction" + P + "existing" + P + "SetterHandler.java";


        File[] fList = new File[]{new File(fooHandlerPath), new File(iFooPath),
                                  new File(iSetterPath),
                                  new File(setterHandlerPath)};

        //use created BindingConfig
        TestBindingConfig bind = new TestBindingConfig(confs, fList, cPath);

        //set XSDs
        XmlObject obj1 = XmlObject.Factory.parse(new File(dir + P + "po.xsd"));
        XmlObject obj2 = XmlObject.Factory.parse(
                new File(dir2 + P + "company.xsd"));
        XmlObject[] schemas = new XmlObject[]{obj1, obj2};

        //filer must be present on this method
        SchemaTypeSystem apiSts = XmlBeans.compileXmlBeans("apiCompile", null,
                schemas, bind, XmlBeans.getBuiltinTypeSystem(), f, xm_opts);

        if (!bind.isIslookupPrefixForNamespace())
            throw new Exception("isIslookupPrefixForNamespace not invoked");
        if (!bind.isIslookupPackageForNamespace())
            throw new Exception("isIslookupPackageForNamespace not invoked");
        if (!bind.isIslookupSuffixForNamespace())
            throw new Exception("isIslookupSuffixForNamespace not invoked");
        if (!bind.isIslookupJavanameForQName())
            throw new Exception("isIslookupJavanameForQName not invoked");
        if (!bind.isIsgetInterfaceExtensionsString())
            throw new Exception("isIsgetInterfaceExtensionsString not invoked");
        if (!bind.isIsgetInterfaceExtensions())
            throw new Exception("isIsgetInterfaceExtensions not invoked");
        if (!bind.isIsgetPrePostExtensions())
            throw new Exception("isIsgetPrePostExtensions not invoked");
        if (!bind.isIsgetInterfaceExtensions())
            throw new Exception("isIsgetInterfaceExtensions not invoked");
        if (!bind.isIsgetPrePostExtensionsString())
            throw new Exception("isIsgetPrePostExtensionsString not invoked");
    }

    /**
     * Verify basic incremental compilation
     * and compilation with partial SOM usages
     */
    public void test_incrCompile() throws Exception
    {
        XmlObject obj1 = XmlObject.Factory.parse(forXsd);
        obj1.documentProperties().setSourceName("OBJ1");
        XmlObject[] schemas = new XmlObject[]{obj1};
        QName sts1 = new QName("http://baz", "elName");

        XmlObject obj2 = XmlObject.Factory.parse(incrXsd);
        obj2.documentProperties().setSourceName("OBJ2");
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        QName sts2 = new QName("http://bar", "elName");

        XmlObject obj3 = XmlObject.Factory.parse(errXsd);
        obj3.documentProperties().setSourceName("OBJ3");
        XmlObject[] schemas3 = new XmlObject[]{obj3};
        QName sts3 = new QName("http://bar", "elErrName");

        SchemaTypeSystem sts;
        ArrayList err = new ArrayList();
        XmlOptions opt = new XmlOptions().setErrorListener(err);
        opt.put("COMPILE_PARTIAL_TYPESYSTEM");

        //BASIC COMPILATION
        sts = XmlBeans.compileXmlBeans(null,
                null, schemas, null,
                XmlBeans.getBuiltinTypeSystem(), null, opt);

        Assert.assertTrue("Errors should have been empty", err.isEmpty());
        // find element in the type System
        if (!findGlobalElement(sts.globalElements(), sts1))
            throw new Exception(
                    "Could Not find Type from first Type System: " + sts1);

        //SIMPLE INCR COMPILATION
        sts = XmlBeans.compileXmlBeans(null,
                sts, schemas2, null,
                XmlBeans.getBuiltinTypeSystem(), null, opt);
        Assert.assertTrue("Errors should have been empty", err.isEmpty());
        // find element in the type System

        if (!findGlobalElement(sts.globalElements(), sts1))
            throw new Exception("Could Not find Type from first Type System: " +
                    sts1);

        if (!findGlobalElement(sts.globalElements(), sts2))
            throw new Exception("Could Not find Type from 2nd Type System: " +
                    sts2);

        System.out.println("Building over Existing");
        //BUILDING OFF BASE SIMPLE INCR COMPILATION
        sts = XmlBeans.compileXmlBeans(null,
                sts, schemas2, null,
                sts, null, opt);
        Assert.assertTrue("Errors should have been empty", err.isEmpty());
        // find element in the type System

        if (!findGlobalElement(sts.globalElements(), sts1))
            throw new Exception("Could Not find Type from first Type System: " +
                    sts1);

        if (!findGlobalElement(sts.globalElements(), sts2))
            throw new Exception("Could Not find Type from 2nd Type System: " +
                    sts2);

        //INCR COMPILATION WITH RECOVERABLE ERROR
        err.clear();
        SchemaTypeSystem b = XmlBeans.compileXmlBeans(null,
                sts, schemas3, null,
                XmlBeans.getBuiltinTypeSystem(), null, opt);
        // find element in the type System
        if (!findGlobalElement(b.globalElements(), sts1))
            throw new Exception("Could Not find Type from first Type System: " +
                    sts1);

        if (!findGlobalElement(b.globalElements(), sts2))
            throw new Exception("Could Not find Type from 2nd Type System: " +
                    sts2);

        if (!findGlobalElement(b.globalElements(), sts3))
            throw new Exception("Could Not find Type from 3rd Type System: " +
                    sts3);

        printSTS(b);

        //INSPECT ERRORS
        boolean psom_expError = false;
        // print out the recovered xm_errors
        if (!err.isEmpty()) {
            System.out.println(
                    "Schema invalid: partial schema type system recovered");
            for (Iterator i = err.iterator(); i.hasNext();) {
                XmlError xErr = (XmlError) i.next();
                System.out.println(xErr);
                //compare to the expected xm_errors
                if ((xErr.getErrorCode().compareTo("src-resolve") == 0) &&
                        (xErr.getMessage().compareTo(
                                "type 'bType@http://baz' not found.") ==
                        0))
                    psom_expError = true;
            }
        }
        if (!psom_expError)
            throw new Exception("Error Code was not as Expected");


    }






    /*public void test_diff_compilationMethods() throws IOException,
    XmlException, Exception
    {


    //initialize the schema compiler
    SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
    params.setXsdFiles(new File[]{scompFile});
    params.setSrcDir(scompDirFile);
    params.setClassesDir(scompDirFile);

    //save out schema for use in scomp later
    XmlObject obj1 = XmlObject.Factory.parse(forXsd);
    obj1.save(scompFile);

    //scomp saved out schema
    SchemaCompiler.compile(params);

    //use new api to get typesystem
    XmlObject[] schemas = new XmlObject[]{obj1};
    SchemaTypeSystem apiSts = XmlBeans.compileXmlBeans("apiCompile", null,
    schemas, null, XmlBeans.getBuiltinTypeSystem(), null, xm_opts);

    //use alternative api to get typesystem
    SchemaTypeSystem altSts = XmlBeans.compileXsd(schemas,
    XmlBeans.getBuiltinTypeSystem(), null);

    //save out sts for diff later
    SchemaCodeGenerator.saveTypeSystem(apiSts, apiDirFile, null, null,
    null);
    SchemaCodeGenerator.saveTypeSystem(altSts, baseDirFile, null, null,
    null);

    //diff new api to old api
    xm_errors = null;
    xm_errors = new ArrayList();
    Diff.dirsAsTypeSystems(apiDirFile, baseDirFile, xm_errors);
    if (xm_errors.size() >= 1)
    throw new Exception("API STS ERRORS: " + xm_errors.toString());

    //diff scomp sts to new api
    xm_errors = null;
    xm_errors = new ArrayList();
    Diff.dirsAsTypeSystems(apiDirFile, scompDirFile, xm_errors);
    if (xm_errors.size() >= 1)
    throw new Exception("API SCOMP ERRORS: " + xm_errors.toString());
    }  */


}
