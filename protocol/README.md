# xFx Protocol

The client opens a connection with the server and _informs_ the server whether it wants to _download_ or _upload_ a file using a _header_. The client can also get the _list_ of all the files shareable by the server, and _resume_ the file download where it stopped in case there was a network connectivity issue.

## Download

If the client wants to download a file, then the header will be as the following:

- **download[one space][file name][Line Feed]**

Upon receiving this header, the server searches for the specified file.

- If the file is not found, then the server shall reply with a header as the following:

  - **NOT[one space]FOUND[Line Feed]**

- If the file is found, then the server shall reply with a header as the following:
  - **OK[one space][file size][one space][last modified time][Line Feed]**

The client then checks if it already has the file already downloaded and with the same last modified time.

- If the file is found with the same modified time, the download is skipped. The client informs the server with a header: -**NO[one space]DOWNLOAD[Line Feed]**

- If the file is not found, OR the modified time is different, then the client informs the server with the following header: -**DOWNLOAD[Line Feed]**

The server proceeds to send the file's bytes to the client.

## Resume Download

If a network connectivitiy issue interrupted the file download, and the client wants to resume the download from where it stopped, then the header will be as the following:

- **resume[one space][file name][one space][file position][Line Feed]**

Upon receiving this header, the server searches for the specified file.

- If the file is not found, then the server shall reply with a header as the following:
  - **NOT[one space]FOUND[Line Feed]**
- If the file is found, then the server shall reply
  - with a header as the following:
    - **OK[one space][remaining file size][Line Feed]**
  - followed by the remaining bytes of the file that were not previously downloaded.

## Upload

If the client wants to upload a file, then the header will be as the following:

- **upload[one space][file name][one space][file size][Line Feed]**

After sending the header, the client shall send the bytes of the file

## Get list of files

If the client wants to get a list of all the files shareable by the server, then the header will be as the following:

- **list[Line Feed]**

Upon receiving the header, the server searches for the shareable files.

- If there are no shareable files, then the server shall reply with a header as the following:
  - **NO[one space]FILES[Line Feed]**
- If there is one or more files available, then the server shall reply:
  - with a header as the following:
    - **OK[one space][number of files][one space]FILES[one space][files list size][Line Feed]**
  - followed by the list of files following this format:
    - **[file name][one space][file size][Line Feed]**
