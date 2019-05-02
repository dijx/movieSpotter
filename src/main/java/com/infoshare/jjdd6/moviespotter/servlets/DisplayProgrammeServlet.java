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
public class DisplayProgrammeServlet extends HttpServlet {

    @Inject
    TemplateProvider templateProvider;

    @Inject
    ProgrammeDao programmeDao;

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        List <Programme> programmeList = programmeDao.findByChannel(request.getParameter("ch1"));
        programmeList.addAll(programmeDao.findByChannel(request.getParameter("ch2")));
        programmeList.addAll(programmeDao.findByChannel(request.getParameter("ch3")));

        List<String> channels = new ArrayList<>();
        channels.add(request.getParameter("ch1"));
        channels.add(request.getParameter("ch2"));
        channels.add(request.getParameter("ch3"));

        Map<String, Object> model = new HashMap<>();
            model.put("programmes", programmeList);
            model.put("channels", channels);

        log.info("programmes/model has entries: "+model.size());


        Template template = templateProvider.getTemplate(getServletContext(), "programme3cols.ftlh");

        log.info("using freemarker template: "+template.getName());

        try {
            template.process(model, response.getWriter());
        } catch (TemplateException e) {
            log.error("Error processing template: "+e);
        }
    }
}
