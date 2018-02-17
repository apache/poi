/*
 *   Copyright 2004 The Apache Software Foundation
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
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.impl.common.SystemCache;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/* Test class loading using XmlBeans.getContextLoader() after changes to SystemCache.java (r240333)
*  Now a custom implementation of the SystemCache can be provided
*/
public class SystemCacheClassloadersTest extends TestCase
{
    public SystemCacheClassloadersTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(SystemCacheClassloadersTest.class);
    }


    public static void testSystemCacheAndThreadLocal()
    {
        Thread testThread = new SystemCacheThread("SchemTypeLoader Test Thread");

        try {
            testThread.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        finally {
            try {
                testThread.join();
            }
            catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }

    }


    public static void main(String[] args) throws Throwable
    {
        testSystemCacheAndThreadLocal();
    }

    public static class SystemCacheThread extends Thread
    {
        private String name;

        public SystemCacheThread(String threadName)
        {
            super();
            name = threadName;
        }

        /**
         */
        public void run()
        {
            System.out.println("Run Method of thread " + name);

            try {

                // test classloading from 2 different scomp jars using the default impl of SystemCache
                testDefaultSystemCacheClassLoading();

            }
            catch (Throwable t) {
                t.printStackTrace();
            }
        }

        public void testDefaultSystemCacheClassLoading()
        {
            try {
                // create classloaders here
                String xbean_home = System.getProperty("xbean.rootdir");
                if (xbean_home == null) {
                    xbean_home = new File(".").getAbsolutePath();
                }

                File xbeanFile = new File(xbean_home + "/build/lib/xbean.jar");
                //File xbeanFile = new File(xbean_home + "/xbean.jar");
                if (!xbeanFile.exists()) {
                    throw new Exception("File " + xbeanFile + "does not exist");
                }
                URL xbeanjar = xbeanFile.toURL();

                File domFile = new File(xbean_home + "/build/test/lib/schemajars/dom.jar");
                if (!domFile.exists()) {
                    throw new Exception("File does not exist : " + domFile.toString());
                }
                URL domjar = domFile.toURL();

                File miscFile = new File(xbean_home + "/build/test/lib/schemajars/misc.jar");
                if (!miscFile.exists()) {
                    throw new Exception("File does not exist");
                }
                URL miscjar = miscFile.toURL();

                // dom.jar
                URLClassLoader domTestsLoader = new URLClassLoader(new URL[]{xbeanjar, domjar});
                // misc.jar
                URLClassLoader miscTestsLoader = new URLClassLoader(new URL[]{xbeanjar, miscjar});

                System.out.println("Contents of domTestsLoader URL");
                URL[] urls = domTestsLoader.getURLs();
                for (int i = 0; i < urls.length; i++) {
                    System.out.println("URL:" + urls[i].toString());
                }
                System.out.println("------------------");

                System.out.println("Contents of miscTestsLoader URL");
                urls = miscTestsLoader.getURLs();
                for (int i = 0; i < urls.length; i++) {
                    System.out.println("URL:" + urls[i].toString());
                }
                System.out.println("------------------");


                // define the Qnames of types to look for in the compiled xbeans after switching the class loaders
                QName domTypeQName = new QName("http://xbean/dom/ComplexTypeTest", "elementT");
                QName miscPersonTypeQName = new QName("http://xbean/misc/SyscacheTests", "personType", "test");

                setContextClassLoader(domTestsLoader);
                //System.out.println("Testing elementT Type From dom tests complexTypeTest.xsd");
                SchemaTypeLoader initialDomLoader = XmlBeans.getContextTypeLoader();
                SchemaType domSchemaType = initialDomLoader.findType(domTypeQName);
                assertNotNull(domSchemaType);
                assertEquals("Invalid Type!", domSchemaType.getFullJavaImplName(), "xbean.dom.complexTypeTest.impl.ElementTImpl");

                // -ve test, look for the person type from cases\misc\syscachetest.xsd
                SchemaType personTypeFromMiscTests = initialDomLoader.findType(miscPersonTypeQName);
                assertNull(personTypeFromMiscTests);

                // switch the SchemaTypeLoader
                setContextClassLoader(miscTestsLoader);
                //System.out.println("Testing Person Type From misc syscachetests.xsd");
                SchemaTypeLoader initialMiscSchemaLoader = XmlBeans.getContextTypeLoader();
                SchemaType miscPersonType = initialMiscSchemaLoader.findType(miscPersonTypeQName);
                assertTrue(miscPersonType != null);
                assertEquals("Invalid Type!", miscPersonType.getFullJavaImplName(), "xbean.misc.syscacheTests.impl.PersonTypeImpl");

                // -ve test
                SchemaType personTypeFromMisc = initialMiscSchemaLoader.findType(domTypeQName);
                assertNull(personTypeFromMisc);

                // reload the original loader
                setContextClassLoader(domTestsLoader);
                SchemaTypeLoader secondDomLoader = XmlBeans.getContextTypeLoader();
                assertNotNull(secondDomLoader.findType(domTypeQName));
                assertTrue("SchemaTypeLoaders expected to be equal", initialDomLoader == secondDomLoader);

                setContextClassLoader(miscTestsLoader);
                SchemaTypeLoader secondMiscLoader = XmlBeans.getContextTypeLoader();
                assertTrue("SchemaTypeLoaders expected to be equal", initialMiscSchemaLoader == secondMiscLoader);

            }
            catch (Throwable t) {
                t.printStackTrace();
            }

        }


    }

}
