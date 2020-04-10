package nathanielwendt.mpc.ut.edu.paco.fire_MQTT;

public class PrivacySettingData {
    private int KeyLevel;

    public void setKeyLevel(int KeyLevel) {this.KeyLevel = KeyLevel;}
    public int getKeyLevel() { return KeyLevel; }

    public PrivacySettingData() {
        this.setKeyLevel(1);
    }
}
