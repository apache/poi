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
package scomp.elements.detailed;

import scomp.common.BaseCase;
import xbean.scomp.element.any.AnyEltDocument;
import xbean.scomp.element.any.AnySimpleDocument;
import xbean.scomp.substGroup.wide.BusinessShirtType;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.XmlDate;

import java.math.BigInteger;
import java.util.GregorianCalendar;

/**
 *
 *
 */
public class AnyTest extends BaseCase {

    public void testAny() throws Throwable {
        AnyEltDocument doc = AnyEltDocument.Factory.newInstance();
        BusinessShirtType bst = BusinessShirtType.Factory.newInstance();
        bst.setName("shirt");
        bst.setNumber("SkU034");
        bst.setColor("white");
        bst.setSize(new BigInteger("10"));
        doc.setAnyElt(bst);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }


        XmlString val = XmlString.Factory.newInstance();
        val.setStringValue("foobar");
        doc.setAnyElt(val);

        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }


    }

    public void testAnySimple() throws Throwable {
        AnySimpleDocument doc =
                AnySimpleDocument.Factory.newInstance();
        XmlString str = XmlString.Factory.newInstance();
        str.setStringValue("foobar");
        doc.setAnySimple(str);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        XmlDate date = XmlDate.Factory.newInstance();
        date.setCalendarValue(new GregorianCalendar(2004, 8, 12));
        doc.setAnySimple(date);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

}
