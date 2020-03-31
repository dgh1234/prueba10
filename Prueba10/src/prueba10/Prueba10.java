package prueba10;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;



public class Prueba10 {
	
	static int puerto=9999;
	static boolean conmuta=false;
	static HttpServer httpServer;

	public static void main(String[] args) throws IOException, URISyntaxException {
		
		//esto cambia la forma del explorador de archivos
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { 
        	System.out.println("algo no jalo");       	
        }
		
    	Host host=new Host();
    	 	     	
    	httpServer = HttpServer.create(new InetSocketAddress(puerto), 0);
    	
    	httpServer.createContext("/", new Principal());		//esta es la pagina principal
    	httpServer.createContext("/apagado", new Apagado());
    	httpServer.createContext("/descarga", new Descarga());// llama a la pagina como video1.mp4
    	httpServer.createContext("/openFileDialog", new OpenFileDialog());
           
    	httpServer.setExecutor(null);
    	httpServer.start();
    	

    	host.abreLocalHost();
    	host.cargaGPIO();
	}
}


//PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP
class Principal implements HttpHandler{
	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void handle(HttpExchange he) throws IOException {
		
		//System.out.println("direccion de quien se conecto " + he.getRemoteAddress().getHostString());
		
		if("0:0:0:0:0:0:0:1".equals(he.getLocalAddress().getHostName())) { //esta pagina es para el servidor
			
		String cad="<input type=\"button\" value=\"CLICK\" onclick=\"window.location.href='/descarga"+OpenFileDialog.ext+"'\">";

		String response=			
			"<!DOCTYPE html>"+
      		"<html>"+
      		"<body>"+
      		
      		"<h1> Direccion IP </h1>" + 
      		"<h2>" + new Host().daDireccioIP()+":"+ Prueba10.puerto +"</h2>"+
      		
			"<p>Click para cargar direccion</p>" + 
			"<form method=\"get\">" + 
			"<input type=\"button\" value=\"CLICK\" onclick=\"window.location.href='/openFileDialog'\">" + 
			"</form>"+		
			"<h4>" + OpenFileDialog.ruta + "</h4>" +
			
			"<p>Click para Descargar</p>" + 
			"<form method=\"get\">" + 
			cad + 
			"</form>"+
  
			"<p>Click para apagar</p>" + 
			"<form method=\"get\">" + 
			"<input type=\"button\" value=\"CLICK\" onclick=\"window.location.href='/apagado'\">" + 
			"</form>"+
							
			"</body>"+
			"</html>"
			;
		
		he.sendResponseHeaders(200, response.length());            
		OutputStream outputStream = he.getResponseBody();
		outputStream.write(response.getBytes());
		outputStream.close();
		
		}else {//esta pagina es para el cliente, el cual solo puede descargar archivos
			
			String cad="<input type=\"button\" value=\"CLICK\" onclick=\"window.location.href='/descarga"+OpenFileDialog.ext+"'\">";

			String response=			
				"<!DOCTYPE html>"+
	      		"<html>"+
	      		"<body>"+

				"<p>Click para Descargar</p>" + 
				"<form method=\"get\">" + 
				cad + 
				"</form>"+
				
      			"<h1> Archivo </h1>" + 
      			"<h2>" + OpenFileDialog.nombreArch + "</h2>"+
								
				"</body>"+
				"</html>"
				;
			
			he.sendResponseHeaders(200, response.length());            
			OutputStream outputStream = he.getResponseBody();
			outputStream.write(response.getBytes());
			outputStream.close();
			
		}
	}
}
//PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP



//VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV
class Descarga implements HttpHandler{
	
	@Override
	public void handle(HttpExchange arg0) throws IOException {

		Headers headers = arg0.getResponseHeaders();
		//headers.add("Content-Type", "application/mp4");		
		headers.add("Content-Type", "application");

		File file = new File (OpenFileDialog.ruta);
		byte[] bytes  = new byte [(int)file.length()];
           
		FileInputStream fileInputStream = new FileInputStream(file);
		@SuppressWarnings("resource")
		BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
		bufferedInputStream.read(bytes, 0, bytes.length);
		
		arg0.sendResponseHeaders(200, file.length());
		OutputStream outputStream = arg0.getResponseBody();
		outputStream.write(bytes, 0, bytes.length);
		outputStream.close();
	} 
}
//VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV


class OpenFileDialog extends JFrame implements HttpHandler{
	
	static String ruta;
	static String nombreArch;
	static String ext;
	private JFileChooser fileChooser;
	
