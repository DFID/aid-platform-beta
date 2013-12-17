rbenv-deps:
  pkg.installed:
    - pkgs:
      - bash
      - git
      - openssl
      - curl
      - make

ruby-2.0.0-p353:
  rbenv.installed:
    - default: True
    - require:
      - pkg: rbenv-deps