package com.god.seep.weather.entity

import java.io.File


fun generateWeather(): List<String> {
    return listOf(
            "Mon 6/23 - Sunny - 31/17",
            "Tue 6/24 - Foggy - 21/8",
            "Wed 6/25 - Cloudy - 22/17",
            "Thurs 6/26 - Rainy - 18/11",
            "Fri 6/27 - Foggy - 21/10",
            "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
            "Sun 6/29 - Sunny - 20/7"
    )
}

fun generateMenuItem(): List<String> {
    return listOf("删除", "打开", "打开文件位置")
}

fun getMIMEType(file: File): String {
    var type = "text/*"
    val fName = file.name
    //获取后缀名前的分隔符"."在fName中的位置。
    val dotIndex = fName.lastIndexOf(".")
    if (dotIndex < 0)
        return type
    /* 获取文件的后缀名 */
    val fileType = fName.substring(dotIndex, fName.length).toLowerCase()
    if (fileType == null || "" == fileType)
        return type
    //在MIME和文件类型的匹配表中找到对应的MIME类型。
    val mineMap = generateMineMap()
    return mineMap[fileType] ?: type
}

fun generateMineMap(): Map<String, String> {
    return mapOf(
            Pair(".3gp", "video/3gpp"),
            Pair(".apk", "application/vnd.android.package-archive"),
            Pair(".asf", "video/x-ms-asf"),
            Pair(".avi", "video/x-msvideo"),
            Pair(".bin", "application/octet-stream"),
            Pair(".bmp", "image/bmp"),
            Pair(".c", "text/plain"),
            Pair(".class", "application/octet-stream"),
            Pair(".conf", "text/plain"),
            Pair(".cpp", "text/plain"),
            Pair(".doc", "application/msword"),
            Pair(".exe", "application/octet-stream"),
            Pair(".gif", "image/gif"),
            Pair(".gtar", "application/x-gtar"),
            Pair(".gz", "application/x-gzip"),
            Pair(".h", "text/plain"),
            Pair(".htm", "text/html"),
            Pair(".html", "text/html"),
            Pair(".jar", "application/java-archive"),
            Pair(".java", "text/plain"),
            Pair(".jpeg", "image/jpeg"),
            Pair(".jpg", "image/jpeg"),
            Pair(".js", "application/x-javascript"),
            Pair(".log", "text/plain"),
            Pair(".m3u", "audio/x-mpegurl"),
            Pair(".m4a", "audio/mp4a-latm"),
            Pair(".m4b", "audio/mp4a-latm"),
            Pair(".m4p", "audio/mp4a-latm"),
            Pair(".m4u", "video/vnd.mpegurl"),
            Pair(".m4v", "video/x-m4v"),
            Pair(".mov", "video/quicktime"),
            Pair(".mp2", "audio/x-mpeg"),
            Pair(".mp3", "audio/x-mpeg"),
            Pair(".mp4", "video/mp4"),
            Pair(".mpc", "application/vnd.mpohun.certificate"),
            Pair(".mpe", "video/mpeg"),
            Pair(".mpeg", "video/mpeg"),
            Pair(".mpg", "video/mpeg"),
            Pair(".mpg4", "video/mp4"),
            Pair(".mpga", "audio/mpeg"),
            Pair(".msg", "application/vnd.ms-outlook"),
            Pair(".ogg", "audio/ogg"),
            Pair(".pdf", "application/pdf"),
            Pair(".png", "image/png"),
            Pair(".pps", "application/vnd.ms-powerpoint"),
            Pair(".ppt", "application/vnd.ms-powerpoint"),
            Pair(".prop", "text/plain"),
            Pair(".rar", "application/x-rar-compressed"),
            Pair(".rc", "text/plain"),
            Pair(".rmvb", "audio/x-pn-realaudio"),
            Pair(".rtf", "application/rtf"),
            Pair(".sh", "text/plain"),
            Pair(".tar", "application/x-tar"),
            Pair(".tgz", "application/x-compressed"),
            Pair(".txt", "text/plain"),
            Pair(".wav", "audio/x-wav"),
            Pair(".wma", "audio/x-ms-wma"),
            Pair(".wmv", "audio/x-ms-wmv"),
            Pair(".wps", "application/vnd.ms-works"),
            Pair(".xml", "text/plain"),
            Pair(".z", "application/x-compress"),
            Pair(".zip", "application/zip"),
            Pair("", "*/*"))
}