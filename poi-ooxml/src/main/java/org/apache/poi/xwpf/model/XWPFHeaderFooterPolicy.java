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
package org.apache.poi.xwpf.model;

import com.microsoft.schemas.office.office.CTLock;
import com.microsoft.schemas.office.office.STConnectType;
import com.microsoft.schemas.vml.CTFormulas;
import com.microsoft.schemas.vml.CTGroup;
import com.microsoft.schemas.vml.CTH;
import com.microsoft.schemas.vml.CTHandles;
import com.microsoft.schemas.vml.CTPath;
import com.microsoft.schemas.vml.CTShape;
import com.microsoft.schemas.vml.CTShapetype;
import com.microsoft.schemas.vml.CTTextPath;
import com.microsoft.schemas.vml.STExt;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLDocumentPart.RelationPart;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFactory;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFHeaderFooter;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.xmlbeans.impl.values.XmlValueOutOfRangeException;
import org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STTrueFalse;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHdrFtr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHdrFtrRef;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPicture;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.FtrDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.HdrDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHdrFtr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHdrFtr.Enum;

/**
 * A .docx file can have no headers/footers, the same header/footer
 * on each page, odd/even page footers, and optionally also
 * a different header/footer on the first page.
 * This class handles sorting out what there is, and giving you
 * the right headers and footers for the document.
 */
public class XWPFHeaderFooterPolicy {
    public static final Enum DEFAULT = STHdrFtr.DEFAULT;
    public static final Enum EVEN = STHdrFtr.EVEN;
    public static final Enum FIRST = STHdrFtr.FIRST;

    private XWPFDocument doc;

    private XWPFHeader firstPageHeader;
    private XWPFFooter firstPageFooter;

    private XWPFHeader evenPageHeader;
    private XWPFFooter evenPageFooter;

    private XWPFHeader defaultHeader;
    private XWPFFooter defaultFooter;
    //This variable allows to have a reference for each section, and having different headers in each of them.
    private CTSectPr sectPr;

    /**
     * Figures out the policy for the given document,
     * and creates any header and footer objects
     * as required.
     */
    public XWPFHeaderFooterPolicy(XWPFDocument doc) {
        this(doc, null);
    }

    /**
     * Figures out the policy for the given document,
     * and creates any header and footer objects
     * as required.
     */
    public XWPFHeaderFooterPolicy(XWPFDocument doc, CTSectPr sectPr) {
        // Grab what headers and footers have been defined
        // For now, we don't care about different ranges, as it
        //  doesn't seem that .docx properly supports that
        //  feature of the file format yet
        if (sectPr == null) {
            CTBody ctBody = doc.getDocument().getBody();
            sectPr = ctBody.isSetSectPr()
                    ? ctBody.getSectPr()
                    : ctBody.addNewSectPr();
        }
        this.doc = doc;
        this.sectPr = sectPr;

        for (int i = 0; i < sectPr.sizeOfHeaderReferenceArray(); i++) {
            // Get the header
            CTHdrFtrRef ref = sectPr.getHeaderReferenceArray(i);
            POIXMLDocumentPart relatedPart = doc.getRelationById(ref.getId());
            XWPFHeader hdr = null;
            if (relatedPart instanceof XWPFHeader) {
                hdr = (XWPFHeader) relatedPart;
            }
            // Assign it; treat invalid options as "default" POI-60293
            Enum type;
            try {
                type = ref.getType();
            } catch (XmlValueOutOfRangeException e) {
                type = STHdrFtr.DEFAULT;
            }

            assignHeader(hdr, type);
        }
        for (int i = 0; i < sectPr.sizeOfFooterReferenceArray(); i++) {
            // Get the footer
            CTHdrFtrRef ref = sectPr.getFooterReferenceArray(i);
            POIXMLDocumentPart relatedPart = doc.getRelationById(ref.getId());
            XWPFFooter ftr = null;
            if (relatedPart instanceof XWPFFooter) {
                ftr = (XWPFFooter) relatedPart;
            }
            // Assign it; treat invalid options as "default" POI-60293
            Enum type;
            try {
                type = ref.getType();
            } catch (XmlValueOutOfRangeException e) {
                type = STHdrFtr.DEFAULT;
            }
            assignFooter(ftr, type);
        }
    }

    private void assignFooter(XWPFFooter ftr, Enum type) {
        if (type == STHdrFtr.FIRST) {
            firstPageFooter = ftr;
        } else if (type == STHdrFtr.EVEN) {
            evenPageFooter = ftr;
        } else {
            defaultFooter = ftr;
        }
    }

