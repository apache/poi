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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.model.Table;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFMap;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.helpers.XSSFSingleXmlCell;
import org.apache.poi.xssf.usermodel.helpers.XSSFXmlColumnPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STXmlDataType;
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

    private XSSFMap map;

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
     * @param validate if true, validates the XML againts the XML Schema
     * @throws SAXException
     * @throws TransformerException  
     * @throws ParserConfigurationException 
     */
    public void exportToXML(OutputStream os, boolean validate) throws SAXException, ParserConfigurationException, TransformerException {
        exportToXML(os, "UTF-8", validate);
    }

    private Document getEmptyDocument() throws ParserConfigurationException{

        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        return doc;
    }

    /**
     * Exports the data in an XML stream
     *
     * @param os OutputStream in which will contain the output XML
     * @param encoding the output charset encoding
     * @param validate if true, validates the XML againts the XML Schema
     * @throws SAXException
     * @throws ParserConfigurationException 
     * @throws TransformerException 
     * @throws InvalidFormatException
     */
    public void exportToXML(OutputStream os, String encoding, boolean validate) throws SAXException, ParserConfigurationException, TransformerException{
        List<XSSFSingleXmlCell> singleXMLCells = map.getRelatedSingleXMLCell();
        List<Table> tables = map.getRelatedTables();

        String rootElement = map.getCtMap().getRootElement();

        Document doc = getEmptyDocument();

        Element root = null;

        if (isNamespaceDeclared()) {
            root=doc.createElementNS(getNamespace(),rootElement);
        } else {
            root=doc.createElement(rootElement);
        }
        doc.appendChild(root);


        List<String> xpaths = new Vector<String>();
        Map<String,XSSFSingleXmlCell> singleXmlCellsMappings = new HashMap<String,XSSFSingleXmlCell>();
        Map<String,Table> tableMappings = new HashMap<String,Table>();

        for(XSSFSingleXmlCell simpleXmlCell : singleXMLCells) {
            xpaths.add(simpleXmlCell.getXpath());
            singleXmlCellsMappings.put(simpleXmlCell.getXpath(), simpleXmlCell);
        }
        for(Table table : tables) {
            String commonXPath = table.getCommonXpath();
            xpaths.add(commonXPath);
            tableMappings.put(commonXPath, table);
        }


        Collections.sort(xpaths,this);

        for(String xpath : xpaths) {

            XSSFSingleXmlCell simpleXmlCell = singleXmlCellsMappings.get(xpath);
            Table table = tableMappings.get(xpath);

            if (!xpath.matches(".*\\[.*")) {

                // Exports elements and attributes mapped with simpleXmlCell
                if (simpleXmlCell!=null) {
                    XSSFCell cell = simpleXmlCell.getReferencedCell();
                    if (cell!=null) {
                        Node currentNode = getNodeByXPath(xpath,doc.getFirstChild(),doc,false);
                        STXmlDataType.Enum dataType = simpleXmlCell.getXmlDataType();
                        mapCellOnNode(cell,currentNode,dataType);
                    }
                }

                // Exports elements and attributes mapped with tables
                if (table!=null) {

                    List<XSSFXmlColumnPr> tableColumns = table.getXmlColumnPrs();

                    XSSFSheet sheet = table.getXSSFSheet();

                    int startRow = table.getStartCellReference().getRow();
                    // In mappings created with Microsoft Excel the first row contains the table header and must be skipped
                    startRow +=1;

                    int endRow = table.getEndCellReference().getRow();

                    for(int i = startRow; i<= endRow; i++) {
                        XSSFRow row = sheet.getRow(i);

                        Node tableRootNode = getNodeByXPath(table.getCommonXpath(),doc.getFirstChild(),doc,true);

                        short startColumnIndex = table.getStartCellReference().getCol();
                        for(int j = startColumnIndex; j<= table.getEndCellReference().getCol();j++) {
                            XSSFCell cell = row.getCell(j);
                            if (cell!=null) {
                                XSSFXmlColumnPr pointer = tableColumns.get(j-startColumnIndex);
                                String localXPath = pointer.getLocalXPath();
                                Node currentNode = getNodeByXPath(localXPath,tableRootNode,doc,false);
                                STXmlDataType.Enum dataType = pointer.getXmlDataType();


                                mapCellOnNode(cell,currentNode,dataType);
                            }

                        }

                    }



                }
            } else {
                // TODO:  implement filtering management in xpath
            }
        }

        boolean isValid = true;
        if (validate) {
            isValid =isValid(doc);
        }



        if (isValid) {

            /////////////////
            //Output the XML

            //set up a transformer
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
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
     * @return
     */
    private boolean isValid(Document xml) throws SAXException{
        boolean isValid = false;
        try{
            String language = "http://www.w3.org/2001/XMLSchema";
            SchemaFactory factory = SchemaFactory.newInstance(language);

            Source source = new DOMSource(map.getSchema());
            Schema schema = factory.newSchema(source);
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(xml));
            //if no exceptions where raised, the document is valid
            isValid=true;


        } catch(IOException e) {
            e.printStackTrace();
        }
        return isValid;
    }


    private void mapCellOnNode(XSSFCell cell, Node node, STXmlDataType.Enum  outputDataType) {

        String value ="";
        switch (cell.getCellType()) {

        case XSSFCell.CELL_TYPE_STRING: value = cell.getStringCellValue(); break;
        case XSSFCell.CELL_TYPE_BOOLEAN: value += cell.getBooleanCellValue(); break;
        case XSSFCell.CELL_TYPE_ERROR: value = cell.getErrorCellString();  break;
        case XSSFCell.CELL_TYPE_FORMULA: value = cell.getStringCellValue(); break;
        case XSSFCell.CELL_TYPE_NUMERIC: value += cell.getRawValue(); break;
        default: ;

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



    private Node getNodeByXPath(String xpath,Node rootNode,Document doc,boolean createMultipleInstances) {
        String[] xpathTokens = xpath.split("/");


        Node currentNode =rootNode;
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


                Node attribute = createAttribute(doc, currentNode, axisName);

                currentNode = attribute;
            }
        }
        return currentNode;
    }

    private Node createAttribute(Document doc, Node currentNode, String axisName) {
        String attributeName = axisName.substring(1);
        NamedNodeMap attributesMap = currentNode.getAttributes();
        Node attribute = attributesMap.getNamedItem(attributeName);
        if (attribute==null) {
            attribute = doc.createAttribute(attributeName);
            attributesMap.setNamedItem(attribute);
        }
        return attribute;
    }

    private Node createElement(Document doc, Node currentNode, String axisName) {
        Node selectedNode;
        if (isNamespaceDeclared()) {
            selectedNode =doc.createElementNS(getNamespace(),axisName);
        } else {
            selectedNode =doc.createElement(axisName);
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
        return schemaNamespace!=null && !schemaNamespace.equals("");
    }

    private String getNamespace() {
        return map.getCTSchema().getNamespace();
    }


    /**
     * Compares two xpaths to define an ordering according to the XML Schema
     *
     */
    public int compare(String leftXpath, String rightXpath) {

        int result = 0;
        Node xmlSchema = map.getSchema();


        String[] leftTokens = leftXpath.split("/");
        String[] rightTokens = rightXpath.split("/");

        int minLenght = leftTokens.length< rightTokens.length? leftTokens.length : rightTokens.length;

        Node localComplexTypeRootNode = xmlSchema;


        for(int i =1;i <minLenght; i++) {

            String leftElementName =leftTokens[i];
            String rightElementName = rightTokens[i];

            if (leftElementName.equals(rightElementName)) {


                Node complexType = getComplexTypeForElement(leftElementName, xmlSchema,localComplexTypeRootNode);
                localComplexTypeRootNode = complexType;
            } else {
                int leftIndex = indexOfElementInComplexType(leftElementName,localComplexTypeRootNode);
                int rightIndex = indexOfElementInComplexType(rightElementName,localComplexTypeRootNode);
                if (leftIndex!=-1 && rightIndex!=-1) {
                    if ( leftIndex < rightIndex) {
                        result = -1;
                    }if ( leftIndex > rightIndex) {
                        result = 1;
                    }
                } else {
                    // NOTE: the xpath doesn't match correctly in the schema
                }
            }
        }

        return result;
    }

    private int indexOfElementInComplexType(String elementName,Node complexType) {

        NodeList list  = complexType.getChildNodes();
        int indexOf = -1;

        for(int i=0; i< list.getLength();i++) {
            Node node = list.item(i);
            if (node instanceof Element) {
                if (node.getLocalName().equals("element")) {
                    Node nameAttribute  = node.getAttributes().getNamedItem("name");
                    if (nameAttribute.getNodeValue().equals(removeNamespace(elementName))) {
                        indexOf = i;
                        break;
                    }

                }
            }
        }
        return indexOf;
    }

    private Node getComplexTypeForElement(String elementName,Node xmlSchema,Node localComplexTypeRootNode) {
        Node complexTypeNode = null;

        String elementNameWithoutNamespace = removeNamespace(elementName);


        NodeList  list  = localComplexTypeRootNode.getChildNodes();
        String complexTypeName = "";



        for(int i=0; i< list.getLength();i++) {
            Node node = list.item(i);
            if ( node instanceof Element) {
                if (node.getLocalName().equals("element")) {
                    Node nameAttribute  = node.getAttributes().getNamedItem("name");
                    if (nameAttribute.getNodeValue().equals(elementNameWithoutNamespace)) {
                        Node complexTypeAttribute = node.getAttributes().getNamedItem("type");
                        if (complexTypeAttribute!=null) {
                            complexTypeName = complexTypeAttribute.getNodeValue();
                            break;
                        }
                    }
                }
            }
        }
        // Note: we expect that all the complex types are defined at root level
        if (!"".equals(complexTypeName)) {
            NodeList  complexTypeList  = xmlSchema.getChildNodes();
            for(int i=0; i< complexTypeList.getLength();i++) {
                Node node = list.item(i);
                if ( node instanceof Element) {
                    if (node.getLocalName().equals("complexType")) {
                        Node nameAttribute  = node.getAttributes().getNamedItem("name");
                        if (nameAttribute.getNodeValue().equals(complexTypeName)) {

                            NodeList complexTypeChildList  =node.getChildNodes();
                            for(int j=0; j<complexTypeChildList.getLength();j++) {
                                Node sequence = complexTypeChildList.item(j);

                                if ( sequence instanceof Element) {
                                    if (sequence.getLocalName().equals("sequence")) {
                                        complexTypeNode = sequence;
                                        break;
                                    }
                                }
                            }
                            if (complexTypeNode!=null) {
                                break;
                            }

                        }
                    }
                }
            }
        }
        return complexTypeNode;
    }
}
