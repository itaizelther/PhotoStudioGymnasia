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

public class TeachersActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, EquipmentList.DataLoaderListener {


    private FloatingActionButton btnAddItem;
    private ListView lvTeachers;
    private SearchView searchTeachers;
    private EquipmentList equipmentList;
    private ArrayAdapter<StudioItem> listAdapater;
    private GraySquareLoadingView animLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teachers);

        lvTeachers = findViewById(R.id.lvTeachers);
        btnAddItem = findViewById(R.id.btnAddItem);
        searchTeachers = findViewById(R.id.searchTeachers);
        animLoading = findViewById(R.id.animLoading);

        equipmentList = EquipmentList.sharedInstance();

        animLoading.setAnimationOn(true);

        listAdapater = new ArrayAdapter<StudioItem>(this, R.layout.listview_teachers, R.id.teacherListTitle, equipmentList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                StudioItem studioItem = listAdapater.getItem(position);
                TextView textTitle = view.findViewById(R.id.teacherListTitle);
                TextView textSubtitle = view.findViewById(R.id.teacherListSubtitle);
                ImageView icon = view.findViewById(R.id.teacherListIcon);

                textTitle.setText(studioItem.toString());
                textSubtitle.setText(studioItem.getTeachersData());
                icon.setImageResource(getResources().getIdentifier(studioItem.getType(),"drawable",getPackageName()));
                return view;
            }
        };
        lvTeachers.setAdapter(listAdapater);

        searchTeachers.setOnQueryTextListener(this);

        equipmentList.loadData(this, EquipmentList.TakenFilter.BOTH, null);
    }

    public void filterButton(View v) {
        //TODO make filter dialog
    }

    @Override
    public boolean onQueryTextSubmit(String query) { return  true; }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(newText.isEmpty())
            listAdapater.getFilter().filter(null);
        else
            listAdapater.getFilter().filter(newText);
        return true;
    }

    @Override
    public void dataLoadChange(boolean isOk) {
        if(isOk) {
            animLoading.setAnimationOn(false);
            listAdapater.notifyDataSetChanged();
        }
    }
}
