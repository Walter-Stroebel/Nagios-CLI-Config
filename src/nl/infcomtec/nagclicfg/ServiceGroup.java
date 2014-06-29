/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.util.ArrayList;
import java.util.TreeSet;

public class ServiceGroup extends NoDepNagItem {

    public ServiceGroup(NagCliCfg owner) {
        super(owner, Types.servicegroup);
    }

    @Override
    public ArrayList<NagPointer> getChildren() {
        ArrayList<NagPointer> children = super.getChildren();
        children.addAll(getChildren("servicegroup_members",Types.servicegroup));
        String members = get("members");
        if (members != null) {
            String[] mems = members.split(",");
            for (int i = 0; i < mems.length; i += 2) {
                {
                    NagItem c = owner.get(Types.host, mems[i].trim());
                    if (c != null) {
                        children.add(new NagPointer("members", c));
                    }
                }
                {
                    NagItem c = owner.get(Types.service, mems[i + 1].trim());
                    if (c != null) {
                        children.add(new NagPointer("members", c));
                    }
                }
            }
        }
        return children;
    }

}
