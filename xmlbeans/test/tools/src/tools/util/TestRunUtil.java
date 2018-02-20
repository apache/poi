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
package tools.util;

import java.io.File;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class TestRunUtil
{
    /**
     * Runs method that must be declared "static void test()" on the given
     * class, with the given additional jars or directories on the classpath.
     */
    public static void run(String classname, File[] classpath) throws Throwable
    {
        // System.err.println("Running " + classname + " with classpath:");
        for (int i = 0; i < classpath.length; i++)
        {
            // System.err.println(classpath[i]);
            if (!classpath[i].exists())
                throw new IllegalArgumentException("Classpath component " + classpath + " cannot be found!");
        }

        URL[] extracp = new URL[classpath.length];
        for (int i = 0; i < classpath.length; i++)
        {
            try
            {
                extracp[i] = classpath[i].toURL();
            }
            catch (MalformedURLException e)
            {
                throw new IllegalArgumentException("Malformed classpath filename");
            }
        }

        ClassLoader curcl = Thread.currentThread().getContextClassLoader();

        try
        {
            ClassLoader childcl = new URLClassLoader(extracp);
            Class javaClass = childcl.loadClass(classname);
            Class testClass = childcl.loadClass("org.openuri.mytest.CustomerDocument");
            if (testClass == null)
                throw new IllegalStateException();
            Method meth = javaClass.getMethod("test", new Class[0]); // should be static
            Thread.currentThread().setContextClassLoader(childcl);
            meth.invoke(null, new Object[0]);
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalArgumentException("class not found");
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalArgumentException("no test() method found");
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalArgumentException("could not invoke static public test method");
        }
        catch (InvocationTargetException e)
        {
            throw e.getCause();
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(curcl);
        }
    }
}
