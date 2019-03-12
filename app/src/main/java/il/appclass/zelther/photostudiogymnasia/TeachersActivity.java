package il.appclass.zelther.photostudiogymnasia;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class TeachersActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, EquipmentList.DataLoaderListener, EquipmentList.DataUploadListener, View.OnClickListener, EquipmentList.DataDeleteListener {


    private ListView lvTeachers;
    private SearchView searchTeachers;
    private EquipmentList equipmentList;
    private ArrayAdapter<StudioItem> listAdapter;
    private GraySquareLoadingView animLoading;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teachers);

        lvTeachers = findViewById(R.id.lvTeachers);
        searchTeachers = findViewById(R.id.searchTeachers);
        animLoading = findViewById(R.id.animLoading);

        equipmentList = EquipmentList.sharedInstance();

        animLoading.setAnimationOn(true);

        listAdapter = new ArrayAdapter<StudioItem>(this, R.layout.listview_teachers, R.id.teacherListTitle, equipmentList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                StudioItem studioItem = listAdapter.getItem(position);
                TextView textTitle = view.findViewById(R.id.teacherListTitle);
                TextView textSubtitle = view.findViewById(R.id.teacherListSubtitle);
                ImageView icon = view.findViewById(R.id.teacherListIcon);
                ImageButton btnDelete = view.findViewById(R.id.btnDelete);

                textTitle.setText(studioItem.toString());
                textSubtitle.setText(studioItem.getTeachersData());
                icon.setImageResource(getResources().getIdentifier(studioItem.getType(),"drawable",getPackageName()));
                btnDelete.setTag(studioItem);
                btnDelete.setOnClickListener(TeachersActivity.this);
                return view;
            }
        };
        lvTeachers.setAdapter(listAdapter);

        searchTeachers.setOnQueryTextListener(this);

        sp = getSharedPreferences("teacher_filter",MODE_PRIVATE);

        updateList(sp.getBoolean("showNonTaken",true), orderRadioButtonIdToString(sp.getInt("orderBy",R.id.rbOrderType)));
    }

    /**
     * Called when filter button is clicked. Once clicked, a dialog will pop up, and will filter the list according to the options selected.
     * @param v The filter button.
     */
    public void filterButton(View v) {
        final Dialog filterDialog = new Dialog(this);
        filterDialog.setContentView(R.layout.teacher_filter_dialog);

        final CheckBox chShowNonTaken = filterDialog.findViewById(R.id.chShowNonTaken);
        final RadioGroup rgOrderTeacher = filterDialog.findViewById(R.id.rgOrderTeacher);
        final Button btnConfirmFilter = filterDialog.findViewById(R.id.btnConfirmFilter);

        chShowNonTaken.setChecked(sp.getBoolean("showNonTaken",true));
        rgOrderTeacher.check(sp.getInt("orderBy",R.id.rbOrderType));

        btnConfirmFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String orderByChosen = orderRadioButtonIdToString(rgOrderTeacher.getCheckedRadioButtonId());
                boolean showNonTaken = chShowNonTaken.isChecked();

                sp.edit().putBoolean("showNonTaken",showNonTaken).putInt("orderBy", rgOrderTeacher.getCheckedRadioButtonId()).apply();
                updateList(showNonTaken, orderByChosen);
                filterDialog.dismiss();
            }
        });

        filterDialog.show();
    }

    /**
     * Called when the add (plus) button is clicked.
     * @param v The add button.
     */
    public void addButton(View v) {
        final Dialog addDialog = new Dialog(this);
        addDialog.setContentView(R.layout.teacher_add_dialog);

        final Spinner categoryAddSpinner = addDialog.findViewById(R.id.spnAddCategory);
        Button btnAddConfirm = addDialog.findViewById(R.id.btnAddConfirm);
        Button btnAddDelete = addDialog.findViewById(R.id.btnAddCancel);
        final EditText etAddItemName = addDialog.findViewById(R.id.etAddItemName);
        final EditText etAddItemID = addDialog.findViewById(R.id.etAddItemID);

        btnAddConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemName = etAddItemName.getText().toString();
                String itemId = etAddItemID.getText().toString();
                String itemCategory;

                switch (categoryAddSpinner.getSelectedItem().toString()) {
                    case "מצלמות": itemCategory = "camera"; break;
                    case "עדשות": itemCategory = "lenses"; break;
                    case "סלייב": itemCategory = "slave"; break;
                    case "פלאשים": itemCategory = "speedlight"; break;
                    case "חצובות": itemCategory = "tripod"; break;
                    case "בטריות": itemCategory = "battery"; break;
                    default: itemCategory = null;
                }

                if(!(itemName.trim().isEmpty() || itemId.trim().isEmpty())) {
                    StudioItem si = new StudioItem(itemName, itemCategory, itemId);
                    equipmentList.uploadNewItem(TeachersActivity.this, si);
                    addDialog.dismiss();
                }
            }
        });

        btnAddDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDialog.cancel();
            }
        });

        addDialog.show();
    }

    /**
     * Updates the list with the given filter parameters.
     * @param showNonTaken if given true, the list will also show non taken items.
     * @param orderBy by what field should the list be sorted. Acceptable values: name, lastUsed, owner.
     */
    private void updateList(boolean showNonTaken, String orderBy) {
        EquipmentList.TakenFilter takenFilter;
        if(showNonTaken)
            takenFilter = EquipmentList.TakenFilter.BOTH;
        else
            takenFilter = EquipmentList.TakenFilter.ONLY_TAKEN;
        equipmentList.loadData(this,takenFilter, orderBy, null);
    }

    /**
     * Return a matching string to a specific id of one of the radio buttons in the filter dialog.
     * @param rbId A radio button's id from filter dialog.
     * @return A matching "sort" string.
     */
    private String orderRadioButtonIdToString(int rbId) {
        switch (rbId) {
            case R.id.rbOrderLastUsed: return "lastUsed";
            case R.id.rbOrderOwner: return "owner";
            default: return "type";
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) { return  true; }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(newText.isEmpty())
            listAdapter.getFilter().filter(null);
        else
            listAdapter.getFilter().filter(newText);
        return true;
    }

    @Override
    public void dataLoadChange(boolean isOk) {
        if(isOk) {
            animLoading.setAnimationOn(false);
            listAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "משהו השתבש - ודא שהנך מחובר למרשתת", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void dataUploadDidComplete(boolean isOk) {
        if(isOk) {
            Toast.makeText(this, "פריט נוסף בהצלחה!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "כשל בהוספת הפריט - ודא שאתה מחובר לאינטרנט.", Toast.LENGTH_SHORT).show();
        }
    }

    //On delete button clicked
    @Override
    public void onClick(View v) {
        final StudioItem studioItem = (StudioItem) v.getTag();

        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == DialogInterface.BUTTON_POSITIVE) {
                    equipmentList.deleteItem(TeachersActivity.this, studioItem);
                } else {
                    dialog.cancel();
                }
            }
        };

        AlertDialog deleteConfirmDialog = new AlertDialog.Builder(this)
                .setTitle("מחיקה")
                .setMessage("האם אתה בטוח שאתה רוצה למחוק את הפריט "+studioItem.toString()+"?")
                .setIcon(getResources().getIdentifier(studioItem.getType(),"drawable",getPackageName()))
                .setPositiveButton("כן",clickListener)
                .setNegativeButton("לא", clickListener)
                .create();
        deleteConfirmDialog.show();
    }

    @Override
    public void dataDeleteDidComplete(boolean isOk) {
        if(isOk) {
            Toast.makeText(this, "פריט נמחק בהצלחה.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "שגיאה במחיקת פריט. נסה שוב.", Toast.LENGTH_SHORT).show();
        }
    }
}
