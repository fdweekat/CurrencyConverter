package currency.currencyconverter;


import android.app.Fragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class ConverterFragment extends Fragment {

    private String _converterid;


    private String _from;
    private String _to;
    private float _ratio;
    private EditText _fromValue;
    private EditText _toValue;


    public ConverterFragment() {
        super();
    }



    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.converter_fragment, container, false);
        Bundle bundle = getArguments();
        if(bundle ==null) {
            return null;
        }
        _converterid = bundle.getString("converterid");
        if (savedInstanceState != null) {
            _converterid = savedInstanceState.getString("converterid");
        }

        Cursor cursor = view.getContext().getContentResolver().query(CurrencyConverterContentProvider.CONTENT_URI,
                null, CurrencyConverterContentProvider.CONVERTER_ID + "='" + _converterid + "'", null, null);

        if (cursor != null) {
            cursor.moveToNext();
            _to = cursor.getString(cursor.getColumnIndex(CurrencyConverterContentProvider.TO_CURRENCY_NAME));
            _from = cursor.getString(cursor.getColumnIndex(CurrencyConverterContentProvider.FROM_CURRENCY_NAME));
            _ratio = cursor.getFloat(cursor.getColumnIndex(CurrencyConverterContentProvider.RATIO));
        }

        TextView toTextview = (TextView) view.findViewById(R.id.toTextView);
        toTextview.setText(_to);
        TextView fromTextView = (TextView) view.findViewById(R.id.fromTextView);
        fromTextView.setText(_from);
        _fromValue = (EditText) view.findViewById(R.id.fromValue);
        _toValue = (EditText) view.findViewById(R.id.toValue);

        view.findViewById(R.id.convert).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convert(view);
            }
        });

        view.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete(view);
            }
        });
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("converterid", _converterid);
        super.onSaveInstanceState(outState);
    }

    public void convert(View view) {
        String str = _fromValue.getText().toString();
        if(str.isEmpty()) {
            return;
        }
        float value = Float.parseFloat(str);
        _toValue.setText(String.valueOf(value * _ratio));
    }

    public void delete(View view) {
        ContentValues values = new ContentValues();
        values.put(CurrencyConverterContentProvider.VISIBLE, 0);
        view.getContext().getContentResolver().update(CurrencyConverterContentProvider.CONTENT_URI, values,
                CurrencyConverterContentProvider.CONVERTER_ID + "='" + _converterid + "'", null);
        getFragmentManager().popBackStack();
    }
}