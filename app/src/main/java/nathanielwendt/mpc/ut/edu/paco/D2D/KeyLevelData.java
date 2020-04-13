package nathanielwendt.mpc.ut.edu.paco.D2D;

public class KeyLevelData {
    private boolean needIdentity;
    private boolean needLocation;

    public void setNeedIdentity(boolean needIdentity) {this.needIdentity = needIdentity;}
    public boolean getNeedIdentity() { return needIdentity; }

    public void setNeedLocation(boolean needLocation) {this.needLocation = needLocation;}
    public boolean getNeedLocation() { return needLocation; }

    public KeyLevelData() {
        this.setNeedIdentity(false);
        this.setNeedLocation(false);
    }
}
