<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:template match="document">
    <article>
      <articleinfo>
        <authorgroup>
          <xsl:for-each select="header/person">
            <author>
              <firstname><xsl:value-of select="@name"/></firstname>
              <address><email><xsl:value-of select="@email"/></email></address>
            </author>
          </xsl:for-each>
        </authorgroup>
      </articleinfo>
      <title><xsl:value-of select="header/title"/></title>
      <xsl:apply-templates select="body"/>
    </article>
  </xsl:template>

  <xsl:template match="changes">
    <revhistory>
      <xsl:apply-templates select="//action"/>
    </revhistory>
  </xsl:template>

  <xsl:template match="action">
    <revision>
      <xsl:attribute name="revisionflag">
        <xsl:choose>
          <xsl:when test="@type='add'">added</xsl:when>
          <xsl:when test="@type='update'">changed</xsl:when>
          <xsl:when test="@type='remove'">deleted</xsl:when>
          <xsl:when test="@type='fix'">off</xsl:when>
          <xsl:otherwise>changed</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <revnumber><xsl:value-of select="../@version"/></revnumber>
      <date><xsl:value-of select="../@date"/></date>
      <authorinitials><xsl:value-of select="@dev"/></authorinitials>
      <revremark>
        <xsl:value-of select="."/>
      </revremark>
    </revision>
  </xsl:template>

  <xsl:template match="body">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="s1">
    <section>
      <title><xsl:value-of select="@title"/></title>
      <xsl:apply-templates/>
    </section>
  </xsl:template>

  <xsl:template match="s2">
    <section>
      <title><xsl:value-of select="@title"/></title>
      <xsl:apply-templates/>
    </section>
  </xsl:template>

  <xsl:template match="s3">
    <section>
      <title><xsl:value-of select="@title"/></title>
      <xsl:apply-templates/>
    </section>
  </xsl:template>

  <xsl:template match="s4">
    <section>
      <title><xsl:value-of select="@title"/></title>
      <xsl:apply-templates/>
    </section>
  </xsl:template>

  <xsl:template match="p|br">
    <para>
      <xsl:apply-templates/>
    </para>
  </xsl:template>

  <xsl:template match="strong|em">
    <emphasis><xsl:apply-templates/></emphasis>
  </xsl:template>

  <xsl:template match="ul">
    <itemizedlist><xsl:apply-templates/></itemizedlist>
  </xsl:template>

  <xsl:template match="li">
    <listitem><xsl:apply-templates/></listitem>
  </xsl:template>

  <xsl:template match="ol">
    <orderedlist><xsl:apply-templates/></orderedlist>
  </xsl:template>

  <xsl:template match="link">
    <ulink uri="{@href}"><xsl:apply-templates/></ulink>
  </xsl:template>

  <xsl:template match="figure">
    <xsl:choose>
      <xsl:when test="@src">
        <figure>
          <title><xsl:value-of select="@alt"/></title>
          <graphic fileref="{@src}" srccredit="{@alt}"/>
        </figure>
      </xsl:when>
      <xsl:otherwise>
        <figure>
          <xsl:apply-templates/>
        </figure>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="fixme">
    <warning><xsl:apply-templates/></warning>
  </xsl:template>

  <xsl:template match="note">
    <note><xsl:apply-templates/></note>
  </xsl:template>

  <xsl:template match="warn">
    <warning><xsl:apply-templates/></warning>
  </xsl:template>

  <xsl:template match="code">
    <classname><xsl:apply-templates/></classname>
  </xsl:template>

  <xsl:template match="source">
    <programlisting><xsl:apply-templates/></programlisting>
  </xsl:template>

  <xsl:template match="table">
    <table>
      <tgroup>
        <xsl:attribute name="cols"><xsl:value-of select="count(tr/td)"/></xsl:attribute>
        <xsl:if test="th">
          <thead>
            <xsl:apply-templates select="th"/>
          </thead>
        </xsl:if>
        <tbody>
          <xsl:apply-templates select="tr"/>
        </tbody>
      </tgroup>
    </table>
  </xsl:template>

  <xsl:template match="th|tr">
    <row>
      <xsl:apply-templates/>
    </row>
  </xsl:template>

  <xsl:template match="td">
    <entry>
      <xsl:apply-templates/>
    </entry>
  </xsl:template>

  <xsl:template match="node()|@*" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>

