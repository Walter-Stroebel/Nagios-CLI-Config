/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.util.ArrayList;
import java.util.TreeSet;

public class ServiceGroup extends NoDepNagItem {

    public static class HostAndService implements Comparable<HostAndService> {

        public final String host;
        public final String service;

        public HostAndService(String host, String service) {
            this.host = host;
            this.service = service;
        }

        @Override
        public int compareTo(HostAndService o) {
            int c = host.compareTo(o.host);
            if (c != 0) {
                return c;
            }
            return service.compareTo(o.service);
        }
    }

    public ServiceGroup(NagCliCfg owner) {
        super(owner, Types.servicegroup);
    }

    public TreeSet<HostAndService> members() {
        TreeSet<HostAndService> ret = new TreeSet<>();
        String members = get("members");
        if (members != null) {
            String[] mems = members.split(",");
            for (int i = 0; i < mems.length; i += 2) {
                ret.add(new HostAndService(mems[i].trim(), mems[i + 1].trim()));
            }
        }
        return ret;
    }

    public String membersToString(TreeSet<HostAndService> set) {
        if (set.isEmpty()) {
            return "";
        }
        HostAndService e = set.first();
        set.remove(e);
        StringBuilder ret = new StringBuilder(e.host + "," + e.service);
        for (HostAndService e2 : set) {
            ret.append(",").append(e2.host).append(",").append(e2.service);
        }
        set.add(e);
        return ret.toString();
    }

    @Override
    public ArrayList<NagPointer> getChildren() {
        ArrayList<NagPointer> children = super.getChildren();
        children.addAll(getChildren("servicegroup_members", Types.servicegroup));
        TreeSet<HostAndService> mems = members();
        for (HostAndService hs : mems) {
            {
                NagItem c = owner.get(Types.host, hs.host);
                if (c != null) {
                    children.add(new NagPointer("members", c));
                }
            }
            {
                NagItem c = owner.get(Types.service, hs.service);
                if (c != null) {
                    children.add(new NagPointer("members", c));
                }
            }
        }
        return children;
    }

}
