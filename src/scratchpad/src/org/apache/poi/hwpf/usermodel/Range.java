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

package org.apache.poi.hwpf.usermodel;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.poi.util.Internal;

import org.apache.poi.hwpf.model.BytePropertyNode;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.model.CHPX;
import org.apache.poi.hwpf.model.FileInformationBlock;
import org.apache.poi.hwpf.model.ListTables;
import org.apache.poi.hwpf.model.PAPX;
import org.apache.poi.hwpf.model.PropertyNode;
import org.apache.poi.hwpf.model.SEPX;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.model.SubdocumentType;
import org.apache.poi.hwpf.model.TextPieceTable;
import org.apache.poi.hwpf.sprm.CharacterSprmCompressor;
import org.apache.poi.hwpf.sprm.ParagraphSprmCompressor;
import org.apache.poi.hwpf.sprm.SprmBuffer;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * This class is the central class of the HWPF object model. All properties that
 * apply to a range of characters in a Word document extend this class.
 *
 * It is possible to insert text and/or properties at the beginning or end of a
 * range.
 *
 * Ranges are only valid if there hasn't been an insert in a prior Range since
 * the Range's creation. Once an element (text, paragraph, etc.) has been
 * inserted into a Range, subsequent Ranges become unstable.
 *
 * @author Ryan Ackley
 */
public class Range { // TODO -instantiable superclass

    private POILogger logger = POILogFactory.getLogger( Range.class );
    
    @Deprecated
	public static final int TYPE_PARAGRAPH = 0;
    @Deprecated
	public static final int TYPE_CHARACTER = 1;
    @Deprecated
	public static final int TYPE_SECTION = 2;
    @Deprecated
	public static final int TYPE_TEXT = 3;
    @Deprecated
	public static final int TYPE_LISTENTRY = 4;
    @Deprecated
	public static final int TYPE_TABLE = 5;
    @Deprecated
	public static final int TYPE_UNDEFINED = 6;

	/** Needed so inserts and deletes will ripple up through containing Ranges */
	private WeakReference<Range> _parent;

	/** The starting character offset of this range. */
	protected int _start;

	/** The ending character offset of this range. */
	protected int _end;

	/** The document this range blongs to. */
	protected HWPFDocumentCore _doc;

	/** Have we loaded the section indexes yet */
	boolean _sectionRangeFound;

	/** All sections that belong to the document this Range belongs to. */
	protected List<SEPX> _sections;

	/** The start index in the sections list for this Range */
	protected int _sectionStart;

	/** The end index in the sections list for this Range. */
	protected int _sectionEnd;

	/** Have we loaded the paragraph indexes yet. */
	protected boolean _parRangeFound;

	/** All paragraphs that belong to the document this Range belongs to. */
	protected List<PAPX> _paragraphs;

	/** The start index in the paragraphs list for this Range, inclusive */
	protected int _parStart;

	/** The end index in the paragraphs list for this Range, exclusive */
	protected int _parEnd;

	/** Have we loaded the characterRun indexes yet. */
	protected boolean _charRangeFound;

	/** All CharacterRuns that belong to the document this Range belongs to. */
	protected List<CHPX> _characters;

	/** The start index in the characterRuns list for this Range */
	protected int _charStart;

	/** The end index in the characterRuns list for this Range. */
	protected int _charEnd;

	protected StringBuilder _text;
	
	// protected Range()
	// {
	//
	// }

	/**
	 * Used to construct a Range from a document. This is generally used to
	 * create a Range that spans the whole document, or at least one whole part
	 * of the document (eg main text, header, comment)
	 *
	 * @param start
	 *            Starting character offset of the range.
	 * @param end
	 *            Ending character offset of the range.
	 * @param doc
	 *            The HWPFDocument the range is based on.
	 */
	public Range(int start, int end, HWPFDocumentCore doc) {
		_start = start;
		_end = end;
		_doc = doc;
		_sections = _doc.getSectionTable().getSections();
		_paragraphs = _doc.getParagraphTable().getParagraphs();
		_characters = _doc.getCharacterTable().getTextRuns();
		_text = _doc.getText();
		_parent = new WeakReference<Range>(null);

		sanityCheckStartEnd();
	}

	/**
	 * Used to create Ranges that are children of other Ranges.
	 *
	 * @param start
	 *            Starting character offset of the range.
	 * @param end
	 *            Ending character offset of the range.
	 * @param parent
	 *            The parent this range belongs to.
	 */
	protected Range(int start, int end, Range parent) {
		_start = start;
		_end = end;
		_doc = parent._doc;
		_sections = parent._sections;
		_paragraphs = parent._paragraphs;
		_characters = parent._characters;
		_text = parent._text;
		_parent = new WeakReference<Range>(parent);

		sanityCheckStartEnd();
		assert sanityCheck();
	}

