package com.wjy.wutool.msg.reactor.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author weijiayu
 * @date 2024/4/30 11:34
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbsBaseMsg {

    public abstract Long getMsgId();
}
