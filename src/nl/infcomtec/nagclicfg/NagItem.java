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
public abstract class NagItem extends TreeMap<String, String> {

    protected final Types type;
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

    /**
     * Service has a non-standard unique ID, basic function for all other
     * objects.
     *
     * @return Unique ID
     */
    public abstract String getName();

    /**
     * Must be implemented to read fields like parent, host_name in service and
     * members.
     */
    public abstract void collectChildren();

    /**
     * Construct the proper object.
     *
     * @param type The type to construct.
     * @return A generic or specialized NagItem.
     */
    public static NagItem construct(Types type) {
        switch (type) {
            default:
                return new NoDepNagItem(type);
            case contactgroup:
                return new ContactGroup();
            case host:
                return new Host();
            case hostgroup:
                return new HostGroup();
            case service:
                return new Service();
            case servicegroup:
                return new ServiceGroup();
        }
    }
}
