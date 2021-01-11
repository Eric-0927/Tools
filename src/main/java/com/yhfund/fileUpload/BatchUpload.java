package com.yhfund.fileUpload;

import com.alibaba.druid.util.StringUtils;
import com.sale.bo.SysFileAttachment;
import com.sale.bo.SysLogUser;
import com.sale.common.Bean2MapUtil;
import com.sale.common.DateUtil;
import com.sale.common.StringTools;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * @author jiangb
 * @desc 客户开户电子附件批量上传功能
 * @date 2019/4/25
 */
public class BatchUpload {

    private static Logger LOGGER = LoggerFactory.getLogger(BatchUpload.class);

    /**
     * .UP文件后缀
     */
    private static final String UPFILE = ".UP";

    public static final String DEFAULT_CHARSET = "UTF-8";

    public static final String DRIVER = "oracle.jdbc.driver.OracleDriver";

    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("tool");
    private static Properties properties = PropertiesUtils.getProperties(System.getProperty("user.dir") + "/toolsProperties/tool.properties");
    /**
     * 文件路径
     */
    private static String filePath = properties.get("filePath").toString();
    /**
     * 生成临时文件路径
     */
    private static String tempFilePath = properties.get("tempFilePath").toString();
    /**
     * 取最多的条数
     */
    private static int maxSize = Integer.parseInt(properties.get("maxSize").toString());
    /**
     * 获取申请单编号sql
     */
    private static String sql = properties.get("getAppsheetserialnoSql").toString();
    /**
     * 上传文件地址配置
     */
    public static String UPLOAD_BASE_URL = properties.get("UPLOAD_BASE_URL").toString();

    public static String UPLOAD_FILE_URL = properties.get("UPLOAD_FILE_URL").toString();
    /**
     * 文件上传时需要传入的KEY
     */
    private static String FILEUPLOAD_USER_KEY = properties.get("FILEUPLOAD_USER_KEY").toString();

    /**
     * 数据库连接信息
     */
    /**
     * 驱动标识符
     */
    private static String driver = properties.get("driver").toString();
    /**
     * 链接字符串
     */
    private static String url = properties.get("url").toString();
    /**
     * 数据库的用户名及密码
     */
    private static String user_a = properties.get("user_a").toString();
    private static String password_a = properties.get("password_a").toString();
    private static String user_main = properties.get("user_main").toString();
    private static String password_main = properties.get("password_main").toString();
    /**
     * 计算日期参数
     */
    private static final int CALENDARDATE = 5;
    /**
     * 日期向前推移天数
     */
    private static int beforeDateDay = Integer.parseInt("-" + properties.get("beforeDateDay").toString());
    /**
     * 日期向后推移天数
     */
    private static int laterDateDay = Integer.parseInt(properties.get("laterDateDay").toString());

