#!/bin/bash
chmod +x $1/asadmin
$1/asadmin start-domain
$1/asadmin add-resources $2
$1/asadmin stop-domain
