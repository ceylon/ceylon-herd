# How to run the Ceylon Herd Module Repository

Note: at the moment Herd requires Play 1.2 which does not work on Java 8, so proceed with Java 7. We
are going to move it to Play 1.3 as soon as possible to fix that.

1. [Download Play Framework 1.2.7.2](https://downloads.typesafe.com/play/1.2.7.2/play-1.2.7.2.zip) and install it
1. Clone the [Herd repository](https://github.com/ceylon/ceylon-herd)
1. Open a shell and go to the `ceylon-herd` directory you just cloned
1. Run `play dependencies` to setup the required modules
    - Note: the `play` script requires Python 2, but runs on `/usr/bin/env python`,
      which may default to Python 3 depending on your system.
      If you get an error like this:

      ```
        File "/tmp/play-1.2.7.2/play", line 50
          print r"~        _            _ "
                                          ^
      SyntaxError: Missing parentheses in call to 'print'
      ```
      then change the first line of the script to
      ```python
      #!/usr/bin/env python2
      ```
    - Note 2: This will pretend to fail due to not being able to find `db` but
      in fact they are already there. This is due to Play apparently having removed their Maven
      repo online. Those two modules have been added to our Git repo so we have them, but this
      step is still necessary in order to set up the `secure` module which comes from the Play
      distribution itself. Ignore the two errors and move on: it should work.
1. Create your Postgres DB
    1. `sudo su - postgres`
    1. `createuser -PSRD ceylon-herd`
    1. _enter `ceylon-herd` as password when prompted_
    1. `createdb -O ceylon-herd -E utf8 ceylon-herd`
    1. exit
1. Run the application
    1. `play run`

# How to make your user (if registration is disabled, which is the default for now)

This can only be done by hand for now:

1. Pick a (dummy temporary) password
1. Hash it with BCrypt (for example, online at http://bcrypthashgenerator.apphb.com or other locations)
    1. Copy the resulting BCrypt Hash
    1. Don't worry about giving your password online, just pick a dummy temporary password and you can
       change it later in Herd.
1. Open a `psql` console to your database:
    1. `psql -h localhost -U ceylon-herd`
1. Add your user (as admin)
    1. `INSERT INTO user_table  (id, email, firstname, admin, lastname, isbcrypt, password, status, username) VALUES ((select nextval('hibernate_sequence')), 'email@example.org', 'FirstName', true, 'LastName', true, 'YOUR_BCRYPT_HASH', 'REGISTERED', 'UserName');`
1. You can now log in and change your password using the UI

# How to make your user admin (if registration is enabled)

This can only be done by hand for now:

1. Register your user at http://localhost:9000/register
1. See in the logs in the console what your activation link is, and follow it (in DEV mode no mail is sent)
1. Complete your registration
1. Open a `psql` console to your database:
    1. `psql -h localhost -U ceylon-herd`
1. Set yourself as admin
    1. `UPDATE user_table SET admin = true WHERE username = 'your-user-name';`

# Updating the DB to a new version

Just run the `query-db-version.sh` command to see if and how to update

# Troubleshooting

1. If you get the error `FATAL: Ident authentication failed for user "ceylon-herd"` look [here for a possible solution](http://www.cyberciti.biz/faq/psql-fatal-ident-authentication-failed-for-user/)

# Setting up Herd in Eclipse

1. Create a project using Play by going to the `ceylon-herd` project and running:
    1. `play eclipsify`
1. Import the resulting project into Eclipse

This also creates some useful launch configurations in the `eclipse`
folder that you can use to run the application within Eclipse.
But unfortunately the configurations are not 100% correct, so to make
things work you need to go into the properties of each configuration and
add the following to the `VM Arguments` section: `-XX:-UseSplitVerifier -Dfile.encoding=utf-8 -XX:CompileCommand=exclude,jregex/Pretokenizer,next`
    
# License

The content of this repository is released under ASL2 as provided in
the LICENSE file that accompanied this code.

