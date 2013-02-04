#!/bin/bash

apt-get update && apt-get install -y wget

cd /tmp
wget -nc -nv http://apt.puppetlabs.com/puppetlabs-release-precise.deb
dpkg -i puppetlabs*.deb

apt-get update && apt-get install -y puppet-common

cp -Rv /vagrant/puppet/* /etc/puppet/

puppet apply /etc/puppet/manifests/site.pp
