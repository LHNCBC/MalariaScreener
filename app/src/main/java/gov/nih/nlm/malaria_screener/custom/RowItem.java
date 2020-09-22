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

package gov.nih.nlm.malaria_screener.custom;

/**
 * Created by yuh5 on 4/27/2016.
 */
public class RowItem {

    private String item;
    private String txt;

    public RowItem (String item, String txt){

        this.item = item;
        this.txt = txt;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public String getItem() {
        return item;
    }

    public String getTxt() {
        return txt;
    }
}
