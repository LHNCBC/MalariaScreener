package gov.nih.nlm.malaria_screener.database;

/**
 * Created by yuh5 on 3/10/2017.
 */

public class UpdateListViewEvent {

    private String folderName;

    public UpdateListViewEvent(String folderName) {
        this.folderName = folderName;
    }

    public String getUploadedFolderName(){
        return folderName;
    }

}


