package org.apache.poi.hslf.record;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class represents a comment on a slide, in the format used by
 *  PPT 2000/XP/etc. (PPT 97 uses plain Escher Text Boxes for comments) 
 * @author Nick Burch
 */
public class Comment2000 extends RecordContainer {
	private byte[] _header;
	private static long _type = 12000;
	
	// Links to our more interesting children
	private CString authorRecord;
	private CString authorInitialsRecord;
	private CString commentRecord;
	private Comment2000Atom commentAtom; 
	
	/** 
	 * Returns the Comment2000Atom of this Comment
	 */ 
	public Comment2000Atom getComment2000Atom() { return commentAtom; }
	
	/**
	 * Get the Author of this comment
	 */
	public String getAuthor() {
		return authorRecord.getText();
	}
	/**
	 * Set the Author of this comment
	 */
	public void setAuthor(String author) {
		authorRecord.setText(author);
	}

	/**
	 * Get the Author's Initials of this comment
	 */
	public String getAuthorInitials() {
		return authorInitialsRecord.getText();
	}
	/**
	 * Set the Author's Initials of this comment
	 */
	public void setAuthorInitials(String initials) {
		authorInitialsRecord.setText(initials);
	}

	/**
	 * Get the text of this comment
	 */
	public String getText() {
		return commentRecord.getText();
	}
	/**
	 * Set the text of this comment
	 */
	public void setText(String text) {
		commentRecord.setText(text);
	}
	
	/** 
	 * Set things up, and find our more interesting children
	 */
	protected Comment2000(byte[] source, int start, int len) {
		// Grab the header
		_header = new byte[8];
		System.arraycopy(source,start,_header,0,8);

		// Find our children
		_children = Record.findChildRecords(source,start+8,len-8);

		// First child should be the author
		if(_children[0] instanceof CString) {
			authorRecord = (CString)_children[0];
		} else {
			throw new IllegalStateException("First child record wasn't a CString, was of type " + _children[0].getRecordType());
		}
		// Second child should be the text
		if(_children[1] instanceof CString) {
			commentRecord = (CString)_children[1];
		} else {
			throw new IllegalStateException("Second child record wasn't a CString, was of type " + _children[1].getRecordType());
		}
		// Third child should be the author's initials
		if(_children[2] instanceof CString) {
			authorInitialsRecord = (CString)_children[2];
		} else {
			throw new IllegalStateException("Third child record wasn't a CString, was of type " + _children[2].getRecordType());
		}
		// Fourth child should be the comment atom
		if(_children[3] instanceof Comment2000Atom) {
			commentAtom = (Comment2000Atom)_children[3];
		} else {
			throw new IllegalStateException("Fourth child record wasn't a Comment2000Atom, was of type " + _children[3].getRecordType());
		}
	}

	/**
	 * We are of type 1200
	 */
	public long getRecordType() { return _type; }

	/**
	 * Write the contents of the record back, so it can be written
	 *  to disk
	 */
	public void writeOut(OutputStream out) throws IOException {
		writeOut(_header[0],_header[1],_type,_children,out);
	}

}
