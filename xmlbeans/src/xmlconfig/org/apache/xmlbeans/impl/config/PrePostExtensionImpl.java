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

package org.apache.xmlbeans.impl.config;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.PrePostExtension;
import org.apache.xmlbeans.impl.jam.JamClassLoader;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JMethod;
import org.apache.xmlbeans.impl.xb.xmlconfig.Extensionconfig;


public class PrePostExtensionImpl implements PrePostExtension
{

    private static JClass[] PARAMTYPES_PREPOST = null; //new JClass[]{int.class, XmlObject.class, QName.class, boolean.class, int.class};
    private static final String[] PARAMTYPES_STRING = new String[] {"int", "org.apache.xmlbeans.XmlObject",
        "javax.xml.namespace.QName", "boolean", "int"};
    private static final String SIGNATURE;
    static
    {
        String sig = "(";
        for (int i = 0; i < PARAMTYPES_STRING.length; i++)
        {
            String t = PARAMTYPES_STRING[i];
            if (i!=0)
                sig += ", ";
            sig += t;
        }
        SIGNATURE = sig + ")";
    }

    private NameSet _xbeanSet;
    private JClass _delegateToClass;
    private String _delegateToClassName;
    private JMethod _preSet;
    private JMethod _postSet;

    static PrePostExtensionImpl newInstance(JamClassLoader jamLoader, NameSet xbeanSet, Extensionconfig.PrePostSet prePostXO)
    {
        if (prePostXO==null)
            return null;

        PrePostExtensionImpl result = new PrePostExtensionImpl();

        result._xbeanSet = xbeanSet;
        result._delegateToClassName = prePostXO.getStaticHandler();
        result._delegateToClass = InterfaceExtensionImpl.validateClass(jamLoader, result._delegateToClassName, prePostXO);

        if ( result._delegateToClass==null ) // no HandlerClass
        {
            BindingConfigImpl.warning("Handler class '" + prePostXO.getStaticHandler() + "' not found on classpath, skip validation.", prePostXO);
            return result;
        }

        if (!result.lookAfterPreAndPost(jamLoader, prePostXO))
            return null;

        return result;
    }

    private boolean lookAfterPreAndPost(JamClassLoader jamLoader, XmlObject loc)
    {
        assert _delegateToClass!=null : "Delegate to class handler expected.";
        boolean valid = true;

        initParamPrePost(jamLoader);

        _preSet = InterfaceExtensionImpl.getMethod(_delegateToClass, "preSet", PARAMTYPES_PREPOST);
        if (_preSet==null)
        {} // not available is ok, _preSet will be null

        if (_preSet!=null && !_preSet.getReturnType().equals(jamLoader.loadClass("boolean")))
        {
            // just emit an warning and don't remember as a preSet
            BindingConfigImpl.warning("Method '" + _delegateToClass.getSimpleName() +
                ".preSet" + SIGNATURE + "' " +
                "should return boolean to be considered for a preSet handler.", loc);
            _preSet = null;
        }

        _postSet = InterfaceExtensionImpl.getMethod(_delegateToClass, "postSet", PARAMTYPES_PREPOST);
        if (_postSet==null)
        {} // not available is ok, _postSet will be null

        if (_preSet==null && _postSet==null)
        {
            BindingConfigImpl.error("prePostSet handler specified '" + _delegateToClass.getSimpleName() +
                "' but no preSet" + SIGNATURE + " or " +
                "postSet" + SIGNATURE + " methods found.", loc);
            valid = false;
        }

        return valid;
    }

    private void initParamPrePost(JamClassLoader jamLoader)
    {
        if (PARAMTYPES_PREPOST==null)
        {
            PARAMTYPES_PREPOST = new JClass[PARAMTYPES_STRING.length];
            for (int i = 0; i < PARAMTYPES_PREPOST.length; i++)
            {
                PARAMTYPES_PREPOST[i] = jamLoader.loadClass(PARAMTYPES_STRING[i]);
                if (PARAMTYPES_PREPOST[i]==null)
                {
                    throw new IllegalStateException("JAM should have access to the following types " + SIGNATURE);
                }
            }
        }
    }

    // public methods
    public NameSet getNameSet()
    {
        return _xbeanSet;
    }

    public boolean contains(String fullJavaName)
    {
        return _xbeanSet.contains(fullJavaName);
    }

    public boolean hasPreCall()
    {
        return _preSet!=null;
    }

    public boolean hasPostCall()
    {
        return _postSet!=null;
    }

    public String getStaticHandler()
    {
        return _delegateToClassName;
    }

    /**
     * Returns the name of the handler in a form that can be put in a java source.
     */
    public String getHandlerNameForJavaSource()
    {
        // used only in validation
        if (_delegateToClass==null)
            return null;

        return InterfaceExtensionImpl.emitType(_delegateToClass);
    }

    boolean hasNameSetIntersection(PrePostExtensionImpl ext)
    {
        return !NameSet.EMPTY.equals(_xbeanSet.intersect(ext._xbeanSet));
    }

}
