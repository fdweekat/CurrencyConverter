package currency.currencyconverter;


import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import android.os.Handler;
/**
 * A simple {@link Fragment} subclass.
 */
public class splashFragment extends Fragment {

    private CurrencyIDsSqliteTable _currencyIDsSqliteTable;
    private Context _context;
    private boolean _dateReady = false;
    private boolean _hidden;

    public splashFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _hidden = false;
        _context = container.getContext();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        _hidden = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        _currencyIDsSqliteTable = new CurrencyIDsSqliteTable(_context);
        SQLiteDatabase database = _currencyIDsSqliteTable.getReadableDatabase();
        Cursor cursor = database.query(CurrencyIDsSqliteTable.TABLE_NAME, null, null, null, null, null, CurrencyIDsSqliteTable.CURRENCY_NAME + " ASC");
        if (cursor.getCount() > 0) {
            _dateReady = true;
        } else {
            new Request().execute();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                while (!_dateReady);
                loadNextView();
            }
        }, 4000);
    }

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
                            "' ('"+CurrencyIDsSqliteTable.CURRENCY_ID +"','" + CurrencyIDsSqliteTable.CURRENCY_NAME+"')" + " VALUES";

                    JSONObject results = jsonObject.getJSONObject("results");
                    Iterator<String> iterator = results.keys();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        JSONObject currencyJson = results.getJSONObject(key);
                        Currency currency = new Currency(currencyJson.getString("currencyId"),
                                currencyJson.getString("currencyName"));
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
            _dateReady = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        _hidden = true;
    }

    private void loadNextView() {

        if (_hidden) {
            return;
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Cursor cursor = _context.getContentResolver().
                query(CurrencyConverterContentProvider.CONTENT_URI, null, CurrencyConverterContentProvider.VISIBLE + "= 1", null, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                transaction.replace(R.id.container, new MainFragment());
                transaction.commit();
                return;
            }
        }

        Intent intent = new Intent(_context, ChooseActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        _context.startActivity(intent);
    }
}
