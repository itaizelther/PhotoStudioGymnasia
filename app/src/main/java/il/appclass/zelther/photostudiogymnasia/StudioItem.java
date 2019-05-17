package il.appclass.zelther.photostudiogymnasia;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Calendar;
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
        if(lastUsed != null){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(lastUsed);
            date = calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH)+1) + "/" + calendar.get(Calendar.YEAR);
        }

        if(taken) {
            return "נשאל על ידי " + owner + " ב-" + date + ".";
        } else {
            if(owner != null) {
                return "הוחזר לסטודיו על ידי " + owner + " ב-" + date + ".";
            } else {
                return "נמצא כעת בסטודיו.";
            }
        }
    }

    /**
     * @return true if the item is taken by someone. false if the item is in the studio.
     */
    public boolean isTaken() {
        return taken;
    }

    /**
     * @return the name of the current holder of the item or the previous one if the item is currently unused.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @return date object of the last date the item has been used. it can be when the item has been lent last or returned to the studio.
     */
    public Date getLastUsed() { return lastUsed; }

    /**
     * @return the item's name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the item's type (category).
     */
    public String getType() {
        return type;
    }


    /**
     * @return the item's ID.
     */
    public String getId() {
        return id;
    }
}
