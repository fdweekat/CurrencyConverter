package currency.currencyconverter;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;



public class MainFragment extends Fragment {
    private static final String LOG_TAG = MainFragment.class.getName();
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
        setHasOptionsMenu(true);
        _converterIds = new ArrayList<String>(30);
        _context = container.getContext();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        Cursor cursor = _context.getContentResolver().
                query(CurrencyConverterContentProvider.CONTENT_URI, null, CurrencyConverterContentProvider.VISIBLE + "= 1", null, null);


        if (cursor != null) {
            while (cursor.moveToNext()) {
                _converterIds.add(cursor.getString(cursor.getColumnIndex(CurrencyConverterContentProvider.CONVERTER_ID)));
            }
        }

        if (_converterIds.isEmpty()) {
            Intent intent = new Intent(_context, ChooseActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            _context.startActivity(intent);
            return null;
        }

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        _listview = (ListView) view.findViewById(R.id.listView);
        _adapter = new ConverterCurrencyListAdapter(getActivity(), _converterIds);
        _listview.setAdapter(_adapter);
        final int id = getId();
        _listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Bundle bundle = new Bundle();
                Fragment fragment = new ConverterFragment();
                fragment.setArguments(bundle);
                bundle.putString("converterid", (String) adapterView.getAdapter().getItem(i));
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                if (getResources().getBoolean(R.bool.is_tablet)) {
                    fragmentTransaction.replace(R.id.converterFragment, fragment);
                    Log.d(LOG_TAG, "screen size is large");
                } else {
                    fragmentTransaction.replace(R.id.container, fragment);
                    Log.d(LOG_TAG, "screen size is normal");
                }
                fragmentTransaction.addToBackStack("Back").commit();

            }
        });

        _adapter.notifyDataSetChanged();
        _listview.requestLayout();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getResources().getBoolean(R.bool.is_tablet)) {
            View converterFragment = getView().getRootView().findViewById(R.id.converterFragment);
            converterFragment.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(ARRAY_LIST_KEY, _converterIds);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_choose, menu);
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

    public void setContext(Context _context) {
        this._context = _context;
    }


}
