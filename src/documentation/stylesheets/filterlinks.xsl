<?xml version="1.0"?>

<!--
This stylesheet filters all references to the javadocs
and the samples.
-->
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

  <xsl:template match="@src|@href|@background">
    <xsl:if test="not(contains(.,'apidocs')) and not(starts-with(., 'samples/'))">
      <xsl:copy>
        <xsl:apply-templates select="."/>
      </xsl:copy>
    </xsl:if>
  </xsl:template>

  <!-- This is a hack which makes the javascript images work -->
  <xsl:template match="img[@onLoad and starts-with(@src, 'graphics')]">
      <img src="{@src}"/>
      <img>
        <xsl:attribute name="src">
          <xsl:value-of select="substring-before(@src, '.')"/>_over.<xsl:value-of select="substring-after(@src, '.')"/>
        </xsl:attribute>
      </img>
  </xsl:template>

  <xsl:template match="img[@onLoad and starts-with(@src, 'images') and contains(@src, '-lo.gif')]">
      <img src="{@src}"/>
      <img>
        <xsl:attribute name="src"><xsl:value-of select="substring-before(@src, '-lo.gif')"/>-hi.gif</xsl:attribute>
      </img>
  </xsl:template>

  <xsl:template match="@*|node()">
   <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
  </xsl:template>

</xsl:stylesheet>