package com.example.nanodg.tambalban;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.nanodg.tambalban.Adapter.AdapterTambalRecyclerView;
import com.example.nanodg.tambalban.Adapter.CustomInfoWindowAdapter;
import com.example.nanodg.tambalban.Model.Tambah;
import com.example.nanodg.tambalban.Model.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import static com.example.nanodg.tambalban.R.layout.toolbar;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,LocationListener,GoogleMap.OnMarkerClickListener{

    public static final String DATA = "com.example.nanodg.tambalban";
    private GoogleMap mMap;
    DatabaseReference mTambah;
    private ArrayList<Tambah> daftarTambal = new ArrayList<> ();
    private Map<Marker, Integer> markersOrderNumbers = new HashMap<>();
    private ArrayList<Integer> list = new ArrayList<>();
    Marker marker;
    int markerIndex = 0;
    int position = 0 ;
   public double lng,lat;
    public ProgressBar progressBar;
    Button tambah,tampil;
    Switch semua;
    public float jarak=0;
    TextView ban,kendaraan,numradius;
    Spinner spinner1,spinner2;
    String[] jenisban = {"Ayam","Itik"};
    String[] jeniskendaraan = {"Potong","Kampung"};
    String jnban,jnkendaraan;
    String provider = null;
    Marker mCurrentPosition = null;
    LocationManager mLocationManager = null;
    private Location location;
    private Location saatIni = new Location("saatIni");
    private Location lokMarker = new Location("lokMarker");
    private float lingkaran = 0;
    private Circle circle;
    private CircleOptions mOptions;
    SeekBar radius;
    private ActionBar actionBar;
    private String view;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mTambah= FirebaseDatabase.getInstance().getReference();
        mTambah.push().setValue(marker);
        tambah = (Button) findViewById(R.id.tambah);
        tampil = (Button)findViewById(R.id.tampil);
        spinner1 = (Spinner) findViewById(R.id.spinner);
        spinner2 = (Spinner)findViewById(R.id.spinner2);
        ban = (TextView)findViewById(R.id.jenisban);
        semua = (Switch)findViewById(R.id.semua);
        kendaraan = (TextView)findViewById(R.id.jeniskendaraan);
        radius = (SeekBar)findViewById(R.id.radius);
        numradius = (TextView)findViewById(R.id.numradius);
        initToolbar();
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
//        getPermissions();

        progressBar.setVisibility(View.GONE);
        radius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                String radiusValue = String.valueOf(i);

                numradius.setText(radiusValue);
            }
            @Override
            public void onStartTrackingTouch(SeekBar radius) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar radius) {

            }
        });


        lingkaran = Float.valueOf(numradius.getText().toString());
        tambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // kelas yang akan dijalankan ketika tombol Create/Insert Data diklik
                startActivity(LoginUserActivity.getActIntent(MapsActivity.this));
            }
        });

        tampil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // kelas yang akan dijalankan ketika tombol Create/Insert Data diklik
                //refreshmap();
                progressBar.setVisibility(View.VISIBLE);
                lingkaran = Float.valueOf(numradius.getText().toString());
                refreshmap(lat,lng);

            }
        });



        semua.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(semua.isChecked()) {
                    mMap.clear();
                    mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));
                    final MarkerOptions mPosisi = new MarkerOptions();

                    DatabaseReference database;
                    database = FirebaseDatabase.getInstance().getReference();
                    database.child("tambah").orderByChild("nama").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            for (DataSnapshot s : dataSnapshot.getChildren()){
                                final Tambah tambah = s.getValue(Tambah.class);
                                daftarTambal.add(tambah);
                                LatLng location = new LatLng(tambah.getLat(), tambah.getLongt());
                                    mPosisi.position(location);
                                    mPosisi.title(tambah.getNama());
                                    mPosisi.snippet("Alamat : " +tambah.getAlamat() +"\n" +
                                            "Jam Operasional : " +tambah.getBuka()+" s/d "+tambah.getTutup()+"\n"+
                                            "Deskripsi : "+"\n"+tambah.getInfo());
                                    mMap.addMarker(mPosisi);
                                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                                    public void onInfoWindowClick(Marker marker) {
                                        String id = marker.getTitle().toString();
                                        Intent edit = new Intent(getApplicationContext(), DtltambalActivity.class);
                                        edit.putExtra(DATA, id);
                                        startActivity(edit);
                                    }

                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            System.out.println(databaseError.getDetails()+" "+databaseError.getMessage());
                        }
                    });

                }
                else {

                    mMap.clear();
                }
            }
        });

