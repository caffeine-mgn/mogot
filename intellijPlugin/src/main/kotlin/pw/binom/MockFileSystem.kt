package pw.binom

import pw.binom.io.AsyncOutputStream
import pw.binom.io.FileSystem
import pw.binom.io.FileSystemAccess

class MockFileSystem:FileSystem<Unit>{
    override suspend fun get(user: Unit, path: String): FileSystem.Entity<Unit>? = null

    override suspend fun getDir(user: Unit, path: String): Sequence<FileSystem.Entity<Unit>>? = null

    override suspend fun mkdir(user: Unit, path: String): FileSystem.Entity<Unit> {
        throw FileSystemAccess.AccessException.ForbiddenException()
    }

    override suspend fun new(user: Unit, path: String): AsyncOutputStream {
        throw FileSystemAccess.AccessException.ForbiddenException()
    }

}