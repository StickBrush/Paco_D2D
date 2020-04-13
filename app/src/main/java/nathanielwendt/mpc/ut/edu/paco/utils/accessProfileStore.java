package nathanielwendt.mpc.ut.edu.paco.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import nathanielwendt.mpc.ut.edu.paco.D2D.AccessProfile;

public class accessProfileStore {

    private static final String PREF_TAG = "accessProfile";
    Context activity;

    public accessProfileStore(Activity activity){
        this.activity = activity;
    }

    public void put(double GridFactor, float minSpaceWindow, float minTempWindow){
        SharedPreferences sharedpreferences = activity.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putLong("GridFactor", Double.doubleToRawLongBits(GridFactor));
        editor.putFloat("minSpaceWindow", minSpaceWindow);
        editor.putFloat("minTempWindow", minTempWindow);
        editor.commit();
    }

    public AccessProfile getAccessProfile(){
        SharedPreferences sharedpreferences = activity.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);

        double GridFactor = Double.longBitsToDouble(sharedpreferences.getLong("GridFactor", Double.doubleToLongBits(0)));
        float minSpaceWindow = sharedpreferences.getFloat("minSpaceWindow", (float) 0);
        float minTempWindow = sharedpreferences.getFloat("minTempWindow", (float) 0);

        AccessProfile accessProfile = new AccessProfile(GridFactor, minSpaceWindow, minTempWindow);

        return accessProfile;
    }

}
