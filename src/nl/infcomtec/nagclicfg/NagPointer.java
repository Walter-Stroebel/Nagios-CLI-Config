/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

/**
 *
 * @author walter
 */
public class NagPointer {

    public final NagPointerDef key;
    public final NagItem item;

    public NagPointer(NagPointerDef key, NagItem item) {
        this.key = key;
        this.item = item;
    }

    @Override
    public String toString() {
        return "NagPointer{" + "key=" + key + ", item=" + item + '}';
    }
}
