class sshd::config {

  file { '/etc/ssh/sshd_config' :
    content => template('sshd/etc/ssh/sshd_config.erb'),
    owner   => 'root',
    group   => 'root',
    mode    => '0400',
  }

}
