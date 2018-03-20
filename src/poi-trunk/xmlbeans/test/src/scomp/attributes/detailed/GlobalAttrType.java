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
package scomp.attributes.detailed;

import scomp.common.BaseCase;
import org.apache.xmlbeans.*;
import xbean.scomp.attribute.globalAttrType.GlobalAttrTypeDocDocument;
import xbean.scomp.attribute.globalAttrType.GlobalAttrTypeT;

import java.util.ArrayList;
import java.math.BigInteger;

/**
 *
 *
 *
 */
public class GlobalAttrType extends BaseCase {
    public void testAllValid() throws Throwable {
        GlobalAttrTypeT testDoc =
                GlobalAttrTypeDocDocument.Factory.parse("<pre:GlobalAttrTypeDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrType\" " +
                "pre:attSimple=\"XBeanAttrStr\" " +
                "pre:attAnyType=\" 1 \" " +
                "pre:attAnonymous=\" 1 \" />").getGlobalAttrTypeDoc();
        try {
            assertTrue(testDoc.validate(validateOptions));
        }
        catch (Throwable t) {
          showErrors();
          throw t;
        }

        assertTrue(testDoc.isSetAttSimple());
        assertEquals("XBeanAttrStr", testDoc.getAttSimple());
        assertEquals(" 1 ", testDoc.getAttAnyType().getStringValue());
        assertEquals(1, testDoc.getAttAnonymous().intValue());
    }

    /**
     * This should awlays be valid
     */
    public void testAnyType() throws Throwable {
        GlobalAttrTypeT testDoc =
                GlobalAttrTypeDocDocument.Factory.parse("<pre:GlobalAttrTypeDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrType\" " +
                " pre:attAnyType=\" 1 \" " +
                " />").getGlobalAttrTypeDoc();

        try {
            assertTrue(testDoc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

        assertEquals(" 1 ", testDoc.getAttAnyType().getStringValue());
        try {
            assertTrue(testDoc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        XmlInteger ival = XmlInteger.Factory.newInstance();
        ival.setBigIntegerValue(BigInteger.ZERO);

        testDoc.setAttAnyType(ival);

       // assertEquals(BigInteger.ZERO,testDoc.getAttAnyType().changeType(XmlInteger.type));
assertEquals(BigInteger.ZERO.toString(),
        testDoc.getAttAnyType().getStringValue());

        try {
            assertTrue(testDoc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        XmlString sval = XmlString.Factory.newInstance();
        sval.setStringValue("foobar");
        testDoc.setAttAnyType(sval);
        assertEquals("foobar", testDoc.getAttAnyType().getStringValue());
        try {
            assertTrue(testDoc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

        XmlDouble fval = XmlDouble.Factory.newInstance();
        fval.setDoubleValue(-0.01);
        testDoc.setAttAnyType(fval);
        assertEquals("-0.01", testDoc.getAttAnyType().getStringValue());
        try {
            assertTrue(testDoc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

    public void testAnonType() throws Throwable {
        GlobalAttrTypeT testDoc =
                GlobalAttrTypeDocDocument.Factory
                .parse("<pre:GlobalAttrTypeDoc" +
                " xmlns:pre=\"http://xbean/scomp/attribute/GlobalAttrType\" " +
                "pre:attAnonymous=\" 1 \" " +
                " />").getGlobalAttrTypeDoc();
        try {
            assertTrue(testDoc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }

        assertEquals(1, testDoc.getAttAnonymous().intValue());

        testDoc.setAttAnonymous( BigInteger.ZERO );
        assertTrue( 0 == testDoc.getAttAnonymous().intValue());
        try {
            assertTrue(testDoc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }
}
