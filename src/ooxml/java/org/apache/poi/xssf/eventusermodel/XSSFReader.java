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
package org.apache.poi.xssf.eventusermodel;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.model.CommentsTable;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.model.ThemesTable;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.xmlbeans.XmlException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class makes it easy to get at individual parts
 *  of an OOXML .xlsx file, suitable for low memory sax
 *  parsing or similar.
 * It makes up the core part of the EventUserModel support
 *  for XSSF.
 */
public class XSSFReader {

    private static final Set<String> WORKSHEET_RELS =
            Collections.unmodifiableSet(new HashSet<String>(
                    Arrays.asList(new String[]{
                            XSSFRelation.WORKSHEET.getRelation(),
                            XSSFRelation.CHARTSHEET.getRelation(),
                    })
            ));
    private static final POILogger LOGGER = POILogFactory.getLogger(XSSFReader.class);

    protected OPCPackage pkg;
    protected PackagePart workbookPart;

    /**
     * Creates a new XSSFReader, for the given package
     */
    public XSSFReader(OPCPackage pkg) throws IOException, OpenXML4JException {
        this.pkg = pkg;

        PackageRelationship coreDocRelationship = this.pkg.getRelationshipsByType(
                PackageRelationshipTypes.CORE_DOCUMENT).getRelationship(0);

        // strict OOXML likely not fully supported, see #57699
        // this code is similar to POIXMLDocumentPart.getPartFromOPCPackage(), but I could not combine it
        // easily due to different return values
        if(coreDocRelationship == null) {
            if (this.pkg.getRelationshipsByType(
                    PackageRelationshipTypes.STRICT_CORE_DOCUMENT).getRelationship(0) != null) {
                throw new POIXMLException("Strict OOXML isn't currently supported, please see bug #57699");
            }

            throw new POIXMLException("OOXML file structure broken/invalid - no core document found!");
        }

        // Get the part that holds the workbook
        workbookPart = this.pkg.getPart(coreDocRelationship);
    }


    /**
     * Opens up the Shared Strings Table, parses it, and
     *  returns a handy object for working with
     *  shared strings.
     */
    public SharedStringsTable getSharedStringsTable() throws IOException, InvalidFormatException {
        ArrayList<PackagePart> parts = pkg.getPartsByContentType( XSSFRelation.SHARED_STRINGS.getContentType());
        return parts.size() == 0 ? null : new SharedStringsTable(parts.get(0));
    }

    /**
     * Opens up the Styles Table, parses it, and
     *  returns a handy object for working with cell styles
     */
    public StylesTable getStylesTable() throws IOException, InvalidFormatException {
        ArrayList<PackagePart> parts = pkg.getPartsByContentType( XSSFRelation.STYLES.getContentType());
        if(parts.size() == 0) return null;
        
        // Create the Styles Table, and associate the Themes if present
        StylesTable styles = new StylesTable(parts.get(0));
        parts = pkg.getPartsByContentType( XSSFRelation.THEME.getContentType());
        if(parts.size() != 0) {
           styles.setTheme(new ThemesTable(parts.get(0)));
        }
        return styles;
    }


    /**
     * Returns an InputStream to read the contents of the
     *  shared strings table.
     */
    public InputStream getSharedStringsData() throws IOException, InvalidFormatException {
        return XSSFRelation.SHARED_STRINGS.getContents(workbookPart);
    }

    /**
     * Returns an InputStream to read the contents of the
     *  styles table.
     */
    public InputStream getStylesData() throws IOException, InvalidFormatException {
        return XSSFRelation.STYLES.getContents(workbookPart);
    }

    /**
     * Returns an InputStream to read the contents of the
     *  themes table.
     */
    public InputStream getThemesData() throws IOException, InvalidFormatException {
        return XSSFRelation.THEME.getContents(workbookPart);
    }

    /**
     * Returns an InputStream to read the contents of the
     *  main Workbook, which contains key overall data for
     *  the file, including sheet definitions.
     */
    public InputStream getWorkbookData() throws IOException, InvalidFormatException {
        return workbookPart.getInputStream();
    }

