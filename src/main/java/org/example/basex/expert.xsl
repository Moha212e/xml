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

                // 1) Count loc right via requête XQuery directe
                const queryLocRight = encodeURIComponent(`
                  <nb-loc-right>{
                    count(
                      distinct-values(
                        db:open('${DB}')/Images/image[
                          Localizations/Localization[. = 'loc right']
                          or LabelsLocalizationsBySentence/Sentence/Localization[. = 'loc right']
                        ]/@Identifiant
                      )
                    )
                  }</nb-loc-right>
                `);

                fetch(`${BASEX_URL}?query=${queryLocRight}`, {
                    headers: { "Authorization": AUTH }
                })
                  .then(r => r.text())
                  .then(xml => {
                      const doc = new DOMParser().parseFromString(xml, "text/xml");
                      const nb = doc.querySelector("nb-loc-right");
                      if (nb) {
                          document.getElementById("locRightCount").textContent = nb.textContent;
                      } else {
                          document.getElementById("locRightCount").textContent = "Erreur";
                      }
                  })
                  .catch(e => {
                      console.error("Erreur loc-right", e);
                      document.getElementById("locRightCount").textContent = "Erreur de connexion";
                  });

                // 2) Top 10 labels via requête XQuery directe
                const queryTop10 = encodeURIComponent(`
                  let $labels := db:open('${DB}')/Images/image/(Labels/Label | LabelsLocalizationsBySentence/Sentence/Label)
                  let $normalized :=
                    for $l in $labels
                    let $norm := lower-case(normalize-space($l))
                    where $norm ne '' and $norm ne 'none'
                    return $norm
                  let $grouped :=
                    for $val in distinct-values($normalized)
                    let $count := count($normalized[. = $val])
                    order by $count descending, $val
                    return <label name="{$val}" count="{$count}"/>
                  return <labels-top10>{ subsequence($grouped, 1, 10) }</labels-top10>
                `);

                fetch(`${BASEX_URL}?query=${queryTop10}`, {
                    headers: { "Authorization": AUTH }
                })
                  .then(r => r.text())
                  .then(xml => {
                      const doc = new DOMParser().parseFromString(xml, "text/xml");
                      const labs = doc.querySelectorAll("label");
                      if (labs.length > 0) {
                          let html = "<ol>";
                          labs.forEach(lab => {
                              html += `<li><strong>${lab.getAttribute("name")}</strong> : ${lab.getAttribute("count")} occurrences</li>`;
                          });
                          html += "</ol>";
                          document.getElementById("top10Labels").innerHTML = html;
                      } else {
                          document.getElementById("top10Labels").innerHTML = "Aucun résultat";
                      }
                  })
                  .catch(e => {
                      console.error("Erreur top10-labels", e);
                      document.getElementById("top10Labels").innerHTML = "Erreur de connexion";
                  });
                ]]></script>

            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
