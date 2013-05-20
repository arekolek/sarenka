
package com.github.arekolek.sarenka.list;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.github.arekolek.sarenka.R;
import com.github.arekolek.sarenka.edit.AlarmEditActivity;

public class AlarmListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add_alarm) {
            // startActivity(new Intent(AlarmClock.ACTION_SET_ALARM));
            AlarmEditActivity.startActivity(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
