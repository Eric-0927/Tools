package com.yhfund.fileUpload;

/**
 * @author jiangb
 * @desc 文件请求数据实体  银华文件系统使用
 * @date 2018/6/9
 */
public class FileRequestVO {
    /**
     * 用Base64转
     */
    private String imageContent;
    /**
     * 图片类型
     */
    private String imageType;
    /**   图片后缀 */
    private String imageSuffix;
    /**
     * 用户唯一码
     */
    private String key;



    public FileRequestVO() {
    }

    public String getImageContent() {
        return imageContent;
    }

    public void setImageContent(String imageContent) {
        this.imageContent = imageContent;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getImageSuffix() {
        return imageSuffix;
    }

    public void setImageSuffix(String imageSuffix) {
        this.imageSuffix = imageSuffix;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
