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
import java.io.InputStream;

import java.net.URL;

/**
 * Get a resource as InputStream, File, or URL from the classpath.
 */
public class ResourceUtil
{
    /**
     * Get the class name minus the first package name; for the class name
     * "foo.bar.Blee", the method returns "bar.Blee".  If the class is missing
     * any package, the whole class name is returned.
     */
    public static String getTailName(Class c)
    {
        String name = c.getName();
        return name.substring(name.indexOf('.')+1);
    }

    /**
     * Get the trailing part of the classname; for the class name
     * "foo.bar.Blee", the method returns "Blee".
     */
    public static String getLastName(Class c)
    {
        String name = c.getName();
        int i = name.lastIndexOf('.');
        return name.substring(i+1, name.length());
    }

    /**
     * Get a URL to a resource on the system classpath. For example:
     * <pre>
     * getURL("xbean/xmlobject/simple/dumb/dumb.xml")
     * </pre>
     * NOTE: This is intended to be used from a junit test since it will throw
     * an exception on failure which will be caught by the junit runner.
     *
     * @throws RuntimeException If no resource is found.
     */
    public static URL getURL(String name)
    {
        URL url = ClassLoader.getSystemResource(name);
        if (url == null)
            throw new RuntimeException("Resource doesn't exist: " + name);

        return url;
    }

    /**
     * Get a File to a resource on the system classpath.
     * NOTE: This is intended to be used from a junit test since it will throw
     * an exception on failure which will be caught by the junit runner.
     *
     * @throws RuntimeException If no resource is found.
     */
    public static File getFile(String name)
    {
        return new File(ResourceUtil.getURL(name).getFile());
    }

    /**
     * Get a InputStream to a resource on the system classpath.
     * NOTE: This is intended to be used from a junit test since it will throw
     * an exception on failure which will be caught by the junit runner.
     *
     * @throws RuntimeException If no resource is found.
     */
    public static InputStream getStream(String name)
    {
        InputStream is = ClassLoader.getSystemResourceAsStream(name);
        if (is == null)
            throw new RuntimeException("Resource doesn't exist: " + name);

        return is;
    }

    /**
     * Find a resource with the given name. For example,
     * <pre>
     * getURL(foo.bar.Blee.class, "file.txt");
     * </pre>
     * will obtain an <code>URL</code> to the file named
     * "foo/bar/Blee/file.txt".  Note that the
     * {@link Class#getResource()} method would return the resource
     * for "foo/bar/file.txt" instead.
     *
     * @param clazz The resource class.
     * @param name The name of the resource to obtain.
     * @return The <code>InputStream</code> to the named resource.
     * @see java.lang.Class#getResource(java.lang.String name)
     * @throws RuntimeException If no resource is found.
     */
    public static URL getURL(Class c, String name)
    {
        return ResourceUtil.getURL(c, ResourceUtil.getLastName(c), name);
    }

    /**
     * Find a resource with the given name. For example,
     * <pre>
     * getURL(foo.bar.Blee.class, "Blee", "file.txt");
     * </pre>
     * will obtain an <code>URL</code> to the file named
     * "foo/bar/Blee/file.txt".  Note that the
     * {@link Class#getResource()} method would return the resource
     * for "foo/bar/file.txt" instead.
     *
     * @param clazz The resource class.
     * @param lastName The name of the class itself, eg. for class
     *   "foo.bar.Blee", the <code>lastName</code> is "Blee".
     * @param name The name of the resource to obtain.
     * @return The <code>URL</code> to the named resource.
     * @see java.lang.Class#getResourceAsStream(java.lang.String name)
     * @throws RuntimeException If no resource is found.
     */
    public static URL getURL(Class c, String lastName, String name)
    {
        // get the contents of the resouce
        URL url = c.getResource(lastName + "/" + name);
        if (url == null) {
            throw new RuntimeException("Can't find resource: " +
                lastName +
                File.separator +
                File.separator + name);
        }
        return url;
    }


    /**
     * Find a resource with the given name. For example,
     * <pre>
     * getStream(foo.bar.Blee.class, "file.txt");
     * </pre>
     * will obtain an <code>InputStream</code> to the file named
     * "foo/bar/Blee/file.txt".  Note that the
     * {@link Class#getResourceAsStream()} method would return the resource
     * for "foo/bar/file.txt" instead.
     *
     * @param clazz The resource class.
     * @param name The name of the resource to obtain.
     * @return The <code>URL</code> to the named resource.
     * @see java.lang.Class#getResourceAsStream(java.lang.String name)
     * @throws RuntimeException If no resource is found.
     */
    public static InputStream getStream(Class clazz, String name)
    {
        return ResourceUtil.getStream(clazz,
            ResourceUtil.getLastName(clazz), name);
    }

    /**
     * Find a resource with the given name. For example,
     * <pre>
     * getStream(foo.bar.Blee.class, "Blee", "file.txt");
     * </pre>
     * will obtain an <code>InputStream</code> to the file named
     * "foo/bar/Blee/file.txt".  Note that the
     * {@link Class#getResourceAsStream()} method would return the resource
     * for "foo/bar/file.txt" instead.
     *
     * @param clazz The resource class.
     * @param lastName The name of the class itself, eg. for class
     *   "foo.bar.Blee", the <code>lastName</code> is "Blee".
     * @param name The name of the resource to obtain.
     * @return The <code>InputStream</code> to the named resource.
     * @see java.lang.Class#getResourceAsStream(java.lang.String name)
     * @throws RuntimeException If no resource is found.
     */
    public static InputStream getStream(Class c, String lastName, String name)
    {
        // get the contents of the resouce
        InputStream is = c.getResourceAsStream(lastName + "/" + name);
        if (is == null) {
            throw new RuntimeException("Can't find resource: " +
                lastName +
                File.separator +
                File.separator + name);
        }
        return is;
    }

    /**
     * Find a resource with the given name. For example,
     * <pre>
     * getFile(foo.bar.Blee.class, "file.txt");
     * </pre>
     * will obtain an <code>File</code> to the file named
     * "foo/bar/Blee/file.txt".  Note that the
     * {@link Class#getResource()} method would return the resource
     * for "foo/bar/file.txt" instead.
     *
     * @param clazz The resource class.
     * @param lastName The name of the class itself, eg. for class
     *   "foo.bar.Blee", the <code>lastName</code> is "Blee".
     * @param name The name of the resource to obtain.
     * @return The <code>File</code> to the named resource.
     * @see java.lang.Class#getResourceAsStream(java.lang.String name)
     * @throws RuntimeException If no resource is found.
     */
    public static File getFile(Class c, String name)
    {
        return new File(getURL(c, name).getFile());
    }

    /**
     * Find a resource with the given name. For example,
     * <pre>
     * getFile(foo.bar.Blee.class, "Blee", "file.txt");
     * </pre>
     * will obtain an <code>File</code> to the file named
     * "foo/bar/Blee/file.txt".  Note that the
     * {@link Class#getResource()} method would return the resource
     * for "foo/bar/file.txt" instead.
     *
     * @param clazz The resource class.
     * @param lastName The name of the class itself, eg. for class
     *   "foo.bar.Blee", the <code>lastName</code> is "Blee".
     * @param name The name of the resource to obtain.
     * @return The <code>File</code> to the named resource.
     * @see java.lang.Class#getResourceAsStream(java.lang.String name)
     * @throws RuntimeException If no resource is found.
     */
    public static File getFile(Class c, String lastName, String name)
    {
        return new File(getURL(c, lastName, name).getFile());
    }

}
