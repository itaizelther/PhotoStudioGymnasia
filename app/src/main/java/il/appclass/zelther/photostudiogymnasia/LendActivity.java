package il.appclass.zelther.photostudiogymnasia;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The screen where the user can lend equipment or return them. The available items in each category will show up, updating it in real time.
 * @author Itai Zelther
 * @see MainActivity
 * @see BarcodeScanActivity
 */
public class LendActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, EquipmentList.DataLoaderListener, EquipmentList.DataUploadListener {

    private ListView lvLend; // the ListView instance
    private ArrayAdapter<StudioItem> adapterItems; // adapter for the list and ListView
    private GraySquareLoadingView animLoading; // the view used for animation
    private EquipmentList equipmentList; // The equipment list object where data is stored
    private boolean toLend; //the user wants to lend or return
    private String username; //user's name
    private TextView tvEmptyList; // TextView to notify if the list is empty
    private final int REQUEST_BARCODE_CODE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lend);

        toLend = getIntent().getBooleanExtra("lend",true);
        username = getIntent().getStringExtra("username");

        TextView tvLendTitle = findViewById(R.id.tvLendTitle);
        tvEmptyList = findViewById(R.id.tvEmptyList);

        if(toLend) {
            tvLendTitle.setText("שאילת ציוד");
            tvEmptyList.setText("אין חפצים זמינים להשאלה");
        }
        else {
            tvLendTitle.setText("החזרת ציוד");
            tvEmptyList.setText("לא השאלת אף חפץ");
        }

        animLoading = findViewById(R.id.animLoading);
        SearchView searchItems = findViewById(R.id.searchItems);
        lvLend = findViewById(R.id.lvLend);

        equipmentList = EquipmentList.sharedInstance();

        adapterItems = new ArrayAdapter<StudioItem>(this,android.R.layout.activity_list_item,android.R.id.text1,equipmentList) {

          @Override
          public View getView(int position, View convertView, ViewGroup parent) {
              View view = super.getView(position, convertView, parent);

              StudioItem studioItem = adapterItems.getItem(position);
              TextView text = view.findViewById(android.R.id.text1);
              ImageView icon = view.findViewById(android.R.id.icon);

              text.setText(studioItem.toString());
              icon.setImageResource(getResources().getIdentifier(studioItem.getType(),"drawable",getPackageName()));
              return view;
          }
        };
        lvLend.setAdapter(adapterItems);

        //when item is chosen from the manual list
        lvLend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chooseItem(adapterItems.getItem(position));
            }
        });

        animLoading.setAnimationOn(true);
        lvLend.setVisibility(View.GONE);
        if(toLend) {
            equipmentList.loadData(this, EquipmentList.TakenFilter.ONLY_NON_TAKEN, null);
        } else {
            equipmentList.loadData(this, EquipmentList.TakenFilter.ONLY_TAKEN, username);
        }

        searchItems.setOnQueryTextListener(this);
        searchItems.clearFocus();
    }

    @Override
    public void dataLoadChange(boolean isOk) {
        if(isOk) {
            animLoading.setAnimationOn(false);
            if(equipmentList.isEmpty()) {
                tvEmptyList.setVisibility(View.VISIBLE);
                lvLend.setVisibility(View.GONE);
            } else {
                adapterItems.notifyDataSetChanged();
                lvLend.setVisibility(View.VISIBLE);
                tvEmptyList.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void dataUploadDidComplete(boolean isOk) {
        if(isOk) {
            if(toLend)
                Toast.makeText(LendActivity.this,"השאלת את הפריט בהצלחה!",Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(LendActivity.this,"החזרת את הפריט בהצלחה!",Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(LendActivity.this, "שגיאה בביצוע הפעולה - בדוק את חיבורך לאינטרנט.",Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Handles the lending item process: showing a dialog to confirm choosing. If chose to confirm, updates the FireBase database in OnClick method.
     * @param item the item that has been chosen
     */
    private void chooseItem(final StudioItem item) {
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == DialogInterface.BUTTON_NEGATIVE)
                    dialog.cancel();
                else {
                    equipmentList.uploadUpdatedData(LendActivity.this, item, toLend, username);
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setIcon(getResources().getIdentifier(item.getType(),"drawable",getPackageName()))
                .setTitle("אישור")
                .setPositiveButton("כן", clickListener)
                .setNegativeButton("לא", clickListener);
        builder.setMessage( toLend ? "האם אתה רוצה להשאיל את "+item.toString() + "?" : "האם אתה רוצה להחזיר את "+item.toString() + "?");
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(newText.isEmpty())
            adapterItems.getFilter().filter(null);
        else
            adapterItems.getFilter().filter(newText);
        return true;
    }

    /**
     * Called when the barcode scan button is clicked.
     * @param v The barcode request button
     */
    public void onBarcodeRequest(View v) {
        Intent intent = new Intent(this, BarcodeScanActivity.class);
        startActivityForResult(intent, REQUEST_BARCODE_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @android.support.annotation.Nullable Intent data) {
        if(requestCode == REQUEST_BARCODE_CODE) {
            if(resultCode == BarcodeScanActivity.RESULT_FAILED_CAMERA_ACCESS) {
                AlertDialog alertDialog = new AlertDialog.Builder(this).setMessage("כשל בגישה למצלמה - ודא שאתה מאשר את גישת המצלמה.")
                        .setTitle("שגיאה!").create();
                alertDialog.show();
            } else if(resultCode == RESULT_OK) {
                String itemID = data.getStringExtra("barcodeValue");
                StudioItem item = equipmentList.searchForID(itemID);
                if(item != null) {
                    chooseItem(item);
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(this).setMessage("הברקוד אותו סרקת לא מתאים לרשימה שאתה מחפש. האם אתה בטוח שסרקת את האחד הנכון?")
                            .setTitle("שגיאה!").create();
                    alertDialog.show();
                }
            }
        }
    }

    //part of query text interface I must implement
    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }
}
