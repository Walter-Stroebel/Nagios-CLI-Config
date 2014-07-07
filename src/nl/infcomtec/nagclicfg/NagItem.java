/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import org.json.JSONObject;

/**
 *
 * @author walter
 */
public class NagItem extends TreeMap<String, String> {

    public static final String SERVICE_DESCRIPTION = "service_description";
    public static final String HOST_NAME = "host_name";
    public static final String HOSTGROUP_NAME = "hostgroup_name";
    public static final String ALIAS = "alias";
    public static final String ADDRESS = "address";
    public static final String MAX_CHECK_ATTEMPTS = "max_check_attempts";
    public static final String CHECK_PERIOD = "check_period";
    public static final String CONTACTS = "contacts";
    /**
     * In a host
     */
    public static final String CONTACT_GROUPS = "contact_groups";
    /**
     * In a contact
     */
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
    public static final String NAME = "name";
    public static final String REGISTER = "register";
    public static final String USE = "use";

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
        pointers.add(new NagPointerDef(Types.host, Types.host, USE, 0));
        pointers.add(new NagPointerDef(Types.hostgroup, Types.host, MEMBERS, 1));
        pointers.add(new NagPointerDef(Types.hostgroup, Types.hostgroup, HOSTGROUP_MEMBERS, 1));
        pointers.add(new NagPointerDef(Types.hostgroup, Types.hostgroup, USE, 0));
        pointers.add(new NagPointerDef(Types.service, Types.host, HOST_NAME, 1));
        pointers.add(new NagPointerDef(Types.service, Types.hostgroup, HOSTGROUP_NAME, 1));
        pointers.add(new NagPointerDef(Types.service, Types.servicegroup, SERVICEGROUPS, 1));
        pointers.add(new NagPointerDef(Types.service, Types.command, CHECK_COMMAND, 0));
        pointers.add(new NagPointerDef(Types.service, Types.timeperiod, CHECK_PERIOD, 0));
        pointers.add(new NagPointerDef(Types.service, Types.command, EVENT_HANDLER, 0));
        pointers.add(new NagPointerDef(Types.service, Types.timeperiod, NOTIFICATION_PERIOD, 0));
        pointers.add(new NagPointerDef(Types.service, Types.contact, CONTACTS, 1));
        pointers.add(new NagPointerDef(Types.service, Types.contactgroup, CONTACT_GROUPS, 1));
        pointers.add(new NagPointerDef(Types.service, Types.service, USE, 0));
        pointers.add(new NagPointerDef(Types.servicegroup, Types.service, MEMBERS, 2));
        pointers.add(new NagPointerDef(Types.servicegroup, Types.servicegroup, SERVICEGROUP_MEMBERS, 1));
        pointers.add(new NagPointerDef(Types.servicegroup, Types.servicegroup, USE, 0));
        pointers.add(new NagPointerDef(Types.contact, Types.contactgroup, CONTACTGROUPS, 1));
        pointers.add(new NagPointerDef(Types.contact, Types.timeperiod, HOST_NOTIFICATION_PERIOD, 0));
        pointers.add(new NagPointerDef(Types.contact, Types.timeperiod, SERVICE_NOTIFICATION_PERIOD, 0));
        pointers.add(new NagPointerDef(Types.contact, Types.command, HOST_NOTIFICATION_COMMANDS, 1));
        pointers.add(new NagPointerDef(Types.contact, Types.command, SERVICE_NOTIFICATION_COMMANDS, 1));
        pointers.add(new NagPointerDef(Types.contact, Types.contact, USE, 0));
        pointers.add(new NagPointerDef(Types.contactgroup, Types.contact, MEMBERS, 1));
        pointers.add(new NagPointerDef(Types.contactgroup, Types.contactgroup, CONTACTGROUP_MEMBERS, 1));
        pointers.add(new NagPointerDef(Types.contactgroup, Types.contactgroup, USE, 0));
        pointers.add(new NagPointerDef(Types.command, Types.command, USE, 0));
        pointers.add(new NagPointerDef(Types.timeperiod, Types.timeperiod, USE, 0));
        pointers.add(new NagPointerDef(Types.hostextinfo, Types.host, HOST_NAME, 0));
        pointers.add(new NagPointerDef(Types.hostextinfo, Types.hostgroup, HOSTGROUP_NAME, 0));
        pointers.add(new NagPointerDef(Types.hostextinfo, Types.hostextinfo, USE, 0));
    }

    public static NagItem define(NagCliCfg owner, Types type) throws IOException {
        NagItem ret = new NagItem(owner, type);
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
    public final TreeMap<String, String> getAllFields() {
        TreeMap<String, String> ret = new TreeMap<>(this);
        ret.remove(USE);
        for (NagPointer c : getChildren(false)) {
            for (Entry<String, String> e : c.item.entrySet()) {
                if (!e.getKey().equals(REGISTER)) {
                    if (!ret.containsKey(e.getKey())) {
                        ret.put(e.getKey(), e.getValue());
                    }
                }
            }
        }
        return ret;
    }

    /**
     * @return the type
     */
    public Types getType() {
        return type;
    }

    @Override
    public String toString() {
        return "NagItem{" + "type=" + type + ",\n  fields=" + super.toString() + '}';
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
        return ret;
    }

    /**
     * Returns the field used to define the name of this object.
     *
     * @return Usually '(type)_name', or 'name' for generic objects or
     * 'service_description','host_name' for a named service.
     */
    public final String[] getNameFields() {
        switch (type) {
            case command:
                if (containsKey(COMMAND_NAME)) {
                    return new String[]{COMMAND_NAME};
                }
                break;
            case contact:
                if (containsKey(CONTACT_NAME)) {
                    return new String[]{CONTACT_NAME};
                }
                break;
            case contactgroup:
                if (containsKey(CONTACTGROUP_NAME)) {
                    return new String[]{CONTACTGROUP_NAME};
                }
                break;
            case host:
                if (containsKey(HOST_NAME)) {
                    return new String[]{HOST_NAME};
                }
                break;
            case hostextinfo:
                if (containsKey(HOST_NAME)) {
                    return new String[]{HOST_NAME};
                } else if (containsKey(HOSTGROUP_NAME)) {
                    return new String[]{HOSTGROUP_NAME};
                }
                break;
            case hostgroup:
                if (containsKey(HOSTGROUP_NAME)) {
                    return new String[]{HOSTGROUP_NAME};
                }
                break;
            case service:
                if (containsKey(HOST_NAME) && containsKey(SERVICE_DESCRIPTION)) {
                    return new String[]{HOST_NAME, SERVICE_DESCRIPTION};
                } else if (containsKey(HOSTGROUP_NAME)) {
                    if (containsKey(SERVICE_DESCRIPTION)) {
                        return new String[]{HOSTGROUP_NAME, SERVICE_DESCRIPTION};
                    }
                    return new String[]{HOSTGROUP_NAME};
                }
                break;
            case servicegroup:
                if (containsKey(SERVICEGROUP_NAME)) {
                    return new String[]{SERVICEGROUP_NAME};
                }
                break;
            case timeperiod:
                if (containsKey(TIMEPERIOD_NAME)) {
                    return new String[]{TIMEPERIOD_NAME};
                }
                break;
        }
        if (is(REGISTER, "0") && containsKey(NAME)) {
            return new String[]{NAME};
        } else {
            String uuid = UUID.randomUUID().toString();
            owner.em.err("Cannot find name for " + this + "; forcing name to: " + uuid);
            put(REGISTER, "0");
            put(NAME, uuid);
            return new String[]{NAME};
        }
    }

    public final void dump(Emitter em, boolean withReferals) {
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

    public final void dump(PrintWriter out, boolean withReferals) {
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
     * @param stride Step by this.
     * @return TreeSet, possibly empty.
     */
    public final TreeSet<String> fieldToSet(String fieldName, int stride) {
        TreeSet<String> ret = new TreeSet<>();
        String members = get(fieldName);
        if (members != null) {
            String[] mems = members.split(",");
            for (int i = 0; i < mems.length; i += stride) {
                StringBuilder mem = null;
                for (int j = 0; j < stride && (i + j) < mems.length; j++) {
                    if (mem == null) {
                        mem = new StringBuilder();
                    } else {
                        mem.append(",");
                    }
                    mem.append(mems[i + j]);
                }
                ret.add(mem.toString().trim());
            }
        }
        return ret;
    }

    /**
     * Get the children.
     *
     * @return List of children, possible empty.
     */
    public final ArrayList<NagPointer> getChildren(boolean check) {
        ArrayList<NagPointer> children = new ArrayList<>();
        for (NagPointerDef e : pointers) {
            if (e.from == type) {
                String val = get(e.byField);
                if (val != null) {
                    int bang = val.indexOf('!');
                    if (bang >= 0) {
                        val = val.substring(0, bang);
                    }
                    if (e.stride == 0) {
                        NagItem child = owner.get(e.to, val);
                        if (child != null) {
                            children.add(new NagPointer(e, child));
                        } else if (check) {
                            owner.em.err("/" + type + "/" + getName() + " refers to /" + e.to + "/" + val + " which does not exist.");
                        }
                    } else {
                        TreeSet<String> mems;
                        if (val.equals("*")) {
                            mems = new TreeSet<>();
                            TreeMap<String, NagItem> l = owner.nagDb.get(e.to);
                            if (l != null) {
                                for (NagItem e2 : l.values()) {
                                    mems.add(e2.getName());
                                }
                            }
                        } else {
                            mems = fieldToSet(e.byField, e.stride);
                        }
                        for (String mem : mems) {
                            NagItem c = owner.get(e.to, mem.trim());
                            if (c != null) {
                                children.add(new NagPointer(e, c));
                            }
                        }
                    }
                }
            }
        }
        return children;
    }

    /**
     * Remove a name, possibly from a list of names.
     *
     * @param fieldName Field with the list.
     * @param name Name to remove.
     * @return the updated field if it was a list or null (removed).
     */
    public final String remove(String fieldName, String name) {
        String asterisk = get(fieldName);
        if (asterisk == null) {
            return null;
        }
        if (asterisk.equals("*")) {
            return "*";
        }
        for (NagPointerDef e : pointers) {
            if (e.from == type && e.byField.equals(fieldName)) {
                if (e.stride == 0) {
                    remove(fieldName);
                } else {
                    TreeSet<String> mems = fieldToSet(e.byField, e.stride);
                    mems.remove(name);
                    if (mems.isEmpty()) {
                        remove(fieldName);
                    } else {
                        put(fieldName, setToField(mems));
                    }
                }
                return get(fieldName);
            }
        }
        // not a referencing field, just remove it.
        remove(fieldName);
        return get(fieldName);
    }

    public boolean is(String fieldName, String value) {
        if (containsKey(fieldName)) {
            return get(fieldName).equals(value);
        }
        return false;
    }

    public String append(String fieldName, String name) {
        String asterisk = get(fieldName);
        if (asterisk == null) {
            put(fieldName, name);
            return name;
        }
        if (asterisk.equals("*")) {
            return "*";
        }
        for (NagPointerDef e : pointers) {
            if (e.from == type && e.byField.equals(fieldName)) {
                if (e.stride == 0) {
                    put(fieldName, name);
                } else {
                    TreeSet<String> mems = fieldToSet(e.byField, e.stride);
                    mems.add(name);
                    put(fieldName, setToField(mems));
                }
                return get(fieldName);
            }
        }
        put(fieldName, name);
        return get(fieldName);
    }
}
