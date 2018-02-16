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

import org.apache.xmlbeans.impl.xb.xmlconfig.Extensionconfig;
import org.apache.xmlbeans.InterfaceExtension;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.jam.JMethod;
import org.apache.xmlbeans.impl.jam.JClass;
import org.apache.xmlbeans.impl.jam.JParameter;
import org.apache.xmlbeans.impl.jam.JamClassLoader;

public class InterfaceExtensionImpl implements InterfaceExtension
{
    private NameSet _xbeanSet;
    private String _interfaceClassName;
    private String _delegateToClassName;
    private MethodSignatureImpl[] _methods;

    static InterfaceExtensionImpl newInstance(JamClassLoader loader, NameSet xbeanSet, Extensionconfig.Interface intfXO)
    {
        InterfaceExtensionImpl result = new InterfaceExtensionImpl();

        result._xbeanSet = xbeanSet;
        JClass interfaceJClass = validateInterface(loader, intfXO.getName(), intfXO);


        if (interfaceJClass == null)
        {
            BindingConfigImpl.error("Interface '" + intfXO.getStaticHandler() + "' not found.", intfXO);
            return null;
        }

        result._interfaceClassName = interfaceJClass.getQualifiedName();

        result._delegateToClassName = intfXO.getStaticHandler();
        JClass delegateJClass = validateClass(loader, result._delegateToClassName, intfXO);

        if (delegateJClass == null) // no HandlerClass
        {
            BindingConfigImpl.warning("Handler class '" + intfXO.getStaticHandler() + "' not found on classpath, skip validation.", intfXO);
            return result;
        }

        if (!result.validateMethods(interfaceJClass, delegateJClass, intfXO))
            return null;

        return result;
    }

    private static JClass validateInterface(JamClassLoader loader, String intfStr, XmlObject loc)
    {
        return validateJava(loader, intfStr, true, loc);
    }

    static JClass validateClass(JamClassLoader loader, String clsStr, XmlObject loc)
    {
        return validateJava(loader, clsStr, false, loc);
    }

    static JClass validateJava(JamClassLoader loader, String clsStr, boolean isInterface, XmlObject loc)
    {
        if (loader==null)
            return null;

        final String ent = isInterface ? "Interface" : "Class";
        JClass cls = loader.loadClass(clsStr);

        if (cls==null || cls.isUnresolvedType())
        {
            BindingConfigImpl.error(ent + " '" + clsStr + "' not found.", loc);
            return null;
        }

        if ( (isInterface && !cls.isInterface()) ||
                (!isInterface && cls.isInterface()))
        {
            BindingConfigImpl.error("'" + clsStr + "' must be " +
                (isInterface ? "an interface" : "a class") + ".", loc);
        }

        if (!cls.isPublic())
        {
            BindingConfigImpl.error(ent + " '" + clsStr + "' is not public.", loc);
        }

        return cls;
    }

    private boolean validateMethods(JClass interfaceJClass, JClass delegateJClass, XmlObject loc)
    {
        //assert _delegateToClass != null : "Delegate to class handler expected.";
        boolean valid = true;

        JMethod[] interfaceMethods = interfaceJClass.getMethods();
        _methods = new MethodSignatureImpl[interfaceMethods.length];

        for (int i = 0; i < interfaceMethods.length; i++)
        {
            JMethod method = validateMethod(interfaceJClass, delegateJClass, interfaceMethods[i], loc);
            if (method != null)
                _methods[i] = new MethodSignatureImpl(getStaticHandler(), method);
            else
                valid = false;
        }


        return valid;
    }

    private JMethod validateMethod(JClass interfaceJClass, JClass delegateJClass, JMethod method, XmlObject loc)
    {
        String methodName = method.getSimpleName();
        JParameter[] params = method.getParameters();
        JClass returnType = method.getReturnType();

        JClass[] delegateParams = new JClass[params.length+1];
        delegateParams[0] = returnType.forName("org.apache.xmlbeans.XmlObject");
        for (int i = 1; i < delegateParams.length; i++)
        {
            delegateParams[i] = params[i-1].getType();
        }

        JMethod handlerMethod = null;
        handlerMethod = getMethod(delegateJClass, methodName, delegateParams);
        if (handlerMethod==null)
        {
            BindingConfigImpl.error("Handler class '" + delegateJClass.getQualifiedName() + "' does not contain method " + methodName + "(" + listTypes(delegateParams) + ")", loc);
            return null;
        }

        // check for throws exceptions
        JClass[] intfExceptions = method.getExceptionTypes();
        JClass[] delegateExceptions = handlerMethod.getExceptionTypes();
        if ( delegateExceptions.length!=intfExceptions.length )
        {
            BindingConfigImpl.error("Handler method '" + delegateJClass.getQualifiedName() + "." + methodName + "(" + listTypes(delegateParams) +
                ")' must declare the same exceptions as the interface method '" + interfaceJClass.getQualifiedName() + "." + methodName + "(" + listTypes(params), loc);
            return null;
        }

        for (int i = 0; i < delegateExceptions.length; i++)
        {
            if ( delegateExceptions[i]!=intfExceptions[i] )
            {
                BindingConfigImpl.error("Handler method '" + delegateJClass.getQualifiedName() + "." + methodName + "(" + listTypes(delegateParams) +
                    ")' must declare the same exceptions as the interface method '" + interfaceJClass.getQualifiedName() + "." + methodName + "(" + listTypes(params), loc);
                return null;
            }
        }

        if (!handlerMethod.isPublic() || !handlerMethod.isStatic())
        {
            BindingConfigImpl.error("Method '" + delegateJClass.getQualifiedName() + "." + methodName + "(" + listTypes(delegateParams) + ")' must be declared public and static.", loc);
            return null;
        }

        if (!returnType.equals(handlerMethod.getReturnType()))
        {
            BindingConfigImpl.error("Return type for method '" + handlerMethod.getReturnType() + " " + delegateJClass.getQualifiedName() +
                    "." + methodName + "(" + listTypes(delegateParams) + ")' does not match the return type of the interface method :'" + returnType + "'.", loc);
            return null;
        }

        return method;
    }

