package currency.currencyconverter;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity2);
        Intent intent = getIntent();
        String fragmentName = intent.getStringExtra("fragment");
        if (savedInstanceState == null) {

            if(MainFragment.class.getName().equalsIgnoreCase(fragmentName)){
                getFragmentManager().beginTransaction()
                        .add(R.id.container, new MainFragment())
                        .commit();
            } else {
                getFragmentManager().beginTransaction()
                        .add(R.id.container, new splashFragment())
                        .commit();
            }
        }
    }

}
