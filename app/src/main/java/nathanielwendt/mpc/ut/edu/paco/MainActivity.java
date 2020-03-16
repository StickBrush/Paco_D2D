package nathanielwendt.mpc.ut.edu.paco;

import android.Manifest;
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
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import androidx.preference.PreferenceManager;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.ut.mpc.setup.Initializer;
import com.ut.mpc.utils.LSTFilter;
import com.ut.mpc.utils.QueryWindow;
import com.ut.mpc.utils.STPoint;
import com.ut.mpc.utils.STRegion;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nathanielwendt.mpc.ut.edu.paco.Data.AccessProfile;
import nathanielwendt.mpc.ut.edu.paco.Data.NotificationData;
import nathanielwendt.mpc.ut.edu.paco.Data.PlaceData;
import nathanielwendt.mpc.ut.edu.paco.Data.SettingData;
import nathanielwendt.mpc.ut.edu.paco.Data.UserData;
import nathanielwendt.mpc.ut.edu.paco.Data.sendData;
import nathanielwendt.mpc.ut.edu.paco.utils.SQLiteRTree;
import nathanielwendt.mpc.ut.edu.paco.utils.notificationStore;

import static com.ut.mpc.setup.Constants.PoK.T_CUBE;
import static com.ut.mpc.setup.Constants.PoK.X_CUBE;
import static com.ut.mpc.setup.Constants.PoK.Y_CUBE;
import static com.ut.mpc.setup.Initializer.vehicularDefaults;
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

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.MessagesClient;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;

