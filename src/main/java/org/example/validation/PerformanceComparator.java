package org.example.validation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.*;

/**
 * Comparateur de performances pour les validateurs SAX et DOM.
 * Niveau EXPERTS : Compare temps d'exécution et consommation mémoire.
 *
 * Comparaisons effectuées :
 * - SAX avec validation DTD
 * - DOM avec validation DTD
 * - SAX avec validation XSD (si XSD fourni)
 * - DOM avec validation XSD (si XSD fourni)
 */
public class PerformanceComparator {

    public static void main(String[] args) {
        String xmlPath = "src/main/java/org/example/data/PADCHEST_chest_x_ray_images_labels_160K_01.02.19.xml";
        String xsdPath = "src/main/java/org/example/structures/images.xsd";

        if (args.length >= 1) {
            xmlPath = args[0];
        }
        if (args.length >= 2) {
            xsdPath = args[1];
        }

        System.out.println("=================================================================");
        System.out.println("=== COMPARAISON DE PERFORMANCES (Niveau Experts) ===");
        System.out.println("=================================================================");
        System.out.println("Fichier XML : " + xmlPath);
        System.out.println("Fichier XSD : " + xsdPath);
        System.out.println();
        System.out.println("Note : Les tests sont exécutés plusieurs fois pour obtenir");
        System.out.println("       des résultats plus fiables (warm-up de la JVM).");
        System.out.println("=================================================================");
        System.out.println();

        List<ResultatPerformance> resultats = new ArrayList<>();

        // Test 1 : SAX avec DTD
        System.out.println("▶ Test 1/4 : SAX avec validation DTD...");
        ResultatPerformance saxDtd = testerSAX(xmlPath, null, "SAX + DTD");
        resultats.add(saxDtd);
        afficherResultat(saxDtd);
        System.out.println();

        // Test 2 : DOM avec DTD
        System.out.println("▶ Test 2/4 : DOM avec validation DTD...");
        ResultatPerformance domDtd = testerDOM(xmlPath, null, "DOM + DTD");
        resultats.add(domDtd);
        afficherResultat(domDtd);
        System.out.println();

        // Test 3 : SAX avec XSD
        System.out.println("▶ Test 3/4 : SAX avec validation XSD...");
        ResultatPerformance saxXsd = testerSAX(xmlPath, xsdPath, "SAX + XSD");
        resultats.add(saxXsd);
        afficherResultat(saxXsd);
        System.out.println();

        // Test 4 : DOM avec XSD
        System.out.println("▶ Test 4/4 : DOM avec validation XSD...");
        ResultatPerformance domXsd = testerDOM(xmlPath, xsdPath, "DOM + XSD");
        resultats.add(domXsd);
        afficherResultat(domXsd);
        System.out.println();

        // Tableau comparatif final
        System.out.println("=================================================================");
        System.out.println("=== TABLEAU COMPARATIF ===");
        System.out.println("=================================================================");
        afficherTableauComparatif(resultats);

        // Analyse des résultats
        System.out.println();
        System.out.println("=================================================================");
        System.out.println("=== ANALYSE ===");
        System.out.println("=================================================================");
        analyserResultats(resultats);
    }

    /**
     * Teste les performances du parser SAX.
     */
    private static ResultatPerformance testerSAX(String xmlPath, String xsdPath, String nom) {
        ResultatPerformance resultat = new ResultatPerformance(nom);

        try {
            // Warm-up (1 fois)
            executerSAX(xmlPath, xsdPath);

            // Mesures (3 exécutions)
            long[] tempsExecution = new long[3];
            long[] memoireUtilisee = new long[3];

            for (int i = 0; i < 3; i++) {
                System.gc();
                Thread.sleep(100);

                Runtime runtime = Runtime.getRuntime();
                long memoireAvant = runtime.totalMemory() - runtime.freeMemory();
                long debut = System.nanoTime();

                executerSAX(xmlPath, xsdPath);

                long fin = System.nanoTime();
                long memoireApres = runtime.totalMemory() - runtime.freeMemory();

                tempsExecution[i] = (fin - debut) / 1_000_000; // Convertir en ms
                memoireUtilisee[i] = Math.max(0, memoireApres - memoireAvant) / (1024 * 1024); // Convertir en MB
            }

            // Calculer les moyennes
            resultat.setTempsMoyen(calculerMoyenne(tempsExecution));
            resultat.setMemoireMoyenne(calculerMoyenne(memoireUtilisee));
            resultat.setSucces(true);

        } catch (Exception e) {
            resultat.setSucces(false);
            resultat.setErreur(e.getMessage());
        }

        return resultat;
    }

