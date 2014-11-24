SRW MX Portable Script Extractor by Dashman
-----------------------------------

This program allows you to split and merge the files inside MAP_ADD.BIN for SRW MX Portable, which contains (amongst other things) the stage script files of the game. You'll need to have Java installed in your computer to operate this.


How to extract:

1) Extract MAP_ADD.BIN from the ISO and place it in the same folder as the application (script_extract.jar).
2) In a command / shell window, execute this:

java -jar script_extract.jar -e

* Alternatively, use script_extract.bat to perform this. The BAT file has to be in the same folder where the BIN and the executable are.

3) This will extract 200+ SRWL files in <extract_folder>, along with a couple of BIN files and a files.list file. The SRWL files are the ones containing the script of the stages, use the proper tool to edit those.


How to insert:

1) Put the program inside <extract_folder> along with the generated files and its corresponding LIST file. Make sure all the files have the same name they had when they were extracted (for example, if you edited a file and saved it as 0005-edit.SRWL, rename it now as 0005.SRWL if you want it to be included).

2) Execute

java -jar script_extract.jar -i

* Alternatively, use reinsert.bat to do this.

3) This will generate a new MAP_ADD.BIN file in <extract_folder>. You can replace the one in the ISO for this.


IMPORTANT NOTES:

* Keep backups!

* When reinserting, make sure you don't have already a MAP_ADD.BIN file already in the folder. If by any chance your generated file is smaller than the old one, the new file might have issues.

* MAP_ADD_End.BIN contains TX48 textures.