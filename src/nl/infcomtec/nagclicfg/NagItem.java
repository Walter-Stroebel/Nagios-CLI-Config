/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 *
 * @author walter
 */
public abstract class NagItem extends TreeMap<String, String> {

    protected final Types type;
    public final ArrayList<NagPointer> children = new ArrayList<>();
    protected final NagCliCfg owner;

    /**
     * Abstract base constructor.
     *
     * @param owner Owning object, needed for referrals.
     * @param type One of the known Nagios object types.
     */
    public NagItem(NagCliCfg owner, Types type) {
        this.owner = owner;
        this.type = type;
    }

    /**
     * Returns all the fields (long listing) by including fields from templates
     * and referrals.
     *
     * @return Fields in this object along with fields from 'use' templates.
     */
    public TreeMap<String, String> getAllFields() {
        return this;
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
     * All objects should be uniquely named, this method returns that name.
     *
     * @return Unique ID
     */
    public final String getName() {
        if (getNameField() != null) {
            return get(getNameField());
        }
        return "Object has no name!";
    }

    /**
     * Returns the field used to define the name of this object.
     *
     * @return Usually '(type)_name', or 'name' for generic objects or
     * 'service_description' for a named service.
     */
    public abstract String getNameField();

    /**
     * Must be implemented to read fields like parent, host_name in service and
     * members.
     */
    public abstract void collectChildren();

    /**
     * Construct the proper object.
     *
     * @param owner The Nagios data.
     * @param type The type to construct.
     * @return A generic or specialized NagItem.
     */
    public static NagItem construct(NagCliCfg owner, Types type) {
        switch (type) {
            default:
                return new NoDepNagItem(owner, type);
            case contactgroup:
                return new ContactGroup(owner);
            case host:
                return new Host(owner);
            case hostgroup:
                return new HostGroup(owner);
            case service:
                return new Service(owner);
            case servicegroup:
                return new ServiceGroup(owner);
        }
    }
}
