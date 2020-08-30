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

package org.apache.poi.xslf.util;

import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.sl.draw.EmbeddedExtractor.EmbeddedPart;
import org.apache.poi.util.Internal;

@Internal
abstract class MFProxy implements Closeable {
    boolean ignoreParse;
    boolean quite;

    void setIgnoreParse(boolean ignoreParse) {
        this.ignoreParse = ignoreParse;
    }

    void setQuite(boolean quite) {
        this.quite = quite;
    }

    abstract void parse(File file) throws IOException;
    abstract void parse(InputStream is) throws IOException;

    abstract Dimension2D getSize();

    void setSlideNo(int slideNo) {}

    abstract String getTitle();
    abstract void draw(Graphics2D ctx);

    int getSlideCount() { return 1; }

    Set<Integer> slideIndexes(String range) {
        return Collections.singleton(1);
    }

    abstract GenericRecord getRoot();

    abstract Iterable<EmbeddedPart> getEmbeddings(int slideNo);

    abstract void setDefaultCharset(Charset charset);
}
