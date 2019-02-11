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
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

public class LendActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, DialogInterface.OnClickListener {

    private ListView lvLend; // the ListView instance
    private ArrayList<StudioItem> availableItems; // the array of items retrieved from the cloud
    private ArrayAdapter<StudioItem> adapterItems; // adapater for the list and ListView
    private SearchView searchItems; // the search bar
    private View animLoading; // the view used for animation
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
        searchItems = findViewById(R.id.searchItems);
        lvLend = findViewById(R.id.lvLend);

        availableItems = new ArrayList<>();
        adapterItems = new ArrayAdapter<StudioItem>(this,android.R.layout.activity_list_item,android.R.id.text1,availableItems) {

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
    }


    /**
     * set animation for first loading, then assign listener to the db
     */
    private void updateList() {
        RotateAnimation rotateAnimation = new RotateAnimation(0,360, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
        rotateAnimation.setDuration(4000);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        animLoading.startAnimation(rotateAnimation);

        animLoading.setVisibility(View.VISIBLE);
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
                availableItems.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    availableItems.add(doc.toObject(StudioItem.class).withId(doc.getId()));
                }
                if(animLoading.getVisibility() == View.VISIBLE) {
                    animLoading.clearAnimation();
                    animLoading.setVisibility(View.GONE);
                }
                if (availableItems.isEmpty()) {
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
     * handles the lending item process: dialog to confirm choosing, and updating the database in firestore
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
            else
                updateValues.put("owner", null);
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
                    Toast.makeText(LendActivity.this, "שגיאה בביצוע הפעולה (001)",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        }
    }

    /**
     * method for handling the barcode scan request
     * @param v the barcode request button
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
                    AlertDialog alertDialog = new AlertDialog.Builder(this).setMessage("הפריט שאת/ה מנסה להשאיל כבר נלקח - האם את/ה בטוח/ה שהוא זמין?")
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
        for(StudioItem si : availableItems) {
            if(si.getId().equals(id)) {
                return si;
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
