package com.example.liew.idelivery;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.liew.idelivery.Common.Common;
import com.example.liew.idelivery.Common.DirectionJSONParser;
import com.example.liew.idelivery.Model.Request;
import com.example.liew.idelivery.Model.ShippingInformation;
import com.example.liew.idelivery.Remote.IGoogleService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dmax.dialog.SpotsDialog;
import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TrackingOrder extends FragmentActivity implements OnMapReadyCallback,ValueEventListener {

    private GoogleMap mMap;
    private static final int REQUEST_PHONE_CALL =1;

    FirebaseDatabase database;
    DatabaseReference request,shippingOrder;

    Request currentOrder;

    IGoogleService mService;

    Marker shippingMarker;
    Polyline polyline;

    TextView distance,duration,time;
    FButton btn_call;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //add calligraphy
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        setContentView(R.layout.activity_tracking_order);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        database = FirebaseDatabase.getInstance();
        request = database.getReference("Requests");
        shippingOrder = database.getReference("ShippingOrders");
        shippingOrder.addValueEventListener(this);

        mService = Common.getGoogleMapAPI();
        distance = (TextView)findViewById(R.id.display_distance);
        duration = (TextView)findViewById(R.id.display_duration);
        time = (TextView)findViewById(R.id.display_expected_hour);
        btn_call = (FButton) findViewById(R.id.btnCall);

      btn_call.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              shippingOrder.child(Common.currentKey).addListenerForSingleValueEvent(new ValueEventListener() {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                      ShippingInformation shippingInformation = dataSnapshot.getValue(ShippingInformation.class);
                      Intent intent = new Intent(Intent.ACTION_DIAL);
                      intent.setData(Uri.parse("tel:" + shippingInformation.getShipperPhone()));
                      if (ActivityCompat.checkSelfPermission(TrackingOrder.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                          ActivityCompat.requestPermissions(TrackingOrder.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
                      }


                      startActivity(intent);
                  }

                  @Override
                  public void onCancelled(@NonNull DatabaseError databaseError) {

                  }
              });
          }
      });
    }

    @Override
    protected void onStop() {
        shippingOrder.removeEventListener(this);
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        trackingLocation();
    }

    private void trackingLocation() {
        request.child(Common.currentKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        currentOrder = dataSnapshot.getValue(Request.class);
                        //if order has address
                        if (currentOrder.getAddress() != null && !currentOrder.getAddress().isEmpty())
                        {
                            mService.getLocationFromAddress(new StringBuilder
                            ("https://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyBW3rhW1EhjhW36DmMyoTTBup4E6Gu1LCY&address=")
                            .append(currentOrder.getAddress()).toString())
                                    .enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {
                                            try
                                            {
                                                JSONObject jsonObject = new JSONObject(response.body());

                                                String lat = ((JSONArray) jsonObject.get("results"))
                                                        .getJSONObject(0)
                                                        .getJSONObject("geometry")
                                                        .getJSONObject("location")
                                                        .get("lat").toString();

                                                String lng = ((JSONArray) jsonObject.get("results"))
                                                        .getJSONObject(0)
                                                        .getJSONObject("geometry")
                                                        .getJSONObject("location")
                                                        .get("lng").toString();

                                                final LatLng location = new LatLng(Double.parseDouble(lat),
                                                        Double.parseDouble(lng));

                                                mMap.addMarker(new MarkerOptions().position(location)
                                                        .title("Order Destination")
                                                        .icon(BitmapDescriptorFactory.defaultMarker()));

                                                //set shipper location
                                                shippingOrder.child(Common.currentKey)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                                ShippingInformation shippingInformation = dataSnapshot.getValue(ShippingInformation.class);

                                                                LatLng shipperLocation = new LatLng(shippingInformation.getLat(),shippingInformation.getLng());
                                                                if (shippingMarker == null)
                                                                {
                                                                    shippingMarker = mMap.addMarker(new MarkerOptions()
                                                                                    .position(shipperLocation)
                                                                                    .title("Shipper #" + shippingInformation.getOrderId())
                                                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                                                    );
                                                                }
                                                                else
                                                                {
                                                                    shippingMarker.setPosition(shipperLocation);
                                                                }

                                                                //update camera
                                                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                                                        .target(shipperLocation)
                                                                        .zoom(16)
                                                                        .bearing(0)
                                                                        .tilt(45)
                                                                        .build();

                                                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                                                //draw route
                                                                if (polyline != null)
                                                                    polyline.remove();
                                                                mService.getDirections(shipperLocation.latitude
                                                                        + "," + shipperLocation.longitude, currentOrder.getAddress())
                                                                        .enqueue(new Callback<String>() {
                                                                            @Override
                                                                            public void onResponse(Call<String> call, Response<String> response) {
                                                                                new ParserTask().execute(response.body().toString());
                                                                            }

                                                                            @Override
                                                                            public void onFailure(Call<String> call, Throwable t) {

                                                                            }
                                                                        });
                                                             }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        });


                                            } catch (JSONException e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {

                                        }
                                    });
                        }
                        //if order has latlng
                        else if (currentOrder.getLatLng() != null && !currentOrder.getLatLng().isEmpty())
                        {
                            mService.getLocationFromAddress(new StringBuilder
                                    ("https://maps.googleapis.com/maps/api/geocode/json?key=AIzaSyBW3rhW1EhjhW36DmMyoTTBup4E6Gu1LCY&latlng=")
                                    .append(currentOrder.getLatLng()).toString())
                                    .enqueue(new Callback<String>() {
                                        @Override
                                        public void onResponse(Call<String> call, Response<String> response) {
                                            try
                                            {
                                                JSONObject jsonObject = new JSONObject(response.body());

                                                String lat = ((JSONArray) jsonObject.get("results"))
                                                        .getJSONObject(0)
                                                        .getJSONObject("geometry")
                                                        .getJSONObject("location")
                                                        .get("lat").toString();

                                                String lng = ((JSONArray) jsonObject.get("results"))
                                                        .getJSONObject(0)
                                                        .getJSONObject("geometry")
                                                        .getJSONObject("location")
                                                        .get("lng").toString();

                                                final LatLng location = new LatLng(Double.parseDouble(lat),
                                                        Double.parseDouble(lng));

                                                mMap.addMarker(new MarkerOptions().position(location)
                                                        .title("Order Destination")
                                                        .icon(BitmapDescriptorFactory.defaultMarker()));

                                                //set shipper location
                                                shippingOrder.child(Common.currentKey)
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                                ShippingInformation shippingInformation = dataSnapshot.getValue(ShippingInformation.class);

                                                                LatLng shipperLocation = new LatLng(shippingInformation.getLat(),shippingInformation.getLng());
                                                                if (shippingMarker == null)
                                                                {
                                                                    shippingMarker = mMap.addMarker(new MarkerOptions()
                                                                            .position(shipperLocation)
                                                                            .title("Shipper #" + shippingInformation.getOrderId())
                                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                                                                    );
                                                                }
                                                                else
                                                                {
                                                                    shippingMarker.setPosition(shipperLocation);
                                                                }

                                                                //update camera
                                                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                                                        .target(shipperLocation)
                                                                        .zoom(16)
                                                                        .bearing(0)
                                                                        .tilt(45)
                                                                        .build();

                                                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                                                //draw route
                                                                if (polyline != null)
                                                                    polyline.remove();
                                                                mService.getDirections(shipperLocation.latitude
                                                                        + "," + shipperLocation.longitude, currentOrder.getLatLng())
                                                                        .enqueue(new Callback<String>() {
                                                                            @Override
                                                                            public void onResponse(Call<String> call, Response<String> response) {
                                                                                new ParserTask().execute(response.body().toString());
                                                                            }

                                                                            @Override
                                                                            public void onFailure(Call<String> call, Throwable t) {

                                                                            }
                                                                        });
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                            }
                                                        });


                                            } catch (JSONException e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<String> call, Throwable t) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        trackingLocation();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        AlertDialog mDialog = new SpotsDialog( TrackingOrder.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
            mDialog.setMessage("Please waiting...");

        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {

            JSONObject jsonObject;

            List<List<HashMap<String, String>>> routes = null;

            try {
                jsonObject = new JSONObject(strings[0]);

                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();
            distance.setText(Common.DISTANCE);
            duration.setText(Common.DURATION);
            time.setText(Common.ESTIMATED_TIME);

            ArrayList points = null;
            PolylineOptions lineOptions = null;

            for (int i = 0; i < lists.size(); i++) {

                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = lists.get(i);

                for (int j = 0; j < path.size(); j++) {

                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));

                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);
            }

            polyline = mMap.addPolyline(lineOptions);
        }
    }
}
