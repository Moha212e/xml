
package org.example.validation;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.*;

/**
 * Validateur SAX pour les fichiers XML PadChest.
 */
public class SAXValidator {

    public static void main(String[] args) {
        String xmlPath = "src/main/java/org/example/data/PADCHEST_chest_x_ray_images_labels_160K_01.02.19.xml";
        String xsdPath = "src/main/java/org/example/structures/images.xsd";

        System.out.println("=== VALIDATION SAX (Niveau Minimum) ===");
        System.out.println("Fichier XML : " + xmlPath);
        if (xsdPath != null) {
            System.out.println("Fichier XSD/DTD fourni : " + xsdPath);
        } else {
            System.out.println("Mode de validation : DTD uniquement (aucun XSD fourni)");
        }
        System.out.println();

        long startTime = System.currentTimeMillis();

        try {
            validate(xmlPath, xsdPath);

            long endTime = System.currentTimeMillis();
            System.out.println("\n✓ Validation et analyse terminées avec succès !");
            System.out.println("Temps d'exécution : " + (endTime - startTime) + " ms");

        } catch (Exception e) {
            System.err.println("✗ Erreur lors de la validation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Valide le fichier XML avec DTD ou XSD et effectue les calculs statistiques.
     *
     * @param xmlPath Chemin vers le fichier XML
     * @param xsdPath Chemin vers le fichier XSD ou DTD (null pour validation DTD uniquement)
     */
    public static void validate(String xmlPath, String xsdPath) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);

        // Détermination automatique du mode en fonction de l'extension du fichier fourni.
        if (xsdPath != null) {
            String lower = xsdPath.toLowerCase(Locale.ROOT); // pour comparaison insensible à la casse
            File schemaFile = new File(xsdPath);
            if (lower.endsWith(".xsd")) {
                if (!schemaFile.exists()) {
                    throw new IllegalArgumentException("Fichier XSD introuvable: " + xsdPath);
                }
                // Validation XSD
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(schemaFile);
                factory.setSchema(schema);
                factory.setValidating(false); // schema-based validation active
                System.out.println("✓ Validation XSD activée");
            } else if (lower.endsWith(".dtd")) {
                if (!schemaFile.exists()) {
                    throw new IllegalArgumentException("Fichier DTD introuvable: " + xsdPath);
                }
                // Validation DTD via déclaration DOCTYPE dans le XML; on active le mode validating
                factory.setValidating(true);
                System.out.println("✓ Validation DTD activée (fichier .dtd fourni)");
            } else {
                // Extension inconnue: choisir DTD si fichier contient ".dtd" dans le nom, sinon signaler
                if (schemaFile.exists() && xsdPath.toLowerCase().contains(".dtd")) {
                    factory.setValidating(true);
                    System.out.println("✓ Validation DTD activée (fichier fourni détecté comme DTD)");
                } else {
                    throw new IllegalArgumentException("Type de schéma non reconnu pour: " + xsdPath + ". Utiliser .xsd ou .dtd, ou passez null pour DTD via DOCTYPE.");
                }
            }
        } else {
            // Mode DTD uniquement (le XML doit contenir un DOCTYPE qui référence la DTD)
            factory.setValidating(true);
            System.out.println("✓ Validation DTD activée (aucun XSD fourni)");
        }

        SAXParser parser = factory.newSAXParser();
        ImageHandler handler = new ImageHandler();
        parser.parse(new File(xmlPath), handler);

        // Afficher les résultats
        System.out.println();
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
