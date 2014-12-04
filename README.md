Nagios-CLI-Config
=================

Command line configuration of Nagios and Ansible module.

This has swiftly become a kind of "Swiss Army Knife" for Nagios.

What works:

- cd to groups, hosts, services.
- ls the data and/or members.
- Change information with set (eg. set host_name new.name.net) and all references get updated as well.
- Supports generic objects (host, service).
- Clone any object! After cloning, change any needed parameters (eg. alias, address).
- Write the changed configuration to neatly formatted files, one per object type (host.cfg, service.cfg, ...)
- find any named object (by substring) in the entire configuration. 
- Fully configurable for where things live.
- diff with Nagios raw objects.
- export cooked object.
- dump raw object.
- idem-potent creation of object, see the createOrAdd.ncc sample script.
- Functions as an Ansible module.
- Remove an item, also removing it from groups and such pointing to it.
- Rename an item, automatically updating references.
- Low learning curve, fairly obvious commands for system managers.
- Optional JSON output for integration in higher-level applications.
- Import foreign Nagios objects.

Usage: 

    nagclicfg [arguments file] | [arg1[,arg2[,...,argN]]][-q][-e][-j]
    If there is only one argument and it is a valid file, we enter Ansible module mode.
    In Ansible mode the options -q and -j are in effect and arguments are read from the
    file in arg1=value1 arg2=value2 argsN=valueN format.
    In Ansible mode a few simple commands can be passed as cmd^cmd^cmd where ^ is replaced with an EOLN.");
    Alternatively, a script (input redirection) can be passed as script=path.
    The arguments (from either invocation) can be used in commands as |%1%|, |%2%|, ..., |%N%|
    These options are supported:
    -- stop interpreting options.
    -e print commands as read, debug mode.
    -h this brief help (likewise --help or -anything-unknown).
    -j output JSON, not text. Meant for use as a module by a higher application.
    -q to suppress printing the prompt and banner.
