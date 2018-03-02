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
package org.apache.xmlbeans.impl.jam.internal.javadoc;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ProgramElementDoc;
import org.apache.xmlbeans.impl.jam.internal.elements.ElementContext;
import org.apache.xmlbeans.impl.jam.provider.JamLogger;
import org.apache.xmlbeans.impl.jam.mutable.MAnnotatedElement;
import org.apache.xmlbeans.impl.jam.mutable.MClass;


/**
 * This class is required by JAM so it runs under JDK1.5
 * Since XmlBeans doesn't require 1.5 in order to run
 * this stub replaces the real impl.
 */
public final class JavadocTigerDelegateImpl_150 extends JavadocTigerDelegate
{
    public void init(ElementContext ctx)
    {}

    public void init(JamLogger logger)
    {}

    public void populateAnnotationTypeIfNecessary(ClassDoc cd,
        MClass clazz,
        JavadocClassBuilder builder)
    {
    }


    // ========================================================================
    // OLD STUFF remove someday


    public void extractAnnotations(MAnnotatedElement dest, ProgramElementDoc src)
    {
    }

    public void extractAnnotations(MAnnotatedElement dest,
                                 ExecutableMemberDoc method,
                                 Parameter src)
    {
    }

    public boolean isEnum(ClassDoc cd)
    {
        return false; // under 1.4, nothing is enum
    }
}
