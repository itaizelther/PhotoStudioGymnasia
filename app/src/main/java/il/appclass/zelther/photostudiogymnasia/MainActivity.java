package il.appclass.zelther.photostudiogymnasia;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sp; //SharedPreferences instance for saving user data
    private boolean isTeacher; // true if the user logged as teacher
    private Button btnManageDatabase; // the button exclusive for teachers
    private MenuItem menuTeacher; // menu exclusive for teachers
    private String username; // user's name
    private final int LOGIN_REQUEST_CODE = 1; // request code for login activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnManageDatabase = findViewById(R.id.btnManageDatabase);

        sp = getSharedPreferences("account", MODE_PRIVATE);
        if (!sp.getBoolean("logged", false)) //if not logged in
            askLogin();
        else
            username = sp.getString("username",null);
    }

    /**
     * handles the login process, moving to the login activity
     */
    private void askLogin() {
        Intent loginIntent = new Intent(this , LoginActivity.class);
        startActivityForResult(loginIntent, LOGIN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == LOGIN_REQUEST_CODE && resultCode == RESULT_OK) {
            username = data.getStringExtra("username");
            sp.edit().putBoolean("logged", true).putString("username",username).apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        menuTeacher = menu.findItem(R.id.menuTeacher);

        if(isTeacher = sp.getBoolean("isTeacher",false)) //hide the teacher login if is already logged in as a teacher
            updateTeacherLayout();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuAbout:  // about
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                AlertDialog aboutDialog = builder.setTitle("אודות").setNeutralButton("סגור", null)
                        .setMessage("אפליקציה זו נוצרה עבור הסטודיו של מגמת צילום בבית ספר התיכון \"הגימנסיה העברית הרצליה\" בתל אביב. " +
                                "בעזרת האפליקציה, יכולים האחראים על הסטודיו לעקוב אחר הציוד." +
                                "\n\n אפליקציה זו נוצרה על ידי איתי זלצר. צרו איתי קשר פה: itaizelther@gmail.com" +
                                "\n גרסה: " + BuildConfig.VERSION_NAME).create();
                aboutDialog.show();
                return true;

            case R.id.menuTeacher: // teachers login
                final Dialog teacherLoginDialog = new Dialog(this);
                teacherLoginDialog.setContentView(R.layout.teacher_login_dialog);

                final Button btnLoginTeacher = teacherLoginDialog.findViewById(R.id.btnLoginTeacher);
                Button btnCancelLoginTeacher = teacherLoginDialog.findViewById(R.id.btnCancelLoginTeacher);

                btnLoginTeacher.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {    //Login teacher
                        EditText etPasswordTeacher = teacherLoginDialog.findViewById(R.id.etPasswordTeacher);
                        if(etPasswordTeacher.getText().toString().equals(getResources().getString(R.string.teacher_password))) {
                            teacherLoginDialog.dismiss();
                            isTeacher = true;
                            sp.edit().putBoolean("isTeacher",isTeacher).commit();
                            updateTeacherLayout();
                        }
                    }
                });

                btnCancelLoginTeacher.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {   //cancel teacher login dialog
                        teacherLoginDialog.cancel();
                    }
                });

                teacherLoginDialog.show();
                return true;

            default:
                return false;
        }
    }

    /**
     * This method updates the main layout to be as it should be to teachers.
     */
    private void updateTeacherLayout() {
        btnManageDatabase.setVisibility(View.VISIBLE);
        menuTeacher.setVisible(false);
    }

    /**
     * When one of the main buttons is clicked. listener is set on the xml file
     * @param v the button instance
     */
    public void onButtonAction(View v) {
        switch (v.getId()) {
            case R.id.btnLend: // if the user wants to take equipment
                Intent lendIntent = new Intent(this, LendActivity.class);
                lendIntent.putExtra("lend",true); // if want to take and not to return
                lendIntent.putExtra("username",username);
                startActivity(lendIntent);
                break;

            case R.id.btnReturn: //if the user wants to return equipment
                Intent returnIntent = new Intent(this, LendActivity.class);
                returnIntent.putExtra("lend",false); // if want to return and not to take
                returnIntent.putExtra("username",username);
                startActivity(returnIntent);
                break;

            case R.id.btnManageDatabase: //if a teacher wants to manage the equipment data
                //TODO teachers screen
                break;
            default: break;
        }
    }
}
