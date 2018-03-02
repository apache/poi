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

import junit.framework.TestCase;
import common.Common;

import java.util.Random;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Method;

import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.common.SystemCache;

import javax.xml.namespace.QName;

public class SystemCacheTests extends TestCase
{
    public void testSystemCacheImplFromAPITest() throws Throwable
    {
        try
        {
            // store the default SystemCache implementation before switch
            SystemCache defaultImpl = SystemCache.get();

            //assertEquals("org.apache.xmlbeans.impl.common.SystemCache",defaultImpl.getClass().getName());
            // The expected default impl does not get picked up as the test is not run from a single jvm
            // when run from the test infrastructure. Hence compare against the actual impl that gets picked up
            // The assert above commented out will hold good if this test is invoked as follows:
            // ant run.junit -Dtest.area=misc -Dtest.spec=misc.detailed.SystemCacheTests
            assertEquals("org.apache.xmlbeans.impl.schema.SchemaTypeLoaderImpl$SchemaTypeLoaderCache",defaultImpl.getClass().getName());

            // switch the Impl to the test Impl
            SystemCacheTestImpl testImpl = new SystemCacheTestImpl();
            SystemCache.set(testImpl);
            assertEquals("misc.detailed.SystemCacheTestImpl",testImpl.getClass().getName());
            assertEquals(testImpl.getAccessed(), 1);

            // switch back to default impl
            SystemCache.set(defaultImpl);
            System.out.println("Third 1:" + defaultImpl.getClass().getName());
            System.out.println("Third 2:" + defaultImpl.getClass().getName());
            //assertEquals("org.apache.xmlbeans.impl.common.SystemCache",defaultImpl.getClass().getName());
            assertEquals("org.apache.xmlbeans.impl.schema.SchemaTypeLoaderImpl$SchemaTypeLoaderCache",defaultImpl.getClass().getName());
        }
        catch(ExceptionInInitializerError err)
        {
            System.out.println(err.getMessage());
            throw new Exception("File does not exist");
        }
    }

}
