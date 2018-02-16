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

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlBeans;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.InputStream;

public class VersionTest extends TestCase
{

    public VersionTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(VersionTest.class);
    }

    // Test the getVersion API
    public void testXBeansVersion()
    {
        try
        {
            String version = XmlBeans.getVersion();
            System.out.println("XmlBeans version:" + version);
            assertNotNull(version);
        }
        catch(NullPointerException npe)
        {
            fail("NPE thrown for XmlBeans.getVersion()");
        }
    }

}
