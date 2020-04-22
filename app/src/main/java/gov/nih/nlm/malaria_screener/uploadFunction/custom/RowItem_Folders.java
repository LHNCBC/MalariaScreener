package gov.nih.nlm.malaria_screener.uploadFunction.custom;

/**
 * Created by yuh5 on 4/27/2016.
 */
public class RowItem_Folders {

    private String item;
    private String date;
    private boolean isSelected;
    private boolean isUploaded;

    public boolean getSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public RowItem_Folders(String item, String date, boolean isSelected, boolean isUploaded){

        this.item = item;
        this.isSelected = isSelected;
        this.date = date;
        this.isUploaded = isUploaded;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getItem() {
        return item;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean getUploaded() {
        return isUploaded;
    }

    public void setUploaded(boolean uploaded) {
        isUploaded = uploaded;
    }

}
