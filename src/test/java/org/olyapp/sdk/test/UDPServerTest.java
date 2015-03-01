package org.olyapp.sdk.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.olyapp.sdk.comm.UDPServer;

public class UDPServerTest {

	@Test
	public void testCameraModel() throws InterruptedException, IOException {
		UDPServer server = new UDPServer(50529,false);
		
		int id = 0;
		while (id<2000) {
			byte[] data = server.getNextFrame(5000,id);
			if (data!=null) {
				//System.out.println("Received frame");
				Files.write(Paths.get("file" + String.format("%04d",id) + ".jpg"), data);
			}
			id++;
		}
	}

}
