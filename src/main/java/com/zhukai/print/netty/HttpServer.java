package com.zhukai.print.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.extern.slf4j.Slf4j;

/**
 * netty搭建的简易http服务
 *
 * @author zhukai
 * @date 2019/1/25
 */
@Slf4j
public class HttpServer {

    public void bind(int port) {

        EventLoopGroup bossGroup = new NioEventLoopGroup();     // bossGroup就是parentGroup，是负责处理TCP/IP连接的
        EventLoopGroup workerGroup = new NioEventLoopGroup();   // workerGroup就是childGroup,是负责处理Channel(通道)的I/O事件

        try {
            ServerBootstrap sb = new ServerBootstrap();
            sb.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 16)  // 初始化服务端可连接队列,指定了队列的大小16
                    .childOption(ChannelOption.SO_KEEPALIVE, true)      // 保持长连接
                    .childHandler(new ChannelInitializer<SocketChannel>() {   // 绑定客户端连接时候触发操作
                        @Override
                        protected void initChannel(SocketChannel sh) throws Exception {
                            ChannelPipeline pipeline = sh.pipeline();
                            pipeline.addLast(new HttpServerCodec());    // http 编解码
                            pipeline.addLast("httpAggregator", new HttpObjectAggregator(512 * 1024)); // http 消息聚合器                                                                     512*1024为接收的最大contentlength
                            pipeline.addLast(new ServerHandler());      // 请求处理器
                        }
                    });
            // 绑定监听端口，调用sync同步阻塞方法等待绑定操作完
            ChannelFuture future = sb.bind(port).sync();

            if (future.isSuccess()) {
                // 成功绑定到端口之后,给channel增加一个管道关闭的监听器并同步阻塞,直到channel关闭,线程才会往下执行,结束进程。
                log.info("服务端启动成功! {}", "http://localhost:" + port);
                future.channel().closeFuture().sync();
            } else {
                log.error("服务端启动失败", future.cause());
            }
        } catch (Exception e) {
            log.error("", e);
        } finally {
            // 关闭线程组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }


    }

}
