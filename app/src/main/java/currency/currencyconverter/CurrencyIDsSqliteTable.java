package currency.currencyconverter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CurrencyIDsSqliteTable extends SQLiteOpenHelper {

    private static final int VERSION = 2;
    public static final String TABLE_NAME = "currencyID";
    public static final String CURRENCY_ID = "id";
    public static final String CURRENCY_NAME = "name";
    private static final String TABLE_CREATE = "create table " + TABLE_NAME + "( '" + CURRENCY_ID +
            "' string unique primary key , '" + CURRENCY_NAME + "' text not null)";

    public CurrencyIDsSqliteTable(Context context) {
        super(context, TABLE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.w(CurrencyIDsSqliteTable.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
