
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.*;

import java.util.*;
import java.util.zip.*;

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;

/**
 * Will set the given property if the requested class is available in the
 * specified classpath. The found class is not loaded!
 * This class is heavily based on the available task in the ant package:
 * @author Stefano Mazzocchi <a href="mailto:stefano@apache.org">stefano@apache.org</a>
 *
 * This task searches only in the defined path but not in the parents path
 * unless explicitly overridden by the value of ${build.sysclasspath}
 * like the original available task does.
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision$ $Date$
 */

public class ClassAvailable
    extends Task
{

    /**
     * A hashtable of zip files opened by the classloader
     */

    private Hashtable zipFiles = new Hashtable();
    private String    property;
    private String    classname;
    private Path      classpath;
    private String    value = "true";

    public void setClasspath(Path classpath)
    {
        createClasspath().append(classpath);
    }

    public Path createClasspath()
    {
        if (this.classpath == null)
        {
            this.classpath = new Path(this.project);
        }
        return this.classpath.createPath();
    }

    public void setClasspathRef(Reference r)
    {
        createClasspath().setRefid(r);
    }

    public void setProperty(String property)
    {
        this.property = property;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public void setClassname(String classname)
    {
        if (!"".equals(classname))
        {
            this.classname = classname;
        }
    }

    public void execute()
        throws BuildException
    {
        if (property == null)
        {
            throw new BuildException("property attribute is required",
                                     location);
        }
        if (eval())
        {
            this.project.setProperty(property, value);
        }
    }

    public boolean eval()
        throws BuildException
    {
        if (classname == null)
        {
            throw new BuildException(
                "At least one of (classname|file|resource) is required",
                location);
        }
        if (classpath != null)
        {
            classpath.setProject(project);
            classpath = classpath.concatSystemClasspath("ignore");
        }
        if (!findClassInComponents(classname))
        {
            log("Unable to load class " + classname + " to set property "
                + property, Project.MSG_VERBOSE);
            return false;
        }
        return true;
    }

    /**
     * Get an inputstream to a given resource in the given file which may
     * either be a directory or a zip file.
     *
     * @param file the file (directory or jar) in which to search for the resource.
     * @param resourceName the name of the resource for which a stream is required.
     *
     * @return a stream to the required resource or null if the resource cannot be
     * found in the given file object
     */

    private boolean contains(File file, String resourceName)
    {
        try
        {
            if (!file.exists())
            {
                return false;
            }
            if (file.isDirectory())
            {
                File resource = new File(file, resourceName);

                if (resource.exists())
                {
                    return true;
                }
            }
            else
            {

                // is the zip file in the cache
                ZipFile zipFile = ( ZipFile ) zipFiles.get(file);

                if (zipFile == null)
                {
                    zipFile = new ZipFile(file);
                    zipFiles.put(file, zipFile);
                }
                ZipEntry entry = zipFile.getEntry(resourceName);

                if (entry != null)
                {
                    return true;
                }
            }
        }
        catch (Exception e)
        {
            log("Ignoring Exception " + e.getClass().getName() + ": "
                + e.getMessage() + " reading resource " + resourceName
                + " from " + file, Project.MSG_VERBOSE);
        }
        return false;
    }

    /**
     * Find a class on the given classpath.
     */

    private boolean findClassInComponents(String name)
    {

        // we need to search the components of the path to see if we can find the
        // class we want.
        final String   classname = name.replace('.', '/') + ".class";
        final String[] list      = classpath.list();
        boolean        found     = false;
        int            i         = 0;

        while ((i < list.length) && (found == false))
        {
            final File pathComponent =
                ( File ) project.resolveFile(list[ i ]);

            found = this.contains(pathComponent, classname);
            i++;
        }
        return found;
    }
}
