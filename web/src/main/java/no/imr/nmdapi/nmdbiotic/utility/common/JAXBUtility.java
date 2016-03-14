package no.imr.nmdapi.nmdbiotic.utility.common;

import java.io.File;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import no.imr.framework.logging.slf4j.aspects.stereotype.PerformanceLogging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JAXBUtility {

    private static final Logger LOGGER = LoggerFactory.getLogger(JAXBUtility.class.getName());

    public final static String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
    public final static String XML_XMLNS = "xmlns=\"";
    public final static String XML_NMD_BIOTIC_FORMAT = "http://www.imr.no/formats/nmdbiotic/v1\"";
    public final static String XML_XMLNS_NMD_BIOTIC_FORMAT = XML_XMLNS + XML_NMD_BIOTIC_FORMAT;

    private boolean formatted_output = true;

    public JAXBUtility(boolean formatted_output) {
        this.formatted_output = formatted_output;
    }

    /**
     * Unmarshall.
     *
     * @param xmlString the xml string
     * @param objClasses
     * @return the object
     * @throws JAXBException
     */
    @PerformanceLogging
    public Object unmarshal(String xmlString, Class<?>... objClasses) throws JAXBException {
        Object clInstance = null;
        try {
            JAXBContext jaxbContextReadXml = JAXBContext.newInstance(objClasses);
            Unmarshaller jaxbUnmarshaller = jaxbContextReadXml.createUnmarshaller();

            StringReader reader = new StringReader(xmlString);
            clInstance = (Object) jaxbUnmarshaller.unmarshal(reader);
            reader.close();
        } catch (JAXBException exp) {
            LOGGER.error(exp.getMessage(), exp);
            throw exp;
        }

        return clInstance;
    }

    private Marshaller initMarshaller(Class<?>... objClasses) throws JAXBException {
        Marshaller jaxbMarshaller = null;
        try {
            JAXBContext jaxbContextGenerateXml = JAXBContext.newInstance(objClasses);
            jaxbMarshaller = jaxbContextGenerateXml.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, formatted_output);
            jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        } catch (JAXBException exp) {
            LOGGER.error(exp.getMessage(), exp);
            throw exp;
        }
        return jaxbMarshaller;
    }

    @PerformanceLogging
    private String removeStrings(String target, String... strings) {
        int index = 0;
        while ((target != null) && (index < strings.length)) {
            target = target.replace(strings[index], "");
            index++;
        }
        return target;
    }

    @PerformanceLogging
    public String marshalChildren(Object metadata, String namespaceURI) throws JAXBException {
        String result = null;
        try {
            Class<?> objectClass = metadata.getClass();

            String name = objectClass.getSimpleName().replace("Type", "").toLowerCase();
            String rootPackage = objectClass.getName();
            rootPackage = rootPackage.substring(0, rootPackage.lastIndexOf("."));

            QName qName = new QName(namespaceURI, name);
            JAXBElement<Object> root = new JAXBElement(qName, objectClass, metadata);
            result = marshal(root, objectClass);
            result = removeStrings(result, XML_HEADER, XML_XMLNS_NMD_BIOTIC_FORMAT);

        } catch (JAXBException exp) {
            LOGGER.error(exp.getMessage(), exp);
            throw exp;
        } catch (Exception exp) {
            LOGGER.error(exp.getMessage(), exp);
        }

        return result;
    }

    /**
     * Marshall.
     *
     * @param fileName the file name
     * @param metadata the evry metadata
     * @param objClasses
     * @throws JAXBException
     */
    @PerformanceLogging
    public void marshal(String fileName, Object metadata, Class<?>... objClasses) throws JAXBException {
        try {
            Marshaller jaxbMarshaller = initMarshaller(objClasses);
            jaxbMarshaller.marshal(metadata, new File(fileName));
        } catch (JAXBException exp) {
            LOGGER.error(exp.getMessage(), exp);
            throw exp;
        }
    }

    @PerformanceLogging
    public String marshal(Object metadata, Class<?>... objClasses) throws JAXBException {
        String xmlString = null;
        try {
            Marshaller jaxbMarshaller = initMarshaller(objClasses);

            StringWriter stringWriter = new StringWriter();
            jaxbMarshaller.marshal(metadata, stringWriter);

            xmlString = stringWriter.toString();
        } catch (JAXBException exp) {
            LOGGER.error(exp.getMessage(), exp);
            throw exp;
        }
        return xmlString;
    }

    public List<?> getJAXBElementValues(List<Serializable> contentList) {
        List<Object> objectList = new ArrayList<Object>();
        for (Serializable serializable : contentList) {
            if (serializable instanceof JAXBElement) {
                JAXBElement<?> jaxb = (JAXBElement<?>) serializable;
                Object value = jaxb.getValue();
                objectList.add(value);
            }
        }
        return objectList;
    }

}
