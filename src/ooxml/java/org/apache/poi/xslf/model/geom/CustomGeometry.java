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

import org.openxmlformats.schemas.drawingml.x2006.main.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Definition of a custom geometric shape
 *
 * @author Yegor Kozlov
 */
public class CustomGeometry implements Iterable<Path>{
    List<Guide> adjusts = new ArrayList<Guide>();
    List<Guide> guides = new ArrayList<Guide>();
    List<Path> paths = new ArrayList<Path>();
    Path textBounds;

    public CustomGeometry(CTCustomGeometry2D geom) {
        CTGeomGuideList avLst = geom.getAvLst();
        if(avLst != null) for(CTGeomGuide gd : avLst.getGdList()){
            adjusts.add(new AdjustValue(gd));
        }

        CTGeomGuideList gdLst = geom.getGdLst();
        if(gdLst != null) for(CTGeomGuide gd : gdLst.getGdList()){
            guides.add(new Guide(gd));
        }

        CTPath2DList pathLst = geom.getPathLst();
        if(pathLst != null) for(CTPath2D spPath : pathLst.getPathList()){
            paths.add(new Path(spPath));
        }

        if(geom.isSetRect()) {
            CTGeomRect rect = geom.getRect();
            textBounds = new Path();
            textBounds.addCommand(
                    new MoveToCommand(rect.getL().toString(), rect.getT().toString()));
            textBounds.addCommand(
                    new LineToCommand(rect.getR().toString(), rect.getT().toString()));
            textBounds.addCommand(
                    new LineToCommand(rect.getR().toString(), rect.getB().toString()));
            textBounds.addCommand(
                    new LineToCommand(rect.getL().toString(), rect.getB().toString()));
            textBounds.addCommand(
                    new ClosePathCommand());
        }
    }



    public Iterator<Path> iterator() {
        return paths.iterator();
    }

    public Path getTextBounds(){
        return textBounds;        
    }
}
