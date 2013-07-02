package jp.classmethod.android.sample.searchstation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchActivity extends FragmentActivity
        implements  View.OnClickListener, AdapterView.OnItemSelectedListener,
            LoaderManager.LoaderCallbacks<ArrayList<HashMap<String, String>>> {

    private final SearchActivity self = this;

    private ProgressDialog mProgressDialog;
    private Spinner mAreas;
    private Spinner mPrefectures;
    private Spinner mLines;
    private Spinner mStations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mAreas = (Spinner) findViewById(R.id.areas);
        mPrefectures = (Spinner) findViewById(R.id.prefectures);
        mLines = (Spinner) findViewById(R.id.lines);
        mStations = (Spinner) findViewById(R.id.stations);

        findViewById(R.id.view).setOnClickListener(self);

        getSupportLoaderManager().initLoader(ExpressLoader.AREAS, null, self);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public Loader<ArrayList<HashMap<String, String>>> onCreateLoader(int i, Bundle bundle) {
        mProgressDialog = ProgressDialog.show(self, "検索中", "しばらくお待ち下さい...");
        String name = bundle == null ? "" : bundle.getString("name");
        return new ExpressLoader(self, name);
    }

    @Override
    public void onLoadFinished(
            Loader<ArrayList<HashMap<String, String>>> loader,
            ArrayList<HashMap<String, String>> list) {
        mProgressDialog.dismiss();
        if (list == null) {
            Toast.makeText(self, "エラー", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleAdapter adapter = new SimpleAdapter(
                self, list, android.R.layout.simple_spinner_item,
                new String[] {"name"}, new int[] {android.R.id.text1});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        switch (loader.getId()) {
            case ExpressLoader.AREAS:
                mAreas.setAdapter(adapter);
                mAreas.setOnItemSelectedListener(self);
                mPrefectures.setAdapter(null);
                mLines.setAdapter(null);
                mStations.setAdapter(null);
                break;
            case ExpressLoader.PREFECTURES:
                mPrefectures.setAdapter(adapter);
                mPrefectures.setOnItemSelectedListener(self);
                mLines.setAdapter(null);
                mStations.setAdapter(null);
                break;
            case ExpressLoader.LINES:
                mLines.setAdapter(adapter);
                mLines.setOnItemSelectedListener(self);
                mStations.setAdapter(null);
                break;
            case ExpressLoader.STATIONS:
                mStations.setAdapter(adapter);
                break;
        }
        getSupportLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<HashMap<String, String>>> loader) {
    }

    @Override
    public void onClick(View view) {
        HashMap<String, String> station = (HashMap<String, String>) mStations.getSelectedItem();
        if (station == null) {
            Toast.makeText(self, "駅が選択されていません", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
        intent.setData(Uri.parse("geo:" + station.get("latitude") + "," + station.get("longitude") + "?z=16"));
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (i == 0) {
            return;
        }
        HashMap<String, String> item = (HashMap<String, String>) adapterView.getAdapter().getItem(i);
        Bundle bundle = new Bundle();
        bundle.putString("name", item.get("name"));

        int loaderId = -1;
        switch (adapterView.getId()) {
            case R.id.areas:
                loaderId = ExpressLoader.PREFECTURES;
                break;
            case R.id.prefectures:
                loaderId = ExpressLoader.LINES;
                break;
            case R.id.lines:
                loaderId = ExpressLoader.STATIONS;
                break;
        }
        getSupportLoaderManager().initLoader(loaderId, bundle, self);
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}
