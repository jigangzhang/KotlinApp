// IStateListener.aidl
package com.god.seep.weather.aidl;

import com.god.seep.weather.aidl.FileInfo;

/**
* 回调接口，由客户端实现，注册到服务端
* */
interface IStateListener {

    void onConnectState(int state);

    void onProgress(String name, int type, int progress);

    void onRevFileList(in List<FileInfo> list);
}
