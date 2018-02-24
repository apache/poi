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
package compile.scomp.common;

import junit.framework.Assert;
import org.apache.xmlbeans.impl.tool.SchemaCompiler;
import org.apache.xmlbeans.impl.tool.Diff;
import org.apache.xmlbeans.impl.tool.SchemaCodeGenerator;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.xb.xsdschema.SchemaDocument;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;

/**
 * TODO: modify for deprecation warnings
 */
public class CompileTestBase extends CompileCommon {

    public static String outputDir = "compile" + P + "scomp" + P + "incr";
    public static String outPath = "compile" + P + "scomp" + P + "incr" + P + "out";

    public static String sanityPath = "compile" + P + "scomp" + P + "incr"+ P + "sanity";
    public static String incrPath = "compile" + P + "scomp" + P + "incr" + P + "outincr";


    public SchemaTypeSystem builtin = XmlBeans.getBuiltinTypeSystem();
    public File out;
    public File outincr;
    public File sanity;
    public List errors;
    public XmlOptions xm;

    //schemas to use
    public String forXsd = "<xs:schema attributeFormDefault=\"unqualified\" " +
            "elementFormDefault=\"qualified\" " +
            "targetNamespace=\"http://baz\" " +
            "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"> " +
            "<xs:element name=\"elName\" type=\"bas:aType\" " +
            "xmlns:bas=\"http://baz\"/> <xs:complexType name=\"aType\"> " +
            "<xs:simpleContent> " +
            "<xs:extension base=\"xs:string\" " +
            "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"> " +
            "<xs:attribute type=\"xs:string\" name=\"attrName\"/> " +
            "</xs:extension> " +
            "</xs:simpleContent> " +
            "</xs:complexType> " +
            "</xs:schema>";
    public String incrXsd = "<xs:schema attributeFormDefault=\"unqualified\" " +
            "elementFormDefault=\"qualified\" " +
            "targetNamespace=\"http://bar\" " +
            "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"> " +
            "<xs:element name=\"elName\" type=\"bas:aType\" " +
            "xmlns:bas=\"http://baz\"/> <xs:complexType name=\"aType\"> " +
            "<xs:simpleContent> " +
            "<xs:extension base=\"xs:string\" " +
            "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"> " +
            "<xs:attribute type=\"xs:string\" name=\"attrName\"/> " +
            "</xs:extension> " +
            "</xs:simpleContent> " +
            "</xs:complexType> " +
            "</xs:schema>";
    public String errXsd = "<xs:schema attributeFormDefault=\"unqualified\" " +
            "elementFormDefault=\"qualified\" " +
            "targetNamespace=\"http://bar\" " +
            "xmlns:tnf=\"http://baz\" " +
            "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"> " +
            "<xs:element name=\"elErrName\" type=\"tnf:bType\" /> " +
            "</xs:schema>";

    public CompileTestBase(String name) {
        super(name);
        out =  xbeanOutput(outPath);
        sanity =  xbeanOutput(sanityPath);
        outincr =  xbeanOutput(incrPath);

        errors = new ArrayList();
        xm = new XmlOptions();
        xm.setErrorListener(errors);
        xm.setSavePrettyPrint();
    }

    public static File[] getClassPath()
    {
        String cp = System.getProperty("java.class.path");
        String[] cpList = cp.split(File.pathSeparator);
        File[] fList = new File[cpList.length];

        for (int i = 0; i < cpList.length; i++) {
            fList[i] = new File(cpList[i]);
        }
        return fList;
    }

    protected SchemaCompiler.Parameters getCompilerParams() {
        SchemaCompiler.Parameters params = new SchemaCompiler.Parameters();
        params.setDownload(true);
        params.setVerbose(true);
        return params;
    }


    protected SchemaCompiler.Parameters getIncrCompilerParams() {
        SchemaCompiler.Parameters params = getCompilerParams();
        params.setIncrementalSrcGen(true);
        return params;
    }

