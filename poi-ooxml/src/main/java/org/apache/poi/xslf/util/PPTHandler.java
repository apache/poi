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

import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;

import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.sl.draw.EmbeddedExtractor.EmbeddedPart;
import org.apache.poi.sl.usermodel.ObjectData;
import org.apache.poi.sl.usermodel.ObjectShape;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.SlideShowFactory;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;

/** Handler for ppt and pptx files */
@Internal
class PPTHandler extends MFProxy {
    private SlideShow<?,?> ppt;
    private Slide<?,?> slide;

    @Override
    public void parse(File file) throws IOException {
        try {
            ppt = SlideShowFactory.create(file, null, true);
        } catch (IOException e) {
            if (e.getMessage().contains("scratchpad")) {
                throw new PPTX2PNG.NoScratchpadException(e);
            } else {
                throw e;
            }
        }
        if (ppt == null) {
            throw new IOException("Unknown file format or missing poi-scratchpad.jar / poi-ooxml.jar");
        }
        slide = ppt.getSlides().get(0);
    }

    @Override
    public void parse(InputStream is) throws IOException {
        try {
            ppt = SlideShowFactory.create(is, null);
        } catch (IOException e) {
            if (e.getMessage().contains("scratchpad")) {
                throw new PPTX2PNG.NoScratchpadException(e);
            } else {
                throw e;
            }
        }
        if (ppt == null) {
            throw new IOException("Unknown file format or missing poi-scratchpad.jar / poi-ooxml.jar");
        }
        slide = ppt.getSlides().get(0);
    }

    @Override
    public Dimension2D getSize() {
        return ppt.getPageSize();
    }

    @Override
    public int getSlideCount() {
        return ppt.getSlides().size();
    }

    @Override
    public void setSlideNo(int slideNo) {
        slide = ppt.getSlides().get(slideNo-1);
    }

    @Override
    public String getTitle() {
        return slide.getTitle();
    }

    private static final String RANGE_PATTERN = "(^|,)(?<from>\\d+)?(-(?<to>\\d+))?";

    @Override
    public Set<Integer> slideIndexes(String range) {
        final Matcher matcher = Pattern.compile(RANGE_PATTERN).matcher(range);
        Spliterator<Matcher> sp = new Spliterators.AbstractSpliterator<Matcher>(range.length(), ORDERED|NONNULL){
            @Override
            public boolean tryAdvance(Consumer<? super Matcher> action) {
                boolean b = matcher.find();
                if (b) {
                    action.accept(matcher);
                }
                return b;
            }
        };

        return StreamSupport.stream(sp, false).
                flatMap(this::range).
                collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public void draw(Graphics2D ctx) {
        slide.draw(ctx);
    }

    @Override
    public void close() throws IOException {
        if (ppt != null) {
            ppt.close();
        }
    }

    @Override
    public GenericRecord getRoot() {
        return (ppt instanceof GenericRecord) ? (GenericRecord)ppt : null;
    }

    private Stream<Integer> range(Matcher m) {
        final int slideCount = ppt.getSlides().size();
        String fromStr = m.group("from");
        String toStr = m.group("to");
        int from = (fromStr == null || fromStr.isEmpty() ? 1 : Integer.parseInt(fromStr));
        int to = (toStr == null) ? from
                : (toStr.isEmpty() || ((fromStr == null || fromStr.isEmpty()) && "1".equals(toStr))) ? slideCount
                : Integer.parseInt(toStr);
        return IntStream.rangeClosed(from, to).filter(i -> i <= slideCount).boxed();
    }

    @Override
    public Iterable<EmbeddedPart> getEmbeddings(int slideNo) {
        return () -> ppt.getSlides().get(slideNo).getShapes().stream().
            filter(s -> s instanceof ObjectShape).
            map(PPTHandler::fromObjectShape).
            iterator()
        ;
    }

    private static EmbeddedPart fromObjectShape(Shape<?,?> s) {
        final ObjectShape<?,?> os = (ObjectShape<?,?>)s;
        final ObjectData od = os.getObjectData();
        EmbeddedPart embed = new EmbeddedPart();
        embed.setName(od.getFileName());
        embed.setData(() -> {
            try (InputStream is = od.getInputStream()) {
                return IOUtils.toByteArray(is);
            } catch (IOException e) {
                // TODO: change to custom runtime exception
                throw new RuntimeException(e);
            }
        });
        return embed;
    }

    @Override
    void setDefaultCharset(Charset charset) {
    }
}
