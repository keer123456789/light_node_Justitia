package com.ibt.lightnode.service.implement;

import com.ibt.lightnode.dao.EventDao;
import com.ibt.lightnode.pojo.WebResult;
import com.ibt.lightnode.service.TraceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.service.implement
 * @Author: keer
 * @CreateTime: 2020-05-08 09:31
 * @Description:
 */
@Component
public class TraceInfoServiceImpl implements TraceInfoService {
    @Autowired
    EventDao eventDao;

    @Override
    public WebResult getTraceInfoById(int id) {
        Map map =eventDao.getEventDataByID(id);
        WebResult webResult=new WebResult();
        webResult.setStatus(WebResult.SUCCESS);
        webResult.setData(map);
        return webResult;
    }
}
