Nagios-CLI-Config
=================

Command line (sort of) configuration of Nagios.

2014-06-27: Release early, release often.
2014-06-28: Beta version, it works for me!

What works (also see the demo-session):

- cd to groups, hosts, services.
- ls the data and/or members.
- Change information with set (eg. set host_name new.name.net) and all references get updated as well.
- Supports generic objects (host, service).
- Clone any object! After cloning, change any needed parameters (eg. alias, address).
- Write the changed configuration to neatly formatted files, one per object type (host.cfg, service.cfg, ...)
- find any named object (by substring) in the entire configuration. 
- Fully configurable for where things live.

TODO:
=====

- Implement commands like "Link host to service group" and so on.
- Delete stuff.
- Import foreign Nagios objects.