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
import org.apache.xmlbeans.*;

import java.io.File;
import java.io.StringWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import tools.util.TestRunUtil;
import compile.scomp.common.CompileCommon;
import compile.scomp.common.CompileTestBase;

import javax.xml.namespace.QName;


/**
 *
 *
 */
public class ModelGroupTests extends CompileTestBase {


    public ModelGroupTests(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(ModelGroupTests.class);
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

    public void test_model_diffns_choice2seqchange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getSchemaTop("baz") +
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:choice>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:choice>" +
                "</xs:complexType>" + getSchemaBottom());
        XmlObject obj2 = XmlObject.Factory.parse(getSchemaTop("baz") +
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:sequence>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:sequence>" +
                "</xs:complexType>" + getSchemaBottom());
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

    public void test_model_seq2choicechange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getSchemaTop("baz") +
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:sequence>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:sequence>" +
                "</xs:complexType>" + getSchemaBottom());
        XmlObject obj2 = XmlObject.Factory.parse(getSchemaTop("baz") +
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:choice>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:choice>" +
                "</xs:complexType>" + getSchemaBottom());
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

    public void test_model_seq2choicechange_diffns() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getBaseSchema("bar", "elName", "string", "attrName", "string"));
        XmlObject obj2 = XmlObject.Factory.parse(
                getSchemaTop("baz")+
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:sequence>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:sequence>" +
                "</xs:complexType>"+getSchemaBottom());
        XmlObject[] schemas = new XmlObject[]{obj1};
        XmlObject[] schemas2 = new XmlObject[]{obj2};
        schemas[0].documentProperties().setSourceName("obj1");
        schemas2[0].documentProperties().setSourceName("obj2");

        SchemaTypeSystem base = compileSchemas(schemas, builtin, xm);
        SchemaTypeSystem incr = incrCompileXsd(base, schemas2, builtin, xm);

        echoSts(base, incr);
        QName[] baseTypes = new QName[]{new QName("http://bar", "elName")};
        QName[] incrTypes = new QName[]{new QName("http://baz", "elName")};

        findElementbyQName(base, baseTypes);
        findElementbyQName(incr, incrTypes);

        //        if (incr.findElement(incrTypes[0]).getType().g
        Assert.assertNotSame(base, incr);

        compareandPopErrors(out, outincr, errors);
        handleErrors(errors);
    }

    public void test_model_seq2allchange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getSchemaTop("baz") +
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:sequence>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:sequence>" +
                "</xs:complexType>" + getSchemaBottom());
        XmlObject obj2 = XmlObject.Factory.parse(getSchemaTop("baz") +
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:all>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:all>" +
                "</xs:complexType>" + getSchemaBottom());
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

    public void test_model_all2seqchange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getSchemaTop("baz") +
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:all>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:all>" +
                "</xs:complexType>" + getSchemaBottom());
        XmlObject obj2 = XmlObject.Factory.parse(getSchemaTop("baz") +
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:sequence>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:sequence>" +
                "</xs:complexType>" + getSchemaBottom());
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

    public void test_model_all2choicechange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getSchemaTop("baz") +
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:all>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:all>" +
                "</xs:complexType>" + getSchemaBottom());
        XmlObject obj2 = XmlObject.Factory.parse(getSchemaTop("baz") +
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:choice>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:choice>" +
                "</xs:complexType>" + getSchemaBottom());
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

    public void test_model_choice2choicechange() throws Exception {
        //XmlObject.Factory.parse(getBaseSchema("baz","elName", "elType", "attrName","attrType"));
        XmlObject obj1 = XmlObject.Factory.parse(getSchemaTop("baz") +
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:choice>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:choice>" +
                "</xs:complexType>" + getSchemaBottom());
        XmlObject obj2 = XmlObject.Factory.parse(getSchemaTop("baz") +
                "<xs:element name=\"elName\" type=\"bas:aType\" xmlns:bas=\"http://baz\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:all>" +
                "<xs:element name=\"a\" type=\"xs:string\" />" +
                "<xs:element name=\"b\" type=\"xs:string\" />" +
                "</xs:all>" +
                "</xs:complexType>" + getSchemaBottom());
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



}
