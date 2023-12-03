# photo-organizer

given a directory, move photos into a more sane structure while removing duplicates

* get sha256sum of file
* get exif date of file
* get date file created (filesystem hook?)
* preserve file create times when moving (rsync -av, etc.)
* move file into directory structure by %Y/%m/%Y-%m-%d_%H-%M-%S_sha256sum.extension
* if file has no exif use filesystem date created
