package br.eti.x87.wizard.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import br.eti.x87.model.Coordinate;
import br.eti.x87.points.ListPoints;
import br.eti.x87.points.ListPointsFromFile;
import br.eti.x87.points.PointsHandler;
import br.eti.x87.property.ListProperties;
import br.eti.x87.property.ListPropertiesFromFile;
import br.eti.x87.snac.R;
import br.eti.x87.wizard.model.MapPage;
import br.eti.x87.wizard.model.Page;

public class MapaFragment
        extends Fragment
        implements GoogleMap.OnMarkerClickListener {

    private static int flag;
    private static Context mContext;
    private Marker pontoSelecionado;

    private static final String ARG_KEY = "key";

    private PageFragmentCallbacks mCallbacks;
    private String mKey;
    private MapPage mPage;

    MapView mMapView;
    private GoogleMap googleMap;

    public Marker getPontoSelecionado() {
        return pontoSelecionado;
    }

    public void setPontoSelecionado(Marker pontoSelecionado) {
        this.pontoSelecionado = pontoSelecionado;
    }

    public static Context getmContext() {
        return mContext;
    }

    private ArrayList<Coordinate> pointsList = new ArrayList<>();
    private ArrayList<Coordinate> borderPointsList = new ArrayList<>();
    private ArrayList<Float> distanciasPontosBorda = new ArrayList<>();

    public static int getFlag() {
        return flag;
    }

    public ArrayList<Float> getDistanciasPontosBorda() {
        return distanciasPontosBorda;
    }

    public ArrayList<Coordinate> getPointsList() {
        return pointsList;
    }

    public ArrayList<Coordinate> getBorderPointsList() {
        return borderPointsList;
    }

    public void setDistanciasPontosBorda(ArrayList<Float> distanciasPontosBorda) {
        this.distanciasPontosBorda = distanciasPontosBorda;
    }

    public void setPointsList(ArrayList<Coordinate> pointsList) {
        this.pointsList = pointsList;
    }

    public void setBorderPointsList(ArrayList<Coordinate> borderPointsList) {
        this.borderPointsList = borderPointsList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mKey = args.getString(ARG_KEY);
        mPage = (MapPage) mCallbacks.onGetPage(mKey);

        super.onCreate(savedInstanceState);
    }

    public static MapaFragment create(String key, int flague, Context context) {
        Bundle args = new Bundle();
        args.putString(ARG_KEY, key);

        MapaFragment fragment = new MapaFragment();
        fragment.setArguments(args);

        flag = flague;                    // A flag irá indicar o índice da propriedade selecionada.
        MapaFragment.mContext = context; // O contexto é necessário para localizar os Assets

        return fragment;
    }

    public MapaFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.location_fragment, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                obterPontosEPlotar();

                LatLng pontoZoom = new LatLng(getPointsList().get(0).getLat(),
                        getPointsList().get(0).getLng());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pontoZoom, 16));
                googleMap.setOnMarkerClickListener(MapaFragment.this);

                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof PageFragmentCallbacks)) {
            throw new ClassCastException("Activity must implement PageFragmentCallbacks");
        }

        mCallbacks = (PageFragmentCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void generatePointsList(int flag) {
        /*
        Com base na flag informada no Wizard, é selecionada a propriedade correspondente, para então
        determinar o conjunto de pontos que devem ser plotados no mapa.
         */
        ListProperties lpp = new ListPropertiesFromFile(getmContext());
        ListPoints lpt = new ListPointsFromFile(getmContext());

        List<String> minhaLista = lpp.getPropertiesListArray();

        String nomePropriedade = minhaLista.get(flag);
        nomePropriedade = nomePropriedade.replace(" ", "");
        nomePropriedade = nomePropriedade.concat(".xml");

        pointsList = lpt.getPointsList(nomePropriedade);
        borderPointsList = lpt.getBorderPointsList(nomePropriedade);
    }

    public void calcularDistanciasPontosBorda(ArrayList<Coordinate> listaPontosOriginal) {
        ArrayList<Coordinate> listaPontosCopiado;
        ArrayList<Coordinate> listaPontosOrdenado;
        float auxDistancia = 0;

        for (int i = 0; i < getBorderPointsList().size(); i++) {
            listaPontosCopiado = new ArrayList<>();
            for (Coordinate c : listaPontosOriginal) {
                listaPontosCopiado.add(c);
            }

            Coordinate bolaDaVez = getBorderPointsList().get(i);
            listaPontosOrdenado = PointsHandler.orderPoints(bolaDaVez, listaPontosCopiado);
            auxDistancia = PointsHandler.calcularDistanciaTotal(listaPontosOrdenado);
            distanciasPontosBorda.add(auxDistancia);
        }
    }

    public void atualizarValoresBordaEDistancia() {
        for (Coordinate c : getPointsList()) {
            for (int i = 0; i < borderPointsList.size(); i++) {
                if (c.getDescricao().equals(borderPointsList.get(i).getDescricao())) {
                    c.setBorder(1);
                    c.setDistanciaIniciandoAqui(distanciasPontosBorda.get(i));
                    break;
                }
            }
        }
    }

    public void plotarPontosDeColeta() {
        NumberFormat format = new DecimalFormat("0");
        for (Coordinate c : getPointsList()) {
            LatLng temp = new LatLng(c.getLat(), c.getLng());
            Marker marker = googleMap.addMarker(new MarkerOptions().position(temp).title("Ponto " + c.getDescricao()));
            marker.setTag("Ponto " + c.getDescricao());
            if (c.getBorder() == 1) {
                marker.setSnippet("Percurso total: " + format.format(c.getDistanciaIniciandoAqui()) + "m");
            }
        }
    }

    public void obterPontosEPlotar() {
        generatePointsList(getFlag());
        calcularDistanciasPontosBorda(getPointsList());
        atualizarValoresBordaEDistancia();
        plotarPontosDeColeta();
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getmContext());
        builder.setMessage("Confirma seleção?");
        builder.setPositiveButton("Sim", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mPage.getData().putString(Page.SIMPLE_DATA_KEY, getPontoSelecionado().getTitle());
                mPage.notifyDataChanged();
                mensagemPontoConfirmado();
            }
        });

        builder.setNegativeButton("Não", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getmContext(), "Selecione outro ponto no mapa", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void mensagemPontoConfirmado(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getmContext());
        builder.setMessage("Ponto selecionado! \n\nClique em \"Próximo\" para continuar");
        builder.setNeutralButton("OK", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (getPontoSelecionado() == null) {
            setPontoSelecionado(marker);
            Toast.makeText(getmContext(), "Clique novamente para selecionar!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (getPontoSelecionado().getTag().equals(marker.getTag())) {
            showDialog();
        } else {
            setPontoSelecionado(marker);
            Toast.makeText(getmContext(), "Clique novamente para selecionar!", Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}