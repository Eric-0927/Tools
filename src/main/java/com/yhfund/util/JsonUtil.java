/**
 * <p>文件名称: JsonUtil.java</p>
 * <p>文件描述: </p>
 * <p>版权所有: 版权所有(C)2015-2017</p>
 * <p>公    司: 深圳市金证科技股份有限公司</p>
 * <p>内容摘要: </p>
 * <p>其他说明: </p>
 * <p>完成日期：2018年03月08日</p>
 *
 * @version 1.0
 * @author Ren_C
 */
package com.yhfund.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.szkingdom.jros.msf.consts.bean.BaseResult;
import com.szkingdom.jros.msf.consts.bean.ResponseData;
import com.szkingdom.jros.msf.consts.bean.ResponseHead;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * json与对象的转换
 * 将json转换成对象
 *
 * @author Ren_C
 * @version 1.0 2018年03月08日
 * @see
 * @since jisp-parent
 */
public class JsonUtil {
    private static Gson gson = new Gson();
    private static ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object src) {
        if (src == null) {

            return null;
        }
        try {
            return mapper.writeValueAsString(src);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> Object fromJson(String json, Class<T> classOfT) {
        try {
            return mapper.readValue(json, classOfT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gson.fromJson(json, classOfT);
    }

    public static Object fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }
    public static <T> T convertToCustomObject(ResponseData responseData, Class<T> clazz) throws Exception {
        if(responseData != null && responseData.getBody() != null) {
            Object result = responseData.getBody();
            if(BaseResult.class.isAssignableFrom(clazz)) {
                HashMap params = new HashMap();
                if(Map.class.isAssignableFrom(result.getClass())) {
                    params.putAll((Map)result);
                }

                ResponseHead head = responseData.getHead();
                params.put("code", head.getCode());
                params.put("msg", head.getMsg());
                return convertToCustomObject((Object)params, clazz);
            } else {
                return convertToCustomObject(result, clazz);
            }
        } else {
            return null;
        }
    }

    public static <T> T convertToCustomObject(Object result, Class<T> clazz) throws Exception {
        try {
            if(result != null) {
                if(Map.class.isAssignableFrom(clazz)) {
                    return (T) result;
                } else {
                    String e = mapper.writeValueAsString(result);
                    Object obj = mapper.readValue(e, clazz);
                    return (T) obj;
                }
            } else {
                return null;
            }
        } catch (Exception var4) {
            throw new Exception("返回类型转换为自定义类型出现异常。");
        }
    }

}
