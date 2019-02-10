package il.appclass.zelther.photostudiogymnasia;

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