	/**
	 * Constructor used to build a Range from indexes in one of its internal
	 * lists.
	 *
	 * @param startIdx
	 *            The starting index in the list, inclusive
	 * @param endIdx
	 *            The ending index in the list, exclusive
	 * @param idxType
	 *            The list type.
	 * @param parent
	 *            The parent Range this range belongs to.
	 */
	@Deprecated
	protected Range(int startIdx, int endIdx, int idxType, Range parent) {
		_doc = parent._doc;
		_sections = parent._sections;
		_paragraphs = parent._paragraphs;
		_characters = parent._characters;
		_text = parent._text;
		_parent = new WeakReference<Range>(parent);

		sanityCheckStartEnd();
	}

	/**
	 * Ensures that the start and end were were given are actually valid, to
	 * avoid issues later on if they're not
	 */
	private void sanityCheckStartEnd() {
		if (_start < 0) {
			throw new IllegalArgumentException("Range start must not be negative. Given " + _start);
		}
		if (_end < _start) {
			throw new IllegalArgumentException("The end (" + _end
					+ ") must not be before the start (" + _start + ")");
		}
	}

    /**
     * @return always return true
     * @deprecated Range is not linked to any text piece anymore, so to check if
     *             unicode is used please access {@link TextPieceTable} during
     *             document load time
     */
    @Deprecated
    public boolean usesUnicode()
    {
        return true;
    }

	/**
	 * Gets the text that this Range contains.
	 *
	 * @return The text for this range.
	 */
	public String text() {
	    return _text.substring( _start, _end );
	}

	/**
	 * Removes any fields (eg macros, page markers etc) from the string.
	 * Normally used to make some text suitable for showing to humans, and the
	 * resultant text should not normally be saved back into the document!
	 */
	public static String stripFields(String text) {
		// First up, fields can be nested...
		// A field can be 0x13 [contents] 0x15
		// Or it can be 0x13 [contents] 0x14 [real text] 0x15

		// If there are no fields, all easy
		if (text.indexOf('\u0013') == -1)
			return text;

		// Loop over until they're all gone
		// That's when we're out of both 0x13s and 0x15s
		while (text.indexOf('\u0013') > -1 && text.indexOf('\u0015') > -1) {
			int first13 = text.indexOf('\u0013');
			int next13 = text.indexOf('\u0013', first13 + 1);
			int first14 = text.indexOf('\u0014', first13 + 1);
			int last15 = text.lastIndexOf('\u0015');

			// If they're the wrong way around, give up
			if (last15 < first13) {
				break;
			}

			// If no more 13s and 14s, just zap
			if (next13 == -1 && first14 == -1) {
				text = text.substring(0, first13) + text.substring(last15 + 1);
				break;
			}

			// If a 14 comes before the next 13, then
			// zap from the 13 to the 14, and remove
			// the 15
			if (first14 != -1 && (first14 < next13 || next13 == -1)) {
				text = text.substring(0, first13) + text.substring(first14 + 1, last15)
						+ text.substring(last15 + 1);
				continue;
			}

			// Another 13 comes before the next 14.
			// This means there's nested stuff, so we
			// can just zap the lot
			text = text.substring(0, first13) + text.substring(last15 + 1);
			continue;
		}

		return text;
	}

	/**
	 * Used to get the number of sections in a range. If this range is smaller
	 * than a section, it will return 1 for its containing section.
	 *
	 * @return The number of sections in this range.
	 */
	public int numSections() {
		initSections();
		return _sectionEnd - _sectionStart;
	}

	/**
	 * Used to get the number of paragraphs in a range. If this range is smaller
	 * than a paragraph, it will return 1 for its containing paragraph.
	 *
	 * @return The number of paragraphs in this range.
	 */

	public int numParagraphs() {
		initParagraphs();
		return _parEnd - _parStart;
	}

	/**
	 *
	 * @return The number of characterRuns in this range.
	 */

	public int numCharacterRuns() {
		initCharacterRuns();
		return _charEnd - _charStart;
	}

    /**
     * Inserts text into the front of this range.
     * 
     * @param text
     *            The text to insert
     * @return The character run that text was inserted into.
     */
    public CharacterRun insertBefore( String text )
    {
        initAll();

        _text.insert( _start, text );
        _doc.getCharacterTable().adjustForInsert( _charStart, text.length() );
        _doc.getParagraphTable().adjustForInsert( _parStart, text.length() );
        _doc.getSectionTable().adjustForInsert( _sectionStart, text.length() );
        if ( _doc instanceof HWPFDocument )
        {
            ( (BookmarksImpl) ( (HWPFDocument) _doc ).getBookmarks() )
                    .afterInsert( _start, text.length() );
        }
        adjustForInsert( text.length() );

        // update the FIB.CCPText + friends fields
        adjustFIB( text.length() );

        assert sanityCheck();

        return getCharacterRun( 0 );
    }

