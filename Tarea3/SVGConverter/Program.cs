using System.Xml;
using System.Collections;

namespace SVGConverter
{
    class Converter
    {
        private class Node
        {
            public int x { get; set; }
            public int y { get; set; }

            public Node(int x, int y)
            {
                this.x = x;
                this.y = y;
            }
        }

        private static int x, y;
        private const int ancho = 210;
        private const int altura = 50;
        private const int elementSpacingX = 215;
        private const int elementSpacingY = 50;
        private static Stack stack;
        private static string XML = "arbol-genealogico.xml";
        private static string SVG = "arbol.svg";

        public static void Main(string[] args)
        {
            stack = new Stack();
            XmlTextReader reader = new XmlTextReader(XML);

            x = 20;
            y = 20;

            XmlWriter xmlWriter = XmlWriter.Create(SVG);
                int height = estimateHeight(XML);
                WriteSVGHeader(xmlWriter, height);
                while (reader.Read())
                {
                    switch (reader.NodeType)
                    {
                        case XmlNodeType.Element: // El nodo es un elemento.
                            WriteElement(xmlWriter, reader.Name);
                            int attributeY = y;
                            stack.Push(new Node(x, y));
                            x += elementSpacingX;
                            while (reader.MoveToNextAttribute())
                            {// Read the attributes.
                                attributeY += 10;
                                WriteAttribute(xmlWriter, reader.Name + ": " + reader.Value, attributeY);
                            }
                            if (reader.IsStartElement())
                            {
                                if (reader.IsEmptyElement)
                                {
                                    x -= elementSpacingX;
                                    y += elementSpacingY;
                                    stack.Pop();
                                }
                            }

                            break;
                        case XmlNodeType.Text: // Texto en cada elemento
                            WriteValue(xmlWriter, reader.Value);
                            break;
                        case XmlNodeType.EndElement: // El final de cada elemento
                            x -= elementSpacingX;
                            y += elementSpacingY;
                            stack.Pop();
                            break;
                    }
                }
                xmlWriter.WriteEndDocument();
                xmlWriter.Close();
        }

        private static void WriteElement(XmlWriter xmlWriter, string text)
        {
            xmlWriter.WriteStartElement("rect");
            xmlWriter.WriteAttributeString("x", x.ToString());
            xmlWriter.WriteAttributeString("y", y.ToString());
            xmlWriter.WriteAttributeString("width", ancho.ToString());
            xmlWriter.WriteAttributeString("height", altura.ToString());
            xmlWriter.WriteAttributeString("style", "fill:white;stroke:black;stroke-width:1");
            xmlWriter.WriteEndElement();
            xmlWriter.WriteStartElement("text");
            xmlWriter.WriteAttributeString("x", (x + 10).ToString());
            xmlWriter.WriteAttributeString("y", (y + 10).ToString());
            xmlWriter.WriteAttributeString("font-size", "10");
            xmlWriter.WriteAttributeString("style", "fill:blue");
            xmlWriter.WriteString(text);
            xmlWriter.WriteEndElement();
            if (stack.Count != 0)
            {
                Node parent = (Node)stack.Peek();
                xmlWriter.WriteStartElement("path");
                int startX = (parent.x + ancho);
                int startY = (parent.y + altura / 2);
                int finalX = x;
                int finalY = (y + altura / 2);
                xmlWriter.WriteAttributeString("d", "M" + startX + " " + startY + " C" + finalX + " " + startY + " " + startX + " " + finalY + " " + finalX + " " + finalY);
                xmlWriter.WriteAttributeString("style", "fill:transparent;stroke:black");
                xmlWriter.WriteEndElement();
            }
        }

        private static void WriteValue(XmlWriter xmlWriter, string text)
        {
            Node current = (Node)stack.Peek();
            xmlWriter.WriteStartElement("rect");
            xmlWriter.WriteAttributeString("x", (current.x + ancho / 3).ToString());
            xmlWriter.WriteAttributeString("y", y.ToString());
            xmlWriter.WriteAttributeString("width", (ancho / 3 * 2 + 5).ToString());
            xmlWriter.WriteAttributeString("height", altura.ToString());
            xmlWriter.WriteAttributeString("style", "fill:purple;stroke:black;stroke-width:1");
            xmlWriter.WriteEndElement();
            xmlWriter.WriteStartElement("text");
            xmlWriter.WriteAttributeString("x", ((current.x + ancho / 3) + 10).ToString());
            xmlWriter.WriteAttributeString("y", (y + 10).ToString());
            xmlWriter.WriteAttributeString("font-size", "10");
            xmlWriter.WriteAttributeString("style", "fill:white");
            xmlWriter.WriteString(text);
            xmlWriter.WriteEndElement();
        }
        private static int estimateHeight(string xmlFile)
        {
            XmlTextReader reader = new XmlTextReader(xmlFile);
            int altura = y;
            while (reader.Read())
            {
                switch (reader.NodeType)
                {
                    case XmlNodeType.Element:
                        if (reader.IsStartElement())
                        {
                            if (reader.IsEmptyElement)
                            {
                                altura += elementSpacingY;
                            }
                        }
                        break;
                    case XmlNodeType.EndElement:
                        altura += elementSpacingY;
                        break;
                }
            }
            reader.ResetState();
            return altura;
        }

        private static void WriteSVGHeader(XmlWriter xmlWriter, int altura)
        {
            xmlWriter.WriteStartDocument();
            xmlWriter.WriteStartElement("svg", "http://www.w3.org/2000/svg");
            xmlWriter.WriteAttributeString("width", "auto");
            xmlWriter.WriteAttributeString("height", altura + "");
            xmlWriter.WriteAttributeString("style", "overflow:visible ");
            xmlWriter.WriteAttributeString("version", "1.0");
        }

        private static void WriteAttribute(XmlWriter xmlWriter, string text, int attributeY)
        {
            Node current = (Node)stack.Peek();
            xmlWriter.WriteStartElement("text");
            xmlWriter.WriteAttributeString("x", ((current.x) + 10).ToString());
            xmlWriter.WriteAttributeString("y", (attributeY + 10).ToString());
            xmlWriter.WriteAttributeString("font-size", "10");
            xmlWriter.WriteAttributeString("style", "fill:black");
            xmlWriter.WriteString(text);
            xmlWriter.WriteEndElement();
        }

    }
}