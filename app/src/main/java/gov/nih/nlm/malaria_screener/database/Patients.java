package gov.nih.nlm.malaria_screener.database;

/**
 * Created by yuh5 on 4/20/2016.
 */
public class Patients {

    private String _id;
    private String _gender;
    private String _initial;
    private String _age;


    public Patients(){

    }

    public Patients(String id,String gender, String initial, String age) {
        this._id = id;
        this._gender = gender;
        this._initial = initial;
        this._age = age;

    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_gender() {
        return _gender;
    }
    public void set_gender(String _gender) {
        this._gender = _gender;
    }

    public String get_age() {
        return _age;
    }
    public void set_age(String _age) {
        this._age = _age;
    }

    public String get_initial() {
        return _initial;
    }
    public void set_initial(String _initial) {
        this._initial = _initial;
    }



}
