package com.yhfund.fileUpload;/**
 * @author: wangyq
 * @Date: 2020/10/26
 * @Time: 18:53
 * @Description:
 */

/**
 *@ClassNameatest
 *@Description
 *@Author
 *@Date2020/10/26 18:53
 *@Version V1.0
 **/
public class atest {
    public static void main(String[] args) {
        String fileName = "中国南方电网公司企业年金计划—中国工商银行（980899050947、99B881908857）—20100830&20101013.pdf";
        String fileName2 = "中国南方电网公司企业年金计划-中国工商银行（980899050947、99B881908857）-20100830&20101013.pdf";
        System.out.println("1名称小:"+fileName.lastIndexOf("-"));
        System.out.println("1名称大:"+fileName.lastIndexOf("—"));
        System.out.println("2名称小:"+fileName2.lastIndexOf("-"));
        System.out.println("2名称大:"+fileName2.lastIndexOf("—"));

    }
}
