package com.raf.johnvrachnis.firespot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.*;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.ibm.watson.developer_cloud.language_translator.v3.LanguageTranslator;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslationResult;
import com.ibm.watson.developer_cloud.language_translator.v3.util.Language;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Form extends AppCompatActivity {
    private static int CAMERA_PIC_REQUEST =1337;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ArrayList<Circle> circles = new ArrayList<Circle>();
    ArrayList<Marker> markers = new ArrayList<Marker>();
    ArrayList<String> ids = new ArrayList<String>();
    private GoogleMap mMap;
    private boolean map_loaded = false;
    LatLng latLng = null;
    LocationManager locationManager;
    EditText editText,discription ;

    Bitmap thumbnail;
    public static int MY_PERMITIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    public static int MY_PERMITIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    public static int MY_PERMITIONS_REQUEST_INTERNET = 1;
    final Context context = this;
    ImageView imageView;
    TextView textView;
    String full_address="";
    Spinner spinner;
    Button b2;
    String userid;
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byte[] byteArray ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        userid= getIntent().getStringExtra("userid");
        Toast.makeText(getApplicationContext(), userid, Toast.LENGTH_LONG).show();
        Button b = (Button)findViewById(R.id.photo_button);
        imageView = (ImageView)findViewById(R.id.imageView);
        editText= (EditText)findViewById(R.id.editText);
        discription = (EditText)findViewById(R.id.editText2);
        spinner = (Spinner)findViewById(R.id.spinner1);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
            }
        });
        b2 = (Button)findViewById(R.id.submit);
        b2.setClickable(false);
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            if(thumbnail!=null) {
                thumbnail.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byteArray = byteArrayOutputStream.toByteArray();


                //FirebaseDatabase database = FirebaseDatabase.getInstance();

                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                Map<String, Object> issue = new HashMap<>();

                issue.put("disc", String.valueOf(discription.getText()));
                issue.put("userid", userid);
                issue.put("full_address", String.valueOf(editText.getText()));

                issue.put("issue_type", String.valueOf(spinner.getSelectedItem()));
                issue.put("lat", latLng.latitude);
                issue.put("lng", latLng.longitude);
                issue.put("claimed", false);

                issue.put("image", encoded);

                issue.put("time", Calendar.getInstance().getTimeInMillis());
                Map<String, Object> user = new HashMap<>();
                DocumentReference doc = db.collection("user").document("JVRA");

                doc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        db.collection("user").document("JVRA").update("score",(Long)documentSnapshot.get("score")+2);
                    }
                });

                db.collection("user").add(user);
                // Add a new document with a generated ID
                db.collection("issue").add(issue)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                try {
                                    JSONObject jsonBody = new JSONObject();
                                    jsonBody.put("$class ", "gr.municipality.mercury.VolunteerIssue");
                                    jsonBody.put("issue_id", documentReference.getId());
                                    jsonBody.put("status", "Submitted");
                                    jsonBody.put("category", String.valueOf(spinner.getSelectedItem()));
                                    jsonBody.put("comments", String.valueOf(discription.getText()));
                                    jsonBody.put("municipality", "ATHENS");
                                    jsonBody.put("address", String.valueOf(editText.getText()));
                                    jsonBody.put("photo", " ");
                                    jsonBody.put("lat", latLng.latitude);
                                    jsonBody.put("lng", latLng.longitude);
                                    jsonBody.put("submission_time", Calendar.getInstance().getTimeInMillis());
                                    postData("http://172.16.220.154:3000/api/VolunteerIssue", jsonBody);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.d("test", "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("test", "Error adding document", e);
                            }
                        });
                finish();
            }
            }
        });
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
                            editText.setText(str);
                            textView.setText(str);
                            //mMap.addMarker(new MarkerOptions().position(latLng).title(str));
                            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.2f));


                        } catch (IOException e) {
                            e.printStackTrace();

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


                            str += addressList.get(0).getAddressLine(0);
                            if (!full_address.equals(str)){
                                full_address = str;
                                editText.setText(str);
                            }


                            //mMap.addMarker(new MarkerOptions().position(latLng).title(str));
                            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10.2f));
                        } catch (IOException e) {
                            e.printStackTrace();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if( requestCode == CAMERA_PIC_REQUEST)
        {
            thumbnail = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(thumbnail);
            b2.setClickable(true);
        }
        else
        {
            Toast.makeText(this, "Picture NOt taken", Toast.LENGTH_LONG);
        }
        super.onActivityResult(requestCode, resultCode, data);
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
    public void postData(String URL,JSONObject jsonBody){

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

}
