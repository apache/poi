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

import java.io.InputStream;
import java.util.LinkedHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.EventFilter;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.poi.sl.draw.binding.CTCustomGeometry2D;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.StaxHelper;

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
        // StAX:
        EventFilter startElementFilter = new EventFilter() {
            @Override
            public boolean accept(XMLEvent event) {
                return event.isStartElement();
            }
        };
        
        XMLInputFactory staxFactory = StaxHelper.newXMLInputFactory();
        XMLEventReader staxReader = staxFactory.createXMLEventReader(is);
        XMLEventReader staxFiltRd = staxFactory.createFilteredReader(staxReader, startElementFilter);
        // ignore StartElement:
        /* XMLEvent evDoc = */ staxFiltRd.nextEvent();
        // JAXB:
        JAXBContext jaxbContext = JAXBContext.newInstance(BINDING_PACKAGE);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        long cntElem = 0;
        while (staxFiltRd.peek() != null) {
            StartElement evRoot = (StartElement)staxFiltRd.peek();
            String name = evRoot.getName().getLocalPart();
            JAXBElement<CTCustomGeometry2D> el = unmarshaller.unmarshal(staxReader, CTCustomGeometry2D.class);
            CTCustomGeometry2D cus = el.getValue();
            cntElem++;
            
            if(containsKey(name)) {
                LOG.log(POILogger.WARN, "Duplicate definition of " + name);
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
            // use a local object first to not assign a partly constructed object
            // in case of failure
            PresetGeometries lInst = new PresetGeometries();
            try {
                InputStream is = PresetGeometries.class.
                    getResourceAsStream("presetShapeDefinitions.xml");
                try {
                    lInst.init(is);
                } finally {
                    is.close();
                }
            } catch (Exception e){
                throw new RuntimeException(e);
            }
            _inst = lInst;
        }

        return _inst;
    }
}
