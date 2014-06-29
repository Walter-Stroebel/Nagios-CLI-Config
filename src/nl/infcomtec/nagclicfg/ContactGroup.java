/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.util.ArrayList;

public class ContactGroup extends NoDepNagItem {

    public ContactGroup(NagCliCfg owner) {
        super(owner, Types.contactgroup);
    }

    @Override
    public ArrayList<NagPointer> getChildren() {
        ArrayList<NagPointer> children = 
        super.getChildren();
        children.addAll(super.members(Types.contact));
        return children;
    }

}
