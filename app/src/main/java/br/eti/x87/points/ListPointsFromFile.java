package br.eti.x87.points;

import android.content.Context;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;

import br.eti.x87.model.Coordinate;
import br.eti.x87.xmlParser.ParserPoints;

public class ListPointsFromFile implements ListPoints {

    private Context context;

    public ListPointsFromFile(Context context){
        this.context = context;
    }

    public ArrayList<Coordinate> getPointsList(String fileName) {
        try {
            InputStream is = context.getAssets().open(fileName);
            ParserPoints pp = new ParserPoints();
            ArrayList<Coordinate> pontos = pp.xmlToList(is);
            return pontos;
        } catch (Exception e) {
            Toast.makeText(context, "Erro ao carregar coordenadas!", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    public ArrayList<Coordinate> getBorderPointsList(String fileName) {
        try {
            InputStream is = context.getAssets().open(fileName);
            ParserPoints pp = new ParserPoints();
            ArrayList<Coordinate> pontos = pp.xmlToListJustBorders(is);
            return pontos;
        } catch (Exception e) {
            Toast.makeText(context, "Erro ao carregar bordas!", Toast.LENGTH_SHORT).show();
        }
        return null;
    }
}
