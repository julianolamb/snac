package br.eti.x87.xmlParser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import br.eti.x87.model.Coordinate;

public class ParserPoints {

    /*
    Este método recebe um arquivo XML e devolve um array com as coordenadas
     */
    public ArrayList<Coordinate> xmlToList(InputStream file){
        ArrayList<Coordinate> lista = new ArrayList();
        Coordinate c;

        try {
            InputStream is = file;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);

            Element element=doc.getDocumentElement();
            element.normalize();

            NodeList nList = doc.getElementsByTagName("point");

            for (int i=0; i<nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element2 = (Element) node;
                    double lat = Double.parseDouble(getValue("lat", element2));
                    double lng = Double.parseDouble(getValue("long", element2));
                    c = new Coordinate(lat, lng, getValue("name", element2));
                    lista.add(c);
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


    public ArrayList<Coordinate> xmlToListJustBorders(InputStream file){
        ArrayList<Coordinate> lista = new ArrayList();
        Coordinate c;

        try {
            InputStream is = file;
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(is);

            Element element=doc.getDocumentElement();
            element.normalize();

            NodeList nList = doc.getElementsByTagName("point");

            for (int i=0; i<nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element2 = (Element) node;
                    if (getValue("start", element2).equals("true")){
                        double lat = Double.parseDouble(getValue("lat", element2));
                        double lng = Double.parseDouble(getValue("long", element2));
                        c = new Coordinate(lat, lng, getValue("name", element2));
                        lista.add(c);
                    }
                }
            }
        } catch (Exception e) {e.printStackTrace();}

        return lista;
    }
}
