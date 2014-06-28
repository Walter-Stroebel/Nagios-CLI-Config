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
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author walter
 */
public class NagCliCfg {

    public final static Properties config = new Properties();

    public final TreeMap<Types, ArrayList<NagItem>> nagDb = new TreeMap<>();
    public final ArrayList<NagItem> all = new ArrayList<>();
    public final ArrayList<File> files = new ArrayList<>();

    public NagItem get(Types type, String name) {
        ArrayList<NagItem> items = nagDb.get(type);
        if (items != null) {
            for (NagItem itm : items) {
                if (itm.getName() != null && itm.getName().equals(name)) {
                    return itm;
                }
            }
        }
        return null;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            try (FileReader fr = new FileReader(args[0])) {
                config.load(fr);
            } catch (Exception ex) {
                Logger.getLogger(NagCliCfg.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }
        } else if (new File(System.getProperty("user.home"), ".nagclicfg").exists()) {
            try (FileReader fr = new FileReader(new File(System.getProperty("user.home"), ".nagclicfg"))) {
                config.load(fr);
            } catch (Exception ex) {
                Logger.getLogger(NagCliCfg.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }
        } else {
            config.setProperty("nagios.cache", "/var/cache/nagios3/objects.cache");
            config.setProperty("nagios.binary", "/usr/sbin/nagios3");
            config.setProperty("nagios.config", "/etc/nagios3/nagios.cfg");
            config.setProperty("terminal.width", "100");
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
                                System.err.println("File not found " + config.getProperty("nagios.config") + ": " + f);
                            }
                        } else {
                            System.err.println("Bad directive in " + config.getProperty("nagios.config") + ": " + fnd);
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
                                System.err.println("Directory not found " + config.getProperty("nagios.config") + ": " + dir);
                            }
                        } else {
                            System.err.println("Bad directive in " + config.getProperty("nagios.config") + ": " + fnd);
                        }
                    }
                }
            }
            for (NagItem itm : cfg.all) {
                itm.collectChildren();
            }
            System.out.println("Nagios command-line configurator.");
            System.out.println("Objects loaded from " + config.getProperty("nagios.cache"));
            System.out.println("Type 'help' for some assistance.");
            cfg.cli();
        } catch (IOException ex) {
            Logger.getLogger(NagCliCfg.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    private Types dir = null;
    private NagItem item = null;
    private final Stack<NagItem> stack = new Stack<>();

    private void cli() throws IOException {
        try (BufferedReader bfr = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.print(getPath() + "> ");
            System.out.flush();
            for (String cmd = bfr.readLine(); cmd != null; cmd = bfr.readLine()) {
                cmd = cmd.trim();
                if (cmd.equals("help")) {
                    System.out.println("add: add a value to the current object (see also 'set').");
                    System.out.println("cd: move around in the configuration, use [ls] for suggestions.");
                    System.out.println("check: run 'nagios -v config_file' (do this after write!)");
                    System.out.println("clone: Clones the current object.");
                    System.out.println("rm: Delete the current object.");
                    System.out.println("find: find any named object or group.");
                    System.out.println("help: you are reading it.");
                    System.out.println("ls: list the current object or group.");
                    System.out.println("    -l (long) show more data (1 item per line)");
                    System.out.println("    -r (refs) also show data from referrals");
                    System.out.println("    -s (sort) sort the output");
                    System.out.println("    -d (dns, implies -l) attempt to resolve the 'address' field (may be slow)");
                    System.out.println("quit, exit or ^D: exit the program.");
                    System.out.println("set: set a value in the current object to a new value (see also 'add').");
                    System.out.println("write: write the entire config to /tmp/nagios.big (one file).");
                } else if (cmd.equals("quit") || cmd.equals("exit")) {
                    break;
                } else if (cmd.equals("clone")) {
                    if (item == null) {
                        System.out.println("Nothing to clone, cd to an object first.");
                    } else {
                        cloneObject(bfr);
                    }
                } else if (cmd.equals("rm")) {
                    if (item == null) {
                        System.out.println("Nothing to delete, cd to an object first.");
                    } else {
                        delete(bfr);
                    }
                } else if (cmd.startsWith("cd")) {
                    cd(cmd.substring(2).trim());
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
                    for (NagItem e : all){
                        if (e.getName().toLowerCase().contains(arg)){
                            System.out.println("/"+e.getType().toString()+"/"+e.getName());
                        }
                    }
                } else if (cmd.startsWith("set")) {
                    set(cmd.substring(3).trim(), true);
                } else if (cmd.startsWith("add")) {
                    set(cmd.substring(3).trim(), false);
                } else if (cmd.startsWith("write")) {
                    write(bfr);
                } else if (!cmd.isEmpty()) {
                    System.out.println("Unknown cmd '" + cmd + "'");
                    System.out.println("... want to implement it?");
                    System.out.println("git clone https://github.com/Walter-Stroebel/NagCliCfg.git");
                }
                System.out.print(getPath() + "> ");
                System.out.flush();
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
                } else if (c == 'd') {
                    oDNS = true;
                    oLong = true;
                } else {
                    System.out.println("Unknown option " + c + " for ls");
                    return;
                }
            }
        }
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
            ArrayList<NagItem> col = nagDb.get(dir);
            if (col != null) {
                for (NagItem e : col) {
                    grid.add(new String[]{e.getName(), e.getNameField().equals("name") ? "generic" : "regular"});
                }
            }
        } else {
            if (oRecr) {
                for (Map.Entry<String, String> e : item.getAllFields().entrySet()) {
                    if (oDNS && e.getKey().equals("address")) {
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
                    if (oDNS && e.getKey().equals("address")) {
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
            if (oRecr) {
                for (NagPointer c : item.children) {
                    grid.add(new String[]{c.key, "--> " + c.item.getName()});
                }
            }
        }
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
                        System.out.print(sep);
                        sep = " ";
                        System.out.format(fmt, grid.get(i + j)[0]);
                    }
                    System.out.println();
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
                    System.out.print(sb.reverse());
                    String v = e[1];
                    while (true) {
                        if (v.length() > m1) {
                            System.out.println(v.substring(0, m1));
                            v = v.substring(m1);
                            System.out.print(rep);
                        } else {
                            System.out.println(v);
                            break;
                        }
                    }
                }
            }
        } else {
            System.out.println("Nothing found to list?" + grid);
        }
    }
    public static int TERMiNAL_WIDTH = 100;

    private void set(String nvp, boolean ifExists) {
        String[] two = splitNVP(nvp);
        String key = two[0];
        String val = two[1];
        if (item == null) {
            System.out.println("No current item, cd to one first");
        } else {
            if (key.equals(item.getNameField())) {
                rename(item.get(key), val);
                System.out.println("Changing '" + key + "' from '" + item.get(key) + "' to '" + val + "'");
                item.put(key, val);
            } else if (ifExists && item.containsKey(key)) {
                System.out.println("Changing '" + key + "' from '" + item.get(key) + "' to '" + val + "'");
                item.put(key, val);
            } else if (!ifExists && !item.containsKey(key)) {
                System.out.println("Adding '" + key + "' as '" + val + "'");
                item.put(key, val);
            } else if (item.containsKey(key)) {
                System.out.println("Not adding '" + key + "' as '" + val + "'; item already exists. Use [set key value] instead.");
            } else {
                System.out.println("Not setting '" + key + "' to '" + val + "'; not an existing item. Use [add key value] instead.");
            }
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
    private void rename(String from, String to) {
        for (NagItem e1 : all) {
            for (NagPointer e2 : e1.children) {
                if (e2.item.getName().equals(from)) {
                    System.out.print("Old name is refered to in field '" + e2.key + "' in object '" + e1.getType() + "." + e1.getName() + "'");
                    String[] split = e1.get(e2.key).split(",");
                    String sep = "";
                    StringBuilder rep = new StringBuilder();
                    for (String sp : split) {
                        rep.append(sep);
                        sep = ",";
                        if (sp.equals(from)) {
                            rep.append(to);
                        } else {
                            rep.append(sp);
                        }
                    }
                    System.out.println(": Replacing '" + e1.get(e2.key) + "' with '" + rep + "'");
                    e1.put(e2.key, rep.toString());
                }
            }
        }
    }

    /**
     * Write this configuration to the Nagios configuration directory.
     */
    private void write(BufferedReader bfr) throws IOException {
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
            System.out.println("Warning! This might restructure your current Nagios configuration completely!");
            System.out.println("The following directive will be disabled in " + config.getProperty("nagios.config") + ":");
            for (File f : files) {
                if (!f.getName().startsWith("ngcli_")) {
                    if (f.isDirectory()) {
                        System.out.println("cfg_dir=" + f.getAbsolutePath());
                    } else {
                        System.out.println("cfg_file=" + f.getAbsolutePath());
                    }
                }
            }
            System.out.println("They will be replaced with one directive: cfg_dir=nagclicfg.d");
            System.out.println("In that directory, files will be created for each object type.");
            System.out.println("You can return to your old setup by inverting those modifications.");
            System.out.println("Are you sure you want to do this (eg. you *HAVE* a backup)?");
            System.out.print("Type YES to continue: ");
            String yes = bfr.readLine();
            if (yes == null) {
                System.exit(0);
            }
            if (!"YES".equals(yes)) {
                return;
            }
            if (!cfgDir.exists()) {
                if (!cfgDir.mkdir()) {
                    System.err.println("Failed to create " + cfgDir.getAbsolutePath() + " ... not root?");
                    System.err.println("Write aborted (nothing was changed).");
                }
            }
            boolean didDelete = nagBak.delete();
            if (!nagCfg.renameTo(nagBak)) {
                System.err.println("Failed to backup old " + config.getProperty("nagios.config") + " ... not root?");
                if (!didDelete) {
                    System.err.println("Write aborted (nothing was changed).");
                } else {
                    System.err.println("Deleted old backup file " + nagBak.getAbsolutePath() + "; nothing else was changed.");
                }
                return;
            }
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
        for (Map.Entry<Types, ArrayList<NagItem>> cfg : nagDb.entrySet()) {
            try (PrintWriter out = new PrintWriter(new File(cfgDir, cfg.getKey().toString() + ".cfg"))) {
                for (NagItem e : cfg.getValue()) {
                    out.println("define " + e.getType().toString() + " {");
                    for (Map.Entry<String, String> e2 : e.entrySet()) {
                        out.println("\t" + e2.getKey() + "\t" + e2.getValue());
                    }
                    out.println("}");
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Change Directory.
     *
     * @param path Where to go.
     */
    private void cd(String path) {
        String oldPath = getPath();
        if (!path.startsWith("/")) {
            path = getPath() + "/" + path;
        }
        dir = null;
        item = null;
        stack.clear();
        StringTokenizer toker = new StringTokenizer(path, "/");
        while (toker.hasMoreTokens()) {
            String part = toker.nextToken();
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
                        if (part.endsWith(t.toString())) {
                            dir = t;
                            break;
                        }
                    }
                    if (dir == null) {
                        cdError(part, oldPath);
                        return;
                    }
                } else if (item == null) {
                    for (NagItem e : nagDb.get(dir)) {
                        if (part.endsWith(e.getName())) {
                            item = e;
                            break;
                        }
                    }
                    if (item == null) {
                        cdError(part, oldPath);
                        return;
                    }
                } else {
                    NagItem nItem = null;
                    for (NagPointer e2 : item.children) {
                        if (part.endsWith(e2.item.getName())) {
                            nItem = e2.item;
                            break;
                        }
                    }
                    if (nItem == null) {
                        cdError(part, oldPath);
                        return;
                    } else {
                        stack.push(item);
                        item = nItem;
                    }
                }
            }
        }
    }

    /**
     * Just prints an error message and returns to the path before an attempted
     * cd.
     *
     * @param part Path part that was not found.
     * @param oldPath Original path.
     */
    private void cdError(String part, String oldPath) {
        System.out.println("Not found: '" + part + "' in " + getPath());
        cd(oldPath);
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
                //System.out.println(s);
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
                    ArrayList<NagItem> items = nagDb.get(what);
                    if (items == null) {
                        nagDb.put(what, items = new ArrayList<>());
                    }
                    items.add(itm);
                    all.add(itm);
                    for (String s2 = bfr.readLine(); s2 != null; s2 = bfr.readLine()) {
                        s2 = s2.trim();
                        if (s2.startsWith("#")) {
                            continue;
                        }
                        if (s2.isEmpty()) {
                            continue;
                        }
                        if (s2.equals("}")) {
                            if (itm.getNameField() == null || itm.getName() == null) {
                                System.err.println("Fatal error for " + itm + "; cannot handle unnamed items.");
                                System.exit(1);
                            }
                            break;
                        }
                        //System.out.println(s2);
                        String[] parts = splitNVP(s2);
                        if (parts.length != 2) {
                            System.out.println(Arrays.toString(parts));
                            System.exit(1);
                        } else {
                            itm.put(parts[0], parts[1]);
                        }
                    }
                    if (itm.getName() == null || itm.getName().isEmpty()) {
                        System.err.println("Invalid object found: " + itm);
                        System.exit(1);
                    }
                } else {
                    System.out.println(s);
                    break;
                }
            }
        }
    }
    
    private void cloneObject(BufferedReader bfr) throws IOException {
        NagItem ni = NagItem.construct(this, item.getType());
        if (item.getNameField().equals("name")) {
            // kewl, cloning a generic object
            ni.put("use", item.getName());
            ni.remove("name");
        } else {
            // ah, cloning an existing item
            ni.putAll(item);
            ni.remove(item.getNameField());
        }
        System.out.print("Enter a new name: ");
        String name = bfr.readLine();
        if (name == null) {
            // user pressed ^D I guess.
            System.exit(0);
        }
        // blech, this is hacky
        if (item.type == Types.service) {
            ni.put("service_description", name);
        } else if (item.type == Types.hostextinfo) {
            ni.put("hostgroup_name", name);
        } else {
            ni.put(item.type.toString() + "_name", name);
        }
        if (ni.getNameField() == null || ni.getName() == null) {
            System.err.println("Fatal error cloning " + ni + "; managed to create a unnamed item.");
            System.exit(1);
        }
        // crash if the below returns null -- major weird stuff going on!
        nagDb.get(item.type).add(ni);
        ni.collectChildren();
        stack.clear();
        item = ni;
        ls("ls -rd");
    }

    private void delete(BufferedReader bfr) {
        System.out.println("Not implemented yet.");
        System.out.println("You would have nuked:");
        ls("ls -rd");
    }
}
