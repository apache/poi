package org.apache.poi.hslf.model;

import org.apache.poi.hslf.record.Comment2000;

public class Comment {
	private Comment2000 comment2000;
	
	public Comment(Comment2000 comment2000) {
		this.comment2000 = comment2000;
	}
	
	protected Comment2000 getComment2000() {
		return comment2000;
	}
	
	/**
	 * Get the Author of this comment
	 */
	public String getAuthor() {
		return comment2000.getAuthor();
	}
	/**
	 * Set the Author of this comment
	 */
	public void setAuthor(String author) {
		comment2000.setAuthor(author);
	}

	/**
	 * Get the Author's Initials of this comment
	 */
	public String getAuthorInitials() {
		return comment2000.getAuthorInitials();
	}
	/**
	 * Set the Author's Initials of this comment
	 */
	public void setAuthorInitials(String initials) {
		comment2000.setAuthorInitials(initials);
	}

	/**
	 * Get the text of this comment
	 */
	public String getText() {
		return comment2000.getText();
	}
	/**
	 * Set the text of this comment
	 */
	public void setText(String text) {
		comment2000.setText(text);
	}
}
