/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.util.ArrayList;
import java.util.TreeMap;

public class Service extends NoDepNagItem {

    @Override
    public String[] getNameFields() {
        if (containsKey(SERVICE_DESCRIPTION) && containsKey(NagItem.HOST_NAME)) {
            // as used as key by service_groups
            return new String[]{NagItem.HOST_NAME, SERVICE_DESCRIPTION};
        }
        if (containsKey(NagItem.HOSTGROUP_NAME)) {
            if (containsKey(SERVICE_DESCRIPTION)) {
                return new String[]{NagItem.HOSTGROUP_NAME, SERVICE_DESCRIPTION};
            }
            return new String[]{NagItem.HOSTGROUP_NAME};
        }
        return super.getNameFields();
    }

    @Override
    public ArrayList<NagPointer> getChildren() {
        ArrayList<NagPointer> children = super.getChildren();
        children.addAll(getChildren("hostgroups", Types.hostgroup));
        children.addAll(getChildren("contact_groups", Types.contactgroup));
        addChild(children, NagItem.HOST_NAME, Types.host);
        addChild(children, NagItem.HOSTGROUP_NAME, Types.hostgroup);
        addChild(children, "use", Types.service);
        return children;
    }

    public Service(NagCliCfg owner) {
        super(owner, Types.service);
    }

    @Override
    public TreeMap<String, String> getAllFields() {
        TreeMap<String, String> ret = new TreeMap<>(super.getAllFields());
        if (containsKey("use")) {
            ret.putAll(owner.get(Types.service, get("use")));
        }
        return ret;
    }

}
