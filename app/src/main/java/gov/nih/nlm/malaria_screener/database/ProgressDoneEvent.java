package gov.nih.nlm.malaria_screener.database;

/**
 * Created by yuh5 on 3/10/2017.
 */

public class ProgressDoneEvent {

    private boolean done;

    public ProgressDoneEvent(boolean done) {
        this.done = done;
    }

    public boolean getProgressDone(){
        return done;
    }

}
