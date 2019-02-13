package jp.co.nri.voice.demo;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class TranslateUtil {

    private static final String TRANSLATE_BASE_URL = "https://translate.google.cn/";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";

    private static final String LAN_AUTO = "auto";

    // 非同期変換要求
    public void translate(Context context, String sourceLan, String targetLan, String content, TranslateCallback callback) {
        TranslateTask task = new TranslateTask(context, sourceLan, targetLan, content, callback);
        task.executeOnExecutor(Executors.newCachedThreadPool());
    }

    private class TranslateTask extends AsyncTask<Void, Void, String> {
        String sourceLan;
        String targetLan;
        String content;
        Context context;
        TranslateCallback callback;

        TranslateTask(Context context, String sourceLan, String targetLan, String content, TranslateCallback callback) {
            this.context = context;
            this.content = content;
            this.callback = callback;
            this.sourceLan = sourceLan;
            this.targetLan = targetLan;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = "";
            if (content == null || content.equals("")) {
                return result;
            }
            try {
                String googleResult = "";
                URL url = new URL(getTranslateUrl(sourceLan, targetLan, content));
                HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
                urlConn.setConnectTimeout(5 * 1000);
                urlConn.setReadTimeout(5 * 1000);
                urlConn.setUseCaches(false);
                urlConn.setRequestMethod("GET");
                urlConn.setRequestProperty("User-Agent", USER_AGENT);
                urlConn.connect();
                int statusCode = urlConn.getResponseCode();
                if (statusCode == 200) {
                    googleResult = streamToString(urlConn.getInputStream());
                }
                urlConn.disconnect();
                JSONArray jsonArray = new JSONArray(googleResult).getJSONArray(0);
                for (int i = 0; i < jsonArray.length(); i++) {
                    result += jsonArray.getJSONArray(i).getString(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
                result = "";
            }
            Log.d("TAG", "翻译结果：" + result);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (callback != null) {
                callback.onTranslateDone(result);
            }
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    public static String streamToString(InputStream is) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.close();
            is.close();
            byte[] byteArray = out.toByteArray();
            return new String(byteArray);
        } catch (Exception e) {
            return null;
        }
    }

    public void translate(Context context, String targetLan, String content, TranslateCallback callback) {
        translate(context, LAN_AUTO, targetLan, content, callback);
    }


    private static String getTranslateUrl(String sourceLan, String targetLan, String content) {
        try {
            return TRANSLATE_BASE_URL + "translate_a/single?client=gtx&sl=" + sourceLan + "&tl=" + targetLan + "&dt=t&q=" + URLEncoder.encode(content, "UTF-8");
        } catch (Exception e) {
            return TRANSLATE_BASE_URL + "translate_a/single?client=gtx&sl=" + sourceLan + "&tl=" + targetLan + "&dt=t&q=" + content;
        }
    }

    public interface TranslateCallback {
        public void onTranslateDone(String result);
    }
}