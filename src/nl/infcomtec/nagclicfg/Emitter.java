/*
 * Copyright (c) 2014 by Walter Stroebel and InfComTec.
 * All rights reserved.
 */
package nl.infcomtec.nagclicfg;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author walter
 */
public class Emitter {

    public boolean quiet = false;
    public boolean echo = false;
    public boolean json = false;
    public boolean ans = false;
    public ArrayList<String> args = new ArrayList<>();
    private final StringBuilder err = new StringBuilder();
    public boolean changed = false;

    public Emitter(String[] args) throws IOException {
        boolean opts = true;
        for (String s : args) {
            if (opts && s.equals("-q")) {
                quiet = true;
            } else if (opts && s.equals("-e")) {
                echo = true;
            } else if (opts && s.equals("-j")) {
                json = true;
            } else if (opts && s.equals("--")) {
                opts = false;
            } else if (s.startsWith("-")) {
                System.out.println("Usage: nagclicfg [arguments file] | [arg1[,arg2[,...,argN]]][-q][-e][-j]");
                System.out.println("If there is only one argument and it is a valid file, we enter Ansible module mode.");
                System.out.println("In Ansible mode the options -q and -j are in effect and arguments are read from the");
                System.out.println("file in arg1=value1 arg2=value2 argsN=valueN format.");
                System.out.println("In Ansible mode a few simple commands can be passed as cmd^cmd^cmd where ^ is.");
                System.out.println("replaced with an EOLN.");
                System.out.println("Alternatively, a script (input redirection) can be passed as script=path.");
                System.out.println("The arguments (from either invocation) can be used in commands as |%1%|, |%2%|, ..., |%N%|");
                System.out.println("These options are supported:");
                System.out.println("  -- stop interpreting options.");
                System.out.println("  -e print commands as read, debug mode.");
                System.out.println("  -h this brief help (likewise --help or -anything-unknown).");
                System.out.println("  -j output JSON, not text. Meant for use as a module by a higher application.");
                System.out.println("  -q to suppress printing the prompt and banner.");
                System.exit(0);
            } else {
                if (args.length == 1 && new File(s).exists()) {
                    ans = true;
                    json = true;
                    quiet = true;
                    echo = false;
                    parseAnsible(new File(s));
                } else {
                    this.args.add(s);
                }
            }
        }
    }

    public void print(String s) {
        if (!json) {
            System.out.print(s);
            System.out.flush();
        }
    }

    public void println(String s) {
        if (!json) {
            System.out.println(s);
        }
    }

    public void println() {
        println("");
    }

    private void parseAnsible(File file) throws IOException {
        try (BufferedReader bfr = new BufferedReader(new FileReader(file))) {
            // we should only get one line but, hey, we can handle more ;)
            for (String s = bfr.readLine(); s != null; s = bfr.readLine()) {
                //System.err.println("line=" + s);
                StringTokenizer q = new StringTokenizer(s, "\"\\ ", true);
                StringBuilder a = new StringBuilder();
                boolean qt = false;
                while (q.hasMoreTokens()) {
                    String tok = q.nextToken();
                    //System.err.println("tok=" + tok);
                    if (tok.equals("\\")) {
                        String n = q.nextToken();
                        a.append(n.charAt(0));
                        a.append(n.substring(1));
                    } else if (tok.equals("\"")) {
                        qt = !qt;
                    } else if (qt) {
                        a.append(tok);
                    } else if (tok.equals(" ")) {
                        parseArg(a.toString());
                        a.setLength(0);
                    } else {
                        a.append(tok);
                    }
                }
                if (a.length() > 0) {
                    parseArg(a.toString());
                }
            }
        }
    }

    public void err(Object message) {
        if (json){
            err.append(message);
            err.append('\n');
        } else {
            System.err.println(message);
        }
    }

    public void failed(Object message) {
        if (message instanceof Throwable) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                ((Throwable) message).printStackTrace(pw);
            }
            message = sw.toString();
        }
        err(message);
        if (json) {
            JSONObject o = new JSONObject();
            o.put("failed", true);
            o.put("msg", err.toString());
            System.out.println(o.toString(4));
        } else {
            System.err.println(err);
        }
        System.exit(1);
    }

    private void parseArg(String a) {
        //System.err.println("arg=" + a);
        int idx = a.indexOf('=');
        if (idx < 1) {
            failed("'" + a + "' is not a valid argument; no '=' found");
        } else {
            String s1 = a.substring(0, idx);
            String s2 = a.substring(idx + 1);
            if (s1.startsWith("arg")) {
                int pi = -1;
                try {
                    pi = Integer.parseInt(s1.substring(3));
                } catch (Exception ex) {
                    failed("'" + a + "' is not a valid argument; " + s1.substring(3) + " is not an integer");
                }
                while (pi > args.size()) {
                    args.add("");
                }
                args.set(pi - 1, s2);
            } else if (s1.equals("cmds")) {
                System.setIn(new ByteArrayInputStream(s2.replace('^', '\n').getBytes(StandardCharsets.UTF_8)));
            } else if (s1.equals("script")) {
                try {
                    System.setIn(new FileInputStream(new File(s2)));
                } catch (FileNotFoundException ex) {
                    failed(ex);
                }
            } else {
                failed("'" + a + "' is not known argument");
            }
        }
    }
    public final JSONArray jsonOut = new JSONArray();

    public void exit(int i, NagCliCfg facts) {
        if (i != 0) {
            failed("Program aborted");
        } else if (json) {
            if (ans) {
                JSONObject o = new JSONObject();
                o.put("changed", changed);
                if (err.length() > 0) {
                    o.put("warning", err.toString());
                }
                if (jsonOut.length() == 1 && (jsonOut.get(0) instanceof JSONObject)) {
                    JSONObject jo = (JSONObject) jsonOut.get(0);
                    for (String k : jo.keySet()) {
                        o.put(k, jo.get(k));
                    }
                } else if (jsonOut.length() == 1) {
                    o.put("item", jsonOut.get(0));
                } else if (jsonOut.length() > 0) {
                    o.put("list", jsonOut);
                }
                System.out.println(o.toString(4));
            } else {
                if (jsonOut.length() == 1 && (jsonOut.get(0) instanceof JSONObject)) {
                    System.out.println(((JSONObject) jsonOut.get(0)).toString(4));
                } else if (jsonOut.length() == 1 && (jsonOut.get(0) instanceof JSONArray)) {
                    System.out.println(((JSONArray) jsonOut.get(0)).toString(4));
                } else {
                    System.out.println(jsonOut.toString(4));
                }                
            }
        }
        System.exit(i);
    }
}
