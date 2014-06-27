/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.util.TreeSet;

/**
 *
 * @author walter
 */
public enum Types {

    command, contact, contactgroup, host, hostgroup, service, servicegroup, timeperiod;
    public TreeSet<Types> memberOf = new TreeSet<>();
    public TreeSet<Types> referencedBy = new TreeSet<>();
}
