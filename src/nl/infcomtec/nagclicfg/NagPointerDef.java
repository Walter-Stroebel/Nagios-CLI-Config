/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

/**
 *
 * @author walter
 */
public class NagPointerDef implements Comparable<NagPointerDef> {

    /**
     * Type of the object pointing to another object.
     */
    public final Types from;
    /**
     * Type of the object being pointed to.
     */
    public final Types to;
    /**
     * Field in the 'from' object doing the pointing.
     */
    public final String byField;
    /**
     * Stride, 0=one value, 1=single values (eg members), N=N values (eg
     * servicegroup_members).
     */
    public final int stride;

    /**
     * Constructor.
     *
     * @param from Type of the object pointing to another object.
     * @param to Type of the object being pointed to.
     * @param byField Field in the 'from' object doing the pointing.
     * @param stride Stride, 0=one value, 1=single values (eg members), N=N
     * values (eg servicegroup_members).
     */
    public NagPointerDef(Types from, Types to, String byField, int stride) {
        this.from = from;
        this.to = to;
        this.byField = byField;
        this.stride = stride;
    }

    @Override
    public int compareTo(NagPointerDef o) {
        int c = from.compareTo(o.from);
        if (c != 0) {
            return c;
        }
        return byField.compareTo(o.byField);
    }

}
