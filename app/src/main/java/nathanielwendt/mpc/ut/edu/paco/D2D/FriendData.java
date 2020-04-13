package nathanielwendt.mpc.ut.edu.paco.D2D;

import com.ut.mpc.utils.STRegion;

public class FriendData {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    private String name;
    private String uri;
    private String token;

    public FriendData(){
        this.name = "Default";
        this.token = null;
        this.uri = null;
    }

    public FriendData(String name,String token, String uri){
        this.name = name;
        this.token = token;
        this.uri = uri;
    }
}
