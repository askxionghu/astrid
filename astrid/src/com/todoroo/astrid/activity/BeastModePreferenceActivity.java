package com.todoroo.astrid.activity;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.commonsware.cwac.tlv.TouchListView;
import com.commonsware.cwac.tlv.TouchListView.DropListener;
import com.timsu.astrid.R;
import com.todoroo.andlib.utility.Preferences;

public class BeastModePreferenceActivity extends ListActivity {

    private TouchListView touchList;
    private ArrayAdapter<String> adapter;

    private ArrayList<String> items;

    public static final String BEAST_MODE_ORDER_PREF = "beast_mode_order"; //$NON-NLS-1$

    public static final String BEAST_MODE_PREF_ITEM_SEPARATOR = ";"; //$NON-NLS-1$

    private static final String BEAST_MODE_MORE_ITEM_SPECIAL_CHAR = "-*"; //$NON-NLS-1$

    private static final String BEAST_MODE_MIGRATE_PREF = "beast_mode_migrate"; //$NON-NLS-1$

    // Migration function to fix the fact that I stupidly chose to use the control
    // set names (which are localized and subject to change) as the values in the beast
    // mode preferences
    public static void migrateBeastModePreferences(Context context) {
        boolean hasMigratedBeastMode = Preferences.getBoolean(BEAST_MODE_MIGRATE_PREF, false);
        if (hasMigratedBeastMode) return;
        String setPref = Preferences.getStringValue(BEAST_MODE_ORDER_PREF);
        if (setPref == null) {
            Preferences.setBoolean(BEAST_MODE_MIGRATE_PREF, true);
            return;
        }

        ArrayList<String> defaults = new ArrayList<String>();
        String[] defaultOrder = context.getResources().getStringArray(R.array.TEA_control_sets);
        for (String s : defaultOrder) {
            if (s.contains(BEAST_MODE_MORE_ITEM_SPECIAL_CHAR)) {
                String[] stripped = s.split(BEAST_MODE_MORE_ITEM_SPECIAL_CHAR);
                s = stripped[0];
            }
            defaults.add(s);
        }

        ArrayList<String> setOrder = new ArrayList<String>();
        String[] setOrderArray = setPref.split(BEAST_MODE_PREF_ITEM_SEPARATOR);
        for (String s : setOrderArray) {
            setOrder.add(s);
        }

        String[] prefKeys = context.getResources().getStringArray(R.array.TEA_control_sets_prefs);

        StringBuilder newPref = new StringBuilder();

        //Try to match old preference string to new preference string by index in defaults array
        for (String pref : setOrder) {
            int index = defaults.indexOf(pref);
            if (index > -1 && index < prefKeys.length) {
                newPref.append(prefKeys[index]);
                newPref.append(BEAST_MODE_PREF_ITEM_SEPARATOR);
            } else { // Should never get here--weird error if we do
                // Failed to successfully migrate--reset to defaults
                StringBuilder resetToDefaults = new StringBuilder();
                for (String s : prefKeys) {
                    resetToDefaults.append(s);
                    resetToDefaults.append(BEAST_MODE_PREF_ITEM_SEPARATOR);
                }
                Preferences.setString(BEAST_MODE_ORDER_PREF, resetToDefaults.toString());
                Preferences.setBoolean(BEAST_MODE_MIGRATE_PREF, true);
                return;
            }
        }

        Preferences.setString(BEAST_MODE_ORDER_PREF, newPref.toString());
        Preferences.setBoolean(BEAST_MODE_MIGRATE_PREF, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beast_mode_pref_activity);
        setTitle(R.string.EPr_beastMode_desc);

        touchList = (TouchListView) getListView();
        String order = Preferences.getStringValue(BEAST_MODE_ORDER_PREF);
        String[] itemsArray;
        if (order == null) {
            itemsArray = getResources().getStringArray(R.array.TEA_control_sets);
        } else {
            itemsArray = order.split(BEAST_MODE_PREF_ITEM_SEPARATOR);
        }

        items = new ArrayList<String>();
        for (String s : itemsArray) {
            items.add(s);
        }

        adapter = new ArrayAdapter<String>(this, R.layout.preference_draggable_row, R.id.text, items);
        touchList.setAdapter(adapter);
        touchList.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        touchList.setDropListener(new DropListener() {
            @Override
            public void drop(int from, int to) {
                String s = items.remove(from);
                items.add(to, s);
                adapter.notifyDataSetChanged();
            }
        });

        Button resetButton = (Button) findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetToDefault();
            }
        });
    }

    private void resetToDefault() {
        String[] itemsArray = getResources().getStringArray(R.array.TEA_control_sets);
        while (items.size() > 0)
            items.remove(0);
        for (String s : itemsArray)
            items.add(s);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void finish() {
        StringBuilder newSetting = new StringBuilder(30);
        for (int i = 0; i < adapter.getCount(); i++) {
            newSetting.append(adapter.getItem(i));
            newSetting.append(BEAST_MODE_PREF_ITEM_SEPARATOR);
        }
        Preferences.setString(BEAST_MODE_ORDER_PREF, newSetting.toString());
        super.finish();
    }

}
