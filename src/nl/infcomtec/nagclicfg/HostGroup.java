/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

public class HostGroup extends NoDepNagItem {

    public HostGroup(NagCliCfg owner) {
        super(owner, Types.hostgroup);
    }

    @Override
    public void collectChildren() {
        super.collectChildren();
        String members = get("members");
        if (members != null) {
            String[] mems = members.split(",");
            for (String mem : mems) {
                NagItem c = owner.get(Types.host, mem);
                if (c != null) {
                    children.add(new NagPointer("members", c));
                }
            }
        }
        {
            NagItem c = owner.get(Types.hostextinfo, getName());
            if (c != null) {
                children.add(new NagPointer(getNameField(), c));
            }
        }
    }

}
