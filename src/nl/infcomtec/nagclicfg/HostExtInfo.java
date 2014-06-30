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
    public String[] getNameFields() {
        if (containsKey(HOST_NAME)) {
            return new String[]{HOST_NAME};
        }
        return new String[]{HOSTGROUP_NAME};
    }

    @Override
    public ArrayList<NagPointer> getChildren() {
        ArrayList<NagPointer> children = super.getChildren();
        children.addAll(getChildren(HOST_NAME, Types.host));
        children.addAll(getChildren(HOSTGROUP_NAME, Types.hostgroup));
        return children;
    }

}
