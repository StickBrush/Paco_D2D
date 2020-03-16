package nathanielwendt.mpc.ut.edu.paco.Data;

public class NotificationData {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotification() {
        return notifify;
    }

    public void setNotification(String notifify) {
        this.notifify = notifify;
    }

    private String name;
    private String notifify;

    public NotificationData(String name, String notifify){
        this.name = name;
        this.notifify = notifify;
    }
}
