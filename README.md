# photo-organizer

given a directory, move photos into a more sane structure while removing duplicates

* get sha256sum of file
* get exif data of file
* get date file created (filesystem hook?)
* preserve file create times when moving (rsync -av, etc.)
* move file into directory structure by YYYY/MM/YYYY-MM-DD_HH-MM-SS_sha256sum.extension
* if file has no exif use date created, and no_exif directory root
