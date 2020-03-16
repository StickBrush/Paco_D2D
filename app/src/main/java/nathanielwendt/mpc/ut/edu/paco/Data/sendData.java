package nathanielwendt.mpc.ut.edu.paco.Data;

import android.util.Log;

import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

    public sendData(){
    }

//    //share places
//    public sendData(int stage, String sender, String receiver, String title, String message, String uri){
//        this.stage = stage;
//        if(stage%2 == 0){
//            this.dataOwnderToken = receiver;
//            this.requesterToken = sender;
//        }
//        else{
//            this.dataOwnderToken = sender;
//            this.requesterToken = receiver;
//        }
//        this.title = title;
//        this.uri = uri;
//        this.message = message;
//    }

    //Handle receive message
    public List<String> HandleFireData(){
        if(this.stage == 1){
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

        //Handle list of places
        else if(this.stage == 2){
            try {
                JSONObject jsonData = new JSONObject(message);
                if(permission){
                    //JSONObject json_token = json_data.getJSONObject("Token");
                    JSONObject json_params = jsonData.getJSONObject("params");
                    ///String title = json_params.get("title").toString();

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
                }
                else {
                    List<String> msg = new ArrayList<String>();
                    msg.add("Permission");
                    msg.add("false");
                    return msg;
                }

            } catch (JSONException e) {
                Log.d("JSONException", e.toString());
                return null;
            }
        }

        else{return null;}

    }

    public List<String> HandleMQTTData(){
        if(this.stage == 1){
            try {
                JSONObject jsonData = new JSONObject(message);

                JSONObject json_data = jsonData.getJSONObject("data");
                JSONObject json_token = json_data.getJSONObject("Token");
                JSONObject json_params = json_data.getJSONObject("params");
                String title = json_params.get("title").toString();
                String range = json_params.get("range").toString();
                String senderToken = json_token.get("SenderToken").toString();
                String fireBase_msg = "Title: " + title +"\n" + "Request From: " +"\n" + senderToken +"\n" + "Request Time: " +"\n" + timeStamp + "\n" + "Request Range: " +"\n" + range /*+"\n" + "Latitude: " + latitude +"\n" + "Longitude: " + longitude*/;

                //String[] msg = {senderToken+timeStamp ,fireBase_msg};
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
        else{return null;}

    }


    public String getSendData(){
        //starting to make a request
        if(stage == 0){
            String data = "{\n" +
                    "\t\"to\": \"" + this.dataOwnderToken + "\",\n" +
                    "\t\"data\":{\n" +
                    "\t\t\"params\" : {\n" +
                    "\t\t\t\t\"title\" : \"" + this.title + "\",\n" +
                    "        \t\t\"range\" : \"" + this.message + "\"\n" +
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
                extraStage.put("CurrStage", stage+1);
                oldData.put("Stage", extraStage);//next stage is 1

                inData.put("data", oldData);
                String strData = inData.toString();
                return strData;
            }
            catch (JSONException e){
                Log.d("JSONException", e.toString());
                return null;
            }
        }

        if(stage == 1){

            String data = "{\n" +
                    "\t\"to\": \"" + this.requesterToken + "\",\n" +
                    "\t\"data\":{\n" +
                    "\t\t\"params\" : {\n" +
                    "\t\t\t\t\"title\" : \"" + this.title + "\"\n" +
                    /*"        \t\t\"description\" : \"" + this.message.toString() + "\"\n" +*/
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
                    extraStage.put("CurrStage", "2");//next stage is 2
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
                    Log.d("checkRequestPermissionSendTrue", strData);
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

                    inData.put("data", oldData);
                    String strData = inData.toString();
                    return strData;
                }
                catch (JSONException e){
                    Log.d("JSONException", e.toString());
                    return null;
                }
            }

            //                JSONObject locationJson = new JSONObject();
//                JSONObject locationJson2 = new JSONObject();
//                JSONObject sendBackJson = new JSONObject();
//
//                JSONObject jsonToken = jsonData.getJSONObject("Token");
//                String SenderToken = jsonToken.get("SenderToken").toString();
//                locationJson.put("Location", currentLocation);
//                locationJson2.put("SendBackLocation", locationJson);
//                sendBackJson.put("to", SenderToken);
//                sendBackJson.put("data", locationJson2);
//                sendBackJson.put("target", "Firebase");
//                String sendBackData = sendBackJson.toString();
//                SEND(sendBackData);
        } else{
            return null;
        }
    }
}
