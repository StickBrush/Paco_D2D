package nathanielwendt.mpc.ut.edu.paco;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.ut.mpc.utils.STRegion;
import com.ut.mpc.utils.STStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nathanielwendt.mpc.ut.edu.paco.D2D.FriendData;
import nathanielwendt.mpc.ut.edu.paco.D2D.FriendDataAdapter;
import nathanielwendt.mpc.ut.edu.paco.D2D.FriendStore;

public class FriendsFragment extends Fragment {

    private List<FriendData> friends;
    private FriendsFragment.OnFragmentInteractionListener mListener;
    /**
     * The fragment's ListView/GridView.
     */
    private ListView friendsList;

    private FriendStore friendStore;
    private EditText friendToekn;
    private EditText friendName;
    private Button btn_Add_Friend;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FriendsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mAdapter != null){
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);

        Dexter.checkPermissions(storagePermissionsListener, Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        friendsList = (ListView) view.findViewById(R.id.friend_list_view);
        friendToekn = (EditText) view.findViewById(R.id.friend_token);
        friendName = (EditText) view.findViewById(R.id.friend_name);
        btn_Add_Friend = (Button) view.findViewById(R.id.btn_add_friend);

        MainActivity activity = (MainActivity) getActivity();
        friendStore = new FriendStore(activity);

        btn_Add_Friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String token = friendToekn.getText().toString();
                String name = friendName.getText().toString();

                MainActivity activity = (MainActivity) getActivity();
                friendStore.put(name, token, null);//

                //refresh page
                Fragment frag = activity.getSupportFragmentManager().findFragmentByTag("FriednsFragment");
                if(frag.isVisible()){
                    activity.getFragmentHelper().show("FriednsFragment", new FriendsFragment());
                }
            }
        });

        return view;
    }

    private MultiplePermissionsListener storagePermissionsListener = new MultiplePermissionsListener() {
        @Override
        public void onPermissionsChecked(MultiplePermissionsReport report) {
            friends = friendStore.getFriends();
            mAdapter = new FriendDataAdapter(getActivity(), mListener, friends);
            friendsList.setAdapter(mAdapter);
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
            friends = friendStore.getFriends();
            //super hacky way to update list view...
            mAdapter = new FriendDataAdapter(getActivity(), mListener, friends);
            friendsList.setAdapter(mAdapter);
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
            mListener = (FriendsFragment.OnFragmentInteractionListener) context;
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
        friendsList.setAdapter(null);
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = friendsList.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);

        public double windowPoK(STRegion region);
    }
}