    public static void main(String[] args) {
        LOGGER.info("进入批量上传::sale-tool:main方法");
        LOGGER.info("开始执行批量上传::filePath文件路径:" + filePath);
        LOGGER.info("开始执行批量上传::tempFilePath文件路径:" + tempFilePath);
        // 序列号结尾符
        int ends = 0;
        // 1.遍历指定目录及其子目录下的所有文件
        List<String> fileList = new ArrayList<>();
        try {
            traverseFolder(filePath, fileList);
        } catch (Exception e) {
            LOGGER.error("遍历指定目录及其子目录下的所有文件异常！", e);
            System.exit(0);
        }

        int j = 0;
        for (int i = 0; i < fileList.size(); i++) {
            // 超出最大限制上传数时停止上传，跳出循环
            if (j >= maxSize) {
                break;
            }
            // 2.遍历文件时，若存在同名.UP文件，则不再重复上传附件
            // 排除.UP文件，如果是.UP文件则跳过次循环
            if (!fileList.get(i).endsWith(UPFILE)) {
                boolean isFcUp = isExistUP(fileList, fileList.get(i));
                if (isFcUp) {
                    LOGGER.info(fileList.get(i) + "存在.UP文件");
                    continue;
                }
            } else {
                continue;
            }
            String fileName = null;
            String fundaccoStr = null;
            String requestdateStr = null;
            try {
                // 3.文件名中匹配不到基金账号或申请日期的，则不上传该附件
                fileName = fileList.get(i).substring(fileList.get(i).lastIndexOf("\\") + 1);
                // 基金账号字符串
                fundaccoStr = fileName.substring(fileName.lastIndexOf("（") + 1, fileName.lastIndexOf("）"));
                // 申请日期字符串requestdate
                if (fileName.lastIndexOf("-") > 0) {
                    // 小-
                    requestdateStr = fileName.substring(fileName.lastIndexOf("-") + 1, fileName.lastIndexOf("."));
                } else if (fileName.lastIndexOf("—") > 0) {
                    // 大—
                    requestdateStr = fileName.substring(fileName.lastIndexOf("—") + 1, fileName.lastIndexOf("."));
                } else {
                    LOGGER.error(fileList.get(i) + "文件名规则无法识别！");
                    break;
                }
            } catch (Exception e) {
                LOGGER.error(fileList.get(i) + "文件名规则无法识别！");
                break;
            }
            // 判断文件名中是否匹配到基金账号和申请日期，匹配不到则不上传该文件
            if (null != fundaccoStr && fundaccoStr.length() > 0 && null != requestdateStr && requestdateStr.length() > 0) {
                String[] fundaccoArr = fundaccoStr.split("、");
                String[] requestdateArr = requestdateStr.split("&");
                boolean isUpload = false;
                String begintime;
                String endtime;
                for (String fundacco : fundaccoArr) {
                    for (String requestdate : requestdateArr) {
                        // JDBC查询，入参fundacco和requestdate，如果查询到直接上传，然后break跳出循环
                        // JDBC，远程调用，查询是否有开户申请

                        // 按照需求将日期向前或者向后调整
                        begintime = transDate(requestdate, beforeDateDay);
                        endtime = transDate(requestdate, laterDateDay);

                        Map<String, Object> params = new HashMap<>();
                        params.put("fundacco", fundacco);
                        params.put("requestdate", requestdate);
                        Map<String, String> dataMap = new HashMap<>();
                        dataMap = selectAppsheetserialno(dataMap, fundacco, begintime, endtime);
                        if (!StringUtils.isEmpty(dataMap.get("appsheetserialno"))) {
                            Date now = new Date();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                            String tablename = dateFormat.format(now);

                            if (ends < 99) {
                                ends++;
                            } else {
                                ends = 0;
                            }
                            // 01为节点号
                            String serialno = tablename + String.format("%02d", ends) + "01";

                            // 上传文件
                            boolean upResult = uploadFile(fileList.get(i), dataMap, fileName, serialno);
                            if (upResult) {
                                LOGGER.info("客户开户电子附件批量上传:" + fileList.get(i) + "上传成功");
                                // 判断是否上传成功，成功即生成.UP文件
                                createUpFile(fileList.get(i));
                                // 写系统用户日志
                                // 查询申请单编号，SQL从配置文件中取，获取文件路径，操作员
//                                String getReceiveDateQuery1 = RequestGateUtil.doPostGate("getSerialno", new HashMap<String, Object>());
//                                ResponseDataBO responseDataBO1 = Bean2MapUtil.getBean(getReceiveDateQuery1, ResponseDataBO.class);
//                                String serialno = responseDataBO1.getData().get("serialno").toString();

                                /**  有日志记录需求  */
                                SysLogUser sysLogUser = new SysLogUser();
                                sysLogUser.setSno(serialno);
                                sysLogUser.setOperdate(DateUtil.getNatureDate());
                                sysLogUser.setOpertime(DateUtil.getNowTime());
                                /**  菜单ID,暂时默认一个值  */
                                String g_menuId = "8888";
                                sysLogUser.setMenuid(Long.parseLong(g_menuId));
                                /** 操作内容  */
                                sysLogUser.setContent("客户开户电子附件批量上传, 文件路径为：" + fileList.get(i));
                                // 操作类型  A-新增 U-修改 D-删除 Q-查询
                                sysLogUser.setAction("A");

                                sysLogUser.setAppsheetserialno(dataMap.get("appsheetserialno"));

                                /**  操作员代码  */
                                String g_userId = "0";
                                sysLogUser.setUserid(g_userId);
                                /** 业务类型 业务类型0-账户 1-交易 2-资金 3-设置  */
                                sysLogUser.setOpertype("0");
                                sysLogUser.setAuthuserid("");
                                sysLogUser.setAuthremark("");
                                sysLogUser.setIpaddress("");
                                sysLogUser.setMacaddress("");
                                int result = insertSysLogUser(sysLogUser);
                                if (result > 0) {
                                    j++;
                                    isUpload = true;
                                    LOGGER.info("客户开户电子附件批量上传，写入系统日志成功！文件为：" + fileList.get(i));
                                } else {
                                    LOGGER.info("客户开户电子附件批量上传，写入系统日志失败！文件为：" + fileList.get(i));
                                }
                            } else {
                                LOGGER.info("客户开户电子附件批量上传:" + fileList.get(i) + "上传失败");
                            }
                            break;
                        }
                        if (!isUpload) {
                            LOGGER.info("文件" + fileList.get(i) + "无开户申请，不进行上传");
                        }
                    }
                }
            } else {
                LOGGER.info(fileList.get(i) + "未匹配到基金账号和申请日期，不上传该文件！");
            }
        }
    }

