package ml.fastest.vgchat;

import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.gc.materialdesign.widgets.SnackBar;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import ml.fastest.vgchat.core.Facade;
import ml.fastest.vgchat.core.MessagesAdapter;
import ml.fastest.vgchat.models.Message;
import ml.fastest.vgchat.models.MessageSending;
import ml.fastest.vgchat.notifications.NewMessageNotification;

public class ChatActivity extends AppCompatActivity {

    @Bind(R.id.listView)
    ListView listView;

    @Bind(R.id.message)
    EditText message;

    @Bind(R.id.typing)
    TextView typingInd;

    private MessagesAdapter adapter;
    private ArrayList<Message> messages;

    private String nickname;
    private String avatar;
    private String sessid;
    private String user_id;
    private boolean CONNECTED = true;
    private boolean mIsInForegroundMode;

    private Timer typingOut = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        if (!Facade.isLoggedIn(this)) {
            startActivity(new Intent(this, MainActivity.class));
            finishActivity(0);
            finish();

            return;
        }

        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        try {
            getSupportActionBar().show();
        } catch (Exception e) {
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        nickname = prefs.getString("nickname", "");
        avatar = prefs.getString("avatar", "");
        sessid = prefs.getString("sessid", "");
        user_id = prefs.getString("user_id", "");
        mSocket.on("chat message", onNewMessage);
        mSocket.on("chat typing", onTyping);
        mSocket.on("broadcast", onBroadcast);
        mSocket.on("users online", onUsersOnline);
        mSocket.on("activity", onUserActivity);
        mSocket.connect();

        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
        listView.setStackFromBottom(true);


        final SnackBar errorInformer = new SnackBar(this, "Connection lost. Trying to reconnect...");
        errorInformer.setIndeterminate(true);

        Timer checker = new Timer();

        checker.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!mSocket.connected() && CONNECTED) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            errorInformer.show();
                        }
                    });

                    CONNECTED = false;
                }

                if (mSocket.connected() && !CONNECTED) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            errorInformer.hide();
                        }
                    });
                    CONNECTED = true;
                }

                try {
                    JSONObject args = new JSONObject();
                    args.put("sessid", sessid);

                    args.put("id", user_id);

                    args.put("active", mIsInForegroundMode);
                    mSocket.emit("activity", args);
                } catch (Exception e) {

                }
            }
        }, 1000, 2000);
        message.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String msg = message.getText().toString();

                try {
                    msg = new String(msg.getBytes(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.e("utf8", "conversion", e);
                }

                if (msg.trim().length() == 0) {
                    return true;
                } else {
                    try {
                        JSONObject args = new JSONObject();
                        args.put("sessid", sessid);
                        args.put("avatar", avatar);
                        args.put("nickname", nickname);
                        args.put("user", nickname);
                        args.put("message", msg.trim());
                        args.put("user_id", user_id);
                        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                        args.put("date", dateFormat.format(new Date()).toString());
                        mSocket.emit("chat message", args);


                        MessageSending m = new MessageSending();
                        m.setMessage(msg);


                        Facade.sendMessage(ChatActivity.this, m);

                    } catch (Exception e) {

                    }

                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject args2 = new JSONObject();
                                args2.put("sessid", sessid);
                                args2.put("nickname", nickname);
                                mSocket.emit("chat notyping", args2);
                            } catch (Exception e) {
                                Log.e("Chat", "error", e);
                            }
                        }
                    }, 1000);

                    scrollMyListViewToBottom();
                    message.getText().clear();
                    return true;
                }
            }
        });

        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                try {
                    final JSONObject args = new JSONObject();
                    typingOut.cancel();
                    args.put("sessid", sessid);
                    args.put("nickname", nickname);
                    mSocket.emit("chat typing", args);
                    typingOut = new Timer();
                    typingOut.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mSocket.emit("chat notyping", args);
                            typingOut.cancel();
                            this.cancel();
                        }
                    }, 4000, 1000);
                } catch (Exception e) {
                    Log.e("Excc", e.getMessage(), e);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Facade.getMessages(this, new Facade.OnMessagesLoaded() {
            @Override
            public void action(ArrayList<Message> messages) {
                ChatActivity.this.messages = messages;
                adapter = new MessagesAdapter(ChatActivity.this, messages);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });

    }


    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
        this.onPause();
    }

    @Override
    public void onPause() {
        super.onPause();
        mIsInForegroundMode = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsInForegroundMode = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            Facade.logout(this);
            finish();
        }
        return true;
    }

    private void scrollMyListViewToBottom() {
        listView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listView.setSelection(adapter.getCount() - 1);
            }
        });
    }

    private Socket mSocket;

    {
        try {
            mSocket = IO.socket(Facade.URL_PREFIX + ":3000");
        } catch (URISyntaxException e) {
        }
    }


    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    String avatar;
                    String date;
                    String user_id;

                    try {
                        username = data.getString("nickname");
                        message = data.getString("message");
                        avatar = data.getString("avatar");
                        date = data.getString("date");
                        user_id = data.getString("user_id");

                        Message m = new Message(avatar, username, message, date, user_id);
                        m.setOnline(true);
                        messages.add(m);
                        adapter.notifyDataSetChanged();
                        scrollMyListViewToBottom();

                        //final MediaPlayer mp = MediaPlayer.create(ChatActivity.this, R.raw.notification);
                        sendNotification(avatar, username, message);
                        //mp.start();

                    } catch (JSONException e) {

                    }


                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    JSONObject typing = (JSONObject) args[0];

                    String typingStr = "";
                    Iterator<String> keys = typing.keys();
                    while (keys.hasNext()) {
                        typingStr += keys.next() + ", ";
                    }


                    if (typingStr.length() > 0) {
                        typingStr += " печатает";
                    }
                    typingInd.setText(typingStr);


                }
            });
        }
    };

    private Emitter.Listener onUsersOnline = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject typing = (JSONObject) args[0];
                        processUsersOnline(typing);
                    } catch (Exception e) {
                        Log.e("ChatActivity", e.getLocalizedMessage());
                    }

                }
            });
        }
    };
    private Emitter.Listener onUserActivity = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject act = (JSONObject) args[0];
                        processUserActivity(act);
                    } catch (Exception e) {
                        Log.e("ChatActivity", e.getLocalizedMessage());
                    }

                }
            });
        }
    };

    private void processUserActivity(JSONObject act) {
        try {
            int userId = Integer.parseInt(act.get("id").toString());
            boolean active = Boolean.parseBoolean(act.get("active").toString());


            for (int i = 0; i < messages.size(); i++) {
                Message m = messages.get(i);
                int mid = Integer.parseInt(m.getUser_id());

                if (mid == userId) {
                    adapter.setActive(i, active);
                    adapter.notifyDataSetChanged();
                }
            }

        } catch (Exception e) {
            Log.e("Exc", e.getLocalizedMessage());
        }
    }

    private Emitter.Listener onBroadcast = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final JSONObject msg = (JSONObject) args[0];
                        final Bitmap icon = BitmapFactory.decodeResource(getResources(),
                                R.drawable.notif);
                        NewMessageNotification.notify(
                                ChatActivity.this,
                                icon,
                                "Broadcasting",
                                msg.getString("msg"),
                                1,
                                Notification.CATEGORY_SOCIAL
                        );
                    } catch (Exception e) {
                        Log.e("ChatActivity", e.getLocalizedMessage());
                    }
                }
            });
        }
    };

    private void sendNotification(final String avatar, final String nickname, final String message) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                Bitmap picture = null;
                try {
                    picture = Picasso.with(ChatActivity.this).load("https://fastest.ml" + avatar).get();
                } catch (IOException e) {
                    picture = BitmapFactory.decodeResource(getResources(),
                            R.drawable.notif);
                }

                if (!nickname.equals(ChatActivity.this.nickname)) {
                    NewMessageNotification.notify(ChatActivity.this, picture, nickname, message, 0, Notification.CATEGORY_MESSAGE);
                }
                return null;
            }
        }.execute();
    }

    private void processUsersOnline(JSONObject data) {
        try {
            Iterator<String> keys = data.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                int userId = Integer.parseInt(key);
                boolean online = data.getBoolean(key);

                for (int i = 0; i < messages.size(); i++) {
                    Message m = messages.get(i);
                    int mid = Integer.parseInt(m.getUser_id());
                    if (mid == userId) {
                        adapter.setOnline(i, online);
                        adapter.notifyDataSetChanged();
                    }


                }
            }
        } catch (Exception ignored) {

        }
    }
}
