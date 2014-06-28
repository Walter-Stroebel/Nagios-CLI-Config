/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.util.TreeMap;

public class Service extends NoDepNagItem {

    @Override
    public String getNameField() {
        if (containsKey("service_description")) return "service_description";
        return super.getNameField();
    }

    @Override
    public void collectChildren() {
        super.collectChildren();
        String k = get("host_name");
        if (k != null && !k.isEmpty()) {
            NagItem c = owner.get(Types.host, k);
            children.add(new NagPointer("host_name", c));
        }
        k = get("use");
        if (k != null && !k.isEmpty()) {
            NagItem c = owner.get(Types.service, k);
            if (c == null) {
                System.out.println(getName() + " tries to use " + k + "; which does not exist");
            } else {
                children.add(new NagPointer("use", c));
            }
        }
    }

    public Service(NagCliCfg owner) {
        super(owner, Types.service);
    }

    @Override
    public TreeMap<String, String> getAllFields() {
        TreeMap<String, String> ret = new TreeMap<>(super.getAllFields());
        if (containsKey("use")){
            ret.putAll(owner.get(Types.service, get("use")));
        }
        return ret;
    }

}
