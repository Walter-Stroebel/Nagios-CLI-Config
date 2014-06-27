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

    public final String key;
    public final NagItem item;

    public NagPointer(String key, NagItem item) {
        this.key = key;
        this.item = item;
    }
}
