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

package compile.scomp.som.checkin;

import compile.scomp.som.common.SomTestBase;
import junit.framework.Assert;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.impl.tool.Diff;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 *
 */

public class PartialSOMCheckinTest extends SomTestBase
{

    public PartialSOMCheckinTest(String name)
    {
        super(name);
    }


    public void setUp()
    {
        // initialize the built in schema type
        builtin = XmlBeans.getBuiltinTypeSystem();

        // populate the XmlOptions
        if (errors == null)
        {
            errors = new ArrayList();
        }
        if (options == null)
        {
            options = (new XmlOptions()).setErrorListener(errors);
            options.setCompileDownloadUrls();
            options.put("COMPILE_PARTIAL_TYPESYSTEM");
            options.setLoadLineNumbers();
        }

        // initialize the runid to be used for generating output files for the PSOM walk thro's
        runid = new Date().getTime();

        // clean the output from the previous run
        // delete directories created by checkPSOMSave() and output text file created by inspectPSOM()
        deleteDirRecursive(new File(somOutputRootDir));

    }

    public void tearDown()
    {
        errors.clear();
    }


    public void testAddAttributeAndElements() throws Exception
    {
        System.out.println("Inside test case testAddAttributeAndElements()");

        // Step 1 : create a Schema Type System with the base 'bad' xsd and create the Schema Type System (STS) for it
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("elemattr.xsd_",
                null,
                "BaseSchemaTS",
                sBaseSourceName);

        Assert.assertNotNull("Schema Type System created is Null.",
                            baseSTS);

        // the tests - Walk thro the SOM, save, validate against an xml instance
        inspectSOM(baseSTS, 1, 1, 1, 0);

        // test for recoverable errors
        Assert.assertTrue("No Recovered Errors for Invalid Schema",
                printRecoveredErrors());

        // Test for saving of the PSOM - should not be able to save
        Assert.assertFalse("Partial SOM " + baseSTS.getName() + "Save successful - should fail!",
                checkPSOMSave(baseSTS));

        // instance validation - should fail
        Assert.assertFalse("Validation against instance Success - should fail ",
                validateInstance(getTestCaseFile("instance_elemattr_valid.xml"), baseSTS));

        // additional validation
        Assert.assertFalse("Attribute found but not expected - 'testAttributeComplex'",
                        lookForAttributeInSTS(baseSTS,
                        "testAttributeComplex"));
        Assert.assertFalse("Element found but not expected 'ComplexTypeElem'",
                        lookForElemInSTS(baseSTS,
                        "ComplexTypeElem"));
        Assert.assertFalse("Element found but not expected  'SimpleTypeElem'",
                lookForElemInSTS(baseSTS, "SimpleTypeElem"));


        // Step 2: create a Schema Type System with the new xsd file that has additions to this schema
        SchemaTypeSystem modifiedSTS = createNewSTS("elemattr_added.xsd_",
                baseSTS,
                "ModifiedSchemaTS",
                sBaseSourceName);

        Assert.assertNotNull("Schema Type System created is Null.",
                modifiedSTS);

        // test the PSOM created : walk thro the PSOM, look for # of elements,attributes,types & attribute groups
        inspectSOM(modifiedSTS, 3, 2, 1, 0);

        // Test for successful saving of the PSOM
        Assert.assertTrue("Valid Partial SOM " + modifiedSTS.getName() + "Save failed",
                checkPSOMSave(modifiedSTS));

        // Look for  added attribute(s)/Element(s) by name in the STS
        Assert.assertTrue("Attribute expected, not found 'testAttributeComplex'",
                lookForAttributeInSTS(modifiedSTS,
                "testAttributeComplex"));
        Assert.assertTrue("Element expected, not found 'ComplexTypeElem'",
                lookForElemInSTS(modifiedSTS,
                "ComplexTypeElem"));
        Assert.assertTrue("Element expected, not found 'SimpleTypeElem'",
                lookForElemInSTS(modifiedSTS,
                        "SimpleTypeElem"));

        // validate against an xml instance
        Assert.assertTrue("Validation against instance failed ",
        validateInstance(getTestCaseFile("instance_elemattr_valid.xml"), modifiedSTS));

        // Step 3: now creat the Schema Type System with the original XSD again
        SchemaTypeSystem finalSTS = createNewSTS("elemattr.xsd_",
                modifiedSTS,
                "FinalSchemaTS",
                sBaseSourceName);

        Assert.assertNotNull("Schema Type System created is Null.", finalSTS);

        //walk the SOM
        inspectSOM(finalSTS, 1, 1, 1, 0);

        // Test for saving of the PSOM - should not be able to save
        Assert.assertFalse("Partial SOM " + finalSTS.getName() + "Save successful - should fail!",
                checkPSOMSave(finalSTS));

        // instance validation - should fail
        Assert.assertFalse("Validation against instance Success - should fail ",
                validateInstance(getTestCaseFile("instance_elemattr.xml"), finalSTS));

    }

    public void testModifyAttributeAndElements() throws Exception
    {
        System.out.println("Inside test case testModifyAttributeAndElements()");

        // Step 1 : create a Schema Type System with the base 'good' xsd and create the Schema Type System (STS) for it
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("elemattr_added.xsd_",
                null,
                "BaseSchemaTS",
                sBaseSourceName);

        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // the tests - Walk thro the SOM, save, validate against an xml instance
        inspectSOM(baseSTS, 3, 2, 1, 0);

        // validate successful save
        Assert.assertTrue("Valid SOM " + baseSTS.getName() + "Save failed ",
                checkPSOMSave(baseSTS));

        // validate against instance successfully
        Assert.assertTrue("Validation against instance Failed ",
                validateInstance(getTestCaseFile("instance_elemattr_valid.xml"), baseSTS));

        // Step 2: create a Schema Type System with the new xsd file with modifications to existing schema
        SchemaTypeSystem modifiedSTS = createNewSTS("elemattr_modified.xsd_",
                baseSTS,
                "ModifiedSchemaTS",
                sBaseSourceName);

        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        // test for recoverable errors
        Assert.assertTrue("No Recovered Errors for Invalid Schema",
                printRecoveredErrors());

        // test the PSOM created
        inspectSOM(modifiedSTS, 2, 2, 1, 0); // walk thro the PSOM, look for # of elements,attributes,types & attribute groups

        // Look for a modified attribute(s)/elements by name in the STS
        Assert.assertTrue("Attribute expected, not found 'testAttributeComplex'",
                lookForAttributeInSTS(modifiedSTS, "testAttributeComplex"));
        Assert.assertTrue("Element expected, not found 'ComplexTypeElem'",
                lookForElemInSTS(modifiedSTS, "ComplexTypeElem"));
        Assert.assertFalse("Element expected, not found 'SimpleTypeElem'",
                lookForElemInSTS(modifiedSTS, "SimpleTypeElem"));

        // Test for saving of the PSOM - should not be able to save
        Assert.assertFalse("Partial SOM " + modifiedSTS.getName() + " Save successful- should fail",
                checkPSOMSave(modifiedSTS));

        // validate against an xml instance - should fail
        Assert.assertFalse("Validation against instance Success - should Fail",
                validateInstance(getTestCaseFile("instance_elemattr_valid.xml"), modifiedSTS));

        // Step 3: now creat the Schema Type System with the original XSD again
        SchemaTypeSystem finalSTS = createNewSTS("elemattr_added.xsd_",
                modifiedSTS,
                "FinalSchemaTS",
                sBaseSourceName);

        Assert.assertNotNull("Schema Type System created is Null.", finalSTS);

        // walk the PSOM
        inspectSOM(finalSTS, 3, 2, 1, 0);

        // should be able to save as its a valid SOM
        Assert.assertTrue("Partial SOM " + finalSTS.getName() + "Save failed for complete SOM",
                checkPSOMSave(finalSTS));

        // validate against instance successfully
        Assert.assertTrue("Validation against instance Failed ",
                validateInstance(getTestCaseFile("instance_elemattr_valid.xml"), finalSTS));

        // compare this to the original schema here - the root dir names used to save the PSOMs are the same as the STS names
        Assert.assertTrue(compareSavedSOMs("BaseSchemaTS","FinalSchemaTS"));
    }

