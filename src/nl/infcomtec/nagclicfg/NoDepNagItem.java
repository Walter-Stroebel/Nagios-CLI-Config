/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.util.ArrayList;
import java.util.Map;

/**
 * For basic objects.
 *
 * @author walter
 */
public class NoDepNagItem extends NagItem {

    @Override
    public ArrayList<NagPointer> getChildren() {
        ArrayList<NagPointer> children = new ArrayList<>();
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
        return children;
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
    public String[] getNameFields() {
        String fNam = type.toString() + "_name";
        if (containsKey(fNam)) {
            return new String[]{fNam};
        }
        if (containsKey("name")) {
            return new String[]{"name"};
        }
        owner.em.err(this);
        owner.em.failed("Fatal: NagItem does not know how to name itself");
        return null;
    }

}
