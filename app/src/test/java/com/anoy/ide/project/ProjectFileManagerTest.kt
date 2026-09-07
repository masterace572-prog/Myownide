package com.anoy.ide.project

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class ProjectFileManagerTest {

    @Test
    fun createsFilesAndDirectories() {
        val root = Files.createTempDirectory("project").toFile()
        val file = ProjectFileManager.createFile(root, "Main.kt")
        val dir = ProjectFileManager.createDirectory(root, "src")
        assertTrue(file.isFile)
        assertTrue(dir.isDirectory)
    }

    @Test
    fun rejectsInvalidNames() {
        val root = Files.createTempDirectory("project").toFile()
        assertFalse(ProjectFileManager.isValidName(""))
        assertFalse(ProjectFileManager.isValidName("a/b"))
        assertFalse(ProjectFileManager.isValidName(".gradle"))
    }

    @Test
    fun renamesAndDeletes() {
        val root = Files.createTempDirectory("project").toFile()
        val file = ProjectFileManager.createFile(root, "old.kt")
        assertTrue(ProjectFileManager.rename(file, "new.kt"))
        assertTrue(File(root, "new.kt").isFile)
        assertTrue(ProjectFileManager.delete(File(root, "new.kt")))
        assertFalse(File(root, "new.kt").exists())
    }
}
