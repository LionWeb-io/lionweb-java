*Note*: eventually we should drop this module and use the one in LW-Java with the same name.

This module contains facility for testing connections to the LionWeb Repository, as it requires non trivial configuration.

As part of the Functional Tests supported by this library we start both the Postgres database and the LionWeb Repository.

We pick the specific commit of the LionWeb Repository defined by the gradle property `lionwebRepositoryCommitID`.

When starting the repository, we generate the configuration file (`server-config.json`) through Python. This is why we need Python in the docker image running the LionWeb Repository.