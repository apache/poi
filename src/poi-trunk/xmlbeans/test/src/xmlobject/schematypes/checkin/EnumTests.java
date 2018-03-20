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

package xmlobject.schematypes.checkin;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import com.enumtest.StatusreportDocument;
import com.enumtest.SalesreportDocument;
import com.enumtest.SalesreportDocument.Salesreport.Unit;
import com.enumtest.Quantity;
import com.enumtest.Data;

import tools.util.*;


public class EnumTests extends TestCase
{
    public EnumTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(EnumTests.class); }

    public static void testReport1() throws Exception
    {
        StatusreportDocument doc = (StatusreportDocument)
                    XmlObject.Factory.parse(
                            JarUtil.getResourceFromJarasFile(
                                    "xbean/xmlobject/enumtest.xml"));

        Quantity.Enum[] contents = new Quantity.Enum[]
        {
            Quantity.ALL,
            Quantity.ALL,
            Quantity.FEW,
            Quantity.ALL,
            Quantity.MOST,
            Quantity.NONE,
            Quantity.NONE,
            Quantity.NONE,
        };
        Data[] data = doc.getStatusreport().getStatusArray();
        int t = 0;
        for (int i = 0; i < data.length; i++)
        {
            Assert.assertEquals(contents[t++], data[i].enumValue());
            // System.out.println("Target: " + data[i].getTarget() + ", value: " + data[i].enumValue());
            Assert.assertEquals(contents[t++], data[i].getTarget());
        }
    }

    public static void testReport2() throws Exception
    {
        StatusreportDocument doc = StatusreportDocument.Factory.newInstance();
        StatusreportDocument.Statusreport report = doc.addNewStatusreport();

        Data d = report.addNewStatus();
        d.set(Quantity.ALL);
        d.setTarget(Quantity.ALL);

        d = report.addNewStatus();
        d.set(Quantity.FEW);
        d.setTarget(Quantity.ALL);

        d = report.addNewStatus();
        d.set(Quantity.MOST);
        d.setTarget(Quantity.NONE);

        d = report.addNewStatus();
        d.set(Quantity.NONE);
        d.setTarget(Quantity.NONE);

        Quantity.Enum[] contents = new Quantity.Enum[]
        {
            Quantity.ALL,
            Quantity.ALL,
            Quantity.FEW,
            Quantity.ALL,
            Quantity.MOST,
            Quantity.NONE,
            Quantity.NONE,
            Quantity.NONE,
        };
        Data[] data = doc.getStatusreport().getStatusArray();
        int t = 0;
        for (int i = 0; i < data.length; i++)
        {
            Assert.assertEquals(contents[t++], data[i].enumValue());
            // System.out.println("Target: " + data[i].getTarget() + ", value: " + data[i].enumValue());
            Assert.assertEquals(contents[t++], data[i].getTarget());
        }
    }

    public static void testReport3() throws Exception
    {
        SalesreportDocument doc = SalesreportDocument.Factory.newInstance();
        SalesreportDocument.Salesreport report = doc.addNewSalesreport();

        report.addUnit(Unit.ONE);
        report.addUnit(Unit.TWO);
        report.addUnit(Unit.NINETY_NINE);
        report.addUnit(Unit.ONE_HUNDRED);

        Unit.Enum[] contents = new Unit.Enum[]
        {
            Unit.ONE,
            Unit.TWO,
            Unit.NINETY_NINE,
            Unit.ONE_HUNDRED,
        };

        Unit[] xunits = report.xgetUnitArray();
        for (int i = 0; i < xunits.length; i++)
        {
            Assert.assertEquals(contents[i], xunits[i].enumValue());
        }

        Unit.Enum[] units = report.getUnitArray();
        for (int i = 0; i < units.length; i++)
        {
            Assert.assertEquals(contents[i], units[i]);
        }
    }

    public static void testEnumRestriction() throws Exception
    {
        String schema =
            "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "    xmlns:tns=\"foo\" targetNamespace=\"foo\">\n" +
            "\n" +
            "    <xs:simpleType name=\"parent\">\n" +
            "        <xs:restriction base=\"xs:string\">\n" +
            "            <xs:enumeration value=\"abc\"/>\n" +
            "            <xs:enumeration value=\"123\"/>\n" +
            "        </xs:restriction>\n" +
            "    </xs:simpleType>\n" +
            "\n" +
            "    <xs:simpleType name=\"child\">\n" +
            "        <xs:restriction base=\"tns:parent\">\n" +
            "            <xs:enumeration value=\"abc\"/>\n" +
            "            <xs:enumeration value=\"123\"/>\n" +
                         // 'xyz' is an invalid enumeration value
            "            <xs:enumeration value=\"xyz\"/>\n" +
            "        </xs:restriction>\n" +
            "    </xs:simpleType>\n" +
            "\n" +
            "</xs:schema>\n";

        XmlObject xobj = XmlObject.Factory.parse(schema);
        //ArrayList errors = new ArrayList();
        //XmlOptions opts = new XmlOptions().setErrorListener(errors);

        try
        {
            XmlBeans.loadXsd(new XmlObject[]{xobj});
            fail("Expected loadXsd() to throw an exception.");
        }
        catch (XmlException xe)
        {
            //
            System.out.println("caught: " + xe);
        }
    }

}
