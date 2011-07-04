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
package org.apache.poi.hwpf.extractor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public class HtmlDocumentFacade
{

    protected final Element body;
    protected final Document document;
    protected final Element head;
    protected final Element html;

    public HtmlDocumentFacade( Document document )
    {
        this.document = document;

        html = document.createElement( "html" );
        document.appendChild( html );

        body = document.createElement( "body" );
        head = document.createElement( "head" );

        html.appendChild( head );
        html.appendChild( body );
    }

    public Element createHyperlink( String internalDestination )
    {
        final Element basicLink = document.createElement( "a" );
        basicLink.setAttribute( "href", internalDestination );
        return basicLink;
    }

    public Element createListItem()
    {
        return document.createElement( "li" );
    }

    public Element createParagraph()
    {
        return document.createElement( "p" );
    }

    public Element createTable()
    {
        return document.createElement( "table" );
    }

    public Element createTableBody()
    {
        return document.createElement( "tbody" );
    }

    public Element createTableCell()
    {
        return document.createElement( "td" );
    }

    public Element createTableHeader()
    {
        return document.createElement( "thead" );
    }

    public Element createTableHeaderCell()
    {
        return document.createElement( "th" );
    }

    public Element createTableRow()
    {
        return document.createElement( "tr" );
    }

    public Text createText( String data )
    {
        return document.createTextNode( data );
    }

    public Element createUnorderedList()
    {
        return document.createElement( "ul" );
    }

    public Document getDocument()
    {
        return document;
    }

}
