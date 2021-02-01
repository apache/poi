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

package org.apache.poi.xdgf.usermodel.section;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.microsoft.schemas.office.visio.x2012.main.SectionType;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.util.Internal;
import org.apache.poi.xdgf.usermodel.XDGFSheet;

@Internal
enum XDGFSectionTypes {
    LINE_GRADIENT("LineGradient", GenericSection::new),
    FILL_GRADIENT("FillGradient", GenericSection::new),
    CHARACTER("Character", CharacterSection::new),
    PARAGRAPH("Paragraph", GenericSection::new),
    TABS("Tabs", GenericSection::new),
    SCRATCH("Scratch", GenericSection::new),
    CONNECTION("Connection", GenericSection::new),
    CONNECTION_ABCD("ConnectionABCD", GenericSection::new),
    FIELD("Field", GenericSection::new),
    CONTROL("Control", GenericSection::new),
    GEOMETRY("Geometry", GeometrySection::new),
    ACTIONS("Actions", GenericSection::new),
    LAYER("Layer", GenericSection::new),
    USER("User", GenericSection::new),
    PROPERTY("Property", GenericSection::new),
    HYPERLINK("Hyperlink", GenericSection::new),
    REVIEWER("Reviewer", GenericSection::new),
    ANNOTATION("Annotation", GenericSection::new),
    ACTION_TAG("ActionTag", GenericSection::new);

    private final String sectionType;
    private final BiFunction<SectionType, XDGFSheet, ? extends XDGFSection> constructor;

    XDGFSectionTypes(String sectionType, BiFunction<SectionType, XDGFSheet, ? extends XDGFSection> constructor) {
        this.sectionType = sectionType;
        this.constructor = constructor;
    }

    public String getSectionType() {
        return sectionType;
    }

    public static XDGFSection load(SectionType section, XDGFSheet containingSheet) {
        final String name = section.getN();
        XDGFSectionTypes l = LOOKUP.get(name);
        if (l == null) {
            final String typeName = section.schemaType().getName().getLocalPart();
            throw new POIXMLException("Invalid '" + typeName + "' name '" + name + "'");
        }
        return l.constructor.apply(section, containingSheet);
    }

    private static final Map<String, XDGFSectionTypes> LOOKUP =
        Stream.of(values()).collect(Collectors.toMap(XDGFSectionTypes::getSectionType, Function.identity()));
}
