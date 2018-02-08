package watershine;

/**
 * Created by kevin on 26.06.2017.
 */
public interface ProcessFileProgressListener {

    void progress(int nbrOfFileProcessed, int totalNbrOfFile);

}
