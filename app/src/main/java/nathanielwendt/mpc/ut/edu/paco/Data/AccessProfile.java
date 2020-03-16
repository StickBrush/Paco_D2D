package nathanielwendt.mpc.ut.edu.paco.Data;

import com.ut.mpc.setup.Constants;

public class AccessProfile {

    private Double GridFactor;
    private float minSpaceWindow;
    private float minTempWindow;

    public Double getGridFactor() { return GridFactor; }

    public void setGridFactor(Double GridFactor) {
        this.GridFactor = GridFactor;
    }

    public float getMinSpaceWindow() { return minSpaceWindow; }

    public void setMinSpaceWindow(float minSpaceWindow) { this.minSpaceWindow = minSpaceWindow; }

    public float getMinTempWindow() { return minTempWindow; }

    public void setMinTempWindow(float minWindowSize) { this.minTempWindow = minTempWindow; }

    public AccessProfile(){
        this.GridFactor = 2.0;
        this.minSpaceWindow = 0;
        this.minTempWindow = 0;
    }

    public void updateProfile(int accessLevel){
        if(accessLevel == 1){
            this.setGridFactor(2.0);
            this.setMinSpaceWindow(0);
            this.setMinTempWindow(0);//
        }
        else if(accessLevel == 2){
            this.setGridFactor(1.0);
            this.setMinSpaceWindow(5* Constants.PoK.SPACE_RADIUS);
            this.setMinTempWindow(5* Constants.PoK.TEMPORAL_RADIUS);
        }
        else if(accessLevel == 3){
            this.setGridFactor(0.5);
            this.setMinSpaceWindow(20* Constants.PoK.SPACE_RADIUS);
            this.setMinTempWindow(20* Constants.PoK.TEMPORAL_RADIUS);
        }
        else{}
    }
}
