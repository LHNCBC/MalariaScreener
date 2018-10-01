package gov.nih.nlm.malaria_screener.custom;

/**
 * Created by yuh5 on 5/18/2016.
 */
public class RowItem_Patient {

    private String ID;
    private String initial;
    private String gender;
    private int age;

    public RowItem_Patient(String ID, String initial, String gender, int age) {
        this.ID = ID;
        this.initial = initial;
        this.gender = gender;
        this.age = age;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getInitial() {
        return initial;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
