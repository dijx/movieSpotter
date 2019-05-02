package com.infoshare.jjdd6.moviespotter.services;

import com.infoshare.jjdd6.moviespotter.dao.ProgrammeDao;
import com.infoshare.jjdd6.moviespotter.models.Programme;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

@RequestScoped
public class EpgXmlParser {

    @Inject
    ConfigLoader configLoader;

    @Inject
    EpgXmlLoader epgXmlLoader;

    @Inject
    ProgrammeDao programmeDao;

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    public void parseXmlTvData() {
        doParse();
    }

    private void doParse() {

        log.info("XML parser will try to call EpgXmlLoader.loadEpgData");

        Document doc = epgXmlLoader.loadEpgData();

        String ignore = configLoader.getProperties().getProperty("Ignore");
        String onlyLoad = configLoader.getProperties().getProperty("OnlyLoad");

        EpgDateConverter epgDateConverter = new EpgDateConverter();

        NodeList channelsList = doc.getDocumentElement().getElementsByTagName("programme");

        for (int i = 0; i < channelsList.getLength(); i++) {

            Node node = channelsList.item(i);
            Programme programme = new Programme();

            String channelName = node.getAttributes().getNamedItem("channel").getNodeValue();

            if ((ignore != null && ignore.contains(channelName)) || (onlyLoad != null) && !onlyLoad.contains(channelName))
                continue;


            programme.setChannel(channelName);

            programme
                    .setStart(epgDateConverter
                            .ToLocalDateTime(node.getAttributes()
                                    .getNamedItem("start")
                                    .getNodeValue()
                            )
                    );


            programme
                    .setStop(epgDateConverter
                            .ToLocalDateTime(node.getAttributes()
                                    .getNamedItem("stop")
                                    .getNodeValue()
                            )
                    );


            NodeList childnodes = node.getChildNodes();

            for (int j = 0; j < childnodes.getLength(); j++) {

                Node child = childnodes.item(j);

                if (child.getNodeName().equalsIgnoreCase("title")) {

                    if (child.getAttributes().getNamedItem("lang").getNodeValue().equals("pl") && programme.getTitlePl() == null) {
                        programme.setTitlePl(child.getTextContent());
                    } else if (child.getAttributes().getNamedItem("lang").getNodeValue().equals("en") && programme.getTitleEn() == null) {
                        programme.setTitleEn(child.getTextContent());
                    } else if (child.getAttributes().getNamedItem("lang").getNodeValue().equals("xx") && programme.getTitleXx() == null) {
                        programme.setTitleXx(child.getTextContent());
                    } else {
                        log.info("Title: new language code found: " + programme.getChannel() + ", " + programme.getStart() + " :: " + child.getAttributes().getNamedItem("lang").getNodeValue());
                    }
                }
                if (child.getNodeName().equalsIgnoreCase("sub-title")) {
                    if (child.getAttributes().getNamedItem("lang").getNodeValue().equals("pl") && programme.getSubtitlePl() == null) {
                        programme.setSubtitlePl(child.getTextContent());
                    } else {
                        log.info("Sub-title: new language code found: " + programme.getChannel() + ", " + programme.getStart() + " :: " + child.getAttributes().getNamedItem("lang").getNodeValue());
                    }
                }
                if (child.getNodeName().equalsIgnoreCase("desc")) {
                    if (child.getAttributes().getNamedItem("lang").getNodeValue().equals("pl") && programme.getDescPl() == null) {
                        programme.setDescPl(child.getTextContent());
                    } else {
                        log.info("Desc: new language code found: " + programme.getChannel() + ", " + programme.getStart() + " :: " + child.getAttributes().getNamedItem("lang").getNodeValue());
                    }
                }

                if (child.getNodeName().equalsIgnoreCase("credits")) {
                    for (int k = 0; k < child.getChildNodes().getLength(); k++) {
                        if (child.getChildNodes().item(k).getNodeName().equalsIgnoreCase("director"))
                            programme.addDirector(child.getChildNodes().item(k).getTextContent());
                        if (child.getChildNodes().item(k).getNodeName().equalsIgnoreCase("actor"))
                            programme.addActor(child.getChildNodes().item(k).getTextContent());
                    }
                }

                if (child.getNodeName().equalsIgnoreCase("date")) {
                    if (NumberUtils.isDigits(child.getTextContent()))
                        programme.setDate(Integer.parseInt(child.getTextContent()));
                }

                if (child.getNodeName().equalsIgnoreCase("category")) {
                    programme.addCategoryPl(child.getTextContent());
                }

                if (child.getNodeName().equalsIgnoreCase("episode-num")) {

                    if (child.getAttributes().getNamedItem("system").getNodeValue().equals("xmltv_ns")) {
                        programme.setEpisodeXmlNs(child.getTextContent());
                    } else if (child.getAttributes().getNamedItem("system").getNodeValue().equals("onscreen")) {
                        programme.setEpisodeXmlNs(child.getTextContent());
                    } else {
                        log.info("New episode number format found: " + child.getAttributes().getNamedItem("system").getNodeValue());
                    }
                }

                if (child.getNodeName().equalsIgnoreCase("country")) {
                    programme.addCountry(child.getTextContent());
                }
            }

            if (programmeDao.findByChannelAndDate(programme.getChannel(), programme.getStart(), programme.getStop()).isEmpty()) {

                try {
                    programmeDao.save(programme);
                } catch (javax.persistence.PersistenceException e) {
                    log.error("SQL transaction error: " + e);
                }
            } else {
                log.warn("PROGRAMME table: duplicate ID " + programme.getStart() + programme.getChannel());
            }
        }
        log.info("Number of programmes in XML file: " + channelsList.getLength());
    }
}
