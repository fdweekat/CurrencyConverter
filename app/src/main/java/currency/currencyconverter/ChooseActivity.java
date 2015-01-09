package currency.currencyconverter;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ChooseActivity extends Activity {

    private CurrencyIDsSqliteTable _currencyIDsSqliteTable;
    private List<Currency> currencys;
    private Spinner fromCurrencySpiner;
    private Spinner toCurrencySpiner;
    public static final String INTENT_RESULT = "currencyConverterID";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_choose);
        _currencyIDsSqliteTable = new CurrencyIDsSqliteTable(this);
        SQLiteDatabase database = _currencyIDsSqliteTable.getReadableDatabase();
        Cursor cursor = database.query(CurrencyIDsSqliteTable.TABLE_NAME, null, null, null, null, null, CurrencyIDsSqliteTable.CURRENCY_NAME + " ASC");
        currencys = new ArrayList<Currency>();
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex(CurrencyIDsSqliteTable.CURRENCY_ID));
            String currncyName = cursor.getString(cursor.getColumnIndex(CurrencyIDsSqliteTable.CURRENCY_NAME));
            currencys.add(new Currency(id, currncyName));
        }

        toCurrencySpiner = (Spinner) findViewById(R.id.toCurrency);
        fromCurrencySpiner = (Spinner) findViewById(R.id.fromCurrency);
        if (currencys.isEmpty()) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.container, new MainFragment());
        } else {
            ArrayAdapter<Currency> currencyArrayAdapter = new ArrayAdapter<Currency>(getBaseContext(),
                    android.R.layout.simple_spinner_item, currencys);
            toCurrencySpiner.setAdapter(currencyArrayAdapter);
            fromCurrencySpiner.setAdapter(currencyArrayAdapter);
        }

    }


    public void addCurrency(View view) {
        Currency from = (Currency) fromCurrencySpiner.getSelectedItem();
        Currency to = (Currency) toCurrencySpiner.getSelectedItem();
        final String converterId = from.getId() + "_" + to.getId();
        new CurrencyConverterRequest(this, new CurrencyConverterRequest.Callback() {
            @Override
            public void callback(double value) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("fragment", MainFragment.class.getName());
                startActivity(intent);
            }
        }).execute(from.getId(), to.getId());

    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_choose, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }


}