    /**
     * Inserts text onto the end of this range
     * 
     * @param text
     *            The text to insert
     * @return The character run the text was inserted into.
     */
    public CharacterRun insertAfter( String text )
    {
        initAll();

        _text.insert( _end, text );

        _doc.getCharacterTable().adjustForInsert( _charEnd - 1, text.length() );
        _doc.getParagraphTable().adjustForInsert( _parEnd - 1, text.length() );
        _doc.getSectionTable().adjustForInsert( _sectionEnd - 1, text.length() );
        if ( _doc instanceof HWPFDocument )
        {
            ( (BookmarksImpl) ( (HWPFDocument) _doc ).getBookmarks() )
                    .afterInsert( _end, text.length() );
        }
        adjustForInsert( text.length() );

        assert sanityCheck();
        return getCharacterRun( numCharacterRuns() - 1 );
    }

	/**
	 * Inserts text into the front of this range and it gives that text the
	 * CharacterProperties specified in props.
	 *
	 * @param text
	 *            The text to insert.
	 * @param props
	 *            The CharacterProperties to give the text.
	 * @return A new CharacterRun that has the given text and properties and is
	 *         n ow a part of the document.
     * @deprecated User code should not work with {@link CharacterProperties}
	 */
    @Deprecated
	public CharacterRun insertBefore(String text, CharacterProperties props)
	// throws UnsupportedEncodingException
	{
		initAll();
		PAPX papx = _paragraphs.get(_parStart);
		short istd = papx.getIstd();

		StyleSheet ss = _doc.getStyleSheet();
		CharacterProperties baseStyle = ss.getCharacterStyle(istd);
		byte[] grpprl = CharacterSprmCompressor.compressCharacterProperty(props, baseStyle);
		SprmBuffer buf = new SprmBuffer(grpprl, 0);
		_doc.getCharacterTable().insert(_charStart, _start, buf);

		return insertBefore(text);
	}

	/**
	 * Inserts text onto the end of this range and gives that text the
	 * CharacterProperties specified in props.
	 *
	 * @param text
	 *            The text to insert.
	 * @param props
	 *            The CharacterProperties to give the text.
	 * @return A new CharacterRun that has the given text and properties and is
	 *         n ow a part of the document.
	 * @deprecated User code should not work with {@link CharacterProperties}
	 */
    @Deprecated
	public CharacterRun insertAfter(String text, CharacterProperties props)
	// throws UnsupportedEncodingException
	{
		initAll();
		PAPX papx = _paragraphs.get(_parEnd - 1);
		short istd = papx.getIstd();

		StyleSheet ss = _doc.getStyleSheet();
		CharacterProperties baseStyle = ss.getCharacterStyle(istd);
		byte[] grpprl = CharacterSprmCompressor.compressCharacterProperty(props, baseStyle);
		SprmBuffer buf = new SprmBuffer(grpprl, 0);
		_doc.getCharacterTable().insert(_charEnd, _end, buf);
		_charEnd++;
		return insertAfter(text);
	}

	/**
	 * Inserts and empty paragraph into the front of this range.
	 *
	 * @param props
	 *            The properties that the new paragraph will have.
	 * @param styleIndex
	 *            The index into the stylesheet for the new paragraph.
	 * @return The newly inserted paragraph.
	 * @deprecated Use code shall not work with {@link ParagraphProperties}
	 */
	@Deprecated
	public Paragraph insertBefore(ParagraphProperties props, int styleIndex)
	// throws UnsupportedEncodingException
	{
		return this.insertBefore(props, styleIndex, "\r");
	}

	/**
	 * Inserts a paragraph into the front of this range. The paragraph will
	 * contain one character run that has the default properties for the
	 * paragraph's style.
	 *
	 * It is necessary for the text to end with the character '\r'
	 *
	 * @param props
	 *            The paragraph's properties.
	 * @param styleIndex
	 *            The index of the paragraph's style in the style sheet.
	 * @param text
	 *            The text to insert.
	 * @return A newly inserted paragraph.
     * @deprecated Use code shall not work with {@link ParagraphProperties}
	 */
    @Deprecated
	protected Paragraph insertBefore(ParagraphProperties props, int styleIndex, String text)
	// throws UnsupportedEncodingException
	{
		initAll();
		StyleSheet ss = _doc.getStyleSheet();
		ParagraphProperties baseStyle = ss.getParagraphStyle(styleIndex);
		CharacterProperties baseChp = ss.getCharacterStyle(styleIndex);

		byte[] grpprl = ParagraphSprmCompressor.compressParagraphProperty(props, baseStyle);
		byte[] withIndex = new byte[grpprl.length + LittleEndian.SHORT_SIZE];
		LittleEndian.putShort(withIndex, (short) styleIndex);
		System.arraycopy(grpprl, 0, withIndex, LittleEndian.SHORT_SIZE, grpprl.length);
		SprmBuffer buf = new SprmBuffer(withIndex, 2);

		_doc.getParagraphTable().insert(_parStart, _start, buf);
		insertBefore(text, baseChp);
		return getParagraph(0);
	}

