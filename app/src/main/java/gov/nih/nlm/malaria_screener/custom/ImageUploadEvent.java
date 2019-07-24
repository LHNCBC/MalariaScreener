package gov.nih.nlm.malaria_screener.custom;

public class ImageUploadEvent {

    private boolean done;

        public ImageUploadEvent(boolean done) {
            this.done = done;
        }

        public boolean getImageUploadDone(){
            return done;
        }


}
