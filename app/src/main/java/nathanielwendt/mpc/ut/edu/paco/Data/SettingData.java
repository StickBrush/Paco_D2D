package nathanielwendt.mpc.ut.edu.paco.Data;

import android.app.Activity;
import android.widget.Toast;

import com.ut.mpc.setup.Constants;

import nathanielwendt.mpc.ut.edu.paco.MainActivity;

public class SettingData {
//    public String getRangeValue() {
//        return rangeValue;
//    }
//
//    public void setRangeValue(String rangeValue) {
//        this.rangeValue = rangeValue;
//    }
//
//    public double getGranValue() {
//        return granValue;
//    }
//
//    public void setGranValue(double granValue) {
//        this.granValue = granValue;
//    }

    public int getAccessLevel() {
        return accessLevel;
    }

    //1:open 2:guarded 3:restricted
    public void setAccessLevel(int accessLevel, MainActivity activity) {
        if(accessLevel == 1 || accessLevel == 2 || accessLevel == 3){
            this.accessLevel = accessLevel;
            activity.getAccessProfile().updateProfile(accessLevel);
        }
        else{
            Toast.makeText(activity, "Access Level needs to be 1 , 2 or 3", Toast.LENGTH_SHORT).show();
        }
    }

//    public String getPermissionRegion() {
//        return permissionRegion;
//    }
//
//    public void setPermissionRegion(String permissionRegion) {
//        this.permissionRegion = permissionRegion;
//    }

//    private String rangeValue;
//    private double granValue;
    private int accessLevel;
//    private String permissionRegion;

    public SettingData(){
//        this.rangeValue = "all";
//        this.granValue = 1;
        this.accessLevel = 1;
//        this.permissionRegion = "all";
    }
}
