<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:lxslt="http://xml.apache.org/xslt"
    xmlns:my-ext="ext1"
    extension-element-prefixes="my-ext"
>

  <xsl:output method="xml" indent="yes"/>
  <!--The component and its script are in the lxslt namespace and define the 
                  implementation of the extension.-->
                <lxslt:component prefix="my-ext" elements="timelapse" functions="getdate">
                  <lxslt:script lang="javascript">
		    var month = new Array (
		      "January",
		      "February",
		      "March",
		      "April",
		      "May",
		      "June",
		      "July",
		      "August",
		      "September",
		      "October",
		      "November",
		      "December"
		    );

                    function getdate()
                    {
		      var d = new Date();
		      var mo = month[d.getMonth()];
		      var dy = d.getDate();
		      var yr = d.getFullYear();
		      var dateString = dy + " " + mo + " " + yr;
                      return dateString;
                    }
                  </lxslt:script>
                </lxslt:component>

  <xsl:template match="changes">
    <xsl:variable name="version" select="@version"/>
    <xsl:apply-templates select="document(@file,/)/changes/release[attribute::version=string($version)]"/>
  </xsl:template>

  <xsl:template match="announcement">
    <h3><xsl:value-of select="my-ext:getdate()"/> - <xsl:value-of select="title"/><xsl:text> Released</xsl:text></h3>
    <xsl:apply-templates select="abstract"/>

    <xsl:for-each select="project">
      <p><b>About <xsl:value-of select="title"/>:</b>
        <xsl:apply-templates select="."/>
      </p>
    </xsl:for-each>

    <hr noshade="" size="1"/>
  </xsl:template>

  <xsl:template match="project">
    <xsl:apply-templates select="description"/>

    <p>For more information about <xsl:value-of select="title"/>, please go to
    <a><xsl:attribute name="href"><xsl:value-of select="@site"/></xsl:attribute>
    <xsl:value-of select="@site"/></a>.</p>

    <!-- ignore changelog for site -->
  </xsl:template>

  <xsl:template match="abstract">
    <p><xsl:apply-templates/></p>
  </xsl:template>

  <xsl:template match="description">
    <xsl:choose>
      <xsl:when test="para">
        <xsl:apply-templates select="para[position()=1]/node()"/>
	<xsl:apply-templates select="para[position()>1]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="para">
    <p><xsl:apply-templates/></p>
  </xsl:template>

  <xsl:template match="ulink">
    <a href="{@uri}"><xsl:value-of select="."/></a>
  </xsl:template>
</xsl:stylesheet>
