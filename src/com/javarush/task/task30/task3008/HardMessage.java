package com.javarush.task.task30.task3008;

import java.util.ArrayList;
import java.util.List;

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
