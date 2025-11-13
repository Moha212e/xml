package org.example.html;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import java.io.*;

public class XmlToHtmlExpert {

    public static void main(String[] args) {
        String cheminXml = "src/main/java/org/example/data/PADCHEST_chest_x_ray_images_labels_160K_01.02.19.xml";
        String cheminXslt = "src/main/java/org/example/xslt/expert.xsl";
        String cheminHtml = "src/main/java/org/example/html/output_expert.html";

        try {
            File fichierXml = new File(cheminXml);
            File fichierXslt = new File(cheminXslt);

            if (!fichierXml.exists()) {
                System.out.println("Erreur : Fichier XML introuvable - " + cheminXml);
                return;
            }

            if (!fichierXslt.exists()) {
                System.out.println("Erreur : Fichier XSLT introuvable - " + cheminXslt);
                return;
            }

            long debut = System.currentTimeMillis();

            TransformerFactory factory = TransformerFactory.newInstance();
            Source sourceXslt = new StreamSource(fichierXslt);
            Transformer transformer = factory.newTransformer(sourceXslt);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            Source sourceXml = new StreamSource(fichierXml);
            Result resultatHtml = new StreamResult(new File(cheminHtml));

            transformer.transform(sourceXml, resultatHtml);

            long duree = System.currentTimeMillis() - debut;

            System.out.println("Transformation réussie - " + cheminHtml);
            System.out.println("Durée : " + duree + " ms");

        } catch (TransformerException e) {
            System.out.println("Erreur lors de la transformation : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
