package currency.currencyconverter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class MainFragment extends Fragment {
    private ArrayList<String> _converterIds;
    private static final String ARRAY_LIST_KEY = "converter_ids";
    private ListView _listview;
    private static final String ARRAY_NAME= "CONVERTER_ID_";
    private Context _context;
    private ConverterCurrencyListAdapter _adapter;

    public MainFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            _converterIds = savedInstanceState.getStringArrayList(ARRAY_LIST_KEY);
        }

        if (_converterIds == null) {
            _converterIds = new ArrayList<String>();
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            String deleteid = bundle.getString("delete");
            _converterIds.remove(deleteid);
        }

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        _listview = (ListView) view.findViewById(R.id.listView);
        _adapter = new ConverterCurrencyListAdapter(getActivity(), _converterIds);
        _listview.setAdapter(_adapter);

        _listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
                Fragment fragment = new ConverterFragment();
                fragment.setArguments(bundle);
                bundle.putString("converterid", (String) adapterView.getAdapter().getItem(i));
                getFragmentManager().beginTransaction().replace(R.id.container,
                        fragment)
                .addToBackStack("Back").commit();

            }
        });

        _adapter.notifyDataSetChanged();
        _listview.requestLayout();
        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(ARRAY_LIST_KEY, _converterIds);
        super.onSaveInstanceState(outState);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_choose) {
            Intent intent = new Intent(getActivity(), ChooseActivity.class);
            this.startActivityForResult(intent, 0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addConvertAdd(String converterId) {
        if (_converterIds == null ){
            _converterIds = new ArrayList<String>();
        }
        _converterIds.add(converterId);

        if (_listview != null) {
            _adapter.notifyDataSetChanged();
            _listview.requestLayout();
        }
    }

    public void setContext(Context _context) {
        this._context = _context;
    }


}
