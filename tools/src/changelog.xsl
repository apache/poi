<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:output indent="yes"/>

    <xsl:param name="module">module</xsl:param>
    <xsl:param name="cvsweb">cvsweb</xsl:param>
                
    <xsl:template match="/">

        <html>
            <head>
                <link rel="stylesheet" type="text/css" href="../../../../html/javadoc.css" title="Style"></link>
            </head>
            <body bgcolor="white">
                <h1>Change Log</h1>

                    <xsl:apply-templates select="changelog/entry">
			<xsl:sort order="descending" select="date" />
		    </xsl:apply-templates>
    
            </body>
        </html>
        
    </xsl:template>
  
    <xsl:template match="entry">
        
        <h2>
            <xsl:call-template name="escape-return">
              <xsl:with-param name="string"><xsl:value-of select="msg"/></xsl:with-param>
            </xsl:call-template>
        </h2>
        <p>
            <b>
                <xsl:apply-templates select="date"/>
                by <xsl:value-of disable-output-escaping="yes" select="author"/>
            </b>
        </p>
        <p>
            <xsl:apply-templates select="file"/>
        </p>
        <hr/>
            
    </xsl:template>

    <xsl:template match="date">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="weekday">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="time">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="file">
        <br/><a>
	<xsl:choose>
	    <xsl:when test="string-length(prevrevision) = 0 ">
                <xsl:attribute name="href"><xsl:value-of select="$cvsweb"/><xsl:value-of select="$module" />/<xsl:value-of select="name" />?rev=<xsl:value-of select="revision" />&amp;content-type=text/x-cvsweb-markup</xsl:attribute>
	    </xsl:when>
	    <xsl:otherwise>
                <xsl:attribute name="href"><xsl:value-of select="$cvsweb"/><xsl:value-of select="$module" />/<xsl:value-of select="name" />?r1=<xsl:value-of select="revision" />&amp;r2=<xsl:value-of select="prevrevision"/>&amp;diff_format=h</xsl:attribute>
	    </xsl:otherwise>
	</xsl:choose>
        <xsl:value-of select="name" />
        </a>
    </xsl:template>

  <xsl:template name="escape-return">
    <xsl:param name="string"/>
      <!-- must be a better way to define a carrige return -->
      <xsl:variable name="return"><xsl:text>
</xsl:text>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="contains($string, $return)">
          <xsl:value-of select="substring-before($string, $return)"/><br/>
          <xsl:call-template name="escape-return">
            <xsl:with-param name="string">
              <xsl:value-of select="substring-after($string, $return)"/>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$string"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

</xsl:stylesheet>

