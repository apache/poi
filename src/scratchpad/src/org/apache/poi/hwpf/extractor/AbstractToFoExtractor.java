/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.hwpf.extractor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

public abstract class AbstractToFoExtractor
{

    private static final String NS_XSLFO = "http://www.w3.org/1999/XSL/Format";

    protected final Document document;
    protected final Element layoutMasterSet;
    protected final Element root;

    public AbstractToFoExtractor( Document document )
    {
        this.document = document;

        root = document.createElementNS( NS_XSLFO, "fo:root" );
        document.appendChild( root );

        layoutMasterSet = document.createElementNS( NS_XSLFO,
                "fo:layout-master-set" );
        root.appendChild( layoutMasterSet );
    }

    protected Element addFlowToPageSequence( final Element pageSequence,
            String flowName )
    {
        final Element flow = document.createElementNS( NS_XSLFO, "fo:flow" );
        flow.setAttribute( "flow-name", flowName );
        pageSequence.appendChild( flow );

        return flow;
    }

    protected Element addListItem( Element listBlock )
    {
        Element result = createListItem();
        listBlock.appendChild( result );
        return result;
    }

    protected Element addListItemBody( Element listItem )
    {
        Element result = createListItemBody();
        listItem.appendChild( result );
        return result;
    }

    protected Element addListItemLabel( Element listItem, String text )
    {
        Element result = createListItemLabel( text );
        listItem.appendChild( result );
        return result;
    }

    protected Element addPageSequence( String pageMaster )
    {
        final Element pageSequence = document.createElementNS( NS_XSLFO,
                "fo:page-sequence" );
        pageSequence.setAttribute( "master-reference", pageMaster );
        root.appendChild( pageSequence );
        return pageSequence;
    }

    protected Element addRegionBody( Element pageMaster )
    {
        final Element regionBody = document.createElementNS( NS_XSLFO,
                "fo:region-body" );
        pageMaster.appendChild( regionBody );

        return regionBody;
    }

    protected Element addSimplePageMaster( String masterName )
    {
        final Element simplePageMaster = document.createElementNS( NS_XSLFO,
                "fo:simple-page-master" );
        simplePageMaster.setAttribute( "master-name", masterName );
        layoutMasterSet.appendChild( simplePageMaster );

        return simplePageMaster;
    }

    protected Element addTable( Element flow )
    {
        final Element table = document.createElementNS( NS_XSLFO, "fo:table" );
        flow.appendChild( table );
        return table;
    }

    protected Element createBasicLinkExternal( String externalDestination )
    {
        final Element basicLink = document.createElementNS( NS_XSLFO,
                "fo:basic-link" );
        basicLink.setAttribute( "external-destination", externalDestination );
        return basicLink;
    }

    protected Element createBasicLinkInternal( String internalDestination )
    {
        final Element basicLink = document.createElementNS( NS_XSLFO,
                "fo:basic-link" );
        basicLink.setAttribute( "internal-destination", internalDestination );
        return basicLink;
    }

    protected Element createBlock()
    {
        return document.createElementNS( NS_XSLFO, "fo:block" );
    }

    protected Element createExternalGraphic( String source )
    {
        Element result = document.createElementNS( NS_XSLFO,
                "fo:external-graphic" );
        result.setAttribute( "src", "url('" + source + "')" );
        return result;
    }

    protected Element createInline()
    {
        return document.createElementNS( NS_XSLFO, "fo:inline" );
    }

    protected Element createLeader()
    {
        return document.createElementNS( NS_XSLFO, "fo:leader" );
    }

    protected Element createListBlock()
    {
        return document.createElementNS( NS_XSLFO, "fo:list-block" );
    }

    protected Element createListItem()
    {
        return document.createElementNS( NS_XSLFO, "fo:list-item" );
    }

    protected Element createListItemBody()
    {
        return document.createElementNS( NS_XSLFO, "fo:list-item-body" );
    }

    protected Element createListItemLabel( String text )
    {
        Element result = document.createElementNS( NS_XSLFO,
                "fo:list-item-label" );
        Element block = createBlock();
        block.appendChild( document.createTextNode( text ) );
        result.appendChild( block );
        return result;
    }

    protected Element createTableBody()
    {
        return document.createElementNS( NS_XSLFO, "fo:table-body" );
    }

    protected Element createTableCell()
    {
        return document.createElementNS( NS_XSLFO, "fo:table-cell" );
    }

    protected Element createTableHeader()
    {
        return document.createElementNS( NS_XSLFO, "fo:table-header" );
    }

    protected Element createTableRow()
    {
        return document.createElementNS( NS_XSLFO, "fo:table-row" );
    }

    protected Text createText( String data )
    {
        return document.createTextNode( data );
    }

    public Document getDocument()
    {
        return document;
    }

}
