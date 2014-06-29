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
        if (containsKey("service_description")&&containsKey("host_name")) {
            // as used as key by service_groups
            return new String[]{"host_name","service_description"};
        }
        if (containsKey("hostgroup_name")){
            return new String[]{"hostgroup_name"};
        }
        return super.getNameFields();
    }

    @Override
    public ArrayList<NagPointer> getChildren() {
        ArrayList<NagPointer> children = super.getChildren();
        children.addAll(getChildren("hostgroups", Types.hostgroup));
        children.addAll(getChildren("contact_groups", Types.contactgroup));
        addChild(children, "host_name", Types.host);
        addChild(children, "hostgroup_name", Types.hostgroup);
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