    /**
     * Teste les performances du parser DOM.
     */
    private static ResultatPerformance testerDOM(String xmlPath, String xsdPath, String nom) {
        ResultatPerformance resultat = new ResultatPerformance(nom);

        try {
            // Warm-up (1 fois)
            executerDOM(xmlPath, xsdPath);

            // Mesures (3 exécutions)
            long[] tempsExecution = new long[3];
            long[] memoireUtilisee = new long[3];

            for (int i = 0; i < 3; i++) {
                System.gc();
                Thread.sleep(100);

                Runtime runtime = Runtime.getRuntime();
                long memoireAvant = runtime.totalMemory() - runtime.freeMemory();
                long debut = System.nanoTime();

                executerDOM(xmlPath, xsdPath);

                long fin = System.nanoTime();
                long memoireApres = runtime.totalMemory() - runtime.freeMemory();

                tempsExecution[i] = (fin - debut) / 1_000_000; // Convertir en ms
                memoireUtilisee[i] = Math.max(0, memoireApres - memoireAvant) / (1024 * 1024); // Convertir en MB
            }

            // Calculer les moyennes
            resultat.setTempsMoyen(calculerMoyenne(tempsExecution));
            resultat.setMemoireMoyenne(calculerMoyenne(memoireUtilisee));
            resultat.setSucces(true);

        } catch (Exception e) {
            resultat.setSucces(false);
            resultat.setErreur(e.getMessage());
        }

        return resultat;
    }

