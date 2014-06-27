/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.util.Map;

public class NoDepNagItem extends NagItem {

    @Override
    public void collectChildren() {
        for (Types t : Types.values()) {
            for (Map.Entry<String, String> e : entrySet()) {
                String refAs = t.toString();
                if (refAs.equals("timeperiod")) {
                    refAs = "period";
                }
                if (e.getKey().endsWith("_" + refAs)) {
                    NagItem c = NagCliCfg.get(t, e.getValue());
                    if (c != null) {
                        children.add(new NagPointer(e.getKey(), c));
                    }
                }
            }
        }
    }

    public NoDepNagItem(Types type) {
        super(type);
    }

    @Override
    public String getName() {
        return get(type.toString() + "_name");
    }

}
