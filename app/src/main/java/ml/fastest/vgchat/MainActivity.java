package ml.fastest.vgchat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.gc.materialdesign.widgets.SnackBar;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.email)
    EditText email;

    @Bind(R.id.password)
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);




    }

    public void registerAction(View v){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://fastest.ml/register"));
        startActivity(browserIntent);
    }

    public void forgotAction(View v){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://fastest.ml/remind"));
        startActivity(browserIntent);
    }

    public void loginAction(View view) {
        Facade.login(this, new Facade.OnResult() {
            @Override
            public void action(boolean success, String error) {
                if(!success) {
                    new SnackBar(MainActivity.this, error).show();
                } else {
                    startActivity(new Intent(MainActivity.this, ChatActivity.class));
                }
            }
        }, email.getText().toString(), password.getText().toString());
    }
}
