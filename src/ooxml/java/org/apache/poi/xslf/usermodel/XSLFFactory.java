/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xslf.usermodel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLFactory;
import org.apache.poi.ooxml.POIXMLRelation;

/**
 * Instantiates sub-classes of POIXMLDocumentPart depending on their relationship type
 */
public final class XSLFFactory extends POIXMLFactory  {
    private XSLFFactory(){

    }

    private static final XSLFFactory inst = new XSLFFactory();

    public static XSLFFactory getInstance(){
        return inst;
    }

    /**
     * @since POI 3.14-Beta1
     */
    @Override
    protected POIXMLRelation getDescriptor(String relationshipType) {
        return XSLFRelation.getInstance(relationshipType);
    }

    /**
     * @since POI 3.14-Beta1
     */
    @Override
    protected POIXMLDocumentPart createDocumentPart
        (Class<? extends POIXMLDocumentPart> cls, Class<?>[] classes, Object[] values)
    throws SecurityException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<? extends POIXMLDocumentPart> constructor = cls.getDeclaredConstructor(classes);
        return constructor.newInstance(values);
    }
}
