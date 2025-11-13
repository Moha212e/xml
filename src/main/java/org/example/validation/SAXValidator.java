package org.example.validation;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.*;

/**
 * Validateur SAX pour les fichiers XML PadChest.
 * Niveau MINIMUM : Parser SAX avec validation DTD et calculs statistiques.
 *
 * Calculs effectués :
 * - Nombre d'images contenant la localisation 'loc right'
 * - Top 10 des labels les plus fréquents avec leur nombre d'occurrences
 */
public class SAXValidator {

    public static void main(String[] args) {
        String xmlPath = "src/main/java/org/example/data/PADCHEST_chest_x_ray_images_labels_160K_01.02.19.xml";

        if (args.length >= 1) {
            xmlPath = args[0];
        }

        System.out.println("=== VALIDATION SAX (Niveau Minimum) ===");
        System.out.println("Fichier XML : " + xmlPath);
        System.out.println();

        long startTime = System.currentTimeMillis();

        try {
            validate(xmlPath);

            long endTime = System.currentTimeMillis();
            System.out.println("\n✓ Validation et analyse terminées avec succès !");
            System.out.println("Temps d'exécution : " + (endTime - startTime) + " ms");

        } catch (Exception e) {
            System.err.println("✗ Erreur lors de la validation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Valide le fichier XML avec DTD et effectue les calculs statistiques.
     */
    public static void validate(String xmlPath) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true); // DTD
        factory.setNamespaceAware(true);

        SAXParser parser = factory.newSAXParser();
        ImageHandler handler = new ImageHandler();
        parser.parse(new File(xmlPath), handler);

        // Afficher les résultats
        System.out.println("--- RÉSULTATS DE L'ANALYSE ---");
        System.out.println();
        System.out.println("1. Images contenant 'loc right' : " + handler.getLocRightCount());
        System.out.println();
        System.out.println("2. Top 10 des labels les plus fréquents :");
        List<Map.Entry<String, Integer>> top10 = handler.getTop10Labels();
        int rank = 1;
        for (Map.Entry<String, Integer> entry : top10) {
            System.out.printf("   %2d. %-40s : %6d occurrences%n", rank++, entry.getKey(), entry.getValue());
        }
        System.out.println();
        System.out.println("Total d'images traitées : " + handler.getImageCount());
        System.out.println("Total de labels différents : " + handler.getTotalUniqueLabels());
        System.out.println();
        System.out.println("Note: Le comptage 'loc right' inclut désormais les localisations présentes dans <LabelsLocalizationsBySentence>/<Sentence>.");
    }

    /**
     * Handler SAX pour traiter les événements XML et effectuer les calculs.
     */
    static class ImageHandler extends DefaultHandler {
        private int imageCount = 0;
        private int locRightCount = 0;
        private final Map<String, Integer> labelFrequency = new HashMap<>();

        private final StringBuilder currentText = new StringBuilder();
        private boolean currentImageHasLocRight = false;
        private boolean insideImage = false;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            currentText.setLength(0);
            if ("image".equalsIgnoreCase(qName)) {
                imageCount++;
                currentImageHasLocRight = false;
                insideImage = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            String content = currentText.toString().trim();
            String element = qName.toLowerCase();

            // Compter les localisations 'loc right' (toute balise <Localization> sous une <image>)
            if (insideImage && "localization".equals(element)) {
                if ("loc right".equalsIgnoreCase(content) && !currentImageHasLocRight) {
                    locRightCount++;
                    currentImageHasLocRight = true;
                }
            }

            // Compter les labels
            if (insideImage && "label".equals(element)) {
                if (!content.isEmpty()) {
                    labelFrequency.merge(content, 1, Integer::sum);
                }
            }

            if ("image".equalsIgnoreCase(qName)) {
                insideImage = false;
                currentImageHasLocRight = false; // reset sentinel
            }
            currentText.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            currentText.append(ch, start, length);
        }

        public int getImageCount() { return imageCount; }
        public int getLocRightCount() { return locRightCount; }
        public int getTotalUniqueLabels() { return labelFrequency.size(); }

        public List<Map.Entry<String, Integer>> getTop10Labels() {
            return labelFrequency.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(10)
                    .toList();
        }
    }
}
