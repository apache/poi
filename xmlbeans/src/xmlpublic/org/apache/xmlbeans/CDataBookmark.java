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

/**
 * Represents a CDATA bookmark.
 *
 * <p>When XmlOption UseCDataBookmarks is setted on parse methods,
 * the loader will set these CDataBookmarks in the store on the respective
 * TEXT fields that were represented as CDATA.</p>
 *
 * <p>Users can modify the 'look' of TEXT fields, by annotating them with
 * this bookmark, or remove the bookmark.</p>
 *
 * <p>Additionaly if setted on save methods, the output will look for these
 * bookmarks and will output the text as CDATA.
 * Note: The SaveCDataEntityCountThreshold and SaveCDataLengthThreshold
 * options and their default values still apply.</p>
 *
 * <p>Note: Due to the store representation, a CDATA will not be recognized
 * if it is imediately after non CDATA text and all text following it will
 * be considered CDATA.<br/>
 * Example:<br>
 * <pre>
 * &lt;a>&lt;![CDATA[cdata text]]>&lt;/a>               - is considered as: &lt;a>&lt;![CDATA[cdata text]]>&lt;/a>
 * &lt;b>&lt;![CDATA[cdata text]]> regular text&lt;/b>  - is considered as: &lt;b>&lt;![CDATA[cdata text regular text]]>&lt;/b>
 * &lt;c>text &lt;![CDATA[cdata text]]>&lt;/c>          - is considered as: &lt;c>text cdata text&lt;/c>
 * </pre>
 * </p>
 * @see XmlOptions#setUseCDataBookmarks()
 * @see org.apache.xmlbeans.XmlObject.Factory#parse(String, XmlOptions)
 * @see org.apache.xmlbeans.XmlObject#save(java.io.OutputStream, XmlOptions)
 * @see XmlOptions#setSaveCDataEntityCountThreshold(int) 
 * @see XmlOptions#setSaveCDataLengthThreshold(int)
 * @author Cezar Andrei  (cezar dot andrei at gmail dot com)
 */
public class CDataBookmark
   extends XmlCursor.XmlBookmark
{
    /**
     * The actual bookmark object representing CData.<br>
     * Users must use this bookmark in addition to UseCDataBookmarks
     * option to make use of CDATA representation in XML text.
     */
    public static CDataBookmark CDATA_BOOKMARK = new CDataBookmark();
}