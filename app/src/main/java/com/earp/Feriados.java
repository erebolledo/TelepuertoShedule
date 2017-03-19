package com.earp;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;



public class Feriados {
	
	public String getContenido(String annio){
		String html = "";
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet("http://www.cuandoenelmundo.com/calendario/venezuela/"+annio);
		try{
			HttpResponse response = client.execute(request);

			InputStream in = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			StringBuilder str = new StringBuilder();
			String line = null;
			while((line = reader.readLine()) != null){
				str.append(line);
			}
			in.close();
			html = str.toString();
		}catch(Exception e){}
		return html;
	}

	//Lectura desde el buffer
	/*private String readFromBuffer(BufferedReader br){
		   StringBuilder text = new StringBuilder();
		   try{
		      String line;
		      while ((line = br.readLine()) != null) {
		         text.append(line);
		         text.append("\n");
		      } 
		   } catch (IOException e) { 
		      e.printStackTrace();
		      // tratar excepci�n!!!
		   }
		   return text.toString();
		}
	
	//Obtener Informacion de la pagina web
	public String getContenido(){
		HttpURLConnection con = null;
		URL url;
		String html ="";
		try {
		 
		   HttpGet httpGet = new HttpGet("http://venciclopedia.com/?title=D%C3%ADas_feriados_de_Venezuela/");
		   HttpClient httpclient = new DefaultHttpClient();
		   HttpResponse response = httpclient.execute(httpGet);
		 
		   html = readFromBuffer(
		      new BufferedReader(
		         new InputStreamReader(response.getEntity().getContent())));
		 
		}catch (IOException e) {
		   e.printStackTrace();
		   // tratar excepci�n!!!
		}
		return html;
	}*/
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("prueba");
		// TODO Auto-generated method stub

	}

}
