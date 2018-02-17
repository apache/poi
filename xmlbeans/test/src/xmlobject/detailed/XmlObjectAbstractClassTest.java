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

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.apache.xmlbeans.impl.tool.CodeGenUtil;
import tools.util.JarUtil;
import xmlcursor.common.Common;

/**
 * JUnit Test file to test XmlObject Abstract base class
 *
 * @author: Raju Subramanian.
 *
 *
 */

public class XmlObjectAbstractClassTest
        extends TestCase {

    public XmlObjectAbstractClassTest(String name) {
        super(name);
    }

    /**
     * The test entry point.
     */
    public void testAbstractBaseClass() throws Exception {
        // create the source file
        //String src = JarUtil.getResourceFromJarasStream(Common.XMLCASES_JAR, "xbean/xmlobject/SimpleXmlObject.java.txt");
        File to = new File("SimpleXmlObject.java");
        InputStreamReader r=new InputStreamReader(
                                JarUtil.getResourceFromJarasStream(
                                        "xbean/xmlobject/SimpleXmlObject.java.txt"));
        assertTrue("Could not create source file",
                copyTo(r , to));
        assertTrue("Could not compile SimpleXmlObject.java",
                compileFile(to));
        to.deleteOnExit();
    }


    /**
     * Compiles the source file.
     * The destination for the compiled file is the current directory
     */
    private boolean compileFile(File source) {
        // the location for the compiled file
        File dir = new File(System.getProperty("user.dir"));
        File[] classpath = CodeGenUtil.systemClasspath();
        List srcFiles = new ArrayList();
        srcFiles.add(source);

        if (!CodeGenUtil.externalCompile(srcFiles, dir, classpath, false,
                CodeGenUtil.DEFAULT_COMPILER, null, CodeGenUtil.DEFAULT_MEM_START,
                CodeGenUtil.DEFAULT_MEM_MAX, false, false)){
            return false;
        }
        return true;
    }

    /**
     * Copies a file. If destination file exists it will be overwritten
     */
    private boolean copyTo(InputStreamReader src, File to) {
        try {
            // inputstream to read in the file
            BufferedReader in = new BufferedReader(src);

            // delete the existing file
            to.delete();
            to.createNewFile();
            // outputstream to write out the java file
            FileOutputStream fos = new FileOutputStream(to);
            int b;

            while ((b = in.read()) != -1) {
                fos.write(b);
            }

            in.close();
            fos.close();
        }
        catch (Exception ioe) {
            System.out.println("Could not create source file: " + ioe);
            ioe.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Copies a file. If destination file exists it will be overwritten
     */
    private boolean copyTo(File src, File to) {
        try {
            // inputstream to read in the file
            FileInputStream fis = new FileInputStream(src);

            // delete the existing file
            to.delete();
            to.createNewFile();
            // outputstream to write out the java file
            FileOutputStream fos = new FileOutputStream(to);
            int b;

            while ((b = fis.read()) != -1) {
                fos.write(b);
            }
            fis.close();
            fos.close();
        }
        catch (Exception ioe) {
            System.out.println("Could not create source file: " + ioe);
            return false;
        }

        return true;
    }

    /**
     *
     */
    public static void main(String args[]) throws Exception {
        new XmlObjectAbstractClassTest("test").testAbstractBaseClass();
    }
}
