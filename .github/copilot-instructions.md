# Copilot instructions — PadChest XML project (concise)

Purpose
- Convert the PadChest CSV to a validated XML, run Java parsers to compute simple stats (loc-right count, top‑10 labels), produce HTML via XSLT and support XQuery/BaseX usage.

Big picture (what to know first)
- Primary components (all under `src/main/java/org/example`):
  - conversion: `CsvToXmlConverter.java` — CSV → XML (handles nested lists and quoted fields).
  - structures: `images.dtd` (DTD) and `images.xsd` (optional XSD) describing the XML model.
  - validation: `validation/SAXValidator.java`, `DOMValidator.java` — parsing, DTD/XSD validation, stats.
  - xslt: `xslt/*.xsl` and `html/*.java` — XSLT-based HTML generators (minimum/pro/expert variants).
  - data: `data/` holds the canonical CSV and an example XML.

Key repo conventions
- Source data and XML/DTD/XSLT live inside the Java package tree (e.g. `src/main/java/org/example/data/`), not `resources/`. Code expects relative paths like `src/main/java/org/example/data/PADCHEST_...csv`.
- XML element naming is mixed-case (e.g. `<ImageID>` inside `<image>`). Many handlers compare qName lowercased — be careful when adding new element names.
- CSV parsing is custom: brackets (`[...]`) and nested lists are preserved and mapped to nested XML (see `separerElementsTableau` and `ecrireTableau` in `CsvToXmlConverter`). Follow that pattern when extending.

Important integration details
- Generated XML includes:
  - an XML-stylesheet PI referencing `../xslt/images.xsl` and a DOCTYPE referencing `../structures/images.dtd`. Relative layout matters for transform/validation runs.
- Validators support both DTD (SAXParserFactory.setValidating(true)) and XSD (SchemaFactory + setSchema). See `SAXValidator.main` for example usage.

Build & common run commands (PowerShell)
- Build classes with Maven, then run the main classes directly from `target/classes`:

```powershell
mvn -DskipTests package
java -cp target/classes org.example.conversion.CsvToXmlConverter src/main/java/org/example/data/PADCHEST_chest_x_ray_images_labels_160K_01.02.19.csv
java -cp target/classes org.example.validation.SAXValidator src/main/java/org/example/data/PADCHEST_chest_x_ray_images_labels_160K_01.02.19.xml src/main/java/org/example/structures/images.xsd
java -cp target/classes org.example.html.XmlToHtmlMinimum
```

Notes & gotchas
- `pom.xml` currently sets `<maven.compiler.source>`/`target` to `25`. Ensure your JDK matches or lower these properties before compiling locally.
- Paths in the code are relative and point at files inside `src/main/java/...` (not `src/main/resources`). If you move files, update references in Java mains and XML headers.
- The CSV→XML converter writes `Images` root and `image` elements with attribute `Identifiant` — other code and XSLT expect that structure.

Where to look for examples
- CSV parsing & nested-array handling: `src/main/java/org/example/conversion/CsvToXmlConverter.java` (functions: `separerLigne`, `separerElementsTableau`, `ecrireTableau`).
- SAX-based validation + stats: `src/main/java/org/example/validation/SAXValidator.java` (shows how loc‑right is detected and top‑10 labels computed).
- XSLT with client-side pagination: `src/main/java/org/example/xslt/minimum.xsl` and HTML generator `XmlToHtmlMinimum.java`.

What the AI should do (succinctly)
- Prefer small, local changes. When adding features, mirror current patterns: relative paths, manual CSV parsing, and simple array-to-XML mapping.
- Preserve existing element names and DTD shape; update `images.dtd` and `images.xsd` in lock-step if changing model.
- When proposing CLI/run changes, include exact PowerShell commands and mention JDK version implications.

If anything is unclear, tell me which file or workflow you want expanded (build, CSV format, XML model or XQuery examples) and I’ll update this file.

-- end
# GitHub Copilot – Instructions du dépôt (Projet XML / PadChest)

