/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

public class ContactGroup extends NoDepNagItem {

    public ContactGroup() {
        super(Types.contactgroup);
    }

    @Override
    public void collectChildren() {
        super.collectChildren();
        String members = get("members");
        if (members != null) {
            String[] mems = members.split(",");
            for (String mem : mems) {
                NagItem c = NagCliCfg.get(Types.contact, mem);
                if (c != null) {
                    children.add(new NagPointer("members", c));
                }
            }
        }
    }

}