	/**
	 * Inserts and empty paragraph into the end of this range.
	 *
	 * @param props
	 *            The properties that the new paragraph will have.
	 * @param styleIndex
	 *            The index into the stylesheet for the new paragraph.
	 * @return The newly inserted paragraph.
     * @deprecated Use code shall not work with {@link ParagraphProperties}
	 */
    @Deprecated
	public Paragraph insertAfter(ParagraphProperties props, int styleIndex)
	// throws UnsupportedEncodingException
	{
		return this.insertAfter(props, styleIndex, "\r");
	}

	/**
	 * Inserts a paragraph into the end of this range. The paragraph will
	 * contain one character run that has the default properties for the
	 * paragraph's style.
	 *
	 * It is necessary for the text to end with the character '\r'
	 *
	 * @param props
	 *            The paragraph's properties.
	 * @param styleIndex
	 *            The index of the paragraph's style in the style sheet.
	 * @param text
	 *            The text to insert.
	 * @return A newly inserted paragraph.
     * @deprecated Use code shall not work with {@link ParagraphProperties}
	 */
    @Deprecated
	protected Paragraph insertAfter(ParagraphProperties props, int styleIndex, String text)
	// throws UnsupportedEncodingException
	{
		initAll();
		StyleSheet ss = _doc.getStyleSheet();
		ParagraphProperties baseStyle = ss.getParagraphStyle(styleIndex);
		CharacterProperties baseChp = ss.getCharacterStyle(styleIndex);

		byte[] grpprl = ParagraphSprmCompressor.compressParagraphProperty(props, baseStyle);
		byte[] withIndex = new byte[grpprl.length + LittleEndian.SHORT_SIZE];
		LittleEndian.putShort(withIndex, (short) styleIndex);
		System.arraycopy(grpprl, 0, withIndex, LittleEndian.SHORT_SIZE, grpprl.length);
		SprmBuffer buf = new SprmBuffer(withIndex, 2);

		_doc.getParagraphTable().insert(_parEnd, _end, buf);
		_parEnd++;
		insertAfter(text, baseChp);
		return getParagraph(numParagraphs() - 1);
	}

	public void delete() {

		initAll();

		int numSections = _sections.size();
		int numRuns = _characters.size();
		int numParagraphs = _paragraphs.size();

		for (int x = _charStart; x < numRuns; x++) {
			CHPX chpx = _characters.get(x);
			chpx.adjustForDelete(_start, _end - _start);
		}

		for (int x = _parStart; x < numParagraphs; x++) {
			PAPX papx = _paragraphs.get(x);
			// System.err.println("Paragraph " + x + " was " + papx.getStart() +
			// " -> " + papx.getEnd());
			papx.adjustForDelete(_start, _end - _start);
			// System.err.println("Paragraph " + x + " is now " +
			// papx.getStart() + " -> " + papx.getEnd());
		}

		for (int x = _sectionStart; x < numSections; x++) {
			SEPX sepx = _sections.get(x);
			// System.err.println("Section " + x + " was " + sepx.getStart() +
			// " -> " + sepx.getEnd());
			sepx.adjustForDelete(_start, _end - _start);
			// System.err.println("Section " + x + " is now " + sepx.getStart()
			// + " -> " + sepx.getEnd());
		}

        if ( _doc instanceof HWPFDocument )
        {
            ( (BookmarksImpl) ( (HWPFDocument) _doc ).getBookmarks() )
                    .afterDelete( _start, ( _end - _start ) );
        }

        _text.delete( _start, _end );
        Range parent = _parent.get();
        if ( parent != null )
        {
            parent.adjustForInsert( -( _end - _start ) );
        }

		// update the FIB.CCPText + friends field
		adjustFIB(-(_end - _start));
	}

    /**
     * Inserts a simple table into the beginning of this range. The number of
     * columns is determined by the TableProperties passed into this function.
     * 
     * @param props
     *            The table properties for the table.
     * @param rows
     *            The number of rows.
     * @return The empty Table that is now part of the document.
     * @deprecated Use code shall not work with {@link TableProperties}. Use
     *             {@link #insertTableBefore(short, int)} instead
     */
	@Deprecated
	public Table insertBefore(TableProperties props, int rows) {
		ParagraphProperties parProps = new ParagraphProperties();
		parProps.setFInTable(true);
		parProps.setItap( 1 );

		final int oldEnd = this._end;
		
		int columns = props.getItcMac();
        for ( int x = 0; x < rows; x++ )
        {
            Paragraph cell = this.insertBefore( parProps, StyleSheet.NIL_STYLE );
            cell.insertAfter( String.valueOf( '\u0007' ) );
            for ( int y = 1; y < columns; y++ )
            {
                cell = cell.insertAfter( parProps, StyleSheet.NIL_STYLE );
                cell.insertAfter( String.valueOf( '\u0007' ) );
            }
            cell = cell.insertAfter( parProps, StyleSheet.NIL_STYLE,
                    String.valueOf( '\u0007' ) );
            cell.setTableRowEnd( props );
        }

        final int newEnd = this._end;
        final int diff = newEnd - oldEnd;

        return new Table( _start, _start + diff, this, 1 );
    }

