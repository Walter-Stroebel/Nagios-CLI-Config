/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.util.ArrayList;

public class HostGroup extends NoDepNagItem {

    public HostGroup(NagCliCfg owner) {
        super(owner, Types.hostgroup);
    }
    
    @Override
    public ArrayList<NagPointer> getChildren() {
        ArrayList<NagPointer> children = super.getChildren();
        children.addAll(members(Types.host));
        NagItem c = owner.get(Types.hostextinfo, getName());
        if (c != null) {
            children.add(new NagPointer(getNameFields()[0], c));
        }
        return children;
    }

}
