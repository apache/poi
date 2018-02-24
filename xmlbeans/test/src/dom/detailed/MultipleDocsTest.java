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

package dom.detailed;

import dom.common.Loader;
import junit.framework.TestCase;
import org.w3c.dom.*;


/**
 *
 *
 *
 */
public class MultipleDocsTest extends TestCase {
    String[] sXml = new String[]{"<foo0/>",
                                 "<foo1 foo1_at=\"val0\"/>",
                                 "<foo2 foo2_at=\"val0\">text</foo2>",
                                 "<foo3 foo3_at=\"val0\">text <foo2 foo2_at=\"val0\">text</foo2> </foo3>",
                                 "<foo4 xmlns:myns=\"foo.org\" myns:foo3_at=\"val0\">text <foo2 foo2_at=\"val0\">text</foo2> </foo4>",
                                 "<foo5  xmlns:myns=\"foo_OUT.org\"><myns:foo4 xmlns:myns=\"foo.org\" myns:foo3_at=\"val0\">text <foo2 foo2_at=\"val0\">text</foo2> </myns:foo4></foo5>"
    };

    Thread[] threads;
    int nThreadCount = 6;
    int nIterations = 100;
    Document[] m_doc;

    public MultipleDocsTest(String name) {
        super(name);
    }

    public void testRunThreads() {

        for (int j = 0; j < nThreadCount; j++)
            threads[j].start();

        for (int j = 0; j < nThreadCount; j++) {
            try {
                threads[j].join();
            }
            catch (InterruptedException e) {
                System.err.println("Thread " + j + " interrupted");

            }

        }
    }

    public void setUp() throws Exception {
        threads = new Thread[nThreadCount];
        for (int i = 0; i < nThreadCount; i++) {
            Loader loader = Loader.getLoader();
            if (sXml == null)
                throw new IllegalArgumentException(
                        "Test bug : Initialize xml strings");
            Document m_doc = (org.w3c.dom.Document) loader.load(sXml[i]);

            threads[i] = new Thread(new Worker(i, m_doc, nIterations));
        }
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    private class Worker extends Thread {
        int _ID;
        Document doc;
        int nIter;

        public Worker(int ID, Document doc, int nIter) {
            this._ID = ID;
            this.doc = doc;
            this.nIter = nIter;
        }

        public void run() {
            System.err.println("*** Thread " + _ID + " starting " +
                    doc.getDocumentElement().getNodeName() +
                    " " +
                    nIter);
            try {
                for (int i = 0; i < nIter; i++) {
                   // System.err.println("*** Thread "+_ID+" starting "+nIter+" "+i);
                    switch (0) {//_ID

                        case 0:
                            {
                                doc.getDocumentElement().appendChild(
                                        doc.createElement("foobar"));
                                if (i % 5 == 0) {
                                    NodeList nl = doc.getDocumentElement()
                                            .getElementsByTagName("foobar");
                                    if (nl.getLength() > 0) {
                                        Element par = (Element) nl.item(0)
                                                .getParentNode();
                                        par.removeChild((Element) nl.item(0));
                                    }
                                }
                                // break;
                            }
                        case 1:
                            {
                                ((Element) doc.getDocumentElement()).setAttributeNode(
                                        doc.createAttribute("foobar"));
                                if (i % 5 == 0) {
                                    NamedNodeMap nl = doc.getDocumentElement()
                                            .getAttributes();
                                    if (nl.getLength() > 0) {
                                        Element par = (Element) ((Attr) nl.getNamedItem(
                                                "foobar")).getOwnerElement();
                                        par.removeAttribute("foobar");
                                    }
                                }
                                // break;
                            }
                        case 2:
                            {
                                doc.getDocumentElement().appendChild(
                                        doc.createTextNode("foobar"));
                                if (i % 5 == 0) {
                                    NodeList nl = ((Element) doc.getDocumentElement()).getElementsByTagName(
                                            "foobar");
                                    if (nl != null && nl.getLength() > 0) {
                                        Element par = (Element) nl.item(
                                                i % nl.getLength())
                                                .getParentNode();
                                        par.removeChild((Element) nl.item(0));
                                    }
                                }
                                //  break;

                            }
                        case 3:
                            {
                                Node n = doc.getDocumentElement()
                                        .getFirstChild();
                                if (n != null && (n instanceof Text)) {
                                    ((Text) n).setData("newText");
                                    if (i % 5 == 0) {
                                        int len;
                                        if ((len =
                                                ((Text) n).getData().length()) >
                                                0)
                                            ((Text) n).splitText(len / 2);
                                    }
                                }
                                //    break;
                            }
                        case 4:
                            {
                                //  System.err.println("*** Thread "+_ID+" case 4 ");
                                NodeList n = ((Element) doc.getDocumentElement()).getElementsByTagNameNS(
                                        "foo:org", "myns:foo2");
                                Attr at = doc.createAttribute("foo2_at");
                                if (n != null && n.getLength() > 0) {
                                    ((Element) n.item(0)).setAttributeNode(at);
                                    if (i % 5 == 0) {
                                        ((Element) n.item(0)).removeAttributeNode(
                                                at);
                                    }
                                }
                            }

                    }

                }

            }
            catch (Throwable t) {
                System.err.println("Caught throwable");
               // return;

            }
        }
    }

    public static void main(String[] a) {
        try {
            MultipleDocsTest test = new MultipleDocsTest("");
            test.setUp();
            test.testRunThreads();
        }
        catch (Throwable t) {
            t.printStackTrace(System.err);
        }
    }

}
