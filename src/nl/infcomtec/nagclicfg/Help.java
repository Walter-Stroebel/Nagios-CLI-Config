/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeSet;

/**
 *
 * @author walter
 */
public class Help {

    public static final String[] help = new String[]{
        "explain <command>: gives more information on a command, if /etc/nagios(3)/nagclicfg.md exists.", //
        "import <path>: add an object or a directory of objects.",//
        "replace <path>: substitute an object or a directory of objects.",//
        "tree: print a tree view of the entire configuration.", //
        "add: add a value to the current object (see also 'set').",//
        "cd <path>: move around in the configuration, use [ls] for suggestions.",//
        "check: run 'nagios -v config_file' (do this after write!)",//
        "clone: Clones the current object.", //
        "diff: compare object to Nagios cached object (expect some differences).",//
        "dump: Raw dump of the current object.", //
        "echo: Just print the argument(s) to the output.",//
        "else: inverted part of an if statement",//
        "export: Export the current object (using generics).",//
        "fi: closes an 'if' conditional block",//
        "find: find any named object or group.", //
        "help: you are reading it.",//
        "ifadd <field> <value>: if the field was added continue processing commands, skip to else/fi otherwise.",//
        "ifcd <path>: cd if exists and continue processing commands, skip to else/fi otherwise.",//
        "ifset <field> <value>: if the field was changed continue processing commands, skip to else/fi otherwise.",//
        "ifrm <field>: if the field was deleted continue processing commands, skip to else/fi otherwise.",//
        "ls: list the current object or group.\n"//
        + "    -l (long) show more data (1 item per line)\n"//
        + "    -r (refs, implies -l) also show data from referrals\n"//
        + "    -s (sort) sort the output\n"//
        + "    -d (dns, implies -l) attempt to resolve the 'address' field (may be slow)",//
        "mv: context sensitive, pick from the options offered.", //
        "pwd: shows where you really are.",//
        "quit, exit or ^D: exit the program.", //
        "reload: Make Nagios reload the config (write and check first!)",//
        "rm <field>: Delete a field in the current object.", //
        "rmdir: Delete the current object.",//
        "ifrmdir: if the object was deleted continue processing commands, skip to else/fi otherwise.",//
        "set: set a value in the current object to a new value (see also 'add').",//
        "write: write the entire config.", //
        "define: context sensitive, will ask the required items for the object being defined.", //
        "firstboot: write the entire config; no questios asked!"};
    public final static TreeSet<String> sorted = new TreeSet(Arrays.asList(help));

    /**
     *
     * @param em The NagCliCfg Emitter object.
     */
    public static void printHelp(Emitter em) {
        if (em.json) {
            em.err("Asking for help in JSON mode does nothing.");
        }
        for (String h : sorted) {
            em.println(h);
        }
    }

    public static void explain(NagCliCfg cfg, String what) {
        String _what = what.trim().toLowerCase();
        if (what.isEmpty()) {
            what = "explain";
        }
        File nagDir = new File(cfg.config.getProperty("nagios.config")).getParentFile();
        File docFile = new File(nagDir, "nagclicfg.md");
        try (BufferedReader bfr = new BufferedReader(new FileReader(docFile))) {
            for (String s = bfr.readLine(); s != null; s = bfr.readLine()) {
                if (s.trim().toLowerCase().equals(_what)) {
                    cfg.em.println(s);
                    for (String s2 = bfr.readLine(); s2 != null; s2 = bfr.readLine()) {
                        if (s2.equals("* * * * *")) {
                            return;
                        }
                        cfg.em.println(s2);
                    }
                }
            }
        } catch (IOException ex) {
            cfg.em.err("Cannot explain, could not open " + docFile);
        }
        cfg.em.err("Nothing known on '" + what + "' ... might just be laziness of the developer ;)");
    }
}
