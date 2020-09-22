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
 * Created by yuh5 on 4/29/2016.
 */
public class RowItem_CountsNtexts {

    private String title;
    private int count1;
    private int count2;
    private int txt1;
    private int txt2;

    public RowItem_CountsNtexts(String title, int count1, int count2, int txt1, int txt2) {

        this.title = title;
        this.count1 = count1;
        this.count2 = count2;
        this.txt1 = txt1;
        this.txt2 = txt2;

    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCount1(int count1) {
        this.count1 = count1;
    }

    public void setCount2(int count2) {
        this.count2 = count2;
    }

    public String getTitle() {
        return title;
    }

    public int getCount2() {
        return count2;
    }

    public int getCount1() {
        return count1;
    }

    public int getTxt1() {
        return txt1;
    }

    public void setTxt1(int txt1) {
        this.txt1 = txt1;
    }

    public int getTxt2() {
        return txt2;
    }

    public void setTxt2(int txt2) {
        this.txt2 = txt2;
    }
}