	public OpenFileDialog() {
		
		fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		//quito el filtro para ver todos los archivos disponibles en el directorio
		//fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Archivo", "mp4"));
		fileChooser.setAcceptAllFileFilterUsed(true);
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {			
			File selectedFile = fileChooser.getSelectedFile();			
			ruta=selectedFile.getAbsolutePath();
			nombreArch=selectedFile.getName();			
			ext = nombreArch.substring(nombreArch.lastIndexOf("."));
			
			//System.out.println("Selected file: " + ruta);				
			//System.out.println("nombre archivo: " + nombreArch);
			//System.out.println("extencion: " + ext);
		}  		
	}
	

	@Override
	public void handle(HttpExchange he) throws IOException {

		new OpenFileDialog();

		String response=
		"<!DOCTYPE html>"+
  		"<html>"+
  		"<body>"+
  		"<h2>" + ruta +"</h2>"+ 
  		
		"<p>Click para Principal</p>" + 
		"<form method=\"get\">" + 
		"<input type=\"button\" value=\"CLICK\" onclick=\"window.location.href='/'\">" + 
		"</form>"+
		
		"</body>"+
		"</html>";
		
		he.sendResponseHeaders(200, response.length());            
		OutputStream outputStream = he.getResponseBody();
		outputStream.write(response.getBytes());
		outputStream.close();
	}
}



//Hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh
class Apagado implements HttpHandler{
	
	public void handle(HttpExchange arg0) throws IOException {

		String response=			
				"<!DOCTYPE html>"+
      		"<html>"+
      		"<body>"+
      		"<h1>ADIOS</h1>" +  
      		
      		"<img src=\"https://orig00.deviantart.net/1565/f/2012/039/a/9/mmmm_cerveza_by_anypanfupucca-d4p1t7q.gif\">"+
      
				"</body>"+
				"</html>"
				; 
		
		arg0.sendResponseHeaders(200, response.length());            
		OutputStream outputStream = arg0.getResponseBody();
		outputStream.write(response.getBytes());
		outputStream.close(); 
		Prueba10.httpServer.stop(1);
		System.exit(0);//esto es importante para detener el thread de recibe cadena
	}
}
//Hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh

//hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh
class Host{
	private String SOcadena;
	private boolean banderaHost;
	private String SO;
	
	public Host(){
		SOcadena=System.getProperty("os.name");
		if(SOcadena.equals("Linux")) {
			banderaHost=true;
		}else {	
			banderaHost=false;
		}		
	}
	
	public void abreLocalHost() throws IOException, URISyntaxException {
		if(banderaHost==true) {
			Runtime.getRuntime().exec("xdg-open http://localhost:"+ Prueba10.puerto);//abre la pagina principal
		}else {
			URI uri=new URI("http://localhost:" + Prueba10.puerto + "/");//abre la pagina principal
			Desktop.getDesktop().browse(uri);
		}
	}
	
	public String daDireccioIP() throws IOException {
		if(banderaHost==true) {
			SO=new DireccionLinux().daIPLinux();
			return SO;
		}else {
			SO=new DireccionWin().daIPWin();
			return SO;
		}		
	}
	
	public void cargaGPIO() {
		if (banderaHost==true) {
			//new GPIO();		//usar las librerias gpio si estas en linux
		}
	}
	
	public boolean daBanderaHost() {
		return banderaHost;
	}
}
//hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh


//yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy
class DireccionLinux{
	private String s;			//tomo los caracteres de las lineas que se van leyendo
	private StringBuilder text;	//Construye un generador de cadenas sin caracteres y una 
								//capacidad inicial de 16 caracteres.
	private String cadenaDeText;//es para guardar en una cadena la conversion del objeto StringBuilder
								//a cadena
	private String[] splits;	//uso este  arreglo para poder partei la cadena cadenaDeText
	private String direccionIP;	//
	
	public DireccionLinux() throws IOException {
		
		text = new StringBuilder();
		
		Process p = Runtime.getRuntime().exec("ifconfig wlan0");	//ejecuto el comando en la consola                                                                                                                                                 
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		while ((s = stdInput.readLine()) != null) {
			text.append(s);				//la agrego caracteres al objeto StringBuilder text
			//System.out.println(s);	//les los caracteres de la pantalla de Linux
		}		
		cadenaDeText=text.toString();
		//System.out.println("la cadena es " + cadenaDeText);

		splits = cadenaDeText.split("inet|netmask");	//parto la cadena de salida empiezo en inet y
														//acabo en netmask		
		direccionIP=splits[1].toString();	//hago cadena al elemento 1 del arreglo splits		
		//System.out.println(direccionIP);
	}
	public String daIPLinux() {
		direccionIP=direccionIP.replaceAll("\\s", "");//quito caracteres espacio de palabra de la cadena
		return direccionIP;
	}
}
//yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy

//hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh
class DireccionWin{
	
	private InetAddress IP=null;
	
	public DireccionWin() throws UnknownHostException{	
		IP = InetAddress.getLocalHost();
	}
	public String daIPWin() {
		return IP.getHostAddress();
	}
}
//hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh
