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
package compile.scomp.checkin;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemImpl;
import compile.scomp.common.mockobj.TestFiler;
import compile.scomp.common.CompileCommon;
import compile.scomp.common.CompileTestBase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.List;
import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

/**
 *
 *
 */
public class XmlBeansCompCheckinTests extends CompileTestBase
{   public List xm_errors;
    public XmlOptions xm_opts;
    Vector expBinType;
    Vector expSrcType;

    public XmlBeansCompCheckinTests(String name)
    {
        super(name);
        expBinType = new Vector();
        expBinType.add("schemaorg_apache_xmlbeans/system/apiCompile/atypedb57type.xsb");
        expBinType.add("schemaorg_apache_xmlbeans/system/apiCompile/elname429edoctype.xsb");
        expBinType.add("schemaorg_apache_xmlbeans/system/apiCompile/elnameelement.xsb");
        expBinType.add("schemaorg_apache_xmlbeans/system/apiCompile/index.xsb");
        expBinType.add("schemaorg_apache_xmlbeans/element/http_3A_2F_2Fbaz/elName.xsb");
        expBinType.add("schemaorg_apache_xmlbeans/type/http_3A_2F_2Fbaz/aType.xsb");
        expBinType.add("schemaorg_apache_xmlbeans/namespace/http_3A_2F_2Fbaz/xmlns.xsb");
        expBinType.add("schemaorg_apache_xmlbeans/javaname/baz/ElNameDocument.xsb");
        expBinType.add("schemaorg_apache_xmlbeans/javaname/baz/AType.xsb");
        expBinType.add("schemaorg_apache_xmlbeans/system/apiCompile/TypeSystemHolder.class");

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

    public void test_Filer_compilation() throws Exception
    {
        XmlObject obj1 = XmlObject.Factory.parse(forXsd);
        XmlObject[] schemas = new XmlObject[]{obj1};

        TestFiler f = new TestFiler();//FilerImpl(fClass, fSrc, repackage, true, false);
        SchemaTypeSystem apiSts = XmlBeans.compileXmlBeans("apiCompile", null,
                schemas, null, XmlBeans.getBuiltinTypeSystem(), f, xm_opts);


        for (int i = 0; i < apiSts.globalElements().length; i++) {
            System.out.println("El-" + apiSts.globalElements()[i].getName());
        }
        for (int i = 0; i < apiSts.globalTypes().length; i++) {
            System.out.println("Type-" + apiSts.globalTypes()[i].getBaseType());
        }

        //apiSts = XmlBeans.compileXmlBeans("apiCompile", null,
        //        schemas, null, XmlBeans.getBuiltinTypeSystem(), f, xm_opts);

        if (!f.isCreateBinaryFile())
            throw new Exception("Binary File method not invoked");
        if (!f.isCreateSourceFile())
            throw new Exception("Source File method not invoked");

        System.out.println("BIN");
        CompileCommon.comparefNameVectors(f.getBinFileVec(), expBinType);
        System.out.println("SRC");
        CompileCommon.comparefNameVectors(f.getSrcFileVec(), expSrcType);

    }

    /**
     * Verify Partial SOM cannot be saved to file system
     *
     * @throws Exception
     */
    public void test_sts_noSave() throws Exception
    {
        XmlObject obj1 = XmlObject.Factory.parse(forXsd);
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject obj2 = XmlObject.Factory.parse(incrXsd);
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        XmlObject obj3 = XmlObject.Factory.parse(errXsd);
        XmlObject[] schemas3 = new XmlObject[]{obj3};

        SchemaTypeSystem sts;
        TestFiler f = new TestFiler();
        ArrayList err = new ArrayList();
        XmlOptions opt = new XmlOptions().setErrorListener(err);
        opt.put("COMPILE_PARTIAL_TYPESYSTEM");

        try {
            // since you can't save a partial SOM, don't bother passing in a Filer
            sts = XmlBeans.compileXmlBeans(null,
                    null, schemas3, null,
                    XmlBeans.getBuiltinTypeSystem(), null, opt);
            boolean psom_expError = false;
            // print out the recovered xm_errors
            if (!err.isEmpty()) {
                System.out.println("Schema invalid: partial schema type system recovered");
                for (Iterator i = err.iterator(); i.hasNext();) {
                    XmlError xErr = (XmlError) i.next();
                    System.out.println(xErr);

                    if ((xErr.getErrorCode().compareTo("src-resolve") == 0) &&
                            (xErr.getMessage().compareTo("type 'bType@http://baz' not found.") ==
                            0))
                        psom_expError = true;
                }
            }
            if (!psom_expError)
                throw new Exception("Error Code was not as Expected");

        } catch (XmlException e) {
            //The above case should be recoverable so
            // spit out debug statement and throw the error
            System.out.println("Schema invalid: couldn't recover from xm_errors");
            if (err.isEmpty())
                System.err.println(e.getMessage());
            else
                for (Iterator i = err.iterator(); i.hasNext();)
                    System.err.println(i.next());
            throw e;
        }

        Assert.assertTrue("Expected partial schema type system", ((SchemaTypeSystemImpl)sts).isIncomplete());


        //call some stupid methods on STS
        printSTS(sts);

        // Check using saveToDirectory on Partial SOM
        File tempDir = null;
        try {
            //setUp outputDirectory
            tempDir = new File(OUTPUTROOT, "psom_save");
            tempDir.mkdirs();
            tempDir.deleteOnExit();
            Assert.assertTrue("Output Directory Init needed to be empty",
                    tempDir.listFiles().length == 0);

            //This should not Work
            sts.saveToDirectory(tempDir);
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // ok
            System.out.println("sts.saveToDirectory() threw IllegalStateException as expected");
        }

        //make sure nothing was written
        Assert.assertTrue("Partial SOM output dir needed to be empty",
            tempDir.listFiles().length == 0);

        // Check using save(Filer) on Partial SOM
        TestFiler tf = null;
        try {
            //setUp outputDirectory
            tf = new TestFiler();
            Assert.assertTrue("Filer Source should have been size 0",
                    tf.getBinFileVec().size() == 0);

            //This should not Work
            sts.save(tf);
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // ok
            System.out.println("sts.save() threw IllegalStateException as expected");
        }

        //make sure nothing was written
        Assert.assertTrue("Filer -Bin- Partial SOM " +
            "output dir needed to be empty",
            tf.getBinFileVec().size() == 0);
        Assert.assertTrue("Filer -SRC- Partial SOM " +
            "output dir needed to be empty",
            tf.getSrcFileVec().size() == 0);

        Assert.assertFalse("Filer Create Source File " +
            "method should not have been invoked",
            tf.isCreateSourceFile());

        Assert.assertFalse("Filer Create Binary File " +
            "method should not have been invoked",
            tf.isCreateBinaryFile());

        // Check using filer in partial SOM compilation
        try {
            tf = new TestFiler();

            Assert.assertTrue("Filer Source should have been size 0",
                    tf.getBinFileVec().size() == 0);

            //reset data
            sts = null;
            err.clear();

            //filer methods on partial SOM should not be returned
            sts = XmlBeans.compileXmlBeans(null,
                    null, schemas3, null,
                    XmlBeans.getBuiltinTypeSystem(), tf, opt);

            Assert.assertTrue("Errors was not empty", !err.isEmpty());
            //make sure nothing was written
            Assert.assertTrue("Filer -Bin- Partial SOM " +
                    "output dir needed to be empty",
                    tf.getBinFileVec().size() == 0);
            Assert.assertTrue("Filer -SRC- Partial SOM " +
                    "output dir needed to be empty",
                    tf.getSrcFileVec().size() == 0);

            Assert.assertFalse("Filer Create Source File " +
                    "method should not have been invoked",
                    tf.isCreateSourceFile());

            Assert.assertFalse("Filer Create Binary File " +
                    "method should not have been invoked",
                    tf.isCreateBinaryFile());
        } catch (Exception e) {
            throw e;
        }


        System.out.println("Save Verification passed");

    }

    /**
     * ensure that entry point properly handles
     * different configs with null values
     */
    public void test_entrypoint_nullVals() throws Exception
    {
        XmlObject obj1 = XmlObject.Factory.parse(forXsd);
        XmlObject[] schemas = new XmlObject[]{obj1};

        TestFiler f = new TestFiler();

        SchemaTypeSystem apiSts = XmlBeans.compileXmlBeans(null, null,
                schemas, null, XmlBeans.getBuiltinTypeSystem(), null, null);
        System.out.println("Name: " + apiSts.getName());
        printSTS(apiSts);

        apiSts = XmlBeans.compileXmlBeans(null, null,
                null, null, XmlBeans.getBuiltinTypeSystem(), null, null);
        printSTS(apiSts);

        boolean iArgExThrown = false;
        try {
            apiSts = XmlBeans.compileXmlBeans(null, null,
                    null, null, null, null, null);
            printSTS(apiSts);

        } catch (IllegalArgumentException iaEx) {
            iArgExThrown = true;
            System.err.println(iaEx.getMessage());
        }

        // svn revision 160341. SchemaTypeLoader is not expected to non null any more. All params can be null
        assertFalse(iArgExThrown);

        iArgExThrown = false;
        try {
            apiSts = XmlBeans.compileXmlBeans(null, null,
                    schemas, null, null, null, null);
            printSTS(apiSts);
        } catch (IllegalArgumentException iaEx) {
            iArgExThrown = true;
            System.err.println(iaEx.getMessage());
        }

        // svn revision 160341. SchemaTypeLoader is not expected to non null any more
        assertFalse(iArgExThrown);

    }

}
