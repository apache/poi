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

package org.apache.poi.xssf.extractor;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFMap;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFTableColumn;
import org.apache.poi.xssf.usermodel.helpers.XSSFSingleXmlCell;
import org.apache.poi.xssf.usermodel.helpers.XSSFXmlColumnPr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * Maps an XLSX to an XML according to one of the mapping defined.
 *
 *
 * The output XML Schema must respect this limitations:
 *
 * <ul>
 * <li> all mandatory elements and attributes must be mapped (enable validation to check this)</li>
 *
 * <li> no &lt;any&gt; in complex type/element declaration </li>
 * <li> no &lt;anyAttribute&gt; attributes declaration </li>
 * <li> no recursive structures: recursive structures can't be nested more than one level </li>
 * <li> no abstract elements: abstract complex types can be declared but must not be used in elements. </li>
 * <li> no mixed content: an element can't contain simple text and child element(s) together </li>
 * <li> no &lt;substitutionGroup&gt; in complex type/element declaration </li>
 * </ul>
 */
public class XSSFExportToXml implements Comparator<String>{
    private static final POILogger LOG = POILogFactory.getLogger(XSSFExportToXml.class);


    @FunctionalInterface
    private interface SecurityFeature {
        void accept(String name) throws SAXException;
    }


    private XSSFMap map;
    private final HashMap<String, Integer> indexMap = new HashMap<>();
    /**
     * Creates a new exporter and sets the mapping to be used when generating the XML output document
     *
     * @param map the mapping rule to be used
     */
    public XSSFExportToXml(XSSFMap map) {
        this.map = map;
    }

    /**
     *
     * Exports the data in an XML stream
     *
     * @param os OutputStream in which will contain the output XML
     * @param validate if true, validates the XML against the XML Schema
     * @throws SAXException If validating the document fails
     * @throws TransformerException If transforming the document fails
     */
    public void exportToXML(OutputStream os, boolean validate) throws SAXException, TransformerException {
        exportToXML(os, "UTF-8", validate);
    }

