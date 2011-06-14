package org.apache.poi.hwpf.extractor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.apache.poi.hwpf.model.ListLevel;
import org.apache.poi.hwpf.model.ListTables;
import org.apache.poi.hwpf.usermodel.BorderCode;
import org.apache.poi.hwpf.usermodel.CharacterProperties;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.hwpf.usermodel.SectionProperties;
import org.apache.poi.hwpf.usermodel.TableCell;
import org.apache.poi.hwpf.usermodel.TableIterator;
import org.apache.poi.hwpf.usermodel.TableRow;
import org.w3c.dom.Element;

public class WordToFoUtils {
    static final String EMPTY = "";

    public static final float TWIPS_PER_INCH = 1440.0f;

    public static final int TWIPS_PER_PT = 20;

    static boolean equals(String str1, String str2) {
	return str1 == null ? str2 == null : str1.equals(str2);
    }

    public static String getBorderType(BorderCode borderCode) {
	if (borderCode == null)
	    throw new IllegalArgumentException("borderCode is null");

	switch (borderCode.getBorderType()) {
	case 1:
	case 2:
	    return "solid";
	case 3:
	    return "double";
	case 5:
	    return "solid";
	case 6:
	    return "dotted";
	case 7:
	case 8:
	    return "dashed";
	case 9:
	    return "dotted";
	case 10:
	case 11:
	case 12:
	case 13:
	case 14:
	case 15:
	case 16:
	case 17:
	case 18:
	case 19:
	    return "double";
	case 20:
	    return "solid";
	case 21:
	    return "double";
	case 22:
	    return "dashed";
	case 23:
	    return "dashed";
	case 24:
	    return "ridge";
	case 25:
	    return "grooved";
	default:
	    return "solid";
	}
    }

    public static String getBorderWidth(BorderCode borderCode) {
	int lineWidth = borderCode.getLineWidth();
	int pt = lineWidth / 8;
	int pte = lineWidth - pt * 8;

	StringBuilder stringBuilder = new StringBuilder();
	stringBuilder.append(pt);
	stringBuilder.append(".");
	stringBuilder.append(1000 / 8 * pte);
	stringBuilder.append("pt");
	return stringBuilder.toString();
    }

    public static String getBulletText(ListTables listTables,
	    Paragraph paragraph, int listId) {
	final ListLevel listLevel = listTables.getLevel(listId,
		paragraph.getIlvl());

	if (listLevel.getNumberText() == null)
	    return EMPTY;

	StringBuffer bulletBuffer = new StringBuffer();
	char[] xst = listLevel.getNumberText().toCharArray();
	for (char element : xst) {
	    if (element < 9) {
		ListLevel numLevel = listTables.getLevel(listId, element);

		int num = numLevel.getStartAt();
		bulletBuffer.append(NumberFormatter.getNumber(num,
			listLevel.getNumberFormat()));

		if (numLevel == listLevel) {
		    numLevel.setStartAt(numLevel.getStartAt() + 1);
		}

	    } else {
		bulletBuffer.append(element);
	    }
	}

	byte follow = getIxchFollow(listLevel);
	switch (follow) {
	case 0:
	    bulletBuffer.append("\t");
	    break;
	case 1:
	    bulletBuffer.append(" ");
	    break;
	default:
	    break;
	}

	return bulletBuffer.toString();
    }

    public static String getColor(int ico) {
	switch (ico) {
	case 1:
	    return "black";
	case 2:
	    return "blue";
	case 3:
	    return "cyan";
	case 4:
	    return "green";
	case 5:
	    return "magenta";
	case 6:
	    return "red";
	case 7:
	    return "yellow";
	case 8:
	    return "white";
	case 9:
	    return "darkblue";
	case 10:
	    return "darkcyan";
	case 11:
	    return "darkgreen";
	case 12:
	    return "darkmagenta";
	case 13:
	    return "darkred";
	case 14:
	    return "darkyellow";
	case 15:
	    return "darkgray";
	case 16:
	    return "lightgray";
	default:
	    return "black";
	}
    }

    public static byte getIxchFollow(ListLevel listLevel) {
	try {
	    Field field = ListLevel.class.getDeclaredField("_ixchFollow");
	    field.setAccessible(true);
	    return ((Byte) field.get(listLevel)).byteValue();
	} catch (Exception exc) {
	    throw new Error(exc);
	}
    }

