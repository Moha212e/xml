<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" encoding="UTF-8" indent="yes"/>

    <xsl:template match="/">
        <html>
            <head>
                <meta charset="UTF-8"/>
                <title>PadChest - Version Pro</title>
                <style>
                    body { font-family: Arial, sans-serif; padding: 20px; margin: 0; }
                    table { width: 100%; border-collapse: collapse; margin: 20px 0; }
                    th { background-color: #4CAF50; color: white; padding: 12px; text-align: left; }
                    td { border: 1px solid #ddd; padding: 10px; }
                    tr:nth-child(even) { background-color: #f2f2f2; }
                    tr:hover { background-color: #ddd; }

                    /* IMPORTANT : cacher les lignes par défaut
                    (elles seront affichées par JS pour la page courante) */
                    .table-row { display: none; }

                    .pagination { margin: 20px 0; text-align: center; }
                    .pagination button {
                    margin: 0 5px; padding: 10px 20px; cursor: pointer;
                    background-color: #4CAF50; color: white; border: none; border-radius: 5px; font-size: 14px;
                    }
                    .pagination button:hover:not(:disabled) { background-color: #45a049; }
                    .pagination button:disabled { cursor: not-allowed; opacity: 0.5; background-color: #ccc; }
                    .pagination-info { margin: 15px 0; font-weight: bold; font-size: 16px; }
                    h1, h2 { color: #333; }
                </style>
            </head>
            <body>
                <h1>PadChest - Base de Données d'Images Radiographiques</h1>
                <p>Version Pro - Affichage en tableau HTML</p>

                <h2>Statistiques</h2>
                <p>Nombre total d'images dans la base : <strong><xsl:value-of select="count(//image)"/></strong></p>

                <div class="pagination-info" id="paginationInfo"></div>

                <div class="pagination">
                    <button onclick="firstPage()" id="firstBtn">⏮️ Première</button>
                    <button onclick="previousPage()" id="prevBtn">◀️ Précédent</button>
                    <span id="pageInfo" style="margin: 0 15px; font-weight: bold;"></span>
                    <button onclick="nextPage()" id="nextBtn">Suivant ▶️</button>
                    <button onclick="lastPage()" id="lastBtn">Dernière ⏭️</button>
                </div>

                <hr/>

                <h2>Tableau des Images</h2>

                <table>
                    <thead>
                        <tr>
                            <th>N°</th>
                            <th>ID Image</th>
                            <th>Répertoire</th>
                            <th>ID Étude</th>
                            <th>ID Patient</th>
                            <th>Naissance</th>
                            <th>Projection</th>
                            <th>Méth. Proj.</th>
                            <th>Méth. Label</th>
                            <th>Labels</th>
                            <th>Localisations</th>
                            <th>Rapport</th>
                            <th>ID Rapport</th>
                        </tr>
                    </thead>
                    <tbody id="tableBody">
                        <xsl:for-each select="//image">
                            <tr class="table-row" data-index="{position()}">
                                <td><xsl:value-of select="position()"/></td>
                                <td><xsl:value-of select="ImageID"/></td>
                                <td><xsl:value-of select="ImageDir"/></td>
                                <td><xsl:value-of select="StudyID"/></td>
                                <td><xsl:value-of select="PatientID"/></td>
                                <td>
                                    <xsl:choose>
                                        <xsl:when test="contains(PatientBirth, '.0')">
                                            <xsl:value-of select="substring-before(PatientBirth, '.0')"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="PatientBirth"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </td>
                                <td><xsl:value-of select="Projection"/></td>
                                <td><xsl:value-of select="MethodProjection"/></td>
                                <td><xsl:value-of select="MethodLabel"/></td>
                                <td>
                                    <xsl:choose>
                                        <xsl:when test="Labels/Label">
                                            <xsl:for-each select="Labels/Label">
                                                <xsl:value-of select="."/>
                                                <xsl:if test="position() != last()"> | </xsl:if>
                                            </xsl:for-each>
                                        </xsl:when>
                                        <xsl:otherwise>-</xsl:otherwise>
                                    </xsl:choose>
                                </td>
                                <td>
                                    <xsl:choose>
                                        <xsl:when test="Localizations/Localization">
                                            <xsl:for-each select="Localizations/Localization">
                                                <xsl:value-of select="."/>
                                                <xsl:if test="position() != last()"> | </xsl:if>
                                            </xsl:for-each>
                                        </xsl:when>
                                        <xsl:otherwise>-</xsl:otherwise>
                                    </xsl:choose>
                                </td>
                                <td><xsl:value-of select="Report"/></td>
                                <td><xsl:value-of select="ReportID"/></td>
                            </tr>
                        </xsl:for-each>
                    </tbody>
                </table>

                <div class="pagination">
                    <button onclick="firstPage()">⏮️ Première</button>
                    <button onclick="previousPage()">◀️ Précédent</button>
                    <span id="pageInfo2" style="margin: 0 15px; font-weight: bold;"></span>
                    <button onclick="nextPage()">Suivant ▶️</button>
                    <button onclick="lastPage()">Dernière ⏭️</button>
                </div>

                <footer>
                    <p>Fin du tableau - PadChest Version Pro</p>
                </footer>

                <script>
                    const itemsPerPage = 10;
                    let currentPage = 1;
                    let totalItems = 0;
                    let totalPages = 0;
                    const rows = [];

                    function init() {
                    const allRows = document.querySelectorAll('.table-row');
                    rows.length = 0;
                    allRows.forEach(row => rows.push(row));
                    totalItems = rows.length;
                    totalPages = Math.max(1, Math.ceil(totalItems / itemsPerPage));
                    showPage(1);
                    }

                    function showPage(page) {
                    currentPage = page;

                    rows.forEach((row, index) => {
                    const rowPage = Math.ceil((index + 1) / itemsPerPage);
                    row.style.display = (rowPage === currentPage) ? 'table-row' : 'none';
                    });

                    updatePaginationInfo();
                    updateButtons();
                    }

                    function updatePaginationInfo() {
                    const start = (currentPage - 1) * itemsPerPage + 1;
                    const end = Math.min(currentPage * itemsPerPage, totalItems);
                    const info = `Affichage des images ${start} à ${end} sur ${totalItems} (Page ${currentPage} sur ${totalPages})`;

                    const p1 = document.getElementById('paginationInfo');
                    const i1 = document.getElementById('pageInfo');
                    const i2 = document.getElementById('pageInfo2');

                    if (p1) p1.textContent = info;
                    if (i1) i1.textContent = `Page ${currentPage} / ${totalPages}`;
                    if (i2) i2.textContent = `Page ${currentPage} / ${totalPages}`;
                    }

                    function updateButtons() {
                    const firstBtn = document.getElementById('firstBtn');
                    const prevBtn = document.getElementById('prevBtn');
                    const nextBtn = document.getElementById('nextBtn');
                    const lastBtn = document.getElementById('lastBtn');

                    if (firstBtn) firstBtn.disabled = currentPage === 1;
                    if (prevBtn)  prevBtn.disabled  = currentPage === 1;
                    if (nextBtn)  nextBtn.disabled  = currentPage === totalPages;
                    if (lastBtn)  lastBtn.disabled  = currentPage === totalPages;
                    }

                    function firstPage()  { if (currentPage !== 1) { showPage(1); window.scrollTo(0, 0); } }
                    function previousPage(){ if (currentPage > 1)   { showPage(currentPage - 1); window.scrollTo(0, 0); } }
                    function nextPage()    { if (currentPage &lt; totalPages) { showPage(currentPage + 1); window.scrollTo(0, 0); } }
                    function lastPage()    { if (currentPage !== totalPages) { showPage(totalPages); window.scrollTo(0, 0); } }

                    // Lance init immédiatement si le DOM est déjà prêt, sinon écoute l'évènement
                    if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', init);
                    } else {
                    init();
                    }
                </script>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
