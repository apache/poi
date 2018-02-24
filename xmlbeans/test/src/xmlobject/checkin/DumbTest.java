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

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.xmlbeans.XmlObject;

import dumbNS.RootDocument.Root;
import dumbNS.RootDocument;

import tools.util.Util;
import tools.util.ResourceUtil;
import tools.util.JarUtil;
import xmlcursor.common.Common;


/**
 *
 *
 */
public class DumbTest extends TestCase {
    String instance;

    public DumbTest(String name) {
        super(name);
    }

    public void setUp() {

        try {
            instance = JarUtil.getResourceFromJar("xbean/simple/dumb/dumb.xml");
        } catch (IOException e) {
            System.err.println("Could not load xbean/simple/dumb/dumb.xml from " + Common.XMLCASES_JAR
                               + e.getMessage());
            e.printStackTrace();
        }
    }

    public void testGetB2()
            throws Exception {
        RootDocument rootDoc = (RootDocument) XmlObject.Factory
                .parse(instance);
        Root root = rootDoc.getRoot();

        System.out.println("root.xmlText() = " + root.xmlText());

        assertTrue("bar:b attribute != 4", root.getB2().intValue() == 4);
    }
}
