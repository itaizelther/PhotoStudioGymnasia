package il.appclass.zelther.photostudiogymnasia;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

/**
 * The screen where the user can lend equipment or return them. The available items in each category will show up, updating it in real time.
 * @author Itai Zelther
 * @see MainActivity
 * @see BarcodeScanActivity
 */
public class LendActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, DialogInterface.OnClickListener {

    private ListView lvLend; // the ListView instance
    private ArrayAdapter<StudioItem> adapterItems; // adapater for the list and ListView
    private GraySquareLoadingView animLoading; // the view used for animation
    private StudioItem chosenItem; // the item the user currently chose
    private FirebaseFirestore db; //instance to get data from cloud
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

        adapterItems = new ArrayAdapter<StudioItem>(this,android.R.layout.activity_list_item,android.R.id.text1,new ArrayList<StudioItem>()) {

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

        //when item is choosen from the manual list
        lvLend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chooseItem(adapterItems.getItem(position));
            }
        });

        //get not taken list from the cloud
        db = FirebaseFirestore.getInstance();
        updateList();

        searchItems.setOnQueryTextListener(this);
        searchItems.clearFocus();
    }


    /**
     * Sets animation for first loading, then assign listener to the database in FireBase
     */
    private void updateList() {
        animLoading.setAnimationOn(true);
        lvLend.setVisibility(View.GONE);

        Query dbLink = db.collection("equipment").whereEqualTo("taken",!toLend);
        if(!toLend)
            dbLink = dbLink.whereEqualTo("owner",username);

       dbLink.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("LendList", "Listen failed");
                    return;
                }
                adapterItems.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    adapterItems.add(doc.toObject(StudioItem.class).withId(doc.getId()));
                }
                animLoading.setAnimationOn(false);
                if (adapterItems.isEmpty()) {
                    tvEmptyList.setVisibility(View.VISIBLE);
                    lvLend.setVisibility(View.GONE);
                } else {
                    adapterItems.notifyDataSetChanged();
                    lvLend.setVisibility(View.VISIBLE);
                    tvEmptyList.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Handles the lending item process: showing a dialog to confirm choosing. If chose to confirm, updates the FireBase database in OnClick method.
     * @param item the item that has been chosen
     */
    private void chooseItem(StudioItem item) {
        chosenItem = item;
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setIcon(getResources().getIdentifier(item.getType(),"drawable",getPackageName()))
                .setTitle("אישור")
                .setPositiveButton("כן", this)
                .setNegativeButton("לא", this);
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

    //after dialog click
    @Override
    public void onClick(final DialogInterface dialog, int which) {
        if(which == DialogInterface.BUTTON_NEGATIVE)
            dialog.cancel();
        else {
            HashMap<String, Object> updateValues = new HashMap<>();
            updateValues.put("taken",toLend);
            if(toLend)
                updateValues.put("owner", username);
            db.document("equipment/"+chosenItem.getId())
                    .update(updateValues)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    if(toLend)
                        Toast.makeText(LendActivity.this,"השאלת את הפריט בהצלחה!",Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(LendActivity.this,"החזרת את הפריט בהצלחה!",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LendActivity.this, "שגיאה בביצוע הפעולה - בדוק את חיבורך לאינטרנט.",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        }
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
                StudioItem item = getItemWithIDFromList(itemID);
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

    /**
     * Gets id of item, checks if it is in the available items list, and returns the desired item.
     * @param id Item's id
     * @return The item with the id given, or null if it does not exist in the list.
     */
    private StudioItem getItemWithIDFromList(String id) {
        for(int i=0; i<adapterItems.getCount(); i++) {
            if(adapterItems.getItem(i).getId().equals(id)) {
                return adapterItems.getItem(i);
            }
        }
        return null;
    }

    //part of query text interface I must implement
    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }
}
