<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="entries">
    <entries>
      <xsl:apply-templates/>
    </entries>
  </xsl:template>
  <xsl:template match="entry">
    <entry>
        <xsl:attribute name="field">
		<xsl:value-of select="field"/>
        </xsl:attribute>
    </entry>
  </xsl:template>
</xsl:stylesheet>