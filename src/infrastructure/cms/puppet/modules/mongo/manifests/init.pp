class mongo {

  package { 'mongodb-server':
    ensure => $ensure,
  }

}
