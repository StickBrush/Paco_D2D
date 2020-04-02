package nathanielwendt.mpc.ut.edu.paco.Data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ut.mpc.utils.STRegion;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import nathanielwendt.mpc.ut.edu.paco.FragmentHelper;
import nathanielwendt.mpc.ut.edu.paco.MainActivity;
import nathanielwendt.mpc.ut.edu.paco.PlacesFragment;
import nathanielwendt.mpc.ut.edu.paco.PoKTask;
import nathanielwendt.mpc.ut.edu.paco.R;
import nathanielwendt.mpc.ut.edu.paco.SendFragment;
import nathanielwendt.mpc.ut.edu.paco.SettingFragment;

public class PlaceDataAdapter extends ArrayAdapter<PlaceData> {
    private final Context context;
    private final List<PlaceData> places;
    private PlacesFragment.OnFragmentInteractionListener mListener;

    private FragmentHelper fHelper;
    private Bitmap myBitmap;

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return places.size();
    }

    @Override
    public PlaceData getItem(int position) {
        // TODO Auto-generated method stub
        return places.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public PlaceDataAdapter(Context context, PlacesFragment.OnFragmentInteractionListener mListener, List<PlaceData> places) {
        super(context, R.layout.place_list_item, places);
        this.context = context;
        this.places = places;
        this.mListener = mListener;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);//
        Log.d("LST", "inside get View");
        View rowView = inflater.inflate(R.layout.place_list_item, parent, false);
        final TextView name = (TextView) rowView.findViewById(R.id.name);
        TextView coverage = (TextView) rowView.findViewById(R.id.coverage);
        ImageView poster = (ImageView) rowView.findViewById(R.id.poster);
        ImageView delete = (ImageView) rowView.findViewById(R.id.delete_btn);
//        Button btn_send = (Button) rowView.findViewById(R.id.send_btn);
//        Button btn_access_set = (Button) rowView.findViewById(R.id.access_set_btn);


        //final PlaceStore placeStore = new PlaceStore((Activity) context);
        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String name = places.get(position).getName();//
                ((MainActivity)getContext()).getFilter().delete(name);//
                places.remove(position);
                //placeStore.removePlace(position);
                PlaceDataAdapter.this.notifyDataSetChanged();
            }
        });

        final PlaceData place = places.get(position);
        name.setText(place.getName());
        String coverageVal = place.getCoverage();
        if(coverageVal == null){
//            coverage.setText("calculating");
            STRegion reg = place.getRegion();
            Object[] arr = new Object[]{reg, coverage, place, mListener};
            new PoKTask().execute(arr);

        } else {
            coverage.setText(coverageVal);
        }

        if(!(place.getUri()==null)) {
            File pictureFile = new File(place.getUri());//
            if (pictureFile.exists()) {
                //Bitmap myBitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
                try (InputStream is = new URL("file://" + place.getUri()).openStream()) {
                    myBitmap = BitmapFactory.decodeStream(is);
                    poster.setImageBitmap(myBitmap);
                } catch (Exception e) {
                    Log.d("Error", e.toString());
                }
            }
        }

//        btn_send.setOnClickListener(new Button.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SendFragment sendFragment = new SendFragment();
//
//                Bundle bundle=new Bundle();
//                bundle.putString("title", place.getName());
//                bundle.putString("message", place.getRegion().toString());
//                bundle.putString("image", place.getUri());
//                sendFragment.setArguments(bundle);
//
//                String tag = "SendFragment";
//                fHelper = ((MainActivity)context).getFragmentHelper();
//                fHelper.show(tag, sendFragment);
//            }
//        });

//        btn_access_set.setOnClickListener(new Button.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                SettingFragment settingFragment = new SettingFragment();
//
//                Bundle bundle=new Bundle();
//                bundle.putString("position", places.get(position).toString());
//                settingFragment.setArguments(bundle);
//                String tag = "SettingFragment";
//                fHelper = ((MainActivity)context).getFragmentHelper();
//                fHelper.show(tag, settingFragment);
//
//            }
//        });

        return rowView;
    }

}
