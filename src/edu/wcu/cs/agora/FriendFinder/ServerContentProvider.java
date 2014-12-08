package edu.wcu.cs.agora.FriendFinder;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

/**
 * Created by tyler on 10/16/2014.
 * <p>
 * For Reference: http://developer.android.com/training/sync-adapters/creating-stub-provider.html
 */
public class ServerContentProvider extends ContentProvider
{
    /**
     * Authority for this content provider.
     */
    public static final  String AUTHORITY             = "edu.wcu.cs.agora.FriendFinder" +
                                                        ".ServerContentProvider";
    /**
     * URI for authority.
     */
    public static final  Uri    CONTENT_URI           = Uri.parse("content://" + AUTHORITY);
    /**
     * URI for the group table.
     */
    public static final  Uri    USER_GROUP            = Uri
            .parse(ServerContentProvider.CONTENT_URI + "/user_group");
    /**
     * URI for the users table.
     */
    public static final  Uri    USERS                 = Uri
            .parse(ServerContentProvider.CONTENT_URI + "/users");
    /**
     * URI for the Likes table.
     */
    public static final  Uri    LIKES                 = Uri
            .parse(ServerContentProvider.CONTENT_URI + "/likes");
    /**
     * String defines creation of the events table.
     */
    private static final String SQL_CREATE_EVENT      = "CREATE TABLE " +
                                                        "event" +          // Table's name
                                                        "(" +
                                                        // The columns in the table
                                                        " EVENT_NAME TEXT PRIMARY KEY ON CONFLICT" +
                                                        " " +
                                                        "REPLACE, " +
                                                        " EVENT_DATE TEXT, " +
                                                        " EVENT_TIME TEXT, " +
                                                        " LOCATION_VALUE TEXT," +
                                                        " CREATOR TEXT);";
    /**
     * String defines creation of the groups table.
     */
    private static final String SQL_CREATE_USER_GROUP = " CREATE TABLE " +
                                                        "user_group" +
                                                        "(" +
                                                        " GROUP_NAME TEXT PRIMARY KEY ON CONFLICT" +
                                                        " REPLACE," +
                                                        " GROUP_DESCRIPTION TEXT);";
    /**
     * String defines creation of the users table.
     */
    private static final String SQL_CREATE_USERS      = " CREATE TABLE " +
                                                        "users" +
                                                        "(" +
                                                        "EMAIL TEXT PRIMARY KEY ON CONFLICT " +
                                                        "REPLACE," +
                                                        " FULL_NAME TEXT," +
                                                        " BIRTHDAY TEXT," +
                                                        "GENDER TEXT);";
    /**
     * String defines creation of the likess table.
     */
    private static final String SQL_CREATE_LIKES      = " CREATE TABLE " +
                                                        "likes" +
                                                        "(" +
                                                        " LIKE_LABEL TEXT PRIMARY KEY ON CONFLICT" +
                                                        " " +
                                                        "REPLACE);";
    /**
     * Database's name.
     */
    private static final String DBNAME                = "server_data";

    /**
     * Defines the database helper object.
     */
    private MainDatabaseHelper dbHelper;

    /**
     * Always return true, indicating that the
     * provider loaded correctly.
     */
    @Override
    public boolean onCreate ()
    {
        dbHelper = new MainDatabaseHelper(getContext());
        return true;
    }

    /**
     * Defines a query to our client-side database.
     * @param uri The table to query.
     * @param projection Which columns to use.
     * @param selection What values to use.
     * @param selectionArgs Arguments to selection values.
     * @param sortOrder Not used.
     * @return The query results.
     */
    @Override
    public Cursor query (Uri uri, String[] projection, String selection, String[] selectionArgs,
                         String sortOrder)
    {
        String table = getTableName(uri);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        return database.query(table, projection, selection, selectionArgs, null, null, sortOrder);
    }

    /**
     * Return an empty String for MIME type
     */
    @Override
    public String getType (Uri uri)
    {
        return new String();
    }

    /**
     * Makes an insertion into the client-side database.
     * @param uri The table to make an insertion into.
     * @param values Values to insert.
     * @return Table with entry location.
     */
    @Override
    public Uri insert (Uri uri, ContentValues values)
    {
        String table = getTableName(uri);
        Log.d("INSERT_CONTENT_PROVIDER", table);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long value = database.insert(table, null, values);
        this.getContext().getContentResolver().notifyChange(uri, null);
        return Uri.withAppendedPath(CONTENT_URI, String.valueOf(value));
    }

    /**
     * Defines the deletion operation for the client-side database.
     * @param uri The table to delete from.
     * @param selection What values to delete on.
     * @param selectionArgs Arguments to selection values.
     * @return Number of rows deleted.
     */
    @Override
    public int delete (Uri uri, String selection, String[] selectionArgs)
    {
        String table = getTableName(uri);
        SQLiteDatabase dataBase = dbHelper.getWritableDatabase();
        this.getContext().getContentResolver().notifyChange(uri, null);
        return dataBase.delete(table, selection, selectionArgs);
    }

    /**
     * Performs an update on the client-side database.
     * @param uri The table to update.
     * @param values The values to add.
     * @param selection Select which elements are to be updated.
     * @param selectionArgs Arguments to selection.
     * @return Number of elements updated.
     */
    public int update (Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        String table = getTableName(uri);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        return database.update(table, values, selection, selectionArgs);
    }

    /**
     * Gets a table's name from its URI.
     *
     * @param uri The uri to retrieve a table from.
     *
     * @return The table name.
     */
    public static String getTableName (Uri uri)
    {
        String value = uri.getPath();
        value = value.replace("/", "");//we need to remove '/'
        return value;
    }

    /**
     * Helper class that actually creates and manages the provider's underlying data repository.
     */
    protected static final class MainDatabaseHelper extends SQLiteOpenHelper
    {

        /**
         * Instantiates a helper for the provider's SQLite data repository.
         */
        MainDatabaseHelper (Context context)
        {
            super(context, DBNAME, null, 1);
        }

        /**
         * Creates the tables of the database.
         */
        public void onCreate (SQLiteDatabase db)
        {
            db.execSQL(SQL_CREATE_EVENT);
            db.execSQL(SQL_CREATE_USER_GROUP);
            db.execSQL(SQL_CREATE_USERS);
            db.execSQL(SQL_CREATE_LIKES);
        }

        /**
         * Called when the database needs to be upgraded. The implementation should use this method
         * to drop tables, add tables, or do anything else it needs to upgrade to the new schema
         * version.
         * <p>
         * <p>The SQLite ALTER TABLE documentation can be found <a href="http://sqlite
         * .org/lang_altertable.html">here</a>. If you add new columns you can use ALTER TABLE to
         * insert them into a live table. If you rename or remove columns you can use ALTER TABLE to
         * rename the old table, then create the new table and then populate the new table with the
         * contents of the old table.
         *
         * @param db The database.
         * @param oldVersion The old database version.
         * @param newVersion The new database version.
         */
        @Override
        public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion)
        {
            // kill all tables.
            db.execSQL("DROP TABLE IF EXISTS event");
            db.execSQL("DROP TABLE IF EXISTS user_group");
            db.execSQL("DROP TABLE IF EXISTS likes");
            db.execSQL("DROP TABLE IF EXISTS users");
            onCreate(db);
        }
    }
}