## 🎯 Objectif du projet
Transformer un CSV PadChest en **XML** validé (DTD/XSD), faire un **parsing en Java** (SAX ou DOM) avec statistiques, générer une page **HTML via XSLT**, puis **intégrer et interroger** le XML dans **BaseX (XQuery)**. Le tout respecte strictement les exigences pédagogiques. (Réf. énoncé “XML et notions avancées de bases de données – 3ᵉ Bac”)

## 🧱 Contexte & Données
- Source: fichier CSV `PADCHEST_chest_x_ray_images_labels_160K_01.02.19.csv` (36 champs par image).
- Certains champs sont des **listes** (entre guillemets) et `LabelsLocalizationsBySentence` est un **tableau de tableaux**.
- Utiliser **uniquement le sous-ensemble de champs exigé par l’énoncé** (si la liste n’est pas fournie dans le dépôt, propose un bloc `TODO` pour la fixer au début du projet).
- Le séparateur est la **virgule**; gérer les guillemets, échappements et valeurs multiples.

## ✅ Livrables attendus (avec chemins suggérés)
1) **Conversion CSV → XML** (langage: SQL/PLSQL, Java, C ou C++; privilégier Java ici)
    - `tools/csv_to_xml/` : utilitaire ou script qui produit `data/padchest.xml`
    - Exiger au moins **un attribut** dans un des éléments XML (contrainte pédagogique).
2) **Structure du XML**
    - Minimum: `schema/padchest.dtd`
    - Pro (optionnel): `schema/padchest.xsd` (écrit **à la main**, pas généré auto)
3) **Validation + Parsing Java**
    - `app/parser/` (Java 17+):
        - **Mode SAX (min)** et **Mode DOM (pro)**
        - Vérifier la validité (DTD et si présent XSD)
        - Calculs obligatoires:
            - **Compter** les images contenant la localisation `'loc right'`
            - **Top 10 labels** les plus fréquents avec leur **compte**
        - **Experts**: comparer temps d’exécution et mémoire (SAX vs DOM, DTD vs XSD)
4) **XSLT → HTML**
    - `web/xslt/view.xsl` génère `web/public/index.html` (table lisible au minimum; version “pro” plus jolie/structurée)
5) **BaseX + XQuery**
    - `basex/create-db.xq` : création/chargement de la BD à partir de `data/padchest.xml`
    - `basex/queries/loc-right.xq` : compte des images avec `'loc right'`
    - `basex/queries/top10-labels.xq` : classement des 10 labels les plus fréquents
    - **Pro**: exposer via **webservice**; **Experts**: appeler ce webservice depuis la page HTML générée par XSLT.

## 🔒 Garde-fous pédagogiques (do/don’t)
- **DO** : écrire **à la main** DTD/XSD; documenter les choix de modélisation.
- **DO** : gestion des champs listes et tableau de tableaux; tests unitaires sur ces cas.
- **DON’T** : ne pas utiliser d’API “boîte noire” qui cache la logique de parsing/génération. Préférer une **gestion des chaînes** ou des parseurs standards (SAX/DOM).
- **DON’T** : ne pas générer automatiquement DTD/XSD depuis des outils.

## 🗂️ Modèle XML (guide)
- Racine: `<padchest>`
- Enfant: `<image id="...">`
    - Exemples de sous-éléments (adapter selon la liste officielle du cours):
        - `<patient id="..."><birth>...</birth><sex>...</sex></patient>`
        - `<study><date>...</date><studyId>...</studyId></study>`
        - `<acquisition><view>...</view><projection>...</projection>...</acquisition>`
        - `<labels>` : liste de `<label>` texte + éventuellement `<localizations>` avec `<loc>` multiples
        - Conserver `LabelsLocalizationsBySentence` comme structure imbriquée (p.ex. `<sentences><sentence>...</sentence></sentences>`)

> **Règle**: Au moins **un attribut** (p.ex. `image @id`, `patient @id`). Cohérence types/valeurs.

