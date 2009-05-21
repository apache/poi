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

package org.apache.poi.hslf.model;

import org.apache.poi.hslf.exceptions.HSLFException;

import java.util.HashMap;
import java.lang.reflect.Field;

/**
 * Contains all known shape types in PowerPoint
 *
 * @author Yegor Kozlov
 */
public final class ShapeTypes implements org.apache.poi.sl.usermodel.ShapeTypes {
    /**
     * Return name of the shape by id
     * @param type  - the id of the shape, one of the static constants defined in this class
     * @return  the name of the shape
     */
    public static String typeName(int type) {
        String name = (String)types.get(new Integer(type));
        return name;
    }

    public static HashMap types;
    static {
        types = new HashMap();
        try {
            Field[] f = org.apache.poi.sl.usermodel.ShapeTypes.class.getFields();
            for (int i = 0; i < f.length; i++){
                Object val = f[i].get(null);
                if (val instanceof Integer) {
                    types.put(val, f[i].getName());
                }
            }
        } catch (IllegalAccessException e){
            throw new HSLFException("Failed to initialize shape types");
        }
    }

}
