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

package misc.checkin;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.framework.Assert;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import javax.xml.namespace.QName;

import tools.util.*;

import java.io.File;

public class ThreadingTest extends TestCase {
    public ThreadingTest(String name) {
        super(name);
    }

    public static Test suite() {
        return new TestSuite(ThreadingTest.class);
    }

    public static final int THREAD_COUNT = 4;
    public static final int ITERATION_COUNT = 1;

    public class CompilationThread extends Thread {
        private Throwable _throwable;
        private boolean _result;

        public Throwable getException() {
            return _throwable;
        }

        public boolean getResult() {
            return _result;
        }

        public void run() {
            try {
                for (int i = 0; i < ITERATION_COUNT; i++) {
                    SchemaTypeLoader loader = XmlBeans.loadXsd(
                            new XmlObject[]{
                                XmlObject.Factory.parse(
                                        JarUtil.getResourceFromJarasFile(
                                                "xbean/misc/xmldsig-core-schema.xsd"))});
                    File temp = JarUtil.getResourceFromJarasFile(
                            "xbean/misc/signature-example.xml");
                    XmlObject result = loader.parse(temp, null, null);
                    Assert.assertEquals(loader.findDocumentType(new QName(
                            "http://www.w3.org/2000/09/xmldsig#",
                            "Signature")), result.schemaType());
                }
                _result = true;
            }
            catch (Throwable t) {
                _throwable = t;
                t.printStackTrace();
            }
        }
    }

    public void testThreadedCompilation() throws Throwable {
        CompilationThread[] threads = new CompilationThread[THREAD_COUNT];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new CompilationThread();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        for (int i = 0; i < threads.length; i++) {
            Assert.assertNull(threads[i].getException());
            Assert.assertTrue("Thread " + i + " didn't succeed",
                    threads[i].getResult());
        }
    }
}

