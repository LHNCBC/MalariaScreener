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
