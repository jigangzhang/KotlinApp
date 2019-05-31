package com.god.seep.weather.aidl;


import android.os.Parcel;
import android.os.Parcelable;

public class FileInfo implements Parcelable {
    private String fileName;
    private long fileSize;
    private long modifyTime;
    private boolean downloading;
    private boolean downloaded;

    public FileInfo(String fileName, long fileSize, long modifyTime, boolean downloading, boolean downloaded) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.modifyTime = modifyTime;
        this.downloading = downloading;
        this.downloaded = downloaded;
    }

    protected FileInfo(Parcel in) {
        fileName = in.readString();
        fileSize = in.readLong();
        modifyTime = in.readLong();
        downloading = in.readByte() != 0;
        downloaded = in.readByte() != 0;
    }

    public static final Creator<FileInfo> CREATOR = new Creator<FileInfo>() {
        @Override
        public FileInfo createFromParcel(Parcel in) {
            return new FileInfo(in);
        }

        @Override
        public FileInfo[] newArray(int size) {
            return new FileInfo[size];
        }
    };

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    public boolean isDownloading() {
        return downloading;
    }

    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(fileName);
        dest.writeLong(fileSize);
        dest.writeLong(modifyTime);
        dest.writeByte((byte) (downloading ? 1 : 0));
        dest.writeByte((byte) (downloaded ? 1 : 0));
    }


}