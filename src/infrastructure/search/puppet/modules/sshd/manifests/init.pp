class sshd {

  include sshd::package, sshd::config, sshd::service

  Class['sshd::package'] -> Class['sshd::config'] ~> Class['sshd::service']

}
