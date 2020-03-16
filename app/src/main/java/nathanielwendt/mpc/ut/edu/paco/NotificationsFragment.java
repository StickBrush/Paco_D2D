package nathanielwendt.mpc.ut.edu.paco;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.ut.mpc.utils.STRegion;

import com.karumi.dexter.listener.PermissionRequest;

import java.util.List;

import nathanielwendt.mpc.ut.edu.paco.Data.NotificationData;
import nathanielwendt.mpc.ut.edu.paco.Data.NotificationDataAdapter;
import nathanielwendt.mpc.ut.edu.paco.utils.notificationStore;

public class NotificationsFragment extends Fragment {

    private ListView listView;
    private TextView tokenView;
    private List<NotificationData> notifications;
    private NotificationsFragment.OnFragmentInteractionListener mListener;
    private notificationStore NotificationStore;
    ArrayAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        Dexter.checkPermissions(storagePermissionsListener, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        listView = (ListView) root.findViewById(R.id.list_notification);

        MainActivity activity = (MainActivity) getActivity();
        NotificationStore = new notificationStore(activity);

        //adapter = ((MainActivity)getActivity()).getAdapter();
        //notifications = NotificationStore.getNotifications();
        //adapter = new NotificationDataAdapter(getActivity(), mListener, notifications);
        //listView.setAdapter(adapter);

        tokenView = root.findViewById(R.id.tokenView);
        tokenView.setText(((MainActivity)getActivity()).getTheCurrentClient());

        return root;

    }

    private MultiplePermissionsListener storagePermissionsListener = new MultiplePermissionsListener() {
        @Override
        public void onPermissionsChecked(MultiplePermissionsReport report) {
            notifications = NotificationStore.getNotifications();
            adapter = new NotificationDataAdapter(getActivity(), mListener, notifications);
            listView.setAdapter(adapter);
        }

        @Override
        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
            token.continuePermissionRequest();
        }
    };

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden) {
            int prevSize = notifications.size();
            notifications = NotificationStore.getNotifications();
            if(notifications.size() > prevSize){
                //super hacky way to update list view...
                adapter = new NotificationDataAdapter(getActivity(), mListener, notifications);
                listView.setAdapter(adapter);
            } else {

            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("LST", "on pause");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentInteractionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        Log.d("LST", "on detach");
        //       placesList.setAdapter(null);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);

        public double windowPoK(STRegion region);
    }
}