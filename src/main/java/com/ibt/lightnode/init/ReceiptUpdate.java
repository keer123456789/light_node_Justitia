package com.ibt.lightnode.init;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.init
 * @Author: keer
 * @CreateTime: 2020-04-03 19:37
 * @Description: 同步receipt
 */
@Component
public class ReceiptUpdate implements CommandLineRunner {
    @Value("${Full_Node}")
    private String fullNodeUrl;

    @Override
    public void run(String... args) throws Exception {

    }


}
