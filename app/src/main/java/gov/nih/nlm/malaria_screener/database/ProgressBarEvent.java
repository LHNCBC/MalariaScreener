package gov.nih.nlm.malaria_screener.database;

/**
 * Created by yuh5 on 3/10/2017.
 */

public class ProgressBarEvent {

    private int progress;

    public ProgressBarEvent(int progress) {
        this.progress = progress;
    }

    public int getProgress(){
        return progress;
    }

}


