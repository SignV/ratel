package org.nico.ratel.landlords.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.nico.noson.Noson;
import org.nico.noson.entity.NoType;
import org.nico.ratel.landlords.client.handler.DefaultChannelInitializer;
import org.nico.ratel.landlords.entity.Poker;
import org.nico.ratel.landlords.enums.PokerLevel;
import org.nico.ratel.landlords.enums.PokerType;
import org.nico.ratel.landlords.helper.PokerHelper;
import org.nico.ratel.landlords.print.SimplePrinter;
import org.nico.ratel.landlords.print.SimpleWriter;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class SimpleClient {

	public static int id = -1;
	
	public static String serverAddress = "";
	
	public static int port = 0;
	
	public static void main(String[] args) throws InterruptedException, IOException {
		if(args != null && args.length > 0) {
			for(int index = 0; index < args.length; index = index + 2) {
				if(index + 1 < args.length) {
					if(args[index].equalsIgnoreCase("-p") || args[index].equalsIgnoreCase("-port")) {
						port = Integer.valueOf(args[index + 1]);
					}
					if(args[index].equalsIgnoreCase("-h") || args[index].equalsIgnoreCase("-host")) {
						serverAddress = args[index + 1];
					}
					if(args[index].equalsIgnoreCase("-pt") || args[index].equalsIgnoreCase("-pt")) {
						int selection =  Integer.parseInt(args[index + 1]);

						if( selection >= 1 && selection <= PokerHelper.totalPrinters ){
							PokerHelper.pokerPrinterType = selection - 1;
						}
					}
					
				}
			}
		}
		// List<Poker> pokers = new ArrayList<Poker>();
		// Poker p = new Poker();
		// p.setLevel(PokerLevel.LEVEL_3);
		// p.setType(PokerType.SPADE);
		// Poker p2 = new Poker();
		// p2.setLevel(PokerLevel.LEVEL_K);
		// p2.setType(PokerType.HEART);
		// pokers.add(p);
		// pokers.add(p2);

		// System.out.println(

		// 	pokers.stream().map(
		// 		elt -> {
		// 			int level = elt.getLevel().getLevel() - 1;
		// 			switch (elt.getType()) {
		// 				case SPADE:
		// 				return String.valueOf(Character.toChars(0x1F0A1 + level));
		// 				case HEART:
		// 				return String.valueOf(Character.toChars(0x1F0B1 + level));
		// 				case DIAMOND:
		// 				return String.valueOf(Character.toChars(0x1F0C1 + level));
		// 				case CLUB:
		// 				return String.valueOf(Character.toChars(0x1F0D1 + level));
		// 				default:
		// 				return "";
		// 			}
		// 		}
		// 	).collect(Collectors.joining(""))
		// );
		if(serverAddress.equals("") || port == 0){
			StringBuffer buffer = new StringBuffer();
			try {
				URL url = new URL("https://raw.githubusercontent.com/abbychau/ratel/master/serverlist.json");
				URLConnection con = url.openConnection();
				con.setUseCaches(false);
				InputStreamReader isr = new InputStreamReader(con.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				String s = null;
				while ( (s = in.readLine()) != null) {
					buffer.append(s).append("\n");
				}
			} catch ( Exception e ) {
				System.out.println(e.toString());
				buffer = null;
			} 
			List<String> serverAddressList = Noson.convert(buffer.toString(), new NoType<List<String>>() {});
			SimplePrinter.printNotice("Please select a server:");
			for(int i = 0; i < serverAddressList.size(); i++) {
				SimplePrinter.printNotice((i+1) + ". " + serverAddressList.get(i));
			}
			int serverPick = 0;
			while(serverPick<1 || serverPick>serverAddressList.size()){
				try {
					serverPick = Integer.parseInt(SimpleWriter.write("option"));
				}catch(NumberFormatException e){}
			}
			serverAddress = serverAddressList.get(serverPick-1);
			String[] elements = serverAddress.split(":");
			serverAddress = elements[0];
			port = Integer.parseInt(elements[1]);
		}
		
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap()
					.group(group)
					.channel(NioSocketChannel.class)
					.handler(new DefaultChannelInitializer());
			SimplePrinter.printNotice("Connecting to " + serverAddress + ":" + port);
			Channel channel = bootstrap.connect(serverAddress, port).sync().channel();
			channel.closeFuture().sync();
		} finally {
			group.shutdownGracefully().sync();
		}

	}
	
}