    protected boolean runCompiler(File[] schemas, String srcDir,
                                  String classesDir, String outputDir,
                                  SchemaCompiler.Parameters params) {

        File srcdir = xbeanOutput(srcDir);
        File classesdir = xbeanOutput(classesDir);
        File outputjar =  xbeanOutput(outputDir);
        params.setXsdFiles(schemas);
        params.setSrcDir(srcdir);
        params.setClassesDir(classesdir);
        params.setOutputJar(outputjar);
        return SchemaCompiler.compile(params);
    }

    public void log(SchemaTypeSystem sts) {
        System.out.println("SchemaTypes: " + sts);
    }

    public void log(Object[] arr) {
        for (int i = 0; i < arr.length; i++)
            System.out.print(arr[i].toString());
    }


    /**
     * compares type systems and populates error list based on differences in files
     *
     * @param outDir
     * @param outIncrDir
     * @param errors
     */
    public void compareandPopErrors(File outDir, File outIncrDir, List errors) {
        // Compare the results
        String oldPropValue = System.getProperty("xmlbeans.diff.diffIndex");
        System.setProperty("xmlbeans.diff.diffIndex", "false");
        errors.clear();
        Diff.dirsAsTypeSystems(outDir, outIncrDir, errors);
        System.setProperty("xmlbeans.diff.diffIndex", oldPropValue == null ? "true" : oldPropValue);

    }

    /**
     * compares type systems and populates error list based on
     * default out and outincr directories
     *
     * @param errors
     */
    public void compareandPopErrors(List errors) {
        // Compare the results
        String oldPropValue = System.getProperty("xmlbeans.diff.diffIndex");
        System.setProperty("xmlbeans.diff.diffIndex", "false");
        errors.clear();
        Diff.dirsAsTypeSystems(out, outincr, errors);
        System.setProperty("xmlbeans.diff.diffIndex", oldPropValue == null ? "true" : oldPropValue);

    }


    /**
     * take the type system that gets created in compileSchemas()
     */
    public SchemaTypeSystem incrCompileXsd(SchemaTypeSystem system, XmlObject[] schemas,
                                           SchemaTypeLoader typepath, XmlOptions options) throws XmlException, IOException {

        SchemaTypeSystem sts;
        sts = XmlBeans.compileXsd(system, schemas, builtin, options);
        Assert.assertNotNull("Compilation failed during Incremental Compile.", sts);
        SchemaCodeGenerator.saveTypeSystem(sts, outincr, null, null, null);
        return sts;

    }

    /**
     * Original Compilation to directory specified
     */
    public SchemaTypeSystem compileSchemas(XmlObject[] schemas, SchemaTypeLoader typepath,
                                           XmlOptions options, File outDir) throws XmlException, IOException {
        SchemaTypeSystem system;
        SchemaTypeSystem builtin = XmlBeans.getBuiltinTypeSystem();
        system = XmlBeans.compileXsd(schemas, builtin, options);
        Assert.assertNotNull("Compilation failed during compile.", system);
        SchemaCodeGenerator.saveTypeSystem(system, outDir, null, null, null);
        return system;
    }


    //original compile to get base type system
    public SchemaTypeSystem compileSchemas(XmlObject[] schemas, SchemaTypeLoader typepath,
                                           XmlOptions options) throws XmlException, IOException {
        SchemaTypeSystem system;
        SchemaTypeSystem builtin = XmlBeans.getBuiltinTypeSystem();
        system = XmlBeans.compileXsd(schemas, builtin, options);
        Assert.assertNotNull("Compilation failed during compile.", system);
        SchemaCodeGenerator.saveTypeSystem(system, out, null, null, null);
        return system;
    }

    public void handleErrors(List errors) {
        if (errors.size() > 0) {
            StringWriter message = new StringWriter();
            for (int i = 0; i < errors.size(); i++)
                message.write(((String) errors.get(i)) + "\n");
            Assert.fail("\nDifferences encountered:\n" + message);
        }
    }

