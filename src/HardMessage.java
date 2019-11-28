public class HardMessage extends Message {
    String[] stuff;

    public HardMessage() {
        super();
        this.stuff = null;
    }

    public String[] getStuff() {
        return stuff;
    }

    public void setStuff(String[] stuff) {
        this.stuff = stuff;
    }
}
