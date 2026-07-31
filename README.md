# auto-BlueArchive-feedback
An automated program capable of automatically sending feedback to the official website of **BlueArchive**

# ⚠️WARNING⚠️
1.This project was made by AI,so just use it with caution.

2.You must use **Chrome** browser to use it.

3.You must keep your Chrome browser set to its original default settings and your monitor resolution at 1920 x 1080, otherwise you will not be able to use this program properly.

# How to use?
Configure the Chrome executable and automation settings in [config.json](config.json). By default, the Chrome path is `C:\Users\Administrator\AppData\Local\Google\Chrome\Bin\chromex.exe`.

If your path is different, update `browser.binary`. Set it to an empty string to let Selenium use the default Chrome installation.

The target URL is intentionally fixed in [FeedbackSubmitter.java](src/main/java/jp/bluearchive/shit/FeedbackSubmitter.java).

`config.json` also contains input file paths, browser wait time, delays, mouse behavior, workflow counts, and all screen coordinates. Invalid or incomplete configuration stops the program before browser automation starts.

If `config.json` does not exist, the program creates it with the default settings on startup. Existing configuration is never overwritten automatically.

# What for?
First of all,you should know that **Blue Archive JP** angered players with new gacha rules. I believe that before long, **BlueArchive CN** and **BlueArchive GL** will also adopt this card-drawing mechanism.Getting the rate-up character resets pity, making dual-unit pulls far more expensive. The devs kept the change after protests.

The players want the official to revoke this modification, so they can only resort to continuously writing feedback to the official. Although it is almost impossible, anything is possible.

This script can automatically open your Chrome browser and send feedback to BlueArchive's official team (provided that you have a sufficient number of email addresses).

# Tip
Press **F2** to pause

You can add multiple email addresses to [emails.txt](emails.txt).Remember, only one email address per line, no spaces.


![example](https://raw.githubusercontent.com/asdkwawjd/auto-BlueArchive-feedback/main/img.png)

The content([content.txt](content.txt)) has already been written, and you can make modifications

# Build
Run `./gradlew fatJar` (`gradlew.bat fatJar` on Windows) to build an executable JAR with all runtime dependencies.

The output is `build/libs/auto-report-ba-1.0-SNAPSHOT-all.jar`. Keep `config.json` in the working directory when running it. The email and content file locations are read from that configuration.

```shell
java -jar build/libs/auto-report-ba-1.0-SNAPSHOT-all.jar
```

## Portable Windows application
JDK 21 must be available at `F:/ZuluJDK/21` on the build machine. Build the portable application with:

```shell
gradlew.bat portableZip
```

The outputs are:

```text
build/jpackage/BlueArchiveFeedback/BlueArchiveFeedback.exe
build/distributions/BlueArchiveFeedback-1.0.0-windows.zip
```

The application directory includes a private Java 21 runtime. It does not need to be installed and does not use a system Java installation. Keep the complete directory together; `config.json`, `emails.txt`, and `content.txt` are read relative to `BlueArchiveFeedback.exe`.