    private void assignHeader(XWPFHeader hdr, Enum type) {
        if (type == STHdrFtr.FIRST) {
            firstPageHeader = hdr;
        } else if (type == STHdrFtr.EVEN) {
            evenPageHeader = hdr;
        } else {
            defaultHeader = hdr;
        }
    }

    /**
     * Creates an empty header of the specified type, containing a single
     * empty paragraph, to which you can then set text, add more paragraphs etc.
     */
    public XWPFHeader createHeader(Enum type) {
        return createHeader(type, null);
    }

    /**
     * Creates a new header of the specified type, to which the
     * supplied (and previously unattached!) paragraphs are
     * added to.
     */
    public XWPFHeader createHeader(Enum type, XWPFParagraph[] pars) {
        XWPFHeader header = getHeader(type);

        if (header == null) {
            HdrDocument hdrDoc = HdrDocument.Factory.newInstance();

            XWPFRelation relation = XWPFRelation.HEADER;
            int i = getRelationIndex(relation);

            XWPFHeader wrapper = (XWPFHeader) doc.createRelationship(relation,
                    XWPFFactory.getInstance(), i);
            wrapper.setXWPFDocument(doc);

            CTHdrFtr hdr = buildHdr(type, wrapper, pars);
            wrapper.setHeaderFooter(hdr);
            hdrDoc.setHdr(hdr);
            assignHeader(wrapper, type);
            header = wrapper;
        }

        return header;
    }

    /**
     * Creates an empty footer of the specified type, containing a single
     * empty paragraph, to which you can then set text, add more paragraphs etc.
     */
    public XWPFFooter createFooter(Enum type) {
        return createFooter(type, null);
    }

    /**
     * Creates a new footer of the specified type, to which the
     * supplied (and previously unattached!) paragraphs are
     * added to.
     */
    public XWPFFooter createFooter(Enum type, XWPFParagraph[] pars) {
        XWPFFooter footer = getFooter(type);

        if (footer == null) {
            FtrDocument ftrDoc = FtrDocument.Factory.newInstance();

            XWPFRelation relation = XWPFRelation.FOOTER;
            int i = getRelationIndex(relation);

            XWPFFooter wrapper = (XWPFFooter) doc.createRelationship(relation,
                    XWPFFactory.getInstance(), i);
            wrapper.setXWPFDocument(doc);

            CTHdrFtr ftr = buildFtr(type, wrapper, pars);
            wrapper.setHeaderFooter(ftr);
            ftrDoc.setFtr(ftr);
            assignFooter(wrapper, type);
            footer = wrapper;
        }

        return footer;
    }

    private int getRelationIndex(XWPFRelation relation) {
        int i = 1;
        for (RelationPart rp : doc.getRelationParts()) {
            if (rp.getRelationship().getRelationshipType().equals(relation.getRelation())) {
                i++;
            }
        }
        return i;
    }

    private CTHdrFtr buildFtr(Enum type, XWPFHeaderFooter wrapper, XWPFParagraph[] pars) {
        //CTHdrFtr ftr = buildHdrFtr(pStyle, pars);             // MB 24 May 2010
        CTHdrFtr ftr = buildHdrFtr(pars, wrapper);        // MB 24 May 2010
        setFooterReference(type, wrapper);
        return ftr;
    }

    private CTHdrFtr buildHdr(Enum type, XWPFHeaderFooter wrapper, XWPFParagraph[] pars) {
        //CTHdrFtr hdr = buildHdrFtr(pStyle, pars);             // MB 24 May 2010
        CTHdrFtr hdr = buildHdrFtr(pars, wrapper);        // MB 24 May 2010
        setHeaderReference(type, wrapper);
        return hdr;
    }