    public void testDeleteAttributeAndElements() throws Exception
    {
        System.out.println("Inside test case testDeleteAttributeAndElements()");

        // Step 1 : create a Schema Type System with the base 'good' xsd and create the Schema Type System (STS) for it
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("elemattr_added.xsd_",
                null,
                "BaseSchemaTS",
                sBaseSourceName);

        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // the tests - Walk thro the SOM, save, validate against an xml instance
        inspectSOM(baseSTS, 3, 2, 1, 0);

        // validate successful save
        Assert.assertTrue("Valid SOM " + baseSTS.getName() + "Save failed ",
                checkPSOMSave(baseSTS));

        // validate against instance successfully
        Assert.assertTrue("Validation against instance Failed ",
                validateInstance(getTestCaseFile("instance_elemattr_valid.xml"), baseSTS));

        // Step 2: create a Schema Type System with the new xsd file that has deletions
        SchemaTypeSystem modifiedSTS = createNewSTS("elemattr.xsd_",
                baseSTS,
                "ModifiedSchemaTS",
                sBaseSourceName);

        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        // test for recoverable errors
        Assert.assertTrue("No Recovered Errors for Invalid Schema",
                printRecoveredErrors());

        // test the PSOM created
        inspectSOM(modifiedSTS, 1, 1, 1, 0); // walk thro the PSOM, look for # of elements,attributes,types & attribute groups

        // Test for saving of the PSOM - should not be able to save
        Assert.assertFalse("Invalid PSOM " + modifiedSTS.getName() + " Save successful - Should fail",
                checkPSOMSave(modifiedSTS));

        // verify types
        Assert.assertFalse("Attribute found but not expected - 'testAttributeComplex'",
                lookForAttributeInSTS(modifiedSTS, "testAttributeComplex"));
        Assert.assertFalse("Element found but not expected 'ComplexTypeElem'",
                lookForElemInSTS(modifiedSTS, "ComplexTypeElem"));
        Assert.assertFalse("Element found but not expected  'SimpleTypeElem'",
                lookForElemInSTS(modifiedSTS, "SimpleTypeElem"));

        // validate against an xml instance - should fail
        Assert.assertFalse("Validation against success - should Fail ",
                validateInstance(getTestCaseFile("instance_elemattr_valid.xml"), modifiedSTS));

        // Step 3: now creat the Schema Type System with the original XSD again
        SchemaTypeSystem finalSTS = createNewSTS("elemattr_added.xsd_",
                modifiedSTS,
                "FinalSchemaTS",
                sBaseSourceName);

        Assert.assertNotNull("Schema Type System created is Null.", finalSTS);

        // walk the SOM
        inspectSOM(finalSTS, 3, 2, 1, 0);

        // should be able to save as its a valid SOM
        Assert.assertTrue("Partial SOM " + finalSTS.getName() + "Save failed for complete SOM",
                checkPSOMSave(finalSTS));

        // validate against instance
        Assert.assertTrue("Validation against instance Failed ",
                validateInstance(getTestCaseFile("instance_elemattr_valid.xml"), finalSTS));

        // compare this to the original schema here
        Assert.assertTrue(compareSavedSOMs("BaseSchemaTS","FinalSchemaTS"));

    }

