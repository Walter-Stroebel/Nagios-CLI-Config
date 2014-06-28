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
                    if (getNameField().equals("host_name")) {
                        TreeSet<String> members = new TreeSet<>();
                        members.add(getName());
                        if (c.get("members").contains("*")) {
                            c.put("members", "*"); // all is all
                            for (Iterator<NagPointer> it = c.children.iterator(); it.hasNext();) {
                                NagPointer e = it.next();
                                if (e.key.equals("members")) {
                                    it.remove();
                                }
                            }
                            ArrayList<NagItem> allh = owner.nagDb.get(Types.host);
                            Collections.sort(allh, new Comparator<NagItem>() {

                                @Override
                                public int compare(NagItem o1, NagItem o2) {
                                    return o1.getName().compareTo(o2.getName());
                                }
                            });
                            for (NagItem hi : allh) {
                                c.children.add(new NagPointer("members", hi));
                            }
                        } else {
                            String[] hgmems = c.get("members").split(",");
                            members.addAll(Arrays.asList(hgmems));
                            String sep = "";
                            StringBuilder ms = new StringBuilder();
                            for (Iterator<NagPointer> it = c.children.iterator(); it.hasNext();) {
                                NagPointer e = it.next();
                                if (e.key.equals("members")) {
                                    it.remove();
                                }
                            }
                            for (String hgm : members) {
                                ms.append(sep).append(hgm);
                                NagItem freshChild = owner.get(Types.host, hgm);
                                if (freshChild == null) {
                                    System.err.println("??? Why can't I find a host for " + hgm);
                                } else {
                                    c.children.add(new NagPointer("members", freshChild));
                                }
                                sep = ",";
                            }
                            c.put("members", ms.toString());
                        }
                    }
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
        if (containsKey("use")) {
            ret.putAll(owner.get(Types.host, get("use")));
        }
        return ret;
    }

}
