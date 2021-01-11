package com.yhfund.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author jiangb
 * @desc javabean 与 Map<String,object>相互转化
 * @date 2018/3/22
 */
public class Bean2MapUtil {
    private static ObjectMapper mapper = new ObjectMapper().configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);;

    private Bean2MapUtil(){
        throw new UnsupportedOperationException(" 工具类不能实例化！");
    }
    /** map 转 bean 是否忽略大小写 */
    public static void transMap2Bean(Map<String, Object> map, Object obj, boolean isIgnoreCase) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String properTypeName=property.getPropertyType().getName();
                if ("java.util.Map".equalsIgnoreCase(properTypeName)) {
                    continue;
                }
                String key = property.getName();
                String lowKey = key.toLowerCase();
                Object value = null;
                if (isIgnoreCase) {
                    if (map.containsKey(lowKey)) {
                        value = map.get(lowKey);
                    }
                }
                if (null==value&&map.containsKey(key)) {
                    value = map.get(key);
                }
                /** 得到property对应的setter方法 */
                Method setter = property.getWriteMethod();
                if (null != setter) {
                    setter.invoke(obj, value);
                }

            }

        } catch (Exception e) {
            System.out.println("transMap2Bean Error " + e);
        }
        return;

    }

    /**
     * @desc    Bean --> Map
     *          isIgnoreCase  是否转小写
     * @param
     * @return
     */
    public static Map<String, Object> transBean2Map(Object obj, boolean isIgnoreCase) {

        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>(16);
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String properTypeName=property.getPropertyType().getName();
                if ("java.util.Map".equalsIgnoreCase(properTypeName)) {
                    continue;
                }
                String key = property.getName();
                // 过滤class属性
                if (!"class".equals(key)) {
                    // 得到property对应的getter方法
                    Method getter = property.getReadMethod();
                    Object value = getter.invoke(obj);
                    if (isIgnoreCase) {
                        map.put(key.toLowerCase(), value);
                    } else {
                        map.put(key, value);
                    }

                }
            }
        } catch (Exception e) {
            //System.out.println("transBean2Map Error " + e);
        }
        return map;

    }

    /**
     * json 字符串 string
     * 将字符串解析成相应的对象 fromJson  or 将对象Json化  toJson
     *  Gson map转bean
     * 优点 可以处理字段自定义别名 属性名不一致情况 使用@SerializedName
     * 使用GsonBuilder输出null  serializeNulls()方法
     * 输出格式化Gson setPrettyPrinting()
     * .禁止Html字符串转义 .disableHtmlEscaping()
     * @param
     * @return
     */
    public static <T> T getBean(Map reqMap,Class<T> clazz){
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.enableComplexMapKeySerialization().serializeNulls().create();
        T obj= gson.fromJson(reqMap.toString(),clazz);
        return obj;

    }
    public static <T> T getBean(String jsonStr,Class<T> clazz){
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.enableComplexMapKeySerialization().serializeNulls().create();
        T obj= gson.fromJson(jsonStr,clazz);
        return obj;

    }

    /**
     * jakson方式
     * map 实体vo互相转换
     * 优点 可以处理map的值为空的情况
     * @param
     * @return
     */
    public static <T> T convertToBean(Object result, Class<T> clazz) throws Exception {
        try {
            if (result != null) {
                String json = mapper.writeValueAsString(result);//将返回对象转换为json字符串
                T obj = mapper.readValue(json, clazz);
                return obj;
            }
        } catch (Exception e) {
            throw new Exception("返回类型转换为自定义类型出现异常。请检查实体VO是否存在默认的构造函数，已经类注解 @JsonIgnoreProperties(ignoreUnknown = true)");
        }
        return null;
    }
    /**
     * 说明:实体类 如果多余的属性 需要在类上方加入属性 @JsonIgnoreProperties(ignoreUnknown = true) 注解
     * 同时 确保有默认的构造函数
     * @param
     * @return
     */
    public static <T> List<T> convertToListCustomObject(List<Map<String,Object>> responseData, Class<T> clazz) throws Exception {
        try {
            if(responseData != null) {
                // jackson 处理json 允许出现特殊字符和转义符 是指key对应的value中
                mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
                String json = mapper.writeValueAsString(responseData);
                json=json.replaceAll("\\\\","");
                JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, new Class[]{clazz});
                List list = (List)mapper.readValue(json, javaType);
                return list;
            }
            return null;
        } catch (Exception var6) {
            throw new Exception("返回类型转换为List类型出现异常。请检查实体VO是否存在默认的构造函数，已经类注解 @JsonIgnoreProperties(ignoreUnknown = true)");
        }
    }
    /**
     * xml转OBJ
     * @param
     * @return
     */
    public static  <T>T convertXmlToBean(String busiXml,Class<T> classT){
        JAXBContext context = null;
        T obj=null;
        try {
            context = JAXBContext.newInstance(classT);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            obj= (T) unmarshaller.unmarshal(new StringReader(busiXml));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return  obj;
    }

    public static String[] getNullPropertyNames (Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for(PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) {
                emptyNames.add(pd.getName());
            }
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

}
