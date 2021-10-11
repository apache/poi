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

import static org.apache.poi.xslf.model.ParagraphPropertyFetcher.getThemeProps;
import static org.apache.poi.xslf.model.ParagraphPropertyFetcher.select;

import java.util.function.Consumer;

import org.apache.poi.util.Internal;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSheet;
import org.apache.poi.xslf.usermodel.XSLFSlideMaster;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;

@Internal
public final class CharacterPropertyFetcher<T> extends PropertyFetcher<T> {
    public interface CharPropFetcher<S> {
        void fetch (CTTextCharacterProperties props, Consumer<S> val);
    }

    private final XSLFTextRun run;
    int _level;
    private final CharPropFetcher<T> fetcher;

    public CharacterPropertyFetcher(XSLFTextRun run, CharPropFetcher<T> fetcher) {
        _level = run.getParagraph().getIndentLevel();
        this.fetcher = fetcher;
        this.run = run;
    }

    public boolean fetch(XSLFShape shape) {
        // this is only called when propagating to parent styles
        try {
            fetchProp(select(shape, _level));
        } catch (XmlException ignored) {
        }
        return isSet();
    }


    public T fetchProperty(XSLFShape shape) {
        final XSLFSheet sheet = shape.getSheet();

        fetchRunProp();

        if (!(sheet instanceof XSLFSlideMaster)) {
            fetchParagraphDefaultRunProp();
            fetchShapeProp(shape);
            fetchThemeProp(shape);
        }

        fetchMasterProp();

        return isSet() ? getValue() : null;
    }

    private void fetchRunProp() {
        fetchProp(run.getRPr(false));
    }

    private void fetchParagraphDefaultRunProp() {
        if (!isSet()) {
            CTTextParagraphProperties pr = run.getParagraph().getXmlObject().getPPr();
            if (pr != null) {
                fetchProp(pr.getDefRPr());
            }
        }
    }

    private void fetchShapeProp(XSLFShape shape) {
        if (!isSet()) {
            shape.fetchShapeProperty(this);
        }
    }

    private void fetchThemeProp(XSLFShape shape) {
        if (!isSet()) {
            fetchProp(getThemeProps(shape, _level));
        }
    }

    private void fetchMasterProp() {
        // defaults for placeholders are defined in the slide master
        // TODO: determine master shape
        if (!isSet()) {
            fetchProp(run.getParagraph().getDefaultMasterStyle());
        }
    }

    private void fetchProp(CTTextParagraphProperties props) {
        if (props != null) {
            fetchProp(props.getDefRPr());
        }
    }

    private void fetchProp(CTTextCharacterProperties props) {
        if (props != null) {
            fetcher.fetch(props, this::setValue);
        }
    }
}