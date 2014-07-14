Explain
=======

Gives a short text, like this one, with more information on a command.
Note that the file **(nagios config directory)/nagclicfg.md** must exist
for this to work.

It should also give some usage examples, like this:

    /> explain explain
    Gives a short text, like this one, with more information on a command.
    It should also give some usage examples, like this:

*(stopping here, documentation can get to be so recursive...;)*

* * * * *

Import
======

Takes a file or a directory as argument and adds any **new** objects it
finds in there. Obviously the file or directory is expected to contain
valid Nagios object definitions.

    /> import /var/cache/nagios3/objects.cache
    /command/check-host-alive: already defined.
    /command/check-mysql: added.

*... and so on.*

* * * * *

Replace
=======

Takes a file or a directory as argument and adds or **replaces** any
objects it finds in there. Obviously the file or directory is expected
to contain valid Nagios object definitions.

    /> replace /var/log/nagios/objects.cache         `
    /command/check-host-alive: replaced.
    /command/check-mysql: added.

*... and so on.*

* * * * *

Tree
====

Prints a tree of your Nagios configuration, leaving out most of the
details. Still, it gets to be a fairly long list!

    /> tree

*(long list of command definitions and sensitive data omitted)...*

    servicegroup
     +-- DNS
     | +-- members ->  dns,Check DNS
     +-- all
    timeperiod
     +-- 24x7
     +-- never
     +-- nonworkhours
     +-- workhours
    hostextinfo
     +-- centos_machines
     | +-- hostgroup_name ->  centos_machines
     +-- debian-machines
     | +-- hostgroup_name ->  debian-machines
     +-- phones
     | +-- hostgroup_name ->  phones
    />
        

* * * * *

Add
===

Takes two arguments: the field to add and its value.

If the field is multi-value, this works identically to **set**.

If the field already exists this will do nothing and generate a warning.

    /host/linux-server> clone
    Enter a new value for name: new.machine

*(output omitted)*

    /host/new.machine> add host_name new.machine.loc
    Adding 'host_name' as 'new.machine.loc'
    /host/new.machine.loc> add host_name new.machine.loc
    Not adding 'host_name' as 'new.machine.loc'; item already exists. Use [set key value] instead.
    /host/new.machine.loc>         `

See also **ifadd** and **set**.

* * * * *

Set
===

Takes two arguments: the field to set and its value.

If the field does not already exists this will do nothing and will generate a
warning, unless the field is a multi-value field like **hostgroups**.
Multi-value fields are simply added to, if the value was not already in the
set.

Note that **add** and **set** behave identically for multi-value fields.

    /> cd host/localhost
    /host/localhost> ls
    address    alias      host_name  use       
    /host/localhost> set address 1.2.3.4
    Changing 'address' from '127.0.0.1' to '1.2.3.4'
    /host/localhost> rm address
    Removing 'address'
    /host/localhost> set address 127.0.0.1
    Not setting 'address' to '127.0.0.1'; not an existing item. Use [add key value] instead.
    /host/localhost> add address 127.0.0.1
    Adding 'address' as '127.0.0.1'
    /host/localhost> set host_name thismachine
    Updated members from a10,a20,localhost,virtmachine to a10,a20,thismachine,virtmachine
    Updated members from localhost,linksys,snom320 to linksys,snom320,thismachine
    Updated members from localhost to thismachine
    Updated parents from localhost to thismachine
    Changing 'host_name' from 'localhost' to 'thismachine'
    /host/this-machine> 

See also **ifset** and **add**.

* * * * *

cd
==

Takes an optional argument that should be a relative or absolute
selector of an object or a category.

If you omit the argument a list is printed you can select from.

Finally, you can add an asterisk (\*) to the argument to select the
first matching object or category.

    /> cd /host/lin*
    /host/linux-server> cd /
    /> cd
    0: command
    1: contact
    2: contactgroup
    3: host
    4: hostgroup
    5: service
    6: servicegroup
    7: timeperiod
    8: hostextinfo
    Pick one: 4
    /hostgroup>

See also **ifcd**.

* * * * *

Check
=====

Runs the Nagios pre-flight check. *You should really read some of the
Nagios documentation if this means nothing to you.*

