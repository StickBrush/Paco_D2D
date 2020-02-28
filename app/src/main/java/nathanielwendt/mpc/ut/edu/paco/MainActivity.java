package nathanielwendt.mpc.ut.edu.paco;

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.ut.mpc.setup.Initializer;
import com.ut.mpc.utils.LSTFilter;
import com.ut.mpc.utils.STPoint;
import com.ut.mpc.utils.STRegion;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import nathanielwendt.mpc.ut.edu.paco.utils.PlaceStore;
import nathanielwendt.mpc.ut.edu.paco.utils.SQLiteRTree;

import static com.ut.mpc.setup.Initializer.pedDefaults;
import static com.ut.mpc.setup.Initializer.vehicularDefaults;

import android.location.Criteria;
import android.location.LocationListener;
import android.provider.Settings;
import com.android.volley.RequestQueue;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainActivity extends AppCompatActivity implements PlacesFragment.OnFragmentInteractionListener,
        MapFragment.MapFragmentListener, CreatePlaceFragment.CreatePlaceFragmentDoneListener {

    private ViewGroup viewGroup;
    private static final int LOCATION_PERMISSION = 1;
    private LatLng lastLoc = Constants.DEFAULT_LAT_LNG;
    private Toolbar toolbar;
    private FragmentHelper fHelper;

    private String TOOLBAR_TRACKING;
    private String TOOLBAR_PLAIN ;
    private boolean tracking = false;

    //...//
    MqttAndroidClient client;
    private RequestQueue requestQueue;
    private Receiver Receiver;
    MqttConnectOptions options = new MqttConnectOptions();

    ArrayAdapter adapter;
    ArrayList<String> isSubTopic = new ArrayList<String>();

    //Location
    Location location;
    private String currentLocation;
    boolean getService = false;
    private LocationManager lms;
    private String bestProvider = LocationManager.GPS_PROVIDER;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 102;

    String clientID;

    //private LSTFilter filter;
    public LSTFilter filter;//

    static {
        System.loadLibrary("sqliteX");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
////        super.onCreate(savedInstanceState);
//        Initializer initializer = pedDefaults();//
////        setContentView(R.layout.activity_main);
//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        //Location
        LocationManager status = (LocationManager) (this.getSystemService(Context.LOCATION_SERVICE));
        if(status.isProviderEnabled(LocationManager.GPS_PROVIDER)|| status.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            currentLocation = locationServiceInitial();
            lms.requestLocationUpdates(bestProvider, 10, 100, locationListener);
            //Toast.makeText(MainActivity.this, "currentLocation: "+currentLocation, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please turn on location service", Toast.LENGTH_LONG).show();
            getService = true;
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        // onCreate


        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1);

        Receiver=new Receiver();

        Initializer initializer = vehicularDefaults();//Success
//        Initializer initializer = pedDefaults();//Fail
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //get Firebase Token
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        String FirebaseToken = task.getResult().getToken();
                        Log.d("token", FirebaseToken);
                        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString("TOKEN", FirebaseToken).apply();
                        String clientId = FirebaseToken;

                        //MQTT Client
                        clientID = clientId;
                        final String url = "tcp://192.168.0.104";
//                        final String url = "tcp://10.145.5.82";
                        client =
                                new MqttAndroidClient(MainActivity.this, url, clientId);

                        try {
                            IMqttToken token = client.connect(options);
                            token.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    // We are connected
                                    Toast.makeText(MainActivity.this, "Connect MQTT Successfully", Toast.LENGTH_LONG).show();
                                    subscribe("Firebase_MQTT");
                                }

                                @Override
                                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                    // Something went wrong e.g. connection timeout or firewall problems
                                    Toast.makeText(MainActivity.this, "Failed to Connect MQTT", Toast.LENGTH_LONG).show();

                                }
                            });
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }

                        //Receive notification from MQTT
                        client.setCallback(new MqttCallback() {
                            @Override
                            public void connectionLost(Throwable cause) {
                                Toast.makeText(MainActivity.this, "MQTT lost conncetion", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void messageArrived(String topic, MqttMessage message) throws Exception {
                                //String currentToken = tokenView.getText().toString();
                                String currentToken = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("TOKEN", "defaultStringIfNothingFound");//

                                String msg = new String(message.getPayload());
                                JSONObject temp_jsonMessage = new JSONObject(msg);
                                JSONObject jsonMessage = new JSONObject();
                                String receiverToken = temp_jsonMessage.get("to").toString();

                                jsonMessage = temp_jsonMessage.getJSONObject("data");
                                JSONObject jsonParam = jsonMessage.getJSONObject("params");
                                JSONObject jsonToken = jsonMessage.getJSONObject("Token");
                                String senderToken = jsonToken.get("SenderToken").toString();

                                if(receiverToken.equals(currentToken)){
                                    if(!senderToken.equals(currentToken)){
                                        String title = jsonParam.get("title").toString();
                                        String description = jsonParam.get("description").toString();
                                        String image = jsonParam.get("image").toString();
                                        String final_msg = "Title: " + title +"\n" + "Description: " + description /*+"\n" + "Latitude: " + latitude +"\n" + "Longitude: " + longitude*/;
                                        adapter.add(final_msg);
                                        //update data
                                        updateList(title, description, image);

                                        //send back Location
                                        temp_jsonMessage.put("Location", currentLocation);
                                        temp_jsonMessage.put("to", senderToken);
                                        String sendBackData = temp_jsonMessage.toString();
                                        sendMQTT(sendBackData);
                                    }
                                    //senderToken == currentToken
                                    else{
                                        String Location = temp_jsonMessage.get("Location").toString();
                                        String fireBase_msg = /*"Send notification to: " + receiver_token + "\n" +*/ "Receiver Location: " + Location +"\n";
                                        adapter.add(fireBase_msg);
                                    }
                                }
                                else{

                                }

                            }

                            @Override
                            public void deliveryComplete(IMqttDeliveryToken token) {

                            }
                        });
                    }
                });

        //Firebase
        if (Receiver != null) {
            IntentFilter intentFilter = new IntentFilter("NOW");
            LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver( Receiver, intentFilter);
        }


        TOOLBAR_PLAIN = getResources().getString(R.string.app_name);
        TOOLBAR_TRACKING = TOOLBAR_PLAIN + " (Tracking)";

        //SharedPreferences sharedpreferences = getSharedPreferences("Places", Context.MODE_PRIVATE);
        //sharedpreferences.edit().clear().commit();
        SQLiteRTree rtree = new SQLiteRTree(this, "RTreeMain");
        filter = new LSTFilter(rtree, initializer);//
        filter.setRefPoint(new STPoint((float) lastLoc.longitude, (float) lastLoc.latitude, 0));

        Dexter.initialize(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(toggleTracking);

        fHelper = new FragmentHelper(R.id.container, getSupportFragmentManager());

        LastKnownLocationReceiver myReceiver = new LastKnownLocationReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationService.ACTION_LOC);
        registerReceiver(myReceiver, intentFilter);

        viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);

        fHelper.show("PlacesFragment", new PlacesFragment(), true);
    }

    //Receive notification from FireBase
    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("FirebaseData");
            //Receiver(phone 2) receiving post request from phone 1 & send back location information
            try {
                JSONObject jsonData = new JSONObject(message);

                JSONObject jsonParam = jsonData.getJSONObject("params");
                String title = jsonParam.get("title").toString();
                String description = jsonParam.get("description").toString();
                String image = jsonParam.get("image").toString();
                String fireBase_msg = "Title: " + title +"\n" + "Description: " + description;
                adapter.add(fireBase_msg);
                Log.d("image", image);
                updateList(title, description, image);

                JSONObject locationJson = new JSONObject();
                JSONObject locationJson2 = new JSONObject();
                JSONObject sendBackJson = new JSONObject();

                JSONObject jsonToken = jsonData.getJSONObject("Token");
                String SenderToken = jsonToken.get("SenderToken").toString();
                locationJson.put("Location", currentLocation);
                locationJson2.put("SendBackLocation", locationJson);
                sendBackJson.put("to", SenderToken);
                sendBackJson.put("data", locationJson2);
                sendBackJson.put("target", "Firebase");
                String sendBackData = sendBackJson.toString();
                SEND(sendBackData);

            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                Log.d("JSONException", e.toString());
            }

            //sender(phone 1) receiving location information form phone 2
            try {
                JSONObject jsonData = new JSONObject(message);
                JSONObject jsonLocation = jsonData.getJSONObject("SendBackLocation");
                String Location = jsonLocation.get("Location").toString();
                String fireBase_msg = "Receiver Location: " + Location +"\n";
                adapter.add(fireBase_msg);

            }catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    //Firebase Get Token
    private void getTokenFirebase() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        String token = task.getResult().getToken();
                        //tokenView.setText(token);
                    }
                });
    }

    //FireBase : SEND
    public void SEND(String data)
    {
        final String savedata= data;
        String URL="https://fcm.googleapis.com/fcm/send";

        requestQueue = Volley.newRequestQueue(MainActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject objres=new JSONObject(response);
                    Toast.makeText(MainActivity.this,"Firebase Send Successfully ",Toast.LENGTH_LONG).show();

                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this,"Server Error",Toast.LENGTH_LONG).show();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "key=AAAADZqMCa0:APA91bEPutekx6WZKNG9ekiishPS1g-dZhJiSsq1uZWgLpsWtuJ0-wePj-LU7KdKv6h0J4ZawlY4BrZ_nReIhG6zf-AeLNA8wpDeJmQ8YQCPAywJ9lT51hKiiO54BWbDdAUd91eEObzT");
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return savedata == null ? null : (savedata).getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    //Log.v("Unsupported Encoding while trying to get the bytes", data);
                    return null;
                }
            }

        };
        requestQueue.add(stringRequest);
    }

    //MQTT: SEND
    public void sendMQTT(String message){
        byte[] encodedMessage = new byte[0];
        String strMessage;
        try {
            try{
                JSONObject inMessage = new JSONObject(message);
                strMessage = inMessage.toString();
            }
            catch (JSONException e){
                strMessage = "";
            }
            encodedMessage = strMessage.getBytes("UTF-8");
            MqttMessage fianl_message = new MqttMessage(encodedMessage);
            client.publish("Firebase_MQTT", fianl_message);
            Toast.makeText(MainActivity.this, "Send to MQTT Successfully", Toast.LENGTH_SHORT).show();
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Failed to Send", Toast.LENGTH_SHORT).show();
        }
    }

    //Subscribe for MQTT
    private void subscribe(String in_topic)
    {
        String topic = in_topic;
        int qos = 1;
        if(isSubTopic.contains(topic) == false){
            try {
                IMqttToken subToken = client.subscribe(topic, qos);
                subToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // The message was published
                        //Toast.makeText(MainActivity.this, "Subscribe Successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken,
                                          Throwable exception) {
                        Toast.makeText(MainActivity.this, "Failed to Subscribe", Toast.LENGTH_SHORT).show();

                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
            isSubTopic.add(topic);
        }
    }

    //upadate Place
    private  void updateList(String title, String description, String image){
        //update data
        STRegion region;
        //PlaceStore placeStore = new PlaceStore(MainActivity.this);

        if(STRegion.fromString(description) != null){
            region = STRegion.fromString(description);//
        }
        else{
            region = new STRegion(new STPoint(0, 0), new STPoint(0, 0));
        }
        try {
            Bitmap bitImage = StringToBitMap(image);
            Log.d("bitImage", image);
            Uri imUri = getImageUri(getApplicationContext(), bitImage, title);
            String posterPath = getRealPathFromURI(imUri);
            //placeStore.put(title, posterPath, region);
            //this.filter.insert(region.getMins(), title, posterPath);//
        }
        catch (Exception e){
            Log.d("Exception", e.toString());
        }

        //refresh page
        Fragment frag = MainActivity.this.getSupportFragmentManager().findFragmentByTag("PlacesFragment");
        if(frag.isVisible()){
            fHelper.show("PlacesFragment", new PlacesFragment());
        }
    }

    //Location
    private String locationServiceInitial() {
        lms = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        bestProvider = lms.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return "No permission";
        }
        location = lms.getLastKnownLocation(bestProvider);

        String InitLocation = getLocation(location);
        return InitLocation;
    }

    private void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasPermission = checkSelfPermission(
                    Manifest.permission.ACCESS_FINE_LOCATION);

            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_FINE_LOCATION_PERMISSION);
            }
            else {
            }
        }
    }

    private String getLocation(Location location) {
        String GETLocation;
        if(location != null) {
            Double longitude = location.getLongitude();
            Double latitude = location.getLatitude();
            currentLocation = "(" + latitude +", " + longitude + ")";
            GETLocation = "(" + latitude +", " + longitude + ")";
            Toast.makeText(MainActivity.this, "GET Location: "+ GETLocation, Toast.LENGTH_SHORT).show();
        }
        else {
            GETLocation ="NULL";
            Toast.makeText(this, "Unable to locate coordinates", Toast.LENGTH_LONG).show();
        }
        return GETLocation;
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onLocationChanged(Location location) {
            // New Location return here
            Toast.makeText(MainActivity.this, "onLocationChanged", Toast.LENGTH_SHORT).show();
            getLocation(location);
        }
    };

    //image
    public Uri getImageUri(Context inContext, Bitmap inImage, String title) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, title, null);
        return Uri.parse(path);
    }

    private String getRealPathFromURI(Uri contentUri)
    {
        try
        {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = this.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        catch (Exception e)
        {
            return contentUri.getPath();
        }
    }

    public Bitmap StringToBitMap(String encodedString){
        try{
            byte [] encodeByte = Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }



    public MqttAndroidClient getClient() {return client;}
    public String getTheCurrentClient() {return clientID;}
    public ArrayAdapter getAdapter() {return adapter;}
    public FragmentHelper getFragmentHelper() {return fHelper;}


    private class LastKnownLocationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            double lastLoc[] = arg1.getDoubleArrayExtra(LocationService.LAT_LONG_DATA);
            MainActivity.this.lastLoc = new LatLng(lastLoc[0], lastLoc[1]);
        }
    }

    View.OnClickListener toggleTracking = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            tracking = !tracking;
            if(tracking){
                Dexter.checkPermission(locationPermissionListener, Manifest.permission.ACCESS_FINE_LOCATION);
            } else {
                stopTracking();
            }
        }
    };

    private PermissionListener locationPermissionListener = new PermissionListener() {
        @Override
        public void onPermissionGranted(PermissionGrantedResponse response) {
            startTracking();
        }

        @Override
        public void onPermissionDenied(PermissionDeniedResponse response) {
            showSnackBar("Location permissions not granted, cannot track");
            tracking = false;
        }

        @Override
        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
            token.continuePermissionRequest();
        }
    };

    @Override
    public void showSnackBar(String text){
        Snackbar.make(viewGroup, text, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    public void startTracking(){
        startService(new Intent(MainActivity.this, LocationService.class));
        showSnackBar("started tracking");
        toolbar.setTitle(TOOLBAR_TRACKING);
    }

    public void stopTracking(){
        stopService(new Intent(MainActivity.this, LocationService.class));
        showSnackBar("stopped tracking");
        toolbar.setTitle(TOOLBAR_PLAIN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        String tag;
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_show_mapview) {
            tag = "MapFragment";
            Fragment mapFragment = new MapFragment();
            fHelper.show(tag, mapFragment);
            return true;
        } else if(id == R.id.action_show_places) {
            tag = "PlacesFragment";
            fHelper.show(tag, new PlacesFragment());
            return true;
        } else if(id == R.id.action_send_places) {
            tag = "SendFragment";
            fHelper.show(tag, new SendFragment());
            return true;
        } else if(id == R.id.action_receive_places) {
            tag = "NotificationFragment";
            fHelper.show(tag, new NotificationsFragment());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(String id) {

    }

    @Override
    public double windowPoK(STRegion region) {
        return filter.windowPoK(region);
    }

    @Override
    public LatLng lastLoc(){
        return lastLoc;
    }

    @Override
    public void createPlace(STRegion region){
        fHelper.show("CreatePlaceFragment", CreatePlaceFragment.newInstance(region));
    }

    @Override
    public void onCreatePlaceDone() {
        fHelper.show("PlacesFragment", new PlacesFragment(), true);
    }

}
