/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

public class Service extends NoDepNagItem {

    @Override
    public String getName() {
        return get("service_description");
    }

    @Override
    public void collectChildren() {
        super.collectChildren();
        String k = get("host_name");
        if (k != null && !k.isEmpty()) {
            NagItem c = NagCliCfg.get(Types.host, k);
            children.add(new NagPointer("host_name", c));
        }
    }

    public Service() {
        super(Types.service);
    }

}
