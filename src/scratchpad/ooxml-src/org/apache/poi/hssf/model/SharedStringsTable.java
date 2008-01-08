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

package org.apache.poi.hssf.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openxml4j.opc.PackagePart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class SharedStringsTable extends LinkedList<String> {

    private static final String MAIN_SML_NS_URI = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";

    /** XXX: should have been using an XMLBeans object, but it cannot parse the sharedStrings schema, so we'll use DOM temporarily.
    CTSst sst;
    */

    private PackagePart part;

    private DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    
    private DocumentBuilder parser;
    
    public SharedStringsTable(PackagePart part) throws IOException {
        this.part = part;
        InputStream is = part.getInputStream();
        try {
            builderFactory.setNamespaceAware(true);
            this.parser = builderFactory.newDocumentBuilder();
            readFrom(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) is.close();
        }


    }

    public void readFrom(InputStream is) throws IOException, SAXException {
        Document doc = parser.parse(is);
        Element root = doc.getDocumentElement();
        NodeList sis = root.getElementsByTagNameNS(MAIN_SML_NS_URI, "si");
        for (int i = 0 ; i < sis.getLength() ; ++i) {
            Element si = (Element) sis.item(i);
            NodeList ts = si.getElementsByTagNameNS(MAIN_SML_NS_URI, "t");
            String t = "";
            if (ts.getLength() > 0 && ts.item(0).getFirstChild() != null) {
                t = ts.item(0).getFirstChild().getNodeValue();
                add(t);
            }
        }
    }
}