public class MainActivity extends AppCompatActivity implements PlacesFragment.OnFragmentInteractionListener, NotificationsFragment.OnFragmentInteractionListener,
        MapFragment.MapFragmentListener, CreatePlaceFragment.CreatePlaceFragmentDoneListener {

    private ViewGroup viewGroup;
    private static final int LOCATION_PERMISSION = 1;
    private LatLng lastLoc = Constants.DEFAULT_LAT_LNG;
    private Toolbar toolbar;
    private FragmentHelper fHelper;

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
    private AccessProfile accessProfile = new AccessProfile();////

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
    //private DatabaseReference mDatabaseReference = mDatabase.getReference().child("user");
    private DatabaseReference mDatabaseReference;

    private LSTFilter filter;
//    public LSTFilter filter;//

    static {
        System.loadLibrary("sqliteX");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                        final String url = "tcp://192.168.0.104";
//                        final String url = "tcp://10.147.88.144";
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
                            public void messageArrived(String topic, MqttMessage MQTTmessage) throws Exception {
                                //String currentToken = tokenView.getText().toString();
                                String currentToken = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("TOKEN", "defaultStringIfNothingFound");//

                                String message = new String(MQTTmessage.getPayload());

                                //HaldleMQTTMessage(message, currentToken);
                                DateFormat df = new SimpleDateFormat("MMM d, yyyy HH:mm:ss");
                                String ts = df.format(new Date());

                                JSONObject jsonData = new JSONObject(message);
                                JSONObject json_data = jsonData.getJSONObject("data");
                                JSONObject json_stage = json_data.getJSONObject("Stage");
                                String Stage = json_stage.get("CurrStage").toString();

                                sendData SendData = new sendData();
                                SendData.setTimeStamp(ts);
                                SendData.setStage(Integer.parseInt(Stage));
                                SendData.setMessage(message);

                                //updateNotificationList(SendData.HandleMQTTData());
                                String not1 =  SendData.HandleMQTTData().get(0);
                                String not2 =  SendData.HandleMQTTData().get(1);
                                String[] not = {not1, not2};
                                updateNotificationList(not);
                                //check Permission
                                //updateList(title, description, image);

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

//    @Override
//    public void onStart() {
//        super.onStart();
//
//        Nearby.getMessagesClient(this).publish(mMessage);
//        Nearby.getMessagesClient(this).subscribe(mMessageListener);
//    }
//
//    @Override
//    public void onStop() {
//        Nearby.getMessagesClient(this).unpublish(mMessage);
//        Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
//
//        super.onStop();
//    }
//
//    private void signIn() {
//        // Launches the sign in flow, the result is returned in onActivityResult
//        Intent intent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(intent, RC_SIGN_IN);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task =
//                    GoogleSignIn.getSignedInAccountFromIntent(data);
//            if (task.isSuccessful()) {
//                // Sign in succeeded, proceed with account
//                GoogleSignInAccount acct = task.getResult();
//            } else {
//                // Sign in failed, handle failure and update UI
//                // ...
//            }
//        }
//    }
//
////    private void buildGoogleApiClient() {
////        if (mGoogleApiClient != null) {
////            return;
////        }
////        mGoogleApiClient = new GoogleApiClient.Builder(this)
////                .addApi(Nearby.MESSAGES_API)
////                .addConnectionCallbacks(this)
////                .enableAutoManage(this, this)
////                .build();
////    }
//
//    /**
//     * Publishes a message to nearby devices and updates the UI if the publication either fails or
//     * TTLs.
//     */
//    private void publish() {
//        Log.i(TAG, "Publishing");
//        PublishOptions options = new PublishOptions.Builder()
//                .setStrategy(PUB_SUB_STRATEGY)
//                .setCallback(new PublishCallback() {
//                    @Override
//                    public void onExpired() {
//                        super.onExpired();
//                        Log.i(TAG, "No longer publishing");
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                //mPublishSwitch.setChecked(false);
//                            }
//                        });
//                    }
//                }).build();
//
//        Nearby.Messages.publish( mGoogleApiClient, mPubMessage, options)
//                .setResultCallback(new ResultCallback<Status>() {
//                    @Override
//                    public void onResult(@NonNull Status status) {
//                        if (status.isSuccess()) {
//                            Log.i(TAG, "Published successfully.");
//                        } else {
//                            //logAndShowSnackbar("Could not publish, status = " + status);
//                            //mPublishSwitch.setChecked(false);
//                        }
//                    }
//                });
//    }
//    /**
//     * Stops publishing message to nearby devices.
//     */
//    private void unpublish() {
//        Log.i(TAG, "Unpublishing.");
//        Nearby.Messages.unpublish(mGoogleApiClient, mPubMessage);
//    }

    //Receive notification from FireBase
    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DateFormat df = new SimpleDateFormat("MMM d, yyyy HH:mm:ss");
            String ts = df.format(new Date());

            try {
                String message = intent.getStringExtra("FirebaseData");
                JSONObject jsonData = new JSONObject(message);
                JSONObject json_stage = jsonData.getJSONObject("Stage");
                String Stage = json_stage.get("CurrStage").toString();

                sendData SendData = new sendData();
                SendData.setTimeStamp(ts);
                SendData.setStage(Integer.parseInt(Stage));
                SendData.setMessage(message);

                //check Permission
                if(SendData.getStage() == 1){
                    String not1 =  SendData.HandleFireData().get(0);
                    String not2 =  SendData.HandleFireData().get(1);
                    String[] not = {not1, not2};
                    updateNotificationList(not);

                    SendData.setSenderToken(getTheCurrentClient());//
                    SendData.setDataOwnderToken(getTheCurrentClient());//

                    if(checkRequestPermission(SendData.getRequestRange())){
                        SendData.setPermission(true);
                        STRegion region = STRegion.fromString(SendData.getRequestRange());
                        List<PlaceData> places = filter.getPlacesByRange(region);
                        SendData.setTitle("Request Places");
                        SendData.setPlacesInfo(places);
                        SEND(SendData.getSendData());
                    }
                    else{
                        SendData.setPermission(false);
                        SendData.setMessage("");
                        SEND(SendData.getSendData());
                    }
                }
                if(SendData.getStage() == 2){
                    //whether request is accepted
                    JSONObject json_permission = jsonData.getJSONObject("Permission");
                    String permission = json_permission.get("Permission").toString();
                    SendData.setPermission(Boolean.valueOf(permission));
                    List<String> placeGet =  SendData.HandleFireData();
                    if(Boolean.valueOf(permission)){
                        for(int i =0; i<placeGet.size(); i+=2){
                            updateList(placeGet.get(i), placeGet.get(i+1), "");//
                        }
                    } else{
                        Toast.makeText(MainActivity.this,"Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }

            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                Log.d("JSONException", e.toString());
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

    public boolean checkRequestPermission(String range){
        STRegion region = STRegion.fromString(range);

        float range_x = region.getMaxs().getX() - region.getMins().getX();
        float range_y = region.getMaxs().getY() - region.getMins().getY();
        float range_t = region.getMaxs().getT() - region.getMins().getT();

        float S_RANGE = accessProfile.getMinSpaceWindow();
        float T_RANGE = accessProfile.getMinTempWindow();

        if(S_RANGE <= range_x && S_RANGE <= range_y && (T_RANGE <= range_t || T_RANGE == 0.0) ){
            Log.d("RnageRnage", "A"+true);
            return true;
        }
        else{
            Log.d("RnageRnage", "A"+false);
            return false;
        }

    }

//    public boolean checkRequestPermission(String range){
//        float xGridGran = X_CUBE;
//        float yGridGran = Y_CUBE;
//        float tGridGran = T_CUBE;
//        STRegion region = STRegion.fromString(range);
//
//        Log.d("checkRequestPermissionregion", region.toString());
//        float range_x = region.getMaxs().getX() - region.getMins().getX();
//        float range_y = region.getMaxs().getY() - region.getMins().getY();
//        float range_t = region.getMaxs().getT() - region.getMins().getT();
//        STPoint reqMax = region.getMaxs();
//        STPoint reqMin = region.getMins();
//
//        float gran = (float)setData.getGranValue();
//
//        if((double)xGridGran*(double)gran > (double)range_x || (double) yGridGran*(double)gran > (double)range_y || (double)tGridGran*(double)gran> (double)range_t){
//            return false;
//        }
//        else{
//            STPoint perMax;
//            STPoint perMin;
//            String RangeValue = setData.getRangeValue();
//            if(RangeValue.equals("all")){
//                return true;
//            }
//            else{
//                STRegion perRegion = STRegion.fromString(RangeValue);
//                perMax = perRegion.getMaxs();/////
//                perMin = perRegion.getMins();/////
//
//                if( (double)reqMax.getX()>(double)perMax.getX() || (double)reqMax.getY()>(double)perMax.getY() || (double)reqMax.getT()>(double)perMax.getT()
//                    || (double)reqMin.getX()<(double)perMin.getX() || (double)reqMin.getY()<(double)perMin.getY() && (double)reqMin.getT()<(double)perMin.getT()
//                ){Log.d("checkRequestPermission1", "YYY");
//                    return false;
//                } else {
//                    Log.d("checkRequestPermission2", "NNN");
//                    return true;
//                }
//            }
//        }
//    }

    //upadate Place
    public void updateList(String title, String description, String image){
        //update data
        //STRegion region;
        STPoint location;
        //PlaceStore placeStore = new PlaceStore(MainActivity.this);

        //Log.d("checkRequestPermission3", description);
        location = STPoint.fromString(description);//


        if(!"".equals(image)) {
            try {
                Bitmap bitImage = StringToBitMap(image);
                Uri imUri = getImageUri(getApplicationContext(), bitImage, title);
                //String posterPath = getRealPathFromURI(imUri);
                //placeStore.put(title, posterPath, region);
                this.filter.insert(location, title, imUri.toString());/////??imUri.toString()
            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
        } else{
            this.filter.insert(location, title, "");
        }

        //refresh page
        Fragment frag = MainActivity.this.getSupportFragmentManager().findFragmentByTag("PlacesFragment");
        if(frag.isVisible()){
            fHelper.show("PlacesFragment", new PlacesFragment());
        }
    }

    //upadate Notification
    public void updateNotificationList(String[] notification){
        notificationStore NotificationStore = new notificationStore(this);
        NotificationStore.put(notification[0], notification[1]);

        //refresh page
        Fragment frag = MainActivity.this.getSupportFragmentManager().findFragmentByTag("NotificationFragment");
        if(frag!=null && frag.isVisible()){
            fHelper.show("NotificationFragment", new NotificationsFragment());
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
    public String getCurrentLocation() {return currentLocation;}
    public FragmentHelper getFragmentHelper() {return fHelper;}
    public SettingData getSetData() {return setData;}
    public AccessProfile getAccessProfile() {return accessProfile;}
    public LSTFilter getFilter() {return filter;}
    public UserData getUser() {return user;}
    public DatabaseReference getDatabaseReference() {return mDatabaseReference;}
    public FirebaseDatabase getDatabase(){return mDatabase;}

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
        }
//        else if(id == R.id.action_send_places) {
//            tag = "SendFragment";
//            fHelper.show(tag, new SendFragment());
//            return true;
//        }
//        else if(id == R.id.action_request_places) {
//            tag = "RequestFragment";
//            fHelper.show(tag, new RequestFragment());
//            return true;
//        }

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
