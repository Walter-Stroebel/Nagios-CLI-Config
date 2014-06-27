Nagios-CLI-Config
=================

Command line (sort of) configuration of Nagios.

2014-06-27: Release early, release often.

What works (also see the demo-session):

- cd to groups, hosts, services.
- ls the data and/or members.
- Change information with set (eg. set host_name new.name.net) and watch all references get updated as well. 

TODO:

- Make things configurable, like where Nagios lives.
- Implement commands like "create host", "Link host to service group" and so on.
- Perhaps support setups that are not one huge file?
