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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeMap;
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
            // config.setProperty("nagios.cache", "/var/cache/nagios3/objects.cache");
            config.setProperty("nagios.binary", "/usr/sbin/nagios3");
            config.setProperty("nagios.config", "/etc/nagios3/nagios.cfg");
            config.setProperty("sudo.ask-pass", "/usr/bin/ssh-askpass");
            System.err.println("No configuration found. Below is a sample property file.");
            System.err.println("Adjust as needed and save as " + System.getProperty("user.home") + "/.nagclicfg");
            try {
                config.store(System.err, "");
            } catch (IOException ex) {
                Logger.getLogger(NagCliCfg.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.exit(0);
        }
        try {
            NagCliCfg cfg = new NagCliCfg();
            try (BufferedReader main = new BufferedReader(new FileReader(config.getProperty("nagios.config")))) {
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
                            cfg.read(new File(fnd.substring(idx + 1).trim()));
                        }
                    } else if (fnd.startsWith("cfg_dir")) {
                        int idx = fnd.indexOf('=');
                        if (idx > 0) {
                            File dir = new File(fnd.substring(idx + 1).trim());
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
                    System.out.println("quit, exit or ^D: exit the program.");
                    System.out.println("help: you are reading it.");
                    System.out.println("cd: move around in the configuration, use [ls] for suggestions.");
                    System.out.println("ls: list the current object or group.");
                    System.out.println("sudo_check: run nagios -v config_file using sudo.");
                    System.out.println("check: run nagios -v config_file without sudo (being root already).");
                    System.out.println("find: find any named object or group.");
                    System.out.println("set: set a value in the current object to a new value.");
                    System.out.println("add: add a value to the current object.");
                    System.out.println("write: write the entire config to /tmp/nagios.big (one file).");
                } else if (cmd.equals("quit") || cmd.equals("exit")) {
                    break;
                } else if (cmd.startsWith("cd")) {
                    cd(cmd.substring(2).trim());
                } else if (cmd.equals("sudo_check")) {
                    ProcessBuilder pb = new ProcessBuilder("sudo", "-A", config.getProperty("nagios.binary"), "-v", config.getProperty("nagios.config"));
                    pb.environment().put("SUDO_ASKPASS", config.getProperty("sudo.ask-pass"));
                    pb.redirectErrorStream(true);
                    pb.inheritIO();
                    final Process p = pb.start();
                    try {
                        p.waitFor();
                    } catch (InterruptedException ex) {
                        // no need to wait any longer?
                    }
                } else if (cmd.equals("check")) {
                    ProcessBuilder pb = new ProcessBuilder(config.getProperty("nagios.binary"), "-v", config.getProperty("nagios.config"));
                    pb.redirectErrorStream(true);
                    pb.inheritIO();
                    final Process p = pb.start();
                    try {
                        p.waitFor();
                    } catch (InterruptedException ex) {
                        // no need to wait any longer?
                    }
                } else if (cmd.equals("ls")) {
                    if (dir == null) {
                        for (Types t : Types.values()) {
                            System.out.println(t);
                        }
                    } else if (item == null) {
                        ArrayList<NagItem> col = nagDb.get(dir);
                        if (col != null) {
                            for (NagItem e : col) {
                                System.out.println(e.getName());
                            }
                        }
                    } else {
                        for (Map.Entry<String, String> e : item.entrySet()) {
                            System.out.println(e.getKey() + " " + e.getValue());
                        }
                        for (NagPointer c : item.children) {
                            System.out.println(c.key + " --> " + c.item.getName());
                        }
                    }
                } else if (cmd.startsWith("find ")) {
                    find("/", nagDb, cmd.substring(4).trim());
                } else if (cmd.startsWith("set")) {
                    set(cmd.substring(3).trim(), true);
                } else if (cmd.startsWith("add")) {
                    set(cmd.substring(3).trim(), false);
                } else if (cmd.startsWith("write")) {
                    write();
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
                    System.out.print("Old name is refered to in field '" + e2.key + "' in object '" + e1.getType() + "." + e1.getName()+"'");
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
     * Write this configuration as one big file.
     */
    private void write() {
        try (PrintWriter out = new PrintWriter(new File("/tmp/nagios.big"))) {
            for (NagItem e : all) {
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

    /**
     * Find arg as a part of the name of any child.
     *
     * @param path Current path.
     * @param where Children to search.
     * @param arg String to find.
     */
    private void find(String path, ArrayList<NagItem> where, String arg) {
        for (NagItem e : where) {
            if (e.getName().contains(arg)) {
                System.out.println(path + e.getName());
            }
            findC(path + e.getName() + "/", e.children, arg);
        }
    }

    /**
     * Find arg as a part of the name of any child.
     *
     * @param path Current path.
     * @param where Children to search.
     * @param arg String to find.
     */
    private void findC(String path, ArrayList<NagPointer> where, String arg) {
        for (NagPointer e : where) {
            if (e.item.getName().contains(arg)) {
                System.out.println(path + e.item.getName());
            }
            findC(path + e.item.getName() + "/", e.item.children, arg);
        }
    }

    /**
     * Find arg as a part of the name of any child.
     *
     * @param path Current path.
     * @param where Children to search.
     * @param arg String to find.
     */
    private void find(String path, TreeMap<Types, ArrayList<NagItem>> where, String arg) {
        for (Map.Entry<Types, ArrayList<NagItem>> e : where.entrySet()) {
            find(path + e.getKey().toString() + "/", e.getValue(), arg);
        }
    }
}
