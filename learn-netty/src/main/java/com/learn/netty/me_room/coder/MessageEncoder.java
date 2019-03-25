package com.learn.netty.me_room.coder;

import com.fasterxml.jackson.databind.ObjectMapper;
import framework.netty.me_room.empty.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * MessageEncoder
 * Created by mac_zly on 2017/5/30.
 */

public class MessageEncoder extends MessageToByteEncoder<Message> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        byte[] bytes = objectMapper.writeValueAsBytes(msg);
        out.writeBytes(bytes);
        ctx.flush();
    }
}
