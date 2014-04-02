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

package org.apache.poi.xslf.model.geom;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTCustomGeometry2D;

import java.io.InputStream;
import java.util.LinkedHashMap;

/**
 * Date: 10/25/11
 *
 * @author Yegor Kozlov
 */
public class PresetGeometries extends LinkedHashMap<String, CustomGeometry> {
    private static PresetGeometries _inst;

    private PresetGeometries(){
        try {
            InputStream is =
                    XMLSlideShow.class.getResourceAsStream("presetShapeDefinitions.xml");
            read(is);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void read(InputStream is) throws Exception {
        XmlObject obj = XmlObject.Factory.parse(is);
        for (XmlObject def : obj.selectPath("*/*")) {

            String name = def.getDomNode().getLocalName();
            CTCustomGeometry2D geom = CTCustomGeometry2D.Factory.parse(def.toString());

            if(containsKey(name)) {
                System.out.println("Duplicate definoition of " + name) ;
            }
            put(name, new CustomGeometry(geom));
        }
    }

    public static PresetGeometries getInstance(){
        if(_inst == null) _inst = new PresetGeometries();

        return _inst;
    }

}
