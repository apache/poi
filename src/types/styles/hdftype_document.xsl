<!-- Copyright (C) 2004 The Apache Software Foundation. All rights reserved. -->
<xsl:stylesheet version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:recutil="org.apache.poi.generator.RecordUtil"
   xmlns:field="org.apache.poi.generator.FieldIterator"
   xmlns:java="java" >

<xsl:template match="type">
<document>
    <header>
        <title><xsl:value-of select="@name"/> Documentation For HDF Type</title>
    </header>

    <body>
        <s1 title="Type Description">
            <p><xsl:value-of select="/type/description"/>
            </p>
        </s1>
        <s1 title="Fields">
            <table>
                <tr>
                    <th colspan="1" rowspan="1">Name</th>
                    <th colspan="1" rowspan="1">Size</th>
                    <th colspan="1" rowspan="1">Offset</th>
                    <th colspan="1" rowspan="1">Description</th>
                    <th colspan="1" rowspan="1">Default Value</th>
                </tr>
                <xsl:apply-templates select="//field"/>
            </table>
        </s1>
    </body>
    <footer>
        <legal>
          Copyright (c) @year@ The Poi Project All rights reserved.
          $Revision$ $Date$
        </legal>
    </footer>

</document>
</xsl:template>

<xsl:template match="field">
    <tr>
        <td><xsl:value-of select="@name"/></td>
        <td><xsl:value-of select="@size"/></td>
        <td> </td>
        <td><xsl:value-of select="@description"/></td>
        <td><xsl:value-of select="@default"/></td>
    </tr>
</xsl:template>

</xsl:stylesheet>
