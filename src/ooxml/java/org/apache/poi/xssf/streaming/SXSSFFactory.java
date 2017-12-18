package org.apache.poi.xssf.streaming;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLRelation;
import org.apache.poi.xssf.usermodel.XSSFFactory;
import org.apache.poi.xssf.usermodel.XSSFRelation;

class SXSSFFactory extends XSSFFactory {

    private static final SXSSFFactory inst = new SXSSFFactory();

    public static XSSFFactory getInstance(){
        return inst;
    }

    private SXSSFFactory() {
        super();
    }

    @Override
    public POIXMLDocumentPart newDocumentPart(POIXMLRelation descriptor) {
        if (XSSFRelation.SHARED_STRINGS.getRelation().equals(descriptor.getRelation())) {
            return new TempFileSharedStringsTable();
        }
        return super.newDocumentPart(descriptor);
    }
}
