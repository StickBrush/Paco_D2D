package nathanielwendt.mpc.ut.edu.paco.fire_MQTT;

import com.ut.mpc.setup.Constants;

public class AccessProfile {

    private double GridFactor;
    private float minSpaceWindow;
    private float minTempWindow;
    private int accessLevel;

    public Double getGridFactor() { return GridFactor; }

    public void setGridFactor(Double GridFactor) {
        this.GridFactor = GridFactor;
    }

    public float getMinSpaceWindow() { return minSpaceWindow; }

    public void setMinSpaceWindow(float minSpaceWindow) { this.minSpaceWindow = minSpaceWindow; }

    public float getMinTempWindow() { return minTempWindow; }

    public void setMinTempWindow(float minWindowSize) { this.minTempWindow = minTempWindow; }

    public int getAccessLevel() { return accessLevel; }

    public void setAccessLevel(int accessLevel) { this.accessLevel = accessLevel; }

    public AccessProfile(){
        this.GridFactor = 2.0;
        this.minSpaceWindow = 0;
        this.minTempWindow = 0;
        this.accessLevel = 1;
    }

    public AccessProfile(Double GridFactor, float minSpaceWindow, float minTempWindow){
        this.GridFactor = GridFactor;
        this.minSpaceWindow = minSpaceWindow;
        this.minTempWindow = minTempWindow;
    }

    public void updateProfile(int accessLevel){
        if(accessLevel == 1){
            this.setGridFactor(2.0);
            this.setMinSpaceWindow(0);
            this.setMinTempWindow(0);//
            this.accessLevel = 1;
        }
        else if(accessLevel == 2){
            this.setGridFactor(1.0);
            this.setMinSpaceWindow(5* Constants.PoK.SPACE_RADIUS);
            this.setMinTempWindow(5* Constants.PoK.TEMPORAL_RADIUS);
            this.accessLevel = 2;
        }
        else if(accessLevel == 3){
            this.setGridFactor(0.5);
            this.setMinSpaceWindow(20* Constants.PoK.SPACE_RADIUS);
            this.setMinTempWindow(20* Constants.PoK.TEMPORAL_RADIUS);
            this.accessLevel = 3;
        }
        else{}
    }
}
