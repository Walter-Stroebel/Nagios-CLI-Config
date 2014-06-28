/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.util.TreeMap;

public class Host extends NoDepNagItem {

    @Override
    public void collectChildren() {
        super.collectChildren();
        String k = get("parents");
        if (k != null && !k.isEmpty()) {
            String[] mems = k.split(",");
            for (String mem : mems) {
                NagItem c = owner.get(Types.host, mem);
                if (c == null) {
                    System.out.println(getName() + " refers to parent " + mem + "; which does not exist");
                } else {
                    children.add(new NagPointer("parents", c));
                }
            }
        }
        k = get("hostgroups");
        if (k != null && !k.isEmpty()) {
            String[] mems = k.split(",");
            for (String mem : mems) {
                NagItem c = owner.get(Types.hostgroup, mem);
                if (c == null) {
                    System.out.println(getName() + " refers to hostgroup " + mem + "; which does not exist");
                } else {
                    children.add(new NagPointer("hostgroups", c));
                }
            }
        }
        k = get("use");
        if (k != null && !k.isEmpty()) {
            NagItem c = owner.get(Types.host, k);
            if (c == null) {
                System.out.println(getName() + " tries to use " + k + "; which does not exist");
            } else {
                children.add(new NagPointer("use", c));
            }
        }
    }

    public Host(NagCliCfg owner) {
        super(owner, Types.host);
    }

    @Override
    public TreeMap<String, String> getAllFields() {
        TreeMap<String, String> ret = new TreeMap<>(super.getAllFields());
        if (containsKey("use")){
            ret.putAll(owner.get(Types.host, get("use")));
        }
        return ret;
    }

}