    /**
     * 遍历指定目录及其子目录下的所有文件
     *
     * @param path
     * @param fileList
     */
    private static void traverseFolder(String path, List<String> fileList) {

        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null == files || files.length == 0) {
                // 文件夹是空的
                return;
            } else {
                for (File file2 : files) {
                    if (file2.isDirectory()) {
//                        System.out.println("文件夹:" + file2.getAbsolutePath());
                        traverseFolder(file2.getAbsolutePath(), fileList);
                    } else {
//                        System.out.println("文件:" + file2.getAbsolutePath());
                        fileList.add(file2.getAbsolutePath());
                    }
                }
            }
        } else {
            LOGGER.error(path + "文件路径不存在！");
        }
    }

    private static boolean isExistUP(List<String> fileList, String fileNameIndex) {
        boolean isUp = false;
        for (String s : fileList) {
            if (s.startsWith(fileNameIndex) && s.endsWith(UPFILE)) {
                isUp = true;
                break;
            }
        }
        return isUp;
    }

    private static Map<String, String> selectAppsheetserialno(Map<String, String> dataMap, String fundacco, String begintime, String endtime) {
        String appsheetserialno = null;
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            Class.forName(DRIVER);
            con = getConn();
            pstm = con.prepareStatement(sql);

            pstm.setString(1, fundacco);
            pstm.setString(2, begintime);
            pstm.setString(3, endtime);

            rs = pstm.executeQuery();
            while (rs.next()) {
                // 申请单编号
                dataMap.put("appsheetserialno", rs.getString("appsheetserialno"));
                // 业务代码
                dataMap.put("businesscode", rs.getString("businesscode"));
                // 客户编号
                dataMap.put("custno", rs.getString("custno"));
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs, pstm, con);
        }
        return dataMap;
    }

    /**
     * 上传文件
     *
     * @param fileUrl
     * @return
     */
    private static boolean uploadFile(String fileUrl, Map<String, String> dataMap, String fileName, String serialno) {
        boolean result = false;
        byte[] bytes = file2byte(fileUrl);

        OutputStream out = null;
        try {
            BASE64Encoder encoder = new BASE64Encoder();
            String imageContent = encoder.encode(bytes);

            FileRequestVO fileRequestVO = new FileRequestVO();
            fileRequestVO.setImageContent(imageContent);
            fileRequestVO.setImageSuffix(fileName.substring(fileName.lastIndexOf(".")));
            fileRequestVO.setImageType(fileName.substring(fileName.lastIndexOf(".") + 1));
            fileRequestVO.setKey(FILEUPLOAD_USER_KEY);
            // 生成临时文件
            File file2 = null;
            String filePath = tempFilePath;
            file2 = new File(filePath + File.separator + "upload_temp");
            out = new FileOutputStream(file2);
            //Base64解码
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] decoderBytes = decoder.decodeBuffer(imageContent);
            out.write(decoderBytes);
            out.close();
            String resultString = postUploadFile(UPLOAD_BASE_URL + UPLOAD_FILE_URL, file2, Bean2MapUtil.transBean2Map(fileRequestVO, false), dataMap, fileName, serialno);
            // 如果文件上传成功，删除临时文件
            if ("0".equals(resultString)) {
                DeleteFileUtil.delete(filePath + File.separator + "upload_temp");
                result = true;
            }
//            SaleResult saleResult = ServiceRemoteRibbonUtil.remote(getNodeServiceName(SaleConst.SERVICE_ORDER), InterfaceCodeConst.REMOTEFILEUPLOAD, fileRequestVO, SaleResult.class);
//            if (saleResult.isError()) {
//                LOGGER.error("客户开户电子附件批量上传发生异常");
//                result = false;
//            }
        } catch (Exception e) {
            LOGGER.error("客户开户电子附件批量上传发生异常", e);
        }
        return result;
    }

    public static byte[] file2byte(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    public static void createUpFile(String filePath) {
        filePath = filePath + UPFILE;
        createFile(filePath);
    }

    public static void createFile(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            try {
                createDirectory(path);
                Files.createFile(path);
            } catch (IOException e) {
                LOGGER.error("文件创建失败：", e);
            }
        }
    }

    /**
     * 如果目录不存在，则新建目录（迭代处理）
     *
     * @param path
     * @throws IOException
     */
    public static void createDirectory(Path path) throws IOException {
        if (!Files.exists(path.getParent())) {
            createDirectory(path.getParent());
            try {
                Files.createDirectory(path.getParent());
            } catch (FileAlreadyExistsException e) {
                // 捕获文件已存在异常，防止多线程报错
            }
        }
    }

    private static String postUploadFile(String url, File file, Map<String, Object> params, Map<String, String> dataMap, String fileName, String serialno) {
        String resultString = "";
        // 文件类型（后缀）
        String fileType = params.get("imageType") + "";
        /**
         * 指定最大总连接数
         * 指定每个实例的最大连接数
         */
        HttpClient httpClient = HttpClientBuilder.create().setMaxConnTotal(100).setMaxConnPerRoute(100).build();
        HttpPost httppost = new HttpPost(url);
        FileBody binFileBody = new FileBody(file);

        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        // add the file params
        multipartEntityBuilder.addPart("PDF", binFileBody);
        // 设置上传的其他参数
        if (params != null && params.size() > 0) {
            Set<String> keys = params.keySet();
            for (String key : keys) {
                if (StringTools.isNotEmpty(params.get(key))) {
                    multipartEntityBuilder.addPart(key, new StringBody(params.get(key).toString(), ContentType.TEXT_PLAIN));
                }
            }
        }
        HttpEntity reqEntity = multipartEntityBuilder.build();
        httppost.setEntity(reqEntity);
        try {
            HttpResponse res = httpClient.execute(httppost);
            HttpEntity en = res.getEntity();
            InputStream is = en.getContent();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, DEFAULT_CHARSET));
            StringBuffer sbf = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                sbf.append(line);
            }
            String responseJson = sbf.toString();
            EntityUtils.consume(res.getEntity());

            if (StringTools.isNotEmpty(responseJson)) {
                FileResponseVO fileResponseVO = Bean2MapUtil.getBean(responseJson, FileResponseVO.class);
                if (fileResponseVO.getFlag().equals("0")) {
                    LOGGER.error("文件上传失败,详细信息:" + fileResponseVO.getMessageInfo());
                    resultString = fileResponseVO.getMessageInfo();
                } else {
                    SysFileAttachment sysFileAttachment = new SysFileAttachment();
                    // jdbc 插入文件表，获取申请单编号和文件名，
                    // 需要set的值有filesno，appsheetserialno，userfilename，fileurl，fileextname，filesize，filestatus—1

//                    String getReceiveDateQuery1 = RequestGateUtil.doPostGate("getSerialno", new HashMap<String, Object>());
//                    ResponseDataBO responseDataBO1 = Bean2MapUtil.getBean(getReceiveDateQuery1, ResponseDataBO.class);
//                    String serialno = responseDataBO1.getData().get("serialno").toString();

                    sysFileAttachment.setFilesno(serialno);
                    sysFileAttachment.setCustno(dataMap.get("custno"));
                    sysFileAttachment.setBusinesscode(dataMap.get("businesscode"));
                    sysFileAttachment.setAppsheetserialno(dataMap.get("appsheetserialno"));
                    sysFileAttachment.setUserfilename(fileName);
                    sysFileAttachment.setFileurl(fileResponseVO.getBaseUrl());
                    sysFileAttachment.setFileextname(fileType);
                    sysFileAttachment.setFilesize(file.length() / 1024.00 / 1024.00);
                    sysFileAttachment.setFilestatus("1");
                    // 附件类型，默认给01账户
                    sysFileAttachment.setAttachmenttype("01");
                    sysFileAttachment.setOperdate(DateUtil.getNatureDate());
                    sysFileAttachment.setOpertime(DateUtil.getNowTime());
                    int i = insertSysFileAttachment(sysFileAttachment);
                    if (i > 0) {
                        resultString = "0";
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("HttpClientUtil:httpPost执行http请求发生异常", e);
            return null;
        } finally {
            try {
                httpClient.getConnectionManager().shutdown();
            } catch (Exception ignore) {
            }
        }
        return resultString;
    }

    /**
     * 写入文件管理表
     *
     * @param sysFileAttachment
     */
    private static int insertSysFileAttachment(SysFileAttachment sysFileAttachment) {
        int i = 0;
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            Class.forName(DRIVER);
            con = getConn();
            String sql = "insert into SYS_FILEATTACHMENT (filesno, appsheetserialno, custno, businesscode, userfilename, fileurl, fileextname, filesize, filestatus, attachmenttype, operdate, opertime) values(?,?,?,?,?,?,?,?,?,?,?,?)";
            pstm = con.prepareStatement(sql);

            pstm.setString(1, sysFileAttachment.getFilesno());
            pstm.setString(2, sysFileAttachment.getAppsheetserialno());
            pstm.setString(3, sysFileAttachment.getCustno());
            pstm.setString(4, sysFileAttachment.getBusinesscode());
            pstm.setString(5, sysFileAttachment.getUserfilename());
            pstm.setString(6, sysFileAttachment.getFileurl());
            pstm.setString(7, sysFileAttachment.getFileextname());
            pstm.setDouble(8, sysFileAttachment.getFilesize());
            pstm.setString(9, sysFileAttachment.getFilestatus());
            pstm.setString(10, sysFileAttachment.getAttachmenttype());
            pstm.setString(11, sysFileAttachment.getOperdate());
            pstm.setString(12, sysFileAttachment.getOpertime());

            i = pstm.executeUpdate();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs, pstm, con);
        }
        return i;
    }


    private static int insertSysLogUser(SysLogUser sysLogUser) {
        int i = 0;
        Connection con = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;

        try {
            Class.forName(DRIVER);
            con = getConnMain();
            String sql = " insert into SYS_LOGUSER (sno, operdate, opertime, menuid, content, action, " +
                    " appsheetserialno, userid, opertype, authuserid, authremark, ipaddress, macaddress) " +
                    " values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
            pstm = con.prepareStatement(sql);

            pstm.setString(1, sysLogUser.getSno());
            pstm.setString(2, sysLogUser.getOperdate());
            pstm.setString(3, sysLogUser.getOpertime());
            pstm.setLong(4, sysLogUser.getMenuid());
            pstm.setString(5, sysLogUser.getContent());
            pstm.setString(6, sysLogUser.getAction());
            pstm.setString(7, sysLogUser.getAppsheetserialno());
            pstm.setString(8, sysLogUser.getUserid());
            pstm.setString(9, sysLogUser.getOpertype());
            pstm.setString(10, sysLogUser.getAuthuserid());
            pstm.setString(11, sysLogUser.getAuthremark());
            pstm.setString(12, sysLogUser.getIpaddress());
            pstm.setString(13, sysLogUser.getMacaddress());

            i = pstm.executeUpdate();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs, pstm, con);
        }
        return i;
    }

    private static Connection getConn() {
        Connection con = null;
        try {
            Class.forName(driver); //classLoader,加载对应驱动
            con = DriverManager.getConnection(url, user_a, password_a);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

    private static Connection getConnMain() {
        Connection con = null;
        try {
            Class.forName(driver); //classLoader,加载对应驱动
            con = DriverManager.getConnection(url, user_main, password_main);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

    private static void close(ResultSet rs, PreparedStatement pstm, Connection con) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // 关闭执行通道
        if (pstm != null) {
            try {
                pstm.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // 关闭连接通道
        try {
            if (con != null && (!con.isClosed())) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String transDate(String pamaDate, int dateDay) {
        String resultTime = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            Date date = sdf.parse(pamaDate);
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            calendar.add(CALENDARDATE, dateDay);//把日期往前或者往后增加一天.整数往后推,负数往前移动
            date = calendar.getTime();//这个时间就是日期往后推一天的结果
            resultTime = sdf.format(date); //增加一天后的日期
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return resultTime;
    }
}
