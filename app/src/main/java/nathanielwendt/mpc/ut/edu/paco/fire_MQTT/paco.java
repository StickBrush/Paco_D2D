package nathanielwendt.mpc.ut.edu.paco.fire_MQTT;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.ut.mpc.utils.GPSLib;
import com.ut.mpc.utils.LSTFilter;
import com.ut.mpc.utils.LSTFilterException;
import com.ut.mpc.utils.STPoint;
import com.ut.mpc.utils.STRegion;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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

    public void respondTORequest(Intent intent, String ts, AccessProfile accessProfile, PrivacySetting privacySetting) {
        try {
            String message = intent.getStringExtra("FirebaseData");
            JSONObject jsonData = new JSONObject(message);
            JSONObject json_stage = jsonData.getJSONObject("Stage");
            String Stage = json_stage.get("CurrStage").toString();

            sendData SendData = new sendData();
            SendData.setTimeStamp(ts);
            SendData.setStage(Integer.parseInt(Stage));
            SendData.setMessage(message);

            //if(Resource == "restaurant"){
            if(SendData.getStage() == -2){
                JSONObject param = jsonData.getJSONObject("params");
                String type = param.getString("Type");
                ArrayList<String> keyhole = getKeyhole(type, privacySetting);//sendback keyhole
                SendData.setKeyhole(keyhole);
                SendData.setMessage(message);
                SendData.setType(type.toString());
                SendData.setSenderToken(clientId);
                SendData.setDataOwnderToken(clientId);
                SendData.HandleFireData();
                SEND(SendData.getSendData());
                Log.d("type,", SendData.getSendData());
            }

            else if(SendData.getStage() == -1){
                Log.d("type,", message);
                JSONObject data;
                JSONObject param = jsonData.getJSONObject("params");
                String type = param.getString("Type");
                JSONObject json_Keyhole = jsonData.getJSONObject("keyhole");
                Log.d("type", "A"+json_Keyhole.getString("keyhole"));
                if(json_Keyhole.length()>1) {
                    JSONArray array_Keyhole = json_Keyhole.getJSONArray("keyhole");
                    data = gatherKeyMessage(array_Keyhole);
                    SendData.setKey(data);
                    Log.d("type_key", "A"+data);
                }
                SendData.setType(type);
                SendData.setMessage(message);
                SendData.setSenderToken(clientId);
                SendData.setRequesterToken(clientId);
                SendData.HandleFireData();
                SEND(SendData.getSendData());
            }

//            else if(SendData.getStage() == 0){
//                Log.d("type,", message);
//                try{
//                    JSONObject param = jsonData.getJSONObject("params");
//                    String type = param.getString("Type");
//                    if(Boolean.parseBoolean(checkKeyMessage(type, jsonData, privacySetting, accessProfile))){
//                        SendData.setType(type);
//                        SendData.setMessage(message);
//                        SendData.setSenderToken(clientId);
//                        SendData.setDataOwnderToken(clientId);
//                        SendData.HandleFireData();
//                        SEND(SendData.getSendData());
//                    } else{}
//                }catch (JsonIOException e){
//
//                }
//            }

            else if(SendData.getStage() == 1){
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
                Log.d("checkPoK", PoK);
                SendData.setPoK(PoK);

                try{
                    JSONObject param = jsonData.getJSONObject("params");
                    String type = param.getString("Type");
                    if(Boolean.parseBoolean(checkKeyMessage(type, jsonData, privacySetting, accessProfile))){
                        //check AccessProfle
                        try {
                            if(checkRequestPermission(SendData.getRequestRange(), DoublePoK, accessProfile)){
                                if(accessProfile.getAccessLevel() == 1){
                                    SendData.setPermission(true);
                                    SendData.setShowPoK(true);
                                    SendData.setTitle("Request Places");
                                    SEND(SendData.getSendData());
                                } else{
                                    SendData.setPermission(false);
                                    SendData.setShowPoK(true);
                                    SendData.setTitle("Request Places");
                                    SEND(SendData.getSendData());
                                }
                            }
                            else{
                                SendData.setPermission(false);
                                SendData.setShowPoK(false);
                                SendData.setMessage("");
                                SEND(SendData.getSendData());
                            }
                        } catch (LSTFilterException e){
                            Log.d("LSTFilterException", e.toString());
                        }

                        SendData.setType(type);
                        SendData.setMessage(message);
                        SendData.setSenderToken(clientId);
                        SendData.setDataOwnderToken(clientId);
                        SendData.HandleFireData();
                        SEND(SendData.getSendData());

                    } else{}
                }catch (JsonIOException e){

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
                    JSONObject json_showPok = jsonData.getJSONObject("ShowPoK");
                    String showPoK = json_showPok.get("ShowPoK").toString();
                    SendData.HandleFireData();
                    activity.PopupWindowDeny(SendData.getPoK(), SendData, Boolean.parseBoolean(showPoK));
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

    public void respondTOMQTTRequest(String inMessage, String ts, AccessProfile accessProfile, PrivacySetting privacySetting) {
        try {
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

            //if(Resource == "restaurant"){
            if(SendData.getStage() == -2){
                if(!clientId.equals(Token)) {
                    JSONObject json_param = json_data.getJSONObject("params");
                    String type = json_param.getString("Type");

                    ArrayList<String> keyhole = getKeyhole(type, privacySetting);//sendback keyhole
                    SendData.setKeyhole(keyhole);
                    SendData.setMessage(inMessage);
                    SendData.setType(type);
                    SendData.setSenderToken(clientId);
                    SendData.setDataOwnderToken(clientId);
                    SendData.HandleMQTTData();
                    sendMQTT(SendData.getSendData());
                }
            }

            else if(SendData.getStage() == -1){
                if(!clientId.equals(Token)) {
                    JSONObject data;
                    JSONObject json_param = json_data.getJSONObject("params");
                    String type = json_param.getString("Type");
                    JSONObject json_Keyhole = json_data.getJSONObject("keyhole");
                    if(json_Keyhole.length()>1) {
                        JSONArray array_Keyhole = json_Keyhole.getJSONArray("keyhole");
                        data = gatherKeyMessage(array_Keyhole);
                        SendData.setKey(data);
                    }
                    SendData.setType(type);
                    SendData.setMessage(inMessage);
                    SendData.setSenderToken(clientId);
                    SendData.setRequesterToken(clientId);
                    SendData.HandleMQTTData();
                    sendMQTT(SendData.getSendData());
                    Log.d("type_key", "A"+SendData.getSendData());
                }
            }

                else if (SendData.getStage() == 1) {
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

                        try{
                            JSONObject json_param = json_data.getJSONObject("params");
                            String type = json_param.getString("Type");
                            if(Boolean.parseBoolean(checkKeyMessage(type, json_data, privacySetting, accessProfile))) {
                                //check AccessProfle
                                try {
                                    if (checkRequestPermission(SendData.getRequestRange(), DoublePoK, accessProfile)) {
                                        if(accessProfile.getAccessLevel() == 1){
                                            SendData.setPermission(true);
                                            SendData.setShowPoK(true);
                                            SendData.setTitle("Request Places");
                                            sendMQTT(SendData.getSendData());
                                        } else{
                                            SendData.setPermission(false);
                                            SendData.setShowPoK(true);
                                            SendData.setTitle("Request Places");
                                            sendMQTT(SendData.getSendData());
                                        }
                                    } else {
                                        SendData.setPermission(false);
                                        SendData.setShowPoK(false);
                                        SendData.setMessage("");
                                        sendMQTT(SendData.getSendData());//
                                    }
                                } catch (LSTFilterException e) {
                                    Log.d("LSTFilterException", e.toString());
                                }
                            } else{}
                        }catch (JSONException e){
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
                        JSONObject json_showPok = json_data.getJSONObject("ShowPoK");
                        String showPoK  = json_showPok.get("ShowPoK").toString();
                        SendData.HandleMQTTData();
                        activity.PopupWindowDeny(SendData.getPoK(), SendData, Boolean.parseBoolean(showPoK));
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

    public ArrayList<String> getKeyhole(String type, PrivacySetting privacySetting){
            int level = privacySetting.getLevel(type);
            return privacySetting.getRequireType(level);
    }

    public JSONObject gatherKeyMessage(JSONArray jsonArray){
        try {
            JSONObject data = new JSONObject();
            for(int i=0;i<jsonArray.length(); ++i) {
                if(jsonArray.getString(i) == "location"){
                    data.put("location", activity.getCurrentLocation());
                } else if(jsonArray.getString(i) == "identity"){
                    data.put("identity", activity.getClient());
                }
            }
            return data;
        } catch (JSONException e) {
            Log.e("JsonException", e.toString());
            return null;
        }
    }

    public String checkKeyMessage(String type, JSONObject jsonData, PrivacySetting privacySetting, AccessProfile accessProfile){
        try {
            boolean bool1 = true,
                    bool2 = true;
            String location = null;
            String identity = null;
            JSONObject json_key;

            if(jsonData.getString("key") != null){
                json_key = jsonData.getJSONObject("key");

                //String json_type = jsonData.getJSONObject("type").toString();
                int level = privacySetting.getLevel(type);
                ArrayList<String> needKey = privacySetting.getRequireType(level);
                for(int i=0; i<needKey.size(); ++i){
                    if(needKey.get(i)=="location"){
                        location = json_key.getString("location");
                    }
                    else{
                        identity = json_key.getString("identity");////
                    }
                }
            }
            if(location!=null){
                if(CheckNearby(location)){bool1=true;}
                else{bool1=false;}
            }else{
                bool1=true;
            }
            if(identity!=null){
//                if(CheckNearby(location)){bool1=true;}
//                else{bool1=false;}
                bool2 = true;
            } else {
                bool2 = true;
            }

            return Boolean.toString(bool1&bool2);

        }catch (JSONException e){
            Log.d("JSONException", e.toString());
            return null;
        }
    }

    private boolean CheckNearby(String OtherLocation){

        String curLocation = activity.getCurrentLocation();
        curLocation = curLocation.replaceAll("[()]","");
        String[] curLocSplits = curLocation.split(", ");

        OtherLocation = OtherLocation.replaceAll("[()]","");
        String[] otherLocationSplits = OtherLocation.split(",");

        if(!curLocation.equals(OtherLocation)){
            if( Math.pow( (Double.valueOf(curLocSplits[0])-Double.valueOf(otherLocationSplits[0])), 2) + Math.pow( (Double.valueOf(curLocSplits[1])-Double.valueOf(otherLocationSplits[1])), 2) <= Math.pow(100, 2)){
                return true;
            } else{
                return false;
            }
        } else{
            return false;
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
