package com.anoy.ide.project

import java.io.File

/**
 * Simple file operations for the project tree. These stay independent from the
 * project metadata layer so the editor/tree can operate on any opened folder.
 */
object ProjectFileManager {

    private val forbidden = setOf(".gradle", ".idea", ".git", ".forge")

    fun isValidName(name: String): Boolean =
        name.isNotBlank() && name !in forbidden &&
            !name.contains('/') && !name.contains('\\') && name != "." && name != ".."

    fun createFile(parent: File, name: String): File {
        require(parent.isDirectory) { "Parent is not a directory." }
        require(isValidName(name)) { "Invalid file name: $name" }
        val target = File(parent, name)
        require(!target.exists()) { "A file or folder named '$name' already exists." }
        target.createNewFile()
        return target
    }

    fun createDirectory(parent: File, name: String): File {
        require(parent.isDirectory) { "Parent is not a directory." }
        require(isValidName(name)) { "Invalid file name: $name" }
        val target = File(parent, name)
        require(!target.exists()) { "A file or folder named '$name' already exists." }
        target.mkdirs()
        return target
    }

    fun rename(target: File, newName: String): Boolean {
        require(isValidName(newName)) { "Invalid file name: $newName" }
        val destination = File(target.parentFile, newName)
        if (destination.exists()) return false
        return target.renameTo(destination)
    }

    fun delete(target: File): Boolean =
        if (target.isDirectory) target.deleteRecursively() else target.delete()
}
