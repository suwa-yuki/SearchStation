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

/**
 * 駅を検索するActivity.
 */
public class SearchActivity extends FragmentActivity
        implements  View.OnClickListener, AdapterView.OnItemSelectedListener,
            LoaderManager.LoaderCallbacks<ArrayList<HashMap<String, String>>> {

    /** 自身のインスタンス. */
    private final SearchActivity self = this;

    /** ProgressDialog. */
    private ProgressDialog mProgressDialog;

    /** エリア選択Spinner. */
    private Spinner mAreas;
    /** 都道府県選択Spinner. */
    private Spinner mPrefectures;
    /** 路線選択Spinner. */
    private Spinner mLines;
    /** 駅選択Spinner. */
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
            // 何らかの理由により取得できない
            Toast.makeText(self, "エラー", Toast.LENGTH_SHORT).show();
            return;
        }
        // 取得したデータを対象のSpinnerにセットする
        SimpleAdapter adapter = new SimpleAdapter(
                self, list, android.R.layout.simple_spinner_item,
                new String[] {"name"}, new int[] {android.R.id.text1});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        switch (loader.getId()) {
            case ExpressLoader.AREAS:
                mAreas.setAdapter(adapter);
                mAreas.setOnItemSelectedListener(self);
                break;
            case ExpressLoader.PREFECTURES:
                mPrefectures.setAdapter(adapter);
                mPrefectures.setOnItemSelectedListener(self);
                break;
            case ExpressLoader.LINES:
                mLines.setAdapter(adapter);
                mLines.setOnItemSelectedListener(self);
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
        if (mStations.getSelectedItemPosition() == 0) {
            // 駅が選択されていない
            Toast.makeText(self, "駅が選択されていません", Toast.LENGTH_SHORT).show();
            return;
        }
        // GoogleMapアプリで表示
        HashMap<String, String> station = (HashMap<String, String>) mStations.getSelectedItem();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClassName("com.google.android.apps.maps","com.google.android.maps.MapsActivity");
        intent.setData(Uri.parse("geo:" + station.get("latitude") + "," + station.get("longitude") + "?z=16"));
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (i == 0) {
            // アイテムが選択されていない
            return;
        }
        // アイテムを取得してリクエストにセット
        HashMap<String, String> item = (HashMap<String, String>) adapterView.getAdapter().getItem(i);
        Bundle bundle = new Bundle();
        bundle.putString("name", item.get("name"));
        // 実行するメソッドを設定
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
