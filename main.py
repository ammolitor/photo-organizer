#!/usr/bin/env python3
"""
find photos and move them around according to date taken
written and tested on an intel mac,
will likely not run "as is" on other platforms
"""
import datetime
import hashlib
import logging
import os
import shlex
import sys

# import pathlib
import subprocess
import exif

logger = logging.getLogger("boto3")
logger.setLevel(logging.INFO)

IMAGE_EXTENSIONS = [
    #'.CR2',
    #'.HEIC',
    ".JPG",
    #'.PNG',
    ".jpg",
]


def get_file_list(folder):
    """
    get all files in the given base folder
    :param folder:
    :return list:
    """
    files_list = []
    for root, folders, files in os.walk(folder):
        for filename in folders + files:
            file_list_item = os.path.join(root, filename)
            if os.path.isfile(file_list_item):
                files_list.append(file_list_item)
    return files_list


def get_file_sha(file_name):
    """
    get the sha256 has of the given file (path)
    iterate over chunks so the whole file is not loaded into memory
    :param file_name:
    :return string:
    """
    hash_sha256 = hashlib.sha256()
    with open(file_name, "rb") as f:
        for chunk in iter(lambda: f.read(4096), b""):
            hash_sha256.update(chunk)
    return hash_sha256.hexdigest()


def get_exif_create_date(file_name):
    """
    fetch the datetime_original exif field, reformat the string, or return None
    :param file_name:
    :return string or None:
    """
    with open(file_name, "rb") as image_file:
        ret_val = None
        image = exif.Image(image_file)
        if image.has_exif:
            dt_orig = image.get("datetime_original")
            if dt_orig is not None:
                if not dt_orig.isspace():
                    ret_val = dt_orig.replace(":", "-").replace(" ", "_")
        return ret_val


def get_file_create_date(file_name):
    """
    given a file (path) fetch the st_birthdate and return a formatted string
    :param file_name:
    :return string:
    """
    stat = os.stat(file_name)
    file_time = stat.st_birthtime
    file_time_readable = datetime.datetime.fromtimestamp(file_time).strftime(
        "%Y-%m-%d_%H-%M-%S"
    )
    return file_time_readable


def get_file_extension(file_name):
    """
    given a file (path), return the file name without the extension
    :param file_name:
    :return:
    """
    file_name, file_extension = os.path.splitext(file_name)
    return file_extension


def move_file(old_name, new_name, delete=False):
    """
    use os rsync command to copy the file and maintain attributes
    if rsync command returns success, delete original file
    :param old_name:
    :param new_name:
    :param delete:
    :return:
    """
    new_path = os.path.dirname(new_name)
    logger.info("COPYING: %s -> %s", old_name, new_name)
    os.makedirs(new_path, exist_ok=True)
    rsync_command = f'rsync -av "{old_name}" "{new_name}"'
    command = shlex.split(rsync_command)
    result = subprocess.run(command, check=False, capture_output=True)
    if delete:
        if result.returncode == 0:
            logger.info("REMOVING: %s", old_name)
            os.remove(old_name)


def organize_files(input_dir, output_dir):
    """
    do all the reorganization work,
    fetch the file list from input_dir
    collect information about the file
    move the file to a derived path under output_dir
    :param input_dir:
    :param output_dir:
    :return:
    """
    files_list = get_file_list(input_dir)
    for file_name in files_list:
        extension = get_file_extension(file_name)
        # basename = pathlib.Path(file_name).stem
        if extension in IMAGE_EXTENSIONS:
            logger.info("PROCESSING: %s", file_name)
            sha = get_file_sha(file_name)
            exif_create_date = get_exif_create_date(file_name)
            create_date = get_file_create_date(file_name)
            if exif_create_date is not None:
                file_date = exif_create_date
            else:
                file_date = create_date
            file_date_parts = file_date.split("-")
            file_year = file_date_parts[0]
            file_month = file_date_parts[1]
            new_file_name = (
                f"{output_dir}/{file_year}/{file_month}/"
                f"{file_date}_{sha}{extension.lower()}"
            )
            # used when exif and file data were bad
            # new_file_name_badexif = f"{output_dir}/{basename}_{sha}{extension.lower()}"
            move_file(file_name, new_file_name, delete=False)


if __name__ == "__main__":
    stdout_handler = logging.StreamHandler(sys.stdout)
    logger.addHandler(stdout_handler)
    in_dir = os.path.join("/", "Users", "ammolitor", "Pictures", "to_be_sorted")
    out_dir = os.path.join("/", "Users", "ammolitor", "Pictures", "photos")
    organize_files(in_dir, out_dir)
