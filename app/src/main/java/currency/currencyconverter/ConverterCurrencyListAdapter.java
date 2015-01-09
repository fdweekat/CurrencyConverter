package currency.currencyconverter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import android.os.Handler;


public class ConverterCurrencyListAdapter extends BaseAdapter{
    private Context _context;
    ArrayList<String> _ids;

    ConverterCurrencyListAdapter(Context context, ArrayList<String> ids) {
        _ids = ids;
        _context = context;
    }

    @Override
    public int getCount() {
        return _ids.size();
    }

    @Override
    public String getItem(int i) {
        return _ids.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View itemView = view;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            itemView = inflater.inflate(R.layout.convert_item, viewGroup, false);
        }


        showRatio(i, itemView);
        return itemView;
    }

    private void showRatio(int id, View view) {

        ContentResolver contentResolver = _context.getContentResolver();
        Cursor cursor = contentResolver.query(CurrencyConverterContentProvider.CONTENT_URI, null,
                CurrencyConverterContentProvider.CONVERTER_ID + " like '" + getItem(id) + "'", null, null);

        if (cursor == null) {
            return;
        }
        cursor.moveToNext();
        int visible = cursor.getInt(cursor.getColumnIndex(CurrencyConverterContentProvider.VISIBLE));
        if (visible == 0) {
            _ids.remove(id);
            notifyDataSetChanged();
            return;
        }
        TextView from = (TextView) view.findViewById(R.id.fromCurrency);
        final TextView to = (TextView) view.findViewById(R.id.toCurrency);
        from.setText(String.format("%6.2f %s", 1.0, cursor.getString(cursor.getColumnIndex(CurrencyConverterContentProvider.FROM_CURRENCY_NAME))));
        final String toString = cursor.getString(cursor.getColumnIndex(CurrencyConverterContentProvider.TO_CURRENCY_NAME));
        double value = cursor.getFloat(cursor.getColumnIndex(CurrencyConverterContentProvider.RATIO));
        to.setText(String.format("%6.2f %s", value, toString));

        String dateString = cursor.getString(cursor.getColumnIndex(CurrencyConverterContentProvider.DATE));
        Date expiredDate = new Date(Long.parseLong(dateString));
        Date  date = new Date();

        if (date.after(expiredDate)) {
            new CurrencyConverterRequest(_context, new CurrencyConverterRequest.Callback() {
                @Override
                public void callback(final double value) {
                    new Handler(_context.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            to.setText(value + " " + toString);
                        }
                    });
                }
            });
        }



    }
}
