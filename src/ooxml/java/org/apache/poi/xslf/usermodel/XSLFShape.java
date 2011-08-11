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

import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.util.Beta;
import org.apache.xmlbeans.XmlObject;

import java.awt.geom.Rectangle2D;

/**
 * @author Yegor Kozlov
 */
@Beta
public abstract class XSLFShape {


    public abstract Rectangle2D getAnchor();

    public abstract void setAnchor(Rectangle2D anchor);

    public abstract XmlObject getXmlObject();

    public abstract String getShapeName();

    public abstract int getShapeId();

    void onCopy(XSLFSheet srcSheet){

    }
}