<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output indent="yes"/>
  <xsl:param name="stack">bodyroot</xsl:param>

	<xsl:template match="html|HTML">
     <xsl:text disable-output-escaping="yes">
      	<![CDATA[ <!DOCTYPE document PUBLIC "-//APACHE//DTD Documentation V1.0//EN" "../dtd/document-v10.dtd"> ]]>
     </xsl:text> 	
	 <document>
		  <xsl:apply-templates select = "head" />
		  <xsl:apply-templates select="body"/>
    </document>
 	</xsl:template>	
	
	<xsl:template match="head|HEAD">
	 <header>
      <title><xsl:value-of select="title" /><xsl:value-of select="TITLE" /></title>
      <authors>
      <person id="AO" name="Andrew C. Oliver" email="acoliver2@users.sourceforge.net"/>
      </authors>
     </header>
 	</xsl:template>	
	
	<xsl:template match="body|BODY">
	  <body><s1><xsl:attribute name="title"><xsl:value-of select="'pippo'"></xsl:value-of></xsl:attribute> 
		  <xsl:apply-templates select="*"/>
    </s1>  
    </body>	
	</xsl:template>		
  
	
	<xsl:template match="meta|META"/>

	<xsl:template match="title|TITLE"/>		
	
	<xsl:template match="h1|H1">
	</xsl:template>	
	
	<xsl:template match="h2|H2">
   <s2><xsl:attribute name="title"><xsl:value-of select="."></xsl:value-of></xsl:attribute></s2>
	</xsl:template>	
	
	<xsl:template match="h3|H3">
   <s2><xsl:attribute name="title"><xsl:value-of select="."></xsl:value-of></xsl:attribute></s2>  
	</xsl:template>	
	
	<xsl:template match="h4|H4">
   <s2><xsl:attribute name="title"><xsl:value-of select="."></xsl:value-of></xsl:attribute></s2>  
	</xsl:template>	
		
	<xsl:template match="dl|DL">
	  <!--<dl>
		<xsl:apply-templates select = "dd|DD|dt|DT" />		
	  </dl>-->
	</xsl:template>					
	
	<xsl:template match="dd|DD">
	  <!--<dd>
		<xsl:apply-templates select = "*" />		
	  </dd>	-->	
	</xsl:template>	
	
	<xsl:template match="dt|DT">
	  <!--<dt>
		<xsl:apply-templates select = "*" />		
	  </dt>-->		
	</xsl:template>	
	
	<xsl:template match="p|P">
	  <xsl:choose>
	    <xsl:when test="name(parent::node())='li' or name(parent::node())='LI'">
	      <xsl:value-of select = "*" />	
	    </xsl:when>
	    <xsl:otherwise>
		  <p>
			<xsl:value-of select = "*" />		
		  </p>	
	    </xsl:otherwise>
	  </xsl:choose>
	</xsl:template>	
		
	<xsl:template match="pre|PRE">
	  <xsl:choose>
	    <xsl:when test="name(parent::node())='li' or name(parent::node())='LI'">
      <code>
        <xsl:value-of disable-output-escaping="no" select="." />		
	  </code>	
	    </xsl:when>
	    <xsl:otherwise>
      <source>
        <xsl:value-of disable-output-escaping="no" select="." />		
	  </source>	
	    </xsl:otherwise>
	  </xsl:choose>  
	</xsl:template>	
	
	<xsl:template match="ul|UL">
	  <ul>
		<xsl:apply-templates select = "li|LI" />		
	  </ul>		
	</xsl:template>	
	
	<xsl:template match="li|LI">
	  <li>
		<xsl:apply-templates select = "*" />		
	  </li>		
	</xsl:template>	
	
	<xsl:template match="ol|OL">
	  <ol>
		<xsl:apply-templates select = "li|LI" />		
	  </ol>		
	</xsl:template>								

	<xsl:template match="div|DIV">
		<xsl:apply-templates select = "*" />		
	</xsl:template>	
		
	<xsl:template match="br|BR">
	  <br/>
	</xsl:template>	
	
	<xsl:template match="i|I">
	  <em>
		<xsl:value-of select = "*" />		
	  </em>			
	</xsl:template>	
	
	<xsl:template match="b|B">
	  <strong>
		<xsl:value-of select = "*" />	
	  </strong>			
	</xsl:template>					
		
	<xsl:template match="u|U">
	  <em>
		<xsl:value-of select = "*" />			
	  </em>				
	</xsl:template>	
	
	<xsl:template match="a|A">
	  <link><xsl:attribute name="href"><xsl:value-of select="@href" /><xsl:value-of select="@HREF" /></xsl:attribute>
		<xsl:value-of select = "." />			
	  </link>		
	</xsl:template>				
	
	<xsl:template match="img|IMG">
	  <img><xsl:attribute name="src"><xsl:value-of select="@src" /></xsl:attribute></img>		
	</xsl:template>				
	
		
</xsl:stylesheet>