    static JMethod getMethod(JClass cls, String name, JClass[] paramTypes)
    {
        JMethod[] methods = cls.getMethods();
        for (int i = 0; i < methods.length; i++)
        {
            JMethod method = methods[i];
            if (!name.equals(method.getSimpleName()))
                continue;

            JParameter[] mParams = method.getParameters();

            // can have methods with same name but different # of params
            if (mParams.length != paramTypes.length)
                continue;

            for (int j = 0; j < mParams.length; j++)
            {
                JParameter mParam = mParams[j];
                if (!mParam.getType().equals(paramTypes[j]))
                    continue;
            }

            return method;
        }
        return null;
    }

    private static String listTypes(JClass[] types)
    {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < types.length; i++)
        {
            JClass type = types[i];
            if (i>0)
                result.append(", ");
            result.append(emitType(type));
        }
        return result.toString();
    }

    private static String listTypes(JParameter[] params)
    {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < params.length; i++)
        {
            JClass type = params[i].getType();
            if (i>0)
                result.append(", ");
            result.append(emitType(type));
        }
        return result.toString();
    }

    public static String emitType(JClass cls)
    {
        if (cls.isArrayType())
            return emitType(cls.getArrayComponentType()) + "[]";
        else
            return cls.getQualifiedName().replace('$', '.');
    }

    /* public getters */
    public boolean contains(String fullJavaName)
    {
        return _xbeanSet.contains(fullJavaName);
    }

    public String getStaticHandler()
    {
        return _delegateToClassName;
    }

    public String getInterface()
    {
        return _interfaceClassName;
    }

    public InterfaceExtension.MethodSignature[] getMethods()
    {
        return _methods;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("  static handler: ").append(_delegateToClassName).append("\n");
        buf.append("  interface: ").append(_interfaceClassName).append("\n");
        buf.append("  name set: ").append(_xbeanSet).append("\n");

        for (int i = 0; i < _methods.length; i++)
            buf.append("  method[").append(i).append("]=").append(_methods[i]).append("\n");

        return buf.toString();
    }

    // this is used only for detecting method colisions of extending interfaces
    static class MethodSignatureImpl implements InterfaceExtension.MethodSignature
    {
        private String _intfName;  
        private final int NOTINITIALIZED = -1;
        private int _hashCode = NOTINITIALIZED;
        private String _signature;

        private String _name;
        private String _return;
        private String[] _params;
        private String[] _exceptions;

        MethodSignatureImpl(String intfName, JMethod method)
        {
            if (intfName==null || method==null)
                throw new IllegalArgumentException("Interface: " + intfName + " method: " + method);

            _intfName = intfName;
            _hashCode = NOTINITIALIZED;
            _signature = null;

            _name = method.getSimpleName();
            _return = method.getReturnType().getQualifiedName().replace('$', '.');

            JParameter[] paramTypes = method.getParameters();
            _params = new String[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++)
                _params[i] = paramTypes[i].getType().getQualifiedName().replace('$', '.');;

            JClass[] exceptionTypes = method.getExceptionTypes();
            _exceptions = new String[exceptionTypes.length];
            for (int i = 0; i < exceptionTypes.length; i++)
                _exceptions[i] = exceptionTypes[i].getQualifiedName().replace('$', '.');
        }

        String getInterfaceName()
        {
            return _intfName;
        }

        public String getName()
        {
            return _name;
        }

        public String getReturnType()
        {
            return _return;
        }

        public String[] getParameterTypes()
        {
            return _params;
        }

        public String[] getExceptionTypes()
        {
            return _exceptions;
        }

        public boolean equals(Object o)
        {
            if ( !(o instanceof MethodSignatureImpl))
                return false;

            MethodSignatureImpl ms = (MethodSignatureImpl)o;

            if (!ms.getName().equals(getName()) )
                return false;

            String[] params = getParameterTypes();
            String[] msParams = ms.getParameterTypes();

            if (msParams.length != params.length )
                return false;

            for (int i = 0; i < params.length; i++)
            {
                if (!msParams[i].equals(params[i]))
                    return false;
            }

            if (!_intfName.equals(ms._intfName))
                return false;
            
            return true;
        }

        public int hashCode()
        {
            if (_hashCode!=NOTINITIALIZED)
                return _hashCode;

            int hash = getName().hashCode();

            String[] params = getParameterTypes();

            for (int i = 0; i < params.length; i++)
            {
                hash *= 19;
                hash += params[i].hashCode();
            }

            hash += 21 * _intfName.hashCode();

            _hashCode = hash;
            return _hashCode;
        }

        String getSignature()
        {
            if (_signature!=null)
                return _signature;

            StringBuffer sb = new StringBuffer(60);
            sb.append(_name).append("(");
            for (int i = 0; i < _params.length; i++)
                sb.append((i == 0 ? "" : " ,")).append(_params[i]);
            sb.append(")");

            _signature = sb.toString();

            return _signature;
        }

        public String toString()
        {
            StringBuffer buf = new StringBuffer();

            buf.append(getReturnType()).append(" ").append(getSignature());

            return buf.toString();
        }
    }
}
