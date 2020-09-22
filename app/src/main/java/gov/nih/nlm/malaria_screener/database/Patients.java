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
