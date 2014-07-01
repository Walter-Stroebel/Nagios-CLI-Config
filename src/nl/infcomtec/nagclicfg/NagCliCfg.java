/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author walter
 */
public class NagCliCfg {

    public final static Properties config = new Properties();

    public final TreeMap<Types, TreeMap<String, NagItem>> nagDb = new TreeMap<>();
    public final ArrayList<NagItem> all = new ArrayList<>();
    public final ArrayList<File> files = new ArrayList<>();

    public final static NagCliCfg raw = new NagCliCfg();
    private BufferedReader input;
    public Emitter em;

    public NagItem get(Types type, String name) {
        TreeMap<String, NagItem> items = nagDb.get(type);
        if (items != null) {
            return items.get(name);
        }
        return null;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        boolean configOk = false;
        if (new File(System.getProperty("user.home"), ".nagclicfg").exists()) {
            try (FileReader fr = new FileReader(new File(System.getProperty("user.home"), ".nagclicfg"))) {
                config.load(fr);
                configOk = (config.getProperty("nagios.cache") != null);
                configOk = configOk && (config.getProperty("nagios.binary") != null);
                configOk = configOk && (config.getProperty("nagios.config") != null);
                configOk = configOk && (config.getProperty("nagios.reload") != null);
            } catch (Exception ex) {
                Logger.getLogger(NagCliCfg.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }
        }
        if (!configOk) {
            config.setProperty("nagios.cache", config.getProperty("nagios.cache", "/var/cache/nagios3/objects.cache"));
            config.setProperty("nagios.binary", config.getProperty("nagios.binary", "/usr/sbin/nagios3"));
            config.setProperty("nagios.config", config.getProperty("nagios.config", "/etc/nagios3/nagios.cfg"));
            config.setProperty("nagios.reload", config.getProperty("nagios.reload", "/etc/init.d/nagios3 reload"));
            config.setProperty("terminal.width", config.getProperty("terminal.width", "100"));
            System.err.println("No configuration found. Below is a sample property file.");
            System.err.println("Adjust as needed and save as " + System.getProperty("user.home") + "/.nagclicfg");
            try {
                config.store(System.err, "");
            } catch (IOException ex) {
                Logger.getLogger(NagCliCfg.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.exit(0);
        }
        TERMiNAL_WIDTH = Integer.valueOf(config.getProperty("terminal.width", "100"));
        try {
            NagCliCfg cfg = new NagCliCfg();
            try {
                cfg.em = new Emitter(args);
                raw.em = cfg.em;
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
            new UpdateRaw(config.getProperty("nagios.cache")).start();
            File nagCfgFile = new File(config.getProperty("nagios.config"));
            try (BufferedReader main = new BufferedReader(new FileReader(nagCfgFile))) {
                for (String fnd = main.readLine(); fnd != null; fnd = main.readLine()) {
                    fnd = fnd.trim();
                    if (fnd.startsWith("#")) {
                        continue;
                    }
                    if (fnd.isEmpty()) {
                        continue;
                    }
                    if (fnd.startsWith("cfg_file")) {
                        int idx = fnd.indexOf('=');
                        if (idx > 0) {
                            File f = new File(fnd.substring(idx + 1).trim());
                            if (!f.exists()) {
                                f = new File(nagCfgFile.getParentFile(), fnd.substring(idx + 1).trim());
                            }
                            if (f.exists()) {
                                cfg.read(f);
                                cfg.files.add(f);
                            } else {
                                cfg.em.err("File not found " + config.getProperty("nagios.config") + ": " + f);
                            }
                        } else {
                            cfg.em.err("Bad directive in " + config.getProperty("nagios.config") + ": " + fnd);
                        }
                    } else if (fnd.startsWith("cfg_dir")) {
                        int idx = fnd.indexOf('=');
                        if (idx > 0) {
                            File dir = new File(fnd.substring(idx + 1).trim());
                            if (!dir.exists()) {
                                dir = new File(nagCfgFile.getParentFile(), fnd.substring(idx + 1).trim());
                            }
                            if (dir.exists()) {
                                for (File f : dir.listFiles(
                                        new FilenameFilter() {
                                            @Override
                                            public boolean accept(File dir, String name) {
                                                return name.endsWith(".cfg");
                                            }
                                        }
                                )) {
                                    cfg.read(f);
                                }
                                cfg.files.add(dir);
                            } else {
                                cfg.em.err("Directory not found " + config.getProperty("nagios.config") + ": " + dir);
                            }
                        } else {
                            cfg.em.err("Bad directive in " + config.getProperty("nagios.config") + ": " + fnd);
                        }
                    }
                }
            }
            if (!cfg.em.quiet) {
                cfg.em.println("Nagios command-line configurator.");
                cfg.em.println("Objects loaded from " + cfg.files);
                cfg.em.println("Type 'help' for some assistance.");
            }
            try (BufferedReader bfr = new BufferedReader(new InputStreamReader(System.in))) {
                cfg.run(bfr);
            }
        } catch (IOException ex) {
            Logger.getLogger(NagCliCfg.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    /**
     * Main processing loop.
     *
     * @param bfr Input.
     * @param args Arguments
     * @throws IOException If it does.
     */
    private void run(BufferedReader bfr) throws IOException {
        this.input = bfr;
        while (input != null) {
            String cmd = readLine(null);
            if (cmd != null) {
                cli(cmd);
            }
        }
        em.exit(0,this);
    }

    /**
     * Read a line of input.
     *
     * @param prompt Optional prompt.
     * @return Next line or null on EOF.
     * @throws IOException If it does.
     */
    private String readLine(String prompt) throws IOException {
        if (input == null) {
            return null;
        }
        if (!em.quiet) {
            if (prompt == null) {
                em.print(getPath() + "> ");
            } else {
                em.print(prompt);
            }
        }
        String ret = input.readLine();
        if (ret == null) {
            input = null;
        } else {
            ret = ret.trim();
            if (ret.startsWith("#") || ret.startsWith(";")) {
                if (em.echo) {
                    em.println("ignoring: (" + ret + ")");
                }
                return readLine(prompt);
            }
            int start = ret.indexOf("|%");
            while (start >= 0) {
                int end = ret.indexOf("%|", start);
                if (end > start) {
                    String spi = ret.substring(start + 2, end).trim();
                    int pi = 0;
                    try {
                        pi = Integer.valueOf(spi);
                    } catch (Exception oops) {
                        // too bad
                    }
                    if (pi > 0 && pi <= em.args.size()) {
                        pi--;
                        ret = ret.substring(0, start) + em.args.get(pi) + ret.substring(end + 2);
                        start = ret.indexOf("|%");
                    } else {
                        // abort, bad parameter
                        start = -1;
                    }
                } else {
                    // abort, bad parameter
                    start = -1;
                }
            }
        }
        if (em.echo && ret != null) {
            em.println(ret);
        }
        return ret;
    }
    private Types dir = null;
    private NagItem item = null;
    private final Stack<NagItem> stack = new Stack<>();

    private void consolidate() {
        nagDb.clear();
        for (Types t : Types.values()) {
            nagDb.put(t, new TreeMap<String, NagItem>());
        }
        for (Iterator<NagItem> it = all.iterator(); it.hasNext();) {
            NagItem ni = it.next();
            NagItem dup = get(ni.getType(), ni.getName());
            if (dup != null) {
                em.println("Note: Merging duplicate items: " + dup.getType().toString() + "/" + dup.getName());
                dup.putAll(ni);
                it.remove();
            } else {
                nagDb.get(ni.getType()).put(ni.getName(), ni);
            }
        }
    }

    private void cli(String cmd) throws IOException {
        if (cmd.isEmpty()) {
            return;
        }
        consolidate();
        if (cmd.equals("help")) {
            printHelp();
        } else if (cmd.equals("quit") || cmd.equals("exit")) {
            em.exit(0,this);
        } else if (cmd.equals("tree")) {
            tree();
        } else if (cmd.startsWith("echo ")) {
            em.println(cmd.substring(5));
        } else if (cmd.equals("pwd")) {
            if (dir == null) {
                em.println("/");
            } else {
                if (item != null) {
                    em.println("/" + item.getType().toString() + "/" + item.getName());
                } else {
                    em.println("/" + dir.toString());
                }
            }
        } else if (cmd.equals("mv")) {
            move();
        } else if (cmd.equals("diff")) {
            diff();
        } else if (cmd.equals("dump")) {
            if (item == null) {
                em.println("Nothing to dump, cd to an object first.");
            } else {
                NagItem rawItem;
                synchronized (raw) {
                    rawItem = raw.get(item.getType(), item.getName());
                }
                if (rawItem == null) {
                    em.println("Raw item not found (in Nagios object cache).");
                    em.println("Maybe write, check and reload the current config first?");
                    em.println("(anywhere)> write");
                    em.println("(anywhere)> check");
                    em.println("(anywhere)> reload");
                } else {
                    rawItem.dump(em, false);
                }
            }
        } else if (cmd.equals("export")) {
            if (item == null) {
                em.println("Nothing to export, cd to an object first.");
            } else {
                item.dump(em, false);
            }
        } else if (cmd.equals("clone")) {
            if (item == null) {
                em.println("Nothing to clone, cd to an object first.");
            } else {
                cloneObject();
            }
        } else if (cmd.startsWith("ifcd")) {
            if (cd(cmd.substring(4).trim(), false)) {
                doIf();
            } else {
                doElse();
            }
        } else if (cmd.startsWith("cd")) {
            cd(cmd.substring(2).trim(), true);
        } else if (cmd.startsWith("reload")) {
            ProcessBuilder pb = new ProcessBuilder(("/bin/sh " + config.getProperty("nagios.reload")).split(" "));
            pb.redirectErrorStream(true);
            pb.inheritIO();
            final Process p = pb.start();
            try {
                p.waitFor();
            } catch (InterruptedException ex) {
                // no need to wait any longer?
            }
        } else if (cmd.startsWith("check")) {
            ProcessBuilder pb = new ProcessBuilder(config.getProperty("nagios.binary"), "-v", config.getProperty("nagios.config"));
            pb.redirectErrorStream(true);
            pb.inheritIO();
            final Process p = pb.start();
            try {
                p.waitFor();
            } catch (InterruptedException ex) {
                // no need to wait any longer?
            }
        } else if (cmd.startsWith("ls")) {
            ls(cmd);
        } else if (cmd.startsWith("find ")) {
            String arg = cmd.substring(4).trim().toLowerCase();
            find(arg);
        } else if (cmd.startsWith("rm ")) {
            set(cmd.substring(3).trim(), true, true);
        } else if (cmd.startsWith("ifrm")) {
            if (set(cmd.substring(5).trim(), true, true)) {
                doIf();
            } else {
                doElse();
            }
        } else if (cmd.equals("rmdir")) {
            rmdir();
        } else if (cmd.equals("ifrmdir")) {
            if (rmdir()) {
                doIf();
            } else {
                doElse();
            }
        } else if (cmd.startsWith("set")) {
            set(cmd.substring(3).trim(), true, false);
        } else if (cmd.startsWith("ifset")) {
            if (set(cmd.substring(5).trim(), true, false)) {
                doIf();
            } else {
                doElse();
            }
        } else if (cmd.startsWith("ifadd")) {
            if (set(cmd.substring(5).trim(), false, false)) {
                doIf();
            } else {
                doElse();
            }
        } else if (cmd.startsWith("add")) {
            set(cmd.substring(3).trim(), false, false);
        } else if (cmd.equals("write")) {
            write(false);
        } else if (cmd.equals("firstboot")) {
            write(true);
        } else {
            em.err("Unknown cmd '" + cmd + "'");
            em.println("... want to implement it?");
            em.println("git clone https://github.com/Walter-Stroebel/NagCliCfg.git");
        }
    }

    private void diff() throws IOException {
        if (item == null) {
            em.println("Nothing to compare, cd to an object first.");
        } else {
            NagItem rawItem;
            synchronized (raw) {
                rawItem = raw.get(item.getType(), item.getName());
            }
            if (rawItem == null) {
                em.println("Raw item not found (in Nagios object cache).");
                em.println("Maybe write, check and reload the current config first?");
                em.println("(anywhere)> write");
                em.println("(anywhere)> check");
                em.println("(anywhere)> reload");
            } else {
                File f1 = File.createTempFile("nagclicfg", ".cfg");
                File f2 = File.createTempFile("nagclicfg", ".cfg");
                try (PrintWriter pw = new PrintWriter(f1)) {
                    rawItem.dump(pw, false);
                }
                try (PrintWriter pw = new PrintWriter(f2)) {
                    item.dump(pw, true);
                }
                ProcessBuilder pb = new ProcessBuilder("diff", "-W", Integer.toString(TERMiNAL_WIDTH), "-y", f1.getAbsolutePath(), f2.getAbsolutePath());
                pb.redirectErrorStream(true);
                pb.inheritIO();
                final Process p = pb.start();
                try {
                    p.waitFor();
                } catch (InterruptedException ex) {
                    // no need to wait any longer?
                }
                f1.delete();
                f2.delete();
            }
        }
    }

    private void printHelp() {
        TreeSet<String> help = new TreeSet(Arrays.asList(new String[]{
            "tree: print a tree view of the entire configuration.",
            "add: add a value to the current object (see also 'set').",
            "cd <path>: move around in the configuration, use [ls] for suggestions.",
            "check: run 'nagios -v config_file' (do this after write!)",
            "clone: Clones the current object.",
            "diff: compare object to Nagios cached object (expect some differences).",
            "dump: Raw dump of the current object.",
            "echo: Just print the argument(s) to the output.",
            "else: inverted part of an if statement",
            "export: Export the current object (using generics).",
            "fi: closes an 'if' conditional block",
            "find: find any named object or group.",
            "help: you are reading it.",
            "ifadd <field> <value>: if the field was added continue processing commands, skip to else/fi otherwise.",
            "ifcd <path>: cd if exists and continue processing commands, skip to else/fi otherwise.",
            "ifset <field> <value>: if the field was changed continue processing commands, skip to else/fi otherwise.",
            "ifrm <field>: if the field was deleted continue processing commands, skip to else/fi otherwise.",
            "ls: list the current object or group.\n"//
            + "    -l (long) show more data (1 item per line)\n"//
            + "    -r (refs, implies -l) also show data from referrals\n"//
            + "    -s (sort) sort the output\n"//
            + "    -d (dns, implies -l) attempt to resolve the 'address' field (may be slow)",
            "mv: context sensitive, pick from the options offered.",
            "pwd: shows where you really are.",
            "quit, exit or ^D: exit the program.",
            "reload: Make Nagios reload the config (write and check first!)",
            "rm <field>: Delete a field in the current object.",
            "rmdir: Delete the current object.",
            "ifrmdir: if the object was deleted continue processing commands, skip to else/fi otherwise.",
            "set: set a value in the current object to a new value (see also 'add').",
            "write: write the entire config.",
            "firstboot: write the entire config; no questios asked!"}));
        for (String h : help) {
            em.println(h);
        }
    }

    private void move() throws IOException {
        if (item != null) {
            switch (item.getType()) {
                case service:
                    if (!item.containsKey(NagItem.SERVICE_DESCRIPTION)) {
                        em.println("This service lacks a '" + NagItem.SERVICE_DESCRIPTION + "' field, [add] one first.");
                        return;
                    }
                    if (!item.containsKey(NagItem.HOST_NAME) && !item.containsKey(NagItem.HOSTGROUP_NAME)) {
                        em.println("Item must have either a '" + NagItem.HOST_NAME + "' or a '" + NagItem.HOSTGROUP_NAME + "' field.");
                        return;
                    }
                    em.println("Enter H to move to a Host group or S to add to a Service group");
                    String to = readLine("H,S,Enter(do nothing): ");
                    if (to.equalsIgnoreCase("h") && item.containsKey(NagItem.HOST_NAME)) {
                        em.println("Moving this service to a host group.");
                        em.println("- Create a new host group, just type a new name.");
                        TreeMap<String, NagItem> lm = nagDb.get(Types.hostgroup);
                        if (lm == null) {
                            nagDb.put(dir, lm = new TreeMap<>());
                        }
                        for (NagItem e : lm.values()) {
                            em.println("- Existing group '" + e.getName() + "'");
                        }
                        em.println("- [enter] to do nothing.");
                        String pick = readLine("Pick one: ");
                        if (pick.isEmpty()) {
                            return;
                        }
                        NagItem dest = get(Types.hostgroup, pick);
                        if (dest == null) {
                            dest = NagItem.construct(this, Types.hostgroup);
                            dest.put("alias", pick);
                            dest.put(NagItem.HOSTGROUP_NAME, pick);
                            dest.put("members", item.get(NagItem.HOST_NAME));
                            all.add(dest);
                        } else {
                            TreeSet<String> mems = dest.fieldToSet("members");
                            if (mems == null) {
                                dest.put("members", item.get(NagItem.HOST_NAME));
                            } else {
                                mems.add(item.get(NagItem.HOST_NAME));
                                dest.put("members", NagItem.setToField(mems));
                            }
                        }
                        item.remove(NagItem.HOST_NAME);
                        item.put("hostgroup_name", pick);
                    } else if (to.equalsIgnoreCase("s") && item.containsKey(NagItem.HOSTGROUP_NAME)) {
                        String hgn = item.get(NagItem.HOSTGROUP_NAME);
                        em.println("Adding all hosts from hostgroup '" + hgn + "' to a service group.");
                        em.println("- Create a new service group, just type a new name.");
                        TreeMap<String, NagItem> lm = nagDb.get(Types.servicegroup);
                        if (lm == null) {
                            nagDb.put(dir, lm = new TreeMap<>());
                        }
                        for (NagItem e : lm.values()) {
                            em.println("- Existing group '" + e.getName() + "'");
                        }
                        em.println("- [enter] to do nothing.");
                        String pick = readLine("Pick one: ");
                        if (pick.isEmpty()) {
                            return;
                        }
                        ServiceGroup dest = (ServiceGroup) get(Types.servicegroup, pick);
                        if (dest == null) {
                            dest = (ServiceGroup) NagItem.construct(this, Types.servicegroup);
                            all.add(dest);
                            dest.put("alias", pick);
                            dest.put("servicegroup_name", pick);
                            lm.put(dest.getName(), dest);
                        }
                        TreeSet<ServiceGroup.HostAndService> mems = dest.members();
                        NagItem hg = get(Types.hostgroup, hgn);
                        String desc = item.get(NagItem.SERVICE_DESCRIPTION);
                        for (String h : hg.fieldToSet("members")) {
                            mems.add(new ServiceGroup.HostAndService(h, desc));
                        }
                        dest.put("members", dest.membersToString(mems));
                    } else if (to.equalsIgnoreCase("s") && item.containsKey(NagItem.HOST_NAME)) {
                        em.println("Adding this service to a service group.");
                        em.println("- Create a new service group, just type a new name.");
                        TreeMap<String, NagItem> lm = nagDb.get(Types.servicegroup);
                        if (lm == null) {
                            nagDb.put(dir, lm = new TreeMap<>());
                        }
                        for (NagItem e : lm.values()) {
                            em.println("- Existing group '" + e.getName() + "'");
                        }
                        em.println("- [enter] to do nothing.");
                        String pick = readLine("Pick one: ");
                        if (pick.isEmpty()) {
                            return;
                        }
                        ServiceGroup dest = (ServiceGroup) get(Types.servicegroup, pick);
                        if (dest == null) {
                            dest = (ServiceGroup) NagItem.construct(this, Types.servicegroup);
                            dest.put("alias", pick);
                            dest.put("servicegroup_name", pick);
                            dest.put("members", item.get(NagItem.HOST_NAME) + "," + item.get(NagItem.SERVICE_DESCRIPTION));
                            all.add(dest);
                        } else {
                            TreeSet<ServiceGroup.HostAndService> mems = dest.members();
                            mems.add(new ServiceGroup.HostAndService(item.get(NagItem.HOST_NAME), item.get(NagItem.SERVICE_DESCRIPTION)));
                            dest.put("members", dest.membersToString(mems));
                        }
                    } else {
                        em.println("Sorry, no move actions defined for this type of service definition.");
                    }
                    break;
                default:
                    em.println("Sorry, no move actions defined for a " + item.getType().toString());
                    break;
            }
        } else {
            em.println("Sorry, no move actions defined at this level.");
        }
    }

    private void doElse() throws IOException {
        if (skip("fi", "else").equals("else")) {
            while (true) {
                String cmd2 = readLine(null);
                if (cmd2 == null) {
                    return;
                } else {
                    if (cmd2.equals("fi")) {
                        return;
                    } else {
                        cli(cmd2);
                    }
                }
            }
        }
    }

    private void doIf() throws IOException {
        while (true) {
            String cmd2 = readLine(null);
            if (cmd2 == null) {
                return;
            } else {
                if (cmd2.equals("else")) {
                    skip("fi");
                    return;
                } else if (cmd2.equals("fi")) {
                    return;
                } else {
                    cli(cmd2);
                }
            }
        }
    }

    /**
     * List data.
     *
     * @param cmd Options: -l, -r, -s, -d
     */
    private void ls(String cmd) {
        StringTokenizer opts = new StringTokenizer(cmd.substring(2), " -\t");
        boolean oLong = false;
        boolean oSort = false;
        boolean oRecr = false;
        boolean oDNS = false;
        while (opts.hasMoreTokens()) {
            for (char c : opts.nextToken().toLowerCase().toCharArray()) {
                if (c == 'l') {
                    oLong = true;
                } else if (c == 's') {
                    oSort = true;
                } else if (c == 'r') {
                    oRecr = true;
                    oLong = true;
                } else if (c == 'd') {
                    oDNS = true;
                    oLong = true;
                } else {
                    em.println("Unknown option " + c + " for ls");
                    return;
                }
            }
        }
        ArrayList<String[]> grid = list(oRecr, oDNS);
        if (oSort) {
            Collections.sort(grid, new Comparator<String[]>() {

                @Override
                public int compare(String[] o1, String[] o2) {
                    for (int i = 0; i < o1.length && i < o2.length; i++) {
                        int c = o1[i].compareToIgnoreCase(o2[i]);
                        if (c != 0) {
                            return c;
                        }
                    }
                    return Integer.compare(o1.length, o2.length);
                }
            });
        }
        int m0 = 0;
        for (String[] e : grid) {
            if (e[0] != null) {
                m0 = Math.max(m0, e[0].length());
            }
        }
        if (m0 > 0) {
            if (!oLong) {
                int c = TERMiNAL_WIDTH / (m0 + 1);
                if (c == 0) {
                    c = 1;
                }
                String fmt = "%-" + (TERMiNAL_WIDTH / c) + "s";
                for (int i = 0; i < grid.size(); i += c) {
                    String sep = "";
                    for (int j = 0; j < c && (i + j) < grid.size(); j++) {
                        em.print(sep);
                        sep = " ";
                        em.print(String.format(fmt, grid.get(i + j)[0]));
                    }
                    em.println();
                }
            } else {
                if (m0 > TERMiNAL_WIDTH / 2) {
                    m0 = TERMiNAL_WIDTH / 2;
                }
                int m1 = TERMiNAL_WIDTH - m0 - 2;
                String fmt = "%-" + m0 + "s ";
                StringBuilder rep = new StringBuilder(String.format(fmt, "").replace("  ", " .")).reverse();
                for (String[] e : grid) {
                    String k = e[0];
                    if (k.length() > m0) {
                        k = k.substring(0, m0);
                    }
                    StringBuilder sb = new StringBuilder(String.format(fmt, k));
                    sb = new StringBuilder(sb.reverse().toString().replace("  ", " ."));
                    em.print(sb.reverse().toString());
                    String v = e[1];
                    while (true) {
                        if (v.length() > m1) {
                            em.println(v.substring(0, m1));
                            v = v.substring(m1);
                            em.print(rep.toString());
                        } else {
                            em.println(v);
                            break;
                        }
                    }
                }
            }
        } else {
            em.println("Nothing found to list?" + grid);
        }
    }

    private ArrayList<String[]> list(boolean recursive, boolean useDNS) {
        ArrayList<String[]> grid = new ArrayList<>();
        if (dir == null) {
            for (Types t : Types.values()) {
                if (nagDb.get(t) != null) {
                    grid.add(new String[]{t.toString(), Integer.toString(nagDb.get(t).size())});
                } else {
                    grid.add(new String[]{t.toString(), "0"});
                }
            }
        } else if (item == null) {
            TreeMap<String, NagItem> col = nagDb.get(dir);
            if (col != null) {
                for (NagItem e : col.values()) {
                    grid.add(new String[]{e.getName(), e.getNameFields()[0].equals("name") ? "generic" : "regular"});
                }
            }
        } else {
            if (recursive) {
                for (Map.Entry<String, String> e : item.getAllFields().entrySet()) {
                    if (useDNS && e.getKey().equals("address")) {
                        try {
                            InetAddress a = InetAddress.getByName(e.getValue());
                            grid.add(new String[]{e.getKey(), e.getValue() + " Addr=" + a.getHostAddress() + " (" + a.getCanonicalHostName() + ")"});
                        } catch (UnknownHostException unknown) {
                            grid.add(new String[]{e.getKey(), e.getValue() + " (DNS failed)"});
                        }
                    } else {
                        grid.add(new String[]{e.getKey(), e.getValue()});
                    }
                }
            } else {
                for (Map.Entry<String, String> e : item.entrySet()) {
                    if (useDNS && e.getKey().equals("address")) {
                        try {
                            InetAddress a = InetAddress.getByName(e.getValue());
                            grid.add(new String[]{e.getKey(), e.getValue() + " Addr=" + a.getHostAddress() + " (" + a.getCanonicalHostName() + ")"});
                        } catch (UnknownHostException unknown) {
                            grid.add(new String[]{e.getKey(), e.getValue() + " (DNS failed)"});
                        }
                    } else {
                        grid.add(new String[]{e.getKey(), e.getValue()});
                    }
                }
            }
            if (recursive) {
                for (NagPointer c : item.getChildren()) {
                    grid.add(new String[]{c.key, "--> " + c.item.getName()});
                }
            }
        }
        return grid;
    }
    public static int TERMiNAL_WIDTH = 100;

    private boolean set(String nvp, boolean ifExists, boolean remove) {
        String key, val;
        if (!remove) {
            String[] two = splitNVP(nvp);
            key = two[0];
            val = two[1];
        } else {
            key = nvp;
            val = "";
        }
        if (item == null) {
            em.println("No current item, cd to one first");
            return false;
        } else if (ifExists && item.containsKey(key)) {
            String oldVal = item.get(key);
            if (oldVal != null && oldVal.equals(val)) {
                return false;
            }
            if (val.isEmpty()) {
                for (String nf : item.getNameFields()) {
                    if (key.equals(nf)) {
                        em.println("Sorry, cannot remove/clear a field naming an object.");
                        return false;
                    }
                }
            }
            for (String nf : item.getNameFields()) {
                if (key.equals(nf)) {
                    rename(item.get(key), item.type, val);
                }
            }
            if (remove) {
                em.println("Removing '" + key + "'");
                item.remove(key);
            } else {
                em.println("Changing '" + key + "' from '" + oldVal + "' to '" + val + "'");
                item.put(key, val);
            }
            return true;
        } else if (!ifExists && !item.containsKey(key)) {
            em.println("Adding '" + key + "' as '" + val + "'");
            item.put(key, val);
            return true;
        } else if (item.containsKey(key)) {
            em.println("Not adding '" + key + "' as '" + val + "'; item already exists. Use [set key value] instead.");
            return false;
        } else {
            em.println("Not setting '" + key + "' to '" + val + "'; not an existing item. Use [add key value] instead.");
            return false;
        }
    }

    private String[] splitNVP(String nvp) {
        StringBuilder k = new StringBuilder();
        StringBuilder v = new StringBuilder();
        int ph = 0;
        for (char c : nvp.toCharArray()) {
            if (ph == 0) {
                if (Character.isWhitespace(c)) {
                    ph = 1;
                } else {
                    k.append(c);
                }
            } else if (ph == 1) {
                if (!Character.isWhitespace(c)) {
                    if (c != ';') {
                        v.append(c);
                    }
                    ph = 2;
                }
            } else {
                if (c == ';') {
                    break;
                }
                v.append(c);
            }
        }
        return new String[]{k.toString(), v.toString().trim()};
    }

    /**
     * Find and update all references to an object whose name was changed.
     *
     * @param from Original name.
     * @param to New name.
     */
    private void rename(String from, Types type, String to) {
        for (NagItem e1 : all) {
            for (NagPointer e2 : e1.getChildren()) {
                if (e2.item.getName().equals(from) && e2.item.getType() == type) {
                    em.print("Old name is refered to in field '" + e2.key + "' in object '" + e1.getType() + "/" + e1.getName() + "'");
                    TreeSet<String> set = e1.fieldToSet(e2.key);
                    set.remove(from);
                    if (!to.isEmpty()) {
                        set.add(to);
                    }
                    String rep = NagItem.setToField(set);
                    em.println(": Replacing '" + e1.get(e2.key) + "' with '" + rep + "'");
                    e1.put(e2.key, rep);
                }
            }
        }
    }

    /**
     * Write this configuration to the Nagios configuration directory.
     */
    private void write(boolean override) throws IOException {
        boolean isNagCliCfg = true;
        File nagCfg = new File(config.getProperty("nagios.config"));
        File nagDir = nagCfg.getParentFile();
        File nagBak = new File(nagDir, "nagclicfg_nagios_cfg.bak");
        File cfgDir = new File(nagDir, "nagclicfg.d");
        for (File f : files) {
            if (!f.getName().equals("nagclicfg.d") || !f.isDirectory()) {
                isNagCliCfg = false;
            }
        }
        if (!isNagCliCfg) {
            if (!override) {
                if (em.quiet) {
                    em.err("Nagios is not yet configured by NagcliCfg and using batch mode.");
                    em.err("Skipping write command (no changes are made nor saved!)");
                }
                em.println("Warning! This might restructure your current Nagios configuration completely!");
                em.println("The following directive will be disabled in " + config.getProperty("nagios.config") + ":");
                for (File f : files) {
                    if (!f.getName().startsWith("ngcli_")) {
                        if (f.isDirectory()) {
                            em.println("cfg_dir=" + f.getAbsolutePath());
                        } else {
                            em.println("cfg_file=" + f.getAbsolutePath());
                        }
                    }
                }
                em.println("They will be replaced with one directive: cfg_dir=nagclicfg.d");
                em.println("In that directory, files will be created for each object type.");
                em.println("You can return to your old setup by inverting those modifications.");
                em.println("Are you sure you want to do this (ie. you *HAVE* a backup)?");
                String yes = readLine("Type YES to continue: ");
                if (yes == null || !"YES".equals(yes)) {
                    return;
                }
            }
            if (!cfgDir.exists()) {
                if (!cfgDir.mkdir()) {
                    em.err("Failed to create " + cfgDir.getAbsolutePath() + " ... not root?");
                    em.err("Write aborted (nothing was changed).");
                }
            }
            boolean didDelete = nagBak.delete();
            if (!nagCfg.renameTo(nagBak)) {
                em.err("Failed to backup old " + config.getProperty("nagios.config") + " ... not root?");
                if (!didDelete) {
                    em.err("Write aborted (nothing was changed).");
                } else {
                    em.err("Deleted old backup file " + nagBak.getAbsolutePath() + "; nothing else was changed.");
                    em.changed = true;
                }
                return;
            }
            em.changed = true;
            try (PrintWriter pw = new PrintWriter(nagCfg)) {
                try (BufferedReader main = new BufferedReader(new FileReader(nagBak))) {
                    for (String fnd = main.readLine(); fnd != null; fnd = main.readLine()) {
                        fnd = fnd.trim();
                        if (fnd.startsWith("#")) {
                            pw.println(fnd);
                        } else if (fnd.isEmpty()) {
                            pw.println(fnd);
                        } else if (fnd.equals("cfg_dir=nagclicfg.d")) {
                            isNagCliCfg = true;
                            pw.println(fnd);
                        } else if (fnd.startsWith("cfg_file")) {
                            pw.println("# removed by NagCliCfg: " + fnd);
                            if (!isNagCliCfg) {
                                // good place to write this
                                pw.println("cfg_dir=nagclicfg.d");
                                isNagCliCfg = true;
                            }
                        } else if (fnd.startsWith("cfg_dir")) {
                            pw.println("# removed by NagCliCfg: " + fnd);
                        } else {
                            pw.println(fnd);
                        }
                    }
                }
            }
            files.clear();
            files.add(cfgDir);
        }
        for (Map.Entry<Types, TreeMap<String, NagItem>> cfg : nagDb.entrySet()) {
            try (PrintWriter out = new PrintWriter(new File(cfgDir, cfg.getKey().toString() + ".cfg"))) {
                for (NagItem e : cfg.getValue().values()) {
                    e.dump(out, false);
                }
            } catch (FileNotFoundException ex) {
                em.failed(ex);
            }
        }
    }

    /**
     * Change Directory.
     *
     * @param path Where to go.
     */
    private boolean cd(String path, boolean failIfNotExists) throws IOException {
        String oldPath = getPath();
        if (path.isEmpty()) {
            ArrayList<String[]> grid = list(false, false);
            for (int i = 0; i < grid.size(); i++) {
                em.println(Integer.toString(i, 36) + ": " + grid.get(i)[0]);
            }
            String ch = readLine("Pick one: ");
            int sel;
            try {
                sel = Integer.parseInt(ch, 36);
            } catch (NumberFormatException ahWell) {
                sel = -1;
            }
            if (sel >= 0 && sel < grid.size()) {
                path = grid.get(sel)[0];
            }
        }
        if (!path.startsWith("/")) {
            path = getPath() + "/" + path;
        }
        dir = null;
        item = null;
        stack.clear();
        StringTokenizer toker = new StringTokenizer(path, "/");
        while (toker.hasMoreTokens()) {
            String part = toker.nextToken();
            int pLen = -1;
            if (part.endsWith("*")) {
                pLen = part.length() - 1;
                part = part.substring(0, pLen);
            }
            if (part.equals("..")) {
                if (!stack.empty()) {
                    item = stack.pop();
                } else if (item == null) {
                    dir = null;
                } else {
                    item = null;
                }
            } else {
                if (dir == null) {
                    for (Types t : Types.values()) {
                        if (pLen == 0) {
                            dir = t;
                            break;
                        } else if (pLen > 0 && t.toString().length() >= pLen && t.toString().substring(0, pLen).equals(part)) {
                            dir = t;
                            break;
                        } else if (part.equals(t.toString())) {
                            dir = t;
                            break;
                        }
                    }
                    if (dir == null) {
                        cdError(part, oldPath, failIfNotExists);
                        return false;
                    }
                } else if (item == null) {
                    for (NagItem e : nagDb.get(dir).values()) {
                        if (pLen == 0) {
                            item = e;
                            break;
                        } else if (pLen > 0 && e.getName().length() >= pLen && e.getName().substring(0, pLen).equals(part)) {
                            item = e;
                            break;
                        } else if (part.equals(e.getName())) {
                            item = e;
                            break;
                        }
                    }
                    if (item == null) {
                        cdError(part, oldPath, failIfNotExists);
                        return false;
                    }
                } else {
                    NagItem nItem = null;
                    for (NagPointer e2 : item.getChildren()) {
                        if (pLen == 0) {
                            nItem = e2.item;
                            break;
                        } else if (pLen > 0 && e2.item.getName().length() >= pLen && e2.item.getName().substring(0, pLen).equals(part)) {
                            nItem = e2.item;
                            break;
                        } else if (part.equals(e2.item.getName())) {
                            nItem = e2.item;
                            break;
                        }
                    }
                    if (nItem == null) {
                        cdError(part, oldPath, failIfNotExists);
                        return false;
                    } else {
                        stack.push(item);
                        item = nItem;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Just prints an error message and returns to the path before an attempted
     * cd.
     *
     * @param part Path part that was not found.
     * @param oldPath Original path.
     */
    private void cdError(String part, String oldPath, boolean failIfNotExists) throws IOException {
        if (failIfNotExists) {
            em.println("Not found: '" + part + "' in " + getPath());
        }
        cd(oldPath, true);
    }

    /**
     * Return the current path.
     *
     * @return The current path.
     */
    private String getPath() {
        StringBuilder path = new StringBuilder();
        if (dir == null) {
            path.append("/");
        } else {
            path.append("/").append(dir.toString());
            if (item != null) {
                if (!stack.empty()) {
                    for (NagItem e : stack) {
                        path.append("/").append(e.getName());
                    }
                }
                path.append("/").append(item.getName());
            }
        }
        return path.toString();
    }

    /**
     * Read a object or group definition file.
     *
     * @param f The file to read.
     * @throws IOException If it does.
     */
    public void read(File f) throws IOException {
        try (BufferedReader bfr = new BufferedReader(new FileReader(f))) {
            for (String s = bfr.readLine(); s != null; s = bfr.readLine()) {
                s = s.trim();
                if (s.startsWith("#")) {
                    continue;
                }
                if (s.isEmpty()) {
                    continue;
                }
                //em.println(s);
                if (s.startsWith("define ")) {
                    StringTokenizer toker = new StringTokenizer(s, " {");
                    toker.nextToken();
                    NagItem itm;
                    Types what;
                    try {
                        what = Types.valueOf(toker.nextToken());
                        itm = NagItem.construct(this, what);
                    } catch (Exception oops) {
                        continue;
                    }
                    TreeMap<String, NagItem> items = nagDb.get(what);
                    if (items == null) {
                        nagDb.put(what, items = new TreeMap<>());
                    }
                    for (String s2 = bfr.readLine(); s2 != null; s2 = bfr.readLine()) {
                        s2 = s2.trim();
                        if (s2.startsWith("#")) {
                            continue;
                        }
                        if (s2.isEmpty()) {
                            continue;
                        }
                        if (s2.equals("}")) {
                            if (itm.getNameFields().length == 0 || itm.getName() == null) {
                                em.failed("Fatal error for " + itm + "; cannot handle unnamed items.");
                            }
                            if (items.containsKey(itm.getName())) {
                                em.failed("Unable to load configuration; duplicate items:\n" + itm + "\nand:\n" + itm);
                            }
                            items.put(itm.getName(), itm);
                            all.add(itm);
                            break;
                        }
                        //em.println(s2);
                        String[] parts = splitNVP(s2);
                        if (parts.length != 2) {
                            em.failed("Not two elements: " + Arrays.toString(parts));
                        } else {
                            itm.put(parts[0], parts[1]);
                        }
                    }
                    if (itm.getName() == null || itm.getName().isEmpty()) {
                        em.failed("Invalid object found: " + itm);
                    }
                } else {
                    em.println(s);
                    break;
                }
            }
        }
    }

    private void cloneObject() throws IOException {
        NagItem ni = NagItem.construct(this, item.getType());
        ni.putAll(item);
        while (true) {
            for (String s : ni.getNameFields()) {
                String name = readLine("Enter a new value for " + s + ": ");
                if (name == null || name.isEmpty()) {
                    return;
                }
                ni.put(s, name);
            }
            if (nagDb.get(item.getType()).containsKey(ni.getName())) {
                em.err("Duplicate name!");
            } else {
                break;
            }
        }
        all.add(ni);
        stack.clear();
        item = ni;
        if (!em.quiet || em.echo) {
            ls("ls -rd");
        }
    }

    /**
     * Reset all the data
     */
    private void clear() {
        all.clear();
        dir = null;
        files.clear();
        item = null;
        nagDb.clear();
        stack.clear();
        input = null;
    }

    /**
     * Simple if else fi processing.
     *
     * @param bfr Command stream.
     * @param until End tokens.
     * @return Token we ended on.
     * @throws IOException If it does.
     */
    private String skip(String... until) throws IOException {
        TreeSet<String> _until = new TreeSet<>(Arrays.asList(until));
        while (true) {
            String skip = readLine(null);
            if (skip == null) {
                // artifact, force end of processing
                return "fi";
            }
            if (_until.contains(skip)) {
                return skip;
            }
            if (skip.startsWith("if")) {
                if (skip("else", "fi").equals("else")) {
                    skip("fi");
                }
            }
        }
    }

    private void find(String arg) {
        for (Map.Entry<Types, TreeMap<String, NagItem>> top : nagDb.entrySet()) {
            if (top.getKey().toString().toLowerCase().contains(arg)) {
                em.println("/" + top.getKey());
            }
            for (Map.Entry<String, NagItem> obj : top.getValue().entrySet()) {
                if (obj.getKey().toLowerCase().contains(arg)) {
                    em.println("/" + top.getKey() + "/" + obj.getKey());
                }
                for (NagPointer ref : obj.getValue().getChildren()) {
                    if (ref.item.getName().toLowerCase().contains(arg)) {
                        em.println("/" + top.getKey() + "/" + obj.getKey() + ": " + ref.key + " ->  " + ref.item.getName());
                    }
                }
            }
        }
    }

    private void tree() {
        for (Map.Entry<Types, TreeMap<String, NagItem>> top : nagDb.entrySet()) {
            em.println(top.getKey().toString());
            for (Map.Entry<String, NagItem> obj : top.getValue().entrySet()) {
                em.println(" +-- " + obj.getKey());
                for (NagPointer ref : obj.getValue().getChildren()) {
                    em.println(" | +-- " + ref.key + " ->  " + ref.item.getName());
                }
            }
        }
    }

    private boolean rmdir() throws IOException {
        if (item == null) {
            em.println("No current item, cd to one first");
            return false;
        }
        NagItem delete = item;
        while (item != null) {
            cd("..", false);
        }
        String name = delete.getName();
        for (NagItem itm : all) {
            for (NagPointer ptr : itm.getChildren()) {
                if (ptr.item.getType() == delete.getType() && ptr.item.getName().equals(name)) {
                    itm.removeChild(ptr);
                }
            }
        }
        all.remove(delete);
        return true;
    }

    JSONObject jsonTree() {
        JSONObject nag = new JSONObject();
        for (Map.Entry<Types, TreeMap<String, NagItem>> top : nagDb.entrySet()) {
            JSONObject col = new JSONObject();
            for (Map.Entry<String, NagItem> obj : top.getValue().entrySet()) {
                JSONArray itm = new JSONArray();
                for (NagPointer ref : obj.getValue().getChildren()) {
                    JSONObject ref2 = new JSONObject();
                    ref2.put(ref.key,ref.item.getName());
                    itm.put(ref2);
                }
                col.put(obj.getKey(), itm);
            }
            nag.put(top.getKey().toString(), col);
        }
        JSONObject ret= new JSONObject();
        ret.put("nagios", nag);
        return ret;
    }

    private static class UpdateRaw extends Thread {

        private final File rawFile;

        private UpdateRaw(String rawFile) {
            this.rawFile = new File(rawFile);
        }

        @Override
        public void run() {
            if (this.rawFile.exists()) {
                synchronized (raw) {
                    raw.clear();
                    try {
                        raw.read(rawFile);
                    } catch (IOException ex) {
                        raw.em.failed("Failed to read Nagios object cache: " + ex.getMessage());
                    }
                }
            }
        }
    }
}