    public String getBaseSchema(String namespace, String elTypeName,
                                String elType, String attrTypeName, String attrType) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<xs:schema " +
                "attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
                "targetNamespace=\"http://" + namespace + "\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
                "<xs:element name=\"" + elTypeName + "\" type=\"bas:aType\" xmlns:bas=\"http://" + namespace + "\" />" +
                "<xs:complexType name=\"aType\">" +
                "<xs:simpleContent>" +
                "<xs:extension base=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >" +
                "<xs:attribute type=\"xs:" + attrType + "\" name=\"" + attrTypeName + "\" />" +
                "</xs:extension>" +
                "</xs:simpleContent>" +
                "</xs:complexType>" +
                "</xs:schema>";
    }

    public SchemaDocument.Schema[] getSchema(XmlObject schemaObj) throws XmlException {
        SchemaDocument.Schema[] schemas = new SchemaDocument.Schema[1];
        schemas[0] = SchemaDocument.Factory.parse(schemaObj.xmlText()).getSchema();
        return schemas;
    }

    public SchemaDocument.Schema[] getSchema(XmlObject[] schemaObj) throws XmlException {
        SchemaDocument.Schema[] schemas = new SchemaDocument.Schema[schemaObj.length];

        for (int i = 0; i < schemaObj.length; i++)
            schemas[i] = SchemaDocument.Factory.parse(schemaObj[i].xmlText()).getSchema();

        return schemas;
    }

    public void echoSts(SchemaTypeSystem base, SchemaTypeSystem incr) {
        System.out.println("-= Base =-");
        log(base, "base");
        System.out.println("-= Incr =-");
        log(incr, "incr");
    }

    public void log(SchemaTypeSystem base, String msg) {
        for (int i = 0; i < base.globalTypes().length; i++) {
            System.out.println(msg + " [" + i + "]-" + base.globalTypes()[i].getName());
        }
    }

    public void findElementbyQName(SchemaTypeSystem sts, QName[] lookup) throws Exception {

        for (int i = 0; i < lookup.length; i++) {
            if (sts.findElement(lookup[i]) == null)
                throw new Exception("Element was expected but not found\n" + lookup[i]);
        }
    }

    public String getSchemaTop(String tns) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<xs:schema " +
                "attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" " +
                "targetNamespace=\"http://" + tns + "\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" >";
    }

    public String getSchemaBottom() {
        return "</xs:schema>";
    }

    public void printSTS(SchemaTypeSystem sts)
    {
        System.out.println("*******************");
        System.out.println("NAME: " + sts.getName());
        SchemaGlobalElement[] sge = sts.globalElements();
        for (int i = 0; i < sge.length; i++) {
            System.out.println("Global Type: " + sge[i].getType());
            System.out.println("GE Name: " + sge[i].getName());
            System.out.println("GE Class: " + sge[i].getType());
            System.out.println("GE SourceName: " + sge[i].getSourceName());
        }

        SchemaType[] st = sts.globalTypes();
        for (int i = 0; i < st.length; i++) {
            System.out.println("Type Name: " + st[i].getDocumentElementName());
            System.out.println("GT Name: " + st[i].getName());
            System.out.println("GT Class: " + st[i].getJavaClass());
            System.out.println("GT SourceName: " + st[i].getSourceName());
        }

        System.out.println("*******************");
    }

    /**
     *
     * @param sge
     * @param q
     * @return
     */
    public boolean findGlobalElement(SchemaGlobalElement[] sge, QName q)
    {
        boolean sts1TypePresent = false;
        for (int i = 0; i < sge.length; i++) {
            if (sge[i].getName().getLocalPart().compareTo(
                    q.getLocalPart()) == 0 &&
                    sge[i].getName().getNamespaceURI().compareTo(
                            q.getNamespaceURI()) == 0)  {
                sts1TypePresent = true;
                break;
            }
                System.out.println("QName: " + sge[i].getName());
        }

        return sts1TypePresent;
    }

    public void checkPerf(long initBase, long endBase,
                          long initIncr, long endIncr) throws Exception {
        long initTime = endBase - initBase;
        long incrTime = endIncr - initIncr;
        long diffTime = initTime - incrTime;
        System.out.println("Initial Compile Time: " + initTime);
        //assert initbase.compareTo(endbase) > 0;
        System.out.println("Incremental Compile Time" + incrTime);
        //assert incrbase.compareTo(endincr) > 0;
        System.out.println("Perf Time: " + diffTime);
        if (!(diffTime > 0))
            throw new Exception("InitTime: "+initTime+"\n" +
                                "IncrTime: "+incrTime+"\n" +
                                "Perf Time Increased: " + diffTime);
        //assert incrbase.compareTo(endincr) > 0;
    }

    public static String[] invalidSchemas = {
        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n" +
            "  <xs:complexType name='base' final='extension'/>\n" +
            "  <xs:complexType name='ext'>\n" +
            "    <xs:complexContent>\n" +
            "      <xs:extension base='base'/>\n" +
            "    </xs:complexContent>\n" +
            "  </xs:complexType>\n" +
            "</xs:schema>\n",

        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n" +
            "  <xs:complexType name='base' final='#all'/>\n" +
            "  <xs:complexType name='ext'>\n" +
            "    <xs:complexContent>\n" +
            "      <xs:extension base='base'/>\n" +
            "    </xs:complexContent>\n" +
            "  </xs:complexType>\n" +
            "</xs:schema>\n",

        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' finalDefault='#all'>\n" +
            "  <xs:complexType name='base'/>\n" +
            "  <xs:complexType name='rest'>\n" +
            "    <xs:complexContent>\n" +
            "      <xs:restriction base='base'/>\n" +
            "    </xs:complexContent>\n" +
            "  </xs:complexType>\n" +
            "</xs:schema>\n",

        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' finalDefault='restriction'>\n" +
            "  <xs:complexType name='base'/>\n" +
            "  <xs:complexType name='rest'>\n" +
            "    <xs:complexContent>\n" +
            "      <xs:restriction base='base'/>\n" +
            "    </xs:complexContent>\n" +
            "  </xs:complexType>\n" +
            "</xs:schema>\n",
    };

    public static String[] validSchemas = {
        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n" +
            "  <xs:complexType name='base' final='extension'/>\n" +
            "  <xs:complexType name='rest'>\n" +
            "    <xs:complexContent>\n" +
            "      <xs:restriction base='base'/>\n" +
            "    </xs:complexContent>\n" +
            "  </xs:complexType>\n" +
            "</xs:schema>\n",

        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n" +
            "  <xs:complexType name='base' final='restriction'/>\n" +
            "  <xs:complexType name='ext'>\n" +
            "    <xs:complexContent>\n" +
            "      <xs:extension base='base'/>\n" +
            "    </xs:complexContent>\n" +
            "  </xs:complexType>\n" +
            "</xs:schema>\n",

        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' finalDefault='restriction'>\n" +
            "  <xs:complexType name='base'/>\n" +
            "  <xs:complexType name='ext'>\n" +
            "    <xs:complexContent>\n" +
            "      <xs:extension base='base'/>\n" +
            "    </xs:complexContent>\n" +
            "  </xs:complexType>\n" +
            "</xs:schema>\n",

        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema' finalDefault='extension'>\n" +
            "  <xs:complexType name='base'/>\n" +
            "  <xs:complexType name='rest'>\n" +
            "    <xs:complexContent>\n" +
            "      <xs:restriction base='base'/>\n" +
            "    </xs:complexContent>\n" +
            "  </xs:complexType>\n" +
            "</xs:schema>\n",
    };

}