## 🧪 Tâches Copilot (prompts prêts à l’emploi)
- « Génère un parseur **SAX** en Java qui lit `data/padchest.xml`, valide via **DTD**, et calcule: (1) total d’images avec `'loc right'`, (2) **Top 10 labels** avec leurs occurrences. Structure le code en `app/parser/sax/` avec tests JUnit. »
- « Ajoute la variante **DOM** et un petit **benchmark** (temps + mémoire) sur un échantillon: 1 000, 10 000, 50 000 lignes. Produis un tableau Markdown de résultats. »
- « Crée `schema/padchest.dtd` à la main selon le modèle XML défini, en gérant: listes de labels, localisations multiples, et tableau de tableaux pour `LabelsLocalizationsBySentence`. »
- « Écris `web/xslt/view.xsl` qui génère un HTML simple (table responsive) listant les images, avec colonnes ID, PatientID, View, Labels (séparés par virgules). »
- « Écris `basex/queries/top10-labels.xq` : retourne une séquence triée décroissante (label, count) et limite à 10. »
- « Prépare un **Makefile** ou scripts (`scripts/`) pour: (1) convertir CSV→XML, (2) valider, (3) lancer stats SAX/DOM, (4) générer HTML, (5) exécuter XQuery. »

## 🧰 Pile & contraintes
- **Java** pour le parser (SAX min., DOM pro).
- **XSLT 1.0+** pour la transformation.
- **BaseX** pour XQuery (scripts `.xq`).
- **CSV → XML** : privilégier code contrôlé (pas de générateurs opaques).
- **HTML/CSS/JS** : minimal fonctionnel; bonus si ergonomique (sans frameworks imposés).

## 📏 Qualité & style
- Code Java: clair, testable, packages `app.parser.sax`, `app.parser.dom`, etc.
- Logs mesurés; pas de dépendances inutiles.
- Tests unitaires ciblant:
    - champs liste multi-valeurs,
    - `LabelsLocalizationsBySentence` (tableau de tableaux),
    - performance SAX vs DOM.
- Documentation: `README.md` avec **mode d’emploi** et **captures**.

## 🧪 Validation (checklist automatique)
- [ ] `schema/padchest.dtd` présent et référencé dans `padchest.xml` (doctype)
- [ ] Parser **SAX** passe et imprime les deux métriques (loc-right, top10)
- [ ] Variante **DOM** + bench dispo
- [ ] `web/xslt/view.xsl` produit un HTML non vide et lisible
- [ ] BaseX: création BD + 2 requêtes XQuery OK
- [ ] (Optionnel) Webservice + intégration front

## 🔍 Consignes d’implémentation importantes
- Parsing CSV: gérer guillemets, valeurs multiples, virgules internes; écrire tests.
- XML: pas d’IDs dupliqués; encoding UTF-8; éviter espaces inutiles.
- XSLT: séparer présentation/logiciel; aucune dépendance build exotique.
- XQuery: résultats triés et limités correctement; documenter la collection/chemin.

## 📣 Style de réponse attendu de Copilot
- Proposer *d’abord* la structure de fichiers, *ensuite* implémenter.
- Toujours expliquer **pourquoi SAX vs DOM** et l’impact mémoire/temps.
- Quand l’énoncé impose une règle, **la rappeler dans la réponse**.
- Donner des **prompts de test** pour vérifier chaque livrable.
- Si une info manque (p.ex. liste exacte des champs à retenir), proposer un **TODO clair** et une PR pour l’ajouter.

## 🧭 Exemples de snippets utiles (à adapter)
- **XQuery top-10** (esquisse d’intention): group-by label, count, order desc, head 10.
- **XSLT**: itérer sur `<image>`; joindre listes avec séparateur `, `.
- **Java SAX**: `DefaultHandler` + compteurs; **DOM**: parser en mémoire + streams pour comptage.

## 🔚 Évaluation (rappel)
Pondération: Conversion XML (5), DTD/XSD (4), Parsing + stats (6), XSLT (5), XQuery (5). Bench et webservice = bonus “pro/experts”.
