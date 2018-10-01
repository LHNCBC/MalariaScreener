package gov.nih.nlm.malaria_screener.database;

/**
 * Created by yuh5 on 11/14/2017.
 */

public class Images {

    private String _idPatient;
    private String _idSlide;
    private String _idImage;
    private String _cellCount;
    private String _infectedCount;
    private String _cellCountGT;
    private String _infectedCountGT;

    public Images(String _idPatient, String _idSlide, String _idImage, String _cellCount, String _infectedCount, String _cellCountGT, String _infectedCountGT) {
        this._idPatient = _idPatient;
        this._idSlide = _idSlide;
        this._idImage = _idImage;
        this._cellCount = _cellCount;
        this._infectedCount = _infectedCount;
        this._cellCountGT = _cellCountGT;
        this._infectedCountGT = _infectedCountGT;
    }

    public String get_idPatient() {
        return _idPatient;
    }

    public void set_idPatient(String _idPatient) {
        this._idPatient = _idPatient;
    }

    public String get_idSlide() {
        return _idSlide;
    }

    public void set_idSlide(String _idSlide) {
        this._idSlide = _idSlide;
    }

    public String get_idImage() {
        return _idImage;
    }

    public void set_idImage(String _idImage) {
        this._idImage = _idImage;
    }

    public String get_cellCount() {
        return _cellCount;
    }

    public void set_cellCount(String _cellCount) {
        this._cellCount = _cellCount;
    }

    public String get_infectedCount() {
        return _infectedCount;
    }

    public void set_infectedCount(String _infectedCount) {
        this._infectedCount = _infectedCount;
    }

    public String get_cellCountGT() {
        return _cellCountGT;
    }

    public void set_cellCountGT(String _cellCountGT) {
        this._cellCountGT = _cellCountGT;
    }

    public String get_infectedCountGT() {
        return _infectedCountGT;
    }

    public void set_infectedCountGT(String _infectedCountGT) {
        this._infectedCountGT = _infectedCountGT;
    }
}
