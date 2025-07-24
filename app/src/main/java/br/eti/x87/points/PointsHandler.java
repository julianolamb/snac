package br.eti.x87.points;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import br.eti.x87.model.Coordinate;

public class PointsHandler {

    public static ArrayList<Coordinate> orderPoints(Coordinate pontoInicial, ArrayList<Coordinate> listaInicial){
        ArrayList<Coordinate> listaOrdenada = new ArrayList<>();
        ArrayList<Coordinate> listaLocal = new ArrayList<>();
        ArrayList<Float> distancias;
        float menorDistancia = 0;
        int indiceMenorDistancia = 0;

        for (Coordinate c: listaInicial) {
            if (!c.getDescricao().equals(pontoInicial.getDescricao()))
                listaLocal.add(c);
        }

        listaOrdenada.add(pontoInicial);

        Coordinate inicioComparacao = pontoInicial;

        int tamanho = listaLocal.size();

        /*
          A interação tem que ser até N-1 elementos, pois o primeiro ponto já foi adicionado.
        */
        for(int cont = 0; cont < tamanho - 1; cont++) {
            distancias = gerarListaComDistancia(inicioComparacao, listaLocal);

            indiceMenorDistancia = encontrarMenorDistancia(distancias);

            // Guarda o inicio para ser removido da lista
            Coordinate pontoParaRemover = inicioComparacao;

            // Atualiza o início para percorrer a partir do elementos mais próximo
            inicioComparacao = listaLocal.get(indiceMenorDistancia);
            // Adiciona o elemento encontrado na lista ordenada
            listaOrdenada.add(listaLocal.get(indiceMenorDistancia));
            // Remove o ponto já pesquisado
            listaLocal.remove(pontoParaRemover);
        }
        gerarDistanciaProximosPontos(listaOrdenada);
        return listaOrdenada;
    }

    public static int encontrarMenorDistancia(ArrayList<Float> lista){
        /*
            Não é elegante, mas funciona. Iniciei com um valor alto, para poder encontrar o menor valor
         */
        float menorDistancia = 999999999f;
        int indiceMenorDistancia = 0;

        for (int i = 0; i < lista.size(); i++) {
            if ((lista.get(i) < menorDistancia) && (lista.get(i) != 0)) {
                menorDistancia = lista.get(i);
                indiceMenorDistancia = i;
            }
        }
        return indiceMenorDistancia;
    }

    public static ArrayList<Float> gerarListaComDistancia(Coordinate pontoOrigem, ArrayList<Coordinate> lista){
        ArrayList<Float> distancias = new ArrayList<>();
        for (Coordinate umPontoDaLista : lista) {

            if (umPontoDaLista.getDescricao() == pontoOrigem.getDescricao()){
                distancias.add(0f);
            } else {
                distancias.add(calcularDistancia(pontoOrigem, umPontoDaLista));
            }
        }

        return distancias;
    }

    public static float calcularDistancia(Coordinate a, Coordinate b){
        float[] results = new float[1];

        Location.distanceBetween(
                a.getLat(),
                a.getLng(),
                b.getLat(),
                b.getLng(),
                results);

        return results[0];
    }

    public static float calcularDistancia(LatLng a, LatLng b){
        float[] results = new float[1];

        Location.distanceBetween(
                a.latitude,
                a.longitude,
                b.latitude,
                b.longitude,
                results);

        return results[0];
    }


    public static void gerarDistanciaProximosPontos(ArrayList<Coordinate> lista){
        int tamLista = lista.size();
        for (int i=0; i<tamLista-1; i++){
            lista.get(i).setDistanciaProximoPonto(calcularDistancia(lista.get(i), lista.get(i+1)));
        }
        lista.get(tamLista-1).setDistanciaProximoPonto(0);
    }

    public static LatLng encontrarPontoMedio(LatLng a, LatLng b){
        double dLon = Math.toRadians(a.longitude - b.longitude);

        double lat1 = Math.toRadians(b.latitude);
        double lat2 = Math.toRadians(a.latitude);
        double lon1 = Math.toRadians(b.longitude);

        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

        lat3 = Math.toDegrees(lat3);
        lon3 = Math.toDegrees(lon3);

        LatLng ponto = new LatLng(lat3, lon3);
        return ponto;
    }

    public static float calcularDistanciaTotal(ArrayList<Coordinate> lista){
        //Log.d("CDT ", "Inicio do metodo. Tamanho: " + lista.size());
        Log.d("","");
        float total = 0;
        double auxDistancia = 0;

        for (int i = 0; i<(lista.size()); i++){
            Coordinate c  = lista.get(i);
            auxDistancia = c.getDistanciaProximoPonto();
            total += auxDistancia;
            //Log.d("CDT", "Elemento: " + c.getDescricao()
            //        + " Distancia prox ponto: " + auxDistancia);
        }
        //Log.d("","" + total);
        return total;
    }

    public static ArrayList<Coordinate> convertLatLng2Coordinate(ArrayList<LatLng> lista){
        ArrayList<Coordinate> coordinateList = new ArrayList<>();
        Coordinate coordinate;

        for (LatLng ponto: lista) {
            coordinate = new Coordinate(ponto.latitude, ponto.longitude, "");
            coordinateList.add(coordinate);
        }

        gerarDistanciaProximosPontos(coordinateList);

        return coordinateList;
    }

    public void encontrarDistanciaComColetaIniciada(ArrayList<Coordinate> lista){

    }

    public static void imprimirVetor(ArrayList<Coordinate> lista){
        Log.d("", "");
        Log.d("Inicio do método", "Lista tamanho: " + lista.size() );
        String s = "";
        for (int i = 0; i < lista.size(); i++) {
            s = s+ "["+i+"] " + lista.get(i).getLat() + " | ";
        }

        Log.d("IN: ", " " + s );
    }

    public static void imprimirVetorFloat(ArrayList<Float> lista){
        NumberFormat format = new DecimalFormat("0");
        Log.d("Inicio do método", "Lista tamanho: " + lista.size() );
        String s = "";
        for (int i = 0; i < lista.size(); i++) {
            s = s +  "[" + i + "] " + format.format(lista.get(i));
            s = s + " | ";
        }
        Log.d("IN: ", " " + s );
    }

}
