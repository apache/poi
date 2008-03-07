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
package org.apache.poi.xssf.usermodel.extensions;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTAuthors;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTComments;

public class XSSFSheetComments {
	
	private CTComments comments;

	public XSSFSheetComments() {
		this(CTComments.Factory.newInstance());
	}

	public XSSFSheetComments(CTComments comments) {
		this.comments = comments;
	}

	public String getAuthor(long authorId) {
		return getCommentsAuthors().getAuthorArray((int)authorId);
	}
	
	public int findAuthor(String author) {
		for (int i = 0 ; i < getCommentsAuthors().sizeOfAuthorArray() ; i++) {
			if (getCommentsAuthors().getAuthorArray(i).equals(author)) {
				return i;
			}
		}
		return addNewAuthor(author);
	}

	private CTAuthors getCommentsAuthors() {
		if (comments.getAuthors() == null) {
			comments.addNewAuthors();
		}
		return comments.getAuthors();
	}
	
	private int addNewAuthor(String author) {
		int index = getCommentsAuthors().sizeOfAuthorArray();
		getCommentsAuthors().insertAuthor(index, author);
		return index;
	}

}
