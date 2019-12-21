package com.sport.video.airplay.service;

import org.seamless.xml.SAXParser;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;

/**
 * fileDesc
 * <p>
 * Created by ribory on 2019-11-29.
 **/
public class DLNASAXParser extends SAXParser {

    protected XMLReader create() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();

            // Configure factory to prevent XXE attacks
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            //commenting
            //factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            //factory.setXIncludeAware(false);

            //factory.setNamespaceAware(true);

            if (getSchemaSources() != null) {
                factory.setSchema(createSchema(getSchemaSources()));
            }

            XMLReader xmlReader = factory.newSAXParser().getXMLReader();
            xmlReader.setErrorHandler(getErrorHandler());
            return xmlReader;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}