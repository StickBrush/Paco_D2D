package nathanielwendt.mpc.ut.edu.paco.fire_MQTT;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ut.mpc.utils.GPSLib;
import com.ut.mpc.utils.LSTFilter;
import com.ut.mpc.utils.LSTFilterException;
import com.ut.mpc.utils.STPoint;
import com.ut.mpc.utils.STRegion;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nathanielwendt.mpc.ut.edu.paco.Data.PlaceData;
import nathanielwendt.mpc.ut.edu.paco.FragmentHelper;
import nathanielwendt.mpc.ut.edu.paco.MainActivity;
import nathanielwendt.mpc.ut.edu.paco.NotificationsFragment;
import nathanielwendt.mpc.ut.edu.paco.PlacesFragment;
import nathanielwendt.mpc.ut.edu.paco.utils.notificationStore;

public class paco {
    private LSTFilter lstFilter;

    String clientId;
    MqttAndroidClient MQTTclient;
    private Context context;
    private MainActivity activity;
    private RequestQueue requestQueue;

    public paco(Context context, MainActivity activity, LSTFilter lstFilter){
        this.context=context;
        this.activity = activity;
        this.lstFilter = lstFilter;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId(){
        return this.clientId;
    }

    public void setMQTTClient(MqttAndroidClient MQTTclient) {
        this.MQTTclient = MQTTclient;
    }

    public MqttAndroidClient getMQTTClient(){
        return this.MQTTclient;
    }

    public boolean checkRequestPermission(String range, Double PoK, AccessProfile accessProfile) throws LSTFilterException {
        STRegion region = STRegion.fromString(range);

        float range_x = region.getMaxs().getX() - region.getMins().getX();
        float range_y = region.getMaxs().getY() - region.getMins().getY();
        float range_t = region.getMaxs().getT() - region.getMins().getT();
        double rangeD_x = GPSLib.spatialDistanceBetween(new STPoint((float)0, (float)0, (float)0), new STPoint((float)range_x, (float)0, (float)0 ), com.ut.mpc.setup.Constants.SPATIAL_TYPE);
        double rangeD_y = GPSLib.spatialDistanceBetween(new STPoint((float)0, (float)0, (float)0), new STPoint((float)range_y, (float)0, (float)0 ), com.ut.mpc.setup.Constants.SPATIAL_TYPE);
        double rangeD_t = GPSLib.spatialDistanceBetween(new STPoint((float)0, (float)0, (float)0), new STPoint((float)range_t, (float)0, (float)0 ), com.ut.mpc.setup.Constants.SPATIAL_TYPE);

        double S_RANGE = (double) accessProfile.getMinSpaceWindow();
        double T_RANGE = (double) accessProfile.getMinTempWindow();

        if(S_RANGE <= rangeD_x && S_RANGE <= rangeD_y /*&& (T_RANGE <= rangeD_t || T_RANGE == 0.0)*/ /*&& PoK >=0.7*/){
            return true;
        }
        else{
            return false;
        }
    }

    public void respondTORequest(Intent intent, String ts, AccessProfile accessProfile) {
        try {
            String message = intent.getStringExtra("FirebaseData");
            JSONObject jsonData = new JSONObject(message);
            JSONObject json_stage = jsonData.getJSONObject("Stage");
            String Stage = json_stage.get("CurrStage").toString();

            sendData SendData = new sendData();
            SendData.setTimeStamp(ts);
            SendData.setStage(Integer.parseInt(Stage));
            SendData.setMessage(message);

            if(SendData.getStage() == 1){
                String not1 =  SendData.HandleFireData().get(0);
                String not2 =  SendData.HandleFireData().get(1);
                String[] not = {not1, not2};
                updateNotificationList(not);

                SendData.setSenderToken(getClientId());
                SendData.setDataOwnderToken(getClientId());

                //Calculate PoK
                STRegion region = STRegion.fromString(SendData.getRequestRange());
                Double DoublePoK = lstFilter.windowPoK(region);//
                String PoK = String.format("%.2f", DoublePoK * 100.0) + "%";
                Log.d("checkPoK", Double.toString(DoublePoK));
                Log.d("checkPoK", PoK);
                SendData.setPoK(PoK);

                //check Permission
                try {
                    if(checkRequestPermission(SendData.getRequestRange(), DoublePoK, accessProfile)){
                        SendData.setPermission(true);
                        SendData.setTitle("Request Places");
                        SEND(SendData.getSendData());
                    }
                    else{
                        SendData.setPermission(false);
                        SendData.setMessage("");
                        SEND(SendData.getSendData());
                    }
                } catch (LSTFilterException e){
                    Log.d("LSTFilterException", e.toString());
                }

            }

            if(SendData.getStage() == 2){
                JSONObject json_permission = jsonData.getJSONObject("Permission");
                String permission = json_permission.get("Permission").toString();
                SendData.setPermission(Boolean.valueOf(permission));

                SendData.setMessage(message);
                String not1 =  SendData.HandleFireData().get(0);
                String not2 =  SendData.HandleFireData().get(1);
                String[] not = {not1, not2};
                updateNotificationList(not);

                if(Boolean.valueOf(permission)){
                    SendData.setSenderToken(clientId);
                    SendData.setRequesterToken(clientId);

                    SendData.HandleFireData();
                    activity.PopupWindowAccept(SendData.getPoK(), SendData, "Fire");
                } else{
                    SendData.HandleFireData();
                    activity.PopupWindowDeny(SendData.getPoK(), SendData);
                    //Toast.makeText(MainActivity.this,"Permission Denied", Toast.LENGTH_LONG).show();
                }
            }

            if(SendData.getStage() == 3){
                SendData.setMessage(message);
                SendData.setSenderToken(clientId);
                SendData.setDataOwnderToken(clientId);
                SendData.HandleFireData();

                STRegion region = STRegion.fromString(SendData.getRequestRange());
                List<PlaceData> places = lstFilter.getPlacesByRange(region);////
                SendData.setTitle("Request Places");
                SendData.setPlacesInfo(places);
                SEND(SendData.getSendData());
            }

            if(SendData.getStage() == 4){
                List<String> placeGet =  SendData.HandleFireData();
                for(int i =0; i<placeGet.size(); i+=2){
                    updateList(placeGet.get(i), placeGet.get(i+1), "");//
                }
            }

        } catch (JSONException e) {
            Log.d("JSONException", e.toString());
        }
    }

    public void respondTOMQTTRequest(String inMessage, String ts, AccessProfile accessProfile) {
        try {
            //String message = intent.getStringExtra("FirebaseData");
            JSONObject jsonData = new JSONObject(inMessage);
            JSONObject json_data = jsonData.getJSONObject("data");
            JSONObject json_stage = json_data .getJSONObject("Stage");
            String Stage = json_stage.get("CurrStage").toString();
            JSONObject json_token = json_data .getJSONObject("Token");
            String Token = json_token.get("SenderToken").toString();

            sendData SendData = new sendData();
            SendData.setTimeStamp(ts);
            SendData.setStage(Integer.parseInt(Stage));
            SendData.setMessage(json_data.toString());

            if (SendData.getStage() == 1) {
                    if(!clientId.equals(Token)) {//
                        String not1 = SendData.HandleMQTTData().get(0);
                        String not2 = SendData.HandleMQTTData().get(1);
                        String[] not = {not1, not2};
                        updateNotificationList(not);

                        SendData.setSenderToken(getClientId());
                        SendData.setDataOwnderToken(getClientId());

                        //Calculate PoK
                        STRegion region = STRegion.fromString(SendData.getRequestRange());
                        Double DoublePoK = lstFilter.windowPoK(region);//
                        String PoK = String.format("%.2f", DoublePoK * 100.0) + "%";
                        Log.d("checkPoK", PoK);
                        SendData.setPoK(PoK);

                        //check Permission
                        try {
                            if (checkRequestPermission(SendData.getRequestRange(), DoublePoK, accessProfile)) {
                                SendData.setPermission(true);
                                SendData.setTitle("Request Places");
                                sendMQTT(SendData.getSendData());
                            } else {
                                SendData.setPermission(false);
                                SendData.setMessage("");
                                sendMQTT(SendData.getSendData());//
                            }
                        } catch (LSTFilterException e) {
                            Log.d("LSTFilterException", e.toString());
                        }
                    }
                }
                else if(SendData.getStage() == 2 && !clientId.equals(Token) ){
                    JSONObject json_permission = json_data.getJSONObject("Permission");
                    String permission = json_permission.get("Permission").toString();
                    SendData.setPermission(Boolean.valueOf(permission));

                    SendData.setMessage(inMessage);
                    String not1 =  SendData.HandleMQTTData().get(0);
                    String not2 =  SendData.HandleMQTTData().get(1);
                    String[] not = {not1, not2};
                    updateNotificationList(not);

                    if(Boolean.valueOf(permission)){
                        SendData.setSenderToken(clientId);
                        SendData.setRequesterToken(clientId);

                        SendData.HandleMQTTData();
                        activity.PopupWindowAccept(SendData.getPoK(), SendData, "MQTT");
                    } else{
                        SendData.HandleMQTTData();
                        activity.PopupWindowDeny(SendData.getPoK(), SendData);
                    }
                }

                else if (SendData.getStage() == 3 && !clientId.equals(Token)) {
                    SendData.setMessage(inMessage);
                    SendData.setSenderToken(clientId);
                    SendData.setDataOwnderToken(clientId);
                    SendData.HandleMQTTData();

                    STRegion region = STRegion.fromString(SendData.getRequestRange());
                    List<PlaceData> places = lstFilter.getPlacesByRange(region);////
                    SendData.setTitle("Request Places");
                    SendData.setPlacesInfo(places);
                    sendMQTT(SendData.getSendData());
                }

                else if (SendData.getStage() == 4 && !clientId.equals(Token)) {
                    List<String> placeGet = SendData.HandleFireData();
                    for (int i = 0; i < placeGet.size(); i += 2) {
                        updateList(placeGet.get(i), placeGet.get(i + 1), "");//
                    }
                }

        } catch (
                JSONException e) {
            Log.d("JSONException", e.toString());
        }
    }

    //FireBase : SEND
    public void SEND(String data)
    {
        final String savedata= data;
        String URL="https://fcm.googleapis.com/fcm/send";

        requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject objres=new JSONObject(response);
                    Log.d("Send","Firebase Send Successfully ");

                } catch (JSONException e) {
                    Log.d("Send","Server Error");

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("SendError", error.getMessage());

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
            getMQTTClient().publish("Firebase_MQTT", fianl_message);
            Log.d("Send", "Send to MQTT Successfully");
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
            Log.d("Send", "Failed to Send");
        }
    }

