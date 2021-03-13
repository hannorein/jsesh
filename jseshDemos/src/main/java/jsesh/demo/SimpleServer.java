/*
 * Copyright ou © ou Copr. Serge Rosmorduc (2004-2020) 
 * serge.rosmorduc@cnam.fr

 * Ce logiciel est régi par la licence CeCILL-C soumise au droit français et
 * respectant les principes de diffusion des logiciels libres : "http://www.cecill.info".

 * This software is governed by the CeCILL-C license 
 * under French law : "http://www.cecill.info". 
 *
 * Extended and modified by Hanno Rein, March 2021.
 */
package jsesh.demo;

import javax.imageio.ImageIO;
import java.io.*;
import java.awt.image.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;

import jsesh.mdc.constants.TextDirection;
import jsesh.mdc.constants.TextOrientation;
import jsesh.mdcDisplayer.preferences.*;
import jsesh.mdcDisplayer.draw.*;
import jsesh.mdc.*;

public class SimpleServer {

	public static BufferedImage buildImage(String mdcText)
			throws MDCSyntaxError {
		// Create the drawing system:
		MDCDrawingFacade drawing = new MDCDrawingFacade();
		// Change the scale, choosing the cadrat height in pixels.
		drawing.setCadratHeight(60);
		// Change a number of parameters
		DrawingSpecification drawingSpecifications = new DrawingSpecificationsImplementation();
		PageLayout pageLayout = new PageLayout();
		pageLayout.setLeftMargin(5);
		pageLayout.setTopMargin(5);
		drawingSpecifications.setTextDirection(TextDirection.LEFT_TO_RIGHT);
		drawingSpecifications.setTextOrientation(TextOrientation.HORIZONTAL);	
		drawing.setDrawingSpecifications(drawingSpecifications);
		// Create the picture
		BufferedImage result = drawing.createImage(mdcText);
		return result;
	}

	public static Map<String, String> queryToMap(String query){
	    	Map<String, String> result = new HashMap<String, String>();
	    	for (String param : query.split("&")) {
			String pair[] = param.split("=");
			if (pair.length>1) {
		    		result.put(pair[0], pair[1]);
			}else{
		   	 	result.put(pair[0], "");
			}
	    	}
	    	return result;
	}

	static class RenderPNG implements HttpHandler {
		@Override
		public void handle(HttpExchange t) throws IOException {
			Map <String,String>params = SimpleServer.queryToMap(t.getRequestURI().getQuery());
			System.out.println(params);

		    	OutputStream os = t.getResponseBody();
			try{
				BufferedImage img = buildImage(params.get("mdc"));
				Headers headers = t.getResponseHeaders();
				headers.add("Content-Type", "image/png");
				t.sendResponseHeaders(200, 0);
				ImageIO.write(img, "png", os);
			}catch(MDCSyntaxError e){
				System.out.println(e.getMessage());
				String response = e.getMessage();
				t.sendResponseHeaders(404, response.length());
				os.write(response.getBytes());
			}
		    	os.close();
		}
	}


	public static void main(String args[]) throws MDCSyntaxError, IOException {
		ImageIO.setUseCache(false);
		HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
		server.createContext("/render", new RenderPNG());
		server.setExecutor(null); 
		server.start();
	}
}