Note that NagCliCfg has build-in checks as well, you should get some
messages if your configuration is inconsistent. This will not catch all
cases however, run **check** to be sure.

    /> check

    Nagios Core 3.5.1
    Copyright (c) 2009-2011 Nagios Core Development Team and Community Contributors
    Copyright (c) 1999-2009 Ethan Galstad
    Last Modified: 08-30-2013
    License: GPL

    Website: http://www.nagios.org
    Reading configuration data...
       Read main config file okay...
    Processing object config directory '/etc/nagios3/nagclicfg.d'...
    Processing object config file '/etc/nagios3/nagclicfg.d/hostextinfo.cfg'...
    Processing object config file '/etc/nagios3/nagclicfg.d/service.cfg'...
    Processing object config file '/etc/nagios3/nagclicfg.d/timeperiod.cfg'...
    Processing object config file '/etc/nagios3/nagclicfg.d/hostgroup.cfg'...
    Processing object config file '/etc/nagios3/nagclicfg.d/contact.cfg'...
    Processing object config file '/etc/nagios3/nagclicfg.d/host.cfg'...
    Processing object config file '/etc/nagios3/nagclicfg.d/contactgroup.cfg'...
    Processing object config file '/etc/nagios3/nagclicfg.d/servicegroup.cfg'...
    Processing object config file '/etc/nagios3/nagclicfg.d/command.cfg'...
       Read object config files okay...

    Running pre-flight check on configuration data...

    Checking services...
            Checked 26 services.
    Checking hosts...
            Checked 19 hosts.
    Checking host groups...
            Checked 8 host groups.
    Checking service groups...
            Checked 2 service groups.
    Checking contacts...
            Checked 2 contacts.
    Checking contact groups...
            Checked 1 contact groups.
    Checking service escalations...
            Checked 0 service escalations.
    Checking service dependencies...
            Checked 0 service dependencies.
    Checking host escalations...
            Checked 0 host escalations.
    Checking host dependencies...
            Checked 0 host dependencies.
    Checking commands...
            Checked 154 commands.
    Checking time periods...
            Checked 4 time periods.
    Checking for circular paths between hosts...
    Checking for circular host and service dependencies...
    Checking global event handlers...
    Checking obsessive compulsive processor commands...
    Checking misc settings...

    Total Warnings: 0
    Total Errors:   0

    Things look okay - No serious problems were detected during the pre-flight check
    /> 

* * * * *

clone
=====

You should enter this when having selected an object or template first.
You will be prompted for the fields naming the current object so you can
name your new clone. Note that if you are cloning a template (a generic
object), it will be named by a field "name". Unless you are creating a
new template, you should remove and add some fields to instantiate a
concrete object.

    /service/generic-service> clone
    Enter a new value for name: anything as we name this by hand
    active_checks_enabled. . . . 1
    alias. . . . . . . . . . . . 24 Hours A Day, 7 Days A Week
    check_freshness. . . . . . . 0
    check_period . . . . . . . . 24x7
    contact_groups . . . . . . . admins
    contactgroup_name. . . . . . admins
    event_handler_enabled. . . . 1
    failure_prediction_enabled . 1
    flap_detection_enabled . . . 1
    friday . . . . . . . . . . . 00:00-24:00
    is_volatile. . . . . . . . . 0
    max_check_attempts . . . . . 4
    members. . . . . . . . . . . root
    monday . . . . . . . . . . . 00:00-24:00
    name . . . . . . . . . . . . anything as we name this by hand
    normal_check_interval. . . . 5
    notification_interval. . . . 0
    notification_options . . . . w,u,c,r
    notification_period. . . . . 24x7
    notifications_enabled. . . . 1
    obsess_over_service. . . . . 1
    parallelize_check. . . . . . 1
    passive_checks_enabled . . . 1
    process_perf_data. . . . . . 1
    register . . . . . . . . . . 0
    retain_nonstatus_information 1
    retain_status_information. . 1
    retry_check_interval . . . . 1
    saturday . . . . . . . . . . 00:00-24:00
    sunday . . . . . . . . . . . 00:00-24:00
    thursday . . . . . . . . . . 00:00-24:00
    timeperiod_name. . . . . . . 24x7
    tuesday. . . . . . . . . . . 00:00-24:00
    wednesday. . . . . . . . . . 00:00-24:00
    check_period . . . . . . . . --> 24x7
    contact_groups . . . . . . . --> admins
    notification_period. . . . . --> 24x7
    /service/anything as we name this by hand> add host_name localhost
    Adding 'host_name' as 'localhost'
    /service/anything as we name this by hand> add service_description new_service
    Adding 'service_description' as 'new_service'
    /service/localhost,new_service> rm name
    Removing 'name'
    /service/localhost,new_service> rm register
    Removing 'register'
    /service/localhost,new_service> add alias Our shiny new service
    Adding 'alias' as 'Our shiny new service'
    /service/localhost,new_service> add check_command check_shininess
    Adding 'check_command' as 'check_shininess'
    /service/localhost,new_service> 