    public static String getListItemNumberLabel(int number, int format) {

	if (format != 0)
	    System.err.println("NYI: toListItemNumberLabel(): " + format);

	return String.valueOf(number);
    }

    public static SectionProperties getSectionProperties(Section section) {
	try {
	    Field field = Section.class.getDeclaredField("_props");
	    field.setAccessible(true);
	    return (SectionProperties) field.get(section);
	} catch (Exception exc) {
	    throw new Error(exc);
	}
    }

    static boolean isEmpty(String str) {
	return str == null || str.length() == 0;
    }

    static boolean isNotEmpty(String str) {
	return !isEmpty(str);
    }

    public static TableIterator newTableIterator(Range range, int level) {
	try {
	    Constructor<TableIterator> constructor = TableIterator.class
		    .getDeclaredConstructor(Range.class, int.class);
	    constructor.setAccessible(true);
	    return constructor.newInstance(range, Integer.valueOf(level));
	} catch (Exception exc) {
	    throw new Error(exc);
	}
    }

    public static void setBold(final Element element, final boolean bold) {
	element.setAttribute("font-weight", bold ? "bold" : "normal");
    }

    public static void setBorder(Element element, BorderCode borderCode,
	    String where) {
	if (element == null)
	    throw new IllegalArgumentException("element is null");

	if (borderCode == null)
	    return;

	if (isEmpty(where)) {
	    element.setAttribute("border-style", getBorderType(borderCode));
	    element.setAttribute("border-color",
		    getColor(borderCode.getColor()));
	    element.setAttribute("border-width", getBorderWidth(borderCode));
	} else {
	    element.setAttribute("border-" + where + "-style",
		    getBorderType(borderCode));
	    element.setAttribute("border-" + where + "-color",
		    getColor(borderCode.getColor()));
	    element.setAttribute("border-" + where + "-width",
		    getBorderWidth(borderCode));
	}
    }

    public static void setCharactersProperties(final CharacterRun characterRun,
	    final Element inline) {
	final CharacterProperties clonedProperties = characterRun
		.cloneProperties();
	StringBuilder textDecorations = new StringBuilder();

	setBorder(inline, clonedProperties.getBrc(), EMPTY);

	if (characterRun.isCapitalized()) {
	    inline.setAttribute("text-transform", "uppercase");
	}
	if (characterRun.isHighlighted()) {
	    inline.setAttribute("background-color",
		    getColor(clonedProperties.getIcoHighlight()));
	}
	if (characterRun.isStrikeThrough()) {
	    if (textDecorations.length() > 0)
		textDecorations.append(" ");
	    textDecorations.append("line-through");
	}
	if (characterRun.isShadowed()) {
	    inline.setAttribute("text-shadow", characterRun.getFontSize() / 24
		    + "pt");
	}
	if (characterRun.isSmallCaps()) {
	    inline.setAttribute("font-variant", "small-caps");
	}
	if (characterRun.getSubSuperScriptIndex() == 1) {
	    inline.setAttribute("baseline-shift", "super");
	    inline.setAttribute("font-size", "smaller");
	}
	if (characterRun.getSubSuperScriptIndex() == 2) {
	    inline.setAttribute("baseline-shift", "sub");
	    inline.setAttribute("font-size", "smaller");
	}
	if (characterRun.getUnderlineCode() > 0) {
	    if (textDecorations.length() > 0)
		textDecorations.append(" ");
	    textDecorations.append("underline");
	}
	if (textDecorations.length() > 0) {
	    inline.setAttribute("text-decoration", textDecorations.toString());
	}
    }

    public static void setFontFamily(final Element element,
	    final String fontFamily) {
	element.setAttribute("font-family", fontFamily);
    }

    public static void setFontSize(final Element element, final int fontSize) {
	element.setAttribute("font-size", String.valueOf(fontSize));
    }

    public static void setIndent(Paragraph paragraph, Element block) {
	if (paragraph.getFirstLineIndent() != 0) {
	    block.setAttribute(
		    "text-indent",
		    String.valueOf(paragraph.getFirstLineIndent()
			    / TWIPS_PER_PT)
			    + "pt");
	}
	if (paragraph.getIndentFromLeft() != 0) {
	    block.setAttribute(
		    "start-indent",
		    String.valueOf(paragraph.getIndentFromLeft() / TWIPS_PER_PT)
			    + "pt");
	}
	if (paragraph.getIndentFromRight() != 0) {
	    block.setAttribute(
		    "end-indent",
		    String.valueOf(paragraph.getIndentFromRight()
			    / TWIPS_PER_PT)
			    + "pt");
	}
	if (paragraph.getSpacingBefore() != 0) {
	    block.setAttribute("space-before",
		    String.valueOf(paragraph.getSpacingBefore() / TWIPS_PER_PT)
			    + "pt");
	}
	if (paragraph.getSpacingAfter() != 0) {
	    block.setAttribute("space-after",
		    String.valueOf(paragraph.getSpacingAfter() / TWIPS_PER_PT)
			    + "pt");
	}
    }

