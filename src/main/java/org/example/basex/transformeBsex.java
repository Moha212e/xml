package org.example.basex;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import java.io.File;

public class transformeBsex {
    public static void main(String[] args) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        Source xslt = new StreamSource(new File("src/main/java/org/example/basex/expert.xsl"));
        Source xml = new StreamSource(new File("src/main/java/org/example/data/PADCHEST_chest_x_ray_images_labels_160K_01.02.19.xml"));

        Transformer transformer = factory.newTransformer(xslt);
        Result output = new StreamResult(new File("src/main/java/org/example/basex/basex.html"));

        transformer.transform(xml, output);
        System.out.println("✅ HTML expert généré: src/main/java/org/example/basex/basex.html");
    }
}
