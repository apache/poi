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

package org.apache.xmlbeans.samples.xmltree;

import java.io.File;

/**
 * A class with which to test the XmlTree sample.
 */
final class XmlTreeTest
{
    /**
     * Tests the XmlTree sample.
     * 
     * @param args An array in which the first item is a path to an XML file.
     */
    public static void main(String[] args)
    {
        boolean isStarted = start(args);
//        assert !isStarted;
    }

    private static boolean start(String[] args)
    {
        File xmlFile = new File(args[0]);
        XmlTreeFrame frame = new XmlTreeFrame(xmlFile);
        return frame.isVisible();
    }
}