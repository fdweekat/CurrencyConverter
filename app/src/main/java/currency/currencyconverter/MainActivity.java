package currency.currencyconverter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
