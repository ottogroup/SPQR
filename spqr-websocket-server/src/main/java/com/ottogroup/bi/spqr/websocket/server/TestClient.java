package com.ottogroup.bi.spqr.websocket.server;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

@WebSocket
public final class TestClient {

    static final String URL = System.getProperty("url", "ws://127.0.0.1:9090/pipeline");

    private Session session = null;
    private CountDownLatch latch = null;
    
    public static void main(String[] args) throws Exception {
    	new TestClient().run();
    }
    
    public void run() throws Exception {
    	WebSocketClient client = new WebSocketClient();
    	client.start();
		ClientUpgradeRequest upgradeRequest = new ClientUpgradeRequest();			
		client.connect(this, new URI(URL), upgradeRequest);
		this.latch = new CountDownLatch(5);
//		await(5, TimeUnit.SECONDS);
		
//		sendUpdate(session, "webtrends", "another-stream",  "another-query",  "another-version", "another-schema");

    	
/*    	
        URI uri = new URI(URL);
        String scheme = uri.getScheme() == null? "http" : uri.getScheme();
        final String host = uri.getHost() == null? "127.0.0.1" : uri.getHost();
        final int port;
        if (uri.getPort() == -1) {
            if ("http".equalsIgnoreCase(scheme)) {
                port = 80;
            } else if ("https".equalsIgnoreCase(scheme)) {
                port = 443;
            } else {
                port = -1;
            }
        } else {
            port = uri.getPort();
        }

        if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
            System.err.println("Only WS(S) is supported.");
            return;
        }

        final boolean ssl = "wss".equalsIgnoreCase(scheme);
        final SslContext sslCtx;
            sslCtx = null;

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
            // If you change it to V00, ping is not supported and remember to change
            // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
            final WebSocketClientHandler handler =
                    new WebSocketClientHandler(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders()));

            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) {
                     ChannelPipeline p = ch.pipeline();
                     if (sslCtx != null) {
                         p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                     }
                     p.addLast(
                             new HttpClientCodec(),
                             new HttpObjectAggregator(8192),
                             handler);
                 }
             });

            Channel ch = b.connect(uri.getHost(), port).sync().channel();
            handler.handshakeFuture().sync();

            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String msg = console.readLine();
                if (msg == null) {
                    break;
                } else if ("bye".equals(msg.toLowerCase())) {
                    ch.writeAndFlush(new CloseWebSocketFrame());
                    ch.closeFuture().sync();
                    break;
                } else if ("ping".equals(msg.toLowerCase())) {
                    WebSocketFrame frame = new PingWebSocketFrame(Unpooled.wrappedBuffer(new byte[] { 8, 1, 8, 1 }));
                    ch.writeAndFlush(frame);
                } else {
                    WebSocketFrame frame = new TextWebSocketFrame(msg);
                    ch.writeAndFlush(frame);
                }
            }
        } finally {
            group.shutdownGracefully();
        }
        */
    }
    
	/**
	 * Executed after establishing web socket connection with streams api
	 * @param session
	 */
	@OnWebSocketConnect
	public void onConnect(Session session) {
		this.session = session;
		sendUpdate(this.session, "webtrends", "stream-type", "stream-query", "stream-version", "schema-version");
	}
	
	/**
	 * Executed by web socket implementation when receiving a message from the
	 * streams api. The message will be directly handed over to the configured 
	 * {@link ActorRef message receiver}  
	 * @param message
	 */
	@OnWebSocketMessage
	public void onMessage(String message) {
		System.out.println("Message: " + message);
	}

	/**
	 * Executed when closing the web socket connection
	 * @param statusCode
	 * @param reason
	 */
	@OnWebSocketClose		    
	public void onClose(int statusCode, String reason) {	
		//
	}

	/**
	 * Sends an update towards the webtrends stream api using the contents of the 
	 * provided {@link WebtrendsStreamListenerQueryUpdateMessage message}
	 * @param msg
	 */
	protected void sendUpdate(final Session session, final String oAuthToken, final String streamType, final String streamQuery, final String streamVersion, final String schemaVersion) {
		
		// build SAPI query object
		final StringBuilder sb = new StringBuilder();
//		sb.append("{\"access_token\":\"");
//		sb.append(oAuthToken);
//		sb.append("\",\"command\":\"stream\"");
//		sb.append(",\"stream_type\":\"");
//		sb.append(streamType);
//		sb.append("\",\"query\":\"");
//		sb.append(streamQuery);
//		sb.append("\",\"api_version\":\"");
//		sb.append(streamVersion);
//		sb.append("\",\"schema_version\":\"");
//		sb.append(schemaVersion);
//		sb.append("\"}");
		sb.append("{\"topicId\":\"");
		sb.append(oAuthToken);
		sb.append("\"}");

		try {
			session.getRemote().sendString(sb.toString());
		} catch(IOException e) {
		   	throw new RuntimeException("Unable to open stream", e);
		}
	}
	
	/**
	 * Timeout handler
	 * @param duration
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	public boolean await(int duration, TimeUnit unit) throws InterruptedException {
		return latch.await(duration, unit);
	}

}