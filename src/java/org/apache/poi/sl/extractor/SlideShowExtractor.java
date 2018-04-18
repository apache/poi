package org.apache.poi.sl.extractor;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.POITextExtractor;
import org.apache.poi.sl.usermodel.Comment;
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

    private SlideShow<S,P> slideshow;

    private boolean slidesByDefault = true;
    private boolean notesByDefault;
    private boolean commentsByDefault;
    private boolean masterByDefault;

    
    public SlideShowExtractor(final SlideShow<S,P> slideshow) {
        setFilesystem(slideshow);
        this.slideshow = slideshow;
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
        
        if (masterByDefault) {
            for (final MasterSheet<S,P> master : slideshow.getSlideMasters()) {
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
                        sb.append(text);
                        if (!text.endsWith("\n")) {
                            sb.append("\n");
                        }

                    }
                }
            }
        }

        for (final Slide<S, P> slide : slideshow.getSlides()) {
            sb.append(getText(slide));
        }

        return sb.toString();
    }

    public String getText(final Slide<S,P> slide) {
        final StringBuilder sb = new StringBuilder();

        if (slidesByDefault) {
            printShapeText(slide, sb);
        }

        if (commentsByDefault) {
            printComments(slide, sb);
        }

        if (notesByDefault) {
            printNotes(slide, sb);
        }

        return sb.toString();
    }

    private String printHeaderReturnFooter(final Sheet<S,P> sheet, final StringBuilder sb) {
        final Sheet<S, P> m = (sheet instanceof Slide) ? sheet.getMasterSheet() : sheet;
        final StringBuilder footer = new StringBuilder("\n");
        addSheetPlaceholderDatails(sheet, Placeholder.HEADER, sb);
        addSheetPlaceholderDatails(sheet, Placeholder.FOOTER, footer);

        if (masterByDefault) {
            // write header texts and determine footer text
            for (Shape<S, P> s : m) {
                if (!(s instanceof TextShape)) {
                    continue;
                }
                final TextShape<S, P> ts = (TextShape<S, P>) s;
                final PlaceholderDetails pd = ts.getPlaceholderDetails();
                if (pd == null || !pd.isVisible()) {
                    continue;
                }
                switch (pd.getPlaceholder()) {
                    case HEADER:
                        sb.append(ts.getText());
                        sb.append('\n');
                        break;
                    case SLIDE_NUMBER:
                        if (sheet instanceof Slide) {
                            footer.append(ts.getText().replace("‹#›", Integer.toString(((Slide<S, P>) sheet).getSlideNumber() + 1)));
                            footer.append('\n');
                        }
                        break;
                    case FOOTER:
                        footer.append(ts.getText());
                        footer.append('\n');
                        break;
                    case DATETIME:
                        // currently not supported
                    default:
                        break;
                }
            }
        }

        return (footer.length() > 1) ? footer.toString() : "";
    }

    private void addSheetPlaceholderDatails(final Sheet<S,P> sheet, final Placeholder placeholder, final StringBuilder sb) {
        final PlaceholderDetails headerPD = sheet.getPlaceholderDetails(placeholder);
        if (headerPD == null) {
            return;
        }
        final String headerStr = headerPD.getText();
        if (headerStr == null) {
            return;
        }
        sb.append(headerStr);
    }

    private void printShapeText(final Sheet<S,P> sheet, final StringBuilder sb) {
        final String footer = printHeaderReturnFooter(sheet, sb);
        printShapeText((ShapeContainer<S,P>)sheet, sb);
        sb.append(footer);
    }

    @SuppressWarnings("unchecked")
    private void printShapeText(final ShapeContainer<S,P> container, final StringBuilder sb) {
        for (Shape<S,P> shape : container) {
            if (shape instanceof TextShape) {
                printShapeText((TextShape<S,P>)shape, sb);
            } else if (shape instanceof TableShape) {
                printShapeText((TableShape<S,P>)shape, sb);
            } else if (shape instanceof ShapeContainer) {
                printShapeText((ShapeContainer<S,P>)shape, sb);
            }
        }
    }

    private void printShapeText(final TextShape<S,P> shape, final StringBuilder sb) {
        final List<P> paraList = shape.getTextParagraphs();
        if (paraList.isEmpty()) {
            sb.append('\n');
            return;
        }
        for (final P para : paraList) {
            final int oldLen = sb.length();
            for (final TextRun tr : para) {
                final String str = tr.getRawText().replace("\r", "");
                final String newStr;
                switch (tr.getTextCap()) {
                    case ALL:
                        newStr = str.toUpperCase(LocaleUtil.getUserLocale());
                        break;
                    case SMALL:
                        newStr = str.toLowerCase(LocaleUtil.getUserLocale());
                        break;
                    default:
                    case NONE:
                        newStr = str;
                        break;
                }
                sb.append(newStr);
            }
            sb.append('\n');
        }
    }

    @SuppressWarnings("Duplicates")
    private void printShapeText(final TableShape<S,P> shape, final StringBuilder sb) {
        final int nrows = shape.getNumberOfRows();
        final int ncols = shape.getNumberOfColumns();
        for (int row = 0; row < nrows; row++){
            for (int col = 0; col < ncols; col++){
                TableCell<S, P> cell = shape.getCell(row, col);
                //defensive null checks; don't know if they're necessary
                if (cell != null){
                    String txt = cell.getText();
                    txt = (txt == null) ? "" : txt;
                    sb.append(txt);
                    if (col < ncols-1){
                        sb.append('\t');
                    }
                }
            }
            sb.append('\n');
        }
    }

    private void printComments(final Slide<S,P> slide, final StringBuilder sb) {
        for (final Comment comment : slide.getComments()) {
            sb.append(comment.getAuthor());
            sb.append(" - ");
            sb.append(comment.getText());
            sb.append("\n");
        }
    }

    private void printNotes(final Slide<S,P> slide, final StringBuilder sb) {
        final Notes<S, P> notes = slide.getNotes();
        if (notes == null) {
            return;
        }

        final String footer = printHeaderReturnFooter(notes, sb);

        printShapeText(notes, sb);

        sb.append(footer);
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
}
