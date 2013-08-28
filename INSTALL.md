# Installing DevTracker

1. Install Java (following three steps [instructions from here](http://simonholywell.com/post/2013/03/install-netbeans-scala-ubuntu.html)]:

        sudo add-apt-repository ppa:webupd8team/java
        sudo apt-get update
        sudo apt-get install oracle-java7-installer

2. Install Scala:

        wget http://scala-lang.org/files/archive/scala-2.10.2.tgz
        tar -xzf scala-2.10.2.tgz
        sudo mv scala-2.10.2 /usr/share/scala

3. Edit `~/.profile` to add the following environment variables:

        export SCALA_HOME=/usr/share/scala
        export PATH=$PATH:$SCALA_HOME/bin

4. Install Neo4J ([instructions from here](http://www.neo4j.org/download/linux)):

        # start root shell
        sudo -s
        # Import our signing key
        wget -O - http://debian.neo4j.org/neotechnology.gpg.key | apt-key add - 
        # Create an Apt sources.list file
        echo 'deb http://debian.neo4j.org/repo stable/' > /etc/apt/sources.list.d/neo4j.list
        # Find out about the files in our repository
        apt-get update
        # Install Neo4j, community edition
        apt-get install neo4j
        # start neo4j server, available at http://localhost:7474 of the target machine
        neo4j start

5. Install MongoDB ([instructions from here](http://docs.mongodb.org/manual/tutorial/install-mongodb-on-ubuntu/)):

        # The Ubuntu package management tool (i.e. dpkg and apt) ensure package consistency and authenticity by requiring that distributors sign packages with GPG keys. Issue the following command to import the MongoDB public GPG Key:
        sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
        # Create a /etc/apt/sources.list.d/mongodb.list file using the following command.
        echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' | sudo tee /etc/apt/sources.list.d/mongodb.list
        # Now issue the following command to reload your repository:
        sudo apt-get update
        # Issue the following command to install the latest stable version of MongoDB:
        sudo apt-get install mongodb-10gen
        # When this command completes, you have successfully installed MongoDB!

5. Log out and log back in again for environment variable changes to take effect.

6. Install Play (Scala web framework) ([instructions from here](http://flummox-engineering.blogspot.co.uk/2012/11/how-to-install-play-framework-ubuntu.html)):

        wget http://downloads.typesafe.com/play/2.1.3/play-2.1.3.zip
        unzip play-2.1.3

6. Run the platform (will install dependencies on first run):

        cd src/platform
        ./start-app.sh
