package nathanielwendt.mpc.ut.edu.paco.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nathanielwendt.mpc.ut.edu.paco.Data.NotificationData;

public class notificationStore {
    private static final String PREF_TAG = "notification";
    Context activity;

    public notificationStore(Activity activity){
        this.activity = activity;
    }

    public void put(String key, String notification){
        SharedPreferences sharedpreferences = activity.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        if("".equals(key)){
            key = "DEFAULT";
        }
        String data = notification;
        Log.d("LST", "putting shared prefs name: " + key + " data: " + data);
        editor.putString(key, data);
        editor.commit();
    }

    public List<NotificationData> getNotifications(){
        SharedPreferences sharedpreferences = activity.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        Map<String,?> keys = sharedpreferences.getAll();
        List<NotificationData> notifications = new ArrayList<NotificationData>();
        for(Map.Entry<String,?> entry : keys.entrySet()){
            String Name = entry.getKey();
            String data = entry.getValue().toString();
            NotificationData notificationData = new NotificationData(Name, data);
            notifications.add(notificationData);
        }
        return notifications;
    }

    public void removeNotification(int position){
        List<NotificationData> notifications = getNotifications();
        NotificationData notificationToRemove = notifications.get(position);

        SharedPreferences sharedPreferences = activity.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(notificationToRemove.getName()).apply();
    }
}
