/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

public class Service extends NoDepNagItem {

    @Override
    public String getNameField() {
        if (containsKey("service_description")) {
            return "service_description";
        }
        return super.getNameField();
    }

    @Override
    public ArrayList<NagPointer> getChildren() {
        ArrayList<NagPointer> children = super.getChildren();
        children.addAll(getChildren("hostgroups", Types.hostgroup));
        addChild(children, "host_name", Types.host);
        addChild(children, "use", Types.service);
        NagItem c = owner.get(Types.hostextinfo, getName());
        if (c != null) {
            children.add(new NagPointer(getNameField(), c));
        }
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
