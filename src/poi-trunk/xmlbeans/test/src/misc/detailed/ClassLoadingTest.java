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

package misc.detailed;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import org.apache.xmlbeans.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;


public class ClassLoadingTest extends TestCase
{
    public ClassLoadingTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(ClassLoadingTest.class);
    }

    public static class CompilationThread extends Thread
    {
        private Throwable _throwable;
        private boolean _result;
        private Random rand;
        XmlOptions xm;
        ArrayList errors;

        public CompilationThread()
        {
            rand = new Random();
            xm = new XmlOptions();
            ArrayList errors = new ArrayList();
            xm.setErrorListener(errors);
            xm.setValidateOnSet();
        }

        public Throwable getException()
        {
            return _throwable;
        }

        public boolean getResult()
        {
            return _result;
        }

        public int getRandIntVal(int seed)
        {
            return rand.nextInt(seed);
        }

        public void run()
        {

            try {
                String xbean_home = System.getProperty("xbean.rootdir");
                if (xbean_home == null) {
                    xbean_home = new File(".").getAbsolutePath();
                }
                File xbeanFile = new File(xbean_home + "/build/lib/xbean.jar");
                if (!xbeanFile.exists())
                    throw new Exception("File does not exist");
                URL xbeanjar = xbeanFile.toURL();
                File xmlpublicFile = new File(xbean_home + "/build/lib/xmlpublic.jar");
                if (!xmlpublicFile.exists())
                    throw new Exception("File does not exist");
                URL xmlpublicjar = xmlpublicFile.toURL();
                File jsr173File = new File(xbean_home + "/build/lib/jsr173.jar");
                if (!jsr173File.exists())
                    throw new Exception("File does not exist");
                URL jsr173jar = new URL(jsr173File.toURL().toString());
                File jsr173_apiFile = new File(xbean_home + "/build/lib/jsr173_1.0_api.jar");
                if (!jsr173_apiFile.exists())
                    throw new Exception("File does not exist");
                URL jsr173_apijar = jsr173_apiFile.toURL();
                File jsr173_riFile = new File(xbean_home + "/build/lib/jsr173_1.0_ri.jar");
                if (!jsr173_riFile.exists())
                    throw new Exception("File does not exist");
                URL jsr173_rijar = jsr173_riFile.toURL();
                File junitFile = new File(xbean_home + "/external/lib/junit.jar");
                if (!junitFile.exists())
                    throw new Exception("File does not exist");
                URL junitjar = junitFile.toURL();

                File xmlcursorFile = new File(xbean_home + "/build/test/lib/schemajars/xmlcursor.jar");
                if (!xmlcursorFile.exists())
                    throw new Exception("File does not exist");
                URL xmlcursorjar = xmlcursorFile.toURL();
                File validatingFile = new File(xbean_home + "/build/test/lib/schemajars/ValidatingStream.jar");
                if (!validatingFile.exists())
                    throw new Exception("File does not exist");
                URL validating = validatingFile.toURL();


                //URLClassLoader allLoader = new URLClassLoader(new URL[]{xbeanjar, xmlpublicjar,
                //                                                        jsr173jar, jsr173_apijar, jsr173_rijar,
                //                                                        junitjar, validating, xmlcursorjar});

                URLClassLoader personLoader = new URLClassLoader(new URL[]{xbeanjar, xmlpublicjar,
                                                                      jsr173jar, jsr173_apijar, jsr173_rijar,
                                                                      junitjar, validating});

                URLClassLoader poLoader = new URLClassLoader(new URL[]{xbeanjar, xmlpublicjar,
                                                                      jsr173jar, jsr173_apijar, jsr173_rijar,
                                                                      junitjar, xmlcursorjar});

                for (int i = 0; i < ITERATION_COUNT; i++) {
                    switch (i) {
                        case 0:
                            testPO(poLoader);
                            break;
                        case 1:
                            testPerson(personLoader);
                            break;
                        default:
                            System.out.println("Val: " + i);
                            break;
                    }

                }
                _result = true;

            } catch (Throwable t) {
                _throwable = t;
                t.printStackTrace();
            }
        }

        public void testPO(ClassLoader cursorLoader) //from xmlcursor.jar
        {
            try {
                setContextClassLoader(cursorLoader);
                System.out.println("Testing PO");
                String poInstance = "<?xml version=\"1.0\"?>\n" +
                        "<po:purchaseOrder xmlns:po=\"http://xbean.test/xmlcursor/PurchaseOrder\" orderDate=\"1999-10-20\">\n" +
                        "    <po:shipTo country=\"US\">\n" +
                        "        <po:name>Alice Smith</po:name>\n" +
                        "        <po:street>123 Maple Street</po:street>\n" +
                        "        <po:city>Mill Valley</po:city>\n" +
                        "        <po:state>CA</po:state>\n" +
                        "        <po:zip>90952</po:zip>\n" +
                        "    </po:shipTo>\n" +
                        "    <po:billTo country=\"US\">\n" +
                        "        <po:name>Robert Smith</po:name>\n" +
                        "        <po:street>8 Oak Avenue</po:street>\n" +
                        "        <po:city>Old Town</po:city>\n" +
                        "        <po:state>PA</po:state>\n" +
                        "        <po:zip>95819</po:zip>\n" +
                        "    </po:billTo>\n" +
                        "    <po:comment>Hurry, my lawn is going wild!</po:comment>\n" +
                        "    <po:items>\n" +
                        "        <po:item partNum=\"872-AA\">\n" +
                        "            <po:productName>Lawnmower</po:productName>\n" +
                        "            <po:quantity>1</po:quantity>\n" +
                        "            <po:USPrice>148.95</po:USPrice>\n" +
                        "            <po:comment>Confirm this is electric</po:comment>\n" +
                        "        </po:item>\n" +
                        "        <po:item partNum=\"926-AA\">\n" +
                        "            <po:productName>Baby Monitor</po:productName>\n" +
                        "            <po:quantity>1</po:quantity>\n" +
                        "            <po:USPrice>39.98</po:USPrice>\n" +
                        "            <po:shipDate>1999-05-21</po:shipDate>\n" +
                        "        </po:item>\n" +
                        "    </po:items>\n" +
                        "</po:purchaseOrder>";

                Class poClass = this.getContextClassLoader().loadClass("test.xbean.xmlcursor.purchaseOrder.PurchaseOrderDocument$Factory");

                Method m = poClass.getMethod("parse", new Class[]{String.class, XmlOptions.class});
                Object poObject = m.invoke(null, new Object[]{poInstance, xm});

                Method m2 = poObject.getClass().getMethod("validate", new Class[]{});
                Boolean res = (Boolean) m2.invoke(poObject, new Object[]{});
                if (!res.booleanValue()) {
                    System.out.println("Res failed Validation: ");
                    System.out.println("errors: " + errors.toString());
                } else {
                    System.out.println("PurchaseOrderDocument Validated");
                }
            } catch (Throwable t) {
                _throwable = t;
                t.printStackTrace();
            }
        }

        public void testPerson(ClassLoader validateLoader)
        { //from ValidatingStream.jar
            try {

                setContextClassLoader(validateLoader);
                System.out.println("Testing Person");

                String poInstance = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<per:Person xmlns:per=\"http://openuri.org/test/Person\"\n" +
                        "        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "    Sex=\"male\" Birthday=\"1967-08-13\">\n" +
                        "\t<per:Name>\n" +
                        "\t\t<per:First>Person</per:First>\n" +
                        "\t\t<per:Last>One</per:Last>\n" +
                        "\t</per:Name>\n" +
                        "\t<per:Sibling Sex=\"male\" Birthday=\"1967-08-13\">\n" +
                        "\t\t<per:Name>\n" +
                        "\t\t\t<per:First>Person</per:First>\n" +
                        "\t\t\t<per:Last>Two</per:Last>\n" +
                        "\t\t</per:Name>\n" +
                        "        <per:Sibling Sex=\"female\" Birthday=\"1992-12-12\">\n" +
                        "            <per:Name>\n" +
                        "                <per:First>Person</per:First>\n" +
                        "                <per:Last>Three</per:Last>\n" +
                        "            </per:Name>\n" +
                        "        </per:Sibling>\n" +
                        "\t</per:Sibling>\n" +
                        "</per:Person>";

                //org.openuri.test.person.PersonDocument
                Class poClass = this.getContextClassLoader().loadClass("org.openuri.test.person.PersonDocument$Factory");

                Method m = poClass.getMethod("parse", new Class[]{String.class, XmlOptions.class});
                Object poObject = m.invoke(null, new Object[]{poInstance, xm});

                Method m2 = poObject.getClass().getMethod("validate", new Class[]{});
                Boolean res = (Boolean) m2.invoke(poObject, new Object[]{});
                if (!res.booleanValue()) {
                    System.out.println("Res failed Validation: ");
                    System.out.println("errors: " + errors.toString());
                } else {
                    System.out.println("PersonDocument Validated");
                }
            } catch (Throwable t) {
                _throwable = t;
                t.printStackTrace();
            }
        }
    }

    //Moved for convenience of changing
    public static final int THREAD_COUNT = 150;
    public static final int ITERATION_COUNT = 2;

    /**
     * OOM Error happens around thread 97 with the params listed below
     */
    public static void testThreadedCompilation() throws Throwable
    {
        Runtime r = Runtime.getRuntime();
        CompilationThread[] threads = new CompilationThread[THREAD_COUNT];

        //moved to ensure init wasn't helping in problem
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new CompilationThread();
        }

        for (int i = 0; i < threads.length; i++) {
            System.out.println("FREE MEM: "+r.freeMemory()+" TOTAL MEM: "+r.totalMemory());
            System.out.print("Thread["+i+"]-starting ");
            System.out.println("FREE MEM: " + r.freeMemory() + " TOTAL MEM: " + r.totalMemory());
            //    threads[i] = new XPathThread();
            threads[i].start();
            threads[i].join();
        }

        //for (int i = 0; i < threads.length; i++) {
        //    Assert.assertNull(threads[i].getException());
        //    Assert.assertTrue("Thread " + i + " didn't succeed",
        //            threads[i].getResult());
        //}
    }

    /**
     * Call this with something like the following
     * D:\xml\xml-xmlbeans\v2\build\test\lib>
     * java
     * -Xms8M -Xmx16M
     * -cp .\testcases.jar;.\testtools.jar;.;..\..\lib\xbean.jar;
     * ..\..\lib\jsr173.jar;..\..\lib\xmlpublic.jar;
     * ..\..\lib\jsr173_api.jar;..\..\lib\jsr173_ri.jar;
     * ..\..\..\external\lib\junit.jar;
     * misc.detailed.ClassLoadingTest
     * @param args
     * @throws Throwable
     */
    public static void main(String[] args) throws Throwable
    {
        testThreadedCompilation();
        throw new Exception("FOO");
    }
}

