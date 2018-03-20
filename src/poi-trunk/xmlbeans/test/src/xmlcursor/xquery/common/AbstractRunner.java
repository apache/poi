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
package xmlcursor.xquery.common;

import junit.framework.TestSuite;
import tools.util.JarUtil;

import java.io.FileNotFoundException;

/**
 *
 */
public  class AbstractRunner
    extends TestSuite
{

    public void findZip() throws FileNotFoundException
    {
        pathToZip = JarUtil.getFilePath("xmlcases.jar");
        int i = pathToZip.lastIndexOf('/');
        i = (i == -1) ? pathToZip.lastIndexOf('\\') : i;
        pathToZip=pathToZip.substring(0, i);

    }


    public String pathToZip;
}
