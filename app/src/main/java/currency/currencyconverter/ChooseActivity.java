package currency.currencyconverter;

import android.app.Activity;
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
            new Request().execute("");
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
                Intent intent = new Intent();
                intent.putExtra(INTENT_RESULT, converterId);
                setResult(RESULT_OK, intent);
                finish();
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

    class Request extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            HttpClient httpClient = new DefaultHttpClient();
            try {
                HttpResponse response = httpClient.execute(new HttpGet("http://www.freecurrencyconverterapi.com/api/v2/countries"));
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    response.getEntity().writeTo(outputStream);
                    outputStream.close();
                    String responseJsonString = outputStream.toString();

                    JSONObject jsonObject = new JSONObject(responseJsonString);
                    SQLiteDatabase database = _currencyIDsSqliteTable.getWritableDatabase();
                    String insearStatment = "INSERT OR REPLACE INTO '" + CurrencyIDsSqliteTable.TABLE_NAME +
                            "' ('"+CurrencyIDsSqliteTable.CURRENCY_ID +"','" + CurrencyIDsSqliteTable.CURRENCY_NAME+"')" + "VALUES";

                    JSONObject results = jsonObject.getJSONObject("results");
                    Iterator<String> iterator = results.keys();
                    currencys.clear();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        JSONObject currencyJson = results.getJSONObject(key);
                        Currency currency = new Currency(currencyJson.getString("currencyId"),
                                currencyJson.getString("currencyName"));
                        currencys.add(currency);

                        ContentValues contentValues = new ContentValues();
                        contentValues.put(CurrencyIDsSqliteTable.CURRENCY_NAME, currency.getName());
                        contentValues.put(CurrencyIDsSqliteTable.CURRENCY_ID, currency.getId());
                        database.insertWithOnConflict(CurrencyIDsSqliteTable.TABLE_NAME, null,
                                contentValues, SQLiteDatabase.CONFLICT_IGNORE);
                    }

/*
                    database.execSQL(insearStatment);
*/

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            ArrayAdapter<Currency> currencyArrayAdapter = new ArrayAdapter<Currency>(getBaseContext(), android.R.layout.simple_spinner_item, currencys);
            toCurrencySpiner.setAdapter(currencyArrayAdapter);
            fromCurrencySpiner.setAdapter(currencyArrayAdapter);
        }
    }
}
