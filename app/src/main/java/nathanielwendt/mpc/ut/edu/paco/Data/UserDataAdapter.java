package nathanielwendt.mpc.ut.edu.paco.Data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

import nathanielwendt.mpc.ut.edu.paco.FragmentHelper;
import nathanielwendt.mpc.ut.edu.paco.MainActivity;
import nathanielwendt.mpc.ut.edu.paco.NotificationsFragment;
import nathanielwendt.mpc.ut.edu.paco.R;
import nathanielwendt.mpc.ut.edu.paco.RequestFragment;
import nathanielwendt.mpc.ut.edu.paco.SendFragment;
import nathanielwendt.mpc.ut.edu.paco.utils.notificationStore;

public class UserDataAdapter extends ArrayAdapter<UserData> {
    private final Context context;
    private final List<UserData> users;
    private FragmentHelper fHelper;
    private RequestFragment.OnFragmentInteractionListener mListener;

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return users.size();
    }

    @Override
    public UserData getItem(int position) {
        // TODO Auto-generated method stub
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public UserDataAdapter(@NonNull Context context, RequestFragment.OnFragmentInteractionListener mListener, List<UserData> users) {
        super(context, R.layout.user_list_item, users);
        this.context = context;
        this.users = users;
        this.mListener = mListener;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) { final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Log.d("LST", "inside get View");
        View rowView = inflater.inflate(R.layout.user_list_item, parent, false);
        final TextView UserInfo = (TextView) rowView.findViewById(R.id.UserInfo);

        final UserData user = users.get(position);
        Button btn_select = (Button) rowView.findViewById(R.id.select_btn);
        UserInfo.setText(user.getUserName());

        btn_select.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestFragment requestFragment = new RequestFragment();
                SharedPreferences sharedpreferences = ((MainActivity)context).getSharedPreferences("requestRange", Context.MODE_PRIVATE);
                String requestRange = sharedpreferences.getString("requestRange", "Default");

                //save request range information for later
                SharedPreferences Msharedpreferences = ((MainActivity)context).getSharedPreferences("requestInfo", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = Msharedpreferences.edit();
                String key1 = "name";
                String data1 = user.getUserName();
                Log.d("LST", "putting shared prefs name: " + key1 + " data: " + data1);
                editor.putString(key1, data1);

                String key2 = "token";
                String data2 = user.getTokenID();
                Log.d("LST", "putting shared prefs name: " + key2 + " data: " + data2);
                editor.putString(key2, data2);
                editor.commit();

                String key3 = "requestRange";
                String data3 = requestRange;
                Log.d("LST", "putting shared prefs name: " + key3 + " data: " + data3);
                editor.putString(key3, data3);
                editor.commit();

                String tag = "RequestFragment";
                fHelper = ((MainActivity)context).getFragmentHelper();
                fHelper.show(tag, requestFragment);
            }
        });

        return rowView;
    }
}
