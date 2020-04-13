package nathanielwendt.mpc.ut.edu.paco;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.preference.PreferenceManager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import nathanielwendt.mpc.ut.edu.paco.D2D.AccessProfile;
import nathanielwendt.mpc.ut.edu.paco.Data.SettingData;
import nathanielwendt.mpc.ut.edu.paco.Data.UserData;
import nathanielwendt.mpc.ut.edu.paco.D2D.PrivacySetting;
import nathanielwendt.mpc.ut.edu.paco.D2D.paco;
import nathanielwendt.mpc.ut.edu.paco.D2D.sendData;
import nathanielwendt.mpc.ut.edu.paco.utils.SQLiteRTree;

import static com.ut.mpc.setup.Initializer.pedDefaults;

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
//
//import nathanielwendt.mpc.ut.edu.paco.fire_MQTT.paco;

public class MainActivity extends AppCompatActivity implements PlacesFragment.OnFragmentInteractionListener, NotificationsFragment.OnFragmentInteractionListener,
        MapFragment.MapFragmentListener, CreatePlaceFragment.CreatePlaceFragmentDoneListener, FriendsFragment.OnFragmentInteractionListener {

    private ViewGroup viewGroup;
    private static final int LOCATION_PERMISSION = 1;
    private LatLng lastLoc = Constants.DEFAULT_LAT_LNG;
    private Toolbar toolbar;
    private FragmentHelper fHelper;
    private PopupWindow popupWindow;//

    private String TOOLBAR_TRACKING;
    private String TOOLBAR_PLAIN ;
    private boolean tracking = false;

    MqttAndroidClient client;
    private RequestQueue requestQueue;
    private Receiver Receiver;
    MqttConnectOptions options = new MqttConnectOptions();

    //ArrayAdapter adapter;
    ArrayList<String> isSubTopic = new ArrayList<String>();

    private SettingData setData = new SettingData();
    private AccessProfile accessProfile = new AccessProfile();//
    private PrivacySetting privacySetting = new PrivacySetting();//

    //Location
    Location location;
    private String currentLocation;
    boolean getService = false;
    private LocationManager lms;
    private String bestProvider = LocationManager.GPS_PROVIDER;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 102;

    //User
    String clientID;
    private UserData user = new UserData();
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabaseReference;

    private LSTFilter filter;
    //Paco paco;//
    paco mPaco;//

    static {
        System.loadLibrary("sqliteX");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Initializer initializer = pedDefaults();
        //Initializer initializer = vehicularDefaults();
        SQLiteRTree rtree = new SQLiteRTree(this, "RTreeMain");
        filter = new LSTFilter(rtree, initializer);//
        filter.setRefPoint(new STPoint((float) lastLoc.longitude, (float) lastLoc.latitude, 0));

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
        //SharedPreferences sharedpreferences = this.getSharedPreferences("notification", Context.MODE_PRIVATE);
        //adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1);

        Receiver=new Receiver();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPaco = new paco(getBaseContext(), this, filter);//

//        io.moquette.server.Server server = new io.moquette.server.Server();
//        try {
//            MemoryConfig memoryConfig = new MemoryConfig(new Properties());
//            memoryConfig.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator + BrokerConstants.DEFAULT_MOQUETTE_STORE_MAP_DB_FILENAME);
//            //server.startServer(memoryConfig);
//            // server.startServer();//is not working due to DEFAULT_MOQUETTE_STORE_MAP_DB_FILENAME;
//            //Log.d(TAG,"Server Started");
//        }
//        catch (IOException e) { e.printStackTrace(); }
//        catch (Exception e){ e.printStackTrace(); }
        //get Firebase Token
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        String FirebaseToken = task.getResult().getToken();

                        //For nearby User Search
                        user.setTokenID(FirebaseToken);
                        user.setLastLocation(currentLocation);
                        mDatabaseReference = mDatabase.getReference().child("user").child(FirebaseToken);
                        mDatabaseReference.setValue(user);

                        SharedPreferences sharedpreferences = getBaseContext().getSharedPreferences("token", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        String key = "token";
                        String data = FirebaseToken;
                        Log.d("LST", "putting shared prefs name: " + key + " data: " + data);
                        editor.putString(key, data);
                        editor.commit();

                        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString("TOKEN", FirebaseToken).apply();
                        String clientId = FirebaseToken;

                        //MQTT Client
                        clientID = clientId;
                        final String url = "tcp://10.0.2.2:1883";
                        //final String url = "tcp://192.168.0.104";
                        //final String url = "tcp://10.147.88.144";
                        client =
                                new MqttAndroidClient(MainActivity.this, url, clientId);

                        mPaco.setClientId(clientId);////
                        mPaco.setMQTTClient(client);////

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
                            public void messageArrived(String topic, MqttMessage MQTTmessage) throws Exception {
                                //String currentToken = tokenView.getText().toString();
                                String currentToken = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("TOKEN", "defaultStringIfNothingFound");//

                                String message = new String(MQTTmessage.getPayload());

                                DateFormat df = new SimpleDateFormat("MMM d, yyyy HH:mm:ss");
                                String ts = df.format(new Date());
                                mPaco.respondTOMQTTRequest(message, ts, getAccessProfile(), privacySetting);
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

        //loadFile(getApplicationContext(),"Residents.json", filter);
    }

    //Receive notification from FireBase
    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DateFormat df = new SimpleDateFormat("MMM d, yyyy HH:mm:ss");
            String ts = df.format(new Date());
            mPaco.respondTORequest(intent, ts, accessProfile, privacySetting);//
        }
    };

    public void PopupWindowAccept(String PoK, final sendData SendData, final String type) {
        View view = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.pop_window_accept, null);
        popupWindow = new PopupWindow(view);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView PoKText = (TextView) view.findViewById(R.id.PoK_text);
        PoKText.setText("PoK is " + PoK);

        Button btn_YES = (Button) view.findViewById(R.id.Yes);
        btn_YES.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(type=="Fire"){
                    mPaco.SEND(SendData.getSendData());//
                }else{
                    mPaco.sendMQTT(SendData.getSendData());//
                }
                popupWindow.dismiss();
            }
        });

        Button btn_NO = (Button) view.findViewById(R.id.No);
        btn_NO.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        popupWindow.showAtLocation(view, Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    public void PopupWindowDeny(String PoK, final sendData SendData, boolean showPoK) {
        View view = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.pop_window_deny, null);
        popupWindow = new PopupWindow(view);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

            TextView PoKText = (TextView) view.findViewById(R.id.PoK_text);
            if(showPoK){
                PoKText.setText("PoK is " + PoK);
            }

            Button btn_OK = (Button) view.findViewById(R.id.OK);
            btn_OK.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });

        popupWindow.showAtLocation(view, Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    //Firebase Get Token
    private void getTokenFirebase() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                    }
                });
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

            //update to Firebase
            SharedPreferences sharedpreferences = getBaseContext().getSharedPreferences("token", Context.MODE_PRIVATE);
            String token = sharedpreferences.getString("token", "Default");

            mDatabaseReference = mDatabase.getReference().child("user").child(token);
            user.setLastLocation(currentLocation);
            user.setTokenID(token);
            mDatabaseReference.setValue(user);
        }
    };



    public MqttAndroidClient getClient() {return client;}
    public String getTheCurrentClient() {return clientID;}
    public String getCurrentLocation() {return currentLocation;}
    public FragmentHelper getFragmentHelper() {return fHelper;}
    public SettingData getSetData() {return setData;}
    public AccessProfile getAccessProfile() {return accessProfile;}
    public LSTFilter getFilter() {return filter;}
    public UserData getUser() {return user;}
    public DatabaseReference getDatabaseReference() {return mDatabaseReference;}
    public FirebaseDatabase getDatabase(){return mDatabase;}
    public PrivacySetting getPrivacySetting(){return privacySetting;}

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
        } else if(id == R.id.action_receive_places) {
            tag = "NotificationFragment";
            fHelper.show(tag, new NotificationsFragment());
            return true;
        } else if(id == R.id.action_setting_privacy) {
            tag = "SettingFragment";
            fHelper.show(tag, new SettingFragment());
            return true;
        } else if(id == R.id.action_show_friedns){
            tag = "FriednsFragment";
            fHelper.show(tag, new FriendsFragment());
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

    //Load data from Residents.json....
    public void loadFile(Context context, String FileName, LSTFilter filter) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(FileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

            try {
                JSONObject inData = new JSONObject(json);
                JSONArray jsonArray = inData.getJSONArray("Resident");
                for(int i=0;i<jsonArray.length(); ++i) {
                    JSONObject data = new JSONObject();
                    String name = jsonArray.getJSONObject(i).getString("Name");
                    STPoint Location = STPoint.fromString(jsonArray.getJSONObject(i).getString("Location"));
                    filter.insert(Location, name, "");
                }
            } catch (JSONException e){
                Log.e("loadError", e.toString());
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
