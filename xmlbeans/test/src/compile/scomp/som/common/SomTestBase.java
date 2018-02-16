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
package compile.scomp.som.common;

import compile.scomp.common.CompileCommon;
import compile.scomp.common.CompileTestBase;
import junit.framework.Assert;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.tool.Diff;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 *
 */
public class SomTestBase extends CompileTestBase
{
    public static String casesRootDir = XBEAN_CASE_ROOT+ P + "compile" + P + "som" + P;
    public static String somOutputRootDir = OUTPUTROOT+ P + "som" + P;
    public static long runid;
    public static PrintWriter outlog = null;

    public SchemaTypeSystem builtin;
    public List errors;
    public XmlOptions options;

    public static final String anySimpleType = "anySimpleType";
    public static final String anyType = "anyType";

    public SomTestBase(String name)
    {
        super(name);
    }

    public static void inspectSOM(SchemaTypeSystem schematypesys,
                                  int expectedGlobalElems,
                                  int expectedGlobalAttrs,
                                  int expectedGlobalTypes,
                                  int expectedAttrGroups)
    {
        // System.outs written to a log file in the build\test\output\som directory, one file per run
        // ex. SOM_INSPECTION_RESULT_1107129259405.txt

        File outDir = new File(somOutputRootDir);
        if (!outDir.exists())
        {
            outDir.mkdir();
        }

        // check if file exists already
        String logFileName = somOutputRootDir + P + "SOM_INSPECTION_RESULT_" + runid + ".txt";
        File outfile = new File(logFileName);
        PrintWriter out = null;

        try
        {
            // if file exists for this run, append to it
            if (outfile.exists())
            {
                out = new PrintWriter(new FileWriter(outfile, true));
            }
            else
            {
                if (outfile.createNewFile())
                {
                    out = new PrintWriter(new FileWriter(outfile));
                }
            }

            out.println("\n Call to inspectPSOM .. .. .. ..");
            out.println("\n\n =======================================================");
            out.println("Now Inspecting SOM for STS : " + schematypesys.getName());
            out.println("=======================================================");
            out.println("Input Params : #elems (" + expectedGlobalElems + "), #attr (" + expectedGlobalAttrs
                    + "), #types (" + expectedGlobalTypes + "), #attr groups (" + expectedAttrGroups + ")");
            out.println("-------------------------------------------------------");

            out.println("New STUFF -------------------------------------------------------");
            schematypesys.resolve();
            if (schematypesys.isNamespaceDefined("TestNameSpace"))
            {
                out.println("Name Space 'TestNameSpace' for this STS is define ..");
            }
            else
            {
                out.println("No Name Space 'TestNameSpace' for this STS is NOT ndefine ..");
            }
            out.println("End New STUFF -------------------------------------------------------");

            // walk thro the SOM here
            out.println("----- Loader Name      :" + schematypesys.getName());

            // # of global attributes
            out.println("----- # Global Attributes :" + schematypesys.globalAttributes().length);
            Assert.assertEquals("Incorrect Number of Global Attributes in STS " + schematypesys.getName(), expectedGlobalAttrs, schematypesys.globalAttributes().length);
            for (int i = 0; i < schematypesys.globalAttributes().length; i++)
            {
                out.println("\t------> Attr Name  :" + schematypesys.globalAttributes()[i].getName());
                out.println("\t------> Attr Type  :" + schematypesys.globalAttributes()[i].getType());
            }

            // # of global elements
            out.println("----- # Global Elements :" + schematypesys.globalElements().length);
            Assert.assertEquals("Incorrect Number of Global Elements in STS " + schematypesys.getName(), expectedGlobalElems, schematypesys.globalElements().length);
            for (int i = 0; i < schematypesys.globalElements().length; i++)
            {
                out.println("\t------> Elem Name :" + schematypesys.globalElements()[i].getName());
                out.println("\t------> Elem Type :" + schematypesys.globalElements()[i].getType());
            }

            // # of global Types
            out.println("----- # Global Types :" + schematypesys.globalTypes().length);
            Assert.assertEquals("Incorrect Number of Global Types in STS " + schematypesys.getName(), expectedGlobalTypes, schematypesys.globalTypes().length);
            for (int i = 0; i < schematypesys.globalTypes().length; i++)
            {
                out.println("\t------> TypeName:" + schematypesys.globalTypes()[i].getName());
            }

            // # of attribute Groups
            out.println("----- # of Attribute Groups :" + schematypesys.attributeGroups().length);
            Assert.assertEquals("Incorrect Number of Attribute Groups in STS " + schematypesys.getName(), expectedAttrGroups, schematypesys.attributeGroups().length);
            for (int i = 0; i < schematypesys.attributeGroups().length; i++)
            {
                out.println("\t------> Attr Group Name :" + schematypesys.attributeGroups()[i].getName());
                out.println("\t------> Attr STS   :" + schematypesys.attributeGroups()[i].getTypeSystem());
            }

            out.println("----- # of Model Groups :" + schematypesys.modelGroups().length);
            Assert.assertNotNull("Invalid Model Groups Collection returned in STS " + schematypesys.documentTypes());
            for (int i = 0; i < schematypesys.modelGroups().length; i++)
            {
                out.println("\t------> Model Group Name:" + schematypesys.modelGroups()[i].getName());
                out.println("\t------> Model Group STS :" + schematypesys.modelGroups()[i].getTypeSystem());
            }

            out.println("----- # of Schema Annotations :" + schematypesys.annotations().length);
            Assert.assertNotNull("Invalid Annotations Collection returned in STS " + schematypesys.annotations());
            for (int i = 0; i < schematypesys.annotations().length; i++)
            {
                out.println("\t------> Annotation Application Info Array :" + schematypesys.annotations()[i].getApplicationInformation().toString());
                out.println("\t------> Annotation User Info Array :" + schematypesys.annotations()[i].getUserInformation().toString());
            }

            out.println("----- # of Attribute Types :" + schematypesys.attributeTypes().length);
            Assert.assertNotNull("Invalid Attribute Types Collection returned in STS " + schematypesys.attributeTypes());

            for (int i = 0; i < schematypesys.attributeTypes().length; i++)
            {
                out.println("\t------> Attr Type Name :" + schematypesys.attributeTypes()[i].getName());
                out.println("\t------> Attr STS :" + schematypesys.attributeTypes()[i].getTypeSystem());
            }

            out.println("----- # of Document Types :" + schematypesys.documentTypes().length);
            Assert.assertNotNull("Invalid Document Types Collection returned in STS " + schematypesys.documentTypes());
            for (int i = 0; i < schematypesys.documentTypes().length; i++)
            {
                out.println("\t------> Doc Type Name :" + schematypesys.documentTypes()[i].getName());
                out.println("\t------> Doc Type STS  :" + schematypesys.documentTypes()[i].getTypeSystem());
            }

            // walk through the Schema Types of this STS in detail
            out.println("\t=======================================================");
            out.println("\tWalking thro Global Schema TYpes for STS : " + schematypesys.getName());
            out.println("\t=======================================================");
            SchemaType[] schematypes = schematypesys.globalTypes();
            for (int i = 0; i < schematypes.length; i++)
            {
                SchemaType schema = schematypes[i];

                out.println("\n\t Schema Type :" + schema.getName());
                out.println("\t=======================================================");

                out.println("\t----Acessing New Schema Type ......");
                if (schema.isCompiled())
                {
                    out.println("\t----This Schema has been successfully compiled");
                }
                else
                {
                    out.println("\t----This Schema has NOT compiled successfully yet");
                }

                out.println("\t----Content Type: " + schema.getContentType());
                out.println("\t----Name: " + schema.getName());
                out.println("\t----Doc Elem Name : " + schema.getDocumentElementName());
                out.println("\t----Annotation (class) : " + schema.getAnnotation());
                out.println("\t----Java Name : " + schema.getFullJavaName());
                out.println("\t----Java Imp Name : " + schema.getFullJavaImplName());
                out.println("\t----Java Class Name : " + schema.getJavaClass());
                out.println("\t----XSD src File Name : " + schema.getSourceName());


                // get Elements and Attributes
                out.println("\t Elements & Attributes for Schema Type :" + schema.getName());
                out.println("\t=======================================================");
                SchemaProperty[] spropsArr = schema.getProperties();
                for (int j = 0; j < spropsArr.length; j++)
                {
                    SchemaProperty schemaProperty = spropsArr[j];
                    out.println("\t:::-> Each prop name : " + schemaProperty.getName());
                }
                out.println("\t=======================================================");

                // other api's to look for
                SchemaProperty[] sderviedpropArr = schema.getDerivedProperties();
                for (int j = 0; j < sderviedpropArr.length; j++)
                {
                    SchemaProperty schemaProperty = sderviedpropArr[j];
                    out.println("\t+++-> Each derived prop name : " + schemaProperty.getName());
                }

                // TODO anonymus types
                //schema.getAnonymousTypes();

            }
            out.println("-------------------------------------------------------");

            out.println("Output for SchemaTypeSystem " + schematypesys.getName());
            out.close();

        } // end of try
        catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
            ioe.printStackTrace();
        }


    }

    public boolean lookForAttributeInSTS(SchemaTypeSystem tgtSTS,
                                         String sAttrLocalName)
    {
        // The QName for the find is constructed using the local name since the schemas have no namespace
        SchemaGlobalAttribute sga = tgtSTS.findAttribute(new QName(sAttrLocalName));
        if (sga == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean lookForElemInSTS(SchemaTypeSystem tgtSTS,
                                    String sElemLocalName)
    {
        // The QName for the find is constructed using the local name since the schemas have no namespace
        SchemaGlobalElement sge = tgtSTS.findElement(new QName(sElemLocalName));

        if (sge == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean lookForIdentityConstraint(SchemaTypeSystem sts,
                                             String ConstraintLocalName)
    {

        SchemaIdentityConstraint.Ref icref = sts.findIdentityConstraintRef(new QName(ConstraintLocalName));
        if (icref == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean checkPSOMSave(SchemaTypeSystem tgtSTS)
    {
        String outDirName = tgtSTS.getName().split("schemaorg_apache_xmlbeans.system.")[1];
        String outDirNameWithPath = somOutputRootDir + P + runid + P + outDirName;

        // call the save
        try
        {
            tgtSTS.saveToDirectory(new File(outDirNameWithPath));
        }
        catch (IllegalStateException ise)
        {
            // uncomment to see the stack trace
            // ise.printStackTrace();
            return false;
        }
        return true;

    }

    public boolean compareSavedSOMs(String outDirSchemaOne, String outDirSchemaTwo)
    {
        System.out.println("Comparing Schemas....");

        String runDir = somOutputRootDir + P + runid + P;
        File sts1 = new File(somOutputRootDir + P + runid + P + outDirSchemaOne);
        if (!sts1.exists() && (!sts1.isDirectory()))
        {
            System.out.println("Schema Type System save dir specified (" + runDir + outDirSchemaOne + ") does not exist!");
            return false;
        }

        File sts2 = new File(somOutputRootDir + P + runid + P + outDirSchemaTwo);
        if (!sts2.exists() && (!sts2.isDirectory()))
        {
            System.out.println("Schema Type System save dir specified (" + runDir + outDirSchemaTwo + ") does not exist!");
            return false;
        }

        List diff = new ArrayList();
        Diff.filesAsXsb(sts1, sts2, diff);
        if (diff.isEmpty())
        {
            return true;
        }
        else
        {
            for (Iterator itr = diff.iterator(); itr.hasNext();)
            {
                System.out.println("Difference found : " + itr.next());
            }
            return false;
        }
    }

    public boolean printRecoveredErrors()
    {
        // check list of errors and print them
        boolean errFound = false;
        if (!errors.isEmpty())
        {
            for (Iterator i = errors.iterator(); i.hasNext();)
            {
                XmlError eacherr = (XmlError) i.next();
                int errSeverity = eacherr.getSeverity();
                if (errSeverity == XmlError.SEVERITY_ERROR)
                {
                    System.out.println("Schema invalid: partial schema type system recovered");
                    System.out.println("Err Msg (s) at line #" + eacherr.getLine() + ": " + eacherr.getMessage());
                    errFound = true;
                }
                else if (errSeverity == XmlError.SEVERITY_WARNING)
                {
                    System.out.println("Warning Msg (s) at line #" + eacherr.getLine() + ": " + eacherr.getMessage());
                }
                else if (errSeverity == XmlError.SEVERITY_INFO)
                {
                    System.out.println("Info Msg (s) at line #" + eacherr.getLine() + ": " + eacherr.getMessage());
                }
            }
            errors.clear();
        }
        return errFound;
    }

    public boolean validateInstance(File instancefile,
                                    SchemaTypeSystem sts)
    {
        try
        {
            XmlObject instancedoc = sts.parse(instancefile, null, null);

            XmlOptions instanceValOptions = new XmlOptions();
            ArrayList errList = new ArrayList();
            instanceValOptions.setErrorListener(errList);

            if (!instancedoc.validate(instanceValOptions))
            {
                if (!errList.isEmpty())
                {
                    for (Iterator it = errList.iterator(); it.hasNext();)
                    {
                        System.out.println("Instance Validation Error(s) : " + it.next());
                    }
                }
                //Assert.fail("Validation against instance failed");
                return false;
            }
        }
        catch (IOException ioe)
        {
            ioe.getMessage();
            ioe.printStackTrace();
            Assert.fail("IOException throw when accessing instance xml file " + instancefile.getAbsoluteFile());
        }
        catch (XmlException xme)
        {
            System.out.println("Instance Validation Errors .. .. ..");
            if (xme.getErrors().isEmpty())
            {
                System.out.println(xme.getMessage());
            }
            else
            {
                for (Iterator itr = xme.getErrors().iterator(); itr.hasNext();)
                {
                    System.out.println(itr.next());
                }
            }
            System.out.println("END Instance Validation Errors .. .. ..");
            Assert.fail("Instance Validation - Xml Exception caught");
        }

        // validation successful
        return true;

    }

    public File getTestCaseFile(String sFileName)
    {
        String sFileWithPath = casesRootDir + P + sFileName;
        //System.out.println("getTestCaseFile() Opening File : " + sFileWithPath);
        File schemaFile = new File(sFileWithPath);
        Assert.assertNotNull("Schema File " + sFileWithPath + " Loading failed", schemaFile);
        return (schemaFile);
    }

    // returns the Local Part of the type QName for the specified Elem
    public String getElementType(SchemaTypeSystem sts,
                                 String sElementLocalName)
    {

        SchemaGlobalElement elem = sts.findElement(new QName(sElementLocalName));
        if (elem != null)
        {
            return elem.getType().getName().getLocalPart();
        }
        return "ElemNotFound";
    }

    public boolean getAttributeGroup(SchemaTypeSystem sts,
                                     String sAttrGrpLocalName)
    {
        SchemaAttributeGroup attrGp = sts.findAttributeGroup(new QName(sAttrGrpLocalName));
        if (attrGp == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean getModelGroup(SchemaTypeSystem sts,
                                 String sModelGrpLocalName)
    {
        SchemaModelGroup.Ref modelGp = sts.findModelGroupRef(new QName(sModelGrpLocalName));
        if (modelGp == null)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public SchemaTypeSystem createNewSTS(String xsdFileName,
                                         SchemaTypeSystem baseSchema,
                                         String sSTSName, String sBaseSourceName)
    {
        SchemaTypeSystem returnSTS = null;
        try
        {
            File xsdModified = getTestCaseFile(xsdFileName);
            XmlObject xsdModifiedObj = XmlObject.Factory.parse(xsdModified);
            System.out.println("Source Name for STS: " + xsdModifiedObj.documentProperties().getSourceName());

            // If null is passed for the basename, the basename is not set. Modified for namespace testcases.
            // If a source name is specified, deferencing of location for schemaLocation attribute gets messed up.
            if(sBaseSourceName != null)
            {
                xsdModifiedObj.documentProperties().setSourceName(sBaseSourceName);
            }
            Assert.assertNotNull("Xml Object creation failed", xsdModifiedObj);
            XmlObject[] xobjArr = new XmlObject[]{xsdModifiedObj};

            returnSTS = XmlBeans.compileXmlBeans(sSTSName, baseSchema, xobjArr, null, builtin, null, options);
            Assert.assertNotNull("Schema Type System created is Null.", returnSTS);

            // validate the XmlObject created
            Assert.assertTrue("Return Value for Validate()", xsdModifiedObj.validate());
        }
        catch (XmlException xme)
        {
            // even if using "COMPILE_PARTIAL_TYPESYSTEM", compilation will fail if
            // there are any non-recoverable errors and an XmlException will be thrown
            System.out.println("Schema invalid, XML Exception thrown : couldn't recover from errors");
            if (errors.isEmpty())
            {
                System.out.println(xme.getMessage());
            }
            else
            {
                for (Iterator i = errors.iterator(); i.hasNext();)
                {
                    System.out.println(i.next());
                }
            }
            fail("Schema invalid, XML Exception thrown : couldn't recover from errors");
        }
        catch (IOException ioe)
        {
            ioe.getMessage();
            ioe.printStackTrace();
        }
        finally
        {
            //printRecoveredErrors();
            return returnSTS;
        }
    }

    // deletes contents of specified directory, does not delete the specified directory
    public void deleteDirRecursive(File dirToClean)
    {
        if (dirToClean.exists() && dirToClean.isDirectory())
        {
            File filesFound [] = dirToClean.listFiles();
            for (int i = 0; i < filesFound.length; i++)
            {
                if (filesFound[i].isDirectory())
                {
                    deleteDirRecursive(filesFound[i]);
                    Assert.assertTrue("Output Directory " + filesFound[i] + " Deletion Failed ", filesFound[i].delete());
                }
                else if (filesFound[i].isFile())
                {
                    Assert.assertTrue("Output File " + filesFound[i] + " Deletion Failed ", filesFound[i].delete());
                }
            }
        }

    }

    public void createRunLogFile()
    {
        File logfile = new File(somOutputRootDir + P + "PartialSOMCheckinTest_Run_" + runid + ".log");


        try
        {
            // if file exists for this run, append to it
            if (logfile.exists())
            {
                outlog = new PrintWriter(new FileWriter(logfile, true));
            }
            else
            {
                outlog = new PrintWriter(new FileWriter(logfile));
            }
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }


}
