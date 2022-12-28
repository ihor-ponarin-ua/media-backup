# Links
- https://www.xda-developers.com/install-adb-windows-macos-linux/
- https://www.baeldung.com/run-shell-command-in-java

How to delete a file that contains special characters in the name using adb tool: Just escape them:
`adb shell rm -f "/storage/self/primary/DCIM/Camera/P80805-132311\(1\)\(1\)\(1\).jpg"`
[source](https://stackoverflow.com/questions/63652867/system-bin-sh-syntax-error-unexpected-when-specifying-a-file-in-adb)