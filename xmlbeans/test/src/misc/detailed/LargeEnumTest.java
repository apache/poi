package misc.detailed;

import org.apache.xmlbeans.*;

import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.Assert;
import xmlbeans307.*;

/**
 * This test was put together for:
 * http://issues.apache.org/jira/browse/XMLBEANS-307
 * XMLBeans scomp throws error "code too large"
 */
public class LargeEnumTest extends TestCase {

    public LargeEnumTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(LargeEnumTest.class);
    }

    /**
     * These are tests for a enumeration type
     *
     * @throws Exception
     */
    public void testEnumCount_closeToMax() throws Exception {
        SchemaType mType = MaxAllowedEnumType.type;
        assertTrue("Enumeration SchemaType was null", mType.getEnumerationValues() != null);
        assertTrue("EnumerationValue was not 3665 as expected was" + mType.getEnumerationValues().length,
                mType.getEnumerationValues().length == 3665);

        SchemaType mElem = MaxAllowedElementDocument.type;
        assertTrue("Enumeration SchemaType was null", mElem.getEnumerationValues() == null);

        // Test that the Java type associated to this is an enum type
        assertNotNull("This type does not correspond to a Java enumeration", mType.getStringEnumEntries());
    }


    public void testEnumCount_greaterThanMax() throws Exception {
        // TODO: verify if any xpath/xquery issues 
        SchemaType mType = MoreThanAllowedEnumType.type;

        assertNotNull("Enumeration should be null as type should be base type " + mType.getEnumerationValues(),
                mType.getEnumerationValues());
        assertTrue("EnumerationValue was not 3678 as expected was " + mType.getEnumerationValues().length,
                mType.getEnumerationValues().length == 3678);
        System.out.println("GET BASE TYPE: " + mType.getBaseType());
        System.out.println("GET BASE TYPE: " + mType.getPrimitiveType());
        assertTrue("type should have been base type, was " + mType.getBaseType(),
                mType.getBaseType().getBuiltinTypeCode() == XmlToken.type.getBuiltinTypeCode());

        SchemaType mElem = GlobalMoreThanElementDocument.type;
        assertTrue("Enumeration SchemaType was null", mElem.getBaseEnumType() == null);

        // Test that the Java type associated to this is not an enum type
        assertNull("This type corresponds to a Java enumeration, even though it has too many enumeration values",
            mType.getStringEnumEntries());
    }

    public void testEnumCount_validate_invalid_enum() throws Exception {
        MoreThanAllowedEnumType mType = MoreThanAllowedEnumType.Factory.newInstance();

        //This value dos not exist in the enumeration set
        mType.setStringValue("12345AAA");
        ArrayList errors = new ArrayList();
        XmlOptions options = (new XmlOptions()).setErrorListener(errors);
        mType.validate(options);
        XmlError[] xErr = new XmlError[errors.size()];
        for (int i = 0; i < errors.size(); i++) {
            System.out.println("ERROR: " + errors.get(i));
            xErr[i] = (XmlError)errors.get(i);
        }

        Assert.assertTrue("NO Expected Errors after validating enumType after set", errors.size() ==1 );
        Assert.assertTrue("Expected ERROR CODE was not as expected",
                xErr[0].getErrorCode().compareTo("cvc-enumeration-valid") ==0 );
        // string value '12345AAA' is not a valid enumeration value for MoreThanAllowedEnumType in
    }


    public void test_MoreEnum_Operations() throws Exception {
        MoreThanAllowedEnumType mType = MoreThanAllowedEnumType.Factory.newInstance();

        mType.setStringValue("AAA");
        ArrayList errors = new ArrayList();
        XmlOptions options = (new XmlOptions()).setErrorListener(errors);
        mType.validate(options);

        for (int i = 0; i < errors.size(); i++) {
            System.out.println("ERROR: " + errors.get(i));
        }
        Assert.assertTrue("There were errors validating enumType after set", errors.size() == 0);

        GlobalMoreThanElementDocument mDoc = GlobalMoreThanElementDocument.Factory.newInstance();
        mDoc.setGlobalMoreThanElement("AAA");
        errors = null;
        errors = new ArrayList();
        options = (new XmlOptions()).setErrorListener(errors);
        mDoc.validate(options);

        for (int i = 0; i < errors.size(); i++) {
            System.out.println("ERROR: " + errors.get(i));
        }

        Assert.assertTrue("There were errors validating enumDoc after set", errors.size() == 0);

        MoreThanAllowedComplexType mcType = MoreThanAllowedComplexType.Factory.newInstance();
        mcType.setComplexTypeMoreThanEnum("AAA");
        mcType.setSimpleString("This should work");
        errors = null;
        errors = new ArrayList();
        mcType.validate(options);
        for (int i = 0; i < errors.size(); i++) {
            System.out.println("ERROR: " + errors.get(i));
        }

        Assert.assertTrue("There were errors validating complxType after set", errors.size() == 0);
    }


}