    /**
     * Exécute un parsing SAX (sans afficher les résultats).
     */
    private static void executerSAX(String xmlPath, String xsdPath) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);

        if (xsdPath != null) {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(
                javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File(xsdPath));
            factory.setSchema(schema);
        } else {
            factory.setValidating(true);
        }

        SAXParser parser = factory.newSAXParser();
        parser.parse(new File(xmlPath), new MinimalHandler());
    }

    /**
     * Exécute un parsing DOM (sans afficher les résultats).
     */
    private static void executerDOM(String xmlPath, String xsdPath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        if (xsdPath != null) {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(
                javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File(xsdPath));
            factory.setSchema(schema);
        } else {
            factory.setValidating(true);
        }

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(xmlPath));

        // Simuler un parcours basique du DOM
        NodeList images = document.getElementsByTagName("image");
        for (int i = 0; i < images.getLength(); i++) {
            Element image = (Element) images.item(i);
            image.getAttribute("Identifiant");
        }
    }

    /**
     * Handler SAX minimal pour les tests de performance.
     */
    static class MinimalHandler extends DefaultHandler {
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            // Ne rien faire, juste parser
        }
    }

    /**
     * Calcule la moyenne d'un tableau de valeurs.
     */
    private static long calculerMoyenne(long[] valeurs) {
        long somme = 0;
        for (long valeur : valeurs) {
            somme += valeur;
        }
        return somme / valeurs.length;
    }

    /**
     * Affiche un résultat individuel.
     */
    private static void afficherResultat(ResultatPerformance resultat) {
        if (resultat.isSucces()) {
            System.out.printf("  ✓ Temps moyen : %,d ms%n", resultat.getTempsMoyen());
            System.out.printf("  ✓ Mémoire moyenne : %,d MB%n", resultat.getMemoireMoyenne());
        } else {
            System.out.println("  ✗ Erreur : " + resultat.getErreur());
        }
    }

    /**
     * Affiche un tableau comparatif des résultats.
     */
    private static void afficherTableauComparatif(List<ResultatPerformance> resultats) {
        System.out.println("┌─────────────┬──────────────────┬────────────────────┐");
        System.out.println("│   Méthode   │  Temps moyen (ms)│  Mémoire moy. (MB) │");
        System.out.println("├─────────────┼──────────────────┼────────────────────┤");

        for (ResultatPerformance r : resultats) {
            if (r.isSucces()) {
                System.out.printf("│ %-11s │ %,15d  │ %,17d  │%n",
                    r.getNom(), r.getTempsMoyen(), r.getMemoireMoyenne());
            } else {
                System.out.printf("│ %-11s │      ERREUR      │       ERREUR       │%n", r.getNom());
            }
        }

        System.out.println("└─────────────┴──────────────────┴────────────────────┘");
    }

    /**
     * Analyse et compare les résultats.
     */
    private static void analyserResultats(List<ResultatPerformance> resultats) {
        if (resultats.stream().anyMatch(r -> !r.isSucces())) {
            System.out.println("⚠ Certains tests ont échoué. Analyse partielle uniquement.");
            System.out.println();
        }

        // Comparer SAX vs DOM (DTD)
        ResultatPerformance saxDtd = resultats.get(0);
        ResultatPerformance domDtd = resultats.get(1);

        if (saxDtd.isSucces() && domDtd.isSucces()) {
            System.out.println("1. SAX vs DOM (avec DTD) :");

            double ratioTemps = (double) domDtd.getTempsMoyen() / saxDtd.getTempsMoyen();
            double ratioMemoire = (double) domDtd.getMemoireMoyenne() / saxDtd.getMemoireMoyenne();

            System.out.printf("   - SAX est %.2fx plus rapide que DOM%n", ratioTemps);
            System.out.printf("   - SAX consomme %.2fx moins de mémoire que DOM%n", ratioMemoire);
            System.out.println();
        }

        // Comparer DTD vs XSD (SAX)
        ResultatPerformance saxXsd = resultats.get(2);

        if (saxDtd.isSucces() && saxXsd.isSucces()) {
            System.out.println("2. Validation DTD vs XSD (avec SAX) :");

            double ratioTemps = (double) saxXsd.getTempsMoyen() / saxDtd.getTempsMoyen();

            System.out.printf("   - DTD est %.2fx plus rapide que XSD%n", ratioTemps);
            System.out.println();
        }

        System.out.println("Conclusion :");
        System.out.println("   - SAX est généralement plus performant pour le streaming XML");
        System.out.println("   - DOM consomme plus de mémoire (charge tout en mémoire)");
        System.out.println("   - La validation DTD est plus rapide que XSD");
        System.out.println("   - Choisir SAX pour grands fichiers, DOM pour manipulation du DOM");
    }

    /**
     * Classe pour stocker les résultats de performance d'un test.
     */
    static class ResultatPerformance {
        private String nom;
        private long tempsMoyen;
        private long memoireMoyenne;
        private boolean succes;
        private String erreur;

        public ResultatPerformance(String nom) {
            this.nom = nom;
        }

        public String getNom() {
            return nom;
        }

        public long getTempsMoyen() {
            return tempsMoyen;
        }

        public void setTempsMoyen(long tempsMoyen) {
            this.tempsMoyen = tempsMoyen;
        }

        public long getMemoireMoyenne() {
            return memoireMoyenne;
        }

        public void setMemoireMoyenne(long memoireMoyenne) {
            this.memoireMoyenne = memoireMoyenne;
        }

        public boolean isSucces() {
            return succes;
        }

        public void setSucces(boolean succes) {
            this.succes = succes;
        }

        public String getErreur() {
            return erreur;
        }

        public void setErreur(String erreur) {
            this.erreur = erreur;
        }
    }
}

