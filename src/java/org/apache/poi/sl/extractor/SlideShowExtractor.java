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

package org.apache.poi.sl.extractor;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.sl.usermodel.MasterSheet;
import org.apache.poi.sl.usermodel.Notes;
import org.apache.poi.sl.usermodel.ObjectShape;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.sl.usermodel.PlaceholderDetails;
import org.apache.poi.sl.usermodel.Shape;
import org.apache.poi.sl.usermodel.ShapeContainer;
import org.apache.poi.sl.usermodel.Sheet;
import org.apache.poi.sl.usermodel.Slide;
import org.apache.poi.sl.usermodel.SlideShow;
import org.apache.poi.sl.usermodel.TableCell;
import org.apache.poi.sl.usermodel.TableShape;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.sl.usermodel.TextRun;
import org.apache.poi.sl.usermodel.TextShape;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Common SlideShow extractor
 *
 * @since POI 4.0.0
 */
public class SlideShowExtractor<
    S extends Shape<S,P>,
    P extends TextParagraph<S,P,? extends TextRun>
> extends POITextExtractor {
    private static final POILogger LOG = POILogFactory.getLogger(SlideShowExtractor.class);

    // placeholder text for slide numbers
    private static final String SLIDE_NUMBER_PH = "‹#›";


    private SlideShow<S,P> slideshow;

    private boolean slidesByDefault = true;
    private boolean notesByDefault;
    private boolean commentsByDefault;
    private boolean masterByDefault;

    private Predicate<Object> filter = o -> true;

    public SlideShowExtractor(final SlideShow<S,P> slideshow) {
        setFilesystem(slideshow);
        this.slideshow = slideshow;
    }

    /**
     * Returns opened document
     *
     * @return the opened document
     */
    @Override
    public final Object getDocument() {
        return slideshow.getPersistDocument();
    }

    /**
     * Should a call to getText() return slide text? Default is yes
     */
    public void setSlidesByDefault(final boolean slidesByDefault) {
        this.slidesByDefault = slidesByDefault;
    }

    /**
     * Should a call to getText() return notes text? Default is no
     */
    public void setNotesByDefault(final boolean notesByDefault) {
        this.notesByDefault = notesByDefault;
    }

    /**
     * Should a call to getText() return comments text? Default is no
     */
    public void setCommentsByDefault(final boolean commentsByDefault) {
        this.commentsByDefault = commentsByDefault;
    }

    /**
     * Should a call to getText() return text from master? Default is no
     */
    public void setMasterByDefault(final boolean masterByDefault) {
        this.masterByDefault = masterByDefault;
    }

    @Override
    public POITextExtractor getMetadataTextExtractor() {
        return slideshow.getMetadataTextExtractor();
    }

    /**
     * Fetches all the slide text from the slideshow, but not the notes, unless
     * you've called setSlidesByDefault() and setNotesByDefault() to change this
     */
    @Override
    public String getText() {
        final StringBuilder sb = new StringBuilder();
        for (final Slide<S, P> slide : slideshow.getSlides()) {
            getText(slide, sb::append);
        }

        return sb.toString();
    }

    public String getText(final Slide<S,P> slide) {
        final StringBuilder sb = new StringBuilder();
        getText(slide, sb::append);
        return sb.toString();
    }


    private void getText(final Slide<S,P> slide, final Consumer<String> consumer) {
        if (slidesByDefault) {
            printShapeText(slide, consumer);
        }

        if (masterByDefault) {
            final MasterSheet<S,P> ms = slide.getMasterSheet();
            printSlideMaster(ms, consumer);

            // only print slide layout, if it's a different instance
            final MasterSheet<S,P> sl = slide.getSlideLayout();
            if (sl != ms) {
                printSlideMaster(sl, consumer);
            }
        }

        if (commentsByDefault) {
            printComments(slide, consumer);
        }

        if (notesByDefault) {
            printNotes(slide, consumer);
        }
    }

    private void printSlideMaster(final MasterSheet<S,P> master, final Consumer<String> consumer) {
        if (master == null) {
            return;
        }
        for (final Shape<S,P> shape : master) {
            if (shape instanceof TextShape) {
                final TextShape<S,P> ts = (TextShape<S,P>)shape;
                final String text = ts.getText();
                if (text == null || text.isEmpty() || "*".equals(text)) {
                    continue;
                }

                if (ts.isPlaceholder()) {
                    // don't bother about boiler plate text on master sheets
                    LOG.log(POILogger.INFO, "Ignoring boiler plate (placeholder) text on slide master:", text);
                    continue;
                }

                printTextParagraphs(ts.getTextParagraphs(), consumer);
            }
        }
    }

    private void printTextParagraphs(final List<P> paras, final Consumer<String> consumer) {
        printTextParagraphs(paras, consumer, "\n");
    }


    private void printTextParagraphs(final List<P> paras, final Consumer<String> consumer, String trailer) {
        printTextParagraphs(paras, consumer, trailer, SlideShowExtractor::replaceTextCap);
    }

    private void printTextParagraphs(final List<P> paras, final Consumer<String> consumer, String trailer, final Function<TextRun,String> converter) {
        for (P p : paras) {
            for (TextRun r : p) {
                if (filter.test(r)) {
                    consumer.accept(converter.apply(r));
                }
            }
            if (!trailer.isEmpty() && filter.test(trailer)) {
                consumer.accept(trailer);
            }
        }
    }

    private void printHeaderFooter(final Sheet<S,P> sheet, final Consumer<String> consumer, final Consumer<String> footerCon) {
        final Sheet<S, P> m = (sheet instanceof Slide) ? sheet.getMasterSheet() : sheet;
        addSheetPlaceholderDatails(sheet, Placeholder.HEADER, consumer);
        addSheetPlaceholderDatails(sheet, Placeholder.FOOTER, footerCon);

        if (!masterByDefault) {
            return;
        }

        // write header texts and determine footer text
        for (Shape<S, P> s : m) {
            if (!(s instanceof TextShape)) {
                continue;
            }
            final TextShape<S, P> ts = (TextShape<S, P>) s;
            final PlaceholderDetails pd = ts.getPlaceholderDetails();
            if (pd == null || !pd.isVisible() || pd.getPlaceholder() == null) {
                continue;
            }
            switch (pd.getPlaceholder()) {
                case HEADER:
                    printTextParagraphs(ts.getTextParagraphs(), consumer);
                    break;
                case FOOTER:
                    printTextParagraphs(ts.getTextParagraphs(), footerCon);
                    break;
                case SLIDE_NUMBER:
                    printTextParagraphs(ts.getTextParagraphs(), footerCon, "\n", SlideShowExtractor::replaceSlideNumber);
                    break;
                case DATETIME:
                    // currently not supported
                default:
                    break;
            }
        }
    }


    private void addSheetPlaceholderDatails(final Sheet<S,P> sheet, final Placeholder placeholder, final Consumer<String> consumer) {
        final PlaceholderDetails headerPD = sheet.getPlaceholderDetails(placeholder);
        final String headerStr = (headerPD != null) ? headerPD.getText() : null;
        if (headerStr != null && filter.test(headerPD)) {
            consumer.accept(headerStr);
        }
    }

    private void printShapeText(final Sheet<S,P> sheet, final Consumer<String> consumer) {
        final List<String> footer = new LinkedList<>();
        printHeaderFooter(sheet, consumer, footer::add);
        printShapeText((ShapeContainer<S,P>)sheet, consumer);
        footer.forEach(consumer);
    }

    @SuppressWarnings("unchecked")
    private void printShapeText(final ShapeContainer<S,P> container, final Consumer<String> consumer) {
        for (Shape<S,P> shape : container) {
            if (shape instanceof TextShape) {
                printTextParagraphs(((TextShape<S,P>)shape).getTextParagraphs(), consumer);
            } else if (shape instanceof TableShape) {
                printShapeText((TableShape<S,P>)shape, consumer);
            } else if (shape instanceof ShapeContainer) {
                printShapeText((ShapeContainer<S,P>)shape, consumer);
            }
        }
    }

    @SuppressWarnings("Duplicates")
    private void printShapeText(final TableShape<S,P> shape, final Consumer<String> consumer) {
        final int nrows = shape.getNumberOfRows();
        final int ncols = shape.getNumberOfColumns();
        for (int row = 0; row < nrows; row++) {
            String trailer = "";
            for (int col = 0; col < ncols; col++){
                TableCell<S, P> cell = shape.getCell(row, col);
                //defensive null checks; don't know if they're necessary
                if (cell != null) {
                    trailer = col < ncols-1 ? "\t" : "\n";
                    printTextParagraphs(cell.getTextParagraphs(), consumer, trailer);
                }
            }
            if (!trailer.equals("\n") && filter.test("\n")) {
                consumer.accept("\n");
            }
        }
    }

    private void printComments(final Slide<S,P> slide, final Consumer<String> consumer) {
        slide.getComments().stream().filter(filter).map(c -> c.getAuthor()+" - "+c.getText()).forEach(consumer);
    }

    private void printNotes(final Slide<S,P> slide, final Consumer<String> consumer) {
        final Notes<S, P> notes = slide.getNotes();
        if (notes == null) {
            return;
        }

        List<String> footer = new LinkedList<>();
        printHeaderFooter(notes, consumer, footer::add);
        printShapeText(notes, consumer);
        footer.forEach(consumer);
    }

    public List<? extends ObjectShape<S,P>> getOLEShapes() {
        final List<ObjectShape<S,P>> oleShapes = new ArrayList<>();
        
        for (final Slide<S,P> slide : slideshow.getSlides()) {
            addOLEShapes(oleShapes, slide);
        }
        
        return oleShapes;
    }
    
    @SuppressWarnings("unchecked")
    private void addOLEShapes(final List<ObjectShape<S,P>> oleShapes, ShapeContainer<S,P> container) {
        for (Shape<S,P> shape : container) {
            if (shape instanceof ShapeContainer) {
                addOLEShapes(oleShapes, (ShapeContainer<S,P>)shape);
            } else if (shape instanceof ObjectShape) {
                oleShapes.add((ObjectShape<S,P>)shape);
            }
        }
    }

    private static String replaceSlideNumber(TextRun tr) {
        String raw = tr.getRawText();

        if (!raw.contains(SLIDE_NUMBER_PH)) {
            return raw;
        }

        TextParagraph tp = tr.getParagraph();
        TextShape ps = (tp != null) ? tp.getParentShape() : null;
        Sheet sh = (ps != null) ? ps.getSheet() : null;
        String slideNr = (sh instanceof Slide) ? Integer.toString(((Slide)sh).getSlideNumber() + 1) : "";

        return raw.replace(SLIDE_NUMBER_PH, slideNr);
    }

    private static String replaceTextCap(TextRun tr) {
        final TextParagraph tp = tr.getParagraph();
        final TextShape sh = (tp != null) ? tp.getParentShape() : null;
        final Placeholder ph = (sh != null) ? sh.getPlaceholder() : null;

        // 0xB acts like cariage return in page titles and like blank in the others
        final char sep = (
            ph == Placeholder.TITLE ||
            ph == Placeholder.CENTERED_TITLE ||
            ph == Placeholder.SUBTITLE
        ) ? '\n' : ' ';

        // PowerPoint seems to store files with \r as the line break
        // The messes things up on everything but a Mac, so translate them to \n
        String txt = tr.getRawText();
        txt = txt.replace('\r', '\n');
        txt = txt.replace((char) 0x0B, sep);

        switch (tr.getTextCap()) {
            case ALL:
                txt = txt.toUpperCase(LocaleUtil.getUserLocale());
            case SMALL:
                txt = txt.toLowerCase(LocaleUtil.getUserLocale());
        }

        return txt;
    }

    /**
     * Extract the used codepoints for font embedding / subsetting
     * @param typeface the typeface/font family of the textruns to examine
     * @param italic use {@code true} for italic TextRuns, {@code false} for non-italic ones and
     *      {@code null} if it doesn't matter
     * @param bold use {@code true} for bold TextRuns, {@code false} for non-bold ones and
     *      {@code null} if it doesn't matter
     * @return a bitset with the marked/used codepoints
     */
    public BitSet getCodepoints(String typeface, Boolean italic, Boolean bold) {
        final BitSet glyphs = new BitSet();

        Predicate<Object> filterOld = filter;
        try {
            filter = o -> filterFonts(o, typeface, italic, bold);
            slideshow.getSlides().forEach(slide ->
                getText(slide, s -> s.codePoints().forEach(glyphs::set))
            );
        } finally {
            filter = filterOld;
        }

        return glyphs;
    }

    private static boolean filterFonts(Object o, String typeface, Boolean italic, Boolean bold) {
        if (!(o instanceof TextRun)) {
            return false;
        }
        TextRun tr = (TextRun)o;
        return
            typeface.equalsIgnoreCase(tr.getFontFamily()) &&
            (italic == null || tr.isItalic() == italic) &&
            (bold == null || tr.isBold() == bold);
    }
}
