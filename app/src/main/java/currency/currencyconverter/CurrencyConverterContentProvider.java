package currency.currencyconverter;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

public class CurrencyConverterContentProvider extends ContentProvider {

    private static final String PROVIDER_NAME = "currency_converter";
    private static final String CONTENT_PROVIDER ="content://" + PROVIDER_NAME;
    public static final Uri CONTENT_URI = Uri.parse(CONTENT_PROVIDER);
    public static final String CONVERTER_ID = "converter_id";
    public static final String FROM_CURRENCY_ID = "from_curreny_id";
    public static final String FROM_CURRENCY_NAME = "from_currency_name";
    public static final String TO_CURRENCY_ID = "to_curreny_id";
    public static final String TO_CURRENCY_NAME = "to_currency_name";
    public static final String RATIO = "ratio";
    public static final String DATE = "DATE";
    public static final String VISIBLE = "VISIBLE";

    private static final int VERSION = 4;
    public static final String TABLE_NAME = "CurrencyRatio";

    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
            CONVERTER_ID + " STRING PRIMARY KEY, " + FROM_CURRENCY_ID + " STRING, " + FROM_CURRENCY_NAME +
            " TEXT, " + TO_CURRENCY_ID + " STRING, " + TO_CURRENCY_NAME + " TEXT, " + RATIO + " FLAOT, " +
            DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, " + VISIBLE + " integer)";

    private static UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, null, 1);
        uriMatcher.addURI(PROVIDER_NAME, CONVERTER_ID + "/#", 2);
        uriMatcher.addURI(PROVIDER_NAME, "INSERT_OR_UPDATE/*", 3);
    }

    private DatabaseHelper databaseHelper;
    private SQLiteDatabase database;

    public CurrencyConverterContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case 1:
            case 2:
                return PROVIDER_NAME + "/" + CONVERTER_ID;
            case 3:
                return PROVIDER_NAME + "/" + "INSERT_OR_UPDATE";
            default:
                throw new IllegalArgumentException("Uknown Uri:" + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long row = 0;
        if (uriMatcher.match(uri) == 3) {
            row = insertOrUpdate(uri, values);
        } else {
            row = database.insert(TABLE_NAME, null, values);
        }

        if (row > 0) {
            Uri newUri = Uri.parse(CONTENT_PROVIDER + "/" + CONVERTER_ID + "/" + values.get(CONVERTER_ID));
            getContext().getContentResolver().notifyChange(newUri, null);
            return newUri;
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        database = databaseHelper.getWritableDatabase();
        return database != null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(TABLE_NAME);
        switch (uriMatcher.match(uri)) {
            case 1:
                break;
            case 2:
                sqLiteQueryBuilder.appendWhere(CONVERTER_ID + "='" + uri.getLastPathSegment() + "'");
                break;
            default:
                throw new IllegalArgumentException("Uknown Uri:" + uri);
        }

        if (sortOrder != null) {
            if (sortOrder.isEmpty()) {
                sortOrder = null;
            }
        }

        Cursor cursor = sqLiteQueryBuilder.query(database, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        int count  = 0;
        switch (uriMatcher.match(uri)) {
            case 1:
                count = database.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            case 2:
                if (TextUtils.isEmpty(selection)) {
                    selection = "";
                } else {
                    selection += "and ";
                }
                selection += CONVERTER_ID + "='" + uri.getLastPathSegment() + "'";

                count = database.update(TABLE_NAME, values, selection, selectionArgs);
                break;
            case 3:
                insertOrUpdate(uri, values);
                count = 1;
                break;
            default:
                throw new IllegalArgumentException("Uknown Uri:" + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, TABLE_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            Log.w(CurrencyConverterContentProvider.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(sqLiteDatabase);
        }
    }

    private long insertOrUpdate(Uri uri, ContentValues values) {
        return database.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
}
