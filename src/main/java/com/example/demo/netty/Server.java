package com.example.demo.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {

    public static void main(String[] args) throws Exception {
        Server s = new Server();
        s.bind(6888);
        System.out.println("hello");
    }
    public void bind(int port) throws Exception {

        /**
         * 配置服务端的NIO线程组
         * NioEventLoopGroup 是用来处理I/O操作的Reactor线程组
         * bossGroup：用来接收进来的连接，workerGroup：用来处理已经被接收的连接,进行socketChannel的网络读写，
         * bossGroup接收到连接后就会把连接信息注册到workerGroup
         * workerGroup的EventLoopGroup默认的线程数是CPU核数的二倍
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {

            /**
             * ServerBootstrap 是一个启动NIO服务的辅助启动类,
             * ServerSocketChannel是以NIO的selector为基础进行实现的，用来接收新的连接，
             * 告诉Channel通过NioServerSocketChannel获取新的连接
             */
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap = serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap = serverBootstrap.channel(NioServerSocketChannel.class);
            /**
             * option是设置 bossGroup，childOption是设置workerGroup
             * netty 默认数据包传输大小为1024字节, 设置它可以自动调整下一次缓冲区建立时分配的空间大小，避免内存的浪费
             * 最小  初始化  最大 (根据生产环境实际情况来定)
             * 使用对象池，重用缓冲区
             */
            serverBootstrap = serverBootstrap.option(ChannelOption.RCVBUF_ALLOCATOR,
                    new AdaptiveRecvByteBufAllocator(64, 10496, 1048576));
            serverBootstrap = serverBootstrap.childOption(ChannelOption.RCVBUF_ALLOCATOR,
                    new AdaptiveRecvByteBufAllocator(64, 10496, 1048576));

            //设置 I/O处理类,主要用于网络I/O事件，记录日志，编码、解码消息
            serverBootstrap = serverBootstrap.childHandler(new ServerHandleInitializer());

            ChannelFuture f = serverBootstrap.bind(port).sync();

            f.channel().closeFuture().sync();

        } catch (InterruptedException e) {

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
