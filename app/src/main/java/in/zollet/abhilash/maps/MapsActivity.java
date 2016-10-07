package in.zollet.abhilash.maps;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Build;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.server.converter.StringToIntConverter;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import java.util.ArrayList;

import in.zollet.abhilash.maps.data.LocationColumns;

import static java.security.AccessController.getContext;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private static final int LOCATION_PERMISSIONS_REQUEST = 100;
    private static final int PLACE_PICKER_REQUEST = 500;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private boolean placePicker = false;
    Marker current;
    Polyline polyline;
    private LocationRequest mLocationRequest;
    private static int UPDATE_INTERVAL = 120000; // 2min
    private static int FATEST_INTERVAL = 50000; // 50 sec
    private static int DISPLACEMENT = 500; // 0.5 km

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        createLocationRequest();
        setContentView(R.layout.activity_maps);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_map);
        final PlacePicker.IntentBuilder PlaceBuilder = new PlacePicker.IntentBuilder();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (!placePicker) {
                        startActivityForResult(PlaceBuilder.build(MapsActivity.this), PLACE_PICKER_REQUEST);
                        placePicker = true;
                    }
                } catch (GooglePlayServicesRepairableException e) {
                    GooglePlayServicesUtil
                            .getErrorDialog(e.getConnectionStatusCode(), MapsActivity.this, 0);
                } catch (GooglePlayServicesNotAvailableException e) {
                    Toast.makeText(getApplicationContext(), "Google Play Services is not available.",
                            Toast.LENGTH_LONG)
                            .show();
                }


            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            placePicker = false;
            if (resultCode == RESULT_OK) {
                Place selectedPlace = PlacePicker.getPlace(data, this);
                ContentValues values = new ContentValues();

                values.put(LocationColumns.NAME, String.valueOf(selectedPlace.getName()));
                values.put(LocationColumns.LATITUDE, String.valueOf(selectedPlace.getLatLng().latitude));
                values.put(LocationColumns.LONGITUDE, String.valueOf(selectedPlace.getLatLng().longitude));
                ArrayList<ContentProviderOperation> loc = new ArrayList<ContentProviderOperation>();
                loc.add(ContentProviderOperation.newInsert(in.zollet.abhilash.maps.data.LocationProvider.Location.CONTENT_URI).withValues(values).build());


                try {
                    this.getContentResolver().applyBatch(in.zollet.abhilash.maps.data.LocationProvider.AUTHORITY, loc);
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }
                LatLng locc = new LatLng(selectedPlace.getLatLng().latitude,
                        selectedPlace.getLatLng().longitude);
                mMap.addMarker(new MarkerOptions().position(locc).title(String.valueOf(selectedPlace.getName())))
                        .setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_beenhere_black_24dp)));
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
                {

                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        showJourneyDialog(marker);
                        return true;
                    }

                });
            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSIONS_REQUEST);
            }

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
    private Location getLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSIONS_REQUEST);
            }

            return null;
        } else {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            // progressBar_current_location.setVisibility(View.GONE);

            return location;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSIONS_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(mGoogleApiClient.isConnected()){
                    showCurrentLocation();
                } else
                    mGoogleApiClient.connect();
            }
            }


             else {
            Toast.makeText(this, "Location Permission Required", Toast.LENGTH_SHORT).show();
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void showCurrentLocation() {
        Location location = getLocation();
        if (location != null) {
            if (current != null)
                current.remove();
            LatLng currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
            current = mMap.addMarker(new MarkerOptions().position(currentLoc));
            current.setTitle("This is your Location");
            current.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_run_black_24dp)));
            float zoomLevel = (float) 14.0;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, zoomLevel), 200, null);
            Cursor cursor = this.getContentResolver().query
                    (in.zollet.abhilash.maps.data.LocationProvider.Location.CONTENT_URI, null, null, null, null);
            while (cursor.moveToNext()) {


                LatLng addedLoc = new LatLng(Double.parseDouble(cursor.getString(cursor.getColumnIndex(LocationColumns.LATITUDE))),
                        Double.parseDouble(cursor.getString(cursor.getColumnIndex(LocationColumns.LONGITUDE))));
                mMap.addMarker(new MarkerOptions().position(addedLoc).title(cursor.getString(cursor.getColumnIndex(LocationColumns.NAME))))
                        .setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_beenhere_black_24dp)));
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        showJourneyDialog(marker);
                        return true;
                    }

                });
            }

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSIONS_REQUEST);
            } else
                Toast.makeText(this, "OOPS!!. Something went wrong", Toast.LENGTH_SHORT).show();
        }
    }
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private void showJourneyDialog(final Marker marker) {

        if(polyline != null)
        polyline.remove();
        stopLocationUpdates();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Start?");

        alertDialog.setMessage("Start for this Destination");

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startLocationUpdates();
                PolylineOptions polylineOptions = new PolylineOptions()
                        .add(new LatLng(getLocation().getLatitude(),getLocation().getLongitude()))
                .add(new LatLng(marker.getPosition().latitude,marker.getPosition().longitude)).
                                width(5).color(Color.GRAY).geodesic(true);

                polyline = mMap.addPolyline(polylineOptions);
            }
        });

        alertDialog.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

       showCurrentLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if(current != null){
            current.remove();

        }

        double latitude = location.getLatitude();

        double longitude = location.getLongitude();

        LatLng latLng = new LatLng(latitude, longitude);
        current = mMap.addMarker(new MarkerOptions().position(latLng));
        current.setTitle("This is your Location");
        current.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_run_black_24dp)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }
}
