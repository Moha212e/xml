package org.example.conversion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class CsvToXmlConverter {

    private static final Set<Integer> INCLUDED_COLUMNS = Set.of(
            0,  // ImageID
            1,  // ImageDir
            2,  // StudyID
            4,  // PatientID
            5,  // PatientBirth
            6,  // PatientSex_DICOM
            9,  // Projection
            10, // MethodProjection
            11, // MethodLabel
            28, // ViewPosition
            29, // Labels
            30, // Localizations
            31, // LabelsLocalizationsBySentence
            32, // labelCUIS
            33, // LocalizationsCUIS
            34, // Report
            35  // ReportID
    );

    private static final String ROOT_ELEMENT = "Images";

    private FileReader fileReader;
    private BufferedReader bufferedReader;
    private FileWriter xmlWriter;
    private int depth;

    public static void main(String[] args) {
        String csvPath = "src/main/java/org/example/data/PADCHEST_chest_x_ray_images_labels_160K_01.02.19.csv";
        String xmlPath = "src/main/java/org/example/data/PADCHEST_chest_x_ray_images_labels_160K_01.02.19.xml";

        if (args.length >= 1) {
            csvPath = args[0];
        }
        if (args.length >= 2) {
            xmlPath = args[1];
        }

        convert(csvPath, xmlPath);
    }

    public static void convert(String csvPath, String xmlPath) {
        CsvToXmlConverter converter = new CsvToXmlConverter();
        try {
            converter.openFiles(csvPath, xmlPath);
            converter.performConversion();
            System.out.println("Conversion terminée avec succès : " + xmlPath);
        } catch (Exception e) {
            System.err.println("Erreur lors de la conversion : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                converter.closeFiles();
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture des fichiers : " + e.getMessage());
            }
        }
    }


    private void openFiles(String csvPath, String xmlPath) throws IOException {
        this.fileReader = new FileReader(csvPath);
        this.bufferedReader = new BufferedReader(fileReader);
        this.xmlWriter = new FileWriter(xmlPath);
    }

    private void closeFiles() throws IOException {
        if (bufferedReader != null) bufferedReader.close();
        if (fileReader != null) fileReader.close();
        if (xmlWriter != null) xmlWriter.close();
    }

    private void performConversion() throws IOException {
        String[] headers = readHeader();
        if (headers == null) {
            throw new IOException("Impossible de lire l'en-tête du fichier CSV");
        }

        writeXmlHeader();
        writeOpeningTag(ROOT_ELEMENT);

        String line;
        while ((line = readCsvLine()) != null) {
            String[] completeFields = parseLine(line);
            String[] filteredFields = filterIncludedColumns(completeFields);

            writeOpeningTagWithAttribute("image", filteredFields[0]);

            for (int i = 1; i < filteredFields.length; i++) {
                String xmlContent = formatFieldForXml(filteredFields[i], headers[i]);
                if (!xmlContent.isEmpty()) {
                    write(xmlContent);
                }
            }

            writeClosingTag("image");
        }

        writeClosingTag(ROOT_ELEMENT);
    }


    private String[] readHeader() throws IOException {
        String line = bufferedReader.readLine();
        if (line != null) {
            String[] allFields = line.split(",");
            return filterIncludedColumns(allFields);
        }
        return null;
    }

    private String readCsvLine() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        boolean inQuotes = false;

        while ((line = bufferedReader.readLine()) != null) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append('\n');
            }
            stringBuilder.append(line);

            for (int i = 0; i < line.length(); i++) {
                char character = line.charAt(i);
                if (character == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        i++;
                        continue;
                    }
                    inQuotes = !inQuotes;
                }
            }

            if (!inQuotes) {
                break;
            }
        }

        return stringBuilder.length() == 0 ? null : stringBuilder.toString();
    }

    private String[] parseLine(String line) {
        ArrayList<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        int bracketLevel = 0;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);

            if (character == '[') {
                bracketLevel++;
                field.append(character);
            } else if (character == ']') {
                bracketLevel--;
                field.append(character);
            } else if (character == ',' && bracketLevel == 0) {
                fields.add(field.toString().trim());
                field.setLength(0);
            } else {
                field.append(character);
            }
        }
        fields.add(field.toString().trim());

        return fields.toArray(new String[0]);
    }


    private String[] filterIncludedColumns(String[] allFields) {
        ArrayList<String> filteredFields = new ArrayList<>();
        for (int i = 0; i < allFields.length; i++) {
            if (INCLUDED_COLUMNS.contains(i)) {
                filteredFields.add(allFields[i]);
            }
        }
        return filteredFields.toArray(new String[0]);
    }


    private void writeXmlHeader() throws IOException {
        xmlWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xmlWriter.write("<!DOCTYPE Images SYSTEM \"../structures/images.dtd\">\n");
    }

    private void writeOpeningTag(String elementName) throws IOException {
        write("<" + elementName + ">\n");
        depth++;
    }

    private void writeOpeningTagWithAttribute(String elementName, String attributeValue) throws IOException {
        write("<" + elementName + " Identifiant=\"" + attributeValue + "\">\n");
        depth++;
    }

    private void writeClosingTag(String elementName) throws IOException {
        depth--;
        write("</" + elementName + ">\n");
    }

    private void write(String content) throws IOException {
        xmlWriter.write(getIndentation() + content);
    }

    private String getIndentation() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            stringBuilder.append('\t');
        }
        return stringBuilder.toString();
    }

    private String formatFieldForXml(String value, String tagName) throws IOException {
        if (!value.contains("[")) {
            return "<" + tagName + ">" + value + "</" + tagName + ">\n";
        }

        handleSubElements(value, tagName);
        return "";
    }

    private void handleSubElements(String value, String tagName) throws IOException {
        writeOpeningTag(tagName);

        int startIdx = value.indexOf('[');
        int endIdx = value.lastIndexOf(']');

        if (startIdx == -1 || endIdx == -1 || endIdx <= startIdx) {
            writeClosingTag(tagName);
            return;
        }

        String arrayContent = value.substring(startIdx, endIdx + 1).trim();
        if ("[]".equals(arrayContent)) {
            writeClosingTag(tagName);
            return;
        }

        if ("LabelsLocalizationsBySentence".equals(tagName)) {
            handleArrayOfArrays(arrayContent);
        } else {
            String subTagName = getSubTagName(tagName);
            handleSimpleArray(arrayContent, subTagName);
        }

        writeClosingTag(tagName);
    }

    private void handleSimpleArray(String arrayContent, String subTagName) throws IOException {
        boolean inQuotes = false;
        StringBuilder token = new StringBuilder();

        for (int i = 0; i < arrayContent.length(); i++) {
            char character = arrayContent.charAt(i);

            if (character == '\'') {
                inQuotes = !inQuotes;
                continue;
            }

            if (character == ',' && !inQuotes) {
                String value = clean(token.toString());
                token.setLength(0);
                if (!value.isEmpty()) {
                    write("<" + subTagName + ">" + value + "</" + subTagName + ">\n");
                }
                continue;
            }

            token.append(character);
        }

        String lastElement = clean(token.toString());
        if (!lastElement.isEmpty()) {
            write("<" + subTagName + ">" + lastElement + "</" + subTagName + ">\n");
        }
    }

    private void handleArrayOfArrays(String arrayContent) throws IOException {
        boolean inQuotes = false;
        int bracketDepth = 0;
        StringBuilder currentToken = new StringBuilder();
        ArrayList<String> sentenceElements = new ArrayList<>();

        for (int i = 0; i < arrayContent.length(); i++) {
            char character = arrayContent.charAt(i);

            if (character == '\'') {
                inQuotes = !inQuotes;
                continue;
            }

            if (!inQuotes) {
                if (character == '[') {
                    bracketDepth++;
                    continue;
                }

                if (character == ']') {
                    if (bracketDepth == 2) {
                        String value = clean(currentToken.toString().trim());
                        currentToken.setLength(0);
                        if (!value.isEmpty()) {
                            sentenceElements.add(value);
                        }

                        if (!sentenceElements.isEmpty()) {
                            write("<Sentence>\n");
                            depth++;

                            write("<Label>" + sentenceElements.get(0) + "</Label>\n");

                            for (int k = 1; k < sentenceElements.size(); k++) {
                                write("<Localization>" + sentenceElements.get(k) + "</Localization>\n");
                            }

                            depth--;
                            write("</Sentence>\n");
                        }
                        sentenceElements.clear();
                    }
                    bracketDepth--;
                    continue;
                }

                if (character == ',' && bracketDepth == 2) {
                    String value = clean(currentToken.toString().trim());
                    currentToken.setLength(0);
                    if (!value.isEmpty()) {
                        sentenceElements.add(value);
                    }
                    continue;
                }
            }

            if (bracketDepth == 2) {
                currentToken.append(character);
            }
        }
    }

    private String clean(String string) {
        String value = string.trim();
        if (value.startsWith("[")) value = value.substring(1);
        if (value.endsWith("]")) value = value.substring(0, value.length() - 1);
        if (value.startsWith("'")) value = value.substring(1);
        if (value.endsWith("'")) value = value.substring(0, value.length() - 1);
        value = value.trim();
        return "None".equalsIgnoreCase(value) ? "" : value;
    }

    private String getSubTagName(String tagName) {
        switch (tagName) {
            case "Labels": return "Label";
            case "Localizations": return "Localization";
            case "LabelsLocalizationsBySentence": return "Sentence";
            case "labelCUIS": return "labelCUI";
            case "LocalizationsCUIS": return "LocalizationsCUI";
            default: return tagName + "Item";
        }
    }
}

