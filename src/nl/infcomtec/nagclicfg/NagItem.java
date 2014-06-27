/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 *
 * @author walter
 */
public class NagItem extends TreeMap<String, String> {

    private final Types type;
    public final ArrayList<NagPointer> children = new ArrayList<>();

    public NagItem(Types type) {
        this.type = type;
    }

    /**
     * @return the type
     */
    public Types getType() {
        return type;
    }

    @Override
    public String toString() {
        return "\nNagItem{" + "type=" + type + "\n" + super.toString() + "\nChildren: " + children + "\n}";
    }

    public String getName() {
        if (type == Types.service) {
            return get("service_description");
        } else {
            return get(type.toString()+"_name");
        }
    }
}