    /**
     * Exports the data in an XML stream
     *
     * @param os OutputStream in which will contain the output XML
     * @param encoding the output charset encoding
     * @param validate if true, validates the XML against the XML Schema
     * @throws SAXException If validating the document fails
     * @throws TransformerException If transforming the document fails
     */
    public void exportToXML(OutputStream os, String encoding, boolean validate) throws SAXException, TransformerException{
        List<XSSFSingleXmlCell> singleXMLCells = map.getRelatedSingleXMLCell();
        List<XSSFTable> tables = map.getRelatedTables();

        String rootElement = map.getCtMap().getRootElement();

        Document doc = DocumentHelper.createDocument();

        final Element root;

        if (isNamespaceDeclared()) {
            root = doc.createElementNS(getNamespace(),rootElement);
        } else {
            root = doc.createElementNS("", rootElement);
        }
        doc.appendChild(root);


        List<String> xpaths = new Vector<>();
        Map<String,XSSFSingleXmlCell> singleXmlCellsMappings = new HashMap<>();
        Map<String,XSSFTable> tableMappings = new HashMap<>();

        for(XSSFSingleXmlCell simpleXmlCell : singleXMLCells) {
            xpaths.add(simpleXmlCell.getXpath());
            singleXmlCellsMappings.put(simpleXmlCell.getXpath(), simpleXmlCell);
        }
        for(XSSFTable table : tables) {
            String commonXPath = table.getCommonXpath();
            xpaths.add(commonXPath);
            tableMappings.put(commonXPath, table);
        }

        indexMap.clear();
        xpaths.sort(this);
        indexMap.clear();
        
        for(String xpath : xpaths) {

            XSSFSingleXmlCell simpleXmlCell = singleXmlCellsMappings.get(xpath);
            XSSFTable table = tableMappings.get(xpath);

            if (!xpath.matches(".*\\[.*")) {

                // Exports elements and attributes mapped with simpleXmlCell
                if (simpleXmlCell!=null) {
                    XSSFCell cell = simpleXmlCell.getReferencedCell();
                    if (cell!=null) {
                        Node currentNode = getNodeByXPath(xpath,doc.getFirstChild(),doc,false);
                        mapCellOnNode(cell,currentNode);
                        
                        //remove nodes which are empty in order to keep the output xml valid
                        // FIXME: what should be done if currentNode.getTextContent() is null?
                        if ("".equals(currentNode.getTextContent()) && currentNode.getParentNode() != null) {
                            currentNode.getParentNode().removeChild(currentNode);
                        }
                    }
                }

                // Exports elements and attributes mapped with tables
                if (table!=null) {

                    List<XSSFTableColumn> tableColumns = table.getColumns();

                    XSSFSheet sheet = table.getXSSFSheet();

                    int startRow = table.getStartCellReference().getRow() + table.getHeaderRowCount();
                    int endRow = table.getEndCellReference().getRow();

                    for(int i = startRow; i<= endRow; i++) {
                        XSSFRow row = sheet.getRow(i);

                        Node tableRootNode = getNodeByXPath(table.getCommonXpath(), doc.getFirstChild(), doc, true);

                        short startColumnIndex = table.getStartCellReference().getCol();
                        for (XSSFTableColumn tableColumn : tableColumns) {
                            XSSFCell cell = row.getCell(startColumnIndex + tableColumn.getColumnIndex());
                            if (cell != null) {
                                XSSFXmlColumnPr xmlColumnPr = tableColumn.getXmlColumnPr();
                                if (xmlColumnPr != null) {
                                    String localXPath = xmlColumnPr.getLocalXPath();
                                    Node currentNode = getNodeByXPath(localXPath,tableRootNode,doc,false);
                                    mapCellOnNode(cell, currentNode);
                                }
                            }
                        }
                    }
                }
            } /*else {
                // TODO:  implement filtering management in xpath
            }*/
        }

        boolean isValid = true;
        if (validate) {
            isValid =isValid(doc);
        }

        if (isValid) {

            /////////////////
            //Output the XML

            //set up a transformer
            Transformer trans = XMLHelper.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty(OutputKeys.ENCODING, encoding);

            //create string from xml tree

            StreamResult result = new StreamResult(os);
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);

        }
    }


    /**
     * Validate the generated XML against the XML Schema associated with the XSSFMap
     *
     * @param xml the XML to validate
     * @return true, if document is valid
     * @throws SAXException If validating the document fails
     */
    @SuppressWarnings({"squid:S2755"})
    private boolean isValid(Document xml) throws SAXException{
        try {
            SchemaFactory factory = XMLHelper.getSchemaFactory();

            Source source = new DOMSource(map.getSchema());
            Schema schema = factory.newSchema(source);
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(xml));
            
            //if no exceptions where raised, the document is valid
            return true;
        } catch(IOException e) {
            LOG.log(POILogger.ERROR, "document is not valid", e);
        }

        return false;
    }


    private void mapCellOnNode(XSSFCell cell, Node node) {

        String value ="";
        switch (cell.getCellType()) {

        case STRING: value = cell.getStringCellValue(); break;
        case BOOLEAN: value += cell.getBooleanCellValue(); break;
        case ERROR: value = cell.getErrorCellString();  break;
        case FORMULA:
           if (cell.getCachedFormulaResultType() == CellType.STRING) {
               value = cell.getStringCellValue();
           } else {
               if (DateUtil.isCellDateFormatted(cell)) {
                  value = getFormattedDate(cell);
               } else {
                  value += cell.getNumericCellValue();
               }
           }
           break;
        
        case NUMERIC: 
             if (DateUtil.isCellDateFormatted(cell)) {
                  value = getFormattedDate(cell);
              } else {
                 value += cell.getRawValue();
              }
            break;

        default:

        }
        if (node instanceof Element) {
            Element currentElement = (Element) node;
            currentElement.setTextContent(value);
        } else {
            node.setNodeValue(value);
        }
    }

    private String removeNamespace(String elementName) {
        return elementName.matches(".*:.*")?elementName.split(":")[1]:elementName;
    }

    private String getFormattedDate(XSSFCell cell) {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        sdf.setTimeZone(LocaleUtil.getUserTimeZone());
        return sdf.format(cell.getDateCellValue());
    }

    private Node getNodeByXPath(String xpath,Node rootNode,Document doc,boolean createMultipleInstances) {
        String[] xpathTokens = xpath.split("/");


        Node currentNode = rootNode;
        // The first token is empty, the second is the root node
        for(int i =2; i<xpathTokens.length;i++) {

            String axisName = removeNamespace(xpathTokens[i]);


            if (!axisName.startsWith("@")) {

                NodeList list =currentNode.getChildNodes();

                Node selectedNode = null;
                if (!(createMultipleInstances && i==xpathTokens.length-1) ) {
                    // select the last child node only if we need to map to a single cell
                    selectedNode = selectNode(axisName, list);
                }
                if (selectedNode==null) {
                    selectedNode = createElement(doc, currentNode, axisName);
                }
                currentNode = selectedNode;
            } else {
                currentNode = createAttribute(doc, currentNode, axisName);
            }
        }
        return currentNode;
    }

    private Node createAttribute(Document doc, Node currentNode, String axisName) {
        String attributeName = axisName.substring(1);
        NamedNodeMap attributesMap = currentNode.getAttributes();
        Node attribute = attributesMap.getNamedItem(attributeName);
        if (attribute==null) {
            attribute = doc.createAttributeNS("", attributeName);
            attributesMap.setNamedItem(attribute);
        }
        return attribute;
    }

    private Node createElement(Document doc, Node currentNode, String axisName) {
        Node selectedNode;
        if (isNamespaceDeclared()) {
            selectedNode =doc.createElementNS(getNamespace(),axisName);
        } else {
            selectedNode = doc.createElementNS("", axisName);
        }
        currentNode.appendChild(selectedNode);
        return selectedNode;
    }

    private Node selectNode(String axisName, NodeList list) {
        Node selectedNode = null;
        for(int j=0;j<list.getLength();j++) {
            Node node = list.item(j);
            if (node.getNodeName().equals(axisName)) {
                selectedNode=node;
                break;
            }
        }
        return selectedNode;
    }


    private boolean isNamespaceDeclared() {
        String schemaNamespace = getNamespace();
        return schemaNamespace!=null && !schemaNamespace.isEmpty();
    }

    private String getNamespace() {
        return map.getCTSchema().getNamespace();
    }


    /**
     * Compares two xpaths to define an ordering according to the XML Schema
     *
     */
    @Override
    public int compare(String leftXpath, String rightXpath) {
        Node xmlSchema = map.getSchema();

        String[] leftTokens = leftXpath.split("/");
        String[] rightTokens = rightXpath.split("/");
        String samePath = "";

        int minLength = Math.min(leftTokens.length, rightTokens.length);

        Node localComplexTypeRootNode = xmlSchema;

        for(int i =1;i <minLength; i++) {

            String leftElementName = leftTokens[i];
            String rightElementName = rightTokens[i];

            if (leftElementName.equals(rightElementName)) {
                samePath += "/" + leftElementName;
                localComplexTypeRootNode = getComplexTypeForElement(leftElementName, xmlSchema, localComplexTypeRootNode);
            } else {
                return indexOfElementInComplexType(samePath, leftElementName, rightElementName,localComplexTypeRootNode);
            }
        }

        return 0;
    }

    private int indexOfElementInComplexType(String samePath,String leftElementName,String rightElementName,Node complexType) {
        if(complexType == null) {
            return 0;
        }

        int i = 0;
        Node node = complexType.getFirstChild();
        final String leftWithoutNamespace = removeNamespace(leftElementName);
        int leftIndexOf = getAndStoreIndex(samePath, leftWithoutNamespace);
        final String rightWithoutNamespace = removeNamespace(rightElementName);
        int rightIndexOf = getAndStoreIndex(samePath, rightWithoutNamespace);

        while (node != null && (rightIndexOf==-1||leftIndexOf==-1)) {
            if (node instanceof Element && "element".equals(node.getLocalName())) {
                String elementValue = getNameOrRefElement(node).getNodeValue();
                if (elementValue.equals(leftWithoutNamespace)) {
                    leftIndexOf = i;
                    indexMap.put(samePath+"/"+leftWithoutNamespace, leftIndexOf);
                }
                if (elementValue.equals(rightWithoutNamespace)) {
                    rightIndexOf = i;
                    indexMap.put(samePath+"/"+rightWithoutNamespace, rightIndexOf);
                }
            }
            i++;
            node = node.getNextSibling();
        }
        if(leftIndexOf == -1 || rightIndexOf == -1) {
            return 0;
        }
        return Integer.compare(leftIndexOf, rightIndexOf);
    }
    
    private int getAndStoreIndex(String samePath,String withoutNamespace) {
        String withPath = samePath+"/"+withoutNamespace;
        return indexMap.getOrDefault(withPath, -1);
    }

	private Node getNameOrRefElement(Node node) {
		Node returnNode = node.getAttributes().getNamedItem("ref");
        if(returnNode != null) {
            return returnNode;
		}
		
        return node.getAttributes().getNamedItem("name");
	}

    private Node getComplexTypeForElement(String elementName,Node xmlSchema,Node localComplexTypeRootNode) {
        String elementNameWithoutNamespace = removeNamespace(elementName);

        String complexTypeName = getComplexTypeNameFromChildren(localComplexTypeRootNode, elementNameWithoutNamespace);

        // Note: we expect that all the complex types are defined at root level
        Node complexTypeNode = null;
        // FIXME: what should be done if complexTypeName is null?
        if (!"".equals(complexTypeName)) {
            complexTypeNode = getComplexTypeNodeFromSchemaChildren(xmlSchema, null, complexTypeName);
        }

        return complexTypeNode;
    }

    private String getComplexTypeNameFromChildren(Node localComplexTypeRootNode,
            String elementNameWithoutNamespace) {
        if(localComplexTypeRootNode == null) {
            return "";
        }

        Node node  = localComplexTypeRootNode.getFirstChild();
        String complexTypeName = "";

        while (node != null) {
            if ( node instanceof Element && "element".equals(node.getLocalName())) {
                Node nameAttribute = getNameOrRefElement(node);
                if (nameAttribute.getNodeValue().equals(elementNameWithoutNamespace)) {
                    Node complexTypeAttribute = node.getAttributes().getNamedItem("type");
                    if (complexTypeAttribute!=null) {
                        complexTypeName = complexTypeAttribute.getNodeValue();
                        break;
                    }
                }
            }
            node = node.getNextSibling();
        }
        return complexTypeName;
    }

    private Node getComplexTypeNodeFromSchemaChildren(Node xmlSchema, Node complexTypeNode,
            String complexTypeName) {
        Node node = xmlSchema.getFirstChild();
        while (node != null) {
            if ( node instanceof Element) {
                if ("complexType".equals(node.getLocalName())) {
                    Node nameAttribute = getNameOrRefElement(node);
                    if (nameAttribute.getNodeValue().equals(complexTypeName)) {
                        Node sequence = node.getFirstChild();
                        while(sequence != null) {

                            if ( sequence instanceof Element) {
                                final String localName = sequence.getLocalName();
                                if ("sequence".equals(localName) || "all".equals(localName)) {
                                    complexTypeNode = sequence;
                                    break;
                                }
                            }
                            sequence = sequence.getNextSibling();
                        }
                        if (complexTypeNode!=null) {
                            break;
                        }

                    }
                }
            }
            node = node.getNextSibling();
        }
        return complexTypeNode;
    }

    private static void trySet(String name, SecurityFeature securityFeature) {
        try {
            securityFeature.accept(name);
        } catch (Exception e) {
            LOG.log(POILogger.WARN, "SchemaFactory feature unsupported", name, e);
        } catch (AbstractMethodError ame) {
            LOG.log(POILogger.WARN, "Cannot set SchemaFactory feature because outdated XML parser in classpath", name, ame);
        }
    }
}
