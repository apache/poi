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

package org.apache.poi.hwpf.extractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.model.ListFormatOverride;
import org.apache.poi.hwpf.model.ListTables;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.hwpf.usermodel.SectionProperties;
import org.apache.poi.hwpf.usermodel.Table;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import static org.apache.poi.hwpf.extractor.WordToFoUtils.TWIPS_PER_INCH;

/**
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
public class WordToFoExtractor {

    private static final byte BEL_MARK = 7;

    private static final byte FIELD_BEGIN_MARK = 19;

    private static final byte FIELD_END_MARK = 21;

    private static final byte FIELD_SEPARATOR_MARK = 20;

    private static final String NS_XSLFO = "http://www.w3.org/1999/XSL/Format";

    private static HWPFDocument loadDoc(File docFile) throws IOException {
	final FileInputStream istream = new FileInputStream(docFile);
	try {
	    return new HWPFDocument(istream);
	} finally {
	    try {
		istream.close();
	    } catch (Exception exc) {
		// no op
	    }
	}
    }

    static Document process(File docFile) throws Exception {
	final HWPFDocument hwpfDocument = loadDoc(docFile);
	WordToFoExtractor wordToFoExtractor = new WordToFoExtractor(
		DocumentBuilderFactory.newInstance().newDocumentBuilder()
			.newDocument());
	wordToFoExtractor.processDocument(hwpfDocument);
	return wordToFoExtractor.getDocument();
    }

    private final Document document;

    private final Element layoutMasterSet;

    private final Element root;

    public WordToFoExtractor(Document document) throws Exception {
	this.document = document;

	root = document.createElementNS(NS_XSLFO, "fo:root");
	document.appendChild(root);

	layoutMasterSet = document.createElementNS(NS_XSLFO,
		"fo:layout-master-set");
	root.appendChild(layoutMasterSet);
    }

    protected Element addFlowToPageSequence(final Element pageSequence,
	    String flowName) {
	final Element flow = document.createElementNS(NS_XSLFO, "fo:flow");
	flow.setAttribute("flow-name", flowName);
	pageSequence.appendChild(flow);

	return flow;
    }

    protected Element addListItem(Element listBlock) {
	Element result = createListItem();
	listBlock.appendChild(result);
	return result;
    }

    protected Element addListItemBody(Element listItem) {
	Element result = createListItemBody();
	listItem.appendChild(result);
	return result;
    }

    protected Element addListItemLabel(Element listItem, String text) {
	Element result = createListItemLabel(text);
	listItem.appendChild(result);
	return result;
    }

    protected Element addPageSequence(String pageMaster) {
	final Element pageSequence = document.createElementNS(NS_XSLFO,
		"fo:page-sequence");
	pageSequence.setAttribute("master-reference", pageMaster);
	root.appendChild(pageSequence);
	return pageSequence;
    }

    protected Element addRegionBody(Element pageMaster) {
	final Element regionBody = document.createElementNS(NS_XSLFO,
		"fo:region-body");
	pageMaster.appendChild(regionBody);

	return regionBody;
    }

    protected Element addSimplePageMaster(String masterName) {
	final Element simplePageMaster = document.createElementNS(NS_XSLFO,
		"fo:simple-page-master");
	simplePageMaster.setAttribute("master-name", masterName);
	layoutMasterSet.appendChild(simplePageMaster);

	return simplePageMaster;
    }

    protected Element addTable(Element flow) {
	final Element table = document.createElementNS(NS_XSLFO, "fo:table");
	flow.appendChild(table);
	return table;
    }

    protected Element createBlock() {
	return document.createElementNS(NS_XSLFO, "fo:block");
    }

    protected Element createExternalGraphic(String source) {
	Element result = document.createElementNS(NS_XSLFO,
		"fo:external-graphic");
	result.setAttribute("src", "url('" + source + "')");
	return result;
    }

    protected Element createInline() {
	return document.createElementNS(NS_XSLFO, "fo:inline");
    }

    protected Element createLeader() {
	return document.createElementNS(NS_XSLFO, "fo:leader");
    }

    protected Element createListBlock() {
	return document.createElementNS(NS_XSLFO, "fo:list-block");
    }

    protected Element createListItem() {
	return document.createElementNS(NS_XSLFO, "fo:list-item");
    }

    protected Element createListItemBody() {
	return document.createElementNS(NS_XSLFO, "fo:list-item-body");
    }

    protected Element createListItemLabel(String text) {
	Element result = document.createElementNS(NS_XSLFO,
		"fo:list-item-label");
	Element block = createBlock();
	block.appendChild(document.createTextNode(text));
	result.appendChild(block);
	return result;
    }

    protected String createPageMaster(SectionProperties sep, String type,
	    int section) {
	float height = sep.getYaPage() / TWIPS_PER_INCH;
	float width = sep.getXaPage() / TWIPS_PER_INCH;
	float leftMargin = sep.getDxaLeft() / TWIPS_PER_INCH;
	float rightMargin = sep.getDxaRight() / TWIPS_PER_INCH;
	float topMargin = sep.getDyaTop() / TWIPS_PER_INCH;
	float bottomMargin = sep.getDyaBottom() / TWIPS_PER_INCH;

	// add these to the header
	String pageMasterName = type + "-page" + section;

	Element pageMaster = addSimplePageMaster(pageMasterName);
	pageMaster.setAttribute("page-height", height + "in");
	pageMaster.setAttribute("page-width", width + "in");

	Element regionBody = addRegionBody(pageMaster);
	regionBody.setAttribute("margin", topMargin + "in " + rightMargin
		+ "in " + bottomMargin + "in " + leftMargin + "in");

	/*
	 * 6.4.14 fo:region-body
	 *
	 * The values of the padding and border-width traits must be "0".
	 */
	// WordToFoUtils.setBorder(regionBody, sep.getBrcTop(), "top");
	// WordToFoUtils.setBorder(regionBody, sep.getBrcBottom(), "bottom");
	// WordToFoUtils.setBorder(regionBody, sep.getBrcLeft(), "left");
	// WordToFoUtils.setBorder(regionBody, sep.getBrcRight(), "right");

	if (sep.getCcolM1() > 0) {
	    regionBody.setAttribute("column-count", "" + (sep.getCcolM1() + 1));
	    if (sep.getFEvenlySpaced()) {
		regionBody.setAttribute("column-gap",
			(sep.getDxaColumns() / TWIPS_PER_INCH) + "in");
	    } else {
		regionBody.setAttribute("column-gap", "0.25in");
	    }
	}

	return pageMasterName;
    }

    protected Element createTableBody() {
	return document.createElementNS(NS_XSLFO, "fo:table-body");
    }

    protected Element createTableCell() {
	return document.createElementNS(NS_XSLFO, "fo:table-cell");
    }

    protected Element createTableHeader() {
	return document.createElementNS(NS_XSLFO, "fo:table-header");
    }

    protected Element createTableRow() {
	return document.createElementNS(NS_XSLFO, "fo:table-row");
    }

    protected Text createText(String data) {
	return document.createTextNode(data);
    }

    public Document getDocument() {
	return document;
    }

    public void processDocument(HWPFDocument hwpfDocument) {
	final Range range = hwpfDocument.getRange();

	for (int s = 0; s < range.numSections(); s++) {
	    processSection(hwpfDocument, range.getSection(s), s);
	}
    }

    @SuppressWarnings("unused")
    protected void processImage(Element currentBlock, Picture picture) {
	// no default implementation -- skip
    }

    protected void processParagraph(HWPFDocument hwpfDocument,
	    Element parentFopElement, int currentTableLevel,
	    Paragraph paragraph, String bulletText) {
	final Element block = createBlock();
	parentFopElement.appendChild(block);

	WordToFoUtils.setParagraphProperties(paragraph, block);

	final int charRuns = paragraph.numCharacterRuns();

	if (charRuns == 0) {
	    return;
	}

	final String pFontName;
	final int pFontSize;
	final boolean pBold;
	final boolean pItalic;
	{
	    CharacterRun characterRun = paragraph.getCharacterRun(0);
	    pFontSize = characterRun.getFontSize() / 2;
	    pFontName = characterRun.getFontName();
	    pBold = characterRun.isBold();
	    pItalic = characterRun.isItalic();
	}
	WordToFoUtils.setFontFamily(block, pFontName);
	WordToFoUtils.setFontSize(block, pFontSize);
	WordToFoUtils.setBold(block, pBold);
	WordToFoUtils.setItalic(block, pItalic);

	StringBuilder lineText = new StringBuilder();

	if (WordToFoUtils.isNotEmpty(bulletText)) {
	    Element inline = createInline();
	    block.appendChild(inline);

	    Text textNode = createText(bulletText);
	    inline.appendChild(textNode);

	    lineText.append(bulletText);
	}

	for (int c = 0; c < charRuns; c++) {
	    CharacterRun characterRun = paragraph.getCharacterRun(c);

	    String text = characterRun.text();
	    if (text.getBytes().length == 0)
		continue;

	    if (text.getBytes()[0] == FIELD_BEGIN_MARK) {
		int skipTo = tryImageWithinField(hwpfDocument, paragraph, c,
			block);

		if (skipTo != c) {
		    c = skipTo;
		    continue;
		}
		continue;
	    }
	    if (text.getBytes()[0] == FIELD_SEPARATOR_MARK) {
		continue;
	    }
	    if (text.getBytes()[0] == FIELD_END_MARK) {
		continue;
	    }

	    if (characterRun.isSpecialCharacter() || characterRun.isObj()
		    || characterRun.isOle2()) {
		continue;
	    }

	    Element inline = createInline();
	    if (characterRun.isBold() != pBold) {
		WordToFoUtils.setBold(inline, characterRun.isBold());
	    }
	    if (characterRun.isItalic() != pItalic) {
		WordToFoUtils.setItalic(inline, characterRun.isItalic());
	    }
	    if (!WordToFoUtils.equals(characterRun.getFontName(), pFontName)) {
		WordToFoUtils.setFontFamily(inline, characterRun.getFontName());
	    }
	    if (characterRun.getFontSize() / 2 != pFontSize) {
		WordToFoUtils.setFontSize(inline,
			characterRun.getFontSize() / 2);
	    }
	    WordToFoUtils.setCharactersProperties(characterRun, inline);
	    block.appendChild(inline);

	    if (text.endsWith("\r")
		    || (text.charAt(text.length() - 1) == BEL_MARK && currentTableLevel != 0))
		text = text.substring(0, text.length() - 1);

	    Text textNode = createText(text);
	    inline.appendChild(textNode);

	    lineText.append(text);
	}

	if (lineText.toString().trim().length() == 0) {
	    Element leader = createLeader();
	    block.appendChild(leader);
	}

	return;
    }

    protected void processSection(HWPFDocument hwpfDocument, Section section,
	    int sectionCounter) {
	String regularPage = createPageMaster(
		WordToFoUtils.getSectionProperties(section), "page",
		sectionCounter);

	Element pageSequence = addPageSequence(regularPage);
	Element flow = addFlowToPageSequence(pageSequence, "xsl-region-body");

	processSectionParagraphes(hwpfDocument, flow, section, 0);
    }

    protected void processSectionParagraphes(HWPFDocument hwpfDocument,
	    Element flow, Range range, int currentTableLevel) {
	final Map<Integer, Table> allTables = new HashMap<Integer, Table>();
	for (TableIterator tableIterator = WordToFoUtils.newTableIterator(
		range, currentTableLevel + 1); tableIterator.hasNext();) {
	    Table next = tableIterator.next();
	    allTables.put(Integer.valueOf(next.getStartOffset()), next);
	}

	final ListTables listTables = hwpfDocument.getListTables();
	int currentListInfo = 0;

	final int paragraphs = range.numParagraphs();
	for (int p = 0; p < paragraphs; p++) {
	    Paragraph paragraph = range.getParagraph(p);

	    if (allTables.containsKey(Integer.valueOf(paragraph
		    .getStartOffset()))) {
		Table table = allTables.get(Integer.valueOf(paragraph
			.getStartOffset()));
		processTable(hwpfDocument, flow, table, currentTableLevel + 1);
		continue;
	    }

	    if (paragraph.isInTable()
		    && paragraph.getTableLevel() != currentTableLevel) {
		continue;
	    }

	    if (paragraph.getIlfo() != currentListInfo) {
		currentListInfo = paragraph.getIlfo();
	    }

	    if (currentListInfo != 0) {
		final ListFormatOverride listFormatOverride = listTables
			.getOverride(paragraph.getIlfo());

		String label = WordToFoUtils.getBulletText(listTables,
			paragraph, listFormatOverride.getLsid());

		processParagraph(hwpfDocument, flow, currentTableLevel,
			paragraph, label);
	    } else {
		processParagraph(hwpfDocument, flow, currentTableLevel,
			paragraph, WordToFoUtils.EMPTY);
	    }
	}

    }

    protected void processTable(HWPFDocument hwpfDocument, Element flow,
	    Table table, int thisTableLevel) {
	Element tableElement = addTable(flow);

	Element tableHeader = createTableHeader();
	Element tableBody = createTableBody();

	final int tableRows = table.numRows();

	int maxColumns = Integer.MIN_VALUE;
	for (int r = 0; r < tableRows; r++) {
	    maxColumns = Math.max(maxColumns, table.getRow(r).numCells());
	}

	for (int r = 0; r < tableRows; r++) {
	    TableRow tableRow = table.getRow(r);

	    Element tableRowElement = createTableRow();
	    WordToFoUtils.setTableRowProperties(tableRow, tableRowElement);

	    final int rowCells = tableRow.numCells();
	    for (int c = 0; c < rowCells; c++) {
		TableCell tableCell = tableRow.getCell(c);

		if (tableCell.isMerged() && !tableCell.isFirstMerged())
		    continue;

		if (tableCell.isVerticallyMerged()
			&& !tableCell.isFirstVerticallyMerged())
		    continue;

		Element tableCellElement = createTableCell();
		WordToFoUtils.setTableCellProperties(tableRow, tableCell,
			tableCellElement, r == 0, r == tableRows - 1, c == 0,
			c == rowCells - 1);

		if (tableCell.isFirstMerged()) {
		    int count = 0;
		    for (int c1 = c; c1 < rowCells; c1++) {
			TableCell nextCell = tableRow.getCell(c1);
			if (nextCell.isMerged())
			    count++;
			if (!nextCell.isMerged())
			    break;
		    }
		    tableCellElement.setAttribute("number-columns-spanned", ""
			    + count);
		} else {
		    if (c == rowCells - 1 && c != maxColumns - 1) {
			tableCellElement.setAttribute("number-columns-spanned",
				"" + (maxColumns - c));
		    }
		}

		if (tableCell.isFirstVerticallyMerged()) {
		    int count = 0;
		    for (int r1 = r; r1 < tableRows; r1++) {
			TableRow nextRow = table.getRow(r1);
			if (nextRow.numCells() < c)
			    break;
			TableCell nextCell = nextRow.getCell(c);
			if (nextCell.isVerticallyMerged())
			    count++;
			if (!nextCell.isVerticallyMerged())
			    break;
		    }
		    tableCellElement.setAttribute("number-rows-spanned", ""
			    + count);
		}

		processSectionParagraphes(hwpfDocument, tableCellElement,
			tableCell, thisTableLevel);

		if (!tableCellElement.hasChildNodes()) {
		    tableCellElement.appendChild(createBlock());
		}

		tableRowElement.appendChild(tableCellElement);
	    }

	    if (tableRow.isTableHeader()) {
		tableHeader.appendChild(tableRowElement);
	    } else {
		tableBody.appendChild(tableRowElement);
	    }
	}

	if (tableHeader.hasChildNodes()) {
	    tableElement.appendChild(tableHeader);
	}
	if (tableBody.hasChildNodes()) {
	    tableElement.appendChild(tableBody);
	} else {
	    System.err.println("Table without body");
	}
    }

    protected int tryImageWithinField(HWPFDocument hwpfDocument,
	    Paragraph paragraph, int beginMark, Element currentBlock) {
	int separatorMark = -1;
	int pictureMark = -1;
	int endMark = -1;
	for (int c = beginMark + 1; c < paragraph.numCharacterRuns(); c++) {
	    CharacterRun characterRun = paragraph.getCharacterRun(c);

	    String text = characterRun.text();
	    if (text.getBytes().length == 0)
		continue;

	    if (text.getBytes()[0] == FIELD_SEPARATOR_MARK) {
		if (separatorMark != -1) {
		    // double;
		    return beginMark;
		}

		separatorMark = c;
		continue;
	    }

	    if (text.getBytes()[0] == FIELD_END_MARK) {
		if (endMark != -1) {
		    // double;
		    return beginMark;
		}

		endMark = c;
		break;
	    }

	    if (hwpfDocument.getPicturesTable().hasPicture(characterRun)) {
		if (pictureMark != -1) {
		    // double;
		    return beginMark;
		}

		pictureMark = c;
		continue;
	    }
	}

	if (separatorMark == -1 || pictureMark == -1 || endMark == -1)
	    return beginMark;

	final CharacterRun pictureRun = paragraph.getCharacterRun(pictureMark);
	final Picture picture = hwpfDocument.getPicturesTable().extractPicture(
		pictureRun, true);
	processImage(currentBlock, picture);

	return endMark;
    }


    /**
     * Java main() interface to interact with WordToFoExtractor
     *
     * <p>
     *     Usage: WordToFoExtractor infile outfile
     * </p>
     * Where infile is an input .doc file ( Word 97-2007)
     * which will be rendered as XSL-FO into outfile
     *
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: WordToFoExtractor <inputFile.doc> <saveTo.fo>");
            return;
        }

        System.out.println("Converting " + args[0]);
        System.out.println("Saving output to " + args[1]);
        try {
            Document doc = WordToFoExtractor.process(new File(args[0]));

            FileWriter out = new FileWriter(args[1]);
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(out);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");  // TODO set encoding from a command argument
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.transform(domSource, streamResult);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