    /**
     * Inserts a simple table into the beginning of this range.
     * 
     * @param columns
     *            The number of columns
     * @param rows
     *            The number of rows.
     * @return The empty Table that is now part of the document.
     */
	public Table insertTableBefore(short columns, int rows) {
        ParagraphProperties parProps = new ParagraphProperties();
        parProps.setFInTable(true);
        parProps.setItap( 1 );

        final int oldEnd = this._end;
        
        for ( int x = 0; x < rows; x++ )
        {
            Paragraph cell = this.insertBefore( parProps, StyleSheet.NIL_STYLE );
            cell.insertAfter( String.valueOf( '\u0007' ) );
            for ( int y = 1; y < columns; y++ )
            {
                cell = cell.insertAfter( parProps, StyleSheet.NIL_STYLE );
                cell.insertAfter( String.valueOf( '\u0007' ) );
            }
            cell = cell.insertAfter( parProps, StyleSheet.NIL_STYLE,
                    String.valueOf( '\u0007' ) );
            cell.setTableRowEnd( new TableProperties( columns ) );
        }

        final int newEnd = this._end;
        final int diff = newEnd - oldEnd;

        return new Table( _start, _start + diff, this, 1 );
	}
	
	/**
	 * Inserts a list into the beginning of this range.
	 *
	 * @param props
	 *            The properties of the list entry. All list entries are
	 *            paragraphs.
	 * @param listID
	 *            The id of the list that contains the properties.
	 * @param level
	 *            The indentation level of the list.
	 * @param styleIndex
	 *            The base style's index in the stylesheet.
	 * @return The empty ListEntry that is now part of the document.
     * @deprecated Use code shall not work with {@link ParagraphProperties}
	 */
	@Deprecated
	public ListEntry insertBefore(ParagraphProperties props, int listID, int level, int styleIndex) {
		ListTables lt = _doc.getListTables();
		if (lt.getLevel(listID, level) == null) {
			throw new NoSuchElementException("The specified list and level do not exist");
		}

		int ilfo = lt.getOverrideIndexFromListID(listID);
		props.setIlfo(ilfo);
		props.setIlvl((byte) level);

		return (ListEntry) insertBefore(props, styleIndex);
	}

	/**
	 * Inserts a list into the beginning of this range.
	 *
	 * @param props
	 *            The properties of the list entry. All list entries are
	 *            paragraphs.
	 * @param listID
	 *            The id of the list that contains the properties.
	 * @param level
	 *            The indentation level of the list.
	 * @param styleIndex
	 *            The base style's index in the stylesheet.
	 * @return The empty ListEntry that is now part of the document.
     * @deprecated Use code shall not work with {@link ParagraphProperties}
	 */
	@Deprecated
	public ListEntry insertAfter(ParagraphProperties props, int listID, int level, int styleIndex) {
		ListTables lt = _doc.getListTables();
		if (lt.getLevel(listID, level) == null) {
			throw new NoSuchElementException("The specified list and level do not exist");
		}
		int ilfo = lt.getOverrideIndexFromListID(listID);
		props.setIlfo(ilfo);
		props.setIlvl((byte) level);

		return (ListEntry) insertAfter(props, styleIndex);
	}

    /**
     * Replace range text with new one, adding it to the range and deleting
     * original text from document
     * 
     * @param newText
     *            The text to be replaced with
     * @param addAfter
     *            if <tt>true</tt> the text will be added at the end of current
     *            range, otherwise to the beginning
     */
    public void replaceText( String newText, boolean addAfter )
    {
        if ( addAfter )
        {
            int originalEnd = getEndOffset();
            insertAfter( newText );
            new Range( getStartOffset(), originalEnd, this ).delete();
        }
        else
        {
            int originalStart = getStartOffset();
            int originalEnd = getEndOffset();

            insertBefore( newText );
            new Range( originalStart + newText.length(), originalEnd
                    + newText.length(), this ).delete();
        }
    }

	/**
	 * Replace (one instance of) a piece of text with another...
	 *
	 * @param pPlaceHolder
	 *            The text to be replaced (e.g., "${organization}")
	 * @param pValue
	 *            The replacement text (e.g., "Apache Software Foundation")
	 * @param pOffset
	 *            The offset or index where the text to be replaced begins
	 *            (relative to/within this <code>Range</code>)
	 */
	@Internal
	public void replaceText(String pPlaceHolder, String pValue, int pOffset) {
		int absPlaceHolderIndex = getStartOffset() + pOffset;

		Range subRange = new Range(absPlaceHolderIndex, (absPlaceHolderIndex + pPlaceHolder
				.length()), this);
		subRange.insertBefore(pValue);

		// re-create the sub-range so we can delete it
		subRange = new Range((absPlaceHolderIndex + pValue.length()), (absPlaceHolderIndex
				+ pPlaceHolder.length() + pValue.length()), this);

		// deletes are automagically propagated
		subRange.delete();
	}

