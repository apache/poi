package org.apache.poi.poifs.property;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for org.apache.poi.poifs.property<br/>
 * 
 * @author Josh Micich
 */
public final class AllPOIFSPropertyTests {

    public static Test suite() {
        TestSuite result = new TestSuite("Tests for org.apache.poi.poifs.property");
        result.addTestSuite(TestDirectoryProperty.class);
        result.addTestSuite(TestDocumentProperty.class);
        result.addTestSuite(TestPropertyFactory.class);
        result.addTestSuite(TestPropertyTable.class);
        result.addTestSuite(TestRootProperty.class);
        return result;
    }
}
