package com.android.dialer.calllog;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.android.dialer.R;

import java.util.ArrayList;
import java.util.HashMap;

public class WindRecentCallLogActivity extends Activity {

    private static final String TAG = "WindRecentCallLogActivity";
    private static final String SELECTED_GROUD_IDS_KEY = "selected_group_ids";
    private static final String SELECTED_GROUD_CALLINFO_KEY = "selected_group_call_info";

    protected WindRecentCallLogFragment mFragment;

    public StatusBarManager mStatusBarMgr;
    private MenuItem mAddMenu;
    private final int MENU_ID_ADD = 0;
    int mCheckedItemsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log("onCreate()");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wind_activity_recent_calllog);

        // Typing here goes to the dialer
        //setDefaultKeyMode(DEFAULT_KEYS_DIALER);
        /*ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP |
                    ActionBar.DISPLAY_SHOW_TITLE);
            actionBar.setTitle(R.string.recent_call_logs_title);
        }*/

        FragmentManager manager = getFragmentManager();
        mFragment = new WindRecentCallLogFragment();
        manager.beginTransaction()
                .replace(R.id.recentCallLogActivity, mFragment, WindRecentCallLogFragment.RECENT_CALL_LOG_TAG)
                //.commit();
                .commitAllowingStateLoss();

    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mAddMenu = menu.add(Menu.NONE, MENU_ID_ADD, MENU_ID_ADD, R.string.recent_call_log_action_add);
        mAddMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mCheckedItemsCount == 0) {
            mAddMenu.setEnabled(false);
        } else {
            mAddMenu.setEnabled(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        } else if (item.getItemId() == MENU_ID_ADD) {
            ArrayList<Integer> list = mFragment.getSelectedGroupCallIdList();
            HashMap<String, String> callLogMap = mFragment.getSelectedCallLogInfoList();
            log("MENU_ID_ADD clicked,SelectedGroupCallIdList:"+ list);
            log("MENU_ID_ADD clicked,SelectedCallLogInfoList size:"+ callLogMap.size());
            if(list.size() > 0)
            {
                Intent data = new Intent();
                data.putExtra(SELECTED_GROUD_CALLINFO_KEY, callLogMap);
                setResult(Activity.RESULT_OK, data);
            } else {
                setResult(Activity.RESULT_CANCELED);
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        log("onOptionsItemSelected,equals:"+(item.getItemId() == android.R.id.home));
        if (item.getItemId() == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        // TODO check this method
        //SetIndicatorUtils.getInstance().showIndicator(true, this);
    }

    @Override
    protected void onPause() {
        // TODO check this method
        //SetIndicatorUtils.getInstance().showIndicator(false, this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void updateSelectedItemsView(final int checkedItemsCount) {
        mCheckedItemsCount = checkedItemsCount;
        log("updateSelectedItemsView checkedItemsCount:"+checkedItemsCount);
        log("updateSelectedItemsView: id list:"+ mFragment.getSelectedCallLogIds());
        if (mAddMenu != null) {
            if (checkedItemsCount > 0) {
                mAddMenu.setEnabled(true);
            } else {
                mAddMenu.setEnabled(false);
            }
        }
    }

    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        finish();
        super.onBackPressed();
    }

    private void log(final String log) {
        Log.i(TAG, log);
    }

}
