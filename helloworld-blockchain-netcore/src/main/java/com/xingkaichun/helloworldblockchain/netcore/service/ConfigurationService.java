package com.xingkaichun.helloworldblockchain.netcore.service;


import com.xingkaichun.helloworldblockchain.netcore.dto.adminconsole.ConfigurationDto;

/**
 * 配置service
 *
 * @author 邢开春 xingkaichun@qq.com
 */
public interface ConfigurationService {

    /**
     * 根据配置Key获取配置
     */
    ConfigurationDto getConfigurationByConfigurationKey(String confKey);

    /**
     * 设置配置
     */
    void setConfiguration(ConfigurationDto configurationDto);
}
