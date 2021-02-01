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

import static org.apache.poi.ooxml.util.XPathHelper.selectProperty;
import static org.apache.poi.xslf.model.ParagraphPropertyFetcher.DML_NS;
import static org.apache.poi.xslf.model.ParagraphPropertyFetcher.PML_NS;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBodyProperties;

public abstract class TextBodyPropertyFetcher<T> extends PropertyFetcher<T> {
    private static final QName[] TX_BODY = { new QName(PML_NS, "txBody") };
    private static final QName[] BODY_PR = { new QName(DML_NS, "bodyPr") };

    public boolean fetch(XSLFShape shape) {
        CTTextBodyProperties props = null;
        try {
            props = selectProperty(shape.getXmlObject(),
                    CTTextBodyProperties.class, TextBodyPropertyFetcher::parse, TX_BODY, BODY_PR);
            return (props != null) && fetch(props);
        } catch (XmlException e) {
            return false;
        }
    }

    private static CTTextBodyProperties parse(XMLStreamReader reader) throws XmlException {
        CTTextBody body = CTTextBody.Factory.parse(reader);
        return (body != null) ? body.getBodyPr() : null;
    }

    public abstract boolean fetch(CTTextBodyProperties props);

}