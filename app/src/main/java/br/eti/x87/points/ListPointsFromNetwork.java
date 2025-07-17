package br.eti.x87.points;

import android.content.Context;

import java.util.ArrayList;

import br.eti.x87.model.Coordinate;

public class ListPointsFromNetwork implements ListPoints {

    private Context context;

    public ListPointsFromNetwork(Context context){
        this.context = context;
    }

    @Override
    public ArrayList<Coordinate> getPointsList(String fileName) {
        //TODO
        return null;
    }

    public ArrayList<Coordinate> getBorderPointsList(String fileName) {
        // TODO
        return null;
    }
}
