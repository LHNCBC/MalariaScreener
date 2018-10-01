package gov.nih.nlm.malaria_screener.frontEnd;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import gov.nih.nlm.malaria_screener.database.MyDBHandler;

/**
 * Created by yuh5 on 5/16/2016.
 */
public class MySuggestionProvider extends ContentProvider {

    public final static String AUTHORITY = "com.example.hang.malaria.SuggestionProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/patients" );
    MyDBHandler myPatientDB = null;

    private static final int SUGGESTIONS_PATIENT = 1;

    private static final int GET_COUNTRY = 3;

    UriMatcher mUriMatcher = buildUriMatcher();

    private UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SUGGESTIONS_PATIENT);

        uriMatcher.addURI(AUTHORITY, "patients/#", GET_COUNTRY);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        myPatientDB = new MyDBHandler(getContext(), null, null, 1);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor c = null;
        switch(mUriMatcher.match(uri)){
            case SUGGESTIONS_PATIENT :
                c = myPatientDB.getPatients(selectionArgs);
                break;
            case GET_COUNTRY :
                String id = uri.getLastPathSegment();
                c = myPatientDB.getPatient(id);
        }

        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

}
