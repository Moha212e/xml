<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="html" encoding="UTF-8" indent="yes"/>

    <xsl:template match="/">
        <html>
            <head>
                <meta charset="UTF-8"/>
                <title>PadChest Expert</title>
            </head>

            <body>
                <h1>PadChest – Mode Expert (BaseX REST)</h1>

                <p><strong>Images loc right :</strong> <span id="locRightCount">Chargement…</span></p>
                <h3>Top 10 labels</h3>
                <div id="top10Labels">Chargement…</div>

                <!-- ====================== -->
                <!--  SCRIPT JAVASCRIPT     -->
                <!-- ====================== -->

                <script><![CDATA[
                const BASEX_URL = "http://localhost:8090/rest";
                const DB = "padchestDB"; // Nom de la base BaseX déjà créée
                const AUTH = "Basic " + btoa("admin:admin");

                // 1) Count loc right via endpoint RESTXQ
                fetch(`${BASEX_URL}/loc-right`, { headers: { "Authorization": AUTH } })
                  .then(r => r.text())
                  .then(xml => {
                      const doc = new DOMParser().parseFromString(xml, "text/xml");
                      document.getElementById("locRightCount").textContent = doc.getElementsByTagName("nb")[0].textContent;
                  })
                  .catch(e => console.error("Erreur loc-right", e));

                // 2) Top 10 labels via endpoint RESTXQ
                fetch(`${BASEX_URL}/top10-labels`, { headers: { "Authorization": AUTH } })
                  .then(r => r.text())
                  .then(xml => {
                      const doc = new DOMParser().parseFromString(xml, "text/xml");
                      const labs = doc.getElementsByTagName("label");
                      let html = "<ol>";
                      for (let i = 0; i < labs.length; i++) {
                          html += `<li><strong>${labs[i].getAttribute("name")}</strong> : ${labs[i].getAttribute("count")} occurrences</li>`;
                      }
                      html += "</ol>";
                      document.getElementById("top10Labels").innerHTML = html;
                  })
                  .catch(e => console.error("Erreur top10-labels", e));
                ]]></script>

            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
