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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class FoDocumentFacade
{
    private static final String NS_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    private static final String NS_XSLFO = "http://www.w3.org/1999/XSL/Format";

    protected final Element declarations;
    protected final Document document;
    protected final Element layoutMasterSet;
    protected Element propertiesRoot;
    protected final Element root;

    public FoDocumentFacade( Document document )
    {
        this.document = document;

        root = document.createElementNS( NS_XSLFO, "fo:root" );
        document.appendChild( root );

        layoutMasterSet = document.createElementNS( NS_XSLFO,
                "fo:layout-master-set" );
        root.appendChild( layoutMasterSet );

        declarations = document.createElementNS( NS_XSLFO, "fo:declarations" );
        root.appendChild( declarations );
    }

    public Element addFlowToPageSequence( final Element pageSequence,
            String flowName )
    {
        final Element flow = document.createElementNS( NS_XSLFO, "fo:flow" );
        flow.setAttribute( "flow-name", flowName );
        pageSequence.appendChild( flow );

        return flow;
    }

    public Element addListItem( Element listBlock )
    {
        Element result = createListItem();
        listBlock.appendChild( result );
        return result;
    }

    public Element addListItemBody( Element listItem )
    {
        Element result = createListItemBody();
        listItem.appendChild( result );
        return result;
    }

    public Element addListItemLabel( Element listItem, String text )
    {
        Element result = createListItemLabel( text );
        listItem.appendChild( result );
        return result;
    }

    public Element addPageSequence( String pageMaster )
    {
        final Element pageSequence = document.createElementNS( NS_XSLFO,
                "fo:page-sequence" );
        pageSequence.setAttribute( "master-reference", pageMaster );
        root.appendChild( pageSequence );
        return pageSequence;
    }

    public Element addRegionBody( Element pageMaster )
    {
        final Element regionBody = document.createElementNS( NS_XSLFO,
                "fo:region-body" );
        pageMaster.appendChild( regionBody );

        return regionBody;
    }

    public Element addSimplePageMaster( String masterName )
    {
        final Element simplePageMaster = document.createElementNS( NS_XSLFO,
                "fo:simple-page-master" );
        simplePageMaster.setAttribute( "master-name", masterName );
        layoutMasterSet.appendChild( simplePageMaster );

        return simplePageMaster;
    }

    protected Element createBasicLinkExternal( String externalDestination )
    {
        final Element basicLink = document.createElementNS( NS_XSLFO,
                "fo:basic-link" );
        basicLink.setAttribute( "external-destination", externalDestination );
        return basicLink;
    }

    public Element createBasicLinkInternal( String internalDestination )
    {
        final Element basicLink = document.createElementNS( NS_XSLFO,
                "fo:basic-link" );
        basicLink.setAttribute( "internal-destination", internalDestination );
        return basicLink;
    }

    public Element createBlock()
    {
        return document.createElementNS( NS_XSLFO, "fo:block" );
    }

    public Element createExternalGraphic( String source )
    {
        Element result = document.createElementNS( NS_XSLFO,
                "fo:external-graphic" );
        result.setAttribute( "src", "url('" + source + "')" );
        return result;
    }

    public Element createInline()
    {
        return document.createElementNS( NS_XSLFO, "fo:inline" );
    }

    public Element createLeader()
    {
        return document.createElementNS( NS_XSLFO, "fo:leader" );
    }

    public Element createListBlock()
    {
        return document.createElementNS( NS_XSLFO, "fo:list-block" );
    }

    public Element createListItem()
    {
        return document.createElementNS( NS_XSLFO, "fo:list-item" );
    }

    public Element createListItemBody()
    {
        return document.createElementNS( NS_XSLFO, "fo:list-item-body" );
    }

    public Element createListItemLabel( String text )
    {
        Element result = document.createElementNS( NS_XSLFO,
                "fo:list-item-label" );
        Element block = createBlock();
        block.appendChild( document.createTextNode( text ) );
        result.appendChild( block );
        return result;
    }

    protected Element createTable()
    {
        return document.createElementNS( NS_XSLFO, "fo:table" );
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

    protected Element getOrCreatePropertiesRoot()
    {
        if ( propertiesRoot != null )
            return propertiesRoot;

        // See http://xmlgraphics.apache.org/fop/0.95/metadata.html

        Element xmpmeta = document.createElementNS( "adobe:ns:meta",
                "x:xmpmeta" );
        declarations.appendChild( xmpmeta );

        Element rdf = document.createElementNS( NS_RDF, "rdf:RDF" );
        xmpmeta.appendChild( rdf );

        propertiesRoot = document.createElementNS( NS_RDF, "rdf:Description" );
        propertiesRoot.setAttributeNS( NS_RDF, "rdf:about", "" );
        rdf.appendChild( propertiesRoot );

        return propertiesRoot;
    }

    public void setCreator( String value )
    {
        setDublinCoreProperty( "creator", value );
    }

    public void setCreatorTool( String value )
    {
        setXmpProperty( "CreatorTool", value );
    }

    public void setDescription( String value )
    {
        Element element = setDublinCoreProperty( "description", value );

        if ( element != null )
        {
            element.setAttributeNS( "http://www.w3.org/XML/1998/namespace",
                    "xml:lang", "x-default" );
        }
    }

    public Element setDublinCoreProperty( String name, String value )
    {
        return setProperty( "http://purl.org/dc/elements/1.1/", "dc", name,
                value );
    }

    public void setKeywords( String value )
    {
        setPdfProperty( "Keywords", value );
    }

    public Element setPdfProperty( String name, String value )
    {
        return setProperty( "http://ns.adobe.com/pdf/1.3/", "pdf", name, value );
    }

    public void setProducer( String value )
    {
        setPdfProperty( "Producer", value );
    }

    protected Element setProperty( String namespace, String prefix,
            String name, String value )
    {
        Element propertiesRoot = getOrCreatePropertiesRoot();
        NodeList existingChildren = propertiesRoot.getChildNodes();
        for ( int i = 0; i < existingChildren.getLength(); i++ )
        {
            Node child = existingChildren.item( i );
            if ( child.getNodeType() == Node.ELEMENT_NODE )
            {
                Element childElement = (Element) child;
                if ( WordToFoUtils.isNotEmpty( childElement.getNamespaceURI() )
                        && WordToFoUtils.isNotEmpty( childElement
                                .getLocalName() )
                        && namespace.equals( childElement.getNamespaceURI() )
                        && name.equals( childElement.getLocalName() ) )
                {
                    propertiesRoot.removeChild( childElement );
                    break;
                }
            }
        }

        if ( WordToFoUtils.isNotEmpty( value ) )
        {
            Element property = document.createElementNS( namespace, prefix
                    + ":" + name );
            property.appendChild( document.createTextNode( value ) );
            propertiesRoot.appendChild( property );
            return property;
        }

        return null;
    }

    public void setSubject( String value )
    {
        setDublinCoreProperty( "title", value );
    }

    public void setTitle( String value )
    {
        setDublinCoreProperty( "title", value );
    }

    public Element setXmpProperty( String name, String value )
    {
        return setProperty( "http://ns.adobe.com/xap/1.0/", "xmp", name, value );
    }

}
