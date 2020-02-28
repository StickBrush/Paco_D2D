package nathanielwendt.mpc.ut.edu.paco;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.eclipse.paho.android.service.MqttAndroidClient;

public class NotificationsFragment extends Fragment {

    private ListView listView;
    private TextView tokenView;
    ArrayAdapter adapter;

    MqttAndroidClient client;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        adapter = ((MainActivity)getActivity()).getAdapter();
        client = ((MainActivity)getActivity()).getClient();

        listView = (ListView) root.findViewById(R.id.list_notification);
        listView.setAdapter(adapter);

        tokenView = root.findViewById(R.id.tokenView);
        tokenView.setText(((MainActivity)getActivity()).getTheCurrentClient());

        return root;

    }
}