package com.raf.johnvrachnis.firespot;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import com.android.volley.*;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.raf.johnvrachnis.firespot.ui.login.LoginActivity;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;
import com.raf.johnvrachnis.firespot.data.*;
import java.lang.Long;
import android.widget.Toast;
import android.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import static com.google.common.base.Predicates.notNull;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnMarkerClickListener
         {
    String userid="trs";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<Circle> circles = new ArrayList<Circle>();
    ArrayList<Marker> markers = new ArrayList<Marker>();
    ArrayList<String> ids = new ArrayList<String>();
    private GoogleMap mMap;
    Fragment map;
    private boolean map_loaded = false;
    LatLng latLng = null;
    LoginRepository User;
    LocationManager locationManager;
    public static int MY_PERMITIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static int MY_PERMITIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    public static int MY_PERMITIONS_REQUEST_INTERNET = 1;
    final Context context = this;
    private static int CAMERA_PIC_REQUEST =1337;
    private static int LOGIN_USER_REQUEST =1000;
    private static int POST_FORM_REQUEST =666;
    boolean logged_in=false;
    boolean vol=true;
    Button volunteer,login;
    public String getPhoneStatus() {
        TelephonyManager tm =(TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},1);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
        String str = "";
        str += "DeviceId(IMEI) = " + tm.getDeviceId() + "\n";
        str += "DeviceSoftwareVersion = " + tm.getDeviceSoftwareVersion() + "\n";
        str += "Line1Number = " + tm.getLine1Number() + "\n";
        str += "NetworkCountryIso = " + tm.getNetworkCountryIso() + "\n";
        str += "NetworkOperator = " + tm.getNetworkOperator() + "\n";
        str += "NetworkOperatorName = " + tm.getNetworkOperatorName() + "\n";
        str += "NetworkType = " + tm.getNetworkType() + "\n";
        str += "PhoneType = " + tm.getPhoneType() + "\n";
        str += "SimCountryIso = " + tm.getSimCountryIso() + "\n";
        str += "SimOperator = " + tm.getSimOperator() + "\n";
        str += "SimOperatorName = " + tm.getSimOperatorName() + "\n";
        str += "SimSerialNumber = " + tm.getSimSerialNumber() + "\n";
        str += "SimState = " + tm.getSimState() + "\n";
        str += "SubscriberId(IMSI) = " + tm.getSubscriberId() + "\n";
        str += "VoiceMailNumber = " + tm.getVoiceMailNumber() + "\n";
        return str;
    }
    LinearLayout linearLayout;
    ScrollView scrollView;
    Map<String, Object> issues=new HashMap<>();;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Button b = (Button)findViewById(R.id.Button01);
        linearLayout =(LinearLayout)findViewById(R.id.events);

        scrollView=(ScrollView)findViewById(R.id.scrollView);
        scrollView.setVisibility(View.GONE);
        volunteer=(Button)findViewById(R.id.volunteer);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!logged_in){

                    startActivityForResult(new Intent(MapsActivity.this, LoginActivity.class),LOGIN_USER_REQUEST,
                            ActivityOptions.makeSceneTransitionAnimation(MapsActivity.this).toBundle());
                }else {
                    startActivityForResult(new Intent(MapsActivity.this, Form.class).putExtra("userid", userid), POST_FORM_REQUEST);
                }
                //Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                //startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
            }
        });
        Button login = (Button)findViewById(R.id.Button02);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MapsActivity.this, LoginActivity.class),LOGIN_USER_REQUEST);
            }
        });
        volunteer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MapsActivity.this, ps.class),0);
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        GPS_Permission();
        GPS_handler();
    }
    private void GPS_Permission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},1);
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET}, 1);
            GPS_handler();
            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            // Permission has already been granted
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

             @Override
             public boolean onOptionsItemSelected(MenuItem item) {
                 // Handle action bar item clicks here. The action bar will
                 // automatically handle clicks on the Home/Up button, so long
                 // as you specify a parent activity in AndroidManifest.xml.
                 int id = item.getItemId();

                 //noinspection SimplifiableIfStatement
                 if (id == R.id.report) {
                     return true;
                 }

                 return super.onOptionsItemSelected(item);
             }
    private  void  GPS_handler(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {


            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        Geocoder geocoder = new Geocoder(getApplicationContext());

                        try {
                            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                            String str = addressList.get(0).getLocality();
                            str += addressList.get(0).getCountryName();
                            //mMap.addMarker(new MarkerOptions().position(latLng).title(str));
                            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.2f));


                        } catch (IOException e) {
                            e.printStackTrace();
                            ;
                        }

                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {


                    }
                });


            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {/////////////////////////////////

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        Geocoder geocoder = new Geocoder(getApplicationContext());
                        try {
                            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                            String str = addressList.get(0).getLocality();
                            str += addressList.get(0).getCountryName();
                            //mMap.addMarker(new MarkerOptions().position(latLng).title(str));
                            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.2f));
                        } catch (IOException e) {
                            e.printStackTrace();
                            ;
                        }

                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {

                    }

                    @Override
                    public void onProviderEnabled(String s) {

                    }

                    @Override
                    public void onProviderDisabled(String s) {


                    }
                });
            }
        }
    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    GPS_handler();
                    if(map_loaded) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                            mMap.setMyLocationEnabled(true);
                        }
                    }
                    if(latLng!=null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.2f));
                    }
                } else {
                            // permission denied, boo! Disable the
                            // functionality that depends on this permission.
                }
                return;
            }
                    // other 'case' lines to check for other
                    // permissions this app might request
        }
    }
            @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d("test", "here");
        if (requestCode == LOGIN_USER_REQUEST) {
            if (resultCode == RESULT_OK){
                String messageReturn = data.getData().toString();
                Toast.makeText(getApplicationContext(), messageReturn, Toast.LENGTH_LONG).show();
                logged_in=true;
                //login.setEnabled(false);
                //login.setVisibility(View.GONE);

                volunteer.setEnabled(true);
                volunteer.setVisibility(View.VISIBLE);

                userid=md5(messageReturn);
            }
        }
        if(requestCode==POST_FORM_REQUEST){
            get_fires();
        }
        if( requestCode == CAMERA_PIC_REQUEST)
        {
            if(latLng!=null) {
                //  data.getExtras()
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                // time to http post
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();

                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                Map<String, Object> fire = new HashMap<>();
                fire.put("imsi", "test");
                fire.put("lat", latLng.latitude);
                fire.put("lng", latLng.longitude);
                fire.put("image", encoded);
                //fire.put("time",Calendar.getInstance().getTime());

// Add a new document with a generated ID
                db.collection("fire")
                        .add(fire)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("test", "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("test", "Error adding document", e);
                            }
                        });
                get_fires();
            }
        }
        else
        {
            Toast.makeText(this, "Picture NOt taken", Toast.LENGTH_LONG);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void get_fires(){
        CollectionReference fire = db.collection("fire");

        db.collection("issue")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                //Log.d("", document.getId() + " => " + document.getData());

                                Map<String, Object> issue = new HashMap<>();
                                issue = document.getData();
                                String id = (String) document.getId();
                                issues.put(id,document.getData());

                                LatLng lanlng = new LatLng((double)document.getData().get("lat"), (double)document.getData().get("lng"));


                                if(!ids.contains(id)){
                                    BitmapDescriptor icon=BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                                    switch ((String)issue.get("issue_type")){
                                        case "obstructive_flora":
                                            icon = BitmapDescriptorFactory.fromResource(R.drawable.obstructive_flora_icon);
                                            break;
                                        case "electric_grid":
                                            icon = BitmapDescriptorFactory.fromResource(R.drawable.electric_grid_icon);
                                            break;
                                        case "water_grid":
                                            icon = BitmapDescriptorFactory.fromResource(R.drawable.water_grid);
                                            break;
                                        case "general_infrastructure":
                                            icon = BitmapDescriptorFactory.fromResource(R.drawable.general_infrastructure);
                                            break;
                                        case "biohazardous_waste":
                                            icon = BitmapDescriptorFactory.fromResource(R.drawable.biohazardous_waste);
                                            break;
                                        case "road_infrastructure":
                                            icon = BitmapDescriptorFactory.fromResource(R.drawable.road_infrastructure);
                                            break;
                                    }

                                    ids.add(id);
                                    Marker marker =  mMap.addMarker(new MarkerOptions()
                                            .position(lanlng)
                                            .title((String) document.getData().get("issue_type")));

                                    marker.setIcon(icon);

                                    marker.setTag(id);
                                    markers.add(marker);
                                    mMap.setOnMarkerClickListener(MapsActivity.this);
                                    mMap.setOnMapClickListener(new OnMapClickListener() {
                                        @Override
                                        public void onMapClick(LatLng latLng) {
                                            linearLayout.removeAllViews();
                                            scrollView.setVisibility(View.GONE);
                                        }
                                    });
                                }
                            }

                        } else {
                            Log.w("", "Error getting documents.", task.getException());
                        }
                    }
                });
    }
    @Override
    public boolean onMarkerClick(final Marker marker) {
        // Retrieve the data from the marker.
        final String issue_id=(String)marker.getTag();
        final Map<String, Object> issue = (Map<String, Object>)issues.get(issue_id);
        byte[] decodedString = Base64.decode((String) issue.get("image"), Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        BitmapDescriptor icon= BitmapDescriptorFactory.fromBitmap(decodedByte);
        ImageView image = new ImageView(MapsActivity.this);
        image.setImageBitmap(decodedByte);
        TextView tv = new TextView((MapsActivity.this));
        tv.setText((String)issue.get("disc"));
        tv.setTextSize(48);
        tv.setHeight(64);
        tv.setMaxLines(15);
        tv.setLines(2);
        tv.setBackgroundColor(Color.parseColor("#5CFFFFFF"));
        linearLayout.removeAllViews();
        linearLayout.addView(image);
        linearLayout.addView(tv);
        if (vol){
            Button b = new Button(MapsActivity.this);
            b.setText("Claim");
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, Object> issue = (Map<String, Object>)issues.get(issue_id);
                    try {
                        JSONObject jsonBody = new JSONObject();
                        jsonBody.put("$class ", "gr.municipality.mercury.volunteer_claim");
                        jsonBody.put("issue",issue_id);
                        Log.d("test", "DocumentSnapshot added with ID: " + issue_id);
                        postData("http://172.16.220.154:3000/api/volunteer_claim",jsonBody);
                        final Map<String, Object> t=new HashMap<>();
                        t.put("claimed",true);
                        DocumentReference doc = db.collection("user").document("JVRA");

                        doc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                db.collection("user").document("JVRA").update("score",(Long)documentSnapshot.get("score")+5);
                            }
                        });

                        db.collection("issue").document(issue_id).update("claimed",true).addOnSuccessListener(new OnSuccessListener < Void > () {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(MapsActivity.this, "Claimed Successfully",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }

            });
            linearLayout.addView(b);
            b = new Button(MapsActivity.this);
            b.setText("Close");
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, Object> issue = (Map<String, Object>)issues.get(issue_id);
                    DocumentReference doc1 = db.collection("issue").document(issue_id);
                    doc1.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if((boolean) documentSnapshot.get("claimed")) {
                                try {
                                    JSONObject jsonBody = new JSONObject();
                                    jsonBody.put("$class ", "gr.municipality.mercury.volunteer_close");
                                    jsonBody.put("issue", issue_id);

                                    DocumentReference doc = db.collection("user").document("JVRA");
                                    doc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            db.collection("user").document("JVRA").update("score",(Long)documentSnapshot.get("score")+10).addOnSuccessListener(new OnSuccessListener < Void > () {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(MapsActivity.this, "Keep up The Good Work",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                                    postData("http://192.168.1.70:3000/api/volunteer_close", jsonBody);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });


                }

            });
            linearLayout.addView(b);
        }
        scrollView.setVisibility(View.VISIBLE);
        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        map_loaded = true;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        if (latLng != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.2f));
        }
        get_fires();
        // Add a marker in Sydney and move the camera
        //LatLng myloc = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(myloc).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc, 10.2f));
    }
    void request(String url){
        final TextView textView = (TextView) findViewById(R.id.text);
// ...

// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        textView.setText("Response is: "+ response.substring(0,500));
                        try {
                            JSONObject jObj = new JSONObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textView.setText("That didn't work!");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void postData(String URL, JSONObject jsonBody){

             RequestQueue requestQueue = Volley.newRequestQueue(this);



             final String requestBody = jsonBody.toString();

             StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                 @Override
                 public void onResponse(String response) {
                     Log.i("VOLLEY", response);
                 }
             }, new Response.ErrorListener() {
                 @Override
                 public void onErrorResponse(VolleyError error) {
                     Log.e("VOLLEY", error.toString());
                 }
             }) {
                 @Override
                 public String getBodyContentType() {
                     return "application/json; charset=utf-8";
                 }

                 @Override
                 public byte[] getBody() throws AuthFailureError {
                     try {
                         return requestBody == null ? null : requestBody.getBytes("utf-8");
                     } catch (UnsupportedEncodingException uee) {
                         VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                         return null;
                     }
                 }

                 @Override
                 protected Response<String> parseNetworkResponse(NetworkResponse response) {
                     String responseString = "";
                     if (response != null) {
                         responseString = String.valueOf(response.statusCode);
                         // can get more details such as response.headers
                     }
                     return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                 }
             };

             requestQueue.add(stringRequest);

         }
    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
