/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.json.JSONObject;

/**
 *
 * @author walter
 */
public abstract class NagItem extends TreeMap<String, String> {

    public static final String SERVICE_DESCRIPTION = "service_description";
    public static final String HOST_NAME = "host_name";
    public static final String HOSTGROUP_NAME = "hostgroup_name";
    public static final String ALIAS = "alias";
    public static final String ADDRESS = "address";
    public static final String MAX_CHECK_ATTEMPTS = "max_check_attempts";
    public static final String CHECK_PERIOD = "check_period";
    public static final String CONTACTS = "contacts";
    /** In a host */
    public static final String CONTACT_GROUPS = "contact_groups";
    /** In a contact */
    public static final String CONTACTGROUPS = "contactgroups";
    public static final String NOTIFICATION_INTERVAL = "notification_interval";
    public static final String NOTIFICATION_PERIOD = "notification_period";
    public static final String CHECK_COMMAND = "check_command";
    public static final String RETRY_INTERVAL = "retry_interval";
    public static final String CHECK_INTERVAL = "check_interval";
    public static final String SERVICEGROUP_NAME = "servicegroup_name";
    public static final String CONTACTGROUP_NAME = "contactgroup_name";
    public static final String CONTACT_NAME = "contact_name";
    public static final String HOST_NOTIFICATIONS_ENABLED = "host_notifications_enabled";
    public static final String SERVICE_NOTIFICATIONS_ENABLED = "service_notifications_enabled";
    public static final String HOST_NOTIFICATION_PERIOD = "host_notification_period";
    public static final String SERVICE_NOTIFICATION_PERIOD = "service_notification_period";
    public static final String HOST_NOTIFICATION_OPTIONS = "host_notification_options";
    public static final String SERVICE_NOTIFICATION_OPTIONS = "service_notification_options";
    public static final String HOST_NOTIFICATION_COMMANDS = "host_notification_commands";
    public static final String SERVICE_NOTIFICATION_COMMANDS = "service_notification_commands";
    public static final String TIMEPERIOD_NAME = "timeperiod_name";
    public static final String COMMAND_NAME = "command_name";
    public static final String COMMAND_LINE = "command_line";
    public static final TreeMap<Types, String[]> objectDefs = new TreeMap<>();
    public static final TreeSet<NagPointerDef> pointers = new TreeSet<>();
    public static final String PARENTS = "parents";
    public static final String HOSTGROUPS = "hostgroups";
    public static final String EVENT_HANDLER = "event_handler";
    public static final String MEMBERS = "members";
    public static final String HOSTGROUP_MEMBERS = "hostgroup_members";
    public static final String SERVICEGROUPS = "servicegroups";
    public static final String SERVICEGROUP_MEMBERS = "servicegroup_members";
    public static final String CONTACTGROUP_MEMBERS = "contactgroup_members";

