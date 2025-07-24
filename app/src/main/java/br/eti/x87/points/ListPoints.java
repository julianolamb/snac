package br.eti.x87.points;

import java.util.ArrayList;

import br.eti.x87.model.Coordinate;

public interface ListPoints {
    ArrayList<Coordinate> getPointsList(String fileName);
    ArrayList<Coordinate> getBorderPointsList(String fileName);
}