* * * * *

Diff
====

You should enter this when having selected an object or template first.
If the object exists in both the configuration as you have currently
loaded (and possibly altered) and the configuration of the running
Nagios (taken from the objects.cache file), this will do a side-by-side
diff of the two. Note that some differences are to be expected as Nagios
fills out more details than NagCliCfg does.

    /> cd host/lo*
    /host/localhost> diff
    define host {                                                                           define host {
            active_checks_enabled   1                                                     <
            address 127.0.0.1                                                                       address 127.0.0.1
            alias   localhost                                                                       alias   localhost
            check_command   check-host-alive                                                        check_command   check-host-alive
            check_freshness 0                                                             <
            check_interval  5.000000                                                      <
            contact_groups  admins                                                                  contact_groups  admins
            event_handler_enabled   1                                                               event_handler_enabled   1
            failure_prediction_enabled      1                                                       failure_prediction_enabled      1
            first_notification_delay        0.000000                                      <
            flap_detection_enabled  1                                                               flap_detection_enabled  1
            flap_detection_options  o,d,u                                                 <
            freshness_threshold     0                                                     <
            high_flap_threshold     0.000000                                              <
            host_name       localhost                                                               host_name       localhost
            icon_image      base/debian.png                                               <
            icon_image_alt  Debian GNU/Linux                                              <
            initial_state   o                                                             <
            low_flap_threshold      0.000000                                              <
            max_check_attempts      10                                                              max_check_attempts      10
            notes   Debian GNU/Linux servers                                              |         name    generic-host
            notification_interval   0.000000                                              |         notification_interval   0
            notification_options    d,u,r                                                           notification_options    d,u,r
            notification_period     24x7                                                            notification_period     24x7
            notifications_enabled   1                                                               notifications_enabled   1
            obsess_over_host        1                                                     <
            passive_checks_enabled  1                                                     <
            process_perf_data       1                                                               process_perf_data       1
            retain_nonstatus_information    1                                                       retain_nonstatus_information    1
            retain_status_information       1                                                       retain_status_information       1
            retry_interval  1.000000                                                      <
            stalking_options        n                                                     <
            statusmap_image base/debian.gd2                                               <
            vrml_image      debian.png                                                    <
    }                                                                                       }
    /host/localhost> 

* * * * *

Dump
====

You should enter this when having selected an object or template first.
If the object exists in the configuration of the running Nagios (taken
from the objects.cache file), this will show the full definition.

See also **Export**.

    /> cd
    0: command
    1: contact
    2: contactgroup
    3: host
    4: hostgroup
    5: service
    6: servicegroup
    7: timeperiod
    8: hostextinfo
    Pick one: 3
    /host> cd localhost
    /host/localhost> dump
    define host {
            active_checks_enabled   1
            address 127.0.0.1
            alias   localhost
            check_command   check-host-alive
            check_freshness 0
            check_interval  5.000000
            contact_groups  admins
            event_handler_enabled   1
            failure_prediction_enabled      1
            first_notification_delay        0.000000
            flap_detection_enabled  1
            flap_detection_options  o,d,u
            freshness_threshold     0
            high_flap_threshold     0.000000
            host_name       localhost
            icon_image      base/debian.png
            icon_image_alt  Debian GNU/Linux
            initial_state   o
            low_flap_threshold      0.000000
            max_check_attempts      10
            notes   Debian GNU/Linux servers
            notification_interval   0.000000
            notification_options    d,u,r
            notification_period     24x7
            notifications_enabled   1
            obsess_over_host        1
            passive_checks_enabled  1
            process_perf_data       1
            retain_nonstatus_information    1
            retain_status_information       1
            retry_interval  1.000000
            stalking_options        n
            statusmap_image base/debian.gd2
            vrml_image      debian.png
    }
    /host/localhost> 

