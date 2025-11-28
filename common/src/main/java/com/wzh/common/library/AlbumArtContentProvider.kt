package com.wzh.common.library

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit
import com.bumptech.glide.Glide


const val DOWNLOAD_TIMEOUT_SECONDS = 30L
internal class AlbumArtContentProvider : ContentProvider() {

    companion object {
        private val uriMap = mutableMapOf<Uri, Uri>()
        private const val AUTHORITY = "com.wzh.media3demo.albumart"
        //将输入的 URI 映射到内部对应的 URI
        fun mapUri(uri: Uri): Uri {
            //字符串处理，去掉前面的斜杠并将斜杠替换为冒号
            val path = uri.encodedPath?.substring(1)?.replace('/', ':') ?: return Uri.EMPTY
            val contentUri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)//协议：内容URI
                .authority(AUTHORITY)//内容提供者
                .path(path)
                .build()
            uriMap[contentUri] = uri
            return contentUri
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val context = this.context ?: return null
        val remoteUri = uriMap[uri] ?: throw FileNotFoundException(uri.path)
        val path = requireNotNull(uri.path) { "URI path cannot be null" }
        var file = File(context.cacheDir, path)
        if (!file.exists()) {
            // Use Glide to download the album art.
            val cacheFile = Glide.with(context)
                .asFile()
                .load(remoteUri)
                .submit()//submit() 返回一个 Future，调用 get() 方法来等待下载结果，
                .get(DOWNLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)//设置了超时为 DOWNLOAD_TIMEOUT_SECONDS 秒。
            //一旦下载完成，Glide 会将文件存储到临时缓存中，通过 renameTo() 方法
            // 将缓存文件重命名为符合我们本地路径要求的文件，
            // Rename the file Glide created to match our own scheme.
            cacheFile.renameTo(file)
            //并将 file 变量指向这个缓存文件。
            file = cacheFile
        }
        //通过 ParcelFileDescriptor.open() 打开文件，并指定为只读模式
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }
    override fun delete(
        p0: Uri,
        p1: String?,
        p2: Array<out String?>?
    ): Int = 0

    override fun getType(p0: Uri): String? = null

    override fun insert(p0: Uri, p1: ContentValues?): Uri? = null

    override fun onCreate(): Boolean = true

    override fun query(
        p0: Uri,
        p1: Array<out String?>?,
        p2: String?,
        p3: Array<out String?>?,
        p4: String?
    ): Cursor? = null

    override fun update(
        p0: Uri,
        p1: ContentValues?,
        p2: String?,
        p3: Array<out String?>?
    ) = 0
}
