package nathanielwendt.mpc.ut.edu.paco.fire_MQTT;

import android.widget.Spinner;

import com.ut.mpc.setup.Constants;

import java.util.ArrayList;

public class PrivacySetting {
    private PrivacySettingData Restaurant = new PrivacySettingData();
    private PrivacySettingData Park = new PrivacySettingData();
    private KeyLevelData level1 = new KeyLevelData();
    private KeyLevelData level2 = new KeyLevelData();
    String Constrain;

    public PrivacySetting(){
        level1.setNeedIdentity(false);
        level1.setNeedLocation(false);
        level2.setNeedIdentity(false);
        level2.setNeedLocation(false);
    }


    public int getLevel(String requestType){
        if(requestType == "Restaurant"){return Restaurant.getKeyLevel();}
        else if(requestType == "Park"){return Park.getKeyLevel();}
        else{return 1;}
    }

    public ArrayList<String> getRequireType(int level){
        ArrayList<String> list = new ArrayList();
        if(level == 1){
            if(level1.getNeedIdentity()){list.add("Identity");}
            if(level1.getNeedLocation()){list.add("Location");}
            return list;
        } else{
            if(level2.getNeedIdentity()){list.add("Identity");}
            if(level2.getNeedLocation()){list.add("Location");}
            return list;
        }
    }

    public void updateProfile(int keyLevel, String Type){
        if(Type == "Restaurant"){
            Restaurant.setKeyLevel(keyLevel);
        }
        else if(Type == "Park"){
            Park.setKeyLevel(keyLevel);
        }
    }

    public void chengeLevelSetting(int keyLevel, boolean needIdentity, boolean needLocation){
        if(keyLevel == 1){
            level1.setNeedIdentity(needIdentity);
            level1.setNeedLocation(needLocation);
        }
        else if(keyLevel == 2){
            level2.setNeedIdentity(needIdentity);
            level2.setNeedLocation(needLocation);
        }
    }
}