* * * * *

Export
======

You should enter this when having selected an object or template first.
If the object exists in the currently loaded configuration, this will
show the definition.

See also **Dump**.

    /> cd host/localhost
    /host/localhost> export
    define host {
            address 127.0.0.1
            alias   localhost
            host_name       localhost
            use     generic-host
    }
    /host/localhost>

* * * * *

Echo
====

Just prints its arguments. Mostly makes sense in scripts.

    /> ifcd /host/localhost
    /host/localhost> echo it is there!
    it is there!
    /host/localhost> else
    /host/localhost> echo No localhost found.
    /host/localhost> fi
    /host/localhost> 

* * * * *

Else
====

Part of the ifxxx [else] fi constructs.

    /> ifcd /host/localhost
    /host/localhost> echo it is there!
    it is there!
    /host/localhost> else
    /host/localhost> echo No localhost found.
    /host/localhost> fi
    /host/localhost> 

* * * * *

fi
==

Part of the ifxxx [else] fi constructs.

    /> ifcd /host/localhost
    /host/localhost> echo it is there!
    it is there!
    /host/localhost> else
    /host/localhost> echo No localhost found.
    /host/localhost> fi
    /host/localhost> 

* * * * *

Find
====

Does a search of the argument, listing partial matches as well.

    /> find local
    /host/localhost
    /host/virtmachine: parents ->  localhost
    /hostgroup/debian-machines: members ->  localhost
    /hostgroup/http-servers: members ->  localhost
    /hostgroup/nagios-servers: members ->  localhost
    /hostgroup/ssh-servers: members ->  localhost
    /> 

* * * * *

Help
====

Prints the in-program help.

This does not need any external file, contrary to **explain** which
needs the nagclicfg.md file available in the Nagios configuration
directory.

* * * * *

ifadd
=====

The conditional version of **add**, can be followed by **else** and must
be terminated by **fi**.

If the field is a multi-value field like **hostgroups** the value will be
added if the value was not already in the set, returning **true**.

If a multi-value field already contained the value, **ifadd** will return
false.

Note that **ifadd** and **ifset** behave identically for multi-value fields.

* * * * *

ifset
=====

The conditional version of **set**, can be followed by **else** and must
be terminated by **fi**.

If the field is a multi-value field like **hostgroups** the value will be
added if the value was not already in the set, returning **true**.

If a multi-value field already contained the value, **ifset** will return
false.

Note that **ifadd** and **ifset** behave identically for multi-value fields.

* * * * *

ifcd
====

The conditional version of **cd**, can be followed by **else** and must
be terminated by **fi**.

* * * * *

ifrm
====

The conditional version of **rm**, can be followed by **else** and must
be terminated by **fi**.

* * * * *

ifrmdir
=======

The conditional version of **rmdir**, can be followed by **else** and
must be terminated by **fi**.

* * * * *

ls
==

Lists the current object or group

This command can take these options:

-   -l (long) show more data (1 item per line)
-   -r (refs, implies -l) also show data from referrals
-   -s (sort) sort the output
-   -d (dns, implies -l) attempt to resolve the 'address' field. This
    might take a rather long time if the address cannot be resolved.

The following shows all the options:

    /host/localhost> ls
    address    alias      host_name  use       
    /host/localhost> ls -l
    address . 127.0.0.1
    alias . . localhost
    host_name localhost
    use . . . generic-host
    /host/localhost> ls -r 
    address. . . . . . . . . . . 127.0.0.1
    alias. . . . . . . . . . . . localhost
    check_command. . . . . . . . check-host-alive
    contact_groups . . . . . . . admins
    event_handler_enabled. . . . 1
    failure_prediction_enabled . 1
    flap_detection_enabled . . . 1
    host_name. . . . . . . . . . localhost
    max_check_attempts . . . . . 10
    name . . . . . . . . . . . . generic-host
    notification_interval. . . . 0
    notification_options . . . . d,u,r
    notification_period. . . . . 24x7
    notifications_enabled. . . . 1
    process_perf_data. . . . . . 1
    retain_nonstatus_information 1
    retain_status_information. . 1
    use. . . . . . . . . . . . . --> generic-host
    /host/localhost> ls -rd
    address. . . . . . . . . . . 127.0.0.1 Addr=127.0.0.1 (localhost)
    alias. . . . . . . . . . . . localhost
    check_command. . . . . . . . check-host-alive
    contact_groups . . . . . . . admins
    event_handler_enabled. . . . 1
    failure_prediction_enabled . 1
    flap_detection_enabled . . . 1
    host_name. . . . . . . . . . localhost
    max_check_attempts . . . . . 10
    name . . . . . . . . . . . . generic-host
    notification_interval. . . . 0
    notification_options . . . . d,u,r
    notification_period. . . . . 24x7
    notifications_enabled. . . . 1
    process_perf_data. . . . . . 1
    retain_nonstatus_information 1
    retain_status_information. . 1
    use. . . . . . . . . . . . . --> generic-host
    /host/localhost> 

