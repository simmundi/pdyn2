## Setup

### Java  

JDK 11 available from the commandline. To verify:

```
javac -version
```

Should return with something like (only the first 11 is important):
```
javac 11.0.10
```

### DVC

To access dvc you need to use VPN or have a whitelisted IP.

Since dvc configuration contains secret keys to a dedicated S3 bucket
and - for security reasons - the required credentials are not stored in this repository,
they need to be acquired in some other way (email, SMS) from admin.

The credentials consist of the _key id_ and the _secret key_. Once they are known,
they need to be configured in the local config (so that they don't get commited
by accident):

```
dvc remote modify --local icm_s3 access_key_id 'KEY_ID_GOES_HERE'
dvc remote modify --local icm_s3 secret_access_key 'SECRET_ACCESS_KEY_GOES_HERE'  
```

## Steps to build

### blobs

Checkout the large files from DVC:
```
dvc pull
```

### gradle

Gradle is the build system used by the project. It should **not** be installed on the machine, it is bundled
with pdyn2-stack so that building only requires Java.

It _will_, however, take some liberties with your home directory 
(namely, a dir called `.gradle` containing cache and some binaries will appear)
and it _will_ consume some resources (a daemon process called `gradle` will linger on,
to speed up future builds. It can be safely killed.)

To run gradle, use the script `gradlew` (there's a `gradlew.bat` version which works on Windows as well`).

To clean:

```
./gradlew clean
```

To build:

```
./gradlew build
```

### Running

The simplest way to run selected classes project is with GUI.

From shell - it's easy to run main pandemic using gradle (on windows we can use gradlew.bat instead)

```
./gradlew run
```

However, the project has a convenient way to create a complete runnable distribution of pdyn2
(which will run across various linux / unix / windows / macos environments) by executing

```
./gradle installDist
```

A whole distribution (including the dependencies) will be created
in the `pdyn2/build/install/pdyn2` directory, consisting of two directories: `libs`
(containing the compiled Java code) and `bin`, containing the scripts: `pdyn2` and `pdyn2.bat` (separately for windows and unix).

The scripts can be run from anywhere, but they will require `input` and `output` dirs under the current workdir.

Assuming we are on a HPC cluster, have the whole project checked out and Java 11 in path,
we could run it by executing:

```
./gradle installDist
./build/install/rename_me/rename_me
```

(caveat: `input` and `output` directories must be present, large files dvc'ed, social structure created).