    public static void setItalic(final Element element, final boolean italic) {
	element.setAttribute("font-style", italic ? "italic" : "normal");
    }

    public static void setJustification(Paragraph paragraph,
	    final Element element) {
	final int justification = paragraph.getJustification();
	switch (justification) {
	case 0:
	    element.setAttribute("text-align", "start");
	    break;
	case 1:
	    element.setAttribute("text-align", "center");
	    break;
	case 2:
	    element.setAttribute("text-align", "end");
	    break;
	case 3:
	    element.setAttribute("text-align", "justify");
	    break;
	case 4:
	    element.setAttribute("text-align", "justify");
	    break;
	case 5:
	    element.setAttribute("text-align", "center");
	    break;
	case 6:
	    element.setAttribute("text-align", "left");
	    break;
	case 7:
	    element.setAttribute("text-align", "start");
	    break;
	case 8:
	    element.setAttribute("text-align", "end");
	    break;
	case 9:
	    element.setAttribute("text-align", "justify");
	    break;
	}
    }

    public static void setParagraphProperties(Paragraph paragraph, Element block) {
	setIndent(paragraph, block);
	setJustification(paragraph, block);

	setBorder(block, paragraph.getBottomBorder(), "bottom");
	setBorder(block, paragraph.getLeftBorder(), "left");
	setBorder(block, paragraph.getRightBorder(), "right");
	setBorder(block, paragraph.getTopBorder(), "top");

	if (paragraph.pageBreakBefore()) {
	    block.setAttribute("break-before", "page");
	}

	block.setAttribute("hyphenate",
		String.valueOf(paragraph.isAutoHyphenated()));

	if (paragraph.keepOnPage()) {
	    block.setAttribute("keep-together.within-page", "always");
	}

	if (paragraph.keepWithNext()) {
	    block.setAttribute("keep-with-next.within-page", "always");
	}

	block.setAttribute("linefeed-treatment", "preserve");
	block.setAttribute("white-space-collapse", "false");
    }

    public static void setTableCellProperties(TableRow tableRow,
	    TableCell tableCell, Element element, boolean toppest,
	    boolean bottomest, boolean leftest, boolean rightest) {
	element.setAttribute("width", (tableCell.getWidth() / TWIPS_PER_INCH)
		+ "in");
	element.setAttribute("padding-start",
		(tableRow.getGapHalf() / TWIPS_PER_INCH) + "in");
	element.setAttribute("padding-end",
		(tableRow.getGapHalf() / TWIPS_PER_INCH) + "in");

	BorderCode top = tableCell.getBrcTop() != null ? tableCell.getBrcTop()
		: toppest ? tableRow.getTopBorder() : tableRow
			.getHorizontalBorder();
	BorderCode bottom = tableCell.getBrcBottom() != null ? tableCell
		.getBrcBottom() : bottomest ? tableRow.getBottomBorder()
		: tableRow.getHorizontalBorder();

	BorderCode left = tableCell.getBrcLeft() != null ? tableCell
		.getBrcLeft() : leftest ? tableRow.getLeftBorder() : tableRow
		.getVerticalBorder();
	BorderCode right = tableCell.getBrcRight() != null ? tableCell
		.getBrcRight() : rightest ? tableRow.getRightBorder()
		: tableRow.getVerticalBorder();

	setBorder(element, bottom, "bottom");
	setBorder(element, left, "left");
	setBorder(element, right, "right");
	setBorder(element, top, "top");
    }

    public static void setTableRowProperties(TableRow tableRow,
	    Element tableRowElement) {
	if (tableRow.getRowHeight() > 0) {
	    tableRowElement.setAttribute("height",
		    (tableRow.getRowHeight() / TWIPS_PER_INCH) + "in");
	}
	if (!tableRow.cantSplit()) {
	    tableRowElement.setAttribute("keep-together", "always");
	}
    }

}
