package jp.classmethod.android.sample.searchstation;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * APIをコールする非同期タスク.
 */
public class ExpressLoader extends AsyncTaskLoader<ArrayList<HashMap<String, String>>> {

    /** エリア取得ID. */
    public static final int AREAS = 0;
    /** 都道府県取得ID. */
    public static final int PREFECTURES = 1;
    /** 路線取得ID. */
    public static final int LINES = 2;
    /** 駅取得ID. */
    public static final int STATIONS = 3;

    /** Logcat出力用タグ. */
    private static final String TAG = ExpressLoader.class.getSimpleName();
    /** パラメータにつける名前(エリアor都道府県or路線). */
    private String mName;

    /**
     * コンストラクタ.
     * @param context Context
     * @param name パラメータにつける名前(エリアor都道府県or路線)
     */
    public ExpressLoader(Context context, String name) {
        super(context);
        mName = name;
        forceLoad();
    }

    @Override
    public ArrayList<HashMap<String, String>> loadInBackground() {

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

        JSONObject obj = get(mName);

        if (obj != null) {
            // "選択してください"アイテムを先頭に追加
            HashMap<String, String> notSelected = new HashMap<String, String>();
            notSelected.put("name", "選択してください");
            list.add(notSelected);
            try {
                // JSONからデータを取り出す
                JSONObject response = obj.getJSONObject("response");
                JSONArray array = null;
                switch (getId()) {
                    case AREAS:
                        array = response.getJSONArray("area");
                        for (int i = 0; i < array.length(); i++) {
                            HashMap<String, String> station = new HashMap<String, String>();
                            station.put("name", array.getString(i));
                            list.add(station);
                        }
                        break;
                    case PREFECTURES:
                        array = response.getJSONArray("prefecture");
                        for (int i = 0; i < array.length(); i++) {
                            HashMap<String, String> station = new HashMap<String, String>();
                            station.put("name", array.getString(i));
                            list.add(station);
                        }
                        break;
                    case LINES:
                        array = response.getJSONArray("line");
                        for (int i = 0; i < array.length(); i++) {
                            HashMap<String, String> station = new HashMap<String, String>();
                            station.put("name", array.getString(i));
                            list.add(station);
                        }
                        break;
                    case STATIONS:
                        array = response.getJSONArray("station");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject item = array.getJSONObject(i);
                            HashMap<String, String> station = new HashMap<String, String>();
                            station.put("name", item.getString("name"));
                            station.put("latitude", item.getString("y"));
                            station.put("longitude", item.getString("x"));
                            list.add(station);
                        }
                        break;
                }
            } catch (Exception e) {
                return null;
            }
        }
        return list;
    }

    /**
     * Getリクエストを実行してBodyを取得する.
     * @param name パラメータにつける名前(エリアor都道府県or路線)
     * @return JSONObject
     */
    private JSONObject get(String name) {
        try {
            String url = "http://express.heartrails.com/api/json?" + getParams(name);
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet(url);
            HttpResponse res = httpClient.execute(get);
            HttpEntity entity = res.getEntity();
            String body = EntityUtils.toString(entity);
            return new JSONObject(body);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * URLパラメータを返す.
     * @param name パラメータにつける名前(エリアor都道府県or路線)
     * @return URLパラメータ
     */
    private String getParams(String name) {
        switch (getId()) {
            case AREAS:
                return "method=getAreas";
            case PREFECTURES:
                return "method=getPrefectures&area=" + name;
            case LINES:
                return "method=getLines&prefecture=" + name;
            case STATIONS:
                return "method=getStations&line=" + name;
        }
        return null;
    }
}
