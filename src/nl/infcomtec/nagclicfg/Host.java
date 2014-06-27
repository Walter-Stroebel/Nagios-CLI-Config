/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

public class Host extends NoDepNagItem {

    @Override
    public void collectChildren() {
        super.collectChildren();
        String k = get("parents");
        if (k != null && !k.isEmpty()) {
            String[] mems = k.split(",");
            for (String mem : mems) {
                NagItem c = NagCliCfg.get(Types.host, mem);
                children.add(new NagPointer("parents", c));
            }
        }
    }

    public Host() {
        super(Types.host);
    }

}