	/**
	 * Replace (all instances of) a piece of text with another...
	 *
	 * @param pPlaceHolder
	 *            The text to be replaced (e.g., "${organization}")
	 * @param pValue
	 *            The replacement text (e.g., "Apache Software Foundation")
	 */
	public void replaceText(String pPlaceHolder, String pValue) {
		boolean keepLooking = true;
		while (keepLooking) {

			String text = text();
			int offset = text.indexOf(pPlaceHolder);
			if (offset >= 0)
				replaceText(pPlaceHolder, pValue, offset);
			else
				keepLooking = false;
		}
	}

	/**
	 * Gets the character run at index. The index is relative to this range.
	 *
	 * @param index
	 *            The index of the character run to get.
	 * @return The character run at the specified index in this range.
	 */
    public CharacterRun getCharacterRun( int index )
    {
        initCharacterRuns();

        if ( index + _charStart >= _charEnd )
            throw new IndexOutOfBoundsException( "CHPX #" + index + " ("
                    + ( index + _charStart ) + ") not in range [" + _charStart
                    + "; " + _charEnd + ")" );

        CHPX chpx = _characters.get( index + _charStart );
        if ( chpx == null )
        {
            return null;
        }

        short istd;
        if ( this instanceof Paragraph )
        {
            istd = ((Paragraph) this)._istd;
        }
        else
        {
            int[] point = findRange( _paragraphs,
                    Math.max( chpx.getStart(), _start ),
                    Math.min( chpx.getEnd(), _end ) );

            initParagraphs();
            int parStart = Math.max( point[0], _parStart );

            if ( parStart >= _paragraphs.size() )
            {
                return null;
            }

            PAPX papx = _paragraphs.get( point[0] );
            istd = papx.getIstd();
        }

        CharacterRun chp = new CharacterRun( chpx, _doc.getStyleSheet(), istd,
                this );

        return chp;
    }

	/**
	 * Gets the section at index. The index is relative to this range.
	 *
	 * @param index
	 *            The index of the section to get.
	 * @return The section at the specified index in this range.
	 */
	public Section getSection(int index) {
		initSections();
		SEPX sepx = _sections.get(index + _sectionStart);
		Section sep = new Section(sepx, this);
		return sep;
	}

	/**
	 * Gets the paragraph at index. The index is relative to this range.
	 *
	 * @param index
	 *            The index of the paragraph to get.
	 * @return The paragraph at the specified index in this range.
	 */

	public Paragraph getParagraph(int index) {
        initParagraphs();

        if ( index + _parStart >= _parEnd )
            throw new IndexOutOfBoundsException( "Paragraph #" + index + " ("
                    + (index + _parStart) + ") not in range [" + _parStart
                    + "; " + _parEnd + ")" );

		PAPX papx = _paragraphs.get(index + _parStart);
		return Paragraph.newParagraph( this, papx );
	}

	/**
	 * This method is used to determine the type. Handy for switch statements
	 * compared to the instanceof operator.
	 *
	 * @return A TYPE constant.
	 */
	@Deprecated
	public int type() {
		return TYPE_UNDEFINED;
	}

	/**
	 * Gets the table that starts with paragraph. In a Word file, a table
	 * consists of a group of paragraphs with certain flags set.
	 *
	 * @param paragraph
	 *            The paragraph that is the first paragraph in the table.
	 * @return The table that starts with paragraph
	 */
	public Table getTable(Paragraph paragraph) {
		if (!paragraph.isInTable()) {
			throw new IllegalArgumentException("This paragraph doesn't belong to a table");
		}

		Range r = paragraph;
		if (r._parent.get() != this) {
			throw new IllegalArgumentException("This paragraph is not a child of this range instance");
		}

        r.initAll();
        int tableLevel = paragraph.getTableLevel();
        int tableEndInclusive = r._parStart;

        if ( r._parStart != 0 )
        {
            Paragraph previous = Paragraph.newParagraph( this,
                    _paragraphs.get( r._parStart - 1 ) );
            if ( previous.isInTable() && //
                    previous.getTableLevel() == tableLevel //
                    && previous._sectionEnd >= r._sectionStart )
            {
                throw new IllegalArgumentException(
                        "This paragraph is not the first one in the table" );
            }
        }

        Range overallRange = _doc.getOverallRange();
        int limit = _paragraphs.size();
        for ( ; tableEndInclusive < limit - 1; tableEndInclusive++ )
        {
            Paragraph next = Paragraph.newParagraph( overallRange,
                    _paragraphs.get( tableEndInclusive + 1 ) );
            if ( !next.isInTable() || next.getTableLevel() < tableLevel )
                break;
        }

        initAll();
        if ( tableEndInclusive >= this._parEnd )
        {
            logger.log( POILogger.WARN, "The table's bounds ", "["
                    + this._parStart + "; " + tableEndInclusive + ")",
                    " fall outside of this Range paragraphs numbers ", "["
                            + this._parStart + "; " + this._parEnd + ")" );
        }

        if ( tableEndInclusive < 0 )
        {
            throw new ArrayIndexOutOfBoundsException(
                    "The table's end is negative, which isn't allowed!" );
        }

        int endOffsetExclusive = _paragraphs.get( tableEndInclusive ).getEnd();

        return new Table( paragraph.getStartOffset(), endOffsetExclusive,
                this, paragraph.getTableLevel() );
    }

