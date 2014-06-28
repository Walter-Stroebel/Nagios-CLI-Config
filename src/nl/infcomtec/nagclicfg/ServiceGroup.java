/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

public class ServiceGroup extends NoDepNagItem {

    public ServiceGroup(NagCliCfg owner) {
        super(owner, Types.servicegroup);
    }

    @Override
    public void collectChildren() {
        super.collectChildren();
        String members = get("members");
        if (members != null) {
            String[] mems = members.split(",");
            for (int i = 0; i < mems.length; i += 2) {
                {
                    NagItem c = owner.get(Types.host, mems[i]);
                    if (c != null) {
                        children.add(new NagPointer("members", c));
                    } else {
                        System.err.println("Host " + mems[i] + " not found");
                    }
                }
                {
                    NagItem c = owner.get(Types.service, mems[i + 1]);
                    if (c != null) {
                        children.add(new NagPointer("members", c));
                    } else {
                        System.err.println("Service " + mems[i + 1] + " not found");
                    }
                }
            }
        }
    }

}
