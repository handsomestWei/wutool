package com.wjy.wutool.msg.reactor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 消息模型抽象类
 *
 * @author weijiayu
 * @date 2024/4/30 11:34
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbsBaseMsg {

    /** 消息id，保证唯一 */
    public abstract Long getMsgId();
}
