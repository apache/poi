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

package org.apache.poi.sl.draw.geom;

import java.io.*;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;

import javax.xml.bind.*;
import javax.xml.stream.*;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.poi.sl.draw.binding.CTCustomGeometry2D;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * 
 */
public class PresetGeometries extends LinkedHashMap<String, CustomGeometry> {
    private final static POILogger LOG = POILogFactory.getLogger(PresetGeometries.class);
    protected final static String BINDING_PACKAGE = "org.apache.poi.sl.draw.binding";
    
    protected static PresetGeometries _inst;

    protected PresetGeometries(){}

    @SuppressWarnings("unused")
    public void init(InputStream is) throws XMLStreamException, JAXBException {
        // Reader xml = new InputStreamReader( is, Charset.forName("UTF-8") );
        

        // StAX:
        EventFilter startElementFilter = new EventFilter() {
            @Override
            public boolean accept(XMLEvent event) {
                return event.isStartElement();
            }
        };
        
        long cntElem = 0;
        XMLInputFactory staxFactory = XMLInputFactory.newInstance();
        XMLEventReader staxReader = staxFactory.createXMLEventReader(is);
        XMLEventReader staxFiltRd = staxFactory.createFilteredReader(staxReader, startElementFilter);
        // ignore StartElement:
        XMLEvent evDoc = staxFiltRd.nextEvent();
        // JAXB:
        JAXBContext jaxbContext = JAXBContext.newInstance(BINDING_PACKAGE);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        while (staxFiltRd.peek() != null) {
            StartElement evRoot = (StartElement)staxFiltRd.peek();
            String name = evRoot.getName().getLocalPart();
            JAXBElement<CTCustomGeometry2D> el = unmarshaller.unmarshal(staxReader, CTCustomGeometry2D.class);
            CTCustomGeometry2D cus = el.getValue();
            cntElem++;
            
            if(containsKey(name)) {
                LOG.log(POILogger.WARN, "Duplicate definoition of " + name);
            }
            put(name, new CustomGeometry(cus));
        }       
    }
    
    /**
     * Convert a single CustomGeometry object, i.e. from xmlbeans
     */
    public static CustomGeometry convertCustomGeometry(XMLStreamReader staxReader) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(BINDING_PACKAGE);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            JAXBElement<CTCustomGeometry2D> el = unmarshaller.unmarshal(staxReader, CTCustomGeometry2D.class);
            return new CustomGeometry(el.getValue());
        } catch (JAXBException e) {
            LOG.log(POILogger.ERROR, "Unable to parse single custom geometry", e);
            return null;
        }
    }

    public static synchronized PresetGeometries getInstance(){
        if(_inst == null) {
            _inst = new PresetGeometries();
            try {
                InputStream is = PresetGeometries.class.
                    getResourceAsStream("presetShapeDefinitions.xml");
                _inst.init(is);
                is.close();
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        }

        return _inst;
    }

}
