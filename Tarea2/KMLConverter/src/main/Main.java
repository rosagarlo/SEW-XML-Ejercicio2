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
	private static final String FILE_TO = "arbol.kml";

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
	 * a un archivo KML.
	 * 
	 * @param persona que se quiere adaptar.
	 * @return String con el texto que formará parte del documento HTML.
	 */
	private static String appendPerson(Persona persona) {
		String body = "<Placemark>\n"
				+ "  <name>" + persona.nacimiento.lugar + "</name>\n"
				+ "  <description>Lugar de nacimiento de " + persona.datos + "</description>\n"
				+ "  <Point>\n"
				+ "    <coordinates>" + persona.nacimiento.coordenadas + "</coordinates>\n"
				+ "  </Point>\n"
				+ "</Placemark>\n";
		if (persona.fallecimiento.fecha != null) {
			body += "<Placemark>\n"
					+ "<name>" + persona.fallecimiento.lugar + "</name>\n"
					+ "  <description>Lugar de fallecimiento de " + persona.datos + "</description>\n"
					+ "  <Point>\n"
					+ "    <coordinates>" + persona.fallecimiento.coordenadas + "</coordinates>\n"
					+ "  </Point>\n"
					+ "</Placemark>\n";
		}
		return body;
	}

	/**
	 * Declara el elemento 'head' del archivo KML.
	 * 
	 * @return String que contiene el texto indicado.
	 */
	private static String createHead() {
		String header = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
				+ "<Document>\n";

		return header;
	}

	/**
	 * Texto necesario para cerrar el documento KML.
	 * 
	 * @return String con el texto necesario.
	 */
	private static String appendClose() {
		return "</Document>\n</kml>\n";
	}
}
