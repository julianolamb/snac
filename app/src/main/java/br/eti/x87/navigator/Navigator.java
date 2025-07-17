package br.eti.x87.navigator;

import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import br.eti.x87.dialog.FragmentoDialogo;
import br.eti.x87.model.Coordinate;
import br.eti.x87.points.PointsHandler;
import br.eti.x87.snac.R;

public class Navigator extends AppCompatActivity
        implements FragmentoDialogo.NoticeDialogListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnPolylineClickListener, LocationListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private LocationManager locationManager;
    private Criteria criteria;
    private String bestProvider;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private static LatLng currentPosition;
    private static LatLng positionSubject;

    private TextView tvDirecaoProximoPonto;
    private TextView tvDistanciaProximoPonto;

    private TextView tvIdentificacaoProximoPonto;
    private FloatingActionButton fabContinuarDepois;
    private FloatingActionButton fabIniciarNavegacao;
    private FloatingActionButton fabLimparNavegacao;

    private TextView tvPercursoEfetuado;
    private TextView tvPercursoRestante;
    private TextView tvPercursoPlanejado;

    NumberFormat format = new DecimalFormat("0");

    private boolean mLocationPermissionGranted;
    private boolean pontosForamPlotadosUmaVez = false;
    private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;
    public boolean wasInBackground;

    private Marker pontoSelecionado;
    /*
    Aqui uma ressalva, o atributo pointslist foi renomeado para percurso planejado, em função dos
    vários percursos
     */
    private ArrayList<Coordinate> percursoPlanejado;
    private boolean INICIOU_NAVEGACAO = false;
    private boolean INICIOU_SOM = false;
    private ArrayList<LatLng> percursoEfetuado;

    private ArrayList<Marker> markersList = new ArrayList<>();
    private ArrayList<MarkerOptions> listaMarcadores = new ArrayList<>();

    private float distanciaProximoPonto;
    private ArrayList<Integer> tipoPercursoPlanejado;
    private ArrayList<Polyline> linhaPercursoEfetuado = new ArrayList<>();
    private Timer mActivityTransitionTimer;
    private TimerTask mActivityTransitionTimerTask;

    public static LatLng getCurrentPosition() {
        return currentPosition;
    }

    public static LatLng getPositionSubject() {
        return positionSubject;
    }

    public ArrayList<Marker> getMarkersList() {
        return markersList;
    }

    public ArrayList<Coordinate> getPercursoPlanejado() {
        return percursoPlanejado;
    }


    public Marker getPontoSelecionado() {
        return pontoSelecionado;
    }

    public void setPontoSelecionado(Marker pontoSelecionado) {
        this.pontoSelecionado = pontoSelecionado;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();

        percursoEfetuado = new ArrayList<>();
        tipoPercursoPlanejado = new ArrayList<>();

        Bundle b = getIntent().getExtras();
        assert b != null;
        percursoPlanejado = b.getParcelableArrayList("Points");

        setContentView(R.layout.navigator_ac);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        tvPercursoEfetuado = findViewById(R.id.tvPercursoEfetuado);
        tvPercursoPlanejado = findViewById(R.id.tvPercursoPlanejado);
        tvPercursoRestante = findViewById(R.id.tvPercursoRestante);


        fabContinuarDepois = findViewById(R.id.fabContinuarDepois);
        fabIniciarNavegacao = findViewById(R.id.fabIniciarNavegacao);
        fabLimparNavegacao = findViewById(R.id.fabLimparNavegacao);

        fabContinuarDepois.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                INICIOU_NAVEGACAO = false;
                Snackbar.make(v, "Interrompendo ...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        fabIniciarNavegacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                INICIOU_NAVEGACAO = true;
                Snackbar.make(v, "Registrando caminhamento", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        fabLimparNavegacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
                Para limpar o percurso são necessárias duas ações, remover as linhas existestes e,
                esvaziar o vetor, para que as posições antigas não sejam plotadas novamente.
                 */

                for (Polyline polyline : linhaPercursoEfetuado) {
                    polyline.remove();
                }
                percursoEfetuado = new ArrayList<>();
                Snackbar.make(v, "Percurso limpo", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        if (!pontosForamPlotadosUmaVez){
            plotarPontosDeColeta();
            plotarCaminhamento();
            pontosForamPlotadosUmaVez = true;
        }

        mMap.setOnMarkerClickListener(this);
        mMap.setOnPolylineClickListener(this);

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        LatLng ponto1 = new LatLng(percursoPlanejado.get(0).getLat(), percursoPlanejado.get(0).getLng());

        // Estes parâmetros devem ser verificados de modo a posicionar claramente o inicio em função
        // do ponto

        CameraPosition cameraPos = new CameraPosition.Builder().target(ponto1)
                .zoom(45).tilt(1).build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos), null);

    }

    public void onConnected(Bundle connectionHint) {
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }


    private void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            criteria = new Criteria();
            locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);

            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true));
            locationManager.requestLocationUpdates(bestProvider, 100, 0.5f, this);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
