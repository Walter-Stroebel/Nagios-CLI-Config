/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */

package nl.infcomtec.nagclicfg;


public class HostExtInfo extends NoDepNagItem {

    public HostExtInfo(NagCliCfg owner) {
        super(owner, Types.hostextinfo);
    }

    @Override
    public String getNameField() {
        return "hostgroup_name";
    }

    @Override
    public void collectChildren() {
        super.collectChildren();
        String k = get("hostgroup_name");
        if (k != null && !k.isEmpty()) {
            NagItem c = owner.get(Types.hostgroup, k);
            children.add(new NagPointer("hostgroup_name", c));
        }
    }
    
}
