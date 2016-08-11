package com.hll.fileserver;

import com.hll.fileserver.handler.FileServerHanlder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件服务器
 * Created by hll on 2016/8/11.
 */
public class FileServer {

  static Logger logger = LoggerFactory.getLogger(FileServer.class);

  private String host;
  private int port;

  public FileServer(String host, int port) {
    this.host = host;
    this.port = port;
  }

  private EventLoopGroup bossGroup = new NioEventLoopGroup(1);
  private EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());

  public void run() throws InterruptedException {
    try {
      ServerBootstrap sb = new ServerBootstrap();
      sb.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .option(ChannelOption.SO_BACKLOG, 100)
          .childOption(ChannelOption.SO_KEEPALIVE, true)
          .childOption(ChannelOption.TCP_NODELAY, true)
          .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
              ch.pipeline()
                  .addLast(new HttpServerCodec())
                  .addLast(new HttpObjectAggregator(65535))
                  .addLast(new ChunkedWriteHandler())
                  .addLast(new FileServerHanlder());
            }
          });
      ChannelFuture channelFuture = sb.bind(host, port).sync();
      channelFuture.channel().closeFuture().sync();
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }

  public static void main(String[] args) {
    try {
      new FileServer("localhost", 9999).run();
    } catch (Exception e) {
      logger.info(e.getMessage());
      System.exit(-1);
    }
  }
}
