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
package scomp.contentType.complex.modelGroup.detailed;

import xbean.scomp.contentType.modelGroup.SequenceEltDocument;
import xbean.scomp.contentType.modelGroup.SequenceT;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.XmlErrorCodes;
import scomp.common.BaseCase;

import java.math.BigInteger;

/**
 *
 *
 *
 */
public class SequenceTest extends BaseCase {

    public void testWrongOrder() throws Throwable {
        SequenceEltDocument doc = SequenceEltDocument.Factory
                .parse("<foo:SequenceElt xmlns:foo=\"http://xbean/scomp/contentType/ModelGroup\">" +
                "<child1>1</child1>" +
                "<child3>2</child3>" +
                "<child2>Foobar</child2>" +
                "</foo:SequenceElt>   ");

        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT};
        assertTrue(compareErrorCodes(errExpected));


    }

    public void testWrongCardinality() {
        SequenceEltDocument doc = SequenceEltDocument.Factory.newInstance();
        SequenceT elt = doc.addNewSequenceElt();
        XmlString valueStr = XmlString.Factory.newInstance();
        valueStr.setStringValue("foobar");
        BigInteger valueInt = new BigInteger("-3");
        elt.xsetChild2Array(new XmlString[]{});
        elt.setChild3Array(new BigInteger[]{valueInt});
        elt.addChild3(valueInt);
        elt.setChild3Array(1, new BigInteger("10"));
        assertEquals("<xml-fragment><child3>-3</child3>" +
                "<child3>10</child3></xml-fragment>", elt.xmlText());
        assertTrue(!elt.validate(validateOptions));
        assertEquals(3, errorList.size());

        showErrors();
        String[] errExpected = new String[]{
             XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT,
              XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$EXPECTED_DIFFERENT_ELEMENT,
             XmlErrorCodes.ELEM_COMPLEX_TYPE_LOCALLY_VALID$MISSING_ELEMENT
        };
        assertTrue(compareErrorCodes(errExpected));

    }
}
