/* Copyright 2020 The Malaria Screener Authors. All Rights Reserved.

This software was developed under contract funded by the National Library of Medicine,
which is part of the National Institutes of Health, an agency of the Department of Health and Human
Services, United States Government.

Licensed under GNU General Public License v3.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.gnu.org/licenses/gpl-3.0.html

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

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
