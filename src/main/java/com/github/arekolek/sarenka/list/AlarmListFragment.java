package com.github.arekolek.sarenka.list;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import com.github.arekolek.sarenka.edit.Alarm;
import com.github.arekolek.sarenka.edit.AlarmEditActivity;

public class AlarmListFragment extends ListFragment {
    private AlarmAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText("Siema sarenki!");

        adapter = new AlarmAdapter(getActivity(), Alarm.listAll(Alarm.class));
        setListAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.swapList(Alarm.listAll(Alarm.class));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        AlarmEditActivity.startActivity(getActivity(), id);
    }
}
