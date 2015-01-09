package currency.currencyconverter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;


public class CurrencyConverterRequest extends AsyncTask<String, Double, Double> {

    private Context _context;
    private CurrencyIDsSqliteTable _currencyIDsSqliteTable;
    private Callback _callback;

    public CurrencyConverterRequest(Context context, Callback callback) {
        _context = context;
        _callback = callback;
        _currencyIDsSqliteTable = new CurrencyIDsSqliteTable(_context);

    }
    @Override
    protected Double doInBackground(String... strings) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = null;
        if (strings.length < 2) {
            throw new IllegalArgumentException("number of arguments is " + strings.length);
        }

        String fromCurrmencyID = strings[0];
        String toCurrmencyID = strings[1];

        try {
            response = httpClient.execute(new HttpGet(String.format("http://www.freecurrencyconverterapi.com/api/v2/convert?q=%s_%s",
                    fromCurrmencyID, toCurrmencyID)));

            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                response.getEntity().writeTo(outputStream);
                outputStream.close();
                String responseJsonString = outputStream.toString();
                JSONObject jsonObject = new JSONObject(responseJsonString);
                JSONObject results = jsonObject.getJSONObject("results");
                Iterator<String> iterator = results.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    JSONObject currncyObject = results.getJSONObject(key);
                    double value = currncyObject.getDouble("val");
                    insertIntoDatabase(value, fromCurrmencyID, toCurrmencyID);
                    return value;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


    private void insertIntoDatabase(double val, String from, String to) {
        String converterId = from + "_" + to;
        ContentResolver contentResolver = _context.getContentResolver();
        Calendar calendar = Calendar.getInstance();
        ContentValues values = new ContentValues();
        values.put(CurrencyConverterContentProvider.CONVERTER_ID, converterId);
        values.put(CurrencyConverterContentProvider.FROM_CURRENCY_ID, from);
        values.put(CurrencyConverterContentProvider.FROM_CURRENCY_NAME, getCurrencyName(from));
        values.put(CurrencyConverterContentProvider.TO_CURRENCY_ID, to);
        values.put(CurrencyConverterContentProvider.TO_CURRENCY_NAME, getCurrencyName(to));
        values.put(CurrencyConverterContentProvider.RATIO, val);
        values.put(CurrencyConverterContentProvider.DATE, calendar.getTimeInMillis());
        values.put(CurrencyConverterContentProvider.VISIBLE, 1);

        Uri uri = Uri.withAppendedPath(CurrencyConverterContentProvider.CONTENT_URI, "/INSERT_OR_UPDATE/" + converterId);

        contentResolver.insert(uri, values);
        if (_callback != null) {
            _callback.callback(val);
        }

    }

    private String getCurrencyName(String ID) {
        String[] columns = {CurrencyIDsSqliteTable.CURRENCY_NAME};
        Cursor cursor = _currencyIDsSqliteTable.getReadableDatabase().query(CurrencyIDsSqliteTable.TABLE_NAME, columns,
                CurrencyIDsSqliteTable.CURRENCY_ID + " like '" + ID + "'", null, null, null, null);

        if (cursor == null) {
            return null;
        }

        if (cursor.moveToNext()) {
            return cursor.getString(0);
        } else {
            return null;
        }

    }

    public interface Callback {
        public void callback(double value);
    }
}
