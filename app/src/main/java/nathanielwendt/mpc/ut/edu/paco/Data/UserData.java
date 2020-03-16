package nathanielwendt.mpc.ut.edu.paco.Data;

import java.util.List;

public class UserData {

    public String getUserName() { return userName; }

    public void setUserName(String UserName) { this.userName = UserName; }

    public String getTokenID() {
        return tokenID;
    }

    public void setTokenID(String TokenID) { this.tokenID = TokenID; }

    public String getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(String LastLocation) { this.lastLocation = LastLocation; }

    private String userName;
    private String tokenID;
    private String lastLocation;

    public UserData(){
        this.userName = "Default";
    }

    public UserData(String userName, String tokenID){
        this.userName = userName;
        this.tokenID = tokenID;

    }
}
