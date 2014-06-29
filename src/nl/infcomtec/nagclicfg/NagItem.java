/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author walter
 */
public abstract class NagItem extends TreeMap<String, String> {

    protected final Types type;
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
        return "\nNagItem{" + "type=" + type + "\n" + super.toString() + "\nChildren: " + NagItem.this.getChildren() + "\n}";
    }

    /**
     * All objects should be uniquely named, this method returns that name.
     *
     * @return Unique ID
     */
    public final String getName() {
        String ret = null;
        for (String s : getNameFields()) {
            if (ret == null) {
                ret = get(s);
            } else {
                ret = ret + "," + get(s);
            }
        }
        return ret == null ? "Object has no name!" : ret;
    }

    /**
     * Returns the field used to define the name of this object.
     *
     * @return Usually '(type)_name', or 'name' for generic objects or
     * 'service_description','host_name' for a named service.
     */
    public abstract String[] getNameFields();

    /**
     * Must be implemented to read fields like parent, host_name in service and
     * getChildren.
     */
    public abstract ArrayList<NagPointer> getChildren();

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
            case hostextinfo:
                return new HostExtInfo(owner);
            case hostgroup:
                return new HostGroup(owner);
            case service:
                return new Service(owner);
            case servicegroup:
                return new ServiceGroup(owner);
        }
    }

    public void dump(PrintWriter out, boolean withReferals) {
        out.println("define " + getType().toString() + " {");
        if (withReferals) {
            for (Map.Entry<String, String> e2 : getAllFields().entrySet()) {
                out.println("\t" + e2.getKey() + "\t" + e2.getValue());
            }
        } else {
            for (Map.Entry<String, String> e2 : entrySet()) {
                out.println("\t" + e2.getKey() + "\t" + e2.getValue());
            }
        }
        out.println("}");
    }

    public void dump(OutputStream out, boolean withReferals) {
        PrintWriter pw = new PrintWriter(out);
        dump(pw, withReferals);
        pw.flush();
    }

    /**
     * Convert a field of form value,value,...,value to a TreeSet.
     *
     * @param fieldName Field to fetch.
     * @return TreeSet, possibly empty.
     */
    public TreeSet<String> fieldToSet(String fieldName) {
        TreeSet<String> ret = new TreeSet<>();
        String members = get(fieldName);
        if (members != null) {
            String[] mems = members.split(",");
            for (String mem : mems) {
                ret.add(mem.trim());
            }
        }
        return ret;
    }

    /**
     * Convert a set to a field of form value,value,...,value.
     *
     * @param set To convert.
     * @return Field value of form value,value,...,value.
     */
    public String setToField(TreeSet<String> set) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (String s : set) {
            sb.append(sep).append(s);
            sep = ",";
        }
        return sb.toString();
    }

    /**
     * Get the children from a field of form value,value,...,value.
     *
     * @param fieldName Field to fetch.
     * @param memType Type of child this points to.
     * @return List of children, possible empty.
     */
    public ArrayList<NagPointer> getChildren(String fieldName, Types memType) {
        ArrayList<NagPointer> children = new ArrayList<>();
        String asterisk = get(fieldName);
        if (asterisk == null) {
            return children;
        }
        TreeSet<String> mems;
        if (asterisk.trim().equals("*")) {
            mems = new TreeSet<>();
            TreeMap<String,NagItem> l = owner.nagDb.get(memType);
            if (l != null) {
                for (NagItem e : l.values()) {
                    mems.add(e.getName());
                }
            }
        } else {
            mems = fieldToSet(fieldName);
        }
        for (String mem : mems) {
            NagItem c = owner.get(memType, mem.trim());
            if (c != null) {
                children.add(new NagPointer(fieldName, c));
            }
        }
        return children;
    }

    /**
     * Add a child pointed to by a single-value field, eg 'use'. Does nothing if
     * the field does not exist or the referred item does not exist.
     *
     * @param children Child collection to add to.
     * @param fieldName Name of the field.
     * @param memType Type of the item it is pointing to.
     */
    public void addChild(ArrayList<NagPointer> children, String fieldName, Types memType) {
        String key = get(fieldName);
        if (key != null) {
            NagItem c = owner.get(memType, key.trim());
            if (c != null) {
                children.add(new NagPointer(fieldName, c));
            }
        }
    }

    /**
     * Get the children from the 'getChildren' field of form
     * value,value,...,value.
     *
     * @param memType Type of child this points to.
     * @return List of children, possible empty.
     */
    public ArrayList<NagPointer> members(Types memType) {
        return getChildren("members", memType);
    }
}
