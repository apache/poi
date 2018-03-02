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
import xbean.scomp.attribute.attributeGroup.AttGroupEltDocument;
import xbean.scomp.attribute.attributeGroup.GlobalT;
import org.apache.xmlbeans.*;

import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 *
 *
 */
public class AttrGroupTest extends BaseCase {
    public void testAttributeGroup() throws Throwable {
        AttGroupEltDocument doc = AttGroupEltDocument.Factory.newInstance();
        GlobalT elt = doc.addNewAttGroupElt();
        XmlObject obj = elt.addNewChild2();
        XmlString str = XmlString.Factory.newInstance();
        str.setStringValue("child2Elt");
        obj.set(str);
        XmlDecimal val = XmlDecimal.Factory.newInstance();
        elt.xsetGlobalAttr(val);

        elt.setVersion(new BigDecimal(new BigInteger("10")));
        elt.setGlobalAttr(new BigDecimal(BigInteger.ONE));
        //add a wildcard attr: ##other, lax
        XmlCursor cur = elt.newCursor();
        //move to document element
        cur.toNextToken();
        cur.insertAttribute(new QName("http://org.apache.sample", "attr",
                "pre"));
        String[] errExpected=new String[]{
            XmlErrorCodes
                .ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_REQUIRED_ATTRIBUTE};
        assertTrue( !doc.validate(validateOptions) );

       /*  assertTrue( compareErrorCodes(new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID
        }) ); */
        assertEquals(1, errorList.size());
        showErrors();
        assertTrue(compareErrorCodes(errExpected));


        System.out.println("*******");
        elt.setID("IdAttr");
        try {
            assertTrue(elt.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }


    }
}
