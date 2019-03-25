package com.learn.netty.me_room.coder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.netty.me_room.empty.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 消息解码器
 * Created by mac_zly on 2017/5/30.
 */
public class MessageDecoder extends ByteToMessageDecoder {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);

        Message message = objectMapper.readValue(bytes, Message.class);
        out.add(message);
    }

}
