# Filius - Simple Network Simulation
Filius is a network simulator for educational purpose

## License
Filius is provided under the terms of GPLv2 or GPLv3.

## Requirements
Filius is a Java application. A Java Runtime Environment 8 or higher is required. Support of Java 8 is deprecated and will end soon.

For most features, JRE will do. The feature to implement and run your own applications (by means of the Software Wizard) requires the Java Development Kit.

## OS Support
Windows, Linux

## Available Languages
German, French, English

## Configuration
The personal configuration, i.e. selected language as well as the UI state when leaving the program, is stored within the personal home directory within a folder named ".filius".

Global configurations can be modified in the install directory within "filius.ini". For more details see the documented parameters in this file.

## Installation

### Install Filius on Windows

1. Download the Windows installer `Filius-Setup_with-JRE-<version>.exe` from the [Filius website](https://www.lernsoftware-filius.de/). This package includes a Java runtime, so no separate Java installation is needed.
2. Close any running Filius instance, start the installer and allow administrator access when prompted. Filius is installed for all users.
3. Follow the welcome and license pages, then choose the installation folder. The default is `C:\Program Files\Filius`; an existing installation's folder may be proposed instead.
4. Click **Install**, wait for the installation to complete, then click **Finish**.
5. Start Filius from the **Filius** folder in the Windows Start menu.

The installer installs the application, bundled runtime and documentation together. It creates Start menu shortcuts for Filius, the German and English manuals, project information and the website, and associates `.fls` files with Filius. There is no component selection or option to disable these shortcuts or the file association. The installer includes English, German and French translations; it does not display a language selection dialog.

### Command-Line Options

The Windows installer uses NSIS and supports the following case-sensitive options:

| Option | Effect |
| --- | --- |
| `/S` | Runs installation or uninstallation silently, without wizard pages or confirmation dialogs. Administrator privileges are still required. |
| `/D=<absolute path>` | Sets the installation folder. Must be the last argument, with no quotes around the path, even when it contains spaces. |
| `/NCRC` | Skips the installer's integrity check. Normally this option is unnecessary. |

For example, run these commands from a Windows Command Prompt in the download folder, replacing `2.14.0` with the downloaded version:

```bat
Filius-Setup_with-JRE-2.14.0.exe /S
Filius-Setup_with-JRE-2.14.0.exe /D=C:\Applications\Filius
Filius-Setup_with-JRE-2.14.0.exe /S /D=C:\Educational Software\Filius
```

The first command installs silently in the default folder, the second opens the wizard with a custom folder, and the third combines both options. For unattended installation, run the command from an elevated Command Prompt to avoid an administrator access prompt.

See the [NSIS command-line documentation](https://nsis.sourceforge.io/Docs/Chapter3.html#installerusage) for details.

### Uninstall Filius

Use the Filius uninstall entry in Windows, or run `uninst.exe` from the installation folder and confirm removal. For silent removal from the default folder, run:

```bat
"C:\Program Files\Filius\uninst.exe" /S
```

Adjust the path if you installed Filius elsewhere. Uninstallation removes the entire installation folder, the Start menu shortcuts and the `.fls` association. Save your own project files outside the installation folder. The personal configuration in your home directory's `.filius` folder is retained.

## Website
https://www.lernsoftware-filius.de/

## Code Signing
This program uses free code signing provided by [SignPath.io](https://signpath.io?utm_source=foundation&utm_medium=gitlab&utm_campaign=filius), and a free code signing certificate by the [SignPath Foundation](https://signpath.org?utm_source=foundation&utm_medium=gitlab&utm_campaign=filius)

# Contribute / Build Filius

## Development Environment

Filius is developed with Eclipse using Maven as a build tool.

Currently, the build works with Java up to version 13 since some unit tests cannot be executed with the newest Java versions. In other words: It is possible to develop with a newer version but there will be some tests that fail.

## Bundled JRE
Some Filius distributions are shipped with JRE to remove the external dependency on a pre-installed Java Runtime Environment. But since Filius is not built as a Java module, the JRE cannot be built as part of the build process with Maven.

The runtime is created in two steps:
1. Define required Java modules
2. Create the runtime

### Define Required Modules
Use the jdeps tool that is shipped with OpenJDK:

```
jdeps -recursive --ignore-missing-deps --multi-release base --print-module-deps filius.jar lib\*
```

The output contains some warnings and the following required modules:

```
java.base,java.compiler,java.desktop,java.instrument,java.management,java.naming,java.net.http,java.prefs,java.sql,java.xml.crypto,jdk.jfr,jdk.unsupported,jdk.xml.dom
```

Additionally, the following runtime dependencies are required in order to use the Software Assistant for application compilation at runtime in Filius:
- jdk.compiler - This module contains the compiler tool set.
- jdk.zipfs - This module is required to read dependencies on built-in Filius classes from filius.jar.

### Build Runtime
The bundled JRE (if available) is built with the following command:

```
jlink --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.base,java.compiler,java.desktop,java.instrument,java.management,java.naming,java.net.http,java.prefs,java.sql,java.xml.crypto,jdk.jfr,jdk.unsupported,jdk.xml.dom,jdk.compiler,jdk.zipfs --output java-runtime
```

## Generate Documentation
Source code is documented with Javadoc. Generate the API documentation with Maven:

```
mvn javadoc:javadoc
```
The documentation will be stored in the default output directory (target) in 'site/apidocs' as HTML pages.
