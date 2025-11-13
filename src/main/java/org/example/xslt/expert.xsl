<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" encoding="UTF-8" indent="yes"/>

    <xsl:template match="/">
        <html lang="fr">
            <head>
                <meta charset="UTF-8"/>
                <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                <title>PadChest - Version Expert</title>
                <style>
                    :root{
                    --bg: #f6f7fb;
                    --card: #ffffff;
                    --text: #1f2937;
                    --muted: #6b7280;
                    --brand: #6366f1;      /* indigo */
                    --brand-2: #7c3aed;    /* violet */
                    --accent: #10b981;     /* emerald */
                    --border: #e5e7eb;
                    --shadow: 0 10px 30px rgba(0,0,0,.08);
                    --radius: 14px;
                    }
                    @media (prefers-color-scheme: dark){
                    :root{
                    --bg:#0b0e14; --card:#111827; --text:#e5e7eb; --muted:#9ca3af;
                    --border:#1f2937; --shadow:0 10px 30px rgba(0,0,0,.35);
                    }
                    }

                    /* Reset minimal */
                    *,*::before,*::after{box-sizing:border-box}
                    html,body{height:100%}
                    body{
                    margin:0; font-family: ui-sans-serif,system-ui,Segoe UI,Roboto,Arial;
                    background:
                    radial-gradient(1200px 800px at 10% -10%, rgba(99,102,241,.20), transparent 60%),
                    radial-gradient(1000px 700px at 110% 20%, rgba(124,58,237,.18), transparent 55%),
                    var(--bg);
                    color:var(--text);
                    line-height:1.6;
                    -webkit-font-smoothing:antialiased; -moz-osx-font-smoothing:grayscale;
                    padding:24px;
                    }

                    .container{
                    max-width: 1400px; margin:0 auto; background:var(--card);
                    border-radius: var(--radius); box-shadow: var(--shadow); overflow:hidden;
                    animation: fade .25s ease-out;
                    }
                    @keyframes fade{from{opacity:.0; transform:translateY(6px)} to{opacity:1; transform:none}}

                    /* Header */
                    header{
                    padding: 36px 28px; position:relative; overflow:hidden;
                    background: linear-gradient(135deg, var(--brand), var(--brand-2));
                    color:#fff;
                    }
                    header h1{margin:0 0 6px; font-size: clamp(26px, 2.4vw, 34px); letter-spacing:.2px}
                    header p{margin:0; opacity:.9}

                    /* Stats */
                    .stats{
                    display:grid; gap:16px; padding: 22px 24px; background:var(--card);
                    border-bottom:1px solid var(--border);
                    grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
                    }
                    .stat{
                    background: linear-gradient(180deg, rgba(99,102,241,.08), transparent 60%);
                    border:1px solid var(--border); border-radius:12px; padding:18px 16px;
                    }
                    .stat .label{font-size:.85rem; color:var(--muted); text-transform:uppercase; letter-spacing:.08em}
                    .stat .value{font-size: clamp(20px, 2.2vw, 28px); font-weight:700; margin-top:6px}

                    /* Section tableau */
                    .section{padding: 24px}
                    .section h2{margin:0 0 12px; font-size: clamp(20px, 2vw, 26px); color:var(--brand)}
                    .hint{color:var(--muted); margin-bottom:16px}

                    /* Pagination */
                    .pagination-wrap{display:flex; align-items:center; justify-content:center; gap:8px; margin:18px 0}
                    .btn{
                    appearance:none; border:1px solid var(--border); background:linear-gradient(#fff, #f9fafb);
                    color:var(--text); padding:10px 16px; border-radius:10px; font-weight:600; font-size:.95rem;
                    transition: transform .08s ease, box-shadow .08s ease, background .2s ease, border-color .2s ease;
                    box-shadow: 0 2px 8px rgba(0,0,0,.04);
                    }
                    .btn:hover:not(:disabled){transform: translateY(-1px); box-shadow: 0 6px 16px rgba(0,0,0,.08)}
                    .btn:disabled{opacity:.5; cursor:not-allowed}
                    .btn-primary{
                    background: linear-gradient(135deg, var(--brand), var(--brand-2));
                    color:#fff; border-color: transparent;
                    }

                    .page-info{font-weight:700; color:var(--brand); min-width:160px; text-align:center}

                    /* Table */
                    .table-wrap{
                    border:1px solid var(--border); border-radius:12px; overflow:auto; box-shadow: var(--shadow);
                    background:var(--card);
                    }
                    table{width:100%; border-collapse: collapse; background:transparent}
                    thead{position:sticky; top:0; z-index:5; background: linear-gradient(135deg, var(--brand), var(--brand-2)); color:#fff}
                    th{padding:14px 12px; text-align:left; font-size:.85rem; letter-spacing:.06em; text-transform:uppercase; white-space:nowrap}
                    td{padding:14px 12px; border-bottom:1px solid var(--border); vertical-align:top}
                    tbody tr:hover{background: rgba(99,102,241,.06)}
                    .table-row{display:none} /* anti-flash : caché par défaut */

                    /* Cells */
                    .num{font-weight:700; color:var(--brand)}
                    .mono{font-family: ui-monospace,SFMono-Regular,Consolas,Menlo,monospace; font-size:.9rem; color:var(--text); background:rgba(99,102,241,.06); padding:2px 6px; border-radius:6px}
                    .proj{font-weight:700; color:var(--accent); text-align:center}
                    .ped{text-align:center; font-weight:600}
                    .badge{
                    display:inline-block; padding:6px 10px; margin:3px 4px 0 0; border-radius:999px; font-size:.82rem; font-weight:600;
                    border:1px solid var(--border); background:rgba(16,185,129,.08);
                    }
                    .badge.label{background:rgba(250,204,21,.12)}
                    .muted{color:var(--muted); font-style:italic}

                    footer{
                    border-top:1px solid var(--border); padding:18px 24px; color:var(--muted); text-align:center;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <header>
                        <h1>🏥 PadChest Medical Database — Expert</h1>
                        <p>Base d’images radiographiques thoraciques — interface moderne et responsive</p>
                    </header>

                    <section class="stats" aria-label="Statistiques">
                        <div class="stat">
                            <div class="label">Images totales</div>
                            <div class="value"><xsl:value-of select="count(//image)"/></div>
                        </div>
                        <div class="stat">
                            <div class="label">Images affichées</div>
                            <div class="value"><xsl:value-of select="count(//image)"/></div>
                        </div>
                        <div class="stat">
                            <div class="label">Version</div>
                            <div class="value">Expert UI</div>
                        </div>
                    </section>

                    <section class="section">
                        <h2>📋 Tableau détaillé</h2>
                        <div id="paginationInfo" class="hint"></div>

                        <div class="pagination-wrap">
                            <button id="firstBtn" class="btn">⏮️ Première</button>
                            <button id="prevBtn"  class="btn">◀️ Précédent</button>
                            <div id="pageInfo" class="page-info"></div>
                            <button id="nextBtn"  class="btn btn-primary">Suivant ▶️</button>
                            <button id="lastBtn"  class="btn btn-primary">Dernière ⏭️</button>
                        </div>

                        <div class="table-wrap">
                            <table role="table">
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
                                            <td class="num"><xsl:value-of select="position()"/></td>
                                            <td><span class="mono"><xsl:value-of select="ImageID"/></span></td>
                                            <td><span class="mono"><xsl:value-of select="ImageDir"/></span></td>
                                            <td><span class="mono"><xsl:value-of select="StudyID"/></span></td>
                                            <td><span class="mono"><xsl:value-of select="PatientID"/></span></td>
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
                                            <td class="proj"><xsl:value-of select="Projection"/></td>
                                            <td><xsl:value-of select="MethodProjection"/></td>
                                            <td><xsl:value-of select="MethodLabel"/></td>
                                            <td>
                                                <xsl:choose>
                                                    <xsl:when test="Labels/Label">
                                                        <xsl:for-each select="Labels/Label">
                                                            <span class="badge label"><xsl:value-of select="."/></span>
                                                        </xsl:for-each>
                                                    </xsl:when>
                                                    <xsl:otherwise><span class="muted">Aucun label</span></xsl:otherwise>
                                                </xsl:choose>
                                            </td>
                                            <td>
                                                <xsl:choose>
                                                    <xsl:when test="Localizations/Localization">
                                                        <xsl:for-each select="Localizations/Localization">
                                                            <span class="badge"><xsl:value-of select="."/></span>
                                                        </xsl:for-each>
                                                    </xsl:when>
                                                    <xsl:otherwise><span class="muted">Aucune localisation</span></xsl:otherwise>
                                                </xsl:choose>
                                            </td>
                                            <td><xsl:value-of select="Report"/></td>
                                            <td><xsl:value-of select="ReportID"/></td>
                                        </tr>
                                    </xsl:for-each>
                                </tbody>
                            </table>
                        </div>

                        <div class="pagination-wrap">
                            <button class="btn">⏮️ Première</button>
                            <button class="btn">◀️ Précédent</button>
                            <div id="pageInfo2" class="page-info"></div>
                            <button class="btn btn-primary">Suivant ▶️</button>
                            <button class="btn btn-primary">Dernière ⏭️</button>
                        </div>
                    </section>

                    <footer>
                        © 2025 PadChest — Interface moderne, accessible et rapide
                    </footer>
                </div>

                <script>
                    <![CDATA[
                    const itemsPerPage = 10;
                    let currentPage = 1;
                    let totalItems = 0;
                    let totalPages = 0;
                    const rows = [];

                    function init(){
                    const allRows = document.querySelectorAll('.table-row');
                    rows.length = 0;
                    allRows.forEach(r => rows.push(r));
                    totalItems = rows.length;
                    totalPages = Math.max(1, Math.ceil(totalItems / itemsPerPage));

                    // Brancher boutons (une seule fois)
                    const firstBtn = document.getElementById('firstBtn');
                    const prevBtn  = document.getElementById('prevBtn');
                    const nextBtn  = document.getElementById('nextBtn');
                    const lastBtn  = document.getElementById('lastBtn');

                    if(firstBtn) firstBtn.onclick = () => { if(currentPage!==1) showPage(1) };
                    if(prevBtn)  prevBtn.onclick  = () => { if(currentPage>1) showPage(currentPage-1) };
                    if(nextBtn)  nextBtn.onclick  = () => { if(currentPage<totalPages) showPage(currentPage+1) };
                    if(lastBtn)  lastBtn.onclick  = () => { if(currentPage!==totalPages) showPage(totalPages) };

                    showPage(1);
                    }

                    function showPage(page){
                    currentPage = page;
                    rows.forEach((row, idx) => {
                    const rowPage = Math.ceil((idx + 1) / itemsPerPage);
                    row.style.display = (rowPage === currentPage) ? 'table-row' : 'none';
                    });
                    updateUI();
                    window.scrollTo({top:0, behavior:'smooth'});
                    }

                    function updateUI(){
                    const start = (currentPage - 1) * itemsPerPage + 1;
                    const end   = Math.min(currentPage * itemsPerPage, totalItems);
                    const info  = `📊 Affichage des images ${start}–${end} sur ${totalItems} • Page ${currentPage}/${totalPages}`;

                    const pInfo = document.getElementById('paginationInfo');
                    const p1 = document.getElementById('pageInfo');
                    const p2 = document.getElementById('pageInfo2');
                    if(pInfo) pInfo.textContent = info;
                    if(p1) p1.textContent = `Page ${currentPage} / ${totalPages}`;
                    if(p2) p2.textContent = `Page ${currentPage} / ${totalPages}`;

                    const firstBtn = document.getElementById('firstBtn');
                    const prevBtn  = document.getElementById('prevBtn');
                    const nextBtn  = document.getElementById('nextBtn');
                    const lastBtn  = document.getElementById('lastBtn');

                    if(firstBtn) firstBtn.disabled = currentPage === 1;
                    if(prevBtn)  prevBtn.disabled  = currentPage === 1;
                    if(nextBtn)  nextBtn.disabled  = currentPage === totalPages;
                    if(lastBtn)  lastBtn.disabled  = currentPage === totalPages;
                    }

                    // Ready : lance init tout de suite si possible
                    if(document.readyState === 'loading'){
                    document.addEventListener('DOMContentLoaded', init);
                    } else { init(); }
                    ]]>
                </script>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
