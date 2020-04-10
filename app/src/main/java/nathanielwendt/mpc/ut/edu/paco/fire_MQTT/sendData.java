package nathanielwendt.mpc.ut.edu.paco.fire_MQTT;

import android.util.Log;

import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import nathanielwendt.mpc.ut.edu.paco.Data.PlaceData;

public class sendData {

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) { this.stage =  stage; }

    public String getSenderToken() {
        return senderToken;
    }

    public void setSenderToken(String senderToken) { this.senderToken =  senderToken; }

    public String getDataOwnderToken() {
        return dataOwnderToken;
    }

    public void setDataOwnderToken(String dataOwnderToken) { this.dataOwnderToken =  dataOwnderToken; }

    public String getRequesterToken() { return requesterToken; }

    public void setRequesterToken(String senderToken) { this.requesterToken =  senderToken; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUri() { return uri; }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getRequestRange() { return requestRange; }

    public void setRequestRange(String requestRange) {
        this.requestRange = requestRange;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean getPermission() { return permission; }

    public void setPermission(boolean permission) {
        this.permission = permission;
    }

    public List<PlaceData> getPlacesInfo() { return placesInfo; }

    public void setPlacesInfo(List<PlaceData> placesInfo) {
        this.placesInfo = placesInfo;
    }

    public String getPoK() {
        return PoK;
    }

    public void setPoK(String PoK) {
        this.PoK = PoK;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) { this.type = type; }

    public ArrayList<String> getKeyhole() {
        return keyhole;
    }

    public void setKeyhole(ArrayList<String> keyhole) { this.keyhole = keyhole; }

    public JSONObject getKey() {
        return key;
    }

    public void setKey(JSONObject key) { this.key = key; }

    public boolean getShowPoK() {
        return showPoK;
    }

    public void setShowPoK(boolean showPoK) { this.showPoK = showPoK; }


    private int stage;
    private String dataOwnderToken;
    private String requesterToken;
    private String senderToken;
    private String title;
    private String message;
    private String uri;
    private String timeStamp;
    private String requestRange;
    private boolean permission;
    private List<PlaceData> placesInfo;
    private String PoK;
    private String type;
    private ArrayList<String> keyhole;
    private JSONObject key;
    private boolean showPoK;

    public sendData(){
    }

    //Handle receive message
    public List<String> HandleFireData(){
        //Data Owner Handle Request
        if(this.stage == -2){
            try {
                JSONObject jsonData = new JSONObject(message);
                JSONObject json_token = jsonData.getJSONObject("Token");
                JSONObject json_params = jsonData.getJSONObject("params");
                String token = json_token.getString("SenderToken");
                String title = json_params.get("title").toString();
                String range = json_params.get("range").toString();
                this.setTitle(title);
                this.setRequestRange(range);
                this.setRequesterToken(token);

                return null;

            } catch (JSONException e) {
                Log.d("JSONException", e.toString());
                return null;
            }
        }

        else if(this.stage == -1){
            try {
                JSONObject jsonData = new JSONObject(message);
                JSONObject json_token = jsonData.getJSONObject("Token");
                JSONObject json_params = jsonData.getJSONObject("params");
                String token = json_token.getString("SenderToken");
                String title = json_params.get("title").toString();
                String range = json_params.get("range").toString();
                this.setTitle(title);
                this.setRequestRange(range);
                this.setDataOwnderToken(token);

                return null;

            } catch (JSONException e) {
                Log.d("JSONException", e.toString());
                return null;
            }
        }

//        else if(this.stage == 0){
//            try {
//                JSONObject jsonData = new JSONObject(message);
//                JSONObject json_token = jsonData.getJSONObject("Token");
//                JSONObject json_params = jsonData.getJSONObject("params");
//                String token = json_token.getString("SenderToken");
//                String title = json_params.get("title").toString();
//                String range = json_params.get("range").toString();
//                this.setTitle(title);
//                this.setRequestRange(range);
//                this.setRequesterToken(token);
//
//                return null;
//
//            } catch (JSONException e) {
//                Log.d("JSONException", e.toString());
//                return null;
//            }
//        }

        else if(this.stage == 1){
            try {
                JSONObject jsonData = new JSONObject(message);

                JSONObject jsonParam = jsonData.getJSONObject("params");
                JSONObject json_token = jsonData.getJSONObject("Token");
                String title = jsonParam.get("title").toString();
                String range = jsonParam.get("range").toString();
                String senderToken = json_token.get("SenderToken").toString();
                String fireBase_msg = "Title: " + title +"\n" + "Request From: " +"\n" + senderToken +"\n" + "Request Time: " +"\n" + timeStamp + "\n" + "Request Range: " +"\n" + range /*+"\n" + "Latitude: " + latitude +"\n" + "Longitude: " + longitude*/;

                this.setTitle(title);
                this.setRequestRange(range);
                this.setRequesterToken(senderToken);

                List<String> msg = new ArrayList();
                msg.add(senderToken+timeStamp);
                msg.add(fireBase_msg);
                return msg;

            } catch (JSONException e) {
                Log.d("JSONException", e.toString());
                return null;
            }
        }

        //Requester Handle Return Permission and PoK
        else if(this.stage == 2){
            try {
                JSONObject jsonData = new JSONObject(message);
                if(permission){
                    JSONObject json_token = jsonData.getJSONObject("Token");
                    JSONObject json_params = jsonData.getJSONObject("params");
                    JSONObject json_PoK = jsonData.getJSONObject("PoK");
                    String token = json_token.getString("SenderToken");
                    String title = json_params.get("title").toString();
                    String range = json_params.get("range").toString();
                    String PoK = json_PoK.getString("PoK");

                    this.setTitle(title);
                    this.setRequestRange(range);
                    this.setDataOwnderToken(token);
                    this.setPoK(PoK);

                    List<String> msg = new ArrayList();
                    msg.add(senderToken);
                    msg.add(PoK);
                    return msg;
                } else {
                    JSONObject json_PoK = jsonData.getJSONObject("PoK");
                    String PoK = json_PoK.getString("PoK");
                    this.setPoK(PoK);

                    List<String> msg = new ArrayList<String>();
                    msg.add("Permission");
                    msg.add("Permission Denied");
                    return msg;
                }

            } catch (JSONException e) {
                Log.d("JSONException", e.toString());
                return null;
            }
        }

        //Data Owner Handle further Request
        else if(this.stage == 3){
            try {
                JSONObject jsonData = new JSONObject(message);
                JSONObject json_token = jsonData.getJSONObject("Token");
                JSONObject json_params = jsonData.getJSONObject("params");
                String token = json_token.get("SenderToken").toString();
                String range = json_params.getString("range");
                this.setRequesterToken(token);
                this.setRequestRange(range);

                List<String> storePlace = new ArrayList();
                JSONArray json_places = jsonData.getJSONArray("places");
                for (int i = 0; i < json_places.length(); ++i) {
                    JSONObject place = json_places.getJSONObject(i);
                    String location = place.getString("location");
                    String placeTitle = place.getString("title");
                    storePlace.add(placeTitle);
                    storePlace.add(location);
                }
                return storePlace;

            } catch (JSONException e) {
                Log.d("JSONException", e.toString());
                return null;
            }
        }

        //Requester Handle list of places
        else if(this.stage == 4){
            try {
                JSONObject jsonData = new JSONObject(message);
                //JSONObject json_params = jsonData.getJSONObject("params");

                List<String> storePlace = new ArrayList();
                JSONArray json_places = jsonData.getJSONArray("places");
                for (int i = 0; i < json_places.length(); ++i) {
                    JSONObject place = json_places.getJSONObject(i);
                    String location = place.getString("location");
                    String placeTitle = place.getString("title");
                    storePlace.add(placeTitle);
                    storePlace.add(location);
                }
                return storePlace;

            } catch (JSONException e) {
                Log.d("JSONException", e.toString());
                return null;
            }
        }
        else{return null;}

    }

    public List<String> HandleMQTTData(){
        //Data Owner Handle Request
        if(this.stage == -2){
            try {
                JSONObject jsonData = new JSONObject(message);
                JSONObject json_data = jsonData.getJSONObject("data");
                JSONObject jsonParam = json_data.getJSONObject("params");
                JSONObject json_token = json_data.getJSONObject("Token");
                String title = jsonParam.get("title").toString();
                String range = jsonParam.get("range").toString();
                String senderToken = json_token.get("SenderToken").toString();
                //String fireBase_msg = "Title: " + title +"\n" + "Request From: " +"\n" + senderToken +"\n" + "Request Time: " +"\n" + timeStamp + "\n" + "Request Range: " +"\n" + range /*+"\n" + "Latitude: " + latitude +"\n" + "Longitude: " + longitude*/;

                this.setTitle(title);
                this.setRequestRange(range);
                this.setRequesterToken(senderToken);
                return null;

            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                Log.d("JSONException", e.toString());
                return null;
            }
        }

        else if(this.stage == -1){
            try {
                JSONObject jsonData = new JSONObject(message);
                JSONObject json_data = jsonData.getJSONObject("data");
                JSONObject jsonParam = json_data.getJSONObject("params");

                JSONObject json_token = json_data.getJSONObject("Token");
                String title = jsonParam.get("title").toString();
                String range = jsonParam.get("range").toString();
                String senderToken = json_token.get("SenderToken").toString();
                //String fireBase_msg = "Title: " + title +"\n" + "Request From: " +"\n" + senderToken +"\n" + "Request Time: " +"\n" + timeStamp + "\n" + "Request Range: " +"\n" + range /*+"\n" + "Latitude: " + latitude +"\n" + "Longitude: " + longitude*/;

                this.setTitle(title);
                this.setRequestRange(range);
                this.setDataOwnderToken(senderToken);

                return null;

            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                Log.d("JSONException", e.toString());
                return null;
            }
        }

        else if(this.stage == 1){
            try {
                JSONObject jsonData = new JSONObject(message);

                JSONObject jsonParam = jsonData.getJSONObject("params");
                JSONObject json_token = jsonData.getJSONObject("Token");
                String title = jsonParam.get("title").toString();
                String range = jsonParam.get("range").toString();
                String senderToken = json_token.get("SenderToken").toString();
                String fireBase_msg = "Title: " + title +"\n" + "Request From: " +"\n" + senderToken +"\n" + "Request Time: " +"\n" + timeStamp + "\n" + "Request Range: " +"\n" + range /*+"\n" + "Latitude: " + latitude +"\n" + "Longitude: " + longitude*/;

                this.setTitle(title);
                this.setRequestRange(range);
                this.setRequesterToken(senderToken);

                List<String> msg = new ArrayList();
                msg.add(senderToken+timeStamp);
                msg.add(fireBase_msg);
                return msg;

            } catch (JSONException e) {
                //Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                Log.d("JSONException", e.toString());
                return null;
            }
        }

        //Requester Handle Return Permission and PoK
        else if(this.stage == 2){
            try {
                JSONObject json_data = new JSONObject(message);
                JSONObject jsonData = json_data.getJSONObject("data");
                if(permission){
                    JSONObject json_token = jsonData.getJSONObject("Token");
                    JSONObject json_params = jsonData.getJSONObject("params");
                    JSONObject json_PoK = jsonData.getJSONObject("PoK");
                    String token = json_token.getString("SenderToken");
                    String title = json_params.get("title").toString();
                    String range = json_params.get("range").toString();
                    String PoK = json_PoK.getString("PoK");

                    this.setTitle(title);
                    this.setRequestRange(range);
                    this.setDataOwnderToken(token);
                    this.setPoK(PoK);

                    List<String> msg = new ArrayList();
                    msg.add(senderToken);
                    msg.add(PoK);
                    return msg;
                } else {
                    JSONObject json_PoK = jsonData.getJSONObject("PoK");
                    String PoK = json_PoK.getString("PoK");
                    this.setPoK(PoK);

                    List<String> msg = new ArrayList<String>();
                    msg.add("Permission");
                    msg.add("Permission Denied");
                    return msg;
                }

            } catch (JSONException e) {
                Log.d("JSONException", e.toString());
                return null;
            }
        }

        //Data Owner Handle further Request
        else if(this.stage == 3){
            try {
                JSONObject json_data = new JSONObject(message);
                JSONObject jsonData = json_data.getJSONObject("data");
                JSONObject json_token = jsonData.getJSONObject("Token");
                JSONObject json_params = jsonData.getJSONObject("params");
                String token = json_token.get("SenderToken").toString();
                String range = json_params.getString("range");
                this.setRequesterToken(token);
                this.setRequestRange(range);

                List<String> storePlace = new ArrayList();
                JSONArray json_places = jsonData.getJSONArray("places");
                for (int i = 0; i < json_places.length(); ++i) {
                    JSONObject place = json_places.getJSONObject(i);
                    String location = place.getString("location");
                    String placeTitle = place.getString("title");
                    storePlace.add(placeTitle);
                    storePlace.add(location);
                }
                return storePlace;

            } catch (JSONException e) {
                Log.d("JSONException", e.toString());
                return null;
            }
        }

        //Requester Handle list of places
        else if(this.stage == 4){
            try {
                JSONObject json_data = new JSONObject(message);
                JSONObject jsonData = json_data.getJSONObject("data");
                //JSONObject json_params = jsonData.getJSONObject("params");

                List<String> storePlace = new ArrayList();
                JSONArray json_places = jsonData.getJSONArray("places");
                for (int i = 0; i < json_places.length(); ++i) {
                    JSONObject place = json_places.getJSONObject(i);
                    String location = place.getString("location");
                    String placeTitle = place.getString("title");
                    storePlace.add(placeTitle);
                    storePlace.add(location);
                }
                return storePlace;

            } catch (JSONException e) {
                Log.d("JSONException", e.toString());
                return null;
            }
        }
        else{return null;}

    }


    public String getSendData(){
        //starting to make a request
        if(stage == -3){
            String data = "{\n" +
                    "\t\"to\": \"" + this.dataOwnderToken + "\",\n" +
                    "\t\"data\":{\n" +
                    "\t\t\"params\" : {\n" +
                    "\t\t\t\t\"title\" : \"" + this.title + "\",\n" +
                    "        \t\t\"range\" : \"" + this.message + "\",\n" +
                    "        \t\t\"Type\" : \"" + this.type + "\"\n" +
                    "    \t\t\t}\n" +
                    "\t\t\t\n" +
                    "\t\t}\n" +
                    "}";
            try{
                JSONObject inData = new JSONObject(data);
                JSONObject oldData = inData.getJSONObject("data");
                //put token in
                JSONObject extraToken = new JSONObject();
                extraToken.put("SenderToken", requesterToken);
                oldData.put("Token", extraToken);
                //put stage in
                JSONObject extraStage = new JSONObject();
                extraStage.put("CurrStage", stage+1);
                oldData.put("Stage", extraStage);

                inData.put("data", oldData);
                String strData = inData.toString();
                return strData;
            }
            catch (JSONException e){
                Log.d("JSONException", e.toString());
                return null;
            }
        }

        if(stage == -2){
            String data = "{\n" +
                    "\t\"to\": \"" + this.requesterToken + "\",\n" +
                    "\t\"data\":{\n" +
                    "\t\t\"params\" : {\n" +
                    "\t\t\t\t\"title\" : \"" + this.title + "\",\n" +
                    "        \t\t\"range\" : \"" + this.requestRange + "\",\n" +
                    "        \t\t\"Type\" : \"" + this.type + "\"\n" +
                    "    \t\t\t}\n" +
                    "\t\t\t\n" +
                    "\t\t}\n" +
                    "}";
            try{
                JSONObject inData = new JSONObject(data);
                JSONObject oldData = inData.getJSONObject("data");
                //put token in
                JSONObject extraToken = new JSONObject();
                extraToken.put("SenderToken", dataOwnderToken);
                oldData.put("Token", extraToken);
                //put stage in
                JSONObject extraStage = new JSONObject();
                extraStage.put("CurrStage", stage+1);
                oldData.put("Stage", extraStage);
                //put key
                JSONObject extraKeyhole = new JSONObject();
                JSONArray keyholeArray = new JSONArray();
                for(int i=0; i<keyhole.size(); ++i){
                    keyholeArray.put(keyhole.get(i));
                }
                extraKeyhole.put("keyhole", this.keyhole);
                oldData.put("keyhole", extraKeyhole);//next stage is 1

                inData.put("data", oldData);
                String strData = inData.toString();
                return strData;
            }
            catch (JSONException e){
                return null;
            }
        }

        if(stage == -1){
            String data = "{\n" +
                    "\t\"to\": \"" + this.dataOwnderToken + "\",\n" +
                    "\t\"data\":{\n" +
                    "\t\t\"params\" : {\n" +
                    "\t\t\t\t\"title\" : \"" + this.title + "\",\n" +
                    "        \t\t\"range\" : \"" + this.requestRange + "\",\n" +
                    "        \t\t\"Type\" : \"" + this.type + "\"\n" +
                    "    \t\t\t}\n" +
                    "\t\t\t\n" +
                    "\t\t}\n" +
                    "}";
            try{
                JSONObject inData = new JSONObject(data);
                JSONObject oldData = inData.getJSONObject("data");
                //put token in
                JSONObject extraToken = new JSONObject();
                extraToken.put("SenderToken", requesterToken);
                oldData.put("Token", extraToken);
                //put stage in
                JSONObject extraStage = new JSONObject();
                extraStage.put("CurrStage", stage+2);//
                oldData.put("Stage", extraStage);//next stage is 1
                //put key
                JSONObject extraKey = new JSONObject();
                extraKey.put("key", this.key);
                oldData.put("key", extraKey);//next stage is 1

                inData.put("data", oldData);
                String strData = inData.toString();
                return strData;
            }
            catch (JSONException e){
                Log.d("JSONException", e.toString());
                return null;
            }
        }

//        if(stage == 0){
//            String data = "{\n" +
//                    "\t\"to\": \"" + this.dataOwnderToken + "\",\n" +
//                    "\t\"data\":{\n" +
//                    "\t\t\"params\" : {\n" +
//                    "\t\t\t\t\"title\" : \"" + this.title + "\",\n" +
//                    "        \t\t\"range\" : \"" + this.message + "\",\n" +
//                    "        \t\t\"Type\" : \"" + this.type + "\"\n" +
//                    /*"        \t\t\"image\" : \"" + this.uri + "\"\n" +*/
//                    "    \t\t\t}\n" +
//                    "\t\t\t\n" +
//                    "\t\t}\n" +
//                    "}";
//            try{
//                JSONObject inData = new JSONObject(data);
//                JSONObject oldData = inData.getJSONObject("data");
//                //put token in
//                JSONObject extraToken = new JSONObject();
//                extraToken.put("SenderToken", requesterToken);
//                oldData.put("Token", extraToken);
//                //put stage in
//                JSONObject extraStage = new JSONObject();
//                extraStage.put("CurrStage", stage+1);
//                oldData.put("Stage", extraStage);//next stage is 1
//
//                inData.put("data", oldData);
//                String strData = inData.toString();
//                return strData;
//            }
//            catch (JSONException e){
//                Log.d("JSONException", e.toString());
//                return null;
//            }
//        }

        if(stage == 1){

            String data = "{\n" +
                    "\t\"to\": \"" + this.requesterToken + "\",\n" +
                    "\t\"data\":{\n" +
                    "\t\t\"params\" : {\n" +
                    "\t\t\t\t\"title\" : \"" + this.title + "\",\n" +
                    "        \t\t\"range\" : \"" + this.requestRange + "\",\n" +
                    "        \t\t\"Type\" : \"" + this.type + "\"\n" +
                    /*"        \t\t\"image\" : \"" + this.uri + "\"\n" +*/
                    "    \t\t\t}\n" +
                    "\t\t\t\n" +
                    "\t\t}\n" +
                    "}";

            if(permission==true){
                try{
                    JSONObject inData = new JSONObject(data);
                    JSONObject oldData = inData.getJSONObject("data");

                    //put token in
                    JSONObject extraToken = new JSONObject();
                    extraToken.put("SenderToken", dataOwnderToken);
                    JSONObject extraPermission = new JSONObject();
                    extraPermission.put("Permission", permission);
                    oldData.put("Token", extraToken);
                    //put Permission in
                    oldData.put("Permission", extraPermission);
                    //put stage in
                    JSONObject extraStage = new JSONObject();
                    extraStage.put("CurrStage", stage+1);//next stage is 2
                    oldData.put("Stage", extraStage);

                    //put PoK in
                    JSONObject extraPoK = new JSONObject();
                    extraPoK.put("PoK", PoK);
                    oldData.put("PoK", extraPoK);

                    inData.put("data", oldData);
                    String strData = inData.toString();
                    return strData;
                }
                catch (JSONException e){
                    Log.d("JSONException", e.toString());
                    return null;
                }
            } else {
                try{
                    JSONObject inData = new JSONObject(data);
                    JSONObject oldData = inData.getJSONObject("data");

                    //put token in
                    JSONObject extraToken = new JSONObject();
                    extraToken.put("SenderToken", dataOwnderToken);
                    JSONObject extraPermission = new JSONObject();
                    extraPermission.put("Permission", permission);
                    oldData.put("Token", extraToken);
                    //put Permission in
                    oldData.put("Permission", extraPermission);
                    //put stage in
                    JSONObject extraStage = new JSONObject();
                    extraStage.put("CurrStage", "2");//next stage is 2
                    oldData.put("Stage", extraStage);
                    //put PoK in
                    JSONObject extraPoK = new JSONObject();
                    extraPoK.put("PoK", PoK);
                    oldData.put("PoK", extraPoK);
                    //put PoK in
                    JSONObject extraShowPoK = new JSONObject();
                    extraShowPoK.put("ShowPoK", showPoK);
                    oldData.put("ShowPoK", extraShowPoK);

                    inData.put("data", oldData);
                    String strData = inData.toString();
                    return strData;
                }
                catch (JSONException e){
                    Log.d("JSONException", e.toString());
                    return null;
                }
            }
        }

        if(stage == 2){

            String data = "{\n" +
                    "\t\"to\": \"" + this.dataOwnderToken + "\",\n" +
                    "\t\"data\":{\n" +
                    "\t\t\"params\" : {\n" +
                    "\t\t\t\t\"title\" : \"" + this.title + "\",\n" +
                    "        \t\t\"range\" : \"" + this.requestRange + "\"\n" +
                    /*"        \t\t\"description\" : \"" + this.message.toString() + "\"\n" +*/
                    /*"        \t\t\"image\" : \"" + this.uri + "\"\n" +*/
                    "    \t\t\t}\n" +
                    "\t\t\t\n" +
                    "\t\t}\n" +
                    "}";

            try{
                JSONObject inData = new JSONObject(data);
                JSONObject oldData = inData.getJSONObject("data");

                //put token in
                JSONObject extraToken = new JSONObject();
                extraToken.put("SenderToken", requesterToken);
                oldData.put("Token", extraToken);
                //put stage in
                JSONObject extraStage = new JSONObject();
                extraStage.put("CurrStage", stage+1);//next stage is 2
                oldData.put("Stage", extraStage);

                inData.put("data", oldData);
                String strData = inData.toString();
                return strData;
            }
            catch (JSONException e){
                Log.d("JSONException", e.toString());
                return null;
            }
        }

        if(stage == 3){

            String data = "{\n" +
                    "\t\"to\": \"" + this.requesterToken + "\",\n" +
                    "\t\"data\":{\n" +
                    "\t\t\"params\" : {\n" +
                    "\t\t\t\t\"title\" : \"" + this.title + "\",\n" +
                    "        \t\t\"range\" : \"" + this.requestRange + "\"\n" +
                    /*"        \t\t\"description\" : \"" + this.message.toString() + "\"\n" +*/
                    /*"        \t\t\"image\" : \"" + this.uri + "\"\n" +*/
                    "    \t\t\t}\n" +
                    "\t\t\t\n" +
                    "\t\t}\n" +
                    "}";

            try{
                JSONObject inData = new JSONObject(data);
                JSONObject oldData = inData.getJSONObject("data");

                //put token in
                JSONObject extraToken = new JSONObject();
                extraToken.put("SenderToken", dataOwnderToken);
                oldData.put("Token", extraToken);
                //put stage in
                JSONObject extraStage = new JSONObject();
                extraStage.put("CurrStage", stage+1);//next stage is 2
                oldData.put("Stage", extraStage);

                //put places in
                JSONArray jsonArray = new JSONArray();
                for(int i=0;i<this.placesInfo.size(); ++i) {
                    JSONObject extraPlaces = new JSONObject();
                    extraPlaces.put("title", placesInfo.get(i).getName());
                    extraPlaces.put("location", placesInfo.get(i).getRegion().getMins());
                    jsonArray.put(extraPlaces);
                }

                oldData.put("places", jsonArray);
                inData.put("data", oldData);
                String strData = inData.toString();
                return strData;
            }
            catch (JSONException e){
                Log.d("JSONException", e.toString());
                return null;
            }
        }

        else{
            return null;
        }
    }
}
