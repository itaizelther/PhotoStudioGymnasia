package il.appclass.zelther.photostudiogymnasia;

/**
 * This class represent an item from the studio's equipment.
 * @author Itai Zelther
 * @see LendActivity
 */
public class StudioItem {

    private String name, type, id, owner;
    private boolean taken;

    public StudioItem() {}

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }

    public String getId() {
        return id;
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

    public boolean isTaken() {
        return taken;
    }

    public String getOwner() {
        return owner;
    }
}
