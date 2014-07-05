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
