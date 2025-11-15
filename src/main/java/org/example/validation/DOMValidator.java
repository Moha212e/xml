package org.example.validation;

import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Validateur DOM pour les fichiers XML PadChest.
 * Niveau PRO : Parser DOM avec validation DTD/XSD et calculs statistiques.
 *
 * Calculs effectués :
 * - Nombre d'images contenant la localisation 'loc right'
 * - Top 10 des labels les plus fréquents avec leur nombre d'occurrences
 */
public class DOMValidator {

    public static void main(String[] args) {
        String xmlPath = "src/main/java/org/example/data/PADCHEST_chest_x_ray_images_labels_160K_01.02.19.xml";
        //String xsdPath = null; // Optionnel : si null, validation DTD uniquement
        String xsdPath = "src/main/java/org/example/structures/images.xsd"; // Optionnel


        System.out.println("=== VALIDATION DOM (Niveau Pro) ===");
        System.out.println("Fichier XML : " + xmlPath);
        if (xsdPath != null) {
            System.out.println("Fichier XSD : " + xsdPath);
        } else {
            System.out.println("Mode de validation : DTD uniquement");
        }
        System.out.println();

        long startTime = System.currentTimeMillis();

        try {
            ValidationResult result = validate(xmlPath, xsdPath);

            long endTime = System.currentTimeMillis();

            if (result.isValid()) {
                System.out.println("✓ Le document XML est VALIDE !");
            } else {
                System.out.println("✗ Le document XML contient des erreurs :");
                result.getErrors().forEach(System.out::println);
            }

            System.out.println();
            afficherResultats(result);

            System.out.println("\nTemps d'exécution : " + (endTime - startTime) + " ms");

        } catch (Exception e) {
            System.err.println("✗ Erreur lors de la validation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Valide le fichier XML avec DTD ou XSD et effectue les calculs statistiques.
     */
    public static ValidationResult validate(String xmlPath, String xsdPath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        // Configuration de la validation
        if (xsdPath != null) {
            // Validation XSD
            SchemaFactory schemaFactory = SchemaFactory.newInstance(
                javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI); // elle sert à indiquer qu'on utilise XSD
            Schema schema = schemaFactory.newSchema(new File(xsdPath));
            factory.setSchema(schema); // on assigne le schema au factory
        } else {
            // Validation DTD
            factory.setValidating(true);
        }

        DocumentBuilder builder = factory.newDocumentBuilder();

        // Capturer les erreurs de validation
        ValidationErrorHandler errorHandler = new ValidationErrorHandler();
        builder.setErrorHandler(errorHandler);

        // Parser le document
        Document document = builder.parse(new File(xmlPath));

        // Effectuer les calculs statistiques
        ValidationResult result = new ValidationResult();
        result.setValid(errorHandler.getErrors().isEmpty());
        result.setErrors(errorHandler.getErrors());

        analyserDocument(document, result);

        return result;
    }

    /**
     * Analyse le document DOM pour extraire les statistiques.
     */
    private static void analyserDocument(Document document, ValidationResult result) {
        NodeList images = document.getElementsByTagName("image"); // supposons que les images sont dans des éléments <image>
        result.setImageCount(images.getLength());

        Map<String, Integer> labelFrequency = new HashMap<>();
        int locRightCount = 0;

        for (int i = 0; i < images.getLength(); i++) {
            Element image = (Element) images.item(i);

            // Vérifier si l'image contient 'loc right'
            if (containsLocRight(image)) {
                locRightCount++;
            }

            // Compter les labels
            NodeList labels = image.getElementsByTagName("Label");
            for (int j = 0; j < labels.getLength(); j++) {
                String labelText = labels.item(j).getTextContent().trim();
                if (!labelText.isEmpty()) {
                    labelFrequency.merge(labelText, 1, Integer::sum);
                }
            }
        }

        result.setLocRightCount(locRightCount);
        result.setLabelFrequency(labelFrequency);
    }

    /**
     * Vérifie si une image contient la localisation 'loc right'.
     */
    private static boolean containsLocRight(Element image) {
        NodeList localizations = image.getElementsByTagName("Localization");
        for (int i = 0; i < localizations.getLength(); i++) {
            String locText = localizations.item(i).getTextContent().trim();
            if ("loc right".equalsIgnoreCase(locText)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Affiche les résultats de l'analyse.
     */
    private static void afficherResultats(ValidationResult result) {
        System.out.println("--- RÉSULTATS DE L'ANALYSE ---");
        System.out.println();

        // 1. Nombre d'images avec 'loc right'
        System.out.println("1. Images contenant 'loc right' : " + result.getLocRightCount());
        System.out.println();

        // 2. Top 10 des labels
        System.out.println("2. Top 10 des labels les plus fréquents :");
        List<Map.Entry<String, Integer>> top10 = result.getLabelFrequency().entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        int rank = 1;
        for (Map.Entry<String, Integer> entry : top10) {
            System.out.printf("   %2d. %-40s : %6d occurrences%n",
                rank++, entry.getKey(), entry.getValue());
        }

        System.out.println();
        System.out.println("Total d'images traitées : " + result.getImageCount());
        System.out.println("Total de labels différents : " + result.getLabelFrequency().size());
    }

    /**
     * Gestionnaire d'erreurs de validation.
     */
    static class ValidationErrorHandler implements ErrorHandler {
        private List<String> errors = new ArrayList<>();

        @Override
        public void warning(SAXParseException exception) {
            errors.add("WARNING: " + exception.getMessage());
        }

        @Override
        public void error(SAXParseException exception) {
            errors.add("ERROR: " + exception.getMessage());
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            errors.add("FATAL: " + exception.getMessage());
            throw exception;
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    /**
     * Classe pour stocker les résultats de la validation et de l'analyse.
     */
    static class ValidationResult {
        private boolean valid;
        private List<String> errors = new ArrayList<>();
        private int imageCount;
        private int locRightCount;
        private Map<String, Integer> labelFrequency = new HashMap<>();

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors;
        }

        public int getImageCount() {
            return imageCount;
        }

        public void setImageCount(int imageCount) {
            this.imageCount = imageCount;
        }

        public int getLocRightCount() {
            return locRightCount;
        }

        public void setLocRightCount(int locRightCount) {
            this.locRightCount = locRightCount;
        }

        public Map<String, Integer> getLabelFrequency() {
            return labelFrequency;
        }

        public void setLabelFrequency(Map<String, Integer> labelFrequency) {
            this.labelFrequency = labelFrequency;
        }
    }
}