    public void testAddDataTypes() throws Exception
    {
        System.out.println("Inside test case testAddDataTypes()");
        // Step 1 : create a PSOM from an incomplete/invalid xsd (datatypes.xsd) with unresolved references to various types
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("datatypes.xsd_",
                null,
                "BaseSchemaTS",
                sBaseSourceName);

        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // recovearble errors should exist
        Assert.assertTrue("No Recovered Errors for Invalid Schema",
                printRecoveredErrors());

        // Walk thro the SOM (pass #Elems, #Attr, #Types, #AttrGroups)
        inspectSOM(baseSTS, 12, 1, 4, 1);

        // Test for saving of the PSOM - should not be able to save
        Assert.assertFalse("Partial SOM " + baseSTS.getName() + "Save successful - should fail!",
                checkPSOMSave(baseSTS));

        // instance validation - should fail
        Assert.assertFalse("Validation against instance Success - should fail ",
                validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), baseSTS));

        // additional validation - check to see if the unresolved references to types are 'anyType'
        // validate unresolved types
        Assert.assertEquals("Unresolved Simple Type should be 'anyType'",
                anyType,
                getElementType(baseSTS, "testAtomicTypeElem"));
        Assert.assertEquals("Unresolved Simple Type should be 'anyType'",
                anyType,
                getElementType(baseSTS, "testUnionTypeElem"));

        // moved to detailed PSOMDetailedTest class
        //Assert.assertEquals("Unresolved List Type should be 'anySimpleType'", anySimpleType, getElementType(baseSTS, "testListTypeElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'anyType'",
                anyType,
                getElementType(baseSTS, "testComplexTypeSimpleContentElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'anyType'",
                anyType,
                getElementType(baseSTS, "testComplexTypeElementOnlyContentElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'anyType'",
                anyType,
                getElementType(baseSTS, "testComplexTypeMixedElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'anyType'",
                anyType,
                getElementType(baseSTS, "testComplexTypeEmptyElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'anyType'",
                anyType,
                getElementType(baseSTS, "testChoiceGroupElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'anyType'",
                anyType,
                getElementType(baseSTS, "testAllGroupElem"));

        // Step 2 : create an incremental PSOM that is valid by loading datatypes_added.xsd
        SchemaTypeSystem modifiedSTS = createNewSTS("datatypes_added.xsd_", baseSTS, "ModifiedSchemaTS", sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        // no errors expected to be recovered - should be a valid SOM
        Assert.assertFalse("Valid Schema Type System, Errors recovered",
                printRecoveredErrors());

        // test the PSOM created : walk thro the PSOM, look for # of elements,attributes,types & attribute groups
        inspectSOM(modifiedSTS, 13, 1, 15, 1);

        // test successful save
        Assert.assertTrue("Valid SOM " + modifiedSTS.getName() + " Save failed",
                checkPSOMSave(modifiedSTS));

        // validate against an xml valid instance - should succeed
        Assert.assertTrue("Validation against instance Failed ",
                validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), modifiedSTS));

        // validate against an xml invalid instance - should fail
        Assert.assertFalse("Validation against instance Failed ",
                validateInstance(getTestCaseFile("instance_simple_types_invalid.xml"), modifiedSTS));

        // additional validation - check to see if all types are resolved to their respective types
        Assert.assertEquals("Unresolved Simple Type should be 'attachmentTypes'",
                "attachmentTypes",
                getElementType(modifiedSTS, "testAtomicTypeElem"));
        Assert.assertEquals("Unresolved Simple Type should be 'union.attachmentUnionType'",
                "union.attachmentUnionType",
                getElementType(modifiedSTS, "testUnionTypeElem"));
        Assert.assertEquals("Unresolved List Type should be 'attchmentExtensionListTypes'",
                "attchmentExtensionListTypes",
                getElementType(modifiedSTS, "testListTypeElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'headerType'",
                "headerType",
                getElementType(modifiedSTS, "testComplexTypeSimpleContentElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'mailsType'",
                "mailsType",
                getElementType(modifiedSTS, "testComplexTypeElementOnlyContentElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'mixedContentType'",
                "mixedContentType",
                getElementType(modifiedSTS, "testComplexTypeMixedElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'emptyContentType'",
                "emptyContentType",
                getElementType(modifiedSTS, "testComplexTypeEmptyElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'choiceGroupType'",
                "choiceGroupType",
                getElementType(modifiedSTS, "testChoiceGroupElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'allGroupType'",
                "allGroupType",
                getElementType(modifiedSTS, "testAllGroupElem"));


        // Step 3 : create an incremental STS with the file in step 1
        SchemaTypeSystem finalSTS = createNewSTS("datatypes.xsd_",
                modifiedSTS,
                "FinalSchemaTS",
                sBaseSourceName);

        Assert.assertNotNull("Schema Type System created is Null.", finalSTS);

        // test the PSOM created : walk thro the PSOM, look for # of elements,attributes,types & attribute groups
        inspectSOM(finalSTS, 12, 1, 4, 1);

        // test save failure
        Assert.assertFalse("Partial SOM " + finalSTS.getName() + "Save Success ",
                checkPSOMSave(finalSTS));

        // instance validation - should fail
        Assert.assertFalse("Validation against instance Success - should fail ",
                validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), finalSTS));

    }

    public void testDeleteDataTypes() throws Exception
    {
        System.out.println("Inside test case testDeleteDataTypes()");

        // Step 1: read a clean XSD file to get a valid SOM
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("datatypes_added.xsd_",
                null,
                "BaseSchemaTS",
                sBaseSourceName);

        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // there should be NO recovearble errors
        Assert.assertEquals("Recovered Errors for Valid Schema", false, printRecoveredErrors());

        // the tests - Walk thro the SOM, save, validate against an xml instance
        inspectSOM(baseSTS, 13, 1, 15, 1);

        // Recovered Errors, Test for saving of the PSOM - should go thro
        Assert.assertTrue("SOM " + baseSTS.getName() + "Save failed!",
                checkPSOMSave(baseSTS));

        // instance validation - should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), baseSTS));

        // additional validation - check to see if all types are resolved to their respective types
        Assert.assertEquals("Unresolved Simple Type should be 'attachmentTypes'",
                "attachmentTypes",
                getElementType(baseSTS, "testAtomicTypeElem"));
        Assert.assertEquals("Unresolved Simple Type should be 'union.attachmentUnionType'",
                "union.attachmentUnionType",
                getElementType(baseSTS, "testUnionTypeElem"));
        Assert.assertEquals("Unresolved List Type should be 'attchmentExtensionListTypes'",
                "attchmentExtensionListTypes",
                getElementType(baseSTS, "testListTypeElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'headerType'",
                "headerType",
                getElementType(baseSTS, "testComplexTypeSimpleContentElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'mailsType'",
                "mailsType",
                getElementType(baseSTS, "testComplexTypeElementOnlyContentElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'mixedContentType'",
                "mixedContentType",
                getElementType(baseSTS, "testComplexTypeMixedElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'emptyContentType'",
                "emptyContentType",
                getElementType(baseSTS, "testComplexTypeEmptyElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'choiceGroupType'",
                "choiceGroupType",
                getElementType(baseSTS, "testChoiceGroupElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'allGroupType'",
                "allGroupType",
                getElementType(baseSTS, "testAllGroupElem"));


        //Step 2 : delete/remove types from the schema - should result in STS with unresolved refs
        SchemaTypeSystem modifiedSTS = createNewSTS("datatypes.xsd_",
                baseSTS,
                "ModifiedSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        // PSOM - recovered errors are expected
        Assert.assertEquals("Valid Schema Type System, Errors recovered", true, printRecoveredErrors());

        // test the PSOM created : walk thro the PSOM, look for # of elements,attributes,types & attribute groups
        inspectSOM(modifiedSTS, 12, 1, 4, 1);

        // Test for saving of the PSOM - should not be able to save
        Assert.assertFalse("PSOM " + modifiedSTS.getName() + " Save should fail",
                checkPSOMSave(modifiedSTS));

        // validate unresolved types
        Assert.assertEquals("Unresolved Simple Type should be 'anyType'",
                anyType,
                getElementType(modifiedSTS, "testAtomicTypeElem"));
        Assert.assertEquals("Unresolved Simple Type should be 'anyType'",
                anyType,
                getElementType(modifiedSTS, "testUnionTypeElem"));
        // moved to detailed PSOMDetailedTest class
        // Assert.assertEquals("Unresolved List Type should be 'anySimpleType'", anySimpleType, getElementType(modifiedSTS, "testListTypeElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'anyType'",
                anyType,
                getElementType(modifiedSTS, "testComplexTypeSimpleContentElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'anyType'",
                anyType,
                getElementType(modifiedSTS, "testComplexTypeElementOnlyContentElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'anyType'",
                anyType,
                getElementType(modifiedSTS, "testComplexTypeMixedElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'anyType'",
                anyType,
                getElementType(modifiedSTS, "testComplexTypeEmptyElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'anyType'",
                anyType,
                getElementType(modifiedSTS, "testChoiceGroupElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'anyType'",
                anyType,
                getElementType(modifiedSTS, "testAllGroupElem"));

        // validate against an xml valid instance - should fail
        Assert.assertEquals("Validation against instance should Failed ", false, validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), modifiedSTS));

        // Step 3 : reaload the xsd in Step 1
        SchemaTypeSystem finalSTS = createNewSTS("datatypes_added.xsd_",
                modifiedSTS,
                "FinalSchemaTS",
                sBaseSourceName);

        // should be able to save as its a valid SOM
        Assert.assertNotNull("Schema Type System created is Null.", finalSTS);

        // walk the PSOM
        inspectSOM(finalSTS, 13, 1, 15, 1);

        // should be able to save as its a valid SOM
        Assert.assertTrue("SOM " + finalSTS.getName() + "Save failed",
                checkPSOMSave(finalSTS));

        // instance validation - should be fine
        Assert.assertTrue("Validation against instance Failed ",
                validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), finalSTS));

        // compare this to the original schema here
        Assert.assertTrue(compareSavedSOMs("BaseSchemaTS","FinalSchemaTS"));

        // additional validation - check to see if all types are resolved to their respective types
        Assert.assertEquals("Unresolved Simple Type should be 'attachmentTypes'",
                "attachmentTypes",
                getElementType(baseSTS, "testAtomicTypeElem"));
        Assert.assertEquals("Unresolved Simple Type should be 'union.attachmentUnionType'",
                "union.attachmentUnionType",
                getElementType(baseSTS, "testUnionTypeElem"));
        Assert.assertEquals("Unresolved List Type should be 'attchmentExtensionListTypes'",
                "attchmentExtensionListTypes",
                getElementType(baseSTS, "testListTypeElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'headerType'",
                "headerType",
                getElementType(baseSTS, "testComplexTypeSimpleContentElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'mailsType'",
                "mailsType",
                getElementType(baseSTS, "testComplexTypeElementOnlyContentElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'mixedContentType'",
                "mixedContentType",
                getElementType(baseSTS, "testComplexTypeMixedElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'emptyContentType'",
                "emptyContentType",
                getElementType(baseSTS, "testComplexTypeEmptyElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'choiceGroupType'",
                "choiceGroupType",
                getElementType(baseSTS, "testChoiceGroupElem"));
        Assert.assertEquals("Unresolved Complex Type should be 'allGroupType'",
                "allGroupType",
                getElementType(baseSTS, "testAllGroupElem"));

    }

    public void testModifyDataTypes() throws Exception
    {
        System.out.println("Inside test case testModifyDataTypes()");

        // Step 1: read in a clean XSD datatypes_added.xsd, to create a base schema with no unresolved components
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("datatypes_added.xsd_",
                null,
                "BaseSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // there should be NO recovearble errors
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 13, 1, 15, 1);

        // Recovered Errors, Test for saving of the SOM - should go thro
        Assert.assertTrue("SOM " + baseSTS.getName() + "Save failed!",
                checkPSOMSave(baseSTS));

        // instance validation - should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), baseSTS));

        // check types before modify
        Assert.assertEquals("Unresolved Simple Type should be 'attachmentTypes'",
                "attachmentTypes",
                getElementType(baseSTS, "testAtomicTypeElem"));
        Assert.assertEquals("Unresolved List Type should be 'attchmentExtensionListTypes'",
                "attchmentExtensionListTypes",
                getElementType(baseSTS, "testListTypeElem"));
        Assert.assertEquals("Unresolved Simple Type should be 'union.attachmentUnionType",
                "union.attachmentUnionType",
                getElementType(baseSTS, "testUnionTypeElem"));


        //Step 2 : modify types from the schema - should result in STS with unresolved refs
        //remove one of the constituent types for the union and test to see if union is anySimpleType
        SchemaTypeSystem modifiedSTS = createNewSTS("datatypes_modified.xsd_",
                baseSTS,
                "ModifiedSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        // test the PSOM created :walk thro the PSOM, look for # of elements,attributes,types & attribute groups
        inspectSOM(modifiedSTS, 13, 1, 13, 1);

        // Test for saving of the PSOM - should not be able to save
        Assert.assertFalse("PSOM " + modifiedSTS.getName() + " Save should fail",
                checkPSOMSave(modifiedSTS));

        // validate unresolved types
        Assert.assertEquals("Unresolved Simple Type - Atomic should be 'anyType'",
                anyType,
                getElementType(modifiedSTS, "testAtomicTypeElem"));

        // moved to detailed PSOMDetailedTest class
        //Assert.assertEquals("Unresolved List Type should be 'anySimpleType'", anySimpleType, getElementType(modifiedSTS, "testListTypeElem"));
        //Assert.assertEquals("Unresolved Simple Type - Union should be 'anySimpleType'", anySimpleType, getElementType(modifiedSTS, "testUnionTypeElem"));

        // validate against an xml valid instance - should fail
        Assert.assertFalse("Validation against instance should Failed ",
                validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), modifiedSTS));

        // step 3: reload the original STS
        SchemaTypeSystem finalSTS = createNewSTS("datatypes_added.xsd_",
                modifiedSTS,
                "FinalSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", finalSTS);

        // walk the SOM
        inspectSOM(finalSTS, 13, 1, 15, 1);

        // validate successful save
        Assert.assertTrue("SOM " + finalSTS.getName() + "Save failed",
                checkPSOMSave(finalSTS)); // should be able to save as its a valid SOM

        // validate instance - should validate
        Assert.assertTrue("Validation against instance Failed ",
                validateInstance(getTestCaseFile("instance_datatypes_valid.xml"), finalSTS));

        // check types after modify
        Assert.assertEquals("Unresolved Simple Type should be 'attachmentTypes'",
                "attachmentTypes",
                getElementType(finalSTS, "testAtomicTypeElem"));
        Assert.assertEquals("Unresolved List Type should be 'attchmentExtensionListTypes'",
                "attchmentExtensionListTypes",
                getElementType(finalSTS, "testListTypeElem"));
        Assert.assertEquals("Unresolved Simple Type should be 'union.attachmentUnionType",
                "union.attachmentUnionType",
                getElementType(finalSTS, "testUnionTypeElem"));

        // compare this to the original schema here
        Assert.assertTrue(compareSavedSOMs("BaseSchemaTS","FinalSchemaTS"));
    }

    public void testDeleteDerivedTypes() throws Exception
    {
        System.out.println("Inside test case testDeleteDerivedTypes()");

        // Step 1: read in a clean XSD derived_types_added.xsd with base and derived types to create a base schema with no unresolved components
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("derived_types_added.xsd_",
                null,
                "BaseSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // there should be NO recovearble errors
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 13, 0, 14, 0);

        // Recovered Errors, Test for saving of the SOM - should go thro
        Assert.assertTrue("Valid SOM " + baseSTS.getName() + "Save failed!",
                checkPSOMSave(baseSTS));

        // instance validation - should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_derived_types_valid.xml"), baseSTS));

        // check types before deletion of base types
        Assert.assertEquals("Elem Type  should be 'ExtensionBaseType' (base)",
                "ExtensionBaseType",
                getElementType(baseSTS, "ExtensionBaseTypeElem"));
        Assert.assertEquals("Elem Type  should be 'ExtensionDerivedComplexContentType' (derived)",
                "ExtensionDerivedComplexContentType",
                getElementType(baseSTS, "ExtensionDerivedComplexContentTypeElem"));

        Assert.assertEquals("Elem Type  should be 'ExtensionBaseMixedContentType' (base)",
                "ExtensionBaseMixedContentType",
                getElementType(baseSTS, "ExtensionBaseMixedContentTypElem"));
        Assert.assertEquals("Elem Type  should be 'ExtensionDerivedMixedContentType' (derived)",
                "ExtensionDerivedMixedContentType",
                getElementType(baseSTS, "ExtensionDerivedMixedContentTypeElem"));

        Assert.assertEquals("Elem Type  should be 'RestrictionSimpleContentBaseType'",
                "RestrictionSimpleContentBaseType", getElementType(baseSTS, "RestrictionSimpleContentBaseTypeElem"));
        Assert.assertEquals("Elem Type  should be 'RestrictionSimpleContentDerivedType'",
                "RestrictionSimpleContentDerivedType", getElementType(baseSTS, "RestrictionSimpleContentDerivedTypeElem"));

        Assert.assertEquals("Elem Type  should be 'RestrictionBaseComplexContentType'",
                "RestrictionBaseComplexContentType",
                getElementType(baseSTS, "RestrictionBaseComplexContentTypeElem"));
        Assert.assertEquals("Elem Type  should be 'RestrictionDerivedComplexContentType'",
                "RestrictionDerivedComplexContentType",
                getElementType(baseSTS, "RestrictionDerivedComplexContentTypeElem"));

        Assert.assertEquals("Elem Type  should be 'RestrictionBaseMixedContentType'",
                "RestrictionBaseMixedContentType", getElementType(baseSTS, "RestrictionBaseMixedContentTypeElem"));
        Assert.assertEquals("Elem Type  should be 'RestrictionDerivedMixedContentType'",
                "RestrictionDerivedMixedContentType", getElementType(baseSTS, "RestrictionDerivedMixedContentTypeElem"));

        Assert.assertEquals("Elem Type  should be 'RestrictionBaseEmptyContentType'",
                "RestrictionBaseEmptyContentType", getElementType(baseSTS, "RestrictionBaseEmptyContentTypeElem"));
        Assert.assertEquals("Elem Type  should be 'RestrictionDerivedEmptyContentType'",
                "RestrictionDerivedEmptyContentType", getElementType(baseSTS, "RestrictionDerivedEmptyContentTypeElem"));

        // Step 2: create invalid PSOM with base type removed
        SchemaTypeSystem modifiedSTS = createNewSTS("derived_types.xsd_",
                baseSTS,
                "ModifiedSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        // recovearble errors
        Assert.assertTrue("No Recovered Errors for Invalid PSOM",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 13, 0, 9, 0);

        // Recovered Errors, Test for saving of the SOM
        Assert.assertEquals("SOM " + modifiedSTS.getName() + "Save Success - should fail!",
                false, checkPSOMSave(modifiedSTS));

        // instance validation - should fail
        Assert.assertFalse("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_derived_types_valid.xml"), modifiedSTS));

        // check types - base should be 'anyType'
        Assert.assertEquals("Elem Type  should be 'anyType' (base)",
                anyType,
                getElementType(modifiedSTS, "ExtensionBaseTypeElem"));
        Assert.assertEquals("Elem Type  should be 'ExtensionDerivedComplexContentType' (derived)",
                "ExtensionDerivedComplexContentType",
                getElementType(modifiedSTS, "ExtensionDerivedComplexContentTypeElem"));

        Assert.assertEquals("Elem Type  should be 'anyType' (base)",
                anyType,
                getElementType(modifiedSTS, "ExtensionBaseMixedContentTypElem"));
        Assert.assertEquals("Elem Type  should be 'ExtensionDerivedComplexContentType' (derived)",
                "ExtensionDerivedMixedContentType",
                getElementType(modifiedSTS, "ExtensionDerivedMixedContentTypeElem"));

        // Restriction Simple Content Base type commented does not result in recoverable SOM
        // moved to Detailed Test
        //Assert.assertEquals("Elem Type  should be 'anyType'",
        //        anyType,
        //        getElementType(modifiedSTS, "RestrictionSimpleContentBaseTypeElem"));
        //

        Assert.assertEquals("Elem Type  should be 'RestrictionSimpleContentDerivedType'",
                "RestrictionSimpleContentDerivedType",
                getElementType(modifiedSTS, "RestrictionSimpleContentDerivedTypeElem"));

        Assert.assertEquals("Elem Type  should be 'anyType'",
                anyType,
                getElementType(modifiedSTS, "RestrictionBaseComplexContentTypeElem"));
        Assert.assertEquals("Elem Type  should be 'RestrictionDerivedComplexContentType'",
                "RestrictionDerivedComplexContentType",
                getElementType(modifiedSTS, "RestrictionDerivedComplexContentTypeElem"));

        Assert.assertEquals("Elem Type  should be 'anyType'",
                anyType,
                getElementType(modifiedSTS, "RestrictionBaseMixedContentTypeElem"));
        Assert.assertEquals("Elem Type  should be 'RestrictionDerivedMixedContentType'",
                "RestrictionDerivedMixedContentType",
                getElementType(modifiedSTS, "RestrictionDerivedMixedContentTypeElem"));

        Assert.assertEquals("Elem Type  should be 'anyType'", anyType,
                getElementType(modifiedSTS, "RestrictionBaseEmptyContentTypeElem"));
        Assert.assertEquals("Elem Type  should be 'RestrictionDerivedEmptyContentType'",
                "RestrictionDerivedEmptyContentType", getElementType(modifiedSTS, "RestrictionDerivedEmptyContentTypeElem"));


        // step 3: reload the original STS
        SchemaTypeSystem finalSTS = createNewSTS("derived_types_added.xsd_",
                modifiedSTS,
                "FinalSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", finalSTS);

        // there should be NO recovearble errors
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(finalSTS, 13, 0, 14, 0);

        // Recovered Errors, Test for saving of the SOM - should go thro
        Assert.assertTrue("SOM " + finalSTS.getName() + "Save failed!",
                checkPSOMSave(finalSTS));

        // instance validation - should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_derived_types_valid.xml"), finalSTS));

        // compare this to the original schema here
        Assert.assertTrue(compareSavedSOMs("BaseSchemaTS","FinalSchemaTS"));
    }


    public void testAddDerivedTypes() throws Exception
    {
        System.out.println("Inside test case testAddDerivedTypes()");

        // Step 1: start with invalid SOM - one that has derived types but the base types are not defined
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("derived_types.xsd_",
                null,
                "BaseSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        Assert.assertTrue("No Recovered Errors for Invalid PSOM",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 13, 0, 9, 0);

        // Recovered Errors, Test for saving of the SOM
        Assert.assertFalse("SOM " + baseSTS.getName() + "Save Success - should fail!",
                checkPSOMSave(baseSTS));

        // instance validation - should fail
        Assert.assertFalse("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_derived_types_valid.xml"), baseSTS));

        // check types - base should be 'anyType'
        Assert.assertEquals("Elem Type  should be 'anyType' (base)",
                anyType,
                getElementType(baseSTS, "ExtensionBaseTypeElem"));
        Assert.assertEquals("Elem Type  should be 'ExtensionDerivedComplexContentType' (derived)",
                "ExtensionDerivedComplexContentType",
                getElementType(baseSTS, "ExtensionDerivedComplexContentTypeElem"));

        Assert.assertEquals("Elem Type  should be 'anyType' (base)",
                anyType,
                getElementType(baseSTS, "ExtensionBaseMixedContentTypElem"));
        Assert.assertEquals("Elem Type  should be 'ExtensionDerivedComplexContentType' (derived)",
                "ExtensionDerivedMixedContentType",
                getElementType(baseSTS, "ExtensionDerivedMixedContentTypeElem"));

        // Step 2: create valid PSOM now  from xsd with base types defined
        SchemaTypeSystem modifiedSTS = createNewSTS("derived_types_added.xsd_",
                baseSTS,
                "ModifiedSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        // there should be NO recovearble errors
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 13, 0, 14, 0);

        // Recovered Errors, Test for saving of the SOM - should go thro
        Assert.assertTrue("SOM " + modifiedSTS.getName() + "Save failed!",
                checkPSOMSave(modifiedSTS));

        // instance validation - should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_derived_types_valid.xml"), modifiedSTS));

        // check types before deletion of base types
        Assert.assertEquals("Elem Type  should be 'ExtensionBaseType' (base)",
                "ExtensionBaseType",
                getElementType(modifiedSTS, "ExtensionBaseTypeElem"));
        Assert.assertEquals("Elem Type  should be 'ExtensionDerivedComplexContentType' (derived)",
                "ExtensionDerivedComplexContentType",
                getElementType(modifiedSTS, "ExtensionDerivedComplexContentTypeElem"));
        Assert.assertEquals("Elem Type  should be 'ExtensionBaseMixedContentType' (base)",
                "ExtensionBaseMixedContentType",
                getElementType(modifiedSTS, "ExtensionBaseMixedContentTypElem"));
        Assert.assertEquals("Elem Type  should be 'ExtensionDerivedMixedContentType' (derived)",
                "ExtensionDerivedMixedContentType",
                getElementType(modifiedSTS, "ExtensionDerivedMixedContentTypeElem"));

    }

    // moved to PSOMDetaiedTest
    //public void testDeleteReusableGroups() throws Exception
    //{}
    //public void testModifyReusableGroups() throws Exception

    public void testAddReusableGroups() throws Exception
    {
        System.out.println("Inside test case testAddReusableGroups()");

        // Step 1: read in invalid XSD groups.xsd
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("groups.xsd_",
                null,
                "BaseSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // recovearble errors
        Assert.assertTrue("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 7, 0, 4, 1);

        // Recovered Errors, Test for saving of the SOM - should fail
        Assert.assertFalse("Partial SOM " + baseSTS.getName() + "Save successful - should failed!",
                checkPSOMSave(baseSTS));

        // instance validation - should fail
        Assert.assertFalse("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), baseSTS));

        // verify types
        // named model groups
        Assert.assertEquals("Elem Type  should be 'ModelGrpType'",
                "ModelGrpType",
                getElementType(baseSTS, "ModelGrpTypeElem"));
        Assert.assertTrue("Elem Type  should be 'AttributeGroup'",
                getAttributeGroup(baseSTS, "AttributeGroup"));

        // Step 2: create a SOM with valid xsd
        SchemaTypeSystem modifiedSTS = createNewSTS("groups_added.xsd_",
                baseSTS,
                "BaseSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        // there should be NO recovearble errors
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 7, 0, 5, 2);

        // Test for saving of the SOM - should go thro
        Assert.assertTrue("SOM " + modifiedSTS.getName() + "Save failed!",
                checkPSOMSave(modifiedSTS));

        // instance validation - should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), modifiedSTS));

        // verify named model groups
        Assert.assertEquals("Elem Type  should be 'ModelGrpType'",
                "ModelGrpType",
                getElementType(modifiedSTS, "ModelGrpTypeElem"));
        Assert.assertTrue("Elem Type  should be 'AttributeGroup'",
                getAttributeGroup(modifiedSTS, "AttributeGroup"));


    }


    public void testAddSubstitutionGroups() throws Exception
    {
        System.out.println("Inside test case testAddSubstitutionGroups()");

        // step1: load an invalid PSOM by with incomplete/missing Subst Grp head elem definition
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("groups.xsd_",
                null,
                "BaseSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // errors recovered
        Assert.assertTrue("No Recovered Errors for recovered PSOM",
                printRecoveredErrors());

        // Recovered Errors, Test for saving of the SOM
        Assert.assertFalse("SOM " + baseSTS.getName() + "Save Success - should fail!",
                checkPSOMSave(baseSTS));

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 7, 0, 4, 1);

        // instance validation - should fail
        Assert.assertFalse("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), baseSTS));

        // verify types
        Assert.assertEquals("Elem Type  should be 'anyType'",
                anyType,
                getElementType(baseSTS, "SubGrpHeadElem"));
        Assert.assertEquals("Elem Type  should be 'anyType' (Member of Sub. Group)",
                anyType,
                getElementType(baseSTS, "SubGrpMemberElem1"));
        Assert.assertEquals("Elem Type  should be 'ExtensionSubGrpHeadElemType' (base)",
                "ExtensionSubGrpHeadElemType",
                getElementType(baseSTS, "SubGrpMemberElem2"));

        // named model groups
        Assert.assertEquals("Elem Type  should be 'ModelGrpType'",
                "ModelGrpType",
                getElementType(baseSTS, "ModelGrpTypeElem"));
        Assert.assertTrue("Elem Type  should be 'AttributeGroup'",
                getAttributeGroup(baseSTS, "AttributeGroup"));

        // Step 2: create a valid SOM and add to these
        SchemaTypeSystem modifiedSTS = createNewSTS("groups_added.xsd_",
                baseSTS,
                "ModifiedSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        // there should be NO recovearble errors
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 7, 0, 5, 2);

        // Test for saving of the SOM - should go thro
        Assert.assertTrue("SOM " + modifiedSTS.getName() + "Save failed!",
                checkPSOMSave(modifiedSTS));

        // instance validation - should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), modifiedSTS));

        // verify types - substitution groups
        Assert.assertEquals("Elem Type  should be 'SubGrpHeadElemType' (base)",
                "SubGrpHeadElemType",
                getElementType(modifiedSTS, "SubGrpHeadElem"));
        Assert.assertEquals("Elem Type  should be 'SubGrpHeadElemType' (derived)",
                "SubGrpHeadElemType",
                getElementType(modifiedSTS, "SubGrpMemberElem1"));
        Assert.assertEquals("Elem Type  should be 'ExtensionSubGrpHeadElemType' (base)",
                "ExtensionSubGrpHeadElemType",
                getElementType(modifiedSTS, "SubGrpMemberElem2"));

        // named model groups - moved to check in test
        //Assert.assertEquals("Elem Type  should be 'ModelGrpType'", "ModelGrpType", getElementType(baseSTS, "ModelGrpTypeElem"));
        //Assert.assertEquals("Elem Type  should be 'AttributeGroup'", "AttributeGroup", getAttributeGroup(baseSTS,"AttributeGroup"));
    }



    public void testDeleteSubstitutionGroups() throws Exception
    {
        System.out.println("Inside test case testDeleteSubstitutionGroups()");

        // Step 1: read in a clean XSD groups_added.xsd
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("groups_added.xsd_",
                null,
                "BaseSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // there should be NO recovearble errors
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 7, 0, 5, 2);

        // Test for saving of the SOM - should go thro
        Assert.assertTrue("SOM " + baseSTS.getName() + "Save failed!",
                checkPSOMSave(baseSTS));

        // instance validation - should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), baseSTS));

        // verify types - substitution groups
        Assert.assertEquals("Elem Type  should be 'SubGrpHeadElemType' (base)",
                "SubGrpHeadElemType",
                getElementType(baseSTS, "SubGrpHeadElem"));
        Assert.assertEquals("Elem Type  should be 'SubGrpHeadElemType' (derived)",
                "SubGrpHeadElemType",
                getElementType(baseSTS, "SubGrpMemberElem1"));
        Assert.assertEquals("Elem Type  should be 'ExtensionSubGrpHeadElemType' (base)",
                "ExtensionSubGrpHeadElemType",
                getElementType(baseSTS, "SubGrpMemberElem2"));

        // named model groups
        //Assert.assertEquals("Elem Type  should be 'ModelGrpType'", "ModelGrpType", getElementType(baseSTS, "ModelGrpTypeElem"));
        //Assert.assertEquals("Elem Type  should be 'AttributeGroup'", "AttributeGroup", getAttributeGroup(baseSTS,"AttributeGroup"));

        // step2: load an invalid PSOM by deleting the Subst Grp head elem definition
        SchemaTypeSystem modifiedSTS = createNewSTS("groups.xsd_",
                baseSTS,
                "ModifiedSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        Assert.assertTrue("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // Recovered Errors, Test for saving of the SOM
        Assert.assertFalse("SOM " + modifiedSTS.getName() + "Save Success - should fail!",
                checkPSOMSave(modifiedSTS));

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 7, 0, 4, 1);

        // instance validation - should fail
        Assert.assertFalse("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), modifiedSTS));

        // verify types
        Assert.assertEquals("Elem Type  should be 'anyType'",
                anyType,
                getElementType(modifiedSTS, "SubGrpHeadElem"));
        Assert.assertEquals("Elem Type  should be 'anyType' (Member of Sub. Group)",
                anyType,
                getElementType(modifiedSTS, "SubGrpMemberElem1"));
        Assert.assertEquals("Elem Type  should be 'ExtensionSubGrpHeadElemType' (base)",
                "ExtensionSubGrpHeadElemType",
                getElementType(modifiedSTS, "SubGrpMemberElem2"));

        // named model groups
        Assert.assertEquals("Elem Type  should be 'ModelGrpType'",
                "ModelGrpType",
                getElementType(modifiedSTS, "ModelGrpTypeElem"));
        Assert.assertTrue("Elem Type  should be 'AttributeGroup'",
                getAttributeGroup(modifiedSTS, "AttributeGroup"));

        // step 3: create a PSOM with the original xsd
        SchemaTypeSystem finalSTS = createNewSTS("groups_added.xsd_",
                modifiedSTS,
                "FinalSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", finalSTS);

        // Test for saving of the SOM - should go thro
        Assert.assertEquals("SOM " + finalSTS.getName() + "Save failed!",
                true,
                checkPSOMSave(finalSTS));

        // instance validation - should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), finalSTS));

        // verify types
        Assert.assertEquals("Elem Type  should be 'SubGrpHeadElemType' (base)",
                "SubGrpHeadElemType",
                getElementType(finalSTS, "SubGrpHeadElem"));
        Assert.assertEquals("Elem Type  should be 'SubGrpHeadElemType' (derived)",
                "SubGrpHeadElemType",
                getElementType(finalSTS, "SubGrpMemberElem1"));
        Assert.assertEquals("Elem Type  should be 'ExtensionSubGrpHeadElemType' (base)",
                "ExtensionSubGrpHeadElemType",
                getElementType(finalSTS, "SubGrpMemberElem2"));

        // named model groups
        //Assert.assertEquals("Elem Type  should be 'ModelGrpType'", "ModelGrpType", getElementType(baseSTS, "ModelGrpTypeElem"));
        //Assert.assertEquals("Elem Type  should be 'AttributeGroup'", "AttributeGroup", getAttributeGroup(baseSTS,"AttributeGroup"));

        // compare this to the original schema here
        Assert.assertTrue(compareSavedSOMs("BaseSchemaTS","FinalSchemaTS"));
    }

    public void testModifySubstitutionGroups() throws Exception
    {
        System.out.println("Inside test case testModifySubstitutionGroups()");

        // Step 1: read in a clean XSD groups_added.xsd
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("groups_added.xsd_",
                null,
                "BaseSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // there should be NO recovearble errors
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 7, 0, 5, 2);

        // Test for saving of the SOM - should go thro
        Assert.assertTrue("SOM " + baseSTS.getName() + "Save failed!",
                checkPSOMSave(baseSTS));

        // instance validation - should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), baseSTS));

        // verify types
        Assert.assertEquals("Elem Type  should be 'SubGrpHeadElemType' (base)",
                "SubGrpHeadElemType",
                getElementType(baseSTS, "SubGrpHeadElem"));
        Assert.assertEquals("Elem Type  should be 'SubGrpHeadElemType' (derived)",
                "SubGrpHeadElemType",
                getElementType(baseSTS, "SubGrpMemberElem1"));
        Assert.assertEquals("Elem Type  should be 'ExtensionSubGrpHeadElemType' (base)",
                "ExtensionSubGrpHeadElemType",
                getElementType(baseSTS, "SubGrpMemberElem2"));

        // step2: load a modified xsd with type of head elem in subs grp changed
        SchemaTypeSystem modifiedSTS = createNewSTS("groups_modified.xsd_",
                baseSTS,
                "ModifiedSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        // Recovered Errors, Test for saving of the SOM    - still a valid PSOM
        Assert.assertTrue("SOM " + modifiedSTS.getName() + "Save Success - should fail!",
                checkPSOMSave(modifiedSTS));

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 5, 0, 3, 0);

        // instance validation - should fail
        Assert.assertFalse("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), modifiedSTS));

        // verify types
        Assert.assertEquals("Elem Type  should be 'SubGrpHeadElemType2'",
                "SubGrpHeadElemType2",
                getElementType(modifiedSTS, "SubGrpHeadElem"));
        Assert.assertEquals("Elem Type  should be 'SubGrpHeadElemType2' (derived)",
                "SubGrpHeadElemType2",
                getElementType(modifiedSTS, "SubGrpMemberElem1"));
        Assert.assertEquals("Elem Type  should be 'ExtensionSubGrpHeadElemType' (base)",
                "ExtensionSubGrpHeadElemType",
                getElementType(modifiedSTS, "SubGrpMemberElem2"));

        // step3 : reload the original xsd
        SchemaTypeSystem finalSTS = createNewSTS("groups_added.xsd_",
                modifiedSTS,
                "FinalSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", finalSTS);

        // Test for saving of the SOM - should go thro
        Assert.assertTrue("SOM " + finalSTS.getName() + "Save failed!",
                checkPSOMSave(finalSTS));

        // instance validation - should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_subst_grps_valid.xml"), finalSTS));

        // the tests - Walk thro the valid SOM
        inspectSOM(finalSTS, 7, 0, 5, 2);

        // verify types
        Assert.assertEquals("Elem Type  should be 'SubGrpHeadElemType' (base)",
                "SubGrpHeadElemType",
                getElementType(finalSTS, "SubGrpHeadElem"));
        Assert.assertEquals("Elem Type  should be 'SubGrpHeadElemType' (derived)",
                "SubGrpHeadElemType",
                getElementType(finalSTS, "SubGrpMemberElem1"));
        Assert.assertEquals("Elem Type  should be 'ExtensionSubGrpHeadElemType' (base)",
                "ExtensionSubGrpHeadElemType", getElementType(finalSTS, "SubGrpMemberElem2"));

        // compare this to the original schema here
        Assert.assertTrue(compareSavedSOMs("BaseSchemaTS","FinalSchemaTS"));
    }

    public void testModifyIdConstraints() throws Exception
    {
        System.out.println("Inside test case testModifyIdConstraints()");

        // Step 1: read in a clean XSD groups_added.xsd
        String sBaseSourceName = "testsourcename";
        SchemaTypeSystem baseSTS = createNewSTS("constraints_added.xsd_",
                null,
                "BaseSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", baseSTS);

        // there should be NO recovearble errors
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(baseSTS, 5, 0, 2, 0);

        Assert.assertTrue("Constraint 'uniqueConstraint' should be found",lookForIdentityConstraint(baseSTS,"uniqueConstraint"));
        Assert.assertTrue("Constraint 'keyConstraint' should be found",lookForIdentityConstraint(baseSTS,"keyConstraint"));
        Assert.assertTrue("Constraint 'KeyRefConstraint' should be found",lookForIdentityConstraint(baseSTS,"KeyRefConstraint"));

        // Test for saving of the SOM - should go thro
        Assert.assertTrue("SOM " + baseSTS.getName() + "Save failed!",
                checkPSOMSave(baseSTS));

        // instance validation against valid instance- should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_constraints_valid.xml"), baseSTS));

        // validation against instance which violates the Constraints - should fail
        Assert.assertFalse("Validation against invalid should fail",
                validateInstance(getTestCaseFile("instance_constraints_invalid.xml"), baseSTS));

        // Step 2: create an incremental PSOM with the constraint commented out
        // Note: Partial SOMs cannot be created for Unique/Key constraints. They generate valid complete SOMs.
        // The xsd includes these but the invalid SOM in this case is from a keyref definition referring to a
        // non existant key

        SchemaTypeSystem modifiedSTS = createNewSTS("constraints.xsd_",
                baseSTS,
                "ModifiedSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", modifiedSTS);

        // recovearble errors
        Assert.assertTrue("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // Recovered Errors, Test for saving of the SOM
        Assert.assertFalse("valid PSOM " + modifiedSTS.getName() + "Save failed !",
                checkPSOMSave(modifiedSTS));

        // the tests - Walk thro the valid SOM
        inspectSOM(modifiedSTS, 5, 0, 2, 0);

        // instance validation - should fail
        Assert.assertFalse("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_constraints_valid.xml"), modifiedSTS));

        // Invalid instance validation - should fail bcos of Unique constraint definition missing
        Assert.assertFalse("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_constraints_invalid.xml"), modifiedSTS));

        Assert.assertFalse("KeyRef 'KeyRefConstraint' should not be resolved",
                lookForIdentityConstraint(modifiedSTS, "KeyConstraint"));

        // Step 3 : recreate SOM in first step and compare it
        SchemaTypeSystem finalSTS = createNewSTS("constraints_added.xsd_",
                modifiedSTS,
                "FinalSchemaTS",
                sBaseSourceName);
        Assert.assertNotNull("Schema Type System created is Null.", finalSTS);

        // there should be NO recovearble errors
        Assert.assertFalse("Recovered Errors for Valid Schema",
                printRecoveredErrors());

        // the tests - Walk thro the valid SOM
        inspectSOM(finalSTS, 5, 0, 2, 0);

        // Test for saving of the SOM - should go thro
        Assert.assertTrue("SOM " + finalSTS.getName() + "Save failed!",
                checkPSOMSave(finalSTS));

        // instance validation against valid instance- should be ok
        Assert.assertTrue("Validation against instance failed",
                validateInstance(getTestCaseFile("instance_constraints_valid.xml"), finalSTS));

        // compare this to the original schema here
        Assert.assertTrue(compareSavedSOMs("BaseSchemaTS","FinalSchemaTS"));
    }

}




