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
package org.apache.poi.hwpf.converter;

import org.apache.poi.util.Beta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

@Beta
public class TextDocumentFacade
{
    protected final Element body;
    protected final Document document;
    protected final Element head;
    protected final Element root;

    protected Element title;
    protected Text titleText;

    public TextDocumentFacade( Document document )
    {
        this.document = document;

        root = document.createElement( "html" );
        document.appendChild( root );

        body = document.createElement( "body" );
        head = document.createElement( "head" );

        root.appendChild( head );
        root.appendChild( body );
        
        title = document.createElement( "title" );
        titleText = document.createTextNode( "" );
        head.appendChild( title );
    }

    public void addAuthor( String value )
    {
        addMeta( "Author", value );
    }

    public void addDescription( String value )
    {
        addMeta( "Description", value );
    }

    public void addKeywords( String value )
    {
        addMeta( "Keywords", value );
    }

    public void addMeta( final String name, String value )
    {
        Element meta = document.createElement( "meta" );

        Element metaName = document.createElement( "name" );
        metaName.appendChild( document.createTextNode( name + ": " ) );
        meta.appendChild( metaName );

        Element metaValue = document.createElement( "value" );
        metaValue.appendChild( document.createTextNode( value + "\n" ) );
        meta.appendChild( metaValue );

        head.appendChild( meta );
    }

    public Element createBlock()
    {
        return document.createElement( "div" );
    }

    public Element createHeader1()
    {
        Element result = document.createElement( "h1" );
        result.appendChild( document.createTextNode( "        " ) );
        return result;
    }

    public Element createHeader2()
    {
        Element result = document.createElement( "h2" );
        result.appendChild( document.createTextNode( "    " ) );
        return result;
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

    public Element getBody()
    {
        return body;
    }

    public Document getDocument()
    {
        return document;
    }

    public Element getHead()
    {
        return head;
    }

    public String getTitle()
    {
        if ( title == null )
            return null;

        return titleText.getTextContent();
    }

    public void setTitle( String titleText )
    {
        if ( WordToHtmlUtils.isEmpty( titleText ) && this.title != null )
        {
            this.head.removeChild( this.title );
            this.title = null;
            this.titleText = null;
        }

        if ( this.title == null )
        {
            this.title = document.createElement( "title" );
            this.titleText = document.createTextNode( titleText );
            this.title.appendChild( this.titleText );
            this.head.appendChild( title );
        }

        this.titleText.setData( titleText );
    }
}
