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

package org.apache.poi.xdgf.usermodel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLFactory;
import org.apache.poi.ooxml.POIXMLRelation;

/**
 * Instantiates sub-classes of POIXMLDocumentPart depending on their relationship type
 */
public class XDGFFactory extends POIXMLFactory {

    private final XDGFDocument document;

    public XDGFFactory(XDGFDocument document) {
        this.document = document;
    }

    /**
     * @since POI 3.14-Beta1
     */
    protected POIXMLRelation getDescriptor(String relationshipType) {
        return XDGFRelation.getInstance(relationshipType);
    }

    /**
     * @since POI 3.14-Beta1
     */
    @Override
    protected POIXMLDocumentPart createDocumentPart
        (Class<? extends POIXMLDocumentPart> cls, Class<?>[] classes, Object[] values)
    throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?>[] cl;
        Object[] vals;
        if (classes == null) {
            cl = new Class<?>[]{XDGFDocument.class};
            vals = new Object[]{document};
        } else {
            cl = new Class<?>[classes.length+1];
            System.arraycopy(classes, 0, cl, 0, classes.length);
            cl[classes.length] = XDGFDocument.class;
            vals = new Object[values.length+1];
            System.arraycopy(values, 0, vals, 0, values.length);
            vals[values.length] = document;
        }
        
        Constructor<? extends POIXMLDocumentPart> constructor = cls.getDeclaredConstructor(cl);
        return constructor.newInstance(vals);
    }
}
