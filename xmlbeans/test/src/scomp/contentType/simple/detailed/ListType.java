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

package scomp.contentType.simple.detailed;

import scomp.common.BaseCase;
import xbean.scomp.contentType.list.*;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.apache.xmlbeans.impl.values.XmlValueNotSupportedException;
import org.apache.xmlbeans.XmlSimpleList;
import org.apache.xmlbeans.XmlErrorCodes;

/**
 *
 *
 *
 */
public class ListType extends BaseCase {
    public void testListTypeAnonymous() throws Throwable {
        ListEltTokenDocument doc =
                ListEltTokenDocument.Factory.newInstance();
        assertEquals(null, doc.getListEltToken());
        List values = new LinkedList();
        values.add("lstsmall");
        values.add("lstmedium");
        doc.setListEltToken(values);
        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
        values.set(0, new Integer(4));

        // since the list has enumerations, it contains a fixed number of Java constants in the xobj
        // which are checked for types and an exception is expected irrespective of validateOnSet XmlOption
        // if the value being set is not one of them
        boolean vneThrown = false;
        try{
        doc.setListEltToken(values);
        }
        catch(XmlValueNotSupportedException vne){
            vneThrown = true;
        }
        finally{
            if(!vneThrown)
                fail("Expected XmlValueOutOfRangeException here");
        }

    }

    public void testListTypeGlobal() throws Throwable {
        String input =
                "<ListEltInt xmlns=\"http://xbean/scomp/contentType/List\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >" +
                "-1 -3" +
                "</ListEltInt>";
        ListEltIntDocument doc =
                ListEltIntDocument.Factory.parse(input);
        List result = doc.getListEltInt();
        assertEquals(-1, ((Integer) result.get(0)).intValue());
        assertEquals(-3, ((Integer) result.get(1)).intValue());
        GlobalSimpleT gst = GlobalSimpleT.Factory.newInstance();
        try {
            result.set(0, "foobar");
        }
        catch (UnsupportedOperationException e) {
            //immutable list
            assertTrue(result instanceof XmlSimpleList);
        }
        List arrayList = new ArrayList();
        arrayList.add("foobar");
        List newList = new XmlSimpleList(arrayList);
        gst.setListValue(newList);
        doc.xsetListEltInt(gst);
         try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

    public void testListofLists() {
        //also,a list of union that contains a list is not OK
        fail("Compile Time eror");
    }

    /**
     * values should be in [small,medium,large,1-3,-3,-2,-1]
     */
    public void testListofUnions() throws Throwable {
        ListUnionDocument doc =
                ListUnionDocument.Factory.newInstance();
        List arrayList = new ArrayList();
        arrayList.add("small");
        arrayList.add("large");
        arrayList.add(new Integer(-1));
        arrayList.add(new Integer(2));
        doc.setListUnion(arrayList);

        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }

    public void testListofUnionsIllegal() throws Throwable {
        String input =
                "<ListUnion xmlns=\"http://xbean/scomp/contentType/List\"" +
                " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >" +
                "small -3 11" +
                "</ListUnion>";
        ListUnionDocument doc =
                ListUnionDocument.Factory.parse(input);
        assertTrue(!doc.validate(validateOptions));
        showErrors();
        String[] errExpected = new String[]{
            XmlErrorCodes.DATATYPE_VALID$UNION
        };
        assertTrue(compareErrorCodes(errExpected));

    }


    public void testListofUnions2() throws Throwable {
        ListUnion2Document doc =
                ListUnion2Document.Factory.newInstance();
        List arrayList = new ArrayList();
        arrayList.add("small");
        arrayList.add("large");
        arrayList.add(new Integer(-1));
        arrayList.add(new Integer(2));
        arrayList.add("addVal1");
        arrayList.add("addVal2");
        arrayList.add("addVal3");
        doc.setListUnion2(arrayList);

        try {
            assertTrue(doc.validate(validateOptions));
        }
        catch (Throwable t) {
            showErrors();
            throw t;
        }
    }
}
