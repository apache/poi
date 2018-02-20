/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans;

import org.apache.xmlbeans.XmlCursor.XmlBookmark;

/**
 * A subclass of XmlBookmark that holds line number information.
 * If a document is parsed with line numbers
 * enabled, these bookmarks will be placed at appropriate locations
 * within the document.
 * 
 * @see XmlOptions#setLoadLineNumbers 
 */
public class XmlLineNumber extends XmlBookmark
{
    /**
     * Constructs a line number with no column or offset information.
     * @param line the line number - the first line is 1
     */ 
    public XmlLineNumber ( int line ) { this( line, -1, -1 ); }
    
    /**
     * Constructs a line number and column with no file offset information.
     * @param line the line number - the first line is 1
     * @param line the column number - the first column is 1
     */
    public XmlLineNumber ( int line, int column ) { this( line, column, -1 ); }
    
    /**
     * Constructs a line number and column with no file offset information.
     * @param line the line number - the first line is 1
     * @param line the column number - the first column is 1
     * @param line the file character offset - the first character in the file is 0
     */
    public XmlLineNumber ( int line, int column, int offset )
    {
        super( false );
        
        _line = line;
        _column = column;
        _offset = offset;
    }
    
    /**
     * Returns the 1-based line number, or -1 if not known.
     */ 
    public int getLine   ( ) { return _line;   }
    
    /**
     * Returns the 1-based column number, or -1 if not known.
     */ 
    public int getColumn ( ) { return _column; }
    
    /**
     * Returns the 0-based file offset number, or -1 if not known.
     */ 
    public int getOffset ( ) { return _offset; }

    private int _line, _column, _offset;
}
