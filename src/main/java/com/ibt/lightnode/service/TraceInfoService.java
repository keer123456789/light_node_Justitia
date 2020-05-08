package com.ibt.lightnode.service;

import com.ibt.lightnode.pojo.WebResult;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.service
 * @Author: keer
 * @CreateTime: 2020-05-08 09:29
 * @Description:
 */
public interface TraceInfoService {

    /**
     * 根据溯源id查询全部的溯信息
     * @param id
     * @return
     */
    WebResult getTraceInfoById(int id);
}
