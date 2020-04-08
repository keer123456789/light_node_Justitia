package com.ibt.lightnode.service.implement;

import com.ibt.lightnode.service.SimpleService;
import com.ibt.lightnode.util.LevelDbUtil;
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
    LevelDbUtil levelDbUtil;

    @Override
    public String getBlockHeight() {
        levelDbUtil.initLevelDB();
        String height= levelDbUtil.get("currentBlockHeight").toString();
        levelDbUtil.closeDB();
        return height;
    }
}