    /**
     * Returns an InputStream to read the contents of the
     *  specified Sheet.
     * @param relId The relationId of the sheet, from a r:id on the workbook
     */
    public InputStream getSheet(String relId) throws IOException, InvalidFormatException {
        PackageRelationship rel = workbookPart.getRelationship(relId);
        if(rel == null) {
            throw new IllegalArgumentException("No Sheet found with r:id " + relId);
        }

        PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
        PackagePart sheet = pkg.getPart(relName);
        if(sheet == null) {
            throw new IllegalArgumentException("No data found for Sheet with r:id " + relId);
        }
        return sheet.getInputStream();
    }

    /**
     * Returns an Iterator which will let you get at all the
     *  different Sheets in turn.
     * Each sheet's InputStream is only opened when fetched
     *  from the Iterator. It's up to you to close the
     *  InputStreams when done with each one.
     */
    public Iterator<InputStream> getSheetsData() throws IOException, InvalidFormatException {
        return new SheetIterator(workbookPart);
    }

    /**
     * Iterator over sheet data.
     */
    public static class SheetIterator implements Iterator<InputStream> {

        /**
         *  Maps relId and the corresponding PackagePart
         */
        private final Map<String, PackagePart> sheetMap;

        /**
         * Current sheet reference
         */
        XSSFSheetRef xssfSheetRef;

        /**
         * Iterator over CTSheet objects, returns sheets in <tt>logical</tt> order.
         * We can't rely on the Ooxml4J's relationship iterator because it returns objects in physical order,
         * i.e. as they are stored in the underlying package
         */
        final Iterator<XSSFSheetRef> sheetIterator;

        /**
         * Construct a new SheetIterator
         *
         * @param wb package part holding workbook.xml
         */
        SheetIterator(PackagePart wb) throws IOException {

            /*
             * The order of sheets is defined by the order of CTSheet elements in workbook.xml
             */
            try {
                //step 1. Map sheet's relationship Id and the corresponding PackagePart
                sheetMap = new HashMap<String, PackagePart>();
                OPCPackage pkg = wb.getPackage();
                Set<String> worksheetRels = getSheetRelationships();
                for(PackageRelationship rel : wb.getRelationships()){
                    String relType = rel.getRelationshipType();
                    if (worksheetRels.contains(relType)) {
                        PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
                        sheetMap.put(rel.getId(), pkg.getPart(relName));
                    }
                }
                //step 2. Read array of CTSheet elements, wrap it in a LinkedList
                //and construct an iterator
                sheetIterator = createSheetIteratorFromWB(wb);
            } catch (InvalidFormatException e){
                throw new POIXMLException(e);
            }
        }

        Iterator<XSSFSheetRef> createSheetIteratorFromWB(PackagePart wb) throws IOException {

            XMLSheetRefReader xmlSheetRefReader = new XMLSheetRefReader();
            XMLReader xmlReader;
            try {
                xmlReader = SAXHelper.newXMLReader();
            } catch (ParserConfigurationException e) {
                throw new POIXMLException(e);
            } catch (SAXException e) {
                throw new POIXMLException(e);
            }
            xmlReader.setContentHandler(xmlSheetRefReader);
            try {
                xmlReader.parse(new InputSource(wb.getInputStream()));
            } catch (SAXException e) {
                throw new POIXMLException(e);
            }

            List<XSSFSheetRef> validSheets = new ArrayList<XSSFSheetRef>();
            for (XSSFSheetRef xssfSheetRef : xmlSheetRefReader.getSheetRefs()) {
                //if there's no relationship id, silently skip the sheet
                String sheetId = xssfSheetRef.getId();
                if (sheetId != null && sheetId.length() > 0) {
                    validSheets.add(xssfSheetRef);
                }
            }
            return validSheets.iterator();
        }

