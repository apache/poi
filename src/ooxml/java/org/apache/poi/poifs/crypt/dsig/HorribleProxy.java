package org.apache.poi.poifs.crypt.dsig;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

import org.apache.poi.util.MethodUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

public class HorribleProxy implements InvocationHandler {
    
    private static final POILogger LOG = POILogFactory.getLogger(HorribleProxy.class);
    
	protected static interface ProxyIf {
	    Object getDelegate();
	    void setInitDeferred(boolean initDeferred);
	};
	
    private final Class<?> delegateClass;
	private Object delegateRef;
	private boolean initDeferred = true;

	protected HorribleProxy(Class<?> delegateClass, Object delegateRef) {
        this.delegateClass = delegateClass;
	    // delegateRef can be null, then we have to deal with deferred initialisation
	    this.delegateRef = delegateRef;
	}
	
	/**
	 * Create new instance by constructor
	 *
	 * @param proxyClass
	 * @param initargs
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
    @SuppressWarnings("unchecked")
    public static <T extends ProxyIf> T newProxy(Class<T> proxyClass, Object ... initargs)
	throws InvocationTargetException, IllegalAccessException, InstantiationException
	, NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		
		Class<?> delegateClass = getDelegateClass(proxyClass);
		Object delegateRef;
		if (initargs.length == 0) {
		    delegateRef = null;
		} else if (initargs.length == 1 && delegateClass.isAssignableFrom(initargs[0].getClass())) {
			delegateRef = initargs[0];
		} else {
            Class<?> paramTypes[] = updateMethodArgs(null, initargs);
            Constructor<?> cons = null;
            try {
                cons = delegateClass.getConstructor(paramTypes);
            } catch (Exception e) {
                // fallback - find constructor with same amount of parameters
                // horrible et al. ...
                cons = MethodUtils.getMatchingAccessibleConstructor(delegateClass, paramTypes);
                
                if (cons == null) {
                    throw new RuntimeException("There's no constructor for the given arguments.");
                }
            }
            
			delegateRef = cons.newInstance(initargs);
		}

		HorribleProxy hp = new HorribleProxy(delegateClass, delegateRef);
		return (T)Proxy.newProxyInstance(cl, new Class<?>[]{proxyClass}, hp);
	}
	
	/**
	 * Create new instance by factory method 
	 *
	 * @param proxyClass
	 * @param factoryMethod
	 * @param initargs
	 * @return
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 */
    @SuppressWarnings("unchecked")
	public static <T extends ProxyIf> T createProxy(Class<T> proxyClass, String factoryMethod, Object ... initargs)
    throws InvocationTargetException, IllegalAccessException, InstantiationException
    , NoSuchMethodException, ClassNotFoundException, NoSuchFieldException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        Class<?> delegateClass = getDelegateClass(proxyClass);
        Class<?> paramTypes[] = updateMethodArgs(null, initargs);
        Method facMethod = delegateClass.getMethod(factoryMethod, paramTypes);
        Object delegateRef = facMethod.invoke(null, initargs);

        if (delegateRef == null) {
            return null;
        }