	/**
	 * loads all of the list indexes.
	 */
	protected void initAll() {
		initCharacterRuns();
		initParagraphs();
		initSections();
	}

	/**
	 * inits the paragraph list indexes.
	 */
	private void initParagraphs() {
		if (!_parRangeFound) {
			int[] point = findRange(_paragraphs, _start, _end);
			_parStart = point[0];
			_parEnd = point[1];
			_parRangeFound = true;
		}
	}

	/**
	 * inits the character run list indexes.
	 */
	private void initCharacterRuns() {
		if (!_charRangeFound) {
			int[] point = findRange(_characters, _start, _end);
			_charStart = point[0];
			_charEnd = point[1];
			_charRangeFound = true;
		}
	}

	/**
	 * inits the section list indexes.
	 */
	private void initSections() {
		if (!_sectionRangeFound) {
			int[] point = findRange(_sections, _sectionStart, _start, _end);
			_sectionStart = point[0];
			_sectionEnd = point[1];
			_sectionRangeFound = true;
		}
	}

    private static int binarySearchStart( List<? extends PropertyNode<?>> rpl,
            int start )
    {
        if ( rpl.get( 0 ).getStart() >= start )
            return 0;

        int low = 0;
        int high = rpl.size() - 1;

        while ( low <= high )
        {
            int mid = ( low + high ) >>> 1;
            PropertyNode<?> node = rpl.get( mid );

            if ( node.getStart() < start )
            {
                low = mid + 1;
            }
            else if ( node.getStart() > start )
            {
                high = mid - 1;
            }
            else
            {
                assert node.getStart() == start;
                return mid;
            }
        }
        assert low != 0;
        return low - 1;
    }

    private static int binarySearchEnd( List<? extends PropertyNode<?>> rpl,
            int foundStart, int end )
    {
        if ( rpl.get( rpl.size() - 1 ).getEnd() <= end )
            return rpl.size() - 1;

        int low = foundStart;
        int high = rpl.size() - 1;

        while ( low <= high )
        {
            int mid = ( low + high ) >>> 1;
            PropertyNode<?> node = rpl.get( mid );

            if ( node.getEnd() < end )
            {
                low = mid + 1;
            }
            else if ( node.getEnd() > end )
            {
                high = mid - 1;
            }
            else
            {
                assert node.getEnd() == end;
                return mid;
            }
        }
        assert 0 <= low && low < rpl.size();

        return low;
    }

    /**
     * Used to find the list indexes of a particular property.
     * 
     * @param rpl
     *            A list of property nodes.
     * @param min
     *            A hint on where to start looking.
     * @param start
     *            The starting character offset.
     * @param end
     *            The ending character offset.
     * @return An int array of length 2. The first int is the start index and
     *         the second int is the end index.
     */
    private int[] findRange( List<? extends PropertyNode<?>> rpl, int start,
            int end )
    {
        int startIndex = binarySearchStart( rpl, start );
        while ( startIndex > 0 && rpl.get( startIndex - 1 ).getStart() >= start )
            startIndex--;

        int endIndex = binarySearchEnd( rpl, startIndex, end );
        while ( endIndex < rpl.size() - 1
                && rpl.get( endIndex + 1 ).getEnd() <= end )
            endIndex--;

        if ( startIndex < 0 || startIndex >= rpl.size()
                || startIndex > endIndex || endIndex < 0
                || endIndex >= rpl.size() )
            throw new AssertionError();

        return new int[] { startIndex, endIndex + 1 };
    }

	/**
	 * Used to find the list indexes of a particular property.
	 *
	 * @param rpl
	 *            A list of property nodes.
	 * @param min
	 *            A hint on where to start looking.
	 * @param start
	 *            The starting character offset.
	 * @param end
	 *            The ending character offset.
	 * @return An int array of length 2. The first int is the start index and
	 *         the second int is the end index.
	 */
	private int[] findRange(List<? extends PropertyNode<?>> rpl, int min, int start, int end) {
		int x = min;
		
        if ( rpl.size() == min )
            return new int[] { min, min };

        PropertyNode<?> node = rpl.get( x );

		while (node==null || (node.getEnd() <= start && x < rpl.size() - 1)) {
			x++;

            if (x>=rpl.size()) {
                return new int[] {0, 0};
            }

			node = rpl.get(x);
		}

        if ( node.getStart() > end )
        {
            return new int[] { 0, 0 };
        }

        if ( node.getEnd() <= start )
        {
            return new int[] { rpl.size(), rpl.size() };
        }

        for ( int y = x; y < rpl.size(); y++ )
        {
            node = rpl.get( y );
            if ( node == null )
                continue;

            if ( node.getStart() < end && node.getEnd() <= end )
                continue;

            if ( node.getStart() < end )
                return new int[] { x, y +1 };

            return new int[] { x, y };
        }
        return new int[] { x, rpl.size() };
    }

