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

import java.io.InputStream;
import java.io.IOException;

/**
 * Utility for obtaining a class <i>tail name</i> and <i>last name</i>.  These
 * are terms I invented and aren't very good:
 * <dl>
 * <dt>Last Name:<dd>The class name minus the package name part.
 * <dt>Tail Name:<dd>The class name minus the first package.
 * </dl>
 * For example, if a class name is <code>foo.bar.Blee</code>, the last name
 * would be <code>Blee</code>, while the tail name would be
 * <code>bar.Blee</code>.
 */
public class ResourceClass
{
    Class clazz;
    String lastName;
    String tailName;

    public ResourceClass(Class clazz)
    {
        setResourceClass(clazz);
    }

    public void setResourceClass(Class clazz)
    {
        this.clazz = clazz;

        setLastName();
        setTailName();
    }

    private void setTailName()
    {
        tailName = ResourceUtil.getTailName(clazz);
    }

    private void setLastName()
    {
        lastName = ResourceUtil.getLastName(clazz);
    }

    /**
     * Get the class name minus the first package name; for the class name
     * "foo.bar.Blee", the method returns "bar.Blee".  If the class is missing
     * any package, the whole class name is returned.
     *
     * @see ResourceUtil#getTailName(Class)
     */
    public String getTailName()
    {
        return tailName;
    }

    /**
     * Get the trailing part of the classname; for the class name
     * "foo.bar.Blee", the method returns "Blee"
     *
     * @see ResourceUtil#getLastName(Class)
     */
    public String getLastName()
    {
        return lastName;
    }

    public Class getResourceClass()
    {
        return clazz;
    }

    /**
     * Get resource from the class directory into a String. Example:
     * <pre>
     * ResourceClass resClass = new ResourceClass(foo.bar.Blee.class);
     * String msg = resClass.getResource("file.txt");
     * </pre>
     * will obtain the contents of "foo/bar/Blee/file.txt".
     *
     * @param resourceName The name of the resource to obtain.
     * @return The contents of the named resource.
     * @see SoapUtil#getResource(Class clazz, String lastName,
     *     String resourceName)
     * @see java.lang.Class#getResourceAsStream(java.lang.String name)
     */
    public String getResource(String resourceName)
        throws NullPointerException, IOException
    {
        if (clazz == null)
            throw new NullPointerException();

        java.io.InputStream is =
            this.getResourceAsStream(resourceName);
        return Util.read(is);
    }

    /**
     * Get resource from the class message directory. Example:
     * <pre>
     * ResourceClass resClass = new ResourceClass(foo.bar.Blee.class);
     * InputStream is = resClass.getResourceAsStream("file.txt");
     * </pre>
     * will obtain an <code>InputStream</code> to the file named
     * "foo/bar/Blee/file.txt".
     *
     * @param resourceName The name of the resource to obtain.
     * @return The <code>InputStream</code> to the named resource.
     * @see SoapUtil#getResource(Class clazz, String lastName,
     *     String resourceName)
     * @see java.lang.Class#getResourceAsStream(java.lang.String name)
     */
    public InputStream getResourceAsStream(String resourceName)
    {
        return ResourceUtil.getStream(clazz, lastName, resourceName);
    }

}