/**
 * =========================================SPINNER
 */
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapsActivity.this, R.layout.simple_list_item,R.id.test, jenisban);
        spinner1.setAdapter(adapter);


        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(MapsActivity.this, R.layout.simple_list_item,R.id.test, jeniskendaraan);
        spinner2.setAdapter(adapter2);

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinner1.getSelectedItem()==("Ayam")){
                    jnban = ("1");
                }
                if (spinner1.getSelectedItem()==("Itik")){
                    jnban = ("2");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinner2.getSelectedItem().equals("Potong")){
                    jnkendaraan = ("1");
                }
                if (spinner2.getSelectedItem().equals("Kampung")){
                    jnkendaraan =("2");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });
/**
 * ==============================================================SPINNER
 */

    }

    /**
     *
     * Permision
     */
//    public void getPermissions() {
//        /* Check and Request permission */
//        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(MapsActivity.this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
//        }if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(MapsActivity.this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
//        }
//    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//
//                } else {
//
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission..
//                    Toast.makeText(MapsActivity.this, "Permission denied to get Account", Toast.LENGTH_SHORT).show();
//
//                }
//                return;
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
//    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (isProviderAvailable() && (provider != null)) {
            locateCurrentPosition();
        }
    }
    private void locateCurrentPosition() {

        int status = getPackageManager().checkPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                getPackageName());

        if (status == PackageManager.PERMISSION_GRANTED) {
            location = mLocationManager.getLastKnownLocation(provider);
            updateWithNewLocation(location);
            mMap.setMyLocationEnabled(true);
            //  mLocationManager.addGpsStatusListener(this);
            long minTime = 5000;// ms
            float minDist = 5.0f;// meter
            mLocationManager.requestLocationUpdates(provider, minTime, minDist,
                    this);
        }
    }


    private boolean isProviderAvailable() {
        mLocationManager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);

        provider = mLocationManager.getBestProvider(criteria, true);
        if (mLocationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;

            return true;
        }

        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
            return true;
        }

        if (provider != null) {
            return true;
        }
        return false;
    }

    private void updateWithNewLocation(Location location) {
        mMap.clear();
        if (location != null && provider != null) {
            lng = location.getLongitude();
            lat = location.getLatitude();

            //addBoundaryToCurrentPosition(lat, lng);


            CameraPosition camPosition = new CameraPosition.Builder()
                    .target(new LatLng(lat, lng)).zoom(getZoomLevel(circle)).build();

            if (mMap != null)
                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(camPosition));
        } else {
            Log.d("Location error", "Something went wrong");
        }
    }

    public int getZoomLevel(Circle circle) {
        int zoomLevel = 11;
        if (circle != null) {
            double radius = circle.getRadius() + circle.getRadius() / 2;
            double scale = radius / 500;
            zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel;
    }


    public void initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
                onBackPressed();

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bantuan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_help) {
            view = "bantuan";
            startActivity(Bantuan.getActIntent(MapsActivity.this));
        } if (item.getItemId() == R.id.menu_cari) {
            view = "cari";
            startActivity(CariTambalActivity.getActIntent(MapsActivity.this));
        }
        return true;
    }

