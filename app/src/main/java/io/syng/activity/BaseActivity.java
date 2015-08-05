package io.syng.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.syng.R;
import io.syng.app.SyngApplication;
import io.syng.entity.Profile;

public abstract class BaseActivity extends AppCompatActivity implements OnItemClickListener,
        OnClickListener {

    private static final int DRAWER_CLOSE_DELAY_SHORT = 200;
    private static final int DRAWER_CLOSE_DELAY_LONG = 400;

    private static final String CONTRIBUTE_LINK = "https://github.com/syng-io";

    private ArrayList<String> mMenuItemsList = new ArrayList<>(Arrays.asList("Console", "DApps",
            "EtherEx", "TrustDavis", "Augur"));

    private ActionBarDrawerToggle mDrawerToggle;

    private Spinner mAccountSpinner;
    private EditText mSearchTextView;
    private ListView mDrawerListView;
    private DrawerLayout mDrawerLayout;

    private ArrayAdapter<String> mDrawerListAdapter;

    private Handler mHandler = new Handler();

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    };

    protected abstract void onDAppClick(String item);

    @SuppressLint("InflateParams")
    @Override
    public void setContentView(final int layoutResID) {
        LayoutInflater inflater = getLayoutInflater();
        mDrawerLayout = (DrawerLayout) inflater.inflate(R.layout.drawer, null, false);
        FrameLayout content = (FrameLayout) mDrawerLayout.findViewById(R.id.content);

        mDrawerListView = (ListView) mDrawerLayout.findViewById(R.id.drawer_list);
        initDrawer();

        Toolbar toolbar = (Toolbar) inflater.inflate(layoutResID, content, true).findViewById(R.id.myToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                hideKeyBoard(mSearchTextView);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mAccountSpinner = (Spinner) mDrawerLayout.findViewById(R.id.nv_email);
        initSpinner();

        mSearchTextView = (EditText) mDrawerLayout.findViewById(R.id.search);
        initSearch();

        TextView settingsTextView = (TextView) mDrawerLayout.findViewById(R.id.settings);
        settingsTextView.setOnClickListener(this);

        TextView profileTextView = (TextView) mDrawerLayout.findViewById(R.id.profile_manager);
        profileTextView.setOnClickListener(this);

        TextView contributeTextView = (TextView) mDrawerLayout.findViewById(R.id.tv_contribute);
        contributeTextView.setOnClickListener(this);

        ImageView header = (ImageView) mDrawerLayout.findViewById(R.id.iv_header);
        Glide.with(this).load(R.drawable.drawer).into(header);

        super.setContentView(mDrawerLayout);
    }


    private void initDrawer() {
        mDrawerListAdapter = new ArrayAdapter<>(this, R.layout.simple_list_item, new ArrayList<>(mMenuItemsList));
        mDrawerListView.setAdapter(mDrawerListAdapter);
        mDrawerListView.setOnItemClickListener(this);
    }

    private void closeDrawer(int delayMills) {
        mHandler.postDelayed(mRunnable, delayMills);
    }

    public void initSpinner() {

        List<Profile> profilesList = ((SyngApplication) getApplication()).mPreferenceManager.getProfiles();
        ArrayList<String> spinnerItems = new ArrayList<>();
        for (Profile profile : profilesList) {
            spinnerItems.add(profile.getName());
        }
        mAccountSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, spinnerItems.toArray(new String[spinnerItems.size()])));
        mAccountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //String item = (String) adapterView.getItemAtPosition(i);
                if (adapterView != null && adapterView.getChildAt(0) != null) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.parseColor("#ffffff"));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.parseColor("#ffffff"));
            }

        });
    }

    private void initSearch() {

        mSearchTextView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchValue = editable.toString();
                updateAppList(searchValue);
            }
        });

        mSearchTextView.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    hideKeyBoard(mSearchTextView);
                    return true;
                }
                return false;
            }
        });
    }

    protected void updateAppList(String filter) {
        mDrawerListAdapter.clear();
        int length = mMenuItemsList.size();
        for (int i = 0; i < length; i++) {
            String item = mMenuItemsList.get(i);
            if (item.toLowerCase().contains(filter.toLowerCase())) {
                mDrawerListAdapter.add(item);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        onDAppClick(item);
        closeDrawer(DRAWER_CLOSE_DELAY_SHORT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_contribute:
                String url = CONTRIBUTE_LINK;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
            case R.id.settings:
                startActivity(new Intent(BaseActivity.this, SettingsActivity.class));
                break;
            case R.id.profile_manager:
                startActivity(new Intent(BaseActivity.this, ProfileManagerActivity.class));
                break;
        }
        closeDrawer(DRAWER_CLOSE_DELAY_LONG);

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void hideKeyBoard(View view) {
        if (view == null)
            return;
        InputMethodManager imm = (InputMethodManager)
                getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}