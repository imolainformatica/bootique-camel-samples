# TUBO

'tubo' is a simple application to send the content of a local folder to a remote receiver.
The same application can act both as a `transmitter` and `receiver`, running more instances of each.

## `transmitter`
The application can be configured with a list of `transmitter`.
Each `transmitter` monitors a folder (set using the 'localFolder' attribute in the configuration); when a file is added to 'localFolder', it is sent to the remote endpoint configured for that `transmitter`.
When a file is successfully sent to the remote endpoint ('targetUrl') it is moved to the 'archiveFolder'; if the transmission raises an error instead, the file is moved to the 'errorFolder'.

## `recevier`
The application can also run multiple instances of `receiver`.
The `receiver` is configured to write all the files it receives in the 'targetFolder'.


## Configuration

### Location and relative paths
In order to let the application find the configuration file ('tubo.yml'), the full path of the file must be set in an environment parameter called `CONFIGURATION_FOLDER`.
If the configuration contains relative paths, this are resolved relative to the location of the configuration file.