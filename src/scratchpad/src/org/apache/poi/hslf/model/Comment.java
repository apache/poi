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

package org.apache.poi.hslf.model;

import org.apache.poi.hslf.record.Comment2000;

/**
 *
 * @author Nick Burch
 */
public final class Comment {
	private Comment2000 _comment2000;

	public Comment(Comment2000 comment2000) {
		_comment2000 = comment2000;
	}

	protected Comment2000 getComment2000() {
		return _comment2000;
	}

	/**
	 * Get the Author of this comment
	 */
	public String getAuthor() {
		return _comment2000.getAuthor();
	}
	/**
	 * Set the Author of this comment
	 */
	public void setAuthor(String author) {
		_comment2000.setAuthor(author);
	}

	/**
	 * Get the Author's Initials of this comment
	 */
	public String getAuthorInitials() {
		return _comment2000.getAuthorInitials();
	}
	/**
	 * Set the Author's Initials of this comment
	 */
	public void setAuthorInitials(String initials) {
		_comment2000.setAuthorInitials(initials);
	}

	/**
	 * Get the text of this comment
	 */
	public String getText() {
		return _comment2000.getText();
	}
	/**
	 * Set the text of this comment
	 */
	public void setText(String text) {
		_comment2000.setText(text);
	}
}