    /**
     * MB 24 May 2010. Created this overloaded buildHdrFtr() method because testing demonstrated
     * that the XWPFFooter or XWPFHeader object returned by calls to the createHeader(int, XWPFParagraph[])
     * and createFooter(int, XWPFParagraph[]) methods or the getXXXXXHeader/Footer methods where
     * headers or footers had been added to a document since it had been created/opened, returned
     * an object that contained no XWPFParagraph objects even if the header/footer itself did contain
     * text. The reason was that this line of code; CTHdrFtr ftr = CTHdrFtr.Factory.newInstance();
     * created a brand new instance of the CTHDRFtr class which was then populated with data when
     * it should have recovered the CTHdrFtr object encapsulated within the XWPFHeaderFooter object
     * that had previoulsy been instantiated in the createHeader(int, XWPFParagraph[]) or
     * createFooter(int, XWPFParagraph[]) methods.
     */
    private CTHdrFtr buildHdrFtr(XWPFParagraph[] paragraphs, XWPFHeaderFooter wrapper) {
        CTHdrFtr ftr = wrapper._getHdrFtr();
        if (paragraphs != null) {
            for (int i = 0; i < paragraphs.length; i++) {
                /*CTP p =*/ ftr.addNewP();
                ftr.setPArray(i, paragraphs[i].getCTP());
            }
//        } else {
//            CTP p = ftr.addNewP();
//            CTBody body = doc.getDocument().getBody();
//            if (body.sizeOfPArray() > 0) {
//                CTP p0 = body.getPArray(0);
//                if (p0.isSetRsidR()) {
//                    byte[] rsidr = p0.getRsidR();
//                    byte[] rsidrdefault = p0.getRsidRDefault();
//                    p.setRsidP(rsidr);
//                    p.setRsidRDefault(rsidrdefault);
//                }
//            }
//            CTPPr pPr = p.addNewPPr();
//            pPr.addNewPStyle().setVal(pStyle);
        }
        return ftr;
    }


    private void setFooterReference(Enum type, XWPFHeaderFooter wrapper) {
        CTHdrFtrRef ref = this.sectPr.addNewFooterReference();
        ref.setType(type);
        ref.setId(doc.getRelationId(wrapper));
    }


    private void setHeaderReference(Enum type, XWPFHeaderFooter wrapper) {
        CTHdrFtrRef ref = this.sectPr.addNewHeaderReference();
        ref.setType(type);
        ref.setId(doc.getRelationId(wrapper));
    }

    public XWPFHeader getFirstPageHeader() {
        return firstPageHeader;
    }

    public XWPFFooter getFirstPageFooter() {
        return firstPageFooter;
    }

    /**
     * Returns the odd page header. This is
     * also the same as the default one...
     */
    public XWPFHeader getOddPageHeader() {
        return defaultHeader;
    }

    /**
     * Returns the odd page footer. This is
     * also the same as the default one...
     */
    public XWPFFooter getOddPageFooter() {
        return defaultFooter;
    }

    public XWPFHeader getEvenPageHeader() {
        return evenPageHeader;
    }

    public XWPFFooter getEvenPageFooter() {
        return evenPageFooter;
    }

    public XWPFHeader getDefaultHeader() {
        return defaultHeader;
    }

    public XWPFFooter getDefaultFooter() {
        return defaultFooter;
    }

    /**
     * Get the header that applies to the given
     * (1 based) page.
     *
     * @param pageNumber The one based page number
     */
    public XWPFHeader getHeader(int pageNumber) {
        if (pageNumber == 1 && firstPageHeader != null) {
            return firstPageHeader;
        }
        if (pageNumber % 2 == 0 && evenPageHeader != null) {
            return evenPageHeader;
        }
        return defaultHeader;
    }

    /**
     * Get this section header for the given type
     *
     * @param type of header to return
     * @return {@link XWPFHeader} object
     */
    public XWPFHeader getHeader(Enum type) {
        if (type == STHdrFtr.EVEN) {
            return evenPageHeader;
        } else if (type == STHdrFtr.FIRST) {
            return firstPageHeader;
        }
        return defaultHeader;
    }

    /**
     * Get the footer that applies to the given
     * (1 based) page.
     *
     * @param pageNumber The one based page number
     */
    public XWPFFooter getFooter(int pageNumber) {
        if (pageNumber == 1 && firstPageFooter != null) {
            return firstPageFooter;
        }
        if (pageNumber % 2 == 0 && evenPageFooter != null) {
            return evenPageFooter;
        }
        return defaultFooter;
    }

    /**
     * Get this section footer for the given type
     *
     * @param type of footer to return
     * @return {@link XWPFFooter} object
     */
    public XWPFFooter getFooter(Enum type) {
        if (type == STHdrFtr.EVEN) {
            return evenPageFooter;
        } else if (type == STHdrFtr.FIRST) {
            return firstPageFooter;
        }
        return defaultFooter;
    }


    public void createWatermark(String text) {
        XWPFParagraph[] pars = new XWPFParagraph[1];
        pars[0] = getWatermarkParagraph(text, 1);
        createHeader(DEFAULT, pars);
        pars[0] = getWatermarkParagraph(text, 2);
        createHeader(FIRST, pars);
        pars[0] = getWatermarkParagraph(text, 3);
        createHeader(EVEN, pars);
    }

