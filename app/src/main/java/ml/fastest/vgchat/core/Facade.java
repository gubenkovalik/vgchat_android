package ml.fastest.vgchat.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import jp.satorufujiwara.http.LightHttpClient;
import jp.satorufujiwara.http.Request;
import jp.satorufujiwara.http.Response;
import jp.satorufujiwara.http.gson.GsonConverterProvider;
import ml.fastest.vgchat.models.Message;
import ml.fastest.vgchat.models.MessageSending;

/**
 * Created by valik on 15.03.16.
 */
public class Facade {

    public static final String URL_PREFIX = "https://jencat.ml";
    
    public static void login(final Context context, OnResult onResult, final String email, final String password) {

        new AsyncTask<OnResult, Void, Void>() {

            private OnResult onResult;
            private boolean success;
            private String error;

            @Override
            protected Void doInBackground(OnResult... params) {
                this.onResult = params[0];

                LightHttpClient httpClient = new LightHttpClient();
                Request request = httpClient.newRequest()
                        .url(URL_PREFIX + "/android/login?email=" + email + "&password=" + password)
                        .get()
                        .build();
                try {
                    Response<String> response = httpClient.newCall(request).execute();

                    String body = response.getBody();

                    JSONObject resp = new JSONObject(body);
                    if (resp.has("error")) {
                        this.success = false;
                        this.error = resp.getString("error");
                        return null;
                    }

                    String access_token = resp.getString("access_token");
                    String avatar = resp.getString("avatar");
                    String nickname = resp.getString("nickname");
                    String sessid = resp.getString("sessid");
                    String user_id = resp.getString("user_id");



                    SharedPreferences.Editor editor =  PreferenceManager
                            .getDefaultSharedPreferences(context)
                            .edit();

                    editor.putString("access_token", access_token);
                    editor.putString("avatar", avatar);
                    editor.putString("nickname", nickname);
                    editor.putString("sessid", sessid);
                    editor.putString("user_id", user_id);
                    editor.apply();

                    this.success = true;

                } catch (Exception e) {
                    this.error = "Connection problem";
                    this.success = false;
                }


                return null;
            }

            protected void onPostExecute(Void v) {
                this.onResult.action(this.success, this.error);
            }
        }.execute(onResult);
    }

    public static void register(final Context context, OnResult onResult, final String email, final String password, final String nickname) {
        new AsyncTask<OnResult, Void, Void>() {

            private OnResult onResult;
            private boolean success;
            private String error;

            @Override
            protected Void doInBackground(OnResult... params) {
                this.onResult = params[0];

                LightHttpClient httpClient = new LightHttpClient();
                Request request = httpClient.newRequest()
                        .url(URL_PREFIX + "/android/register?email=" + email + "&password=" + password + "&nickname=" + nickname)
                        .get()
                        .build();
                try {
                    Response<String> response = httpClient.newCall(request).execute();

                    String body = response.getBody();

                    JSONObject resp = new JSONObject(body);
                    if (resp.has("error")) {
                        this.success = false;
                        this.error = resp.getString("error");
                        return null;
                    }


                    this.success = true;
                    this.error = "Confirmation sent to " + email;

                } catch (Exception e) {
                    this.error = "Connection problem";
                    this.success = false;
                }


                return null;
            }

            protected void onPostExecute(Void v) {
                this.onResult.action(this.success, this.error);
            }
        };
    }


    public static void getMessages(Context context, OnMessagesLoaded loaded) {
        String access_token = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("access_token", "");

        new MessagesLoader(context, access_token, loaded).execute();


    }

    public interface OnResult {
        void action(boolean success, String error);
    }

    public interface OnMessagesLoaded {
        void action(ArrayList<Message> messages);
    }

    private static class MessagesLoader extends AsyncTask<Void, Void, Void> {

        private final Context context;
        private final String access_token;
        private final OnMessagesLoaded loaded;

        private ArrayList<Message> messages;

        public MessagesLoader(Context context, String access_token, OnMessagesLoaded loaded) {
            this.context = context;
            this.access_token = access_token;
            this.loaded = loaded;
        }

        @Override
        protected Void doInBackground(Void... params) {

            LightHttpClient httpClient = new LightHttpClient();
            Request request = httpClient.newRequest()
                    .url(URL_PREFIX + "/android/get?access_token=" + access_token)
                    .get()
                    .build();
            try {
                Response<String> response = httpClient.newCall(request).execute();

                String body = response.getBody();

                JSONArray msgs = new JSONArray(body);

                messages = new ArrayList<>();

                for (int i = 0; i < msgs.length(); i++) {
                    JSONObject o = msgs.getJSONObject(i);
                    String avatar = o.getString("avatar");
                    String user_id = o.getString("user_id");
                    String nickname = o.getString("nickname");
                    String message = o.getString("message");
                    String created_at = o.getString("created_at");

                    Message m = new Message(avatar, nickname, message, created_at, user_id);
                    messages.add(m);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("exp", e.getLocalizedMessage());
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            this.loaded.action(messages);
        }
    }

    public static void sendMessage(Context context, final MessageSending message){

        final String access_token = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("access_token", "");
        message.setAccess_token(access_token);

        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                LightHttpClient httpClient = new LightHttpClient();
                httpClient.setConverterProvider(new GsonConverterProvider());
                Request request = httpClient.newRequest()
                        .url(URL_PREFIX + "/android/send")
                        .post(message, MessageSending.class)
                        .build();
                try {
                    Response<String> response = httpClient.newCall(request).execute();

                    String body = response.getBody();

                } catch (Exception e){
                    Log.e("ex", e.getLocalizedMessage());
                }
                return null;
            }
        }.execute();
    }

    public static boolean isLoggedIn(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("access_token", null) != null;
    }

    public static void logout(Context c){
        SharedPreferences.Editor editor =  PreferenceManager
                .getDefaultSharedPreferences(c)
                .edit();
        editor.clear().apply();
    }
}
