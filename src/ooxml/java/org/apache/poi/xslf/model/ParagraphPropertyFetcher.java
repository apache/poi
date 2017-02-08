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

package org.apache.poi.xslf.model;

import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;

/**
 *
 * @author Yegor Kozlov
 */
public abstract class ParagraphPropertyFetcher<T> extends PropertyFetcher<T> {
    int _level;

    public ParagraphPropertyFetcher(int level) {
        _level = level;
    }

    public boolean fetch(XSLFShape shape) {

        XmlObject[] o = shape.getXmlObject().selectPath(
                "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' " +
                "declare namespace a='http://schemas.openxmlformats.org/drawingml/2006/main' " +
                ".//p:txBody/a:lstStyle/a:lvl" + (_level + 1) + "pPr"
        );
        if (o.length == 1) {
            CTTextParagraphProperties props = (CTTextParagraphProperties) o[0];
            return fetch(props);
        }
        return false;
    }

    public abstract boolean fetch(CTTextParagraphProperties props);
}