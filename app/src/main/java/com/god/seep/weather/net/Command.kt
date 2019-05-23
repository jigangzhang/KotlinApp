package com.god.seep.weather.net

class Command {
    companion object {
        const val STATE_CONNECTING = 0X01
        const val STATE_CONNECTED = 0X02
        const val STATE_DISCONNECT = 0X03
        const val GET_FILE_LIST = 0X04
        const val SHOW_FILE_LIST = 0X05
        const val GET_FILE = 0X06
        const val UPLOAD_FILE = 0X07
        const val PROGRESS = 0X08
        const val GET_ALL_USER = 0x09

        const val FILE_LIST = "command_file_list"
        const val REV_FILE = "command_rev_file："
        const val SEND_FILE = "command_send_file："
        const val DENY = "command_deny"
        const val ACCEPT = "command_accept"
        const val ALL_USER = "command_all_user"
    }
}