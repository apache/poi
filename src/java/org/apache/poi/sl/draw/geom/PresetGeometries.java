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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;

import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.XMLHelper;

public final class PresetGeometries {
    private final static POILogger LOG = POILogFactory.getLogger(PresetGeometries.class);

    private final Map<String, CustomGeometry> map = new TreeMap<>();

    private static class SingletonHelper{
        private static final PresetGeometries INSTANCE = new PresetGeometries();
    }

    public static PresetGeometries getInstance(){
        return SingletonHelper.INSTANCE;
    }

    private PresetGeometries() {
        // use a local object first to not assign a partly constructed object in case of failure
        try {
            try (InputStream is = PresetGeometries.class.getResourceAsStream("presetShapeDefinitions.xml")) {
                XMLInputFactory staxFactory = XMLHelper.newXMLInputFactory();
                XMLStreamReader sr = staxFactory.createXMLStreamReader(new StreamSource(is));
                try {
                    PresetParser p = new PresetParser(PresetParser.Mode.FILE);
                    p.parse(sr);
                    p.getGeom().forEach(map::put);
                } finally {
                    sr.close();
                }
            }
        } catch (IOException | XMLStreamException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert a single CustomGeometry object, i.e. from xmlbeans
     */
    public static CustomGeometry convertCustomGeometry(XMLStreamReader staxReader) {
        try {
            PresetParser p = new PresetParser(PresetParser.Mode.SHAPE);
            p.parse(staxReader);
            return p.getGeom().values().stream().findFirst().orElse(null);
        } catch (XMLStreamException e) {
            LOG.log(POILogger.ERROR, "Unable to parse single custom geometry", e);
            return null;
        }
    }

    public CustomGeometry get(String name) {
        return name == null ? null : map.get(name);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public int size() {
        return map.size();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return (this == o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }
}
