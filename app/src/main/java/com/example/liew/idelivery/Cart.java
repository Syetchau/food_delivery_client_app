package com.example.liew.idelivery;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.NumberFormat;
import android.location.Location;
import android.net.ParseException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.liew.idelivery.Common.Common;
import com.example.liew.idelivery.Database.Database;
import com.example.liew.idelivery.Helper.RecyclerItemTouchHelper;
import com.example.liew.idelivery.Interface.RecyclerItemTouchHelperListener;
import com.example.liew.idelivery.Model.MyResponse;
import com.example.liew.idelivery.Model.Notification;
import com.example.liew.idelivery.Model.Order;
import com.example.liew.idelivery.Model.Request;
import com.example.liew.idelivery.Model.Sender;
import com.example.liew.idelivery.Model.Token;
import com.example.liew.idelivery.Model.User;
import com.example.liew.idelivery.Remote.APIService;
import com.example.liew.idelivery.Remote.IGoogleService;
import com.example.liew.idelivery.ViewHolder.CartAdapter;
import com.example.liew.idelivery.ViewHolder.CartViewHolder;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import info.hoang8f.widget.FButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Cart extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, RecyclerItemTouchHelperListener {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    public TextView txtTotalPrice;
    FButton btnPlace;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;

    Place shippingAddress;

    String address;

    //location
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;
    private static final int LOCATION_REQUEST_CODE = 9999;
    private static final int PLAY_SERVICES_REQUEST = 9997;

    //Declare Google Map API Retrofit
    IGoogleService mGoogleMapService;
    APIService mService;

    //declare root layout
    RelativeLayout rootLayout;

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

        setContentView(R.layout.activity_cart);


        //Runtime permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED))
        {
            ActivityCompat.requestPermissions(this, new String[]
                    {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },LOCATION_REQUEST_CODE);
        }else
        {
            if (checkPlayServices()) //if have play service on device
            {
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        //init google map service
        mGoogleMapService = Common.getGoogleMapAPI();

        //init rootlayout
        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);

        //Init service
        mService = Common.getFCMService();

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //Init
        recyclerView = (RecyclerView)findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        txtTotalPrice = (TextView)findViewById(R.id.total);
        btnPlace = (FButton)findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (cart.size() > 0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this, "Your cart is empty.", Toast.LENGTH_SHORT).show();

            }
        });

        loadListFood();
    }

    private void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_REQUEST).show();
            else
            {
                Toast.makeText(this, "This device is not supported.", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }

    private void showAlertDialog(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address: ");

           LayoutInflater inflater = this.getLayoutInflater();
           View order_address_comment = inflater.inflate(R.layout.order_address_comment, null);

           final PlaceAutocompleteFragment edtAddress = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

           //Hide search icon before fragment
           edtAddress.getView().findViewById(R.id.place_autocomplete_search_button).setVisibility(View.GONE);

           //set hint for Autocomplete EditText
          ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setHint("Enter your address");

          //set text size
          ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(14);

          //get address from place autocomplete
           edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener() {
               @Override
               public void onPlaceSelected(Place place) {
                   shippingAddress = place;
               }

               @Override
               public void onError(Status status) {
                   Log.e("ERROR",status.getStatusMessage());
               }
           });

          final MaterialEditText edtComment = (MaterialEditText)order_address_comment.findViewById(R.id.edtComment);

          //radio button
        final RadioButton rdyShipToAddress = (RadioButton)order_address_comment.findViewById(R.id.rdyShipToAddress);
        final RadioButton rdyHomeAddress = (RadioButton)order_address_comment.findViewById(R.id.rdyHomeAddress);
        final RadioButton cashOnDelivery = (RadioButton)order_address_comment.findViewById(R.id.cashOnDelivery);


        //radio event
        rdyHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){

                    if (Common.currentUser.getHomeAddress() != null ||
                            !TextUtils.isEmpty(Common.currentUser.getHomeAddress()))
                    {
                        address = Common.currentUser.getHomeAddress();
                        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                .setText(address);
                    }
                    else
                    {
                        Toast.makeText(Cart.this, "Please Update Home Address!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        rdyShipToAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //ship to this address feature
                if (isChecked){

                    mGoogleMapService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?latlng="+mLastLocation.getLatitude()+","+mLastLocation.getLongitude()+"&sensor=false&key=AIzaSyBW3rhW1EhjhW36DmMyoTTBup4E6Gu1LCY",
                            mLastLocation.getLatitude(),
                            mLastLocation.getLongitude()))
                            .enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    // if fetch API ok
                                    try {
                                        JSONObject jsonObject = new JSONObject(response.body().toString());

                                        JSONArray resultArray = jsonObject.getJSONArray("results");

                                        JSONObject firstObject = resultArray.getJSONObject(0);

                                        address = firstObject.getString("formatted_address");

                                        //set this address to edtAddress
                                        ((EditText)edtAddress.getView().findViewById(R.id.place_autocomplete_search_input))
                                                .setText(address);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    Toast.makeText(Cart.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

           alertDialog.setView(order_address_comment);
           alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
           alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {

                   //add check condition here
                   //if user select address from place fragment just use it
                   //if user select ship to this address, get address from location and use it
                   //if user select home address, get homeaddress from profile and use it
                   if(!rdyShipToAddress.isChecked() && !rdyHomeAddress.isChecked()){
                       if (shippingAddress!=null)
                          address = shippingAddress.getAddress().toString();
                       else {
                           Toast.makeText(Cart.this, "Please enter address or select option address", Toast.LENGTH_SHORT).show();

                           //Fix crash fragment
                           getFragmentManager().beginTransaction()
                                   .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                                   .commit();

                           return;
                       }
                   }

                   if (TextUtils.isEmpty(address)){
                       Toast.makeText(Cart.this, "Please enter address or select option address", Toast.LENGTH_SHORT).show();

                       //Fix crash fragment
                       getFragmentManager().beginTransaction()
                               .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                               .commit();

                       return;
                   }

                   //check payment
                   if (!cashOnDelivery.isChecked())
                   {
                       Toast.makeText(Cart.this, "Please select Payment option", Toast.LENGTH_SHORT).show();

                       //Fix crash fragment
                       getFragmentManager().beginTransaction()
                               .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                               .commit();

                       return;
                   }
                   else if (cashOnDelivery.isChecked())
                   {
                       //create new request
                       Request request = new Request(
                               Common.currentUser.getPhone(),
                               Common.currentUser.getName(),
                               address,
                               txtTotalPrice.getText().toString(),
                               "0",
                               edtComment.getText().toString(),
                               "Cash On Delivery",
                               String.format("%s,%s",mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                               cart
                       );

                       //submit to firebase
                       String order_number = String.valueOf(System.currentTimeMillis());
                       requests.child(order_number).setValue(request);

                       //delete cart
                       new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());

                       sendNotification(order_number);
                   }
               }
           });

           alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                   dialog.dismiss();

                   //remove fragment
                   getFragmentManager().beginTransaction()
                           .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                           .commit();
               }
           });


        alertDialog.show();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case LOCATION_REQUEST_CODE:
            {
                if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (checkPlayServices())
                    {
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }
            }
            break;
        }
    }

    private void sendNotification(final String order_number) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");

        //get all node with isServerToken is true
        Query data = tokens.orderByChild("serverToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren()){

                    Token serverToken = postSnapShot.getValue(Token.class);

                    //create raw payload to send
                    Notification notification = new Notification("iDelivery", "You have new order " + order_number);
                    Sender content = new Sender(serverToken.getToken(), notification);

                    mService.sendNotification(content).enqueue(new Callback<MyResponse>() {
                        @Override

                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            //only run when get result
                            if (response.code() == 200) {
                                if (response.body().success == 1) {
                                    Toast.makeText(Cart.this, "Thank you, Order placed.", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(Cart.this, "Failed to place order.", Toast.LENGTH_SHORT).show();

                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {
                            Log.e("ERROR", t.getMessage());
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadListFood() {

        cart = new Database(this).getCarts(Common.currentUser.getPhone());
        adapter = new CartAdapter(cart,this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //calculation total price
        float total = 0;
        for(Order order:cart)
            total +=(Float.parseFloat(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en","MY");
        java.text.NumberFormat fmt = java.text.NumberFormat.getCurrencyInstance(locale);
        txtTotalPrice.setText(fmt.format(total));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {

        //remove item at List<Order> by position
        cart.remove(position);

        //after that,delete all old data from SQLite
        new Database(this).cleanCart(Common.currentUser.getPhone());

        //final,update new data from List<Order> to SQLite
        for (Order item:cart)
            new Database(this).addToCart(item);

        //refresh
        loadListFood();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED))
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED))
        {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null)
        {
           Log.d("LOCATION", "Your location : " + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
        }
        else
        {
            Log.d("LOCATION", "Could not get your location.");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        displayLocation();
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartViewHolder)
        {
            String name = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            final Order deleteItem = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());

            final int deleteIndex = viewHolder.getAdapterPosition();
            adapter.removeItem(deleteIndex);

            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(), Common.currentUser.getPhone());

            //update txttotal
            //calculation total price
            float total = 0;
            List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for(Order item:orders)
                total +=(Float.parseFloat(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("en","MY");
            java.text.NumberFormat fmt = java.text.NumberFormat.getCurrencyInstance(locale);
            txtTotalPrice.setText(fmt.format(total));

            //snackbar
            Snackbar snackbar = Snackbar.make(rootLayout,name + " removed from cart!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);

                    //update txttotal
                    //calculation total price
                    float total = 0;
                    List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for(Order item:orders)
                        total +=(Float.parseFloat(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
                    Locale locale = new Locale("en","MY");
                    java.text.NumberFormat fmt = java.text.NumberFormat.getCurrencyInstance(locale);
                    txtTotalPrice.setText(fmt.format(total));
                }
            });
            snackbar.setActionTextColor(Color.RED);
            snackbar.show();
        }
    }
}