    //upadate Place
    public void updateList(String title, String description, String image){
        //update data
        //STRegion region;
        STPoint location;
        //PlaceStore placeStore = new PlaceStore(MainActivity.this);

        location = STPoint.fromString(description);//

        if(!"".equals(image)) {
            try {
                Bitmap bitImage = StringToBitMap(image);
                Uri imUri = getImageUri(context, bitImage, title);
                //String posterPath = getRealPathFromURI(imUri);
                //placeStore.put(title, posterPath, region);
                lstFilter.insert(location, title, imUri.toString());/////??imUri.toString()
            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
        } else{
            lstFilter.insert(location, title, "");
        }

        //refresh page
        Fragment frag = activity.getSupportFragmentManager().findFragmentByTag("PlacesFragment");
        if(frag.isVisible()){
            activity.getFragmentHelper().show("PlacesFragment", new PlacesFragment());
        }
    }

    //upadate Notification
    public void updateNotificationList(String[] notification){
        notificationStore NotificationStore = new notificationStore(this.activity);
        NotificationStore.put(notification[0], notification[1]);

        //refresh page
        Fragment frag = activity.getSupportFragmentManager().findFragmentByTag("NotificationFragment");
        if(frag!=null && frag.isVisible()){
            activity.getFragmentHelper().show("NotificationFragment", new NotificationsFragment());
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

    //image
    public Uri getImageUri(Context inContext, Bitmap inImage, String title) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, title, null);
        return Uri.parse(path);
    }

}