//                mMap.setMyLocationEnabled(false);
//                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void plotarPontosDeColeta() {
        for (int i = 0; i < percursoPlanejado.size(); i++) {
            LatLng temp = new LatLng(getPercursoPlanejado().get(i).getLat(), getPercursoPlanejado().get(i).getLng());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(temp)
                    .title("Ponto " + getPercursoPlanejado().get(i).getDescricao())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            marker.setTag("Ponto " + getPercursoPlanejado().get(i).getDescricao());
            getMarkersList().add(marker);
        }
    }

    private void plotarCaminhamento() {
        NumberFormat format = new DecimalFormat("0");

        ArrayList<Polyline> listaLinhas = new ArrayList<>();
        PolylineOptions lineOptions;

        try {
            for (int i = 0; i < percursoPlanejado.size(); i++) {
                LatLng aux1 = new LatLng(percursoPlanejado.get(i).getLat(), percursoPlanejado.get(i).getLng());
                LatLng aux2 = new LatLng(percursoPlanejado.get(i + 1).getLat(), percursoPlanejado.get(i + 1).getLng());
                lineOptions = new PolylineOptions().clickable(true).add(aux1).add(aux2).geodesic(true);
                Polyline polyline = mMap.addPolyline(lineOptions);
                polyline.setTag(i);
                listaLinhas.add(polyline);

                listaMarcadores.add(new MarkerOptions()
                        .position(PointsHandler.encontrarPontoMedio(aux1, aux2))
                        .alpha(0f)
                        .title("" + format.format(percursoPlanejado.get(i).getDistanciaProximoPonto()) + "m"));
            }
        } catch (Exception e) {
            //Coloquei exceção para quando acessar a posição i+1
        }
    }

    public void showDialog() {
        DialogFragment dialog = new FragmentoDialogo();
        dialog.show(getFragmentManager(), "FragmentoDialogo");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        int pos = 0;
        for (int i = 0; i < getMarkersList().size(); i++) {
            if (getMarkersList().get(i).getTag().equals(getPontoSelecionado().getTag())) {
                getMarkersList().get(i).setSnippet("Coleta efetuada");
                getMarkersList().get(i).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                getMarkersList().get(i).showInfoWindow();
                pos = i;

            }
        }

        try {
            Location source = new Location("");
            source.setLatitude(getMarkersList().get(pos).getPosition().latitude);
            source.setLongitude(getMarkersList().get(pos).getPosition().longitude);

            Location target = new Location("");
            target.setLatitude(getMarkersList().get(pos + 1).getPosition().latitude);
            target.setLongitude(getMarkersList().get(pos + 1).getPosition().longitude);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(midPoint(getMarkersList().get(pos).getPosition().latitude, getMarkersList().get(pos).getPosition().longitude,
                            getMarkersList().get(pos + 1).getPosition().latitude, getMarkersList().get(pos + 1).getPosition().longitude))
                    .zoom(19)
                    /*.bearing(angleBteweenCoordinate(getMarkersList().get(pos).getPosition().latitude, getMarkersList().get(pos).getPosition().longitude,
                            getMarkersList().get(pos+1).getPosition().latitude, getMarkersList().get(pos+1).getPosition().longitude))*/
                    .bearing(source.bearingTo(target))
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } catch (Exception e){
            //TODO
        }

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Toast.makeText(getBaseContext(), "Ponto não confirmado", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast toast = Toast.makeText(getApplicationContext(),
                "Clique novamente para confirmar!", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 30);
        /*
        Na primeira vez em que o usuário clicar em um ponto do mapa, o método getPontoSelecionado()
        trará um valor nulo, pois ele não clicou em nenhum ponto anteriormente. Atribui-se então
        o valor do marcador ao atributo 'pontoSelecionado'. Depois, compara-se para ver se é o mesmo
        ponto que é clicado ou não.
         */
        if (getPontoSelecionado() == null) {
            setPontoSelecionado(marker);
            toast.show();

            //Toast.makeText(getBaseContext(), "Clique novamente para confirmar!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (getPontoSelecionado().getTag().equals(marker.getTag())) {
            showDialog();
        } else {
            setPontoSelecionado(marker);
            toast.show();
        }
        return false;
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        int tag = (Integer) polyline.getTag();
        mMap.addMarker(listaMarcadores.get(tag)).showInfoWindow();
    }

    private LatLng midPoint(double lat1, double long1, double lat2, double long2) {
        return new LatLng((lat1 + lat2) / 2, (long1 + long2) / 2);
    }

    private float angleBteweenCoordinate(double lat1, double long1, double lat2, double long2) {
        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;
        brng = 360 - brng;

        return (float) brng;
    }

    public static String headingToString2(double x){
        String directions[] = {"N", "NO", "O", "SO", "S", "SE", "E", "NE"};
        return directions[ (int)Math.round((  ((double)x % 360) / 45)) % 8 ];
    }


    public void atualizarPainelProximoPonto(Marker posicaoDestino, LatLng posicaoAtual) {
        /*
        Exibindo a direção cardinal do próximo ponto
         */
        String s = headingToString2(angleBteweenCoordinate(posicaoAtual.latitude, posicaoAtual.longitude,
                posicaoDestino.getPosition().latitude, posicaoDestino.getPosition().longitude));
        tvDirecaoProximoPonto = (TextView) findViewById(R.id.tvDirecaoProximoPonto);
        tvDirecaoProximoPonto.setText(": " + s);

        /*
        Exibindo o nome do próximo ponto
         */
        int posicaoSubject = findSubject();
        tvIdentificacaoProximoPonto = (TextView) findViewById(R.id.tvIdentificacaoProximoPonto);
        tvIdentificacaoProximoPonto.setText(": " + getMarkersList().get(posicaoSubject).getTitle());

        /*
        Exibindo a distância para o próximo ponto, em metros
         */
        NumberFormat format = new DecimalFormat("0");
        distanciaProximoPonto = PointsHandler.calcularDistancia(posicaoAtual, posicaoDestino.getPosition());
        tvDistanciaProximoPonto = (TextView) findViewById(R.id.tvDistanciaProximoPonto);
        tvDistanciaProximoPonto.setText(": " + format.format(distanciaProximoPonto) + "m");

    }

    @Override
    public void onLocationChanged(Location location) {

        /*
        Localizando a posição do primeiro alvo e a posição atual
         */
        Marker subject = getMarkersList().get(findSubject());

        currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        positionSubject = new LatLng(subject.getPosition().latitude, subject.getPosition().longitude);

        if (!INICIOU_SOM) {
            new BeepSound().execute("");
        }

        atualizarPainelProximoPonto(subject, currentPosition);
        /*
        Adicionando a posição atual em uma lista e plotando o percurso caminhado
        NOTE: só ira imprimir no percurso se o usuário estiver com a posição proxima do alvo
         */
        if (INICIOU_NAVEGACAO) {
            percursoEfetuado.add(currentPosition);
            redrawLine();
        }
        atualizarPainelPercursos(currentPosition);
    }

    private void redrawLine(){
        PolylineOptions options = new PolylineOptions().width(5).color(Color.RED).geodesic(true);

        for (int i = 0; i < percursoEfetuado.size(); i++) {
            LatLng point = percursoEfetuado.get(i);
            options.add(point);
        }
        linhaPercursoEfetuado.add(mMap.addPolyline(options));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    public int findSubject() {
        int last = -1, size;

        size = getMarkersList().size();
        for (int i = 0; i < size; i++) {
            try {
                if (getMarkersList().get(i).getSnippet().equals("Coleta efetuada")) {
                    last = i;
                }
            } catch (Exception e){
                //Exceção quando não tiver snippet (NULO)
            }
        }

        if (last == -1)
            return 0;  //percorreu todos e nenhum tem snippet

        if (last == size)
            return -1; //percorreu todos e todos tem snippet

        if ((last > -1) && (last < size))
            return last + 1;

        return -1;
    }

    public void atualizarPainelPercursos(LatLng posicaoAtual) {
        String pEfetuado = ": 0m";
        String pRestante = ": 0m";
        String pPlanejado = ": " + format.format(PointsHandler.calcularDistanciaTotal(percursoPlanejado)) + "m";

        if (INICIOU_NAVEGACAO) {
            float percEfetuado = PointsHandler.calcularDistanciaTotal(PointsHandler.convertLatLng2Coordinate(percursoEfetuado));
            float percRestante = estimarDistanciaRestante(posicaoAtual);

            pEfetuado = ": " + format.format(percEfetuado) + "m";
            pRestante = ": " + format.format(percRestante) + "m";
        }

        tvPercursoEfetuado.setText(pEfetuado);
        tvPercursoRestante.setText(pRestante);
        tvPercursoPlanejado.setText(pPlanejado);
    }

    public float estimarDistanciaRestante(LatLng posicaoAtual){
        int posicaoProximoPontoEmColeta = findSubject();
        int inicio = posicaoProximoPontoEmColeta;

        if (posicaoProximoPontoEmColeta == -1)
            inicio = 0;  //percorreu todos e nenhum tem snippet

        if (posicaoProximoPontoEmColeta == getMarkersList().size())
            return -1; //percorreu todos e todos tem snippet

        LatLng primeiroPonto = new LatLng(getPercursoPlanejado().get(inicio).getLat(),getPercursoPlanejado().get(inicio).getLng());
        float distanciaPrimeiroPonto = PointsHandler.calcularDistancia(posicaoAtual, primeiroPonto);
        float aux = 0;

        for (int i = inicio; i < getPercursoPlanejado().size(); i++) {
            try {
                aux += getPercursoPlanejado().get(i).getDistanciaProximoPonto();
            } catch (Exception e){
                Log.d("REM", "Tentando acessar posição inexistente");
            }
        }

        float percursoRestante = distanciaPrimeiroPonto + aux;

        return percursoRestante;
    }


    @Override
    public void onPause() {
        super.onPause();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, this.NOTIFICATION_SERVICE)
                .setSmallIcon(R.drawable.ic_error_outline_black_24dp)
                .setContentTitle("SNAC")
                .setContentText("Aviso: O sistema ainda está em execução")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, mBuilder.build());

        INICIOU_NAVEGACAO = false;
        this.startActivityTransitionTimer();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    @Override
    public void onResume() {
        super.onResume();

        Application myApp = this.getApplication();

        if (wasInBackground) {
            INICIOU_NAVEGACAO = true;

            int posicaoSubject = findSubject();
            String proxPonto = getMarkersList().get(posicaoSubject).getTitle();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Bem vindo de volta! \nEstamos procurando o \"" + proxPonto + "\".");
            builder.setNeutralButton("OK", new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.show();
        }

        this.stopActivityTransitionTimer();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public void startActivityTransitionTimer() {
        this.mActivityTransitionTimer = new Timer();
        this.mActivityTransitionTimerTask = new TimerTask() {
            public void run() {
                Navigator.this.wasInBackground = true;
            }
        };

        this.mActivityTransitionTimer.schedule(mActivityTransitionTimerTask,
                MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    public void stopActivityTransitionTimer() {
        if (this.mActivityTransitionTimerTask != null) {
            this.mActivityTransitionTimerTask.cancel();
        }

        if (this.mActivityTransitionTimer != null) {
            this.mActivityTransitionTimer.cancel();
        }

        this.wasInBackground = false;
    }


    private static class BeepSound extends AsyncTask<String, Float, String> {

        ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        Handler handler = new Handler();
        private float distancia = 0;
        private int TEMPO_ATUALIZACAO = 500;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... strings) {
            while (true) {
                try {
                    Thread.sleep(1000);
                    distancia = PointsHandler.calcularDistancia(getCurrentPosition(), getPositionSubject());
                    publishProgress(distancia);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return "";
                }
            }
        }

        @Override
        protected void onProgressUpdate(Float... values) {
            final float dist = values[0];
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d("Progress", "Distancia " + dist);
                    toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 100);

                }
            }, 100);


            // update the UI with Data received from publishprogress
        }
    }

}


