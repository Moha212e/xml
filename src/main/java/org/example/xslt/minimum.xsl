<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html" encoding="UTF-8" indent="yes"/>

  <xsl:template match="/">
    <html>
      <head>
        <meta charset="UTF-8"/>
        <title>PadChest - Version Minimum</title>
        <style>
          body {
            font-family: Arial, sans-serif;
            padding: 20px;
          }
          .pagination {
            margin: 20px 0;
            text-align: center;
          }
          .pagination button {
            margin: 0 5px;
            padding: 8px 15px;
            cursor: pointer;
          }
          .pagination button:disabled {
            cursor: not-allowed;
            opacity: 0.5;
          }
          .pagination-info {
            margin: 10px 0;
            font-weight: bold;
          }
          .image-item {
            margin-bottom: 30px;
          }
        </style>
      </head>
      <body>
        <h1>PadChest - Base de Données d'Images Radiographiques</h1>
        <p>Version Minimum - Affichage simple en texte</p>

        <h2>Statistiques</h2>
        <p>Nombre total d'images dans la base : <strong><xsl:value-of select="count(//image)"/></strong></p>

        <div class="pagination-info" id="paginationInfo"></div>

        <div class="pagination">
          <button onclick="firstPage()" id="firstBtn">⏮️ Première</button>
          <button onclick="previousPage()" id="prevBtn">◀️ Précédent</button>
          <span id="pageInfo"></span>
          <button onclick="nextPage()" id="nextBtn">Suivant ▶️</button>
          <button onclick="lastPage()" id="lastBtn">Dernière ⏭️</button>
        </div>

        <hr/>

        <h2>Liste des Images</h2>

        <div id="imagesContainer">
          <xsl:for-each select="//image">
            <div class="image-item" data-index="{position()}">
              <h3>Image #<xsl:value-of select="position()"/></h3>

              <p><strong>ID Image :</strong> <xsl:value-of select="ImageID"/></p>
              <p><strong>Répertoire Image :</strong> <xsl:value-of select="ImageDir"/></p>
              <p><strong>ID Étude :</strong> <xsl:value-of select="StudyID"/></p>
              <p><strong>ID Patient :</strong> <xsl:value-of select="PatientID"/></p>
              <p><strong>Date de Naissance :</strong>
                <xsl:choose>
                  <xsl:when test="contains(PatientBirth, '.0')">
                    <xsl:value-of select="substring-before(PatientBirth, '.0')"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="PatientBirth"/>
                  </xsl:otherwise>
                </xsl:choose>
              </p>
              <p><strong>Projection :</strong> <xsl:value-of select="Projection"/></p>
              <p><strong>Méthode Projection :</strong> <xsl:value-of select="MethodProjection"/></p>
              <p><strong>Méthode Label :</strong> <xsl:value-of select="MethodLabel"/></p>

              <p><strong>Labels :</strong>
                <xsl:choose>
                  <xsl:when test="Labels/Label">
                    <xsl:for-each select="Labels/Label">
                      <xsl:value-of select="."/>
                      <xsl:if test="position() != last()">, </xsl:if>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>Aucun label</xsl:otherwise>
                </xsl:choose>
              </p>

              <p><strong>Localisations :</strong>
                <xsl:choose>
                  <xsl:when test="Localizations/Localization">
                    <xsl:for-each select="Localizations/Localization">
                      <xsl:value-of select="."/>
                      <xsl:if test="position() != last()">, </xsl:if>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>Aucune localisation</xsl:otherwise>
                </xsl:choose>
              </p>

              <p><strong>Labels/Localisations par Phrase :</strong>
                <xsl:choose>
                  <xsl:when test="LabelsLocalizationsBySentence/Sentence">
                    <xsl:for-each select="LabelsLocalizationsBySentence/Sentence">
                      <br/><em>Phrase <xsl:value-of select="position()"/> :</em>
                      <xsl:for-each select="Label | Localization">
                        <xsl:value-of select="."/>
                        <xsl:if test="position() != last()">, </xsl:if>
                      </xsl:for-each>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>Aucun</xsl:otherwise>
                </xsl:choose>
              </p>

              <p><strong>Label CUIs :</strong>
                <xsl:choose>
                  <xsl:when test="labelCUIS/labelCUI">
                    <xsl:for-each select="labelCUIS/labelCUI">
                      <xsl:value-of select="."/>
                      <xsl:if test="position() != last()">, </xsl:if>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>Aucun</xsl:otherwise>
                </xsl:choose>
              </p>

              <p><strong>Localisation CUIs :</strong>
                <xsl:choose>
                  <xsl:when test="LocalizationsCUIS/LocalizationsCUI">
                    <xsl:for-each select="LocalizationsCUIS/LocalizationsCUI">
                      <xsl:value-of select="."/>
                      <xsl:if test="position() != last()">, </xsl:if>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>Aucun</xsl:otherwise>
                </xsl:choose>
              </p>

              <p><strong>Rapport :</strong> <xsl:value-of select="Report"/></p>
              <p><strong>ID Rapport :</strong> <xsl:value-of select="ReportID"/></p>

              <hr/>
            </div>
          </xsl:for-each>
        </div>

        <div class="pagination">
          <button onclick="firstPage()">⏮️ Première</button>
          <button onclick="previousPage()">◀️ Précédent</button>
          <span id="pageInfo2"></span>
          <button onclick="nextPage()">Suivant ▶️</button>
          <button onclick="lastPage()">Dernière ⏭️</button>
        </div>

        <footer>
          <p>Fin de la liste - PadChest Version Minimum</p>
        </footer>

        <script>
          const itemsPerPage = 10;
          let currentPage = 1;
          let totalItems = 0;
          let totalPages = 0;
          const items = [];

          function init() {
            const allItems = document.querySelectorAll('.image-item');
            allItems.forEach(item => items.push(item));
            totalItems = items.length;
            totalPages = Math.ceil(totalItems / itemsPerPage);
            showPage(1);
          }

          function showPage(page) {
            currentPage = page;

            items.forEach((item, index) => {
              const itemPage = Math.ceil((index + 1) / itemsPerPage);
              item.style.display = itemPage === currentPage ? 'block' : 'none';
            });

            updatePaginationInfo();
            updateButtons();
          }

          function updatePaginationInfo() {
            const start = (currentPage - 1) * itemsPerPage + 1;
            const end = Math.min(currentPage * itemsPerPage, totalItems);
            const info = `Affichage des images ${start} à ${end} sur ${totalItems} (Page ${currentPage} sur ${totalPages})`;

            document.getElementById('paginationInfo').textContent = info;
            document.getElementById('pageInfo').textContent = `Page ${currentPage} / ${totalPages}`;
            document.getElementById('pageInfo2').textContent = `Page ${currentPage} / ${totalPages}`;
          }

          function updateButtons() {
            document.getElementById('firstBtn').disabled = currentPage === 1;
            document.getElementById('prevBtn').disabled = currentPage === 1;
            document.getElementById('nextBtn').disabled = currentPage === totalPages;
            document.getElementById('lastBtn').disabled = currentPage === totalPages;
          }

          function firstPage() {
            if (currentPage !== 1) {
              showPage(1);
              window.scrollTo(0, 0);
            }
          }

          function previousPage() {
            if (currentPage > 1) {
              showPage(currentPage - 1);
              window.scrollTo(0, 0);
            }
          }

          function nextPage() {
            if (currentPage &lt; totalPages) {
              showPage(currentPage + 1);
              window.scrollTo(0, 0);
            }
          }

          function lastPage() {
            if (currentPage !== totalPages) {
              showPage(totalPages);
              window.scrollTo(0, 0);
            }
          }

          // Initialiser au chargement de la page
          window.addEventListener('DOMContentLoaded', init);
        </script>
      </body>
    </html>
  </xsl:template>

</xsl:stylesheet>