* * * * *

mv
==

At the time of this writing this only works for services. More on this
command later.

* * * * *

pwd
===

As you can **cd** quite deep down object referals, this command shows
you the current absolute path.

    /host/localhost> cd gen*
    /host/localhost/generic-host> cd ch*    
    /host/localhost/generic-host/check-host-alive> pwd
    /command/check-host-alive
    /host/localhost/generic-host/check-host-alive> 

* * * * *

Quit
====

Like pressing \^D, exits the program. Unsaved changes (see also
**write**) will be silently discarded.

* * * * *

Exit
====

Like pressing \^D, exits the program. Unsaved changes (see also
**write**) will be silently discarded.

* * * * *

Reload
======

Simple executes the configured reload command. Assumes you have
sufficient privileges and that you wrote something to reload. See also
**write** and **check**.

* * * * *

rm
==

Removes a field from the current object or a value from a multi-value field.
In the second case you must supply the value to remove, as in
**rm parents localhost** to remove **localhost** from the list of **parents**.

    /> cd host/localhost
    /host/localhost> rm host_name
    Sorry, cannot remove/clear a field naming an object.
    /host/localhost> rm use
    Removing 'use'
    /host/localhost> ls
    address    alias      host_name 
    /host/localhost> ls -l
    address . 127.0.0.1
    alias . . localhost
    host_name localhost
    /host/localhost> 

* * * * *

rmdir
=====

Removes the entire current object! This also recursively removes the
object from anything that has a referral to it, possibly breaking those
definitions.

    /host/localhost> rmdir
    Removed reference via 'members' from '/hostgroup/debian-machines'
    Removed reference via 'members' from '/hostgroup/http-servers'
    Removed reference via 'members' from '/hostgroup/nagios-servers'
    Removed reference via 'members' from '/hostgroup/ssh-servers'
    Removed reference via 'parents' from '/host/virtmachine'
    /host> 

* * * * *

Write
=====

Writes the current in-memory configuration out to files in the Nagios
configuration directory, in a directory that should be private to this
tool (nagclicfg.d). Note that this unlinks any configuration that was in
place before, like from a package install, your current setup or another
tool.

**You will be warned before this happens!**

However, this change should be fairly safe and restoring your old
configuration should be simple. The old nagios.cfg file is also backed
up so it should be a simple matter of replacing nagios.cfg as updated by
this tool with that backup file.

**There are however no guarantees! Make a backup or live on the edge,
your choice :)**

* * * * *

Firstboot
=========

For use in deployment tasks, does the above write without asking
confirmation.

* * * * *

Define
======

A simple command that prompts for all the required fields for an object,
as specified by the Nagios manual. Not recommended for complex objects
but very handy for creating a hostgroup, for instance.

    /hostgroup> define
    Enter hostgroup_name: new-group
    Enter alias: My new group
    /hostgroup/new-group>

* * * * *

rmrf/
=====

This incredibly dangerous-seeming command deletes all host, service,
hostgroup and servicegroup objects. If you are using hostextinfo this
will probably print warnings about those objects having missing references.

Obviously you should type **quit** immediately after this command unless
you are doing a full idempotent reinstall of your Nagios environment, for
which purpose this command is intended.

In normal use it would be in some kind of script and be followed by an
**import** or **replace** command. Alternatively the script can have a
list of **clone** or **define** commands to recreate the desired configuration.

The advantage of this approach is that you remove any old entries. It is
also useful after an initial install by a package manager of the Nagios
system to get rid of all the stock objects.
 
* * * * *
