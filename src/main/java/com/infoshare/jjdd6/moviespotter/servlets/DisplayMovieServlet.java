package com.infoshare.jjdd6.moviespotter.servlets;

import com.infoshare.jjdd6.moviespotter.freemarker.TemplateProvider;
import com.infoshare.jjdd6.moviespotter.services.FilmWebBrowser;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import info.talacha.filmweb.models.Film;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/movie/detail")
public class DisplayMovieServlet extends HttpServlet {

    @Inject
    TemplateProvider templateProvider;

    @Inject
    FilmWebBrowser filmWebBrowser;

    private Logger log = LoggerFactory.getLogger(this.getClass().getName());

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Long id = (NumberUtils.toLong(request.getParameter("fwID"), 0));

        Map<String, Object> model = new HashMap<>();

        Film film = filmWebBrowser.getFilmInfo(id);

        if (film != null) {
            model.put("m_movie", film);
            model.put("m_persons", filmWebBrowser.getFilmPersons(id));
        }

        Template template = templateProvider.getTemplate(getServletContext(), "movieDetails.ftlh");

        log.info("using freemarker template: " + template.getName());

        try {
            template.process(model, response.getWriter());
        } catch (TemplateException e) {
            log.error("Error processing template: " + e);
        }
    }
}
