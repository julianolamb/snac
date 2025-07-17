package br.eti.x87.xmlParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import br.eti.x87.model.Property;

public class ParserProperties {

    public String getOwner(InputStream file){
        String owner = null;
        try {
            InputStream is = file;

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);

            Element element=doc.getDocumentElement();
            element.normalize();

            NodeList nList = doc.getElementsByTagName("property");


            for (int i=0; i<nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element2 = (Element) node;
                   // if (getValue("owner", element2).equals("owner")) {
                        owner = getValue("owner", element2);
                    //}
                }
            }
        } catch (Exception e) {e.printStackTrace();}
        return owner;
    }

    /*
    Este método recebe um arquivo XML e devolve um array com as propriedades disponiveis
     */
    public List xmlToList(InputStream file){
        ArrayList<Property> lista = new ArrayList();
        Property p;

        try {
            InputStream is = file;

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);

            Element element=doc.getDocumentElement();
            element.normalize();

            NodeList nList = doc.getElementsByTagName("property");

            for (int i=0; i<nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element2 = (Element) node;
                    p = new Property(Long.parseLong(getValue("id", element2)),
                            getValue("name", element2),getValue("surname", element2), getValue("area", element2));
                    lista.add(p);
                }
            }
        } catch (Exception e) {e.printStackTrace();}

        return lista;
    }

    private static String getValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }

    public CharSequence[] getProperties(InputStream is){
        List<Property> listaPropriedades = xmlToList(is);
        List<String> listaNomeSobrenome = new ArrayList<>();

        for (Property p: listaPropriedades) {
            if ((p.getArea().equals(" "))) {
                listaNomeSobrenome.add(p.getName() + " " + p.getSurname());
            } else {
                listaNomeSobrenome.add(p.getName() + " " + p.getSurname() + " (" + p.getArea() + ")");
            }
        }

        CharSequence[] cs = listaNomeSobrenome.toArray(new CharSequence[listaNomeSobrenome.size()]);
        return cs;
    }

    public ArrayList<String> getPropertiesArrayList(InputStream is){
        List<Property> listaPropriedades = xmlToList(is);
        ArrayList<String> listaNomeSobrenome = new ArrayList<>();

        for (Property p: listaPropriedades) {
            if ((p.getArea().equals(" "))) {
                listaNomeSobrenome.add(p.getName() + " " + p.getSurname());
            } else {
                listaNomeSobrenome.add(p.getName() + " " + p.getSurname() + " (" + p.getArea() + ")");
            }
        }

        return listaNomeSobrenome;
    }
}
