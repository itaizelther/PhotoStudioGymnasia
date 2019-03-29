package il.appclass.zelther.photostudiogymnasia;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;

public class EquipmentList extends ArrayList<StudioItem> implements EventListener<QuerySnapshot> {

    private static EquipmentList equipmentList;
    private DataLoaderListener dataLoaderListener;
    private final FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    private EquipmentList() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Returns the sharedInstance of the class, if called.
     * @return reference for the singleton object of this class
     */
    public static EquipmentList sharedInstance() {
        if(equipmentList == null)
            equipmentList = new EquipmentList();
        return equipmentList;
    }


    /**
     * Load new data to this array from the Firestore cloud, and assigning a real time listener for changes.
     * @param dll Listener for real time changes in the list
     * @param tk whether to take only taken items or only non taken items, or both
     * @param orderBy name of field to order the list by
     * @param username filter data by username. if null is given, will not filter.
     */
    public void loadData(DataLoaderListener dll, TakenFilter tk, String orderBy, String username) {
        if(listenerRegistration != null)
            listenerRegistration.remove();

        dataLoaderListener = dll;
        Query query = db.collection("equipment");

        if(orderBy != null)
            query = query.orderBy(orderBy);

        switch (tk) {
            case ONLY_TAKEN:
                query = query.whereEqualTo("taken", true);
                break;
            case ONLY_NON_TAKEN:
                query = query.whereEqualTo("taken", false);
                break;
            default:
                break;
        }
        if(username != null)
            query = query.whereEqualTo("owner", username);
        listenerRegistration = query.addSnapshotListener(this);
    }

    @Override
    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
        if(e != null) {
            dataLoaderListener.dataLoadChange(false);
            return;
        }
        super.clear();
        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
            super.add(doc.toObject(StudioItem.class).withId(doc.getId()));
        }
        dataLoaderListener.dataLoadChange(true);
    }

    /**
     * Updating an item info on the cloud, and assigning a listener for completion.
     * @param dul The listener which will be called on completion.
     * @param studioItem The item which needs to be updated.
     * @param taken Value which will be assigned in cloud.
     * @param username Value which will be assigned in cloud.
     */
    public void uploadUpdatedData(final DataUploadListener dul, final StudioItem studioItem, boolean taken, String username) {
        HashMap<String, Object> updateValues = new HashMap<>();
        updateValues.put("taken",taken);
        updateValues.put("lastUsed", FieldValue.serverTimestamp());
        if(username != null)
            updateValues.put("owner", username);
        db.collection("equipment").document(studioItem.getId()).update(updateValues).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dul.dataUploadDidComplete(task.isSuccessful(), studioItem);
            }
        });
    }

    /**
     * Adding a new item to the list and the cloud.
     * @param dul The listener which will be called on completion.
     * @param studioItem The item object which will be uploaded to the cloud.
     */
    public void uploadNewItem(final DataUploadListener dul, final StudioItem studioItem) {
        db.collection("equipment").document(studioItem.getId()).set(studioItem).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                dul.dataUploadDidComplete(task.isSuccessful(), studioItem);
            }
        });
    }

    public void deleteItem(final DataDeleteListener ddl, StudioItem studioItem) {
        db.collection("equipment").document(studioItem.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                ddl.dataDeleteDidComplete(task.isSuccessful());
            }
        });
    }

    /**
     * Gets id of item, checks if it is in the available items list, and returns the desired item.
     * @param id Item's id
     * @return The item with the id given, or null if it does not exist in the list.
     */
    public StudioItem searchForID(String id) {
        for(int i=0; i<super.size(); i++) {
            if(super.get(i).getId().equals(id)) {
                return super.get(i);
            }
        }
        return null;
    }

    /**
     * Listener for data loading. If a class asked for a new list with new filter, this listener will be called on each real time change occurring.
     * @author Itai Zelther
     * @see EquipmentList
     * @see DataUploadListener
     * @see DataDeleteListener
     */
    public interface DataLoaderListener {
        /**
         * Called each time a changed has been made in the Firestore cloud.
         * @param isOk whether the load has been successful or not.
         */
        void dataLoadChange(boolean isOk);
    }

    /**
     * Listener for data updating. called one time, when a class is pending update on this list this listener will be called when the update is complete.
     * @author Itai Zelther
     * @see EquipmentList
     * @see DataLoaderListener
     * @see DataDeleteListener
     */
    public interface DataUploadListener {
        /**
         * Called when an update has been pending and now it is completed.
         * @param isOk whether the update has been successful or not.
         * @param item The item which has been uploaded or updated in the cloud. The item have the same settings as given, and not the new ones.
         */
        void dataUploadDidComplete(boolean isOk, StudioItem item);
    }

    /**
     * Listener for data deleting. called one time, when a class is pending a delete request, this listener will be called the the deletion is complete.
     * @author Itai Zelther
     * @see EquipmentList
     * @see DataLoaderListener
     * @see DataUploadListener
     */
    public interface DataDeleteListener {
        /**
         * Called when a class asked to delete an item for the list, and the process on the cloud is done.
         * @param isOk whether the deletion has been successful or not.
         */
        void dataDeleteDidComplete(boolean isOk);
    }

    public enum TakenFilter {
        ONLY_TAKEN, ONLY_NON_TAKEN, BOTH
    }
}
