package com.po4yka.trailglass.data.file

import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray
import kotlinx.io.readString

/**
 * Cross-platform file operations using Kotlinx IO.
 * Provides a unified API for file I/O across Android and iOS.
 */
class FileOperations {

    /**
     * Read file contents as a ByteArray.
     *
     * @param path Absolute file path
     * @return ByteArray containing file contents
     * @throws IOException if the file cannot be read
     */
    fun readFileBytes(path: String): ByteArray {
        val filePath = Path(path)
        return SystemFileSystem.source(filePath).buffered().use { source ->
            source.readByteArray()
        }
    }

    /**
     * Read file contents as a String using UTF-8 encoding.
     *
     * @param path Absolute file path
     * @return String containing file contents
     * @throws IOException if the file cannot be read
     */
    fun readFileText(path: String): String {
        val filePath = Path(path)
        return SystemFileSystem.source(filePath).buffered().use { source ->
            source.readString()
        }
    }

    /**
     * Write ByteArray to a file, creating parent directories if needed.
     *
     * @param path Absolute file path
     * @param data ByteArray to write
     * @throws IOException if the file cannot be written
     */
    fun writeFileBytes(
        path: String,
        data: ByteArray
    ) {
        val filePath = Path(path)
        SystemFileSystem.sink(filePath).buffered().use { sink ->
            sink.write(data)
            sink.flush()
        }
    }

    /**
     * Write String to a file using UTF-8 encoding.
     *
     * @param path Absolute file path
     * @param content String content to write
     * @throws IOException if the file cannot be written
     */
    fun writeFileText(
        path: String,
        content: String
    ) {
        val filePath = Path(path)
        SystemFileSystem.sink(filePath).buffered().use { sink ->
            sink.write(content.encodeToByteArray())
            sink.flush()
        }
    }

    /**
     * Append ByteArray to an existing file.
     *
     * @param path Absolute file path
     * @param data ByteArray to append
     * @throws IOException if the file cannot be written
     */
    fun appendFileBytes(
        path: String,
        data: ByteArray
    ) {
        val filePath = Path(path)
        SystemFileSystem.sink(filePath, append = true).buffered().use { sink ->
            sink.write(data)
            sink.flush()
        }
    }

    /**
     * Append String to an existing file using UTF-8 encoding.
     *
     * @param path Absolute file path
     * @param content String content to append
     * @throws IOException if the file cannot be written
     */
    fun appendFileText(
        path: String,
        content: String
    ) {
        val filePath = Path(path)
        SystemFileSystem.sink(filePath, append = true).buffered().use { sink ->
            sink.write(content.encodeToByteArray())
            sink.flush()
        }
    }

    /**
     * Check if a file exists.
     *
     * @param path Absolute file path
     * @return true if file exists, false otherwise
     */
    fun fileExists(path: String): Boolean {
        val filePath = Path(path)
        return SystemFileSystem.exists(filePath)
    }

    /**
     * Delete a file.
     *
     * @param path Absolute file path
     * @throws IOException if the file cannot be deleted
     */
    fun deleteFile(path: String) {
        val filePath = Path(path)
        SystemFileSystem.delete(filePath)
    }

    /**
     * Copy a file from source to destination.
     *
     * @param sourcePath Source file path
     * @param destinationPath Destination file path
     * @throws IOException if the file cannot be copied
     */
    fun copyFile(
        sourcePath: String,
        destinationPath: String
    ) {
        val source = Path(sourcePath)
        val destination = Path(destinationPath)

        SystemFileSystem.source(source).buffered().use { sourceStream ->
            SystemFileSystem.sink(destination).buffered().use { destinationStream ->
                sourceStream.transferTo(destinationStream)
                destinationStream.flush()
            }
        }
    }

    /**
     * Get file size in bytes.
     *
     * @param path Absolute file path
     * @return File size in bytes, or -1 if file doesn't exist
     */
    fun getFileSize(path: String): Long {
        val filePath = Path(path)
        return if (SystemFileSystem.exists(filePath)) {
            SystemFileSystem.metadataOrNull(filePath)?.size ?: -1L
        } else {
            -1L
        }
    }

    /**
     * Create a directory and all necessary parent directories.
     *
     * @param path Directory path
     * @throws IOException if the directory cannot be created
     */
    fun createDirectories(path: String) {
        val dirPath = Path(path)
        SystemFileSystem.createDirectories(dirPath)
    }

    /**
     * Read a file in chunks using a buffer.
     * Useful for processing large files without loading them entirely into memory.
     *
     * @param path Absolute file path
     * @param chunkSize Size of each chunk in bytes
     * @param processor Function to process each chunk
     */
    fun readFileChunked(
        path: String,
        chunkSize: Int = 8192,
        processor: (ByteArray) -> Unit
    ) {
        val filePath = Path(path)
        SystemFileSystem.source(filePath).buffered().use { source ->
            val buffer = Buffer()
            while (!source.exhausted()) {
                val bytesRead = source.readAtMostTo(buffer, chunkSize.toLong())
                if (bytesRead > 0) {
                    val chunk = buffer.readByteArray()
                    processor(chunk)
                }
            }
        }
    }
}
