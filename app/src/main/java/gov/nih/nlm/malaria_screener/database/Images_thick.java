package gov.nih.nlm.malaria_screener.database;

public class Images_thick {

    private String _idPatient;
    private String _idSlide;
    private String _idImage;
    private String _parasiteCount;
    private String _wbcCount;
    private String _parasiteCountGT;
    private String _wbcCountGT;

    public Images_thick(String _idPatient, String _idSlide, String _idImage, String _parasiteCount, String _wbcCount, String _parasiteCountGT, String _wbcCountGT) {
        this._idPatient = _idPatient;
        this._idSlide = _idSlide;
        this._idImage = _idImage;
        this._parasiteCount = _parasiteCount;
        this._wbcCount = _wbcCount;
        this._parasiteCountGT = _parasiteCountGT;
        this._wbcCountGT = _wbcCountGT;
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

    public String get_parasiteCount() {
        return _parasiteCount;
    }

    public void set_parasiteCount(String _parasiteCount) {
        this._parasiteCount = _parasiteCount;
    }

    public String get_wbcCount() {
        return _wbcCount;
    }

    public void set_wbcCount(String _wbcCount) {
        this._wbcCount = _wbcCount;
    }

    public String get_parasiteCountGT() {
        return _parasiteCountGT;
    }

    public void set_parasiteCountGT(String _parasiteCountGT) {
        this._parasiteCountGT = _parasiteCountGT;
    }

    public String get_wbcCountGT() {
        return _wbcCountGT;
    }

    public void set_wbcCountGT(String _wbcCountGT) {
        this._wbcCountGT = _wbcCountGT;
    }
}
