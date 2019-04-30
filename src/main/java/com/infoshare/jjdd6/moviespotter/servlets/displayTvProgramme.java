package com.infoshare.jjdd6.moviespotter.servlets;

import com.infoshare.jjdd6.moviespotter.dao.ProgrammeDao;
import com.infoshare.jjdd6.moviespotter.freemarker.TemplateProvider;
import com.infoshare.jjdd6.moviespotter.models.Programme;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet ("/programme")
public class displayTvProgramme extends HttpServlet {

    @Inject
    TemplateProvider templateProvider;

    @Inject
    ProgrammeDao programmeDao;

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        List<Programme> programmeList = programmeDao.findByChannel("Discovery HD");
        programmeList.addAll(programmeDao.findByChannel("TNT"));
        programmeList.addAll(programmeDao.findByChannel("Comedy Central"));

        List<String> channels = new ArrayList<>();
        channels.add("Discovery HD");
        channels.add("TNT");
        channels.add("Comedy Central");

        Map<String, Object> model = new HashMap<>();
            model.put("programmes", programmeList);
            model.put("channels", channels);


        log.info("programmes/model has entries: "+model.size());


        Template template = templateProvider.getTemplate(getServletContext(), "programme3cols.ftlh");

        log.info("using freemarker template: "+template.getDefaultNS());

        try {
            template.process(model, response.getWriter());
        } catch (TemplateException e) {
            log.error("Error processing template: "+e);
        }

    }

}
