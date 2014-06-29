/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */

package nl.infcomtec.nagclicfg;

import java.util.ArrayList;


public class HostExtInfo extends NoDepNagItem {

    public HostExtInfo(NagCliCfg owner) {
        super(owner, Types.hostextinfo);
    }

    @Override
    public String getNameField() {
        if (containsKey("host_name"))
            return "host_name";
        return "hostgroup_name";
    }
    @Override
    public ArrayList<NagPointer> getChildren() {
        ArrayList<NagPointer> children = super.getChildren();
        children.addAll(getChildren("host_name", Types.host));
        children.addAll(getChildren("hostgroups_name",Types.hostgroup));
        return children;
    }
    
}
