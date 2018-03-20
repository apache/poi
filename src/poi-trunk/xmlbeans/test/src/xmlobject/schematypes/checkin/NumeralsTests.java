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


import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.openuri.testNumerals.DocDocument;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Nov 23, 2003
 */
public class NumeralsTests extends TestCase
{
    public NumeralsTests(String name) { super(name); }
    public static Test suite() { return new TestSuite(NumeralsTests.class); }

    private static DocDocument.Doc doc;
    static
    {
        String inst =
            "<doc xmlns='http://openuri.org/testNumerals'>\n" +
            "  <string>    this is a long string \n" +
            "  ...   </string>\n" +
            "  <int>\n" +
            "  		+5\n" +
            "  </int>\n" +
            "  <int>\n" +
            "  		-6\n" +
            "  </int>\n" +
            "  <int>\n" +
            "  		+00000000015\n" +
            "  </int>\n" +
            "  <int>7<!--this has to be a 77 int value-->7</int>\n" +
            "  <boolean>\n" +
            "  		true\n" +
            "  </boolean>\n" +
            "  <boolean>\n" +
            "  		false\n" +
            "  </boolean>\n" +
            "  <boolean>\n" +
            "  		0\n" +
            "  </boolean>\n" +
            "  <boolean>\n" +
            "  		1\n" +
            "  </boolean>\n" +
            "  <boolean>\n" +
            "  		true is not\n" +
            "  </boolean>\n" +
            "  <short>\n" +
            "  		+03\n" +
            "  </short>\n" +
            "  <byte>\n" +
            "  		+001\n" +
            "  </byte>\n" +
            "  <long>-0500000</long>\n" +
            "  <long>\n" +
            "    001\n" +
            "  </long>\n" +
            "  <long>\n" +
            "    +002\n" +
            "  </long>\n" +
            "  <double>\n" +
            "    +001\n" +
            "  </double>\n" +
            "  <double>\n" +
            "    -002.007000\n" +
            "  </double>\n" +
            "  <double>\n" +
            "    INF\n" +
            "  </double>\n" +
            "  <double>\n" +
            "    -INF\n" +
            "  </double>\n" +
            "  <double>\n" +
            "    NaN\n" +
            "  </double>\n" +
            "  <float>\n" +
            "    +12.325\n" +
            "  </float>\n" +
            "  <float>\n" +
            "    NaN\n" +
            "  </float>\n" +
            "  <float>\n" +
            "    INF\n" +
            "  </float>\n" +
            "  <float>\n" +
            "    -INF\n" +
            "  </float>\n" +
            "  <decimal>\n" +
            "    +001.001\n" +
            "  </decimal>\n" +
            "  <integer>\n" +
            "    +001<!--comments-->000000000\n" +
            "  </integer>\n" +
            "</doc>";

        try
        {
            doc = DocDocument.Factory.parse(inst).getDoc();
        }
        catch (XmlException e)
        {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public void test1() throws XmlException
    {
        String s = "    this is a long string \n" + "  ...   ";
        Assert.assertTrue("String expected:\n'" + s + "'         actual:\n'" +
            doc.getStringArray()[0] + "'", s.equals(doc.getStringArray()[0]));
    }

    public void test2() throws XmlException
    {
        Assert.assertTrue("int expected:" + 5 + " actual:" + doc.getIntArray()[0], doc.getIntArray()[0]==5);
    }

    public void test3() throws XmlException
    {
        Assert.assertTrue("int expected:" + (-6) + " actual:" + doc.getIntArray()[1], doc.getIntArray()[1]==-6);
    }

    public void test4() throws XmlException
    {
        Assert.assertTrue("int expected:" + 15 + " actual:" + doc.getIntArray()[2], doc.getIntArray()[2]==15);
    }

    public void test5() throws XmlException
    {
        Assert.assertTrue("int expected:" + 77 + " actual:" + doc.getIntArray()[3], doc.getIntArray()[3]==77);
    }

    public void test6() throws XmlException
    {
        Assert.assertTrue(doc.getBooleanArray(0)==true);
    }

    public void test7() throws XmlException
    {
        Assert.assertTrue(doc.getBooleanArray(1)==false);
    }

    public void test8() throws XmlException
    {
        Assert.assertTrue(doc.getBooleanArray(2)==false);
    }

    public void test9() throws XmlException
    {
        Assert.assertTrue(doc.getBooleanArray(3)==true);
    }

    public void test10() throws XmlException
    {
        try { boolean b = doc.getBooleanArray()[4]; Assert.assertTrue(false); }
        catch(XmlValueOutOfRangeException e)
        { Assert.assertTrue(true); }
    }

    public void test11() throws XmlException
    {
        Assert.assertTrue(doc.getShortArray()[0]==3);
    }

    public void test12() throws XmlException
    {
        Assert.assertTrue(doc.getByteArray()[0]==1);
    }

    public void test13() throws XmlException
    {
        Assert.assertTrue("long expected:" + (-50000) + " actual:" + doc.getLongArray()[0],
            doc.getLongArray()[0]==-500000);
    }

    public void test14() throws XmlException
    {
        Assert.assertTrue("long expected:" + 1 + " actual:" + doc.getLongArray()[1],
            doc.getLongArray()[1]==1);
    }

    public void test15() throws XmlException
    {
        Assert.assertTrue("long expected:" + 2 + " actual:" + doc.getLongArray()[2],
            doc.getLongArray()[2]==2);
    }

    public void test16() throws XmlException
    {
        Assert.assertTrue(doc.getDoubleArray()[0]==1);
    }

    public void test17() throws XmlException
    {
        Assert.assertTrue("double expected:" + -2.007d + " actual:" + doc.getDoubleArray()[1],
            doc.getDoubleArray()[1]==-2.007d);
    }

    public void test18() throws XmlException
    {
        Assert.assertTrue(new Double(Double.POSITIVE_INFINITY).
            equals(new Double(doc.getDoubleArray()[2])));
    }

    public void test19() throws XmlException
    {
        Assert.assertTrue(new Double(Double.NEGATIVE_INFINITY).
            equals(new Double(doc.getDoubleArray()[3])));
    }

    public void test20() throws XmlException
    {
        Assert.assertTrue(new Double(Double.NaN).
            equals(new Double(doc.getDoubleArray()[4])));
    }

    public void test21() throws XmlException
    {
        Assert.assertTrue("fload expected:" + 12.325f + " actual:" + doc.getFloatArray()[0],
            doc.getFloatArray()[0]==12.325f);
    }

    public void test22() throws XmlException
    {
        Assert.assertTrue(new Float(Float.NaN).
            equals(new Float(doc.getFloatArray()[1])));
    }

    public void test23() throws XmlException
    {
        Assert.assertTrue("fload expected:" + Float.POSITIVE_INFINITY + " actual:" + doc.getFloatArray()[2],
            new Float(Float.POSITIVE_INFINITY).equals(new Float(doc.getFloatArray()[2])));
    }

    public void test24() throws XmlException
    {
        Assert.assertTrue(new Float(Float.NEGATIVE_INFINITY).
            equals(new Float(doc.getFloatArray()[3])));
    }

    public void test25() throws XmlException
    {
        Assert.assertTrue(new BigDecimal("1.001").equals(doc.getDecimalArray()[0]));
    }

    public void test26() throws XmlException
    {
        Assert.assertTrue(new BigInteger("1000000000").equals(doc.getIntegerArray(0)));
    }
}
