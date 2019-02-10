package il.appclass.zelther.photostudiogymnasia;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etName, etPassword;
    private TextView tvLoginError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);
        tvLoginError = findViewById(R.id.tvLoginError);

        // login button listener
        Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String name = etName.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (name.equals("")) { //name is empty
            tvLoginError.setText("נא הכנס את שמך המלא.");
            tvLoginError.setVisibility(View.VISIBLE);

        } else if (!password.equals(getResources().getString(R.string.password))) { //password incorrect
            tvLoginError.setText("סיסמה שגויה. נא פנה לאחראי על הסטודיו.");
            tvLoginError.setVisibility(View.VISIBLE);

        } else { //successfully logged in
            Intent loginDataIntent = new Intent();
            loginDataIntent.putExtra("username",name);
            setResult(RESULT_OK, loginDataIntent);
            finish();
        }
    }
}
