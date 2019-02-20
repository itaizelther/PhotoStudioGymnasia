package il.appclass.zelther.photostudiogymnasia;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class TeachersActivity extends AppCompatActivity {


    private FloatingActionButton btnAddItem;
    private ListView lvTeachers;
    private SearchView searchTeachers;
    private FirebaseFirestore db;
    private ArrayAdapter<StudioItem> listAdapater;
    private View animLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teachers);

        lvTeachers = findViewById(R.id.lvTeachers);
        btnAddItem = findViewById(R.id.btnAddItem);
        searchTeachers = findViewById(R.id.searchTeachers);
        animLoading = findViewById(R.id.animLoading);

        animLoadingSwitch(true);

        listAdapater = new ArrayAdapter<StudioItem>(this, R.layout.listview_teachers, R.id.teacherListTitle, new ArrayList<StudioItem>()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                StudioItem studioItem = listAdapater.getItem(position);
                TextView textTitle = view.findViewById(R.id.teacherListTitle);
                TextView textSubtitle = view.findViewById(R.id.teacherListSubtitle);
                ImageView icon = view.findViewById(R.id.teacherListIcon);

                textTitle.setText(studioItem.toString());
                textSubtitle.setText("subtitle");
                icon.setImageResource(getResources().getIdentifier(studioItem.getType(),"drawable",getPackageName()));
                return view;
            }
        };
        lvTeachers.setAdapter(listAdapater);

        db = FirebaseFirestore.getInstance();

        db.collection("equipment").orderBy("type").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                animLoadingSwitch(false);
                for(DocumentSnapshot ds : queryDocumentSnapshots.getDocuments()) {
                    listAdapater.add(ds.toObject(StudioItem.class).withId(ds.getId()));
                }
            }
        });
    }

    private void animLoadingSwitch(boolean on) {
        if(on) {
            RotateAnimation rotateAnimation = new RotateAnimation(0,360, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            rotateAnimation.setDuration(4000);
            rotateAnimation.setRepeatCount(Animation.INFINITE);
            animLoading.startAnimation(rotateAnimation);
            animLoading.setVisibility(View.VISIBLE);
        } else {
            animLoading.clearAnimation();
            animLoading.setVisibility(View.GONE);
        }
    }

    public void filterButton(View v) {
        //TODO make filter dialog
    }
}