        /**
         * Gets string representations of relationships
         * that are sheet-like.  Added to allow subclassing
         * by XSSFBReader.  This is used to decide what
         * relationships to load into the sheetRefs
         *
         * @return all relationships that are sheet-like
         */
        Set<String> getSheetRelationships() {
            return WORKSHEET_RELS;
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements.
         *
         * @return <tt>true</tt> if the iterator has more elements.
         */
        @Override
        public boolean hasNext() {
            return sheetIterator.hasNext();
        }

        /**
         * Returns input stream of the next sheet in the iteration
         *
         * @return input stream of the next sheet in the iteration
         */
        @Override
        public InputStream next() {
            xssfSheetRef = sheetIterator.next();

            String sheetId = xssfSheetRef.getId();
            try {
                PackagePart sheetPkg = sheetMap.get(sheetId);
                return sheetPkg.getInputStream();
            } catch(IOException e) {
                throw new POIXMLException(e);
            }
        }

        /**
         * Returns name of the current sheet
         *
         * @return name of the current sheet
         */
        public String getSheetName() {
            return xssfSheetRef.getName();
        }
        
        /**
         * Returns the comments associated with this sheet,
         *  or null if there aren't any
         */
        public CommentsTable getSheetComments() {
           PackagePart sheetPkg = getSheetPart();
           
           // Do we have a comments relationship? (Only ever one if so)
           try {
              PackageRelationshipCollection commentsList = 
                   sheetPkg.getRelationshipsByType(XSSFRelation.SHEET_COMMENTS.getRelation());
              if(commentsList.size() > 0) {
                 PackageRelationship comments = commentsList.getRelationship(0);
                 PackagePartName commentsName = PackagingURIHelper.createPartName(comments.getTargetURI());
                 PackagePart commentsPart = sheetPkg.getPackage().getPart(commentsName);
                 return new CommentsTable(commentsPart);
              }
           } catch (InvalidFormatException e) {
              return null;
           } catch (IOException e) {
              return null;
           }
           return null;
        }
        
        /**
         * Returns the shapes associated with this sheet,
         * an empty list or null if there is an exception
         */
        public List<XSSFShape> getShapes() {
            PackagePart sheetPkg = getSheetPart();
            List<XSSFShape> shapes= new LinkedList<XSSFShape>();
           // Do we have a comments relationship? (Only ever one if so)
           try {
              PackageRelationshipCollection drawingsList = sheetPkg.getRelationshipsByType(XSSFRelation.DRAWINGS.getRelation());
              for (int i = 0; i < drawingsList.size(); i++){
                  PackageRelationship drawings = drawingsList.getRelationship(i);
                  PackagePartName drawingsName = PackagingURIHelper.createPartName(drawings.getTargetURI());
                  PackagePart drawingsPart = sheetPkg.getPackage().getPart(drawingsName);
                  if (drawingsPart == null) {
                      //parts can go missing; Excel ignores them silently -- TIKA-2134
                      LOGGER.log(POILogger.WARN, "Missing drawing: "+drawingsName +". Skipping it.");
                      continue;
                  }
                  XSSFDrawing drawing = new XSSFDrawing(drawingsPart);
                  shapes.addAll(drawing.getShapes());
              }
           } catch (XmlException e){
               return null;
           } catch (InvalidFormatException e) {  
              return null;
           } catch (IOException e) {
              return null;
           }
           return shapes;
        }
        
        public PackagePart getSheetPart() {
           String sheetId = xssfSheetRef.getId();
           return sheetMap.get(sheetId);
        }

        /**
         * We're read only, so remove isn't supported
         */
        @Override
        public void remove() {
            throw new IllegalStateException("Not supported");
        }
    }

    protected static final class XSSFSheetRef {
        //do we need to store sheetId, too?
        private final String id;
        private final String name;

        public XSSFSheetRef(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    //scrapes sheet reference info and order from workbook.xml
    private static class XMLSheetRefReader extends DefaultHandler {
        private static final String SHEET = "sheet";
        private static final String ID = "id";
        private static final String NAME = "name";

        private final List<XSSFSheetRef> sheetRefs = new LinkedList<XSSFSheetRef>();

        // read <sheet name="Sheet6" sheetId="4" r:id="rId6"/>
        // and add XSSFSheetRef(id="rId6", name="Sheet6") to sheetRefs
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
            if (localName.equalsIgnoreCase(SHEET)) {
                String name = null;
                String id = null;
                for (int i = 0; i < attrs.getLength(); i++) {
                    final String attrName = attrs.getLocalName(i);
                    if (attrName.equalsIgnoreCase(NAME)) {
                        name = attrs.getValue(i);
                    } else if (attrName.equalsIgnoreCase(ID)) {
                        id = attrs.getValue(i);
                    }
                    if (name != null && id != null) {
                        sheetRefs.add(new XSSFSheetRef(id, name));
                        break;
                    }
                }
            }
        }

        List<XSSFSheetRef> getSheetRefs() {
            return Collections.unmodifiableList(sheetRefs);
        }
    }
}
