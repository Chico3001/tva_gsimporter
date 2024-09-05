package debug.GrupoSalinasSiteImporter;

import brightspot.core.article.Article;
import brightspot.core.image.Image;
import com.psddev.cms.db.Directory;
import com.psddev.cms.db.Site;
import com.psddev.dari.db.Query;

import java.lang.reflect.Type;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.psddev.dari.util.StorageItem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GrupoSalinasSiteImporter {
    // Id del sitio a cargar
    protected static final String siteId = "0000017f-c2a6-da9d-a9ff-cfe6d0120000";    // Prototipo1
//    protected static final String siteId = "00000191-b566-d495-a3dd-bfee9ff80000";    // GrupoSalinas
    // URL del JSON
    protected static final String jsonUrl = "https://miempresaenlanube.xyz/test_sm.json";
    protected static final String imgUrl = "https://miempresaenlanube.xyz/";
    // Terminador de linea
    protected static final String LF = "####";


    protected static StringBuilder msgLog = new StringBuilder();
    protected static Date today = new Date();
    protected static Site site = Query.from(Site.class).where("id = ?", siteId).first();

    public static Object main() throws Throwable {
        msgLog.append(today.toString()).append(LF);

        List<GrupoSalinasArticle> grupoSalinasArticleList = getJson();
        for (GrupoSalinasArticle grupoSalinasArticle : grupoSalinasArticleList) {
            setArticle(grupoSalinasArticle);
        }

        return msgLog;
    }


    public static List<GrupoSalinasArticle> getJson() {
        try {
            URL url = new URL(jsonUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Gson gson = new Gson();
            Type grupoSalinasArticleListType = new TypeToken<List<GrupoSalinasArticle>>(){}.getType();
            List<GrupoSalinasArticle> articles = gson.fromJson(response.toString(), grupoSalinasArticleListType);

            return articles;

        } catch (Exception ex) {
            msgLog.append("/Error: " + ex.getMessage() + LF);
            return null;
        }
    }

    // Agrega Articulo a BSP
    public static void setArticle(GrupoSalinasArticle grupoSalinasArticle){
        Article article = new Article();
        List<String> paths = new ArrayList<>();
        paths.add("/" + grupoSalinasArticle.getId());

        article.setTipeArticle(Article.TipeArticle.Article);
        article.as(Site.ObjectModification.class).setOwner(site);
        article.as(Directory.ObjectModification.class).addSitePath(site, paths.toString(), Directory.PathType.PERMALINK);

        article.setHeadline(grupoSalinasArticle.getTitulo());
        //article.setSubHeadline(grupoSalinasArticle.getResumen());
        article.setBody(setBody(grupoSalinasArticle.getXml()));

        StorageItem image;
        image.

        //article.save();
        msgLog.append("Articulo guardado ID: ").append(grupoSalinasArticle.getId()).append(LF);
    }

    // Analiza el XML y convierte cada elemento en un HTML equivalente que se envia a BSP
    public static String setBody(Document xml) {
        StringBuilder body = new StringBuilder();
        Node reporte = xml.getElementsByTagName("reportes").item(0);
        NodeList nodeList = reporte.getChildNodes();
        int n = nodeList.getLength();
        Node current;
        for (int i=0; i<n; i++) {
            current = nodeList.item(i);
            if(current.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) current;
                switch (current.getNodeName()) {
                    case "seccion":
                        //body.append("<h1>").append(element.getAttribute("titulo")).append("</h1>");
                        break;

                    case "parrafo":
                        body.append("<p>").append(current.getTextContent()).append("</p>");
                        break;

                    case "anexo":
//                        body.append("<p>").append(element.getAttribute("mid")).append("</p>");
                        body.append("<img class=\"Image\" src=\"").append(imgUrl).append(element.getAttribute("mid")).append("\" alt=\"\">");
                        break;

                    default:
                        msgLog.append("---> Elemento extra encontrado ").append(current.getNodeName()).append(LF);
                        break;
                }
            }
        }

        return body.toString();
    }

    public static class GrupoSalinasArticle {
        protected int id;
        protected String titulo;
        protected String resumen;
        protected String fecha;
        protected String xml;
        protected String img_Portada;
        protected String img_Poster;
        protected String tiempo_Lec;

        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }

        public String getTitulo() {
            return titulo;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public String getResumen() {
            return resumen;
        }

        public void setResumen(String resumen) {
            this.resumen = resumen;
        }

        public String getFecha() {
            Date date = new Date(Long.parseLong(fecha));
            return date.toString();
        }

        public void setFecha(String fecha) {
            this.fecha = fecha;
        }

        public String getImg_Portada() {
            return img_Portada;
        }

        public void setImg_Portada(String img_Portada) {
            this.img_Portada = img_Portada;
        }

        public String getImg_Poster() {
            return img_Poster;
        }

        public void setImg_Poster(String img_Poster) {
            this.img_Poster = img_Poster;
        }

        public String getTiempo_Lec() {
            return tiempo_Lec;
        }

        public void setTiempo_Lec(String tiempo_Lec) {
            this.tiempo_Lec = tiempo_Lec;
        }

        public Document getXml() {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                ByteArrayInputStream inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
                Document doc = builder.parse(inputStream);
                doc.getDocumentElement().normalize();
                return doc;
            }
            catch (ParserConfigurationException | SAXException | IOException e) {
                e.getMessage();
                return null;
            }
        }

        public void setXml(String xml) {
            this.xml = xml;
        }
    }

}
