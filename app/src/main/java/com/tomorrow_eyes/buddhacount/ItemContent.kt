package com.tomorrow_eyes.buddhacount

import android.content.Context
import android.util.Log
import java.io.*
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object ItemContent {
    @JvmField
    val ITEMS: MutableList<CountItem> = ArrayList()
    private const val MaxCOUNT = 10

    init {
        ITEMS.clear()
    }

    private fun addItem(item: CountItem) {
        ITEMS.add(item)
    }

    @JvmStatic
    fun sizeOver(): Boolean {
        return ITEMS.size >= MaxCOUNT
    }

    @JvmStatic
    fun insertItemUpdateList(item: CountItem) {
        if (sizeOver()) {   // 己超長,合併最後記錄
            val len = ITEMS.size
            val item2 = ITEMS[len - 2]
            val item1 = ITEMS[len - 1]
            var content = "列表外總計："
            if (item2.content == item1.content) content = item1.content else {
                content = if (item2.count > item1.count) item2.content else item1.content
                if (content.length > 7) content = content.substring(0, 7)
                content += ".."
            }
            ITEMS.removeAt(len - 1)
            ITEMS.removeAt(len - 2)
            addItem(CountItem(item1.id, content,
                    item1.count + item2.count, item2.mark))
        }
        ITEMS.add(0, item)
    }

    @JvmStatic
    val bytes: ByteArray
        get() {
            val sb = StringBuilder()
            for (i in ITEMS.indices) {
                val ci = ITEMS[i]
                sb.append(String.format("'%s', %d, %tF%n", ci.content, ci.count, ci.mark))
            }
            return sb.toString().toByteArray(StandardCharsets.UTF_8)
        }

    @JvmStatic
    fun writeToFile(context: Context) {
        val fileName = context.getExternalFilesDir(null).toString() + "/" + context.getString(R.string.filename_list)
        val stream: FileOutputStream
        try {
            val buf = bytes
            stream = FileOutputStream(fileName, false)
            stream.write(buf)
            stream.close()
        } catch (e: IOException) {
            Log.d("writeToFile: ", e.message!!)
        }
    }

    private fun addItem(id: Int, line: String): Boolean {
        // System.out.println(line);
        try {
            val split = line.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val idStr = id.toString()
            val content = split[0].substring(1, split[0].length - 1)
            val count = split[1].trim { it <= ' ' }.toInt()
            val mark = LocalDate.parse(split[2].trim { it <= ' ' }, DateTimeFormatter.ISO_LOCAL_DATE)
            val item = CountItem(idStr, content, count, mark)
            ITEMS.add(item)
        } catch (e: Exception) {
            Log.d("addItem: ", e.message!!)
            return false
        }
        return true
    }

    @JvmStatic
    @Throws(IOException::class)
    fun streamToItems(stream: InputStream?): Boolean {
        val inputStreamReader = InputStreamReader(stream, StandardCharsets.UTF_8)
        val reader = BufferedReader(inputStreamReader)
        ITEMS.clear()
        var line: String
        var flag = true
        for(i in 1..MaxCOUNT+1) {    // 允許比MaxCOUNT多讀一個
            line = reader.readLine() ?: break
            flag = flag && addItem(i, line)
        }
        if (ITEMS.size > 0) ITEMS[ITEMS.size - 1].id = "-" // 最後統計
        inputStreamReader.close()
        return flag
    }

    @JvmStatic
    fun readFromFile(context: Context) {
        val fileName = context.getExternalFilesDir(null).toString() + "/" + context.getString(R.string.filename_list)
        try {
            val stream = FileInputStream(fileName)
            streamToItems(stream)
            stream.close()
        } catch (e: IOException) {
            Log.d("readFromFile: ", e.message!!)
        }
    }

    class CountItem(var id: String, val content: String, val count: Int, val mark: LocalDate) {

        override fun toString(): String {
            return content
        }
    }
}