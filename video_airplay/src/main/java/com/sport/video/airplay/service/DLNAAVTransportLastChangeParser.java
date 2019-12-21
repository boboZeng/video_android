package com.sport.video.airplay.service;

import org.fourthline.cling.support.avtransport.lastchange.AVTransportLastChangeParser;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParserFactory;


/**
 * fileDesc
 * <p>
 * Created by ribory on 2019-12-16.
 **/
public class DLNAAVTransportLastChangeParser extends AVTransportLastChangeParser {
    @Override
    protected XMLReader create() {
//        return super.create();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

//            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
//            factory.setXIncludeAware(false);
//            factory.setNamespaceAware(true);

            if (this.getSchemaSources() != null) {
                factory.setSchema(this.createSchema(this.getSchemaSources()));
            }

            XMLReader xmlReader = factory.newSAXParser().getXMLReader();
            xmlReader.setErrorHandler(this.getErrorHandler());
            return xmlReader;
        } catch (Exception var3) {
            throw new RuntimeException(var3);
        }
    }
}