        HorribleProxy hp = new HorribleProxy(delegateClass, delegateRef);
        return (T)Proxy.newProxyInstance(cl, new Class<?>[]{proxyClass}, hp);
    }

	@SuppressWarnings("unchecked")
    @Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Exception {
        String methodName = method.getName().replaceFirst("\\$.*", "");
		if (Object.class == method.getDeclaringClass()) {
	        if ("equals".equals(methodName)) {
				return proxy == args[0];
			} else if ("hashCode".equals(methodName)) {
				return System.identityHashCode(proxy);
			} else if ("toString".equals(methodName)) {
				return proxy.getClass().getName() + "@"
						+ Integer.toHexString(System.identityHashCode(proxy))
						+ ", with InvocationHandler " + this;
			} else {
				throw new IllegalStateException(String.valueOf(method));
			}
		}

        if ("getDelegate".equals(methodName)) {
            initDeferred();
            return delegateRef;
        } else if ("setInitDeferred".equals(methodName)) {
            initDeferred = (Boolean)args[0];
            return null;
        }		
		
		Class<?> methodParams[] = updateMethodArgs(method.getParameterTypes(), args);

		Object ret = null;
		boolean isStaticField = false;
		if (methodParams.length == 0) {
		    // check for static fields first
		    try {
		        Field f = delegateClass.getDeclaredField(methodName);
		        ret = f.get(delegateRef);
                if (ret == null) return null;
		        isStaticField = true;
		    } catch (NoSuchFieldException e) {
		        LOG.log(POILogger.DEBUG, "No static field '"+methodName+"' in class '"+delegateClass.getCanonicalName()+"' - trying method now.");
		    }
		}
		
		if (!isStaticField) {
    		Method methodImpl = null;
    		try {
    		    methodImpl = delegateClass.getMethod(methodName, methodParams);
    		} catch (Exception e) {
    		    // fallback - if methodName is distinct, try to use it
    		    // in case we can't provide method declaration in the Proxy interface
    		    // ... and of course, this is horrible ...
                methodImpl = MethodUtils.getMatchingAccessibleMethod(delegateClass, methodName, methodParams);

    		    if (methodImpl == null) {
    		        throw new RuntimeException("There's no method '"+methodName+"' for the given arguments.");
    		    }
    		}
    
    		if (!Modifier.isStatic(methodImpl.getModifiers())) {
    		    initDeferred();
    		}
    		ret = methodImpl.invoke(delegateRef, args);
		}
		
		Class<?> retType = method.getReturnType();
		if (retType.isArray()) {
		    if (ProxyIf.class.isAssignableFrom(retType.getComponentType())) {
		        Class<? extends ProxyIf> cType = (Class<? extends ProxyIf>)retType.getComponentType();
		        ProxyIf paRet[] = (ProxyIf[])Array.newInstance(cType, ((Object[])ret).length);
		        for (int i=0; i<((Object[])ret).length; i++) {
		            paRet[i] = newProxy(cType, ((Object[])ret)[i]);
		            paRet[i].setInitDeferred(false);
		        }
		        ret = paRet;
		    }
		} else if (ProxyIf.class.isAssignableFrom(retType)) {
		    ProxyIf pRet = newProxy((Class<? extends ProxyIf>)retType, ret);
            pRet.setInitDeferred(false);
		    ret = pRet; 
		}
		
		return ret;
	}
	
    @SuppressWarnings("unchecked")
    private static Class<?>[] updateMethodArgs(Class<?> types[], Object args[])
    throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        if (args == null) return new Class<?>[0];
        if (types == null) types = new Class<?>[args.length];
        if (types.length != args.length) {
            throw new IllegalArgumentException();
        }
        
        for (int i=0; i<types.length; i++) {
            if (types[i] == null) {
                if (args[i] == null) {
                    throw new IllegalArgumentException();
                }
                types[i] = args[i].getClass();
            }
            
            if (ProxyIf.class.isAssignableFrom(types[i])) {
                types[i] = getDelegateClass((Class<? extends ProxyIf>)types[i]);
                if (args[i] != null) {
                    args[i] = ((ProxyIf)args[i]).getDelegate();
                }
            }
        }
        return types;
    }

    private void initDeferred() throws Exception {
        if (delegateRef != null || !initDeferred) return;
        // currently works only for empty constructor
        delegateRef = delegateClass.getConstructor().newInstance();
    }
    
	private static Class<?> getDelegateClass(Class<? extends ProxyIf> proxyClass)
	throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
	    Field delegateField;
	    try {
    	    delegateField = proxyClass.getDeclaredField("delegateClass");
	    } catch (NoSuchFieldException e) {
	        // sometimes a proxy interface is returned as proxyClass
	        // this has to be asked for the real ProxyIf interface
	        Class<?> ifs[] = proxyClass.getInterfaces();
	        if (ifs == null || ifs.length != 1) {
	            throw new IllegalArgumentException();
	        }
	        delegateField = ifs[0].getDeclaredField("delegateClass");
	    }

	    String delegateClassName = (String)delegateField.get(null);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class<?> delegateClass = Class.forName(delegateClassName, true, cl);
	    return delegateClass;
	}
}
