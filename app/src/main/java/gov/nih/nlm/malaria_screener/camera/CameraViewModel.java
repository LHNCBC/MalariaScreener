package gov.nih.nlm.malaria_screener.camera;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;


public class CameraViewModel extends ViewModel {

    private MutableLiveData<byte[]> camByteData = new MutableLiveData<>();
    private MutableLiveData<String> imageString = new MutableLiveData<>();
    private MutableLiveData<File> imageFile = new MutableLiveData<>();
    private MutableLiveData<Integer> orientation = new MutableLiveData<>();; // save phone orientation when image taken
    private MutableLiveData<Boolean> captureCountReset = new MutableLiveData<>();


    public void setCamByteData(byte[] bytes) {
        camByteData.postValue(bytes);
    }

    public MutableLiveData<byte[]> getCamByteData() {
        return camByteData;
    }

    public void setImageString(String string) {
        imageString.postValue(string);
    }

    public MutableLiveData<String> getImageString() {
        return imageString;
    }

    public void setImageFile(File file) {
        imageFile.postValue(file);
    }

    public MutableLiveData<File> getImageFile() {
        return imageFile;
    }

    public void setOrientation(int ori) {
        orientation.postValue(ori);
    }

    public MutableLiveData<Integer> getOrientation() {
        return orientation;
    }

    public void setCaptureCountReset(boolean count) {
        captureCountReset.postValue(count);
    }

    public MutableLiveData<Boolean> getCaptureCountReset() {
        return captureCountReset;
    }

}
