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

package xmlobject.detailed;


import org.openuri.test.selectChildren.*;

import org.apache.xmlbeans.*;

import javax.xml.namespace.QName;
import java.util.*;

import xmlobject.common.SelectChildrenAttribCommon;

/**
 *
 *
 */
public class SelectChildrenTests
        extends SelectChildrenAttribCommon
{
    public SelectChildrenTests(String name)
    {
        super(name);
    }


    static String scUri = "http://openuri.org/test/selectChildren";
    static String scStartFrag = "<xm xmlns:sc=\"" + scUri + "\">";

    static String abcUri = "http://abc";
    static String defUri = "http://def";
    static String xyzUri = "http://xyz";

    static String anyStartFrag = "<xm xmlns:sc=\"" + scUri + "\"" +
                                 " xmlns:abc=\"" + abcUri + "\"" +
                                 " xmlns:def=\"" + defUri + "\"" +
                                 " xmlns:xyz=\"" + xyzUri + "\"" + ">";

    static String endFrag = "</xm>";

    //////////////////////////////////////////////////////////////////
    // Tests
    public void testSelectWithQName()
        throws Exception
    {
        String xml = getXml("xbean/xmlobject/SelectChildren-NormalDoc.xml");

        XmlObject[] xos; // For the return from selectChildren
        String[] exps;   // For the expected xml strings

        NormalDocument doc = NormalDocument.Factory.parse(xml);
        assertTrue(doc.validate());
        NormalType norm =  doc.getNormal();

        exps = new String[]{scStartFrag + "first element" + endFrag};
        xos = norm.selectChildren(new QName(scUri, "first"));

        this.validateTest("testSelectWithQName", exps, xos);
    }


    public void testSelectWithURI()
        throws Exception
    {
        String xml = getXml("xbean/xmlobject/SelectChildren-NormalDoc.xml");

        XmlObject[] xos; // For the return from selectChildren
        String[] exps;   // For the expected xml strings

        NormalDocument doc = NormalDocument.Factory.parse(xml);
        assertTrue(doc.validate());
        NormalType norm =  doc.getNormal();

        exps = new String[]{scStartFrag + "second element" + endFrag};
        xos = norm.selectChildren(scUri, "second");

        this.validateTest("testSelectWithURI", exps, xos);
    }

    public void testSelectWithQNameSet()
        throws Exception
    {
        String xml = getXml("xbean/xmlobject/SelectChildren-NormalDoc.xml");

        XmlObject[] xos; // For the return from selectChildren
        String[] exps;   // For the expected xml strings

        NormalDocument doc = NormalDocument.Factory.parse(xml);
        assertTrue(doc.validate());
        NormalType norm =  doc.getNormal();

        QName[] qArr = new QName[] { new QName(scUri, "first"),
                                     new QName(scUri, "numbers"),
                                     new QName(scUri, "second")};

        QNameSet qSet = QNameSet.forArray(qArr);

        exps = new String[]{scStartFrag + "first element" + endFrag,
                            scStartFrag + "second element" + endFrag,
                            scStartFrag + "10" + endFrag,
                            scStartFrag + "11" + endFrag,
                            scStartFrag + "12" + endFrag };

        xos = norm.selectChildren(qSet);

        this.validateTest("testSelectWithQNameSet", exps, xos);
    }

    //////////////////////////////////////////////////////////////////////
    // Tests with 'any' Element
    public void testSelectWithQNameForAny()
        throws Exception
    {
        XmlObject[] xos; // For the return from selectChildren
        String[] exps;   // For the expected xml strings

        String xml = getXml("xbean/xmlobject/SelectChildren-AnyTypeDoc.xml");
        ElemWithAnyDocument doc = ElemWithAnyDocument.Factory.parse(xml);
        assertTrue(doc.validate());

        WithAnyType any = doc.getElemWithAny();
        // Select children from a known namespace
        xos = any.selectChildren(new QName(defUri, "someElem2"));
        exps = new String[]{anyStartFrag + "DEF Namespace" + endFrag };

        validateTest("testSelectWithQNameForAny", exps, xos);
    }

    public void testSelectWithURIForAny()
        throws Exception
    {
        XmlObject[] xos; // For the return from selectChildren
        String[] exps;   // For the expected xml strings

        String xml = getXml("xbean/xmlobject/SelectChildren-AnyTypeDoc.xml");
        ElemWithAnyDocument doc = ElemWithAnyDocument.Factory.parse(xml);
        assertTrue(doc.validate());

        WithAnyType any = doc.getElemWithAny();
        // Select children from a known namespace
        xos = any.selectChildren(scUri, "simple");
        exps = new String[]{anyStartFrag + "Simple String" + endFrag };

        validateTest("testSelectWithURIForAny", exps, xos);
    }

    public void testSelectWithWildcard()
        throws Exception
    {
        XmlObject[] xos; // For the return from selectChildren
        String[] exps;   // For the expected xml strings
        String xml = getXml("xbean/xmlobject/SelectChildren-AnyTypeDoc.xml");
        ElemWithAnyDocument doc = ElemWithAnyDocument.Factory.parse(xml);
        assertTrue(doc.validate());

        WithAnyType any = doc.getElemWithAny();

        xos = any.selectChildren(QNameSet.forWildcardNamespaceString("##other",
                                                                     scUri));
        exps = new String[]{anyStartFrag + "ABC Namespace" + endFrag,
                            anyStartFrag + "DEF Namespace" + endFrag,
                            anyStartFrag + "XYX Namespace" + endFrag,
                            anyStartFrag + "ABC-SameElem" + endFrag,
                            anyStartFrag + "DEF-SameElem" + endFrag,
                            anyStartFrag + "XYZ-SameElem" + endFrag};

        validateTest("testSelectWithWildcard", exps, xos);
    }

    public void testSelectWithQNameBuilder()
        throws Exception
    {
        XmlObject[] xos; // For the return from selectChildren
        String[] exps;   // For the expected xml strings
        String xml = getXml("xbean/xmlobject/SelectChildren-AnyTypeDoc.xml");
        ElemWithAnyDocument doc = ElemWithAnyDocument.Factory.parse(xml);
        assertTrue(doc.validate());

        WithAnyType any = doc.getElemWithAny();

        Set excFromIncSet = new HashSet();
        excFromIncSet.add(new QName(scUri, "simple"));

        Set excSet = new HashSet();
        excSet.add(xyzUri);

        Set incFromExcSet = new HashSet();
        incFromExcSet.add(new QName(xyzUri, "sameElem"));

        QNameSet qset = new QNameSetBuilder(excSet,
                                            null,
                                            excFromIncSet,
                                            incFromExcSet).toQNameSet();
        xos = any.selectChildren(qset);

        for (int i =0; i < xos.length; i++)
            System.out.println(xos[i].xmlText());

    }


}
