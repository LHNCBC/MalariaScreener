package gov.nih.nlm.malaria_screener.database;

/**
 * Created by yuh5 on 5/23/2016.
 */
public class Slides {

    private String _idSlide;
    private String _idPatient;
    private String _date;
    private String _time;
    private String _site;
    private String _preparator;
    private String _operator;
    private String _staining;
    private String _hct;
    private String _parasitemia;
    private String _parasitemia_thick;

    public Slides(String _idPatient, String _idSlide, String _date , String _time, String _site, String _preparator, String _operator, String _staining, String _hct, String _parasitemia, String _parasitemia_thick) {
        this._idPatient = _idPatient;
        this._idSlide = _idSlide;
        this._date = _date;
        this._time = _time;
        this._site = _site;
        this._preparator = _preparator;
        this._operator = _operator;
        this._staining = _staining;
        this._hct = _hct;
        this._parasitemia = _parasitemia;
        this._parasitemia_thick = _parasitemia_thick;
    }

    public String get_parasitemia_thick() {
        return _parasitemia_thick;
    }

    public void set_parasitemia_thick(String _parasitemia_thick) {
        this._parasitemia_thick = _parasitemia_thick;
    }

    public String get_parasitemia() {
        return _parasitemia;
    }

    public void set_parasitemia(String _parasitemia) {
        this._parasitemia = _parasitemia;
    }

    public String get_date() {
        return _date;
    }

    public void set_date(String _date) {
        this._date = _date;
    }

    public String get_hct() {
        return _hct;
    }

    public void set_hct(String _hct) {
        this._hct = _hct;
    }

    public String get_staining() {
        return _staining;
    }

    public void set_staining(String _staining) {
        this._staining = _staining;
    }

    public String get_idSlide() {
        return _idSlide;
    }

    public void set_idSlide(String _idSlide) {
        this._idSlide = _idSlide;
    }

    public String get_idPatient() {
        return _idPatient;
    }

    public void set_idPatient(String _idPatient) {
        this._idPatient = _idPatient;
    }

    public String get_site() {
        return _site;
    }

    public void set_site(String _site) {
        this._site = _site;
    }

    public String get_time() {
        return _time;
    }

    public void set_time(String _time) {
        this._time = _time;
    }

    public String get_preparator() {
        return _preparator;
    }

    public void set_preparator(String _preparator) {
        this._preparator = _preparator;
    }

    public String get_operator() {
        return _operator;
    }

    public void set_operator(String _operator) {
        this._operator = _operator;
    }

}
