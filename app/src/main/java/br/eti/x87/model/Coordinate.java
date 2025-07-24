package br.eti.x87.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Coordinate implements Parcelable {
    private double lat;
    private double lng;
    private String descricao;
    private double distanciaProximoPonto;
    private int border = 0;
    private double distanciaIniciandoAqui;

    public Coordinate(double lat, double lng, String descricao){
        this.lat = lat;
        this.lng = lng;
        this.descricao = descricao;
    }

    protected Coordinate(Parcel in) {
        lat = in.readDouble();
        lng = in.readDouble();
        descricao = in.readString();
        distanciaProximoPonto = in.readDouble();
        border = in.readInt();
        distanciaIniciandoAqui = in.readDouble();
    }

    public static final Creator<Coordinate> CREATOR = new Creator<Coordinate>() {
        @Override
        public Coordinate createFromParcel(Parcel in) {
            return new Coordinate(in);
        }

        @Override
        public Coordinate[] newArray(int size) {
            return new Coordinate[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeString(descricao);
        dest.writeDouble(distanciaProximoPonto);
        dest.writeInt(border);
        dest.writeDouble(distanciaIniciandoAqui);
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getDistanciaProximoPonto() { return distanciaProximoPonto; }

    public void setDistanciaProximoPonto(double distanciaProximoPonto) {
        this.distanciaProximoPonto = distanciaProximoPonto;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getBorder() {
        return border;
    }

    public void setBorder(int border) {
        this.border = border;
    }

    public double getDistanciaIniciandoAqui() {
        return distanciaIniciandoAqui;
    }

    public void setDistanciaIniciandoAqui(double distanciaIniciandoAqui) {
        this.distanciaIniciandoAqui = distanciaIniciandoAqui;
    }
}
