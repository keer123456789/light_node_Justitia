package com.ibt.lightnode.service.implement;

import com.ibt.lightnode.dao.LevelDbTemplete;
import com.ibt.lightnode.service.SimpleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.service.implement
 * @Author: keer
 * @CreateTime: 2020-04-08 22:18
 * @Description: 例子接口实现
 */
@Service
public class SimpleServiceImp implements SimpleService {
    @Autowired
    LevelDbTemplete levelDbTemplete;

    @Override
    public String getBlockHeight() {
        levelDbTemplete.initLevelDB();
        String height= levelDbTemplete.get("currentBlockHeight").toString();
        levelDbTemplete.closeDB();
        return height;
    }
}