//    private void addBoundaryToCurrentPosition(double lat, double lang) {
//
//        MarkerOptions mMarkerOptions = new MarkerOptions();
//        mMarkerOptions.position(new LatLng(lat, lang));
//        mMarkerOptions.icon(BitmapDescriptorFactory
//                .fromResource(R.drawable.ic_place_black_24dp));
//        mMarkerOptions.anchor(0.5f, 0.5f);
//        mMarkerOptions.title("ini lokasi anda");
//        mMarkerOptions.snippet("anda sekarang ada disini");
//
//        mOptions = new CircleOptions()
//                .center(new LatLng(lat, lang)).radius(lingkaran)
//                .strokeColor(0x110000FF).strokeWidth(1).fillColor(0x110000FF);
//        circle = mMap.addCircle(mOptions);
//        mMap.addCircle(mOptions);
//        if (mCurrentPosition != null)
//            mCurrentPosition.remove();
//        mCurrentPosition = mMap.addMarker(mMarkerOptions);
//    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                break;
            case LocationProvider.AVAILABLE:
                break;
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        updateWithNewLocation(location);
    }
    @Override
    public void onProviderEnabled(String provider) {

    }
    @Override
    public void onProviderDisabled(String provider) {
        updateWithNewLocation(null);
    }
    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
    public static Intent getActIntent(Activity activity) {
        // kode untuk pengambilan Intent
        return new Intent(activity, MapsActivity.class);
    }

    private void refreshmap(final double lat, final double lang){
        progressBar.setVisibility(View.INVISIBLE);
        mMap.clear();
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));
        final DecimalFormat formatDesimal = new DecimalFormat("#.##");
        saatIni.setLatitude(lat);
        saatIni.setLongitude(lang);
        final MarkerOptions mPosisi = new MarkerOptions();
        DatabaseReference  mUserContactsRef =  FirebaseDatabase.getInstance().getReference().child("tambah");
        mUserContactsRef.orderByChild("ban").equalTo(jnban).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                //Log.e("barang1", dataSnapshot.toString());
                for (DataSnapshot s : dataSnapshot.getChildren()){
                    final Tambah tambah = s.getValue(Tambah.class);
                    if(tambah.getKendaraan().equals(jnkendaraan)){

                        daftarTambal.add(tambah);
                        final Object row = (Object) tambah.getNama();
//                    Log.e("Data snapshot","barang1"+daftarTambal);
                        LatLng location = new LatLng(tambah.getLat(), tambah.getLongt());
                        lokMarker.setLatitude(tambah.getLat());
                        lokMarker.setLongitude(tambah.getLongt());
                        jarak = saatIni.distanceTo(lokMarker);
                        if(lingkaran >= jarak){
                            jarak = saatIni.distanceTo(lokMarker) / 1000;
                            mPosisi.position(location);
                            mPosisi.anchor(0.3f, 0.3f);
                            mPosisi.title(tambah.getNama());
                            mPosisi.snippet("Alamat : " +tambah.getAlamat()+" - " + formatDesimal.format(jarak) + " km dari anda" +"\n" +
                                    "Jam Operasional : " +tambah.getBuka()+" s/d "+tambah.getTutup()+"\n"+
                                    "Deskripsi : "+"\n"+tambah.getInfo());
                            mMap.addMarker(mPosisi);
                            //mMap.addMarker(new MarkerOptions().position(location).title(tambah.getNama())).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

                        }

                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                            public void onInfoWindowClick(Marker marker) {
                                String id = marker.getTitle().toString();
                                //Log.e("Data snapshot", "barang45" + id);
                                Intent edit = new Intent(getApplicationContext(), DtltambalActivity.class);
                                //String reference = mMarkerPlaceLink.get(id);
                                //daftarBarang.add(barang);
                                //Integer index = markersOrderNumbers.get(marker);
//                                edit.putExtra("data", daftarTambal.get(index));

                                edit.putExtra(DATA, id);


                                //Log.e("Data snapshot","barang1"+daftarBarang.get(position));
                                //Log.e("Data snapshot","barang2"+daftarBarang);
                                startActivity(edit);

                            }

                        });
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        progressBar.setVisibility(View.INVISIBLE);
        mUserContactsRef.orderByChild("ban").equalTo("3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                //Log.e("barang1", dataSnapshot.toString());
                for (DataSnapshot s : dataSnapshot.getChildren()){
                    final Tambah tambah = s.getValue(Tambah.class);
                    if(tambah.getKendaraan().equals("3")){

                        daftarTambal.add(tambah);
                        final Object row = (Object) tambah.getNama();

//                    Log.e("Data snapshot","barang1"+daftarTambal);
                        LatLng location = new LatLng(tambah.getLat(), tambah.getLongt());
                        lokMarker.setLatitude(tambah.getLat());
                        lokMarker.setLongitude(tambah.getLongt());
                        jarak = saatIni.distanceTo(lokMarker);
                        if(lingkaran >= jarak){
                            jarak = saatIni.distanceTo(lokMarker) / 1000;
                            mPosisi.position(location);
                            mPosisi.anchor(0.3f, 0.3f);
                            mPosisi.title(tambah.getNama());
                            mPosisi.snippet("Alamat : " +tambah.getAlamat()+" - " + formatDesimal.format(jarak) + " km dari anda" +"\n" +
                                    "Jam Operasional : " +tambah.getBuka()+" s/d "+tambah.getTutup()+"\n"+
                                    "Deskripsi : "+"\n"+tambah.getInfo());
                            mMap.addMarker(mPosisi);
                            // mMap.addMarker(new MarkerOptions().position(location).title(tambah.getNama())).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

                        }


                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                            public void onInfoWindowClick(Marker marker) {
                                String id = marker.getTitle().toString();
                               // Log.e("Data snapshot", "barang45" + id);
                                Intent edit = new Intent(getApplicationContext(), DtltambalActivity.class);
                                //String reference = mMarkerPlaceLink.get(id);
                                //daftarBarang.add(barang);
                                //Integer index = markersOrderNumbers.get(marker);
//                                edit.putExtra("data", daftarTambal.get(index));
                                edit.putExtra(DATA, id);


                                //Log.e("Data snapshot","barang1"+daftarBarang.get(position));
                                //Log.e("Data snapshot","barang2"+daftarBarang);
                                startActivity(edit);

                            }

                        });
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        progressBar.setVisibility(View.INVISIBLE);
        mUserContactsRef.orderByChild("ban").equalTo("3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                //Log.e("barang1", dataSnapshot.toString());
                for (DataSnapshot s : dataSnapshot.getChildren()){
                    final Tambah tambah = s.getValue(Tambah.class);
                        if(tambah.getKendaraan().equals(jnkendaraan)) {
                            daftarTambal.add(tambah);
                            final Object row = (Object) tambah.getNama();

//                    Log.e("Data snapshot","barang1"+daftarTambal);
                            LatLng location = new LatLng(tambah.getLat(), tambah.getLongt());
                            lokMarker.setLatitude(tambah.getLat());
                            lokMarker.setLongitude(tambah.getLongt());
                            jarak = saatIni.distanceTo(lokMarker);
                            if (lingkaran >= jarak) {
                                jarak = saatIni.distanceTo(lokMarker) / 1000;
                                mPosisi.position(location);
                                mPosisi.anchor(0.3f, 0.3f);
                                mPosisi.title(tambah.getNama());
                                mPosisi.snippet("Alamat : " +tambah.getAlamat()+" - " + formatDesimal.format(jarak) + " km dari anda" +"\n" +
                                        "Jam Operasional : " +tambah.getBuka()+" s/d "+tambah.getTutup()+"\n"+
                                        "Deskripsi : "+"\n"+tambah.getInfo());
                                mMap.addMarker(mPosisi);
                                // mMap.addMarker(new MarkerOptions().position(location).title(tambah.getNama())).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

                            }

                            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                                public void onInfoWindowClick(Marker marker) {
                                    String id = marker.getTitle().toString();
                                    //Log.e("Data snapshot", "barang45" + id);
                                    Intent edit = new Intent(getApplicationContext(), DtltambalActivity.class);
                                    //String reference = mMarkerPlaceLink.get(id);
                                    //daftarBarang.add(barang);
                                    //Integer index = markersOrderNumbers.get(marker);
//                                edit.putExtra("data", daftarTambal.get(index));
                                    edit.putExtra(DATA, id);


                                    //Log.e("Data snapshot","barang1"+daftarBarang.get(position));
                                    //Log.e("Data snapshot","barang2"+daftarBarang);
                                    startActivity(edit);

                                }

                            });
                        }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        progressBar.setVisibility(View.INVISIBLE);
        mUserContactsRef.orderByChild("kendaraan").equalTo("3").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                //Log.e("barang1", dataSnapshot.toString());
                for (DataSnapshot s : dataSnapshot.getChildren()) {
                    final Tambah tambah = s.getValue(Tambah.class);
                    if (tambah.getBan().equals(jnban)) {
                        daftarTambal.add(tambah);
                        final Object row = (Object) tambah.getNama();

//                    Log.e("Data snapshot","barang1"+daftarTambal);
                        LatLng location = new LatLng(tambah.getLat(), tambah.getLongt());
                        lokMarker.setLatitude(tambah.getLat());
                        lokMarker.setLongitude(tambah.getLongt());
                        jarak = saatIni.distanceTo(lokMarker);
                        if (lingkaran >= jarak) {
                            jarak = saatIni.distanceTo(lokMarker) / 1000;
                            mPosisi.position(location);
                            mPosisi.anchor(0.3f, 0.3f);
                            mPosisi.title(tambah.getNama());
                            mPosisi.snippet("Alamat : " +tambah.getAlamat()+" - " + formatDesimal.format(jarak) + " km dari anda" +"\n" +
                                    "Jam Operasional : " +tambah.getBuka()+" s/d "+tambah.getTutup()+"\n"+
                                    "Deskripsi : "+"\n"+tambah.getInfo());
                            mMap.addMarker(mPosisi);
                            // mMap.addMarker(new MarkerOptions().position(location).title(tambah.getNama())).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

                        }


                        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                            public void onInfoWindowClick(Marker marker) {
                                String id = marker.getTitle().toString();
                                //Log.e("Data snapshot", "barang45" + id);
                                Intent edit = new Intent(getApplicationContext(), DtltambalActivity.class);
                                edit.putExtra(DATA, id);
                                startActivity(edit);

                            }

                        });

                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

}