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
package org.apache.xmlbeans.impl.jam.internal.reflect;

import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotatedElement;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotation;
import org.apache.xmlbeans.impl.jam.mutable.MClass;
import org.apache.xmlbeans.impl.jam.mutable.MConstructor;
import org.apache.xmlbeans.impl.jam.mutable.MField;
import org.apache.xmlbeans.impl.jam.mutable.MMember;
import org.apache.xmlbeans.impl.jam.mutable.MParameter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


/**
 * This class is required by JAM so it runs under JDK1.5
 * Since XmlBeans doesn't require 1.5 in order to run
 * this stub replaces the real impl.
 */
public final class ReflectTigerDelegateImpl_150 extends ReflectTigerDelegate
{
  // ========================================================================
  // Reflect15Delegate implementation

    public void populateAnnotationTypeIfNecessary(Class cd,
                                                MClass clazz,
                                                ReflectClassBuilder builder)
    {
    }

    public void extractAnnotations(MMember dest, Method src)
    {
    }

    public void extractAnnotations(MConstructor dest, Constructor src)
    {
    }

    public void extractAnnotations(MField dest, Field src)
    {
    }

    public void extractAnnotations(MClass dest, Class src)
    {
    }

    public void extractAnnotations(MParameter dest, Method src,
                                 int paramNum)
    {
    }

    public void extractAnnotations(MParameter dest, Constructor src,
                                 int paramNum)
    {
    }

    public boolean isEnum(Class clazz)
    {   return false; }

    public Constructor getEnclosingConstructor(Class clazz)
    {
        return null; // JDK1.4 doesn't support this
    }

    public Method getEnclosingMethod(Class clazz)
    {
        return null; // JDK1.4 doesn't support this
    }
}
