package org.apache.xmlbeans.impl.xpathgen;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlCursor.TokenType;

import javax.xml.namespace.QName;
import javax.xml.namespace.NamespaceContext;

/**
 * Generates an XPath String that points to a given position in an XML document
 */
public class XPathGenerator
{
    /**
     * Generates an XPath pointing to the position in the document indicated by <code>node</code>.
     * <p>If the <code>context</code> parameter is null, the XPath is absolute, otherwise the
     * XPath will be relative to the position indicated by <code>context</code>.</p>
     * <p>Note: the cursor position for the <code>node</code> parameter is not preserved</p>
     * @param node the position in the document that the generated path will point to
     * @param context the context node; the generated path will be relative to it if not null and if
     * pointing to an element on the path from the document root to <code>node</code>
     * @param nsctx a namespace context that will be used to obtain prefixes; a (non-default)
     * namespace mapping must be available for all required namespace URIs
     * @return the generated path as a <code>String</code>
     * @throws XPathGenerationException if the path could not be generated: the cursor is in a bad
     * position (like over a comment) or no prefix mapping was found for one of the namespace URIs
     */
    public static String generateXPath(XmlCursor node, XmlCursor context, NamespaceContext nsctx)
        throws XPathGenerationException
    {
        if (node == null)
            throw new IllegalArgumentException("Null node");
        if (nsctx == null)
            throw new IllegalArgumentException("Null namespace context");
        TokenType tt = node.currentTokenType();
        if (context != null && node.isAtSamePositionAs(context))
            return ".";
        switch (tt.intValue())
        {
            case TokenType.INT_ATTR:
                QName name = node.getName();
                node.toParent();
                String pathToParent = generateInternal(node, context, nsctx);
                return pathToParent + '/' + '@' + qnameToString(name, nsctx);
            case TokenType.INT_NAMESPACE:
                name = node.getName();
                node.toParent();
                pathToParent = generateInternal(node, context, nsctx);
                String prefix = name.getLocalPart();
                if (prefix.length() == 0)
                    return pathToParent + "/@xmlns";
                else
                    return pathToParent + "/@xmlns:" + prefix;
            case TokenType.INT_START:
            case TokenType.INT_STARTDOC:
                return generateInternal(node, context, nsctx);
            case TokenType.INT_TEXT:
                int nrOfTextTokens = countTextTokens(node);
                node.toParent();
                pathToParent = generateInternal(node, context, nsctx);
                if (nrOfTextTokens == 0)
                    return pathToParent + "/text()";
                else
                    return pathToParent + "/text()[position()=" + nrOfTextTokens + ']';
            default:
                throw new XPathGenerationException("Cannot generate XPath for cursor position: " +
                    tt.toString());
        }
    }

    private static String generateInternal(XmlCursor node, XmlCursor context, NamespaceContext nsctx)
        throws XPathGenerationException
    {
        if (node.isStartdoc())
            return "";
        if (context != null && node.isAtSamePositionAs(context))
            return ".";
        assert node.isStart();
        QName name = node.getName();
        XmlCursor d = node.newCursor();
        if (!node.toParent())
            return "/" + name;
        int elemIndex = 0, i = 1;
        node.push();
        if (!node.toChild(name))
            throw new IllegalStateException("Must have at least one child with name: " + name);
        do
        {
            if (node.isAtSamePositionAs(d))
                elemIndex = i;
            else
                i++;
        } while (node.toNextSibling(name));
        node.pop();
        d.dispose();
        String pathToParent = generateInternal(node, context, nsctx);
        return  i == 1 ? pathToParent + '/' + qnameToString(name, nsctx) :
            pathToParent + '/' + qnameToString(name, nsctx) + '[' + elemIndex + ']';
    }

    private static String qnameToString(QName qname, NamespaceContext ctx)
        throws XPathGenerationException
    {
        String localName = qname.getLocalPart();
        String uri = qname.getNamespaceURI();
        if (uri.length() == 0)
            return localName;
        String prefix = qname.getPrefix();
        if (prefix != null && prefix.length() > 0)
        {
            // Try to use the same prefix if it maps to the right URI
            String mappedUri = ctx.getNamespaceURI(prefix);
            if (uri.equals(mappedUri))
                return prefix + ':' + localName;
        }
        // The prefix is not specified, or it is not mapped to the right URI
        prefix = ctx.getPrefix(uri);
        if (prefix == null)
            throw new XPathGenerationException("Could not obtain a prefix for URI: " + uri);
        if (prefix.length() == 0)
            throw new XPathGenerationException("Can not use default prefix in XPath for URI: " + uri);
        return prefix + ':' + localName;
    }

    /**
     * Computes how many text nodes the 
     * @param c the position in the document
     * @return how many text nodes occur before the position determined by <code>c</code>
     */
    private static int countTextTokens(XmlCursor c)
    {
        int k = 0;
        int l = 0;
        XmlCursor d = c.newCursor();
        c.push();
        c.toParent();
        TokenType tt = c.toFirstContentToken();
        while (!tt.isEnd())
        {
            if (tt.isText())
            {
                if (c.comparePosition(d) > 0)
                    // We have moved after the initial position
                    l++;
                else
                    k++;
            }
            else if (tt.isStart())
                c.toEndToken();
            tt = c.toNextToken();
        }
        c.pop();
        return l == 0 ? 0 : k;
    }

    public static void main(String[] args) throws org.apache.xmlbeans.XmlException
    {
        String xml =
            "<root>\n" +
                "<ns:a xmlns:ns=\"http://a.com\"><b foo=\"value\">text1<c/>text2<c/>text3<c>text</c>text4</b></ns:a>\n" +
                "</root>";
        NamespaceContext ns = new NamespaceContext() {
            public String getNamespaceURI(String prefix)
            {
                if ("ns".equals(prefix))
                    return "http://a.com";
                else
                    return null;
            }
            public String getPrefix(String namespaceUri)
            {
                return null;
            }
            public java.util.Iterator getPrefixes(String namespaceUri)
            {
                return null;
            }
        };
        XmlCursor c = org.apache.xmlbeans.XmlObject.Factory.parse(xml).newCursor();
        c.toFirstContentToken(); // on <root>
        c.toFirstContentToken(); // on <a>
        c.toFirstChild();        // on <b>
        c.toFirstChild();        // on <c>
        c.push(); System.out.println(generateXPath(c, null, ns)); c.pop();
        c.toNextSibling();
        c.toNextSibling();       // on the last <c>
        c.push(); System.out.println(generateXPath(c, null, ns)); c.pop();
        XmlCursor d = c.newCursor();
        d.toParent();
        c.push(); System.out.println(generateXPath(c, d, ns)); c.pop();
        d.toParent();
        c.push(); System.out.println(generateXPath(c, d, ns)); c.pop();
        c.toFirstContentToken(); // on text content of the last <c>
        c.push(); System.out.println(generateXPath(c, d, ns)); c.pop();
        c.toParent();
        c.toPrevToken();         // on text content before the last <c>
        c.push(); System.out.println(generateXPath(c, d, ns)); c.pop();
        c.toParent();            // on <b>
        c.push(); System.out.println(generateXPath(c, d, ns)); c.pop();
        c.toFirstAttribute();    // on the "foo" attribute
        c.push(); System.out.println(generateXPath(c, d, ns)); c.pop();
        c.toParent();
        c.toParent();
        c.toNextToken();         // on the "xmlns:ns" attribute
        c.push(); System.out.println(generateXPath(c, d, ns)); c.pop();
        c.push(); System.out.println(generateXPath(c, null, ns)); c.pop();
    }
}
