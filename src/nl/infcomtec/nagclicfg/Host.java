/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.util.ArrayList;
import java.util.TreeMap;

public class Host extends NoDepNagItem {

    @Override
    public ArrayList<NagPointer> getChildren() {
        ArrayList<NagPointer> children = super.getChildren();
        children.addAll(getChildren("parents", Types.host));
        children.addAll(getChildren("hostgroups", Types.hostgroup));
        addChild(children, "use", Types.host);
        NagItem c = owner.get(Types.hostextinfo, getName());
        if (c != null) {
            children.add(new NagPointer(getNameFields()[0], c));
        }
        return children;
    }

    public Host(NagCliCfg owner) {
        super(owner, Types.host);
    }

    @Override
    public TreeMap<String, String> getAllFields() {
        TreeMap<String, String> ret = new TreeMap<>(super.getAllFields());
        if (containsKey("use")) {
            ret.putAll(owner.get(Types.host, get("use")));
        }
        return ret;
    }

}
