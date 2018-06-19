package jp.mkserver.bungeediscordchat.transrate;

import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;
import jp.mkserver.bungeediscordchat.BungeeDiscordChat;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class TranslateAPI {
    private static String KEY = ""; // APIキー
    private static final String HTTP_OK = "HTTP_OK"; // HttpUrlConnectionステータス
    private String mTranslateWord = ""; // 翻訳テキスト
    private String mApiToken = ""; // APIトークン
    private BungeeDiscordChat bdc;

    // コンストラクタ
    public TranslateAPI(BungeeDiscordChat bdc, String word, String apikey){
        this.bdc = bdc;
        mTranslateWord = word;
        KEY = apikey;
    }

    public String Translate(String from,String to){
        return Translate_preparation(from,to);
    }

    public String getToken(){
        HttpClient httpclient = HttpClients.createDefault();

        try {
            URIBuilder builder = new URIBuilder("https://api.cognitive.microsoft.com/sts/v1.0/issueToken");


            URI uri = builder.build();
            HttpPost request = new HttpPost(uri);
            request.setHeader("Ocp-Apim-Subscription-Key", KEY);

            // Request body
            StringEntity reqEntity = new StringEntity("{body}");
            request.setEntity(reqEntity);
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null){
                return EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }


    public  String Translate_preparation(String from,String to){
        bdc.getLogger().info(KEY);
        mApiToken = getToken();
        BufferedReader reader = null;
        try {
            // Using the access token to build the appid for the request url
            String mTranslatedWord = "";
            String appId = URLEncoder.encode("Bearer "+mApiToken, "UTF-8");
            String translatorTextApiUrl = "http://api.microsofttranslator.com/v2/Http.svc/Translate?appid="+appId+"&from="+from+"&to="+to+"&text="+URLEncoder.encode(mTranslateWord, "UTF-8");
            HttpURLConnection translateConn = (HttpURLConnection) new URL(translatorTextApiUrl).openConnection();
            translateConn.setRequestMethod("GET");
            translateConn.setRequestProperty("Authorization",appId);
            int statuss = translateConn.getResponseCode();
            if (statuss == HttpURLConnection.HTTP_OK) {
                // レスポンスを受け取る処理等
                StringBuilder stringBuilder = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(translateConn.getInputStream(),"UTF-8"));
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    stringBuilder.append(inputLine);
                }
                mTranslatedWord = stringBuilder.toString();
                mTranslatedWord = mTranslatedWord.replace("<string xmlns=\"http://schemas.microsoft.com/2003/10/Serialization/\">", "");
                mTranslatedWord = mTranslatedWord.replace("</string>", "");
            } else{
                return null;
            }
            return mTranslatedWord;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