    static {
        objectDefs.put(Types.host, new String[]{HOST_NAME, ALIAS, ADDRESS, MAX_CHECK_ATTEMPTS, CHECK_PERIOD, CONTACTS, CONTACT_GROUPS, NOTIFICATION_INTERVAL, NOTIFICATION_PERIOD});
        objectDefs.put(Types.hostgroup, new String[]{HOSTGROUP_NAME, ALIAS});
        objectDefs.put(Types.service, new String[]{HOST_NAME, SERVICE_DESCRIPTION, CHECK_COMMAND, MAX_CHECK_ATTEMPTS, CHECK_INTERVAL, RETRY_INTERVAL, CHECK_PERIOD, NOTIFICATION_INTERVAL, NOTIFICATION_PERIOD, CONTACTS, CONTACT_GROUPS});
        objectDefs.put(Types.servicegroup, new String[]{SERVICEGROUP_NAME, ALIAS});
        objectDefs.put(Types.contact, new String[]{CONTACT_NAME, HOST_NOTIFICATIONS_ENABLED, SERVICE_NOTIFICATIONS_ENABLED, HOST_NOTIFICATION_PERIOD, SERVICE_NOTIFICATION_PERIOD, HOST_NOTIFICATION_OPTIONS, SERVICE_NOTIFICATION_OPTIONS, HOST_NOTIFICATION_COMMANDS, SERVICE_NOTIFICATION_COMMANDS});
        objectDefs.put(Types.contactgroup, new String[]{CONTACTGROUP_NAME, ALIAS});
        objectDefs.put(Types.timeperiod, new String[]{TIMEPERIOD_NAME, ALIAS});
        objectDefs.put(Types.command, new String[]{COMMAND_NAME, COMMAND_LINE});
        objectDefs.put(Types.hostextinfo, new String[]{});
        pointers.add(new NagPointerDef(Types.host, Types.host, PARENTS, 1));
        pointers.add(new NagPointerDef(Types.host, Types.hostgroup, HOSTGROUPS, 1));
        pointers.add(new NagPointerDef(Types.host, Types.command, CHECK_COMMAND, 0));
        pointers.add(new NagPointerDef(Types.host, Types.command, EVENT_HANDLER, 0));
        pointers.add(new NagPointerDef(Types.host, Types.timeperiod, CHECK_PERIOD, 0));
        pointers.add(new NagPointerDef(Types.host, Types.contact, CONTACTS, 1));
        pointers.add(new NagPointerDef(Types.host, Types.contactgroup, CONTACT_GROUPS, 1));
        pointers.add(new NagPointerDef(Types.host, Types.timeperiod, NOTIFICATION_PERIOD, 0));
        pointers.add(new NagPointerDef(Types.hostgroup, Types.host, MEMBERS, 1));
        pointers.add(new NagPointerDef(Types.hostgroup, Types.hostgroup, HOSTGROUP_MEMBERS, 1));
        pointers.add(new NagPointerDef(Types.service, Types.host, HOST_NAME, 0));
        pointers.add(new NagPointerDef(Types.service, Types.hostgroup, HOSTGROUP_NAME, 0));
        pointers.add(new NagPointerDef(Types.service, Types.servicegroup, SERVICEGROUPS, 1));
        pointers.add(new NagPointerDef(Types.service, Types.command, CHECK_COMMAND, 0));
        pointers.add(new NagPointerDef(Types.service, Types.timeperiod, CHECK_PERIOD, 0));
        pointers.add(new NagPointerDef(Types.service, Types.command, EVENT_HANDLER, 0));
        pointers.add(new NagPointerDef(Types.service, Types.timeperiod, NOTIFICATION_PERIOD, 0));
        pointers.add(new NagPointerDef(Types.service, Types.contact, CONTACTS, 1));
        pointers.add(new NagPointerDef(Types.service, Types.contactgroup, CONTACT_GROUPS, 1));
        pointers.add(new NagPointerDef(Types.servicegroup, Types.service, MEMBERS, 2));
        pointers.add(new NagPointerDef(Types.servicegroup, Types.servicegroup, SERVICEGROUP_MEMBERS, 1));
        pointers.add(new NagPointerDef(Types.contact, Types.contactgroup, CONTACTGROUPS, 1));
        pointers.add(new NagPointerDef(Types.contact, Types.timeperiod, HOST_NOTIFICATION_PERIOD, 0));
        pointers.add(new NagPointerDef(Types.contact, Types.timeperiod, SERVICE_NOTIFICATION_PERIOD, 0));
        pointers.add(new NagPointerDef(Types.contact, Types.command, HOST_NOTIFICATION_COMMANDS, 1));
        pointers.add(new NagPointerDef(Types.contact, Types.command, SERVICE_NOTIFICATION_COMMANDS, 1));
        pointers.add(new NagPointerDef(Types.contactgroup, Types.contact, MEMBERS, 1));
        pointers.add(new NagPointerDef(Types.contactgroup, Types.contactgroup, CONTACTGROUP_MEMBERS, 1));
        pointers.add(new NagPointerDef(Types.hostextinfo, Types.host, HOST_NAME, 0));
        pointers.add(new NagPointerDef(Types.hostextinfo, Types.hostgroup, HOSTGROUP_NAME, 0));
    }

    public static NagItem define(NagCliCfg owner, Types type) throws IOException {
        NagItem ret = construct(owner, type);
        for (String key : objectDefs.get(type)) {
            String val = owner.readLine("Enter " + key + ": ", false);
            if (val.isEmpty()) {
                return null;
            }
            ret.put(key, val);
        }
        return ret;
    }

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

    /**
     * Convert a set to a field of form value,value,...,value.
     *
     * @param set To convert.
     * @return Field value of form value,value,...,value.
     */
    public static String setToField(TreeSet<String> set) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (String s : set) {
            sb.append(sep).append(s);
            sep = ",";
        }
        return sb.toString();
    }
    protected final Types type;
    protected final NagCliCfg owner;

    /**
     * Abstract base constructor.
     *
     * @param owner Owning object, needed for referrals.
     * @param type One of the known Nagios object types.
     */
    protected NagItem(NagCliCfg owner, Types type) {
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

    public void dump(Emitter em, boolean withReferals) {
        if (em.json) {
            em.jsonOut.put(new JSONObject(withReferals ? getAllFields() : this));
        } else {
            em.println("define " + getType().toString() + " {");
            if (withReferals) {
                for (Map.Entry<String, String> e2 : getAllFields().entrySet()) {
                    em.println("\t" + e2.getKey() + "\t" + e2.getValue());
                }
            } else {
                for (Map.Entry<String, String> e2 : entrySet()) {
                    em.println("\t" + e2.getKey() + "\t" + e2.getValue());
                }
            }
            em.println("}");
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
            TreeMap<String, NagItem> l = owner.nagDb.get(memType);
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

    /**
     * Remove a name from list of names.
     *
     * @param fieldName Field with the list.
     * @param name Name to remove.
     */
    public String removeFromList(String fieldName, String name) {
        String asterisk = get(fieldName);
        if (asterisk == null) {
            return "";
        }
        if (asterisk.equals("*")) {
            return "*";
        }
        TreeSet<String> set = fieldToSet(asterisk);
        set.remove(name);
        put(fieldName, setToField(set));
        return get(fieldName);
    }

    /**
     * Remove the indicated referral.
     *
     * @param ptr Referral to remove.
     */
    public final void removeChild(NagPointer ptr) {
        if (ptr.key.equals("parents")) {
            String left = removeFromList(ptr.key, ptr.item.getName());
            if (left.isEmpty()) {
                remove(ptr.key);
            }
        }
    }
}
