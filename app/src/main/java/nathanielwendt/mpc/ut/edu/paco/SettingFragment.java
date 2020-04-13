package nathanielwendt.mpc.ut.edu.paco;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

public class SettingFragment extends Fragment {

    private EditText in_level;
    private EditText in_name;
    private Button btn_set_Done;
    private Button btn_set_Name;
    private Button btn_set_key;
    private Button btn_key_config;
    private Spinner key_type;
    private Spinner key_level;
    private Spinner config_key_Level;
    private Spinner SharingIdentity;
    private Spinner SharingLocation;
    private static final String PREF_TAG = "Permission";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_setting, container, false);

        in_level = (EditText)root.findViewById(R.id.access_level);
        in_name = (EditText)root.findViewById(R.id.setting_Name);
        btn_set_Done = (Button) root.findViewById(R.id.btn_set_Done);
        btn_set_Name = (Button) root.findViewById(R.id.btn_set_Name);
        key_type = (Spinner)root.findViewById(R.id.key_type);
        key_level = (Spinner)root.findViewById(R.id.key_level);
        //level_constrain = (Spinner)root.findViewById(R.id.level_constrain);
        config_key_Level = (Spinner)root.findViewById(R.id.config_key_Level);
        SharingIdentity = (Spinner)root.findViewById(R.id.SharingIdentity);
        SharingLocation = (Spinner)root.findViewById(R.id.SharingLocation);

        btn_set_key = (Button) root.findViewById(R.id.btn_set_key);
        btn_key_config = (Button) root.findViewById(R.id.btn_key_config);

        final String[] type = {"Restaurant"/*, "Park"*/};
        final ArrayAdapter<String> typeList = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                type);key_type.setAdapter(typeList);

        final String[] keyLevel = {"1", "2"};
        ArrayAdapter<String> keyLevelList = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                keyLevel);key_level.setAdapter(keyLevelList);

        btn_set_Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int accessLevel;
                accessLevel = Integer.valueOf(in_level.getText().toString());

                in_level.getText().clear();
                MainActivity activity = (MainActivity) getActivity();
                activity.getSetData().setAccessLevel(accessLevel, activity);

                activity.getFilter().setGridFactor(activity.getAccessProfile().getGridFactor());
            }
        });

        btn_set_Name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name;
                name = in_name.getText().toString();

                in_name.getText().clear();
                MainActivity activity = (MainActivity) getActivity();
                activity.getUser().setUserName(name);

                activity.getDatabaseReference().setValue(activity.getUser());
            }
        });

        //set key level for topics
        btn_set_key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String type = key_type.getSelectedItem().toString();
                String level = key_level.getSelectedItem().toString();
                //String constainType = level_constrain.getSelectedItem().toString();

                MainActivity activity = (MainActivity) getActivity();
                activity.getPrivacySetting().updateProfile(Integer.parseInt(level), type);
            }
        });

        final String[] con_key_Level = {"1", "2"};
        final ArrayAdapter<String> config_key_Leve_List = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                con_key_Level);config_key_Level.setAdapter(config_key_Leve_List);

        final String[] identity = {"Stranger", "Friends"};
        ArrayAdapter<String> identityList = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                identity);SharingIdentity.setAdapter(identityList);

        final String[] location = {"Need Location", "Do Not Need Location"};
        ArrayAdapter<String> locationList = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                location);SharingLocation.setAdapter(locationList);

        //key level configuration
        btn_key_config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int level = Integer.parseInt(config_key_Level.getSelectedItem().toString());
                boolean needIdentity, needLocation;
                if(SharingIdentity.getSelectedItem().toString().equals("Stranger")){
                    needIdentity = false;
                } else{
                    needIdentity = true;
                }
                if(SharingLocation.getSelectedItem().toString().equals("Do Not Need Location")) {
                    needLocation = false;
                } else{
                    needLocation = true;
                }

                MainActivity activity = (MainActivity) getActivity();
                activity.getPrivacySetting().chengeLevelSetting(level, needIdentity, needLocation);
            }
        });

        return root;

    }

}
