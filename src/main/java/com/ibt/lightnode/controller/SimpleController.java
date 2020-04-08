package com.ibt.lightnode.controller;

import com.ibt.lightnode.service.SimpleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.controller
 * @Author: keer
 * @CreateTime: 2020-04-08 22:15
 * @Description: 例子接口
 */
@RestController
@RequestMapping("simple")
public class SimpleController {
    @Autowired
    SimpleService simpleService;

    /**
     * 获得当前块高
     * @return
     */
    @RequestMapping(value = "/blockheight", method = RequestMethod.GET)
    public String blockHeight() {
        return simpleService.getBlockHeight();
    }
}
