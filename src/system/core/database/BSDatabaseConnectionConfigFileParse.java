package system.core.database;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BSDatabaseConnectionConfigFileParse extends DefaultHandler {
    private static final String USER_TAG = "user";
    private static final String PASSWORD_TAG = "password";
    private static final String SERVER_NAME_TAG = "serverName";
    private static final String PORT_NUMBER_TAG = "portNumber";
    private static final String DATABASE_NAME_TAG = "databaseName";

    private SQLServerDataSource ds = new SQLServerDataSource();
    private String currentElement = "";

    @Override
    public void startElement (String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        currentElement = qName;
    }

    @Override
    public void endElement (String uri, String localName, String qName) throws SAXException {
        currentElement = "";
    }

    @Override
    public void characters (char[] ch, int start, int length) throws SAXException {
        if (USER_TAG.equals(currentElement))
            ds.setUser(String.valueOf(ch, start, length));
        if (PASSWORD_TAG.equals(currentElement))
            ds.setPassword(String.valueOf(ch, start, length));
        if (SERVER_NAME_TAG.equals(currentElement))
            ds.setServerName(String.valueOf(ch, start, length));
        if (PORT_NUMBER_TAG.equals(currentElement))
            ds.setPortNumber(Integer.valueOf(String.valueOf(ch, start, length)));
        if (DATABASE_NAME_TAG.equals(currentElement))
            ds.setDatabaseName(String.valueOf(ch, start, length));
    }

    public SQLServerDataSource getDs () {
        return ds;
    }
}
