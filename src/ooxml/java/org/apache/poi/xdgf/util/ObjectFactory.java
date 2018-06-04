/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.xdgf.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.xmlbeans.XmlObject;


public class ObjectFactory<T, X extends XmlObject> {

    Map<String, Constructor<? extends T>> _types = new HashMap<>();

    public void put(String typeName, Class<? extends T> cls, Class<?>... varargs) throws NoSuchMethodException, SecurityException {
        _types.put(typeName, cls.getDeclaredConstructor(varargs));
    }

    public T load(String name, Object... varargs) {
        Constructor<? extends T> constructor = _types.get(name);
        if (constructor == null) {

            @SuppressWarnings("unchecked")
            X xmlObject = (X) varargs[0];

            String typeName = xmlObject.schemaType().getName().getLocalPart();
            throw new POIXMLException("Invalid '" + typeName + "' name '" + name + "'");
        }

        try {
            return constructor.newInstance(varargs);
        } catch (InvocationTargetException e) {
            throw new POIXMLException(e.getCause());
        } catch (Exception e) {
            throw new POIXMLException(e);
        }
    }

}
