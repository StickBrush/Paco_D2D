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

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

public class SettingFragment extends Fragment {

    private EditText in_level;
    private EditText in_name;
    private Button btn_set_Done;
    private Button btn_set_Name;
    private FragmentHelper fHelper;
    private static final String PREF_TAG = "Permission";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_setting, container, false);

        in_level = (EditText)root.findViewById(R.id.access_level);
        in_name = (EditText)root.findViewById(R.id.setting_Name);
        btn_set_Done = (Button) root.findViewById(R.id.btn_set_Done);
        btn_set_Name = (Button) root.findViewById(R.id.btn_set_Name);

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

//        btn_set_Done.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String rangeValue;
//                double granValue;
//                if("".equals(in_range.getText().toString())){rangeValue = "all";}
//                else{
//                    rangeValue = in_range.getText().toString();
//                }
//                if("".equals(in_range.getText().toString())){granValue = 1;}
//                else {
//                    granValue = Double.valueOf(in_range.getText().toString());
//                }
//
//                in_range.getText().clear();
//                in_gran.getText().clear();
//
//                ((MainActivity)getActivity()).getSetData().setRangeValue(rangeValue);
//                ((MainActivity)getActivity()).getSetData().setGranValue(granValue);
//
////                SharedPreferences sharedpreferences = ((MainActivity)getActivity().getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
////                SharedPreferences.Editor editor = sharedpreferences.edit();
////                String key1 = "range";
////                String key2 = "gran";
////
////                editor.putString(key1, Double.toString(rangeValue));
////                editor.putString(key2, Double.toString(granValue));
////                editor.commit();
//
//            }
//        });

        return root;

    }

}
