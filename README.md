# vt
 Java implementation of VirusTotal command line interface


## Installation

### Chrome
Installation of Chrome is required.

Go to https://www.google.com/chrome/ if you don't have Chrome installed.


### Chromedriver
Go to chrome://settings/help to check the Chrome version.

Then download Chrome driver from https://chromedriver.chromium.org/downloads with the same version number as your Chrome version.

Example from https://chromedriver.chromium.org/downloads
```
If you are using Chrome version 98, please download ChromeDriver 98.0.4758.48

If you are using Chrome version 97, please download ChromeDriver 97.0.4692.71

If you are using Chrome version 96, please download ChromeDriver 96.0.4664.45
```


### Java
Install Java from https://www.oracle.com/java/technologies/downloads/


### VT
Run `git clone https://github.com/BlazerYoo/vt.git` or [download](https://github.com/BlazerYoo/vt/archive/refs/heads/main.zip) repo.

Open `VT.java` and replace `.\\chromedriver_win32\\chromedriver.exe` on line `357` with the address to the downloaded and unzipped ChromeDriver binary.

Inside the cloned repo, run `./vt -h` for the help menu.

### Help menu
![image](https://user-images.githubusercontent.com/69565038/150281347-9857c224-c97a-4360-b839-25395e5ff948.png)

### License

Read the [AGPL-3.0 License](https://github.com/BlazerYoo/vt/blob/main/LICENSE)