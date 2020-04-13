package nathanielwendt.mpc.ut.edu.paco.D2D;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.ut.mpc.utils.STRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FriendStore {
    private static final String PREF_TAG = "Friends";
    Context activity;

    public FriendStore(Activity activity){
        this.activity = activity;
    }

    public void put(String key, String token ,String posterPath){
        SharedPreferences sharedpreferences = activity.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        if("".equals(key)){
            key = "DEFAULT";
        }
        String data = token + "**" + posterPath;
        Log.d("LST", "putting shared prefs name: " + key + " data: " + data);
        editor.putString(key, data);
        editor.commit();
    }

    public List<FriendData> getFriends(){
        SharedPreferences sharedpreferences = activity.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        Map<String,?> keys = sharedpreferences.getAll();
        List<FriendData> friends = new ArrayList<FriendData>();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            String friendName = entry.getKey();
            String[] data = entry.getValue().toString().split("\\*\\*");
            Log.d("LST", data[0]);
            Log.d("LST", data[1]);
            String token = data[0];
            //String uri = data[1];
            FriendData newFriend = new FriendData(friendName, token, null);//
            friends.add(newFriend);
        }
        return friends;
    }

    public void removeFriend(int position){
        List<FriendData> friends = getFriends();
        FriendData friendToRemove = friends.get(position);

        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(friendToRemove.getName()).apply();
    }
}
