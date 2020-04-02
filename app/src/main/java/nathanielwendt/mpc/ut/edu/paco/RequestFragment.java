package nathanielwendt.mpc.ut.edu.paco;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ut.mpc.utils.STRegion;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.util.ArrayList;
import java.util.Map;

import nathanielwendt.mpc.ut.edu.paco.Data.UserData;
import nathanielwendt.mpc.ut.edu.paco.Data.UserDataAdapter;
import nathanielwendt.mpc.ut.edu.paco.fire_MQTT.sendData;

public class RequestFragment extends Fragment {

    private EditText in_title;
    private EditText in_message;
    private EditText receiverToken;
    private Button btn_req_send;
    private ListView nearby_list_view;
    private SwitchCompat mSwitchCompat;

    private String SenderToken;
    private String ReceiverToken;
    private String title;
    private String message;
    private String data;
    private UserDataAdapter mNearbyDevicesArrayAdapter;
    private RequestFragment.OnFragmentInteractionListener mListener;
    MqttAndroidClient client;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_request, container, false);

        //capture ui elements
        in_title = (EditText) root.findViewById(R.id.req_title);
        in_message = (EditText) root.findViewById(R.id.req_message);
        receiverToken = (EditText) root.findViewById(R.id.req_Receiver);
        btn_req_send = (Button) root.findViewById(R.id.btn_req_send);
        nearby_list_view = (ListView) root.findViewById(R.id.nearby_devices_list_view);

        client = ((MainActivity)getActivity()).getClient();

        //Button Switch
        mSwitchCompat = root.findViewById(R.id.req_switch);
        mSwitchCompat.setChecked(false);

        //place SEND
        Bundle bundle=getArguments();
        if(bundle != null){
            //map -> request
            if(getArguments().getString("message")!=null && getArguments().getString("message")!=""){
                in_message.setText(getArguments().getString("message"));

                //save request range information for later
                SharedPreferences sharedpreferences = getActivity().getSharedPreferences("requestRange", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                String key = "requestRange";
                String data = getArguments().getString("message");
                Log.d("LST", "putting shared prefs name: " + key + " data: " + data);
                editor.putString(key, data);
                editor.commit();

                bundle.clear();
            }
            //select user -> request
            else if(getArguments().getString("name")!=null && getArguments().getString("token")!=null && getArguments().getString("requestRange")!=null){
                in_title.setText("Request From" + getArguments().getString("name"));
                in_message.setText(getArguments().getString("requestRange"));
                receiverToken.setText(getArguments().getString("token"));
            }
            else{
            }
        }

        //default for firebase
        btn_req_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).mPaco.SEND(sendData());
            }
        });

        mSwitchCompat.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            //MQTT
                            btn_req_send.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ((MainActivity)getActivity()).mPaco.sendMQTT(sendData());//

                                }
                            });
                        } else {
                            //Firebase
                            btn_req_send.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ((MainActivity)getActivity()).mPaco.SEND(sendData());//

                                }
                            });
                        }
                    }
                }
        );

        DatabaseReference ref = ((MainActivity)getActivity()).getDatabase().getReference().child("user");
        ref.addListenerForSingleValueEvent(
            new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<UserData> nearbyDevicesArrayList = collectUsers((Map<String,Object>) dataSnapshot.getValue());
                    mNearbyDevicesArrayAdapter = new UserDataAdapter(getActivity(), mListener, nearbyDevicesArrayList);
                    nearby_list_view.setAdapter(mNearbyDevicesArrayAdapter);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //handle databaseError
                }
        });

        return root;

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //update nearby user list
        if(!hidden) {
            DatabaseReference ref = ((MainActivity)getActivity()).getDatabase().getReference().child("user");
            ref.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ArrayList<UserData> nearbyDevicesArrayList = collectUsers((Map<String,Object>) dataSnapshot.getValue());
                            mNearbyDevicesArrayAdapter = new UserDataAdapter(getActivity(), mListener, nearbyDevicesArrayList);
                            nearby_list_view.setAdapter(mNearbyDevicesArrayAdapter);
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            //handle databaseError
                        }
                    });
        }

        //fill the blank of request infomation
        RequestFragment requestFragment = new RequestFragment();
        SharedPreferences sharedpreferences = ((MainActivity)getActivity()).getSharedPreferences("requestInfo", Context.MODE_PRIVATE);
        String name = sharedpreferences.getString("name", "Default");
        String token = sharedpreferences.getString("token", "Default");
        String requestRange = sharedpreferences.getString("requestRange", "Default");

        in_title.setText("Request From" + name);
        receiverToken.setText(token);
        in_message.setText(requestRange);
    }

    private String sendData(){
        SenderToken =((MainActivity)getActivity()).getTheCurrentClient();
        ReceiverToken = receiverToken.getText().toString();
        title = in_title.getText().toString();
        message = in_message.getText().toString();
        sendData data = new sendData();
        data.setStage(0);
        data.setDataOwnderToken(ReceiverToken);
        data.setRequesterToken(SenderToken);
        data.setTitle(title);
        data.setMessage(message);

        return data.getSendData();
    }

    private ArrayList<UserData> collectUsers(Map<String,Object> users) {

        ArrayList<UserData> userInfo = new ArrayList<>();

        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : users.entrySet()){

            Map singleUser = (Map) entry.getValue();
            String userToken = (String) singleUser.get("tokenID");
            String userName = (String) singleUser.get("userName");
            String location = (String) singleUser.get("lastLocation");
            UserData AUser = new UserData(userName, userToken);

            if(CheckNearby(location)){
                userInfo.add(AUser);
            }
        }

        return userInfo;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
        public double windowPoK(STRegion region);
    }

    private boolean CheckNearby(String OtherLocation){

        String curLocation = ((MainActivity)getActivity()).getCurrentLocation();
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

}
