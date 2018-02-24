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

package xmlobject.common;

import java.io.File;
import java.io.IOException;

import tools.util.ResourceUtil;

import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlCursor.TokenType;

/**
 *  XmlReader: A class that implements Runnable. It creates an XMLBean for a given XML
 *             instance and walks through the document
 *
 *
 *
 */

public class XmlReader implements Runnable {
    private File xmlFile;
    private boolean status;

    /**
     * Constructor: Takes a File object that represents the xml to read.
     * No checks made to check if the file exists and is valid xml.
     */
    public XmlReader(File xmlFile) {
        // Parse the xml instance
        this.xmlFile = xmlFile;
        status = true;
    }

    /**
     *  Implements the <code>Runnable</code> Interface
     */
    public void run() {
        // Read in the xml file
        XmlObject x = null;
        String tName = Thread.currentThread().getName();
        try {
            x = XmlObject.Factory.parse(xmlFile);
        } catch (XmlException xe) {
            System.out.println("XmlException in thread " + tName);
            xe.printStackTrace();
            status = false;
            return;
        } catch (IOException ioe) {
            System.out.println("IOException in thread " + tName);
            ioe.printStackTrace();
            status = false;
            return;
        }

        try {
            // Walk through the XML
            XmlCursor cur = x.newCursor();
            cur.toStartDoc();
            do {
                // Sleep for 10 milliseconds
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                    // Continue ahead..
                }
                // Print current token
                //System.out.println("["+tName+"]: " + cur.currentTokenType().toString());
                cur.toNextToken();
            } while (cur.hasNextToken());
            cur.dispose();
        } catch (Exception e) {
            System.out.println("Exception in thread " + tName);
            e.printStackTrace();
            status = false;
        }
    }


    public boolean getStatus() {
        return status;
    }

    public void doTest() throws Exception {
        Thread t = new Thread(this, "test");
        t.start();
        t.join();
    }

    public static void main(String args[]) throws Exception {
        File xmlFile = new File("po.xml");
        new XmlReader(xmlFile).doTest();
    }

}