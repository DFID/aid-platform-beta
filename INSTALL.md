# Installing DevTracker

## Installing the API

1. Install Java (instructions for following three steps [from here](http://simonholywell.com/post/2013/03/install-netbeans-scala-ubuntu.html)):

        sudo add-apt-repository ppa:webupd8team/java
        sudo apt-get update
        sudo apt-get install oracle-java7-installer

2. Install Scala:

        wget http://scala-lang.org/files/archive/scala-2.10.2-RC2.tgz
        tar -xzf scala-2.10.2-RC2.tgz
        sudo mv scala-2.10.2-RC2 /usr/share/scala

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

        wget http://downloads.typesafe.com/play/2.1.0/play-2.1.0.zip
        unzip play-2.1.0
        sudo mv play-2.1.0 /opt
        sudo ln -s /opt/play-2.1.0 /opt/play
        sudo ln -s /opt/play/play /usr/local/bin/play
        # uncomment the following line to confirm everything installed ok
        # play

7. There's a weird dependency issue with play-iteratees_2.10-RC2.

8. Run the platform (will install dependencies on first run):

        cd src/platform
        sudo ./start-app.sh

9. Navigate to `http://localhost:9000` to see the API.

## Create a username and password

1. Change to the scripts folder:

        cd scripts

2. Generate an encrypted password, which will be printed to your console:

        ./bcrypt <password>

3. Take the output of that prompt, which will begin with a $, and create a user (replace `<username>` and `<encryptedpassword>` below)

        mongo
        use dfid
        db.users.insert({'username': '<username>', 'password': '<encryptedpassword>', 'retryCount': 0})

## Installing the front-end

The front-end runs on `middleman`, which is a Ruby gem.

1. Ensure you've got ruby installed. You can run:

        ruby -v

   If you get a response, Ruby is installed. If not, run:

        sudo apt-get install ruby rubygems

2. Change to the site directory:

        cd src/platform/site

3. Install dependencies:

        sudo bundle install

4. Run the server:

        sudo bundle exec middleman server

5. Navigate to:

        http://0.0.0.0:4567/

6. You'll probably get an error, because there's no data loaded. To do that, run:

        http://0.0.0.0:9000/admin

7. Log in with the username and password you created above.
