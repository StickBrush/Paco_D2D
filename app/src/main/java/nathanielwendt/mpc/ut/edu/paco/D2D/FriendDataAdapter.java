package nathanielwendt.mpc.ut.edu.paco.D2D;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ut.mpc.utils.STRegion;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;


import nathanielwendt.mpc.ut.edu.paco.Data.PlaceDataAdapter;
import nathanielwendt.mpc.ut.edu.paco.FragmentHelper;
import nathanielwendt.mpc.ut.edu.paco.FriendsFragment;
import nathanielwendt.mpc.ut.edu.paco.MainActivity;
import nathanielwendt.mpc.ut.edu.paco.PoKTask;
import nathanielwendt.mpc.ut.edu.paco.R;
import nathanielwendt.mpc.ut.edu.paco.utils.PlaceStore;

public class FriendDataAdapter extends ArrayAdapter<FriendData> {
    private final Context context;
    private final List<FriendData> friends;
    private FriendsFragment.OnFragmentInteractionListener mListener;

    private Bitmap myBitmap;

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return friends.size();
    }

    @Override
    public FriendData getItem(int position) {
        // TODO Auto-generated method stub
        return friends.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public FriendDataAdapter(Context context, FriendsFragment.OnFragmentInteractionListener mListener, List<FriendData> friends) {
        super(context, R.layout.friend_list_item, friends);
        this.context = context;
        this.friends = friends;
        this.mListener = mListener;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);//
        Log.d("LST", "inside get View");
        View rowView = inflater.inflate(R.layout.friend_list_item, parent, false);
        final TextView name = (TextView) rowView.findViewById(R.id.name);
        ImageView poster = (ImageView) rowView.findViewById(R.id.poster);
        ImageView delete = (ImageView) rowView.findViewById(R.id.delete_btn);

        final FriendStore friendStore = new FriendStore((Activity) context);
        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                friends.remove(position);
                friendStore.removeFriend(position);
                FriendDataAdapter.this.notifyDataSetChanged();
            }
        });

        final FriendData friend = friends.get(position);
        name.setText(friend.getName());

        if(!(friend.getUri()==null)) {
            File pictureFile = new File(friend.getUri());//
            if (pictureFile.exists()) {
                //Bitmap myBitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
                try (InputStream is = new URL("file://" + friend.getUri()).openStream()) {
                    myBitmap = BitmapFactory.decodeStream(is);
                    poster.setImageBitmap(myBitmap);
                } catch (Exception e) {
                    Log.d("Error", e.toString());
                }
            }
        }

        return rowView;
    }
}
