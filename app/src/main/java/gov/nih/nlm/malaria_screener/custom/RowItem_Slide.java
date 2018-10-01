package gov.nih.nlm.malaria_screener.custom;

/**
 * Created by yuh5 on 5/25/2016.
 */
public class RowItem_Slide {

    private String slideID;
    private String patientID;
    private String time;
    private String date;

    public RowItem_Slide(String slideID, String patientID, String time, String date) {
        this.slideID = slideID;
        this.patientID = patientID;
        this.time = time;
        this.date = date;
    }

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public String getSlideID() {
        return slideID;
    }

    public void setSlideID(String slideID) {
        this.slideID = slideID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
