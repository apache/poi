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

package org.apache.xmlbeans.impl.soap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Properties;
import org.apache.xmlbeans.SystemProperties;

/**
 * This class is used to locate factory classes for javax.xml.soap.
 * It has package scope since it is not part of JAXM and should not
 * be accessed from other packages.
 */
class FactoryFinder {
    /**
     * instantiates an object go the given classname.
     *
     * @param factoryClassName
     * @return a factory object
     * @throws SOAPException
     */
    private static Object newInstance(String factoryClassName) throws SOAPException {
        ClassLoader classloader = null;
        try {
            classloader = Thread.currentThread().getContextClassLoader();
        } catch (Exception exception) {
            throw new SOAPException(exception.toString(), exception);
        }

        try {
            Class factory = null;
            if (classloader == null) {
                factory = Class.forName(factoryClassName);
            } else {
                try {
                    factory = classloader.loadClass(factoryClassName);
                } catch (ClassNotFoundException cnfe) {}
            }
            if (factory == null) {
                classloader = FactoryFinder.class.getClassLoader();
                factory = classloader.loadClass(factoryClassName);
            }
            return factory.newInstance();
        } catch (ClassNotFoundException classnotfoundexception) {
            throw new SOAPException("Provider " + factoryClassName + " not found", classnotfoundexception);
        } catch (Exception exception) {
            throw new SOAPException("Provider " + factoryClassName + " could not be instantiated: " + exception, exception);
        }
    }

    /**
     * Instantiates a factory object given the factory's property name and the
     * default class name.
     *
     * @param factoryPropertyName
     * @param defaultFactoryClassName
     * @return a factory object
     * @throws SOAPException
     */
    static Object find(String factoryPropertyName, String defaultFactoryClassName) throws SOAPException {
        try {
            String factoryClassName = SystemProperties.getProperty(factoryPropertyName);
            if (factoryClassName != null) {
                return newInstance(factoryClassName);
            }
        } catch (SecurityException securityexception) {}

        try {
            String propertiesFileName = SystemProperties.getProperty("java.home")
                                        + File.separator + "lib"
                                        + File.separator + "jaxm.properties";
            File file = new File(propertiesFileName);
            if (file.exists()) {
                FileInputStream fileInput = new FileInputStream(file);
                Properties properties = new Properties();
                properties.load(fileInput);
                fileInput.close();
                String factoryClassName = properties.getProperty(factoryPropertyName);
                return newInstance(factoryClassName);
            }
        } catch (Exception exception1) {}

        String factoryResource = "META-INF/services/" + factoryPropertyName;

        try {
            InputStream inputstream = getResource(factoryResource);
            if (inputstream != null) {
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream, "UTF-8"));
                String factoryClassName = bufferedreader.readLine();
                bufferedreader.close();
                if ((factoryClassName != null) && !"".equals(factoryClassName)) {
                    return newInstance(factoryClassName);
                }
            }
        } catch (Exception exception2) {}

        if (defaultFactoryClassName == null) {
            throw new SOAPException("Provider for " + factoryPropertyName + " cannot be found", null);
        } else {
            return newInstance(defaultFactoryClassName);
        }
    }

    /**
     * Returns an input stream for the specified resource.
     *
     * <p>This method will firstly try
     * <code>ClassLoader.getSystemResourceAsStream()</code> then
     * the class loader of the current thread with
     * <code>getResourceAsStream()</code> and finally attempt
     * <code>getResourceAsStream()</code> on
     * <code>FactoryFinder.class.getClassLoader()</code>.
     *
     * @param factoryResource  the resource name
     * @return  an InputStream that can be used to read that resource, or
     *              <code>null</code> if the resource could not be resolved
     */
    private static InputStream getResource(String factoryResource) {
        ClassLoader classloader = null;
        try {
            classloader = Thread.currentThread().getContextClassLoader();
        } catch (SecurityException securityexception) {}

        InputStream inputstream;
        if (classloader == null) {
            inputstream = ClassLoader.getSystemResourceAsStream(factoryResource);
        } else {
            inputstream = classloader.getResourceAsStream(factoryResource);
        }

        if (inputstream == null) {
            inputstream = FactoryFinder.class.getClassLoader().getResourceAsStream(factoryResource);
        }
        return inputstream;
    }
}
