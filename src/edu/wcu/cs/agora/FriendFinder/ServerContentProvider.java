package edu.wcu.cs.agora.FriendFinder;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * Created by tyler on 10/16/2014.
 *
 * For Reference: http://developer.android.com/training/sync-adapters/creating-stub-provider.html
 */
public class ServerContentProvider extends ContentProvider
{
    // A string that defines the SQL statement for creating a table
    private static final String SQL_CREATE_MAIN = "CREATE TABLE " +
                                                  "main " +          // Table's name
                                                  "(" +             // The columns in the table
                                                  " _ID INTEGER PRIMARY KEY, " +
                                                  " WORD TEXT" +
                                                  " FREQUENCY INTEGER " +
                                                  " LOCALE TEXT )";


    /*
    * Defines a handle to the database helper object. The MainDatabaseHelper class is defined
    * in a following snippet.
    */
    private MainDatabaseHelper mOpenHelper;

    // Defines the database name
    private static final String DBNAME = "server_data";

    // Holds the database object
    private SQLiteDatabase db;


    /*
     * Always return true, indicating that the
     * provider loaded correctly.
     */
    @Override
    public boolean onCreate ()
    {
                /*
         * Creates a new helper object. This method always returns quickly.
         * Notice that the database itself isn't created or opened
         * until SQLiteOpenHelper.getWritableDatabase is called
         */
        mOpenHelper = new MainDatabaseHelper(getContext());
        return true;
    }

    /*
     * Return an empty String for MIME type
     */
    @Override
    public String getType (Uri uri)
    {
        return new String();
    }

    /*
     * query() always returns no results
     *
     */
    @Override
    public Cursor query (Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {
        return null;
    }

    /*
     * insert() always returns null (no URI)
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        db = mOpenHelper.getWritableDatabase();
        return null;
    }
    /*
     * delete() always returns "no rows affected" (0)
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
    /*
     * update() always returns "no rows affected" (0)
     */
    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs) {
        return 0;
    }

    /**
     * Helper class that actually creates and manages the provider's underlying data repository.
     */
    protected static final class MainDatabaseHelper extends SQLiteOpenHelper
    {

        /*
         * Instantiates an open helper for the provider's SQLite data repository
         * Do not do database creation and upgrade here.
         */
        MainDatabaseHelper(Context context) {
            super(context, DBNAME, null, 1);
        }

        /*
         * Creates the data repository. This is called when the provider attempts to open the
         * repository and SQLite reports that it doesn't exist.
         */
        public void onCreate(SQLiteDatabase db) {

            // Creates the main table
            db.execSQL(SQL_CREATE_MAIN);
        }

        /**
         * Called when the database needs to be upgraded. The implementation should use this
         * method to
         * drop tables, add tables, or do anything else it needs to upgrade to the new schema
         * version.
         * <p>
         * <p>The SQLite ALTER TABLE documentation can be found <a href="http://sqlite
         * .org/lang_altertable.html">here</a>.
         * If you add new columns you can use ALTER TABLE to insert them into a live table. If you
         * rename or remove columns you can use ALTER TABLE to rename the old table,
         * then create the new
         * table and then populate the new table with the contents of the old table.
         *
         * @param db The database.
         * @param oldVersion The old database version.
         * @param newVersion The new database version.
         */
        @Override
        public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion)
        {

        }
    }
}
