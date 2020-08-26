// ITransportManager.aidl
package com.god.seep.weather.aidl;

import com.god.seep.weather.aidl.FileInfo;
import com.god.seep.weather.aidl.UserInfo;
import com.god.seep.weather.aidl.IStateListener;

/**
*定向 tag 表示了在跨进程通信中数据的流向，其中 in 表示数据只能由客户端流向服务端，
* out 表示数据只能由服务端流向客户端，而 inout 则表示数据可在服务端与客户端之间双向流通
*
* in 为定向 tag 的话表现为服务端将会接收到一个那个对象的完整数据，但是客户端的那个对象不会因为服务端对传参的修改而发生变动；
* out 的话表现为服务端将会接收到那个对象的的空对象，但是在服务端对接收到的空对象有任何修改之后客户端将会同步变动；
* inout 为定向 tag 的情况下，服务端将会接收到客户端传来对象的完整信息，并且客户端将会同步服务端对该对象的任何变动。
* */
//Declare any non-default types here with import statements
interface ITransportManager {

    void connect(String ip);

    int getConnState();

    void getFileList();

    void downFile(in FileInfo info);

    void getUsers();

    void sendFile(String path);

    void registerListener(in IStateListener listener);

    void unregisterListener(in IStateListener listener);
}
