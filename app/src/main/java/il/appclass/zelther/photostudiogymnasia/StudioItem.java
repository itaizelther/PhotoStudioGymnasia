package il.appclass.zelther.photostudiogymnasia;

import com.google.firebase.firestore.ServerTimestamp;

import java.text.DateFormat;
import java.util.Date;

/**
 * This class represent an item from the studio's equipment.
 * @author Itai Zelther
 * @see LendActivity
 */
public class StudioItem {

    @ServerTimestamp private Date lastUsed;
    private String name, type, id, owner;
    private boolean taken;

    public StudioItem() {}

    public StudioItem(String name, String type, String id) {
        this.name = name;
        this.type = type;
        this.id = id;
        owner = null;
        taken = false;
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }


    /**
     * Returns the same instance with given id.
     * @param id The id that will be assigned to the item.
     * @return This item instance.
     */
    public StudioItem withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Returns a description for the teacher's screen of this specific item.
     * @return A string contains the description.
     */
    public String getTeachersData() {
        // The class is being initialize twice: once without the time and once with it. I don't know why this happens,
        // but making this if for preventing a call on null object fixes it.
        String date = null;
        if(lastUsed != null)
            date = DateFormat.getDateInstance().format(lastUsed);

        if(taken) {
            return "Taken by: " + owner + " in " + date + ".";
        } else {
            if(owner != null) {
                return "Retrieved to the studio by " + owner + " in " + date + ".";
            } else {
                return "In the studio.";
            }
        }
    }

    public boolean isTaken() {
        return taken;
    }

    public String getOwner() {
        return owner;
    }

    public Date getLastUsed() { return lastUsed; }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }


    public String getId() {
        return id;
    }
}
