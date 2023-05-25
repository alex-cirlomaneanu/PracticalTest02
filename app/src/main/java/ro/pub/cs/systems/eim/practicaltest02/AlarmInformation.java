package ro.pub.cs.systems.eim.practicaltest02;

public class AlarmInformation {
    private String status;

    public AlarmInformation() {
        this.status = null;
    }

    public AlarmInformation(
            String query) {
        this.status = query;
    }


    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
