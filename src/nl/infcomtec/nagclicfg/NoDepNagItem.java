/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.util.Map;

/** For basic objects.
 *
 * @author walter
 */
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
                    NagItem c = owner.get(t, e.getValue());
                    if (c != null) {
                        children.add(new NagPointer(e.getKey(), c));
                    }
                }
            }
        }
    }

    /**
     * Constructor
     *
     * @param owner Owning object, needed for referrals.
     * @param type One of the basic Nagios object types.
     */
    public NoDepNagItem(NagCliCfg owner, Types type) {
        super(owner, type);
    }

    @Override
    public String getNameField() {
        String fNam = type.toString() + "_name";
        if (containsKey(fNam)) return fNam;
        if (containsKey("name")) return "name";
        return null;
    }

}
