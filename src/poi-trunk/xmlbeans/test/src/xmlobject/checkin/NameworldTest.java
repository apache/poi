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

package xmlobject.checkin;

import org.openuri.nameworld.NameworldDocument;
import org.openuri.nameworld.NameworldDocument.Nameworld;
import org.openuri.nameworld.Loc;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlObject;

import java.io.File;

import javax.xml.namespace.QName;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;



import tools.util.*;


public class NameworldTest extends TestCase
{
    public NameworldTest(String name) { super(name); }
    public static Test suite() { return new TestSuite(NameworldTest.class); }

    public static void testWorld1() throws Exception
    {
        NameworldDocument doc = (NameworldDocument)
                    XmlObject.Factory.parse(
                                    JarUtil.getResourceFromJarasFile(
                                            "xbean/xmlobject/nameworld.xml"));

        Assert.assertEquals(new QName("http://openuri.org/nameworld", "nameworld"), doc.schemaType().getDocumentElementName());

        QName[] contents = new QName[]
        {
            new QName("http://bar.com/", "barcity"),
            new QName("http://foo.com/", "footown"),
            new QName("http://bar.com/", "barvillage"),
            new QName("http://bar.com/", "bartown"),
            new QName("http://foo.com/", "foovillage"),
            new QName("http://bar.com/", "barvillage"),
            new QName("http://foo.com/", "foocity"),
            new QName("http://bar.com/", "bartown"),
            new QName("http://foo.com/", "foovillage"),
            new QName("http://foo.com/", "footown"),
            new QName("http://bar.com/", "barvillage"),
            new QName("http://foo.com/", "foovillage"),
        };
        int t = 0;

        Nameworld world = doc.getNameworld();
        Nameworld.Island[] islands = world.getIslandArray();
        for (int i = 0; i < islands.length; i++)
        {
            Loc[] locs = islands[i].getLocationArray();
            for (int j = 0; j < locs.length; j++)
            {
                Loc.Reference[] refs = locs[j].getReferenceArray();
                for (int k = 0; k < refs.length; k++)
                {
                    Assert.assertEquals(contents[t++], refs[k].getTo());
                }
            }
        }
    }

    /*
    public static void testAccessByName() throws Exception
    {
        NameworldDocument doc = (NameworldDocument)
                    XmlLoader.Factory.parse(TestEnv.xbeanCase("xbean/xmlobject/nameworld.xml"),
                    NameworldDocument.type);
        String[] contents = new String[]
        {
            "http://foo.com/",
            "foocity",
            "footown",
            "foovillage",
            "http://bar.com/",
            "barcity",
            "bartown",
            "barvillage",
        };
        int t = 0;
        Nameworld world = (Nameworld)doc.elementByName(new Name("http://openuri.org/nameworld", "nameworld"), 0);
        for (int i = 0; i < world.countOfElementByName(new Name("http://openuri.org/nameworld", "island")); i++)
        {
            Nameworld.Island island = (Nameworld.Island)world.elementByName(new Name("http://openuri.org/nameworld", "island"), i);
            Assert.assertEquals(contents[t++], ((SimpleValue)island.attributeByName(new Name("targetNamespace"))).stringValue());
            for (int j = 0; j < island.countOfElementByName(new Name( "http://openuri.org/nameworld", "location")); j++)
            {
                Loc loc = (Loc)island.elementByName(new Name("http://openuri.org/nameworld", "location"), j);
                Assert.assertEquals(contents[t++], ((SimpleValue)loc.attributeByName(new Name("name"))).stringValue());
            }
        }
    }
    */

}
