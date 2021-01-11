package com.yhfund.bo;

/**
 * @author jiangb
 * @desc  以上接口返回均为JSON格式数据，多条数据使用;分割。
 * 如{imageName:1,fileUrl:/s/m/jsajdj939sd.jpg,compressUrl:/m/u/djsaidjaijjie21ejij.jpg; imageName:2,fileUrl:/s/m/jsajdj9wqdsa9sd.jpg,compressUrl:/m/u/dadsad211ejij.jpg;}
 * @date 2018/6/9
 */
public class FileResponseVO {
    private String flag;
    private String messageInfo;
    /**  图片名称  */
    private String imageName;
    /**    原图Url */
    private String fileUrl;

    /**  压缩图Url  */
    private String compressUrl;

    public FileResponseVO() {
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getMessageInfo() {
        return messageInfo;
    }

    public void setMessageInfo(String messageInfo) {
        this.messageInfo = messageInfo;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getBaseUrl() {
        return fileUrl;
    }

    public void setBaseUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getCompressUrl() {
        return compressUrl;
    }

    public void setCompressUrl(String compressUrl) {
        this.compressUrl = compressUrl;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", imageName=").append(imageName);
        sb.append(", fileUrl=").append(fileUrl);
        sb.append("]");
        return sb.toString();
    }
}
