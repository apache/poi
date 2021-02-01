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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.util.Beta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

@Beta
public class HtmlDocumentFacade
{
    protected final Element body;
    protected final Document document;
    protected final Element head;
    protected final Element html;

    /**
     * Map from tag name, to map linking known styles and css class names
     */
    private Map<String, Map<String, String>> stylesheet = new LinkedHashMap<>();
    private Element stylesheetElement;

    protected Element title;
    protected Text titleText;

    public HtmlDocumentFacade( Document document )
    {
        this.document = document;

        html = document.createElement( "html" );
        document.appendChild( html );

        body = document.createElement( "body" );
        head = document.createElement( "head" );
        stylesheetElement = document.createElement( "style" );
        stylesheetElement.setAttribute( "type", "text/css" );

        html.appendChild( head );
        html.appendChild( body );
        head.appendChild( stylesheetElement );

        addStyleClass( body, "b", "white-space-collapsing:preserve;" );
    }

    public void addAuthor( String value )
    {
        addMeta( "author", value );
    }

    public void addDescription( String value )
    {
        addMeta( "description", value );
    }

    public void addKeywords( String value )
    {
        addMeta( "keywords", value );
    }

    public void addMeta( final String name, String value )
    {
        Element meta = document.createElement( "meta" );
        meta.setAttribute( "name", name );
        meta.setAttribute( "content", value );
        head.appendChild( meta );
    }

    public void addStyleClass( Element element, String classNamePrefix,
            String style )
    {
        String exising = element.getAttribute( "class" );
        String addition = getOrCreateCssClass( classNamePrefix, style );
        String newClassValue = AbstractWordUtils.isEmpty( exising ) ? addition : ( exising + " " + addition );
        element.setAttribute( "class", newClassValue );
    }

    protected String buildStylesheet(
            final Map<String, Map<String, String>> prefixToMapOfStyles )
    {
        StringBuilder stringBuilder = new StringBuilder();
        for ( Map<String, String> byPrefix : prefixToMapOfStyles.values() )
        {
            for ( Map.Entry<String, String> byStyle : byPrefix.entrySet() )
            {
                String style = byStyle.getKey();
                String className = byStyle.getValue();

                stringBuilder.append( "." );
                stringBuilder.append( className );
                stringBuilder.append( "{" );
                stringBuilder.append( style );
                stringBuilder.append( "}\n" );
            }
        }
        return stringBuilder.toString();
    }

    public Element createBlock()
    {
        return document.createElement( "div" );
    }

    public Element createBookmark( String name )
    {
        final Element basicLink = document.createElement( "a" );
        basicLink.setAttribute( "name", name );
        return basicLink;
    }

    public Element createHeader1()
    {
        return document.createElement( "h1" );
    }

    public Element createHeader2()
    {
        return document.createElement( "h2" );
    }

    public Element createHyperlink( String internalDestination )
    {
        final Element basicLink = document.createElement( "a" );
        basicLink.setAttribute( "href", internalDestination );
        return basicLink;
    }

    public Element createImage( String src )
    {
        Element result = document.createElement( "img" );
        result.setAttribute( "src", src );
        return result;
    }

    public Element createLineBreak()
    {
        return document.createElement( "br" );
    }

    public Element createListItem()
    {
        return document.createElement( "li" );
    }

    public Element createOption( String value, boolean selected )
    {
        Element result = document.createElement( "option" );
        result.appendChild( createText( value ) );
        if ( selected )
        {
            result.setAttribute( "selected", "selected" );
        }
        return result;
    }

    public Element createParagraph()
    {
        return document.createElement( "p" );
    }

    public Element createSelect()
    {
        return document.createElement( "select" );
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

    public Element createTableColumn()
    {
        return document.createElement( "col" );
    }

    public Element createTableColumnGroup()
    {
        return document.createElement( "colgroup" );
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

    public String getOrCreateCssClass( String classNamePrefix, String style )
    {
        if ( !stylesheet.containsKey( classNamePrefix ) )
            stylesheet.put( classNamePrefix, new LinkedHashMap<>(
                    1) );

        Map<String, String> styleToClassName = stylesheet.get( classNamePrefix );
        String knownClass = styleToClassName.get( style );
        if ( knownClass != null )
            return knownClass;

        String newClassName = classNamePrefix + ( styleToClassName.size() + 1 );
        styleToClassName.put( style, newClassName );
        return newClassName;
    }

    public String getTitle()
    {
        if ( title == null )
            return null;

        return titleText.getTextContent();
    }

    public void setTitle( String titleText )
    {
        if ( AbstractWordUtils.isEmpty( titleText ) && this.title != null )
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

    public void updateStylesheet()
    {
        stylesheetElement.setTextContent( buildStylesheet( stylesheet ) );
    }
}
