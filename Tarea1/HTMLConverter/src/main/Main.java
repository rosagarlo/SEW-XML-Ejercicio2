package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import persona.Fallecimiento;
import persona.Nacimiento;
import persona.Persona;

public class Main {

	private static final String NAME = "arbol-genealogico.xml";
	private static final String FILE_TO = "arbol.html";

	public static void main(String[] args) {
		transform();
	}

	public static void transform() {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			ClassLoader classloader = Main.class.getClassLoader();
			InputStream xmlData = classloader.getResourceAsStream(NAME);

			// procesa el XML con seguridad, evita ataques como XML External
			// Entities (XXE)
			dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

			// parsear el archivo XML
			Document doc = dbf.newDocumentBuilder().parse(xmlData);

			StringWriter stringWriter = new StringWriter();
			stringWriter.append(createHead()); // escribir el header en el
												// documento

			// normalizar el DOM (eliminar \r o \n)
			doc.getDocumentElement().normalize();

			// lista de las personas
			NodeList list = doc.getElementsByTagName("persona");

			for (int i = 0; i < list.getLength(); i++) {

				Node node = list.item(i);

				if (node.getNodeType() == Node.ELEMENT_NODE) {

					Element element = (Element) node;
					Persona persona = new Persona();
					Nacimiento nacimiento = new Nacimiento();
					Fallecimiento fallecimiento = new Fallecimiento();

					// nombre y apellidos
					String datos = element.getElementsByTagName("datos").item(0)
							.getTextContent();

					// atributos de nacimiento
					NodeList nacimientoNodeList = element
							.getElementsByTagName("nacimiento");
					String fechaN = nacimientoNodeList.item(0).getAttributes()
							.getNamedItem("fecha").getTextContent();
					String lugarN = nacimientoNodeList.item(0).getAttributes()
							.getNamedItem("lugar").getTextContent();
					String coordenadasN = nacimientoNodeList.item(0)
							.getAttributes().getNamedItem("coordenadas")
							.getTextContent();

					// atributos de fallecimiento
					NodeList fallecimientoNodeList = element
							.getElementsByTagName("fallecimiento");
					if (fallecimientoNodeList.item(0) != null) {
						String fechaF = fallecimientoNodeList.item(0)
								.getAttributes().getNamedItem("fecha")
								.getTextContent();
						String lugarF = fallecimientoNodeList.item(0)
								.getAttributes().getNamedItem("lugar")
								.getTextContent();
						String coordenadasF = fallecimientoNodeList.item(0)
								.getAttributes().getNamedItem("coordenadas")
								.getTextContent();
						fallecimiento.fecha = fechaF;
						fallecimiento.lugar = lugarF;
						fallecimiento.coordenadas = coordenadasF;
					}

					// path de la imagen
					String img = element.getElementsByTagName("imagen").item(0)
							.getTextContent();

					// comentario
					String comentario = element
							.getElementsByTagName("comentario").item(0)
							.getTextContent();

					nacimiento.fecha = fechaN;
					nacimiento.lugar = lugarN;
					nacimiento.coordenadas = coordenadasN;

					persona.datos = datos;
					persona.nacimiento = nacimiento;
					persona.fallecimiento = fallecimiento;
					persona.imgPath = img;
					persona.comentario = comentario;

					stringWriter.append(appendPerson(persona));
				}
			}
			
			stringWriter.append(appendClose());
			
			// write to file
			File file = new File(FILE_TO);
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(stringWriter.toString());
			bw.close();

		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Se encarga de 'parsear' el elemento persona del archivo XML y adaptarlo
	 * a un archivo HTML.
	 * 
	 * @param persona que se quiere adaptar.
	 * @return String con el texto que formará parte del documento HTML.
	 */
	private static String appendPerson(Persona persona) {
		String body = "<h2>" + persona.datos +"</h2>\r"
				+ "<h3>Nacimiento</h3>\n"
				+ "<table>\n"
				+ "<tr>\n"
				+ "		<th>Fecha</th>\n"
				+ "		<th>Lugar</th>\n"
				+ "		<th>Coordenadas</th>\n"
				+ "	</tr>\n"
				+ "	<tr>\n"
				+ "		<td>" + persona.nacimiento.fecha + "</td>\n"
				+ "		<td>" + persona.nacimiento.lugar + "</td>\n"
				+ "		<td>" + persona.nacimiento.coordenadas + "</td>\n"
				+ "	</tr>\n"
				+ "	</table>\n";
				
		if (!persona.fallecimiento.lugar.isEmpty()) {
			body += "<h3>Fallecimiento</h3>\n"
					+ "<table>\n"
					+ "<tr>\n"
					+ "		<th>Fecha</th>\n"
					+ "		<th>Lugar</th>\n"
					+ "		<th>Coordenadas</th>\n"
					+ "	</tr>\n"
					+ "	<tr>\n"
					+ "		<td>" + persona.fallecimiento.fecha + "</td>\n"
					+ "		<td>" + persona.fallecimiento.lugar + "</td>\n"
					+ "		<td>" + persona.fallecimiento.coordenadas + "</td>\n"
					+ "	</tr>\n"
					+ "	</table>\n";
		}
		body += "<h3>Imagen</h3>\n"
				+ "<img src= \"" + persona.imgPath + "\" alt= \"Imagen de la persona\"/>\n"
				+ "<h3>Comentario</h3>\n"
				+ "<p>"+ persona.comentario +"</p>";
		return body;
	}

	/**
	 * Declara el elemento 'head' del archivo HTML.
	 * 
	 * @return String que contiene el texto indicado.
	 */
	private static String createHead() {
		String header = "<!DOCTYPE HTML>\n"
				+ "\n"
				+ "<html lang=\"es\">\n"
				+ "<head>\n"
				+ "    <meta charset=\"UTF-8\" />\n"
				+ "    <meta name=\"author\" content=\"Rosa García\"/>\n"
				+ "    <title>Árbol genealógico</title>\n"
				+ "    <link rel=\"stylesheet\" type=\"text/css\" href=\"estilo.css\" />\n"
				+ "</head>\n"
				+ "<body>\n"
				+ "<h1>Mi árbol genealógico</h1>\n";

		return header;
	}

	/**
	 * Texto necesario para cerrar el documento KML.
	 * 
	 * @return String con el texto necesario.
	 */
	private static String appendClose() {
		return "</body>\n"
				+ "</html>";
	}
}