    /*
     * This is the default Watermark paragraph; the only variable is the text message
     * TODO: manage all the other variables
     */
    private XWPFParagraph getWatermarkParagraph(String text, int idx) {
        CTP p = CTP.Factory.newInstance();
        CTBody ctBody = doc.getDocument().getBody();
        byte[] rsidr = null;
        byte[] rsidrdefault = null;
        if (ctBody.sizeOfPArray() == 0) {
            // TODO generate rsidr and rsidrdefault
        } else {
            CTP ctp = ctBody.getPArray(0);
            rsidr = ctp.getRsidR();
            rsidrdefault = ctp.getRsidRDefault();
        }
        p.setRsidP(rsidr);
        p.setRsidRDefault(rsidrdefault);
        CTPPr pPr = p.addNewPPr();
        pPr.addNewPStyle().setVal("Header");
        // start watermark paragraph
        CTR r = p.addNewR();
        CTRPr rPr = r.addNewRPr();
        rPr.addNewNoProof();
        CTPicture pict = r.addNewPict();
        CTGroup group = CTGroup.Factory.newInstance();
        CTShapetype shapetype = group.addNewShapetype();
        shapetype.setId("_x0000_t136");
        shapetype.setCoordsize("1600,21600");
        shapetype.setSpt(136);
        shapetype.setAdj("10800");
        shapetype.setPath2("m@7,0l@8,0m@5,21600l@6,21600e");
        CTFormulas formulas = shapetype.addNewFormulas();
        formulas.addNewF().setEqn("sum #0 0 10800");
        formulas.addNewF().setEqn("prod #0 2 1");
        formulas.addNewF().setEqn("sum 21600 0 @1");
        formulas.addNewF().setEqn("sum 0 0 @2");
        formulas.addNewF().setEqn("sum 21600 0 @3");
        formulas.addNewF().setEqn("if @0 @3 0");
        formulas.addNewF().setEqn("if @0 21600 @1");
        formulas.addNewF().setEqn("if @0 0 @2");
        formulas.addNewF().setEqn("if @0 @4 21600");
        formulas.addNewF().setEqn("mid @5 @6");
        formulas.addNewF().setEqn("mid @8 @5");
        formulas.addNewF().setEqn("mid @7 @8");
        formulas.addNewF().setEqn("mid @6 @7");
        formulas.addNewF().setEqn("sum @6 0 @5");
        CTPath path = shapetype.addNewPath();
        path.setTextpathok(STTrueFalse.T);
        path.setConnecttype(STConnectType.CUSTOM);
        path.setConnectlocs("@9,0;@10,10800;@11,21600;@12,10800");
        path.setConnectangles("270,180,90,0");
        CTTextPath shapeTypeTextPath = shapetype.addNewTextpath();
        shapeTypeTextPath.setOn(STTrueFalse.T);
        shapeTypeTextPath.setFitshape(STTrueFalse.T);
        CTHandles handles = shapetype.addNewHandles();
        CTH h = handles.addNewH();
        h.setPosition("#0,bottomRight");
        h.setXrange("6629,14971");
        CTLock lock = shapetype.addNewLock();
        lock.setExt(STExt.EDIT);
        CTShape shape = group.addNewShape();
        shape.setId("PowerPlusWaterMarkObject" + idx);
        shape.setSpid("_x0000_s102" + (4 + idx));
        shape.setType("#_x0000_t136");
        shape.setStyle("position:absolute;margin-left:0;margin-top:0;width:415pt;height:207.5pt;z-index:-251654144;mso-wrap-edited:f;mso-position-horizontal:center;mso-position-horizontal-relative:margin;mso-position-vertical:center;mso-position-vertical-relative:margin");
        shape.setWrapcoords("616 5068 390 16297 39 16921 -39 17155 7265 17545 7186 17467 -39 17467 18904 17467 10507 17467 8710 17545 18904 17077 18787 16843 18358 16297 18279 12554 19178 12476 20701 11774 20779 11228 21131 10059 21248 8811 21248 7563 20975 6316 20935 5380 19490 5146 14022 5068 2616 5068");
        shape.setFillcolor("black");
        shape.setStroked(STTrueFalse.FALSE);
        CTTextPath shapeTextPath = shape.addNewTextpath();
        shapeTextPath.setStyle("font-family:&quot;Cambria&quot;;font-size:1pt");
        shapeTextPath.setString(text);
        pict.set(group);
        // end watermark paragraph
        return new XWPFParagraph(p, doc);
    }
}