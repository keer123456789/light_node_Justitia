package com.ibt.lightnode.controller;

import com.ibt.lightnode.pojo.WebResult;
import com.ibt.lightnode.service.TraceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.controller
 * @Author: keer
 * @CreateTime: 2020-05-08 09:24
 * @Description:
 */
@RestController
@RequestMapping("/trace")
public class TraceController {
    @Autowired
    TraceInfoService traceInfoService;

    @RequestMapping(value = "/getTraceInfo/{id}",method = RequestMethod.GET)
    public WebResult getTraceInfo(@PathVariable int id){
        return traceInfoService.getTraceInfoById(id);
    }
}