	/**
	 * resets the list indexes.
	 */
	protected void reset() {
		_charRangeFound = false;
		_parRangeFound = false;
		_sectionRangeFound = false;
	}

    /**
     * Adjust the value of the various FIB character count fields, eg
     * <code>FIB.CCPText</code> after an insert or a delete...
     * 
     * Works on all CCP fields from this range onwards
     * 
     * @param adjustment
     *            The (signed) value that should be added to the FIB CCP fields
     */
    protected void adjustFIB( int adjustment )
    {
        assert ( _doc instanceof HWPFDocument );

        // update the FIB.CCPText field (this should happen once per adjustment,
        // so we don't want it in
        // adjustForInsert() or it would get updated multiple times if the range
        // has a parent)
        // without this, OpenOffice.org (v. 2.2.x) does not see all the text in
        // the document

        FileInformationBlock fib = _doc.getFileInformationBlock();

        // // Do for each affected part
        // if (_start < cpS.getMainDocumentEnd()) {
        // fib.setCcpText(fib.getCcpText() + adjustment);
        // }
        //
        // if (_start < cpS.getCommentsEnd()) {
        // fib.setCcpAtn(fib.getCcpAtn() + adjustment);
        // }
        // if (_start < cpS.getEndNoteEnd()) {
        // fib.setCcpEdn(fib.getCcpEdn() + adjustment);
        // }
        // if (_start < cpS.getFootnoteEnd()) {
        // fib.setCcpFtn(fib.getCcpFtn() + adjustment);
        // }
        // if (_start < cpS.getHeaderStoryEnd()) {
        // fib.setCcpHdd(fib.getCcpHdd() + adjustment);
        // }
        // if (_start < cpS.getHeaderTextboxEnd()) {
        // fib.setCcpHdrTxtBx(fib.getCcpHdrTxtBx() + adjustment);
        // }
        // if (_start < cpS.getMainTextboxEnd()) {
        // fib.setCcpTxtBx(fib.getCcpTxtBx() + adjustment);
        // }

        // much simple implementation base on SubdocumentType --sergey

        int currentEnd = 0;
        for ( SubdocumentType type : SubdocumentType.ORDERED )
        {
            int currentLength = fib.getSubdocumentTextStreamLength( type );
            currentEnd += currentLength;

            // do we need to shift this part?
            if ( _start > currentEnd )
                continue;

            fib.setSubdocumentTextStreamLength( type, currentLength
                    + adjustment );

            break;
        }
    }

	/**
	 * adjust this range after an insert happens.
	 *
	 * @param length
	 *            the length to adjust for (expected to be a count of
	 *            code-points, not necessarily chars)
	 */
	private void adjustForInsert(int length) {
		_end += length;

		reset();
		Range parent = _parent.get();
		if (parent != null) {
			parent.adjustForInsert(length);
		}
	}

	/**
	 * @return Starting character offset of the range
	 */
	public int getStartOffset() {
		return _start;
	}

	/**
	 * @return The ending character offset of this range
	 */
	public int getEndOffset() {
		return _end;
	}

	protected HWPFDocumentCore getDocument() {
		return _doc;
	}

    @Override
    public String toString()
    {
        return "Range from " + getStartOffset() + " to " + getEndOffset()
                + " (chars)";
    }

    /**
     * Method for debug purposes. Checks that all resolved elements are inside
     * of current range.
     */
    public boolean sanityCheck()
    {
        if ( _start < 0 )
            throw new AssertionError();
        if ( _start > _text.length() )
            throw new AssertionError();
        if ( _end < 0 )
            throw new AssertionError();
        if ( _end > _text.length() )
            throw new AssertionError();
        if ( _start > _end )
            throw new AssertionError();

        if ( _charRangeFound )
        {
            for ( int c = _charStart; c < _charEnd; c++ )
            {
                CHPX chpx = _characters.get( c );

                int left = Math.max( this._start, chpx.getStart() );
                int right = Math.min( this._end, chpx.getEnd() );

                if ( left >= right )
                    throw new AssertionError();
            }
        }
        if ( _parRangeFound )
        {
            for ( int p = _parStart; p < _parEnd; p++ )
            {
                PAPX papx = _paragraphs.get( p );

                int left = Math.max( this._start, papx.getStart() );
                int right = Math.min( this._end, papx.getEnd() );

                if ( left >= right )
                    throw new AssertionError();
            }
        }

        return true;
    }
}
